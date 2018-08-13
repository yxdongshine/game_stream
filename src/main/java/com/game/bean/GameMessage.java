package com.game.bean;

import com.alibaba.fastjson.JSON;

/**
 * Created by YXD on 2018/8/11.
 * 游戏消息中交互的消息内容
 */
public class GameMessage {
    private String key;//消息key

    private long index;//消息次序
    private int gameId;//哪一个游戏，不止统计一个游戏
    private long playerId;//游戏玩家id
    private long roleId;//角色id
    private long sessionId;//tcp/ip长连接的回话id
    private long mapId;//游戏中场景id
    private long timeStamp;//这条记录产生的时间
    private int actionType;// 0:玩家挂机；1:攻击玩家；2：攻击怪物；3：购买物品；4:发送消息
    private long itemId;//物品id
    private int monsterType;//怪物类型 0 小怪；1战车；2暴君；3主宰；4野兽；5红buf；6蓝buf；7塔；8水晶；9河道
    private long monsterId;//怪物id
    private long attackedRoleId;//承受玩家角色id
    private String mesContent;//消息内容

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getMapId() {
        return mapId;
    }

    public void setMapId(long mapId) {
        this.mapId = mapId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public int getMonsterType() {
        return monsterType;
    }

    public void setMonsterType(int monsterType) {
        this.monsterType = monsterType;
    }

    public long getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(long monsterId) {
        this.monsterId = monsterId;
    }

    public long getAttackedRoleId() {
        return attackedRoleId;
    }

    public void setAttackedRoleId(long attackedRoleId) {
        this.attackedRoleId = attackedRoleId;
    }

    public String getMesContent() {
        return mesContent;
    }

    public void setMesContent(String mesContent) {
        this.mesContent = mesContent;
    }

    public String toJsonStr(){
        return JSON.toJSONString(this);
    }
}
