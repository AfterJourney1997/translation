package com.translation.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author wzh Date 2019/1/31 14:29
 * @version 1.0
 **/

@Component
public class FileSystemUtil {

    private static String path;
    private static String userName;

    // 注入配置文件中的值
    @Value("${hdfs.path}")
    private void setPath(String path){
        FileSystemUtil.path = path;
    }

    @Value("${hdfs.username}")
    private void setUsername(String userName){
        FileSystemUtil.userName = userName;
    }

    public static FileSystem getFileSystem() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", path);
        try {
            return FileSystem.get(new URI(path), configuration, userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
