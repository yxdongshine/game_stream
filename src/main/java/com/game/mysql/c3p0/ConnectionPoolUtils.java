package com.game.mysql.c3p0;

/**
 * Created by YXD on 2018/8/20.
 *连接池工具类，返回唯一的一个数据库连接池对象,单例模式
 */
public class ConnectionPoolUtils {
    private ConnectionPoolUtils(){};//私有静态方法
    private static ConnectionPool poolInstance = null;
    public static ConnectionPool GetPoolInstance(){
        if(poolInstance == null) {
            poolInstance = new ConnectionPool(
                    "com.mysql.jdbc.Driver",
                    "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8",
                    "root", "123456");
            try {
                poolInstance.createPool();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return poolInstance;
    }
}
