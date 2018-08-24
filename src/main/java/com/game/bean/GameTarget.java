package com.game.bean;

/**
 * Created by YXD on 2018/8/21.
 */
public class GameTarget {

    private int gameId;//哪一个游戏，不止统计一个游戏
    private int targetType;//1 游戏玩家；2 角色
    private Long roleId;//角色id
    private int onlineNumber;//在线数量
    private Long nowTime;//此时时间戳
    private String params;// 备用参数

    public GameTarget(int gameId,
                           int targetType,
                           Long roleId,
                           int onlineNumber,
                           Long nowTime,
                           String params){
        this.gameId = gameId;
        this.targetType = targetType;
        this.roleId = roleId;
        this.onlineNumber = onlineNumber;
        this.nowTime = nowTime;
        this.params = params;
    }

    public static GameTarget getInstance(int gameId,
                                  int targetType,
                                  Long roleId,
                                  int onlineNumber,
                                  Long nowTime,
                                  String params){
        return new GameTarget(gameId, targetType,roleId,onlineNumber,nowTime,params);
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
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
