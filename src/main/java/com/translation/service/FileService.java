package com.translation.service;

import com.translation.util.FileSystemUtil;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class FileService {

    public void uploadFile(MultipartFile file, String path) throws IOException {

        FileSystem fileSystem = FileSystemUtil.getFileSystem();
        String contentType = path + "/" + file.getOriginalFilename();
        Path upPath = new Path(contentType.replaceFirst("//", "/"));
        OutputStream outputStream = fileSystem.create(upPath);
        outputStream.write(file.getBytes());
        outputStream.close();

    }

}
