package com.translation.service;

import com.translation.dao.ProgramMapper;
import com.translation.entity.HDFSFile;
import com.translation.entity.Program;
import com.translation.entity.ProgramStatus;
import com.translation.util.ConvertUtil;
import com.translation.util.ProgramUtil;
import com.translation.util.xunfei.TranslationUtil;
import lombok.Setter;
import org.apache.hadoop.fs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@Service
public class FileService {
    //    @Setter
//    private String filePath;
    @Setter
    private String filePathNoProgram;
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private ProgramMapper programMapper;

    /**
     * 上传文件 自动拼接文件id
     *
     * @param inputStream inputStream
     * @param filePath    文件全路径
     */
    public void uploadFileProgram(InputStream inputStream, String filePath) {
        int field = programMapper.selectMaxFileId() + 1;
        String[] elems = filePath.split("\\.");
        String path = elems[0] + "_" + field + "." + elems[1];
        Path upPath = new Path(path);
        try {
            OutputStream outputStream = fileSystem.create(upPath);
            ConvertUtil.toOutputStream(inputStream, outputStream);
            elems = filePath.split("/");
            //            将文件id和栏目信息保存至数据库
            insertProgram(elems[elems.length - 1], field);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据hdfs文件id去找文件全路径，下载到内存进行转写
     *
     * @param id
     * @return
     */
    public HDFSFile download(int id) {
        HDFSFile hdfsFile = null;
        try {
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path("/program"));
            for (FileStatus fileStatus : fileStatuses) {
                String fileName = fileStatus.getPath().getName();
                String idStr = fileName.substring(fileName.lastIndexOf("_")).split("\\.")[0].substring(1);
                if (Integer.parseInt(idStr) == id) {
                    hdfsFile = download(fileStatus.getPath().toString());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hdfsFile;
    }

    /**
     * @param filePath 文件全路径 eg:/program/xxx.mp3
     * @return InputStream
     */
    public HDFSFile download(String filePath) {
        Path path = new Path(filePath);
        InputStream inputStream;
        HDFSFile hdfsFile = null;
        try {
            inputStream = fileSystem.open(path);
            hdfsFile = new HDFSFile(inputStream, path.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hdfsFile;
    }


    public boolean exists(String fileName) {
        Path path = new Path(filePathNoProgram + "/" + fileName);
        boolean flag = false;
        try {
            flag = fileSystem.exists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public void updateTaskId(String taskId, int fileId) {
        programMapper.updateTaskId(taskId, fileId);
    }

    /**
     * @param fileName 标准文件名 eg:1061_20190129_0900_1100.mp3
     * @param fileId   fileId
     */
    private void insertProgram(String fileName, int fileId) {
        Program program = ProgramUtil.findProgram(fileName);
        program.setFileId(fileId);
        program.setStatus(ProgramStatus.TRANSLATION_WAIT.getCode());
        programMapper.insert(program);
    }

}
