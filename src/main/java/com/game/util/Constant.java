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

    public static final int BATCH_SECONDS = 10;//stream 批次时间
    public static final int WINDOW_LENGTH_SECONDS = 60;//窗口长度
    public static final int WINDOW_INTERVAL_SECONDS = 50;//窗口滑动时间
    public static final int MALICIOUS_ATTACK_PLAYER_SEND_NUM = 10;//Malicious attack 次数上限

}
