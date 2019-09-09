package com.translation.controller;

import com.translation.aop.ResultBean;
import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RequestMapping("/test")
@RestController
public class TestController {
    @Autowired
    private ProgramMapper programMapper;

    @RequestMapping("/search")
    public Object search() {
        List<Program> programList = programMapper.test();
        return new ResultBean<>(programList);
    }
}
