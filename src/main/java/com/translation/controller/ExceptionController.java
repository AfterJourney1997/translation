package com.translation.controller;

import com.translation.aop.ResultBean;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exception(Exception e) {
        return new ResultBean<>(-1, e.getMessage());
    }
}
