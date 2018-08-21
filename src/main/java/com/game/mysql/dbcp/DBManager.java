package com.game.mysql.dbcp;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
/**
 * Created by YXD on 2018/8/20.
 * https://blog.csdn.net/wqc19920906/article/details/72832780
 */
public class DBManager {

    private static final String configFile = "dbcp.properties";

    private static DataSource dataSource;

    static {
        Properties dbProperties = new Properties();
        try {
            dbProperties.load(DBManager.class.getClassLoader().getResourceAsStream(configFile));
            dataSource = BasicDataSourceFactory.createDataSource(dbProperties);

            Connection conn = getConn();
            DatabaseMetaData mdm = conn.getMetaData();

            System.out.println("Connected to " + mdm.getDatabaseProductName() + " " + mdm.getDatabaseProductVersion());
            if (conn != null) {
                conn.close();
            }

        } catch (Exception e) {
            System.out.println("初始化连接池失败:" + e);
        }
    }

    private DBManager() {

    }

    public static final Connection getConn() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (Exception e) {
            System.out.println("获取数据库连接失败:" + e);
        }
        return conn;
    }

    //关闭数据库连接，将连接返还给数据库连接池
    public static void closeConn(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("关闭数据库连接失败：" + e);
        }
    }

    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            Connection conn = DBManager.getConn();
            System.out.println(i + "连接数：");
            DBManager.closeConn(conn);
        }
        long end = System.currentTimeMillis();
        System.out.println("用时：" + (end - begin));
    }
}
