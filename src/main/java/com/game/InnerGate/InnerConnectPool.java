package com.game.InnerGate;

import com.game.bean.GameMessage;
import com.game.instance.LogInstance;

/**
 * Created by YXD on 2018/8/17.
 */
public class InnerConnectPool {
    //构造方法私有
    private InnerConnectPool(){
    }

    //生命该类的对象
    private static InnerConnectPool sc = null ;

    //统一管理此类的获取
    public static InnerConnectPool getInstance(){
        synchronized (InnerConnectPool.class){
            if(sc == null)
                sc = new InnerConnectPool();
        }
        return sc;
    }


    /**
     * 发送消息
     * @param key
     * @param index
     * @param gameId
     * @param playerId
     * @param roleId
     * @param sessionId
     * @param mapId
     * @param timeStamp
     * @param actionType
     * @param itemId
     * @param monsterId
     * @param attackedRoleId
     * @param mesContent
     * @return
     */
    public boolean sendMessage(String key,
                               long index,
                               int gameId,
                               long playerId,
                               long roleId,
                               long sessionId,
                               long mapId,
                               long timeStamp,
                               int actionType,
                               long itemId,
                               int monsterType,
                               long monsterId,
                               long attackedRoleId,
                               String mesContent
                               ){
        LogInstance.getInstance().info(mesContent);
        return true;
    }
}
