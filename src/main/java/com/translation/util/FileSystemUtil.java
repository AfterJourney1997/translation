package com.translation.util;

import lombok.Setter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * @author wzh Date 2019/1/31 14:29
 * @version 1.0
 **/

@Component
@ConfigurationProperties("hdfs")
public class FileSystemUtil {

    @Setter
    private String path;
    @Setter
    private String userName;


    @Bean
    public FileSystem getFileSystem() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", path);
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(new URI(path), configuration, userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSystem;
    }
}
