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

    public static final int BATCH_SECONDS = 1;//stream 批次时间 1s
    public static final int BLACK_LIST_WINDOW_LENGTH_SECONDS = 60;//黑名单窗口长度
    public static final int BLACK_LIST_WINDOW_INTERVAL_SECONDS = 50;//黑名单窗口滑动时间
    public static final int MALICIOUS_ATTACK_PLAYER_SEND_NUM = 100;//Malicious attack 次数上限
    public static final int HANG_UP_WINDOW_LENGTH_SECONDS = 60;//挂机窗口长度
    public static final int HANG_UP_WINDOW_INTERVAL_SECONDS = 50;//挂机窗口滑动时间
    public static final int HANG_UP_SEND_NUM = 30;//一分钟发送挂机消息 次数上限

    public static final String SYSTEM_PREFIX = "GAME_";//系统app redis前缀
    public static final String BLACK_LIST_KEY = "black_list";//黑名单key
    public static final String WHITE_LIST_KEY = "white_list";//白名单key
    public static final String SENSITIVE_VOCABULARY_LIST_KEY = "sensitive_vocabulary_list";//敏感词汇列表key
    public static final String SENSITIVE_VOCABULARY_REPLACE_CHARS = "***";//白名单key

    public static final int ACTION_TYPE_HANG_UP = 0;//玩家挂机动作
    public static final int ACTION_TYPE_ATTACK_PLAYER = 1;//攻击玩家动作
    public static final int ACTION_TYPE_ATTACK_MONSTER = 2;//攻击怪物动作
    public static final int ACTION_TYPE_BUY_ITEM = 3;//购买物品动作
    public static final int ACTION_TYPE_SEND_MESSAGE = 4;//发送消息动作

}
