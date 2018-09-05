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
        String sql = " insert into game_target (entry_id,target_type,online_number,now_time,params)" +
                " values (?,?,?,?,?) on DUPLICATE key update online_number = online_number + ? ";
        try {
            PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
            pstmt.setInt(1, gt.getEntryId());
            pstmt.setInt(2, gt.getTargetType());
            pstmt.setInt(3, gt.getOnlineNumber());
            pstmt.setLong(4, gt.getNowTime());
            pstmt.setString(5, gt.getParams());
            pstmt.setInt(6, gt.getOnlineNumber());
            isSuccess = pstmt.executeUpdate();
            pstmt.close();
            //conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSuccess > 0 ;
    }
}
