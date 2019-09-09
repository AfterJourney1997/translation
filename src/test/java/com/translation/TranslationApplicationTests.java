package com.translation;

import cn.hutool.core.io.IoUtil;
import com.translation.dao.ProgramMapper;
import com.translation.service.FileService;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TranslationApplicationTests {


    @Autowired
    private ProgramMapper programMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileSystem fileSystem;


    @Test
    public void contextLoads() throws Exception {
        InputStream inputStream = new FileInputStream("E:\\广电\\源文件\\1061_20190129_0900_1100.mp3");
        OutputStream outputStream = fileSystem.create(new Path("/program/1061_20190129_0900_1100_2.mp3"));
        IoUtil.copy(inputStream, outputStream);
    }

    public void upload() throws IOException {
        File file = new File("E:\\广电\\源文件\\1061_20190129_1400_1700.mp3");
        InputStream input = new FileInputStream(file);
        byte[] byt = new byte[input.available()];
        input.read(byt);
        OutputStream outputStream = fileSystem.create(new Path("/program/" + file.getName()));
        outputStream.write(byt);
        outputStream.close();
    }

    public void listFile() {
        try {
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path("/program"));
            for (FileStatus fileStatus : fileStatuses) {
                Path path = fileStatus.getPath();
                System.out.println(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
