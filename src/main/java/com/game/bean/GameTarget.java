package com.game.bean;

/**
 * Created by YXD on 2018/8/21.
 */
public class GameTarget {

    private int entryId;//哪一个游戏，不止统计一个游戏 或者roleid
    private int targetType;//1 游戏玩家；2 角色
    private int onlineNumber;//在线数量
    private Long nowTime;//此时时间戳
    private String params;// 备用参数

    public GameTarget(int entryId,
                           int targetType,
                           int onlineNumber,
                           Long nowTime,
                           String params){
        this.entryId = entryId;
        this.targetType = targetType;
        this.onlineNumber = onlineNumber;
        this.nowTime = nowTime;
        this.params = params;
    }

    public static GameTarget getInstance(int entryId,
                                  int targetType,
                                  int onlineNumber,
                                  Long nowTime,
                                  String params){
        return new GameTarget(entryId, targetType,onlineNumber,nowTime,params);
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public int getOnlineNumber() {
        return onlineNumber;
    }

    public void setOnlineNumber(int onlineNumber) {
        this.onlineNumber = onlineNumber;
    }

    public Long getNowTime() {
        return nowTime;
    }

    public void setNowTime(Long nowTime) {
        this.nowTime = nowTime;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
