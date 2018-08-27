package com.game.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by YXD on 2017/10/24.
 */
public class ThreadPool {
    //构造私有
    private ThreadPool(){}

    //对象为null
    private static ThreadPool tp = null;
    //线程池
    private  ExecutorService es = Executors.newCachedThreadPool();

    //唯一获取对象
    public static ThreadPool getInstance(){
        synchronized (ThreadPool.class){
            if (null == tp)
                tp = new ThreadPool();
        }
        return  tp;
    }

    //添加线程
    public synchronized void addTask(Runnable runnable){
        if (null != runnable)
            getEs().submit(runnable);
    }

    //关闭线程池
    private void colseThreadPool(){
        getEs().shutdown();
    }

    /**
     * 获取线程池
     * @return
     */
    public ExecutorService getEs() {
        return es;
    }
}
