package com.translation.service;

import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.util.FindProgramUtil;
import com.translation.util.exception.FileNameErrorException;
import com.translation.util.xunfei.TranslationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class InfoService {

    private final ProgramMapper programMapper;

    @Autowired
    public InfoService(ProgramMapper programMapper) {
        this.programMapper = programMapper;
    }

    // 节目信息入库且获取对应taskId
    public void insertInfo(MultipartFile file) throws FileNameErrorException {

        // 根据文件名称在信息库中查找对应信息生成对象
        Program program = FindProgramUtil.findProgram(Objects.requireNonNull(file.getOriginalFilename()));

        // 匹配此节目的频率是否在库中，如不在则添加入库
        insertFrequency(program.getFrequency());
        int frequencyId = getFrequencyId(program.getFrequency());

        program.setStatus(0);
        program.setFather(frequencyId);
        // 获取对应的taskId
        String taskId = TranslationUtil.updateFileAndGetTaskId(program, file);
        program.setContent(taskId);

        programMapper.insert(program);

    }

    // 查询全部节目信息
    public List<Program> getAllProgram(){
        return programMapper.selectAllProgram();
    }

    // 修改节目信息
    public void updateProgram(Program program){
        programMapper.updateByPrimaryKey(program);
    }

    // 匹配此节目的频率是否在库中，如不在则添加入库
    private void insertFrequency(String frequencyName){

        List<Program> programs = programMapper.selectFather();

        Stream<Program> contentStream = programs.stream()
                .filter((e) -> e.getFrequency().equals(frequencyName));

        // 判断是否存在与frequencyName相同的频率
        if(!contentStream.findFirst().isPresent()){
            Program frequency = new Program();
            frequency.setFather(0);
            frequency.setFrequency(frequencyName);
            programMapper.insert(frequency);
        }

    }

    // 根据频率名称查询频率对应的主键id
    private int getFrequencyId(String frequencyName){

        List<Program> programs = programMapper.selectFather();

        Stream<Integer> idStream = programs.stream()
                .filter((e) -> e.getFrequency().equals(frequencyName))
                .map(Program::getId);

        return idStream.findFirst().get();
    }


}
