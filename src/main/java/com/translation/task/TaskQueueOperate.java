package com.translation.task;

import cn.hutool.core.util.StrUtil;
import com.translation.dao.ProgramMapper;
import com.translation.entity.Program;
import com.translation.util.ApplicationContextProvider;
import com.translation.util.xunfei.TranslationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TaskQueueOperate {

    private static List<Program> taskQueue;

    // 初始化，将库中的未完成的任务拉入队中
    static void initTaskQueue(){

        List<Program> programs = ApplicationContextProvider.getBean(ProgramMapper.class).selectAllProgram();

            if(programs.stream().anyMatch((e) -> e.getStatus() == 0)){

            taskQueue = programs.stream()
                    .filter((e) -> e.getStatus() == 0 && !StrUtil.isEmpty(e.getContent()))
                    .collect(Collectors.toList());

            log.info("队列初始化完成，拉入" + taskQueue.size() + "个任务。");
        }else {
            taskQueue = new ArrayList<>();
            log.info("初始化完成，库中无数据需拉取。");

        }
    }

    // 添加一个任务
    public static void addTask(Program program){
        taskQueue.add(program);
    }

    // 删除一个任务
    public static void removeTask(Program program){
        taskQueue.removeIf((e) -> e.equals(program));
    }

    public static void removeTask(int index){
        taskQueue.remove(index);
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 60)
    public static void startTask(){

        // 任务队列异常
        if(taskQueue == null) {
            log.info("任务队列异常！");
            return;
        }

        // 队列为空
        if(taskQueue.size() == 0){
            log.info("队列为空！");
            return;
        }

        log.info("任务队列状态：{}", taskQueue);

        // 避免ConcurrentModificationException
        CopyOnWriteArrayList<Program> tempList = new CopyOnWriteArrayList<>(taskQueue);
        for(Program program : tempList){

            try {
                TranslationUtil.scanQueue(program);
            } catch (SignatureException e) {
                e.printStackTrace();
            }

        }

    }

}
