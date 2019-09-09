package com.translation.service;

import com.translation.dao.NoProgramMapper;
import com.translation.dao.ProgramMapper;
import com.translation.entity.NoProgram;
import com.translation.entity.Program;
import com.translation.util.exception.FileNameErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;
import java.util.stream.Stream;

@Service
public class InfoService {

    private final ProgramMapper programMapper;
    private final NoProgramMapper noProgramMapper;

    @Autowired
    public InfoService(ProgramMapper programMapper, NoProgramMapper noProgramMapper) {
        this.programMapper = programMapper;
        this.noProgramMapper = noProgramMapper;
    }


    // 查询全部节目信息
    public List<Program> getAllProgram() {
        return programMapper.selectAllProgram();
    }

    // 修改节目信息
    public void updateProgram(Program program) {
        programMapper.updateByPrimaryKey(program);
    }

    /**
     * 获取全部NoProgram列表
     *
     * @return
     */
    public List<NoProgram> getNoProgram() {
        return noProgramMapper.selectAll();
    }

    // 匹配此节目的频率是否在库中，如不在则添加入库
    private void insertFrequency(String frequencyName) {

        List<Program> programs = programMapper.selectFather();

        Stream<Program> contentStream = programs.stream()
                .filter((e) -> e.getFrequency().equals(frequencyName));

        // 判断是否存在与frequencyName相同的频率
        if (!contentStream.findFirst().isPresent()) {
            Program frequency = new Program();
            frequency.setFather(0);
            frequency.setFrequency(frequencyName);
            programMapper.insert(frequency);
        }

    }

}
