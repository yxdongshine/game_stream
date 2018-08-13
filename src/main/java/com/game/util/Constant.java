package com.game.util;

/**
 * Created by YXD on 2018/8/11.
 */
public class Constant {

    /**
     * 发送消息相关
     */
    public static final String TOPIC_NAME = "gamestream";
    public static final Boolean isSync = false;//是否异步
    /**
     * sparkStream 相关
     */
    public static final String APP_NAME = "GameStatistics" ;
    public static final String CHECK_POINT_PATH = "/tmp/sparkStream/GameStatistics";
    public static final String ZOOKEEPER_CONNECT = "hadoop0:2181,w100:2181,hadoop81:2181";
    public static final String GROUP_ID = "GameStatistics_group";



}
