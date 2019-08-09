package com.translation;

import com.translation.dao.ProgramMapper;
import com.translation.service.InfoService;
import com.translation.util.xunfei.TranslationUtil;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TranslationApplicationTests {

    private static String TRANSLATION_URL;

    @Value("${TRANSLATION_URL}")
    private void setTranslationUrl(String TRANSLATION_URL){
        TranslationApplicationTests.TRANSLATION_URL = TRANSLATION_URL;
    }

    @Autowired
    private ProgramMapper programMapper;
    @Autowired
    private InfoService infoService;

    @Test
    public void contextLoads() throws Exception {

        /*String fileName = "nongcun_20180730_0000_0300.txt";

        infoService.insertInfo(fileName);*/

        System.out.println(TRANSLATION_URL);

        String fileName = "temp.mav";

        File file = new File("E:\\translation\\asfgdgsgljjlsdg.wav");
        System.out.println(file.exists());
        System.out.println(file.getPath());
        FileInputStream fileInputStream = new FileInputStream(file);

        MultipartFile multipartFile = new MockMultipartFile(fileName, file.getName(), ContentType.APPLICATION_OCTET_STREAM.toString(), fileInputStream);

        System.out.println(multipartFile.getSize());
        System.out.println(file.length());

        //TranslationUtil.translation(multipartFile);

/*        File file = new File("E:\\translation\\asfgdgsgljjlsdg.wav");
        TestUtil.translation(file);*/

    }

}
