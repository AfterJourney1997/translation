package com.translation.util;

import cn.hutool.core.io.file.FileReader;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.translation.entity.Program;
import com.translation.util.exception.FileNameErrorException;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FindProgramUtil {

    public static Program findProgram(String fileName) throws FileNameErrorException {

        // 拆分fileName
        String[] fileNames = fileName.split("_");

        // 文件名拆分数组长度如不为4，即说明文件名错误
        if(fileNames.length != 4){
            throw new FileNameErrorException();
        }

        Program program = new Program();
        program.setStatus(0);

        // 读取program.json文件到String
        File file = new File(FindProgramUtil.class.getResource("/programs.json").getPath());
        FileReader fileReader = new FileReader(file);
        String jsonString = fileReader.readString();

        // 删去.txt文件后缀
        fileNames[3] = fileNames[3].substring(0, fileNames[3].length() - 4);
        // 给时间添加冒号
        fileNames[2] = FindProgramUtil.addColon(fileNames[2]);
        fileNames[3] = FindProgramUtil.addColon(fileNames[3]);

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
                        return program;
                    }
                }

            }
        }

        return program;
    }

    // 用于给0900添加一个冒号，成为09:00
    private static String addColon(String time) {

        return time.substring(0, 2) + ":" + time.substring(2);

    }

}
