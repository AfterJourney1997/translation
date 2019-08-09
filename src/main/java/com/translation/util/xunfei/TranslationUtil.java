package com.translation.util.xunfei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.util.dto.ApiResultDto;
import com.translation.task.TaskQueueOperate;
import com.translation.util.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class TranslationUtil {

    private static String TRANSLATION_URL;
    private static String APP_ID;
    private static String SECRET_KEY;

    private static final String PREPARE = "/prepare";
    private static final String UPLOAD = "/upload";
    private static final String MERGE = "/merge";
    private static final String GET_RESULT = "/getResult";
    private static final String GET_PROGRESS = "/getProgress";

    // 文件分片大小
    private static final int SLICE_SICE = 20971520;

    @Value("${TRANSLATION_URL}")
    private void setTRANSLATION_URL(String TRANSLATION_URL){
        TranslationUtil.TRANSLATION_URL = TRANSLATION_URL;
    }

    @Value("${APP_ID}")
    private void setAPP_ID(String APP_ID){
        TranslationUtil.APP_ID = APP_ID;
    }

    @Value("${SECRET_KEY}")
    private void setSECRET_KEY(String SecretKey){
        TranslationUtil.SECRET_KEY = SecretKey;
    }

    // 上传文件获取taskId
    public static String updateFileAndGetTaskId(Program program, MultipartFile file) {

        try {
            InputStream fis = file.getInputStream();

            // 预处理
            String taskId = prepare(file);

            // 分片上传文件
            int len;
            byte[] slice = new byte[SLICE_SICE];
            SliceIdGenerator generator = new SliceIdGenerator();
            while ((len =fis.read(slice)) > 0) {
                // 上传分片
                if (fis.available() == 0) {
                    slice = Arrays.copyOfRange(slice, 0, len);
                }
                uploadSlice(taskId, generator.getNextSliceId(), slice);
            }

            // 合并文件
            merge(taskId);

            // 存入taskId
            program.setContent(taskId);

            // 任务id入队
            TaskQueueOperate.addTask(program);
            log.info("获取taskId成功，加入任务队列：{}", program);

            return taskId;

        } catch (IOException | SignatureException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void scanQueue(Program program) throws SignatureException {

        String taskId = program.getContent();

        // 获取转译过程信息
        ApiResultDto taskProgress = TranslationUtil.getProgress(taskId);
        if (taskProgress.getOk() == 0) {
            // Err_no非0即为转译失败
            if (taskProgress.getErr_no() != 0) {

                // 修改库中program状态
                program.setStatus(-1);
                program.setContent(null);
                ApplicationContextProvider.getBean(ProgramMapper.class).updateByPrimaryKey(program);

                // 任务失败，将任务从队列移除
                log.info(taskId + " 任务失败：{}", taskProgress);
                TaskQueueOperate.removeTask(program);
                return;
            }

            // 获取任务状态
            String taskStatus = taskProgress.getData();
            // 状态码为9即为转译完成
            if (JSON.parseObject(taskStatus).getInteger("status") == 9) {

                // 获取结果，修改库中program信息及状态
                String result = getContentFromJSON(TranslationUtil.getResult(taskId));
                program.setStatus(1);
                program.setContent(result);
                log.info("================{}",result);
                ApplicationContextProvider.getBean(ProgramMapper.class).updateByContentTaskId(taskId, program);

                // 任务成功，将任务从队列移除
                log.info(taskId + " 任务完成：{}", taskProgress);
                TaskQueueOperate.removeTask(program);
                return;
            }

            log.info(taskId + " 仍在转译中......");
        }else{
            log.info(taskId + " 获取任务进度失败！");
        }

    }

    /**
     * 获取每个接口都必须的鉴权参数
     *
     * @return
     * @throws SignatureException
     */
    private static Map<String, String> getBaseAuthParam(String taskId) throws SignatureException {
        Map<String, String> baseParam = new HashMap<>();
        String ts = String.valueOf(System.currentTimeMillis() / 1000L);
        baseParam.put("app_id", APP_ID);
        baseParam.put("ts", ts);
        baseParam.put("signa", EncryptUtil.HmacSHA1Encrypt(Objects.requireNonNull(EncryptUtil.MD5(APP_ID + ts)), SECRET_KEY));
        if (taskId != null) {
            baseParam.put("task_id", taskId);
        }

        return baseParam;
    }

    /**
     * 预处理
     *
     * @param audio     需要转写的音频
     * @return
     * @throws SignatureException
     */
    private static String prepare(MultipartFile audio) throws SignatureException {
        Map<String, String> prepareParam = getBaseAuthParam(null);
        long fileLenth = audio.getSize();

        prepareParam.put("speaker_number", "1");
        prepareParam.put("file_len", fileLenth + "");
        prepareParam.put("file_name", audio.getOriginalFilename());
        prepareParam.put("slice_num", (fileLenth/SLICE_SICE) + (fileLenth % SLICE_SICE == 0 ? 0 : 1) + "");

        // 转写类型
//        prepareParam.put("lfasr_type", "0");
        // 开启分词
//        prepareParam.put("has_participle", "true");
        // 说话人分离
//        prepareParam.put("has_seperate", "true");
        // 设置多候选词个数
//        prepareParam.put("max_alternatives", "2");
        // 是否进行敏感词检出
//        prepareParam.put("has_sensitive", "true");
        // 敏感词类型
//        prepareParam.put("sensitive_type", "1");
        // 关键词
//        prepareParam.put("keywords", "科大讯飞,中国");

        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + PREPARE, prepareParam);
        if (response == null) {
            throw new RuntimeException("预处理接口请求失败！");
        }
        ApiResultDto resultDto = JSON.parseObject(response, ApiResultDto.class);
        String taskId = resultDto.getData();
        if (resultDto.getOk() != 0 || taskId == null) {
            throw new RuntimeException("预处理失败！" + response);
        }

        log.info("预处理成功, taskId：" + taskId);
        return taskId;
    }

    /**
     * 分片上传
     *
     * @param taskId        任务id
     * @param slice         分片的byte数组
     * @throws SignatureException
     */
    private static void uploadSlice(String taskId, String sliceId, byte[] slice) throws SignatureException {
        Map<String, String> uploadParam = getBaseAuthParam(taskId);
        uploadParam.put("slice_id", sliceId);

        String response = com.translation.util.xunfei.HttpUtil.postMulti(TRANSLATION_URL + UPLOAD, uploadParam, slice);
        if (response == null) {
            throw new RuntimeException("分片上传接口请求失败！");
        }
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
     * @param taskId        任务id
     * @throws SignatureException
     */
    private static void merge(String taskId) throws SignatureException {
        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + MERGE, getBaseAuthParam(taskId));
        if (response == null) {
            throw new RuntimeException("文件合并接口请求失败！");
        }
        if (JSON.parseObject(response).getInteger("ok") == 0) {
            log.info("文件合并成功, taskId: " + taskId);
            return;
        }

        throw new RuntimeException("文件合并失败！" + response);
    }

    /**
     * 获取任务进度
     *
     * @param taskId        任务id
     * @throws SignatureException
     */
    private static ApiResultDto getProgress(String taskId) throws SignatureException {
        String response = com.translation.util.xunfei.HttpUtil.post(TRANSLATION_URL + GET_PROGRESS, getBaseAuthParam(taskId));
        if (response == null) {
            throw new RuntimeException("获取任务进度接口请求失败！");
        }

        return JSON.parseObject(response, ApiResultDto.class);
    }

    /**
     * 获取转写结果
     *
     * @param taskId
     * @return
     * @throws SignatureException
     */
    private static String getResult(String taskId) throws SignatureException {
        String responseStr = HttpUtil.post(TRANSLATION_URL + GET_RESULT, getBaseAuthParam(taskId));
        if (responseStr == null) {
            throw new RuntimeException("获取结果接口请求失败！");
        }
        ApiResultDto response = JSON.parseObject(responseStr, ApiResultDto.class);
        if (response.getOk() != 0) {
            throw new RuntimeException("获取结果失败！" + responseStr);
        }

        return response.getData();
    }

    // 获取讯飞result中的转译内容
    private static String getContentFromJSON(String json){

        JSONArray jsonArray = JSONArray.parseArray(json);
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            result.append(jsonObject.getString("onebest"));
        }

        return result.toString();
    }


}
