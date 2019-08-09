package com.translation.controller;

import com.translation.aop.ResultBean;
import com.translation.entity.Program;
import com.translation.service.InfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/program")
@Api(tags = "节目信息操作接口", value = "InfoController")
public class InfoController {

    private final InfoService infoService;

    @Autowired
    public InfoController(InfoService infoService) {
        this.infoService = infoService;
    }

    @RequestMapping(value = "/allProgram", method = RequestMethod.GET)
    @ApiOperation(value = "查询全部节目信息", notes = "参数包括：无")
    public Object getAllProgram(){

        List<Program> allProgramList = infoService.getAllProgram();

        return new ResultBean<>(allProgramList);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "修改指定节目信息", notes = "参数包括：id、内容")
    public Object updateProgram(@RequestBody @NonNull Program program){

        infoService.updateProgram(program);

        return new ResultBean<>();
    }

}
