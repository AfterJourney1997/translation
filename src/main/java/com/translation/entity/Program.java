package com.translation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Program {

    private Integer id;
    private Integer father;
    private String frequency;
    private String program;
    private LocalDate airtime;
    private String content;
    private Integer status;

}
