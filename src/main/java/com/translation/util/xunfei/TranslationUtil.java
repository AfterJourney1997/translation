package com.translation.util.xunfei;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.entity.ProgramStatus;
import com.translation.util.dto.ApiResultDto;
import com.translation.task.TaskQueueOperate;
import com.translation.util.beanFactory.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.yarn.webapp.hamlet2.Hamlet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.*;

@Slf4j
@Component
@ConfigurationProperties("xunfei")
public class TranslationUtil {

    private static String TRANSLATION_URL;
    private static String APP_ID;
    private static String SECRET_KEY;

    private static final String PREPARE = "/prepare";
    private static final String UPLOAD = "/upload";
    private static final String MERGE = "/merge";
    private static final String GET_RESULT = "/getResult";
    private static final String GET_PROGRESS = "/getProgress";
    private static final int REMOTE_TRANSLATION_SUCCESS = 9;

    // 文件分片大小
    private static final int SLICE_SIZE = 10485760;//10M

    public void setUrl(String url) {
        TRANSLATION_URL = url;
    }

    public void setAppId(String appId) {
        APP_ID = appId;
    }

    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
    }


    // 上传文件获取taskId
    public static String updateFileAndGetTaskId(InputStream inputStream, String fileName) {
        String taskId = null;
        try {
            long fileLength = inputStream.available();
            log.info("文件大小:{}", fileLength);
            // 预处理
            taskId = prepare(fileLength, fileName);
            // 分片上传文件
            int len;
            byte[] slice = new byte[SLICE_SIZE];
            SliceIdGenerator generator = new SliceIdGenerator();
            while ((len = inputStream.read(slice)) > 0) {
                // 上传分片
                if (inputStream.available() == 0) {
                    slice = Arrays.copyOfRange(slice, 0, len);
                }
                uploadSlice(taskId, generator.getNextSliceId(), slice);
            }
            log.info("文件上传完成");
            // 合并文件
            merge(taskId);
            TaskQueueOperate.addTask(taskId);
        } catch (IOException | SignatureException e) {
            e.printStackTrace();
        }
        return taskId;

    }

    public static void scanQueue(String taskId) throws SignatureException {
        // 获取转译过程信息
        ApiResultDto taskProgress = TranslationUtil.getProgress(taskId);
        if (taskProgress.getOk() == 0) {
            // Err_no非0即为转译失败
            if (taskProgress.getErr_no() != 0) {
                // 修改库中program状态
                ApplicationContextProvider.getBean(ProgramMapper.class).updateStatus("error", ProgramStatus.TRANSLATION_FAIL.getCode());
                // 任务失败，将任务从队列移除
                log.info(taskId + " 任务失败：{}", taskProgress);
                TaskQueueOperate.removeTask(taskId);
            }
            // 获取任务状态
            String taskStatus = taskProgress.getData();
            // 状态码为9即为转译完成
            if (JSON.parseObject(taskStatus).getInteger("status") == REMOTE_TRANSLATION_SUCCESS) {
                String resultData = TranslationUtil.getResult(taskId);
                updateContent(resultData, taskId);
                // 任务成功，将任务从队列移除
                log.info(taskId + " 任务完成：{}", taskProgress);
                TaskQueueOperate.removeTask(taskId);
                return;
            }
            log.info("任务Id： " + taskId + " 仍在转译中......");
        } else {
            log.info("获取任务进度失败，taskId： {} resultData： {}", taskId, taskProgress);
        }
    }

    /**
     * 预处理
     *
     * @param fileLength 文件大小
     * @param fileName   文件名
     * @return taskId
     */
    private static String prepare(long fileLength, String fileName) throws SignatureException {
        Map<String, String> prepareParam = getBaseAuthParam(null);
        prepareParam.put("file_len", Long.toString(fileLength));
        prepareParam.put("file_name", fileName);
        prepareParam.put("slice_num", (fileLength / SLICE_SIZE) + (fileLength % SLICE_SIZE == 0 ? 0 : 1) + "");


        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + PREPARE, prepareParam);
        Optional.ofNullable(response).orElseThrow(() -> new RuntimeException("预处理接口请求失败！"));
        ApiResultDto resultDto = JSON.parseObject(response, ApiResultDto.class);
        String taskId = resultDto.getData();
        Validator.validateTrue(resultDto.getOk() == 0, "预处理失败" + resultDto.toString());
        log.info("预处理成功, taskId：" + taskId);
        return taskId;
    }


    /**
     * 获取每个接口都必须的鉴权参数
     *
     * @return 请求接口基础参数
     */
    private static Map<String, String> getBaseAuthParam(String taskId) throws SignatureException {
        Map<String, String> baseParam = new HashMap<>();
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        baseParam.put("app_id", APP_ID);
        baseParam.put("ts", ts);
        baseParam.put("signa", EncryptUtil.HmacSHA1Encrypt(Objects.requireNonNull(EncryptUtil.MD5(APP_ID + ts)), SECRET_KEY));
        Optional.ofNullable(taskId).ifPresent(v -> baseParam.put("task_id", v));
        return baseParam;
    }


    /**
     * 分片上传
     *
     * @param taskId 任务id
     * @param slice  分片的byte数组
     */
    private static void uploadSlice(String taskId, String sliceId, byte[] slice) throws SignatureException {
        Map<String, String> uploadParam = getBaseAuthParam(taskId);
        uploadParam.put("slice_id", sliceId);

        String response = com.translation.util.xunfei.HttpUtil.postMulti(TRANSLATION_URL + UPLOAD, uploadParam, slice);
        Optional.ofNullable(response).orElseThrow(() -> new RuntimeException("分片上传接口请求失败"));
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            log.info("分片上传成功, sliceId: " + sliceId + ", sliceLen: " + slice.length);
            return;
        }

        log.info("params: " + JSON.toJSONString(uploadParam));
        throw new RuntimeException("分片上传失败！" + response + "|" + taskId);
    }

    /**
     * 文件合并
     *
     * @param taskId 任务id
     */
    private static void merge(String taskId) throws SignatureException {
        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + MERGE, getBaseAuthParam(taskId));
        Optional.ofNullable(response).orElseThrow(() -> new RuntimeException("文件合并接口请求失败!"));
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            log.info("文件合并成功, taskId: " + taskId);
            return;
        }
        throw new RuntimeException("文件合并失败！" + response);
    }

    /**
     * 获取任务进度
     *
     * @param taskId 任务id
     */
    public static ApiResultDto getProgress(String taskId) throws SignatureException {
        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + GET_PROGRESS, getBaseAuthParam(taskId));
        Optional.ofNullable(response).orElseThrow(() -> new RuntimeException("获取任务进度接口请求失败！"));
        return JSON.parseObject(response, ApiResultDto.class);
    }

    /**
     * 获取转写结果
     *
     * @param taskId taskId
     * @return json文档
     */
    public static String getResult(String taskId) throws SignatureException {
        String responseStr = HttpUtil.post(TRANSLATION_URL + GET_RESULT, getBaseAuthParam(taskId));
        Optional.ofNullable(responseStr).orElseThrow(() -> new RuntimeException("获取结果接口请求失败！"));
        ApiResultDto response = JSON.parseObject(responseStr, ApiResultDto.class);
        Validator.validateTrue(response.getOk() == 0, "获取结果失败" + response);
        return response.getData();
    }

    /**
     * 将转写结果更新数据库
     *
     * @param jsonData 从讯飞拿的json文档
     * @param taskId   根据taskId标识数据库那些数据需要更新
     */
    public static void updateContent(String jsonData, String taskId) {
        JSONArray jsonArray = JSONArray.parseArray(jsonData);
        JSONObject jsonObject = (JSONObject) jsonArray.remove(0);
        System.out.println(jsonObject);
        System.out.println(jsonObject.getString("bg"));
        ProgramMapper programMapper = ApplicationContextProvider.getBean(ProgramMapper.class);
        Program program = programMapper.selectByTaskId(taskId);
        program.setBg(Integer.parseInt(jsonObject.getString("bg")));
        program.setEd(Integer.parseInt(jsonObject.getString("ed")));
        program.setContent(jsonObject.getString("onebest"));
        program.setStatus(ProgramStatus.TRANSLATION_SUCCESS.getCode());
        programMapper.updateByPrimaryKey(program);
        if (jsonArray.size() > 0) {
            List<Program> programList = new ArrayList<>(jsonArray.size());
            for (Object o : jsonArray) {
                jsonObject = (JSONObject) o;
                Program newProgram = new Program();
                BeanUtil.copyProperties(program, newProgram);
                newProgram.setBg(Integer.parseInt(jsonObject.getString("bg")));
                newProgram.setEd(Integer.parseInt(jsonObject.getString("ed")));
                newProgram.setContent(jsonObject.getString("onebest"));
                newProgram.setStatus(ProgramStatus.TRANSLATION_SUCCESS.getCode());
                programList.add(newProgram);
            }
            programMapper.insertList(programList);
        }
    }

    // 获取讯飞result中的转译内容
    private static String getContentFromJSON(String json) {
        JSONArray jsonArray = JSONArray.parseArray(json);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            result.append(jsonObject.getString("onebest"));
        }
        return result.toString();
    }
}
