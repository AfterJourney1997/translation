package com.translation.task;

import com.translation.util.xunfei.TranslationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class TaskQueueOperate {

    private static List<String> taskQueue = new ArrayList<>();

//    // 初始化，将库中的未完成的任务拉入队中
//    static void initTaskQueue() {
//        taskQueue = ApplicationContextProvider.getBean(ProgramMapper.class).selectAllTask();
//        log.info("队列初始化完成，拉入" + taskQueue.size() + "个任务。");
//    }

    // 添加一个任务
    public static void addTask(String taskId) {
        taskQueue.add(taskId);
    }

    // 删除一个任务
    public static void removeTask(String taskId) {
        taskQueue.removeIf((e) -> e.equals(taskId));
    }

    public static void removeTask(int index) {
        taskQueue.remove(index);
    }

    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 60)
    public static void startTask() {
        if (taskQueue == null) {
            log.info("任务队列异常！");
            return;
        }
        if (taskQueue.size() == 0) {
            log.info("队列为空！");
            return;
        }

        log.info("任务队列状态：{}", taskQueue);

        // 避免ConcurrentModificationException
        CopyOnWriteArrayList<String> taskIdList = new CopyOnWriteArrayList<>(taskQueue);
        for (String taskId : taskIdList) {
            try {
                TranslationUtil.scanQueue(taskId);
            } catch (SignatureException e) {
                e.printStackTrace();
            }
        }
    }


}
