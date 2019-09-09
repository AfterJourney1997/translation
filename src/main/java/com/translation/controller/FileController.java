package com.translation.controller;


import cn.hutool.core.io.IoUtil;
import com.translation.aop.ResultBean;
import com.translation.entity.HDFSFile;
import com.translation.service.FileService;
import com.translation.util.ConvertUtil;
import com.translation.util.exception.ParamsterException;
import com.translation.util.xunfei.TranslationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping(value = "/file")
@Api(tags = "文件操作接口", value = "FileController")
public class FileController {
    @Resource
    private FileService fileService;


    @RequestMapping(value = "/file", method = RequestMethod.POST)
    @ApiOperation(value = "文件上传", notes = "参数包括：上传文件，文件名要求带栏目信息")
    public Object uploadFileProgram(@RequestBody MultipartFile[] file) throws IOException {
        Optional.ofNullable(file).orElseThrow(ParamsterException::new);
        for (MultipartFile audio : file) {
            if (audio.isEmpty()) {
                continue;
            }
            fileService.uploadFileProgram(audio.getInputStream(), "/program/" + audio.getOriginalFilename());
        }
        return new ResultBean<>();
    }

    @RequestMapping(value = "/translation/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "音频转写接口", notes = "参数包括：未转写的文件id")
    public Object translation(@PathVariable int id) throws IOException {
        HDFSFile hdfsFile = fileService.download(id);
        String[] elems = hdfsFile.getPath().split("/");
        InputStream hdfsStream = hdfsFile.getInputStream();
        InputStream inputStream = new ByteArrayInputStream(IoUtil.readBytes(hdfsStream));
        hdfsStream.close();
        String taskId = TranslationUtil.updateFileAndGetTaskId(inputStream, elems[elems.length - 1]);
        fileService.updateTaskId(taskId, id);
        return new ResultBean<>();
    }

    @RequestMapping(value = "/download/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public void download(HttpServletRequest request, HttpServletResponse response, @PathVariable int id) throws IOException {
        HDFSFile hdfsFile = fileService.download(id);
        Optional.ofNullable(hdfsFile).orElseThrow(() -> new RuntimeException("找不到音频文件"));
        InputStream inputStream = hdfsFile.getInputStream();
        OutputStream outputStream = response.getOutputStream();
        final long fileLen = inputStream.available();
        String range = request.getHeader("Range");
        if (range == null) {
            ConvertUtil.toOutputStream(inputStream, outputStream);
        } else {
            long start = Integer.parseInt(range.substring(range.indexOf("=") + 1, range.indexOf("-")));
            long end = range.endsWith("-") ? fileLen - 1 : Integer.parseInt(range.substring(range.indexOf("-") + 1));
            String ContentRange = "bytes " + start + "-" + end + "/" + fileLen;
            response.setStatus(206);
            response.setHeader("Content-Range", ContentRange);
            inputStream.skip(start);
            ConvertUtil.toOutputStream(inputStream, outputStream);
        }
    }


    @RequestMapping(value = "/noProgram", method = RequestMethod.POST)
    @ApiOperation(value = "文件上传", notes = "参数包括：上传文件，文件名不要求带栏目信息，上传之后立刻开始转写")
    public Object uploadFileNoProgram(@RequestBody MultipartFile[] file) throws IOException {
        Optional.ofNullable(file).orElseThrow(ParamsterException::new);
        for (MultipartFile audio : file) {
            if (audio.isEmpty()) {
                continue;
            }
            // 上传进hdfs
            fileService.uploadFileProgram(audio.getInputStream(), "/noProgram/" + audio.getOriginalFilename());
//            开始转写
            TranslationUtil.updateFileAndGetTaskId(audio.getInputStream(), audio.getOriginalFilename());
        }
        return new ResultBean<>();
    }

//    @RequestMapping(value = "/test", method = RequestMethod.GET)
//    @ApiOperation(value = "音频转写接口", notes = "参数包括：未转写的文件id")
//    public void test(HttpServletResponse response) throws IOException {
//        File file = new File("E:\\广电\\源文件\\1061_20190129_1400_1700.mp3");
//        InputStream inputStream = new FileInputStream(file);
//        OutputStream outputStream = response.getOutputStream();
//        byte[] slice = new byte[1024];
//        int len;
//        while ((len = inputStream.read(slice)) > 0) {
//            if (inputStream.available() == 0) {
//                slice = Arrays.copyOfRange(slice, 0, len);
//            }
//            outputStream.write(slice);
//        }
//    }


}
