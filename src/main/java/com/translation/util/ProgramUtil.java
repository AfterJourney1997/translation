package com.translation.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.util.exception.FileNameErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * 广电频率工具类
 */
@Component
public class ProgramUtil {
    private final ProgramMapper programMapper;

    private static ProgramMapper programMapperStatic;

    public ProgramUtil(ProgramMapper programMapper) {
        this.programMapper = programMapper;
    }

    @PostConstruct
    public void init() {
        programMapperStatic = programMapper;
    }

    public static Program findProgram(String fileName) throws FileNameErrorException {

        // 拆分fileName
        String[] fileNames = fileName.split("_");

        // 文件名拆分数组长度如不为4，即说明文件名错误
        if (fileNames.length != 4) {
            throw new FileNameErrorException(fileName);
        }

        Program program = new Program();
        // 读取program.json文件到String
        InputStream inputStream;
        try {
            inputStream = ProgramUtil.class.getResource("/programs.json").openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String jsonString = IoUtil.read(inputStream, Charset.defaultCharset());

        // 删去.txt文件后缀
        fileNames[3] = fileNames[3].substring(0, fileNames[3].length() - 4);
        // 给时间添加冒号
        fileNames[2] = ProgramUtil.addColon(fileNames[2]);
        fileNames[3] = ProgramUtil.addColon(fileNames[3]);

        // 存储音频产生时间
        // Date date = DateUtil.parse(fileNames[1], "yyyyMMdd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(fileNames[1], dateTimeFormatter);
        program.setAirtime(date);

        JSONArray jsonArray = JSONArray.parseArray(jsonString);

        for (int i = 0; i < jsonArray.size(); i++) {
            // 读取jsonArray里的Object
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // 匹配频率的拼音简写
            if (fileNames[0].equals(jsonObject.getString("streamName"))) {
                // 存储频率名称
                program.setFrequency(jsonObject.getString("name"));
                JSONArray programArray = JSONArray.parseArray(jsonObject.getString("programs"));

                for (int j = 0; j < programArray.size(); j++) {

                    // 读取programArray里的Object
                    JSONObject programObject = programArray.getJSONObject(j);
                    // 匹配节目开始时间和结束时间
                    if (fileNames[2].equals(programObject.getString("starttime")) &&
                            fileNames[3].equals(programObject.getString("endtime"))) {
                        program.setProgram(programObject.getString("name"));
                        program.setFather(jsonObject.getInteger("id"));
                        return program;
                    }
                }

            }
        }

        return program;
    }

    /**
     * 根据频率名称查询频率对应的主键id
     *
     * @param frequencyName 频率名称
     * @return 返回0表示频率名称不存在
     */
    public static int getFrequencyId(String frequencyName) {
        List<Program> programs = programMapperStatic.selectFather();
        Stream<Integer> idStream = programs.stream()
                .filter((e) -> e.getFrequency().equals(frequencyName))
                .map(Program::getId);
        return idStream.findFirst().orElse(0);
    }

    // 用于给0900添加一个冒号，成为09:00
    private static String addColon(String time) {
        return time.substring(0, 2) + ":" + time.substring(2);

    }

}
