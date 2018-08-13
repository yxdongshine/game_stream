package com.game.data;

import com.game.bean.GameMessage;
import com.game.util.DateUtil;
import com.game.util.StrUtil;

/**
 * Created by YXD on 2018/8/13.
 */
public class Simulation {

    /**
     * 整个游戏行为数据收集
     * @param index
     * @return
     */
    public static GameMessage createGameMessage(Long index){
        GameMessage gm = new GameMessage();
        gm.setKey(StrUtil.createReverseKey());
        gm.setIndex(index);
        gm.setGameId(StrUtil.createGameId());
        gm.setPlayerId(StrUtil.createPlayerId());
        gm.setRoleId(StrUtil.createRoleId());
        gm.setSessionId(StrUtil.createSessionId());
        gm.setMapId(StrUtil.createMapId());
        gm.setTimeStamp(DateUtil.getSystemTime());
        int actionType = StrUtil.createActionType();
        gm.setActionType(actionType);
        double productProbability = StrUtil.random.nextDouble();//产生0到1的double概率
        if(0.2 >= productProbability){//小于0.25 产生挂机行为

        }else if(0.4 >= productProbability && 0.2 < productProbability){
            //产生攻击玩家
            gm.setAttackedRoleId(StrUtil.createPlayerId());
        }else if(0.6 >= productProbability && 0.4 < productProbability){
            //产生攻击怪物
            gm.setMonsterType(StrUtil.createMonsterType());
            gm.setMonsterId(StrUtil.createMonsterId());
        }else if(0.8 >= productProbability && 0.6 < productProbability){
            //产生购买物品
            gm.setItemId(StrUtil.createItemId());
        }else {
            //产生发送消息
            gm.setMesContent(StrUtil.createOneMessageContent());
        }
        return  gm;
    }

}
