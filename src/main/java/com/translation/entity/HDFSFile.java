package com.translation.entity;

import java.io.InputStream;

/**
 * 对HDFS下载返回文件的薄封装
 */
public class HDFSFile {
    private final InputStream inputStream;
    /**
     * 全路径
     */
    private final String path;

    public HDFSFile(InputStream inputStream, String path) {
        this.inputStream = inputStream;
        this.path = path;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "HDFSFile{" +
                "inputStream=" + inputStream +
                ", path='" + path + '\'' +
                '}';
    }
}
