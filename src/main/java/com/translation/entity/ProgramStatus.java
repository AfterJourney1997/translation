package com.translation.entity;

import lombok.Getter;

@Getter
public enum ProgramStatus {
    TRANSLATION_FAIL(-1), //转换失败
    TRANSLATION_SUCCESS(0), //转换成功
    TRANSLATION_PROCESS(1), //转换中
    TRANSLATION_WAIT(2);//音频已上传，等待转换
    private int code;

    ProgramStatus(int code) {
        this.code = code;
    }
}
