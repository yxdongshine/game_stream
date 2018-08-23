package com.game.mysql.Jdbc.Dao;

import com.game.bean.GameTarget;

import java.sql.Connection;

/**
 * Created by YXD on 2018/8/21.
 */
public interface GameTargetDao {
    Boolean add(GameTarget gt,Connection conn);
}
