package com.translation.controller;

import com.translation.aop.ResultBean;
import com.translation.aop.ResultStatus;
import com.translation.service.FileService;
import com.translation.service.InfoService;
import com.translation.util.exception.FileNameErrorException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping(value = "/file")
@Api(tags = "文件操作接口", value = "FileController")
public class FileController {

    private static String filePath;

    private final FileService fileService;
    private final InfoService infoService;

    @Value("{hdfs.filePath}")
    private void setFilePath(String filePath){
        FileController.filePath = filePath;
        System.out.println();
    }

    @Autowired
    public FileController(FileService fileService, InfoService infoService) {
        this.fileService = fileService;
        this.infoService = infoService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "文件上传", notes = "参数包括：上传文件")
    public Object uploadFile(@RequestBody @NonNull @RequestParam(name = "file")MultipartFile[] file) throws IOException {

        for(MultipartFile audio : file){

            if(audio.isEmpty()){
                continue;
            }

            // 获取节目信息入库并开始转译
            try {
                infoService.insertInfo(audio);
            } catch (FileNameErrorException e) {
                e.printStackTrace();
                return new ResultBean<>(ResultStatus.FILE_NAME_ERROR.getCode(), ResultStatus.FILE_NAME_ERROR.getMessage());
            }

            // 上传进hdfs
            fileService.uploadFile(audio, filePath);
        }

        return new ResultBean<>();

    }

}
