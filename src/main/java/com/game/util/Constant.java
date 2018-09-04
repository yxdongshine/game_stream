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
    //public static final String CHECK_POINT_PATH = "tmp/sparkStream/GameStatistics";
    public static final String ZOOKEEPER_CONNECT = "hadoop0:2181,w100:2181,hadoop81:2181";
    public static final String KAFKA_CONNECT = "hadoop0:9092,w100:9092,hadoop81:9092";
    public static final String GROUP_ID = "GameStatistics_group";

    public static final int BATCH_SECONDS = 1;//stream 批次时间 1s
    public static final int BLACK_LIST_WINDOW_LENGTH_SECONDS = 3;//黑名单窗口长度
    public static final int BLACK_LIST_WINDOW_INTERVAL_SECONDS = 3;//黑名单窗口滑动时间
    public static final int MALICIOUS_ATTACK_PLAYER_SEND_NUM = 9;//Malicious attack 次数上限100
    public static final int HANG_UP_WINDOW_LENGTH_SECONDS = 3;//挂机窗口长度
    public static final int HANG_UP_WINDOW_INTERVAL_SECONDS = 3;//挂机窗口滑动时间
    public static final int HANG_UP_SEND_NUM = 3;//一分钟发送挂机消息 次数上限
    public static final int REAL_TIME_TARGET_WINDOW_LENGTH_SECONDS = 3;//实时指标窗口长度
    public static final int REAL_TIME_TARGET_INTERVAL_SECONDS = 3;//实时指标滑动时间
    public static final String REAL_TIME_TARGET_SESSION_NUMBER_KEY = "session_number_";//在线回话数量key

    public static final String SYSTEM_DELIMITED_SYMBOL = "_";//系统app redis前缀
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

    public static final String SMTP_HOST = "smtp.exmail.qq.com";
    public static final String SMTP_PORT = "25";
    public static final String RECEIVE_MAIL_SUBJECT = "游戏警告！！！";
    public static final String RECEIVE_MAIL_CONTENT = "尊敬的玩家playerId，你在timeStamp时间" +
            "gameId游戏中使用roleId角色存在挂机行为。后期系统将根据情况处罚操作！！！";

    public static final int TARGET_TYPE_PLAYER = 1;//1 游戏玩家；2 角色
    public static final int TARGET_TYPE_ROLE = 2;//1 游戏玩家；2 角色

    public static  Long SUM_SEND_MESS_NUMBER = 0l;//并发发送消息数量

    /**
     * 写
     * @param subVariable
     * @return
     */
    public static Long setSumMessNumber(Long subVariable){
        synchronized (SUM_SEND_MESS_NUMBER){
            return SUM_SEND_MESS_NUMBER += subVariable;
        }
    }

    /**
     * 读
     * @return
     */
    public static Long getSumMessNumber(){
        synchronized (SUM_SEND_MESS_NUMBER){
            return SUM_SEND_MESS_NUMBER ;
        }
    }
}
