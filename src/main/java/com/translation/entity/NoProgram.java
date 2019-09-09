package com.translation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class NoProgram {
    private int id;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date insertTime;
    private String content;
    private int status;
}
