package com.automation.zzx.intelligent_basket_demo.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pengchenghu on 2019/3/4.
 * Author Email: 15651851181@163.com
 * Describe: 定时任务
 */
public class CustomTimeTask {
    private Timer timer;
    private TimerTask task;
    private long time;

    public CustomTimeTask(long time, TimerTask task) {
        this.task = task;
        this.time = time;
    }

    public void start(){
        timer = new Timer();
        timer.schedule(task, 0, time);//每隔time时间段就执行一次
    }

    public void stop(){
        if (timer != null) {
            timer.cancel(); // 从此计时器的任务队列中移除所有已取消的任务。
            timer = null;
        }
        if (task != null) {
            task.cancel();  //将原任务从队列中移除
        }
    }
}
