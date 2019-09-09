package com.translation.dao;

import com.translation.entity.NoProgram;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoProgramMapper {
    void insert(NoProgram noProgram);

    void updateStatus(@Param("content") String content, @Param("status") int status);

    List<NoProgram> selectAll();
}
