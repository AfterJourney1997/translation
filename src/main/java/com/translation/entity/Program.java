package com.translation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Program implements Serializable {
    private Integer id;
    private Integer father;
    private String frequency;
    private String program;
    private LocalDate airtime;
    private String content;
    private Integer status = 0;
    private int bg;
    private int ed;
    private int fileId;
}


