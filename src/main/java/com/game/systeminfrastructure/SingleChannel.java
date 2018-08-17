package com.game.systeminfrastructure;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by YXD on 2017/10/20.
 */
public class SingleChannel {
    //构造方法私有
    private SingleChannel(){
    }

    //生命该类的对象
    private static SingleChannel sc = null ;

    //统一的资源内存管理
    ConcurrentHashMap<String,String> configInfo = new ConcurrentHashMap<String,String>();
    //统一管理此类的获取
    public static SingleChannel getInstance(){
        synchronized (SingleChannel.class){
            if(sc == null)
                sc = new SingleChannel();
        }
        return sc;
    }


    //提供该资源的获取
    public static String getConfig(String key){
        return getInstance().getConfigInfo().get(key);
    }

    //提供该资源的写入
    public  static void putConfig(String key,String value){
        getInstance().getConfigInfo().put(key,value);
    }

    private ConcurrentHashMap<String, String> getConfigInfo() {
        return configInfo;
    }

    private void setConfigInfo(ConcurrentHashMap<String, String> configInfo) {
        this.configInfo = configInfo;
    }


}
