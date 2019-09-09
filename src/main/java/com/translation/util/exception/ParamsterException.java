package com.translation.util.exception;

public class ParamsterException extends RuntimeException {
    public ParamsterException() {
        super("参数异常");
    }

    public ParamsterException(String message) {
        super(message);
    }
}
