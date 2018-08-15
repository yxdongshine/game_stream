package com.game.resourcesload;


import com.game.systeminfrastructure.SingleChannel;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by YXD on 2017/10/20.
 */
public class LoadProp {

    static Properties pro = new Properties();

    /**
     * 初始化资源文件属性
     */
    public static synchronized void initPro() {
        if(SingleChannel.getInstance().getConfigInfo().isEmpty()){
            // 加载资源路径
            String path = "src\\main\\resources\\config.properties";
            readPro(path);
        }
    }
    /**
     *
     * @param path prop资源路径
     */
    public static void readPro(String path){
        try {
            //读取属性文件
            InputStream is = new BufferedInputStream(new FileInputStream(new File(path)));
            //加载属性列表
            pro.load(is);
            Iterator<String> iter = pro.stringPropertyNames().iterator();
            while(iter.hasNext()){
                String key = iter.next();
                String value = pro.getProperty(key);
                System.out.println(key+":"+value);
                SingleChannel.putConfig(key, value);
            }
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 写文件
     * @param path
     */
    public static void writePro(String path,String key,String value){
        try {
            //读取属性文件
            InputStream is = new BufferedInputStream(new FileInputStream(new File(path)));
            //加载属性列表
            pro.load(is);
            ///保存属性到b.properties文件
            FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
            pro.setProperty(key, value);
            pro.store(oFile,"");
            oFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
