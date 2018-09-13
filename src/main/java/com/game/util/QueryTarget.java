package com.game.util;

import com.game.RedisPool.RedisUtils;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by YXD on 2018/9/11.
 */
public class QueryTarget {

    public static void main(String[] args) throws Exception{
        //queryWhiteList();
        queryBlackList();
        //queryMWordsList();
        //queryGameTargetList();
    }

    public static void queryWhiteList() throws Exception{
        //创建白名单 1 to 10000 为系统保留用户id
		/*Set<String> bl = new HashSet<String>();
		for(int i=0; i<10000; i++){
			bl.add(i+"");
		}*/
        //sAdd(Constant.SYSTEM_PREFIX + Constant.WHITE_LIST_KEY ,bl);
        String wkey = Constant.SYSTEM_PREFIX + Constant.WHITE_LIST_KEY;
        Set<String> blResult = RedisUtils.sMembers(wkey);
        int min = 10000;
        int max = 0;
        for (Iterator iterator = blResult.iterator(); iterator.hasNext();) {
            //String value = (String) iterator.next();
            int value = Integer.parseInt((String) iterator.next());
            if(value > max) max = value;
            if(value < min) min = value;
            //if(value < 10000)
            System.out.println("key:"+value);
            //System.out.println("value:"+get(value));
        }
        System.out.println("max:"+max);
        System.out.println("min:"+min);
        System.out.println("size:"+blResult.size());
    }

    public static void queryBlackList() throws Exception{
        String bkey = Constant.SYSTEM_PREFIX + Constant.BLACK_LIST_KEY;
        //删除黑名单数据
        //remove(bkey);
        Set<String> blResult = RedisUtils.sMembers(bkey);
        //Set<String> blResult = keys(gameKeys);
        int min = 10000;
        int max = 0;
        for (Iterator iterator = blResult.iterator(); iterator.hasNext();) {
            //String value = (String) iterator.next();
            int value = Integer.parseInt((String) iterator.next());
            if(value > max) max = value;
            if(value < min) min = value;
            //if(value < 10000)
            System.out.println("key:"+value);
            //System.out.println("value:"+get(value));
        }
        System.out.println("max:"+max);
        System.out.println("min:"+min);
        System.out.println("size:"+blResult.size());
    }

    public static void queryMWordsList() throws Exception{
        //添加敏感词汇redis中
        String mKey = Constant.SYSTEM_PREFIX + Constant.SENSITIVE_VOCABULARY_LIST_KEY;
			/*Set set = new HashSet();
			CollectionUtils.addAll(set, StrUtil.blackWords);
			sAdd(mKey,set);*/
        Set<String> blResult = RedisUtils.sMembers(mKey);
        for (Iterator iterator = blResult.iterator(); iterator.hasNext();) {
            String value = (String) iterator.next();
            System.out.println("key:"+value);
        }
        System.out.println("size:"+blResult.size());
    }

    public static void queryGameTargetList() throws Exception{
        //游戏累加指标
        String gameKeys = Constant.SYSTEM_PREFIX + Constant.REAL_TIME_TARGET_SESSION_NUMBER_KEY;
        //removeKeys(gameKeys);
        //获取数据
        Set<String> blResult = RedisUtils.keys(gameKeys);
        for (Iterator iterator = blResult.iterator(); iterator.hasNext();) {
            String value = (String) iterator.next();
            System.out.println(value+":"+RedisUtils.get(value));
        }
        System.out.println("size:"+blResult.size());
    }

}
