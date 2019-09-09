package com.translation.util.exception;

public class FileNameErrorException extends RuntimeException {

    public FileNameErrorException() {
        super("文件名称错误！");
    }

    public FileNameErrorException(String message) {
        super(message);
    }

}
