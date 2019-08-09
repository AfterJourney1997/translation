package com.translation.task;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTask implements ApplicationRunner {


    @Override
    public void run(ApplicationArguments args) throws Exception {

        TaskQueueOperate.initTaskQueue();

    }
}
