package com.translation.service.impl;

import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.service.TranslationService;
import com.translation.task.TaskQueueOperate;
import com.translation.util.ProgramUtil;
import com.translation.util.xunfei.TranslationUtil;
import org.springframework.stereotype.Service;

import java.io.InputStream;


@Service("TranslationProgramService")
public class TranslationProgramService implements TranslationService {
    private final ProgramMapper programMapper;

    public TranslationProgramService(ProgramMapper programMapper) {
        this.programMapper = programMapper;
    }

    @Override
    public String translation(InputStream inputStream, String fileName) {
        String taskId = TranslationUtil.updateFileAndGetTaskId(inputStream, fileName);
        Program program = ProgramUtil.findProgram(fileName);
        program.setContent(taskId);
        program.setStatus(1);
        TaskQueueOperate.addTask(taskId);
        programMapper.insert(program);
        return null;
    }
}
