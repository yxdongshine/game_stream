package com.game.data;

import com.game.bean.GameMessage;
import com.game.util.Constant;
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
        //int actionType = StrUtil.createActionType();
        double productProbability = 0.1;//StrUtil.random.nextDouble();//产生0到1的double概率
        if(0.2 >= productProbability){//小于0.2 产生挂机行为
            gm.setActionType(Constant.ACTION_TYPE_HANG_UP);
        }else if(0.4 >= productProbability && 0.2 < productProbability){
            //产生攻击玩家
            gm.setAttackedRoleId(StrUtil.createPlayerId());
            gm.setActionType(Constant.ACTION_TYPE_ATTACK_PLAYER);
        }else if(0.6 >= productProbability && 0.4 < productProbability){
            //产生攻击怪物
            gm.setMonsterType(StrUtil.createMonsterType());
            gm.setMonsterId(StrUtil.createMonsterId());
            gm.setActionType(Constant.ACTION_TYPE_ATTACK_MONSTER);
        }else if(0.8 >= productProbability && 0.6 < productProbability){
            //产生购买物品
            gm.setItemId(StrUtil.createItemId());
            gm.setActionType(Constant.ACTION_TYPE_BUY_ITEM);
        }else {
            //产生发送消息
            gm.setMesContent(StrUtil.createOneMessageContent());
            gm.setActionType(Constant.ACTION_TYPE_SEND_MESSAGE);
        }
        return  gm;
    }

    public static void main(String[] args) {
        //System.out.println(StrUtil.createOneMessageContent());
    }

}
