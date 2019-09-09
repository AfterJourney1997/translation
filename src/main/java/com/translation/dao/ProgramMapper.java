package com.translation.dao;

import com.translation.entity.Program;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProgramMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Program record);

    int insertList(List<Program> list);

    Program selectByPrimaryKey(Integer id);

    List<Program> selectAll();

    List<Program> selectFather();

    List<Program> selectAllProgram();

    List<String> selectAllTask();

    void updateStatus(String content, int status);

    Program selectByTaskId(@Param(value = "content") String content);

    int updateByPrimaryKey(Program record);

    int updateByContentTaskId(@Param("taskId") String taskId, @Param("record") Program record);

    int updateTaskId(@Param("taskId") String taskId, @Param("fileId") int fileId);

    List<Program> test();

    int selectMaxFileId();
}