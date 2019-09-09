//package com.translation.entity;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import lombok.Data;
//
//import java.io.Serializable;
//import java.time.LocalDate;
//
//@Data
//public class Conversation implements Serializable {
//    private Integer id;
//    private Integer father;
//    private String frequency;
//    private String program;
//    private LocalDate airtime;
//    private String content;
//    private Integer status = 0;
//    private int bg;
//    private int ed;
//
//    public Conversation() {
//
//    }
//
//    public Conversation(Program program) {
//        id = program.getId();
//        father = program.getFather();
//        frequency = program.getFrequency();
//        this.program = program.getProgram();
//        airtime = program.getAirtime();
//        JSONObject jsonObject = JSON.parseObject(program.getContent());
//        this.bg = Integer.parseInt(jsonObject.getString("bg"));
//        this.ed = Integer.parseInt(jsonObject.getString("ed"));
//        this.content = (jsonObject.getString("onebest"));
//    }
//}
