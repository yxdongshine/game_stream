package com.game.mysql.Jdbc.Impl;

import com.game.bean.GameTarget;
import com.game.mysql.Jdbc.Dao.GameTargetDao;
import com.game.mysql.dbcp.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by YXD on 2018/8/21.
 */
public class GameTargetImpl implements GameTargetDao{

    /**
     * 数据库中添加数据
     * @param gt
     */
    public Boolean add(GameTarget gt,Connection conn) {
        int isSuccess = 0;
        String sql = "insert into game_target (game_id,target_type,role_id,online_number,now_time,params) " +
                "values(?,?,?,?,?,?)";
        PreparedStatement pstmt = null;
        try {
            pstmt = (PreparedStatement) conn.prepareStatement(sql);
            pstmt.setInt(1, gt.getGameId());
            pstmt.setInt(2, gt.getTargetType());
            pstmt.setLong(3, gt.getRoleId());
            pstmt.setInt(4, gt.getOnlineNumber());
            pstmt.setLong(5, gt.getNowTime());
            pstmt.setString(6, gt.getParams());
            isSuccess = pstmt.executeUpdate(sql);
            pstmt.close();
            conn.close();
        } catch (SQLException e) {
            try {
                pstmt.close();
            } catch (Exception e1){
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        return isSuccess > 0 ;
    }
}
