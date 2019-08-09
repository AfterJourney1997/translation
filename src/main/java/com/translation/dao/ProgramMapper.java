package com.translation.dao;

import com.translation.entity.Program;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Program record);

    Program selectByPrimaryKey(Integer id);

    List<Program> selectAll();

    List<Program> selectFather();

    List<Program> selectAllProgram();

    Program selectByContent(@Param(value = "content") String content);

    int updateByPrimaryKey(Program record);

    int updateByContentTaskId(@Param("taskId") String taskId, @Param("record") Program record);

}