package com.translation.service.impl;

import com.translation.dao.NoProgramMapper;
import com.translation.entity.NoProgram;
import com.translation.service.TranslationService;
import com.translation.util.xunfei.TranslationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service("TranslationNoProgramService")
public class TranslationNoProgramService implements TranslationService {
    @Autowired
    private NoProgramMapper noProgramMapper;

    @Override
    public String translation(InputStream inputStream, String fileName) {
        String taskId = TranslationUtil.updateFileAndGetTaskId(inputStream, fileName);
        NoProgram noProgram = new NoProgram();
        noProgram.setStatus(1);
        noProgram.setContent(taskId);
        noProgramMapper.insert(noProgram);
        return null;
    }
}
