package com.game.util;

import java.lang.reflect.Array;
import java.util.Random;

/**
 * Created by YXD on 2018/8/13.
 */
public class StrUtil {

    public static final int RandomKey = 100000;//默认10w范围内
    public static final Random random = new Random();
    public static final int gameRange = 5;// 5个游戏编号
    public static final int roleRange = 10;//10个游戏角色
    public static final int playerRange = 100000;//游戏玩家数量10w
    public static final int sessionRange = 10000;//同时单服务器并发1w
    public static final int mapRange = 100;//整个游戏地图100张
    public static final int actionTypeRange = 4;//0:玩家正常发送命令；1:玩家挂机；2：攻击；3：购买；4:发送消息
    public static final int itemRange = 1000;//游戏道具1000件
    public static final int monsterTypeRange = 10;//怪物种类10
    public static final int monsterIdRange = 500;//单场最大产生500个
    public static final double probability = 0.4;//百分十四十概率会发过敏词语

    public static final  String blackWords[] = {
            "你妹","狗日","装逼","草泥马","特么的","撕逼","玛拉戈壁","爆菊","JB","呆逼","本屌",
            "齐B短裙","法克鱿","丢你老母","达菲鸡","装13","逼格","蛋疼","傻逼","绿茶婊","你妈的",
            "表砸","屌爆了","买了个婊","已撸","吉跋猫","妈蛋","逗比","我靠","碧莲","碧池","然并卵",
            "日了狗","屁民","吃翔","XX狗","淫家","你妹","浮尸国","滚粗"};

    public static final String sentences[] = {
            "可不可以让我回到突然长大的那一天至少",
            "那个时候我会觉得长大是一种美好",
            "看着渐渐的失去联系的朋友，，我们只能回忆那些美好的东西",
            "突然发现，这个世界只要自己开心了，就他妈瞬间变得美好了",
            "世界上最美好的事情，就是和你一起走在路上，我未老，而你依然",
            "生活再苦，也不要失去信念，因为美好的日子，也许就在明天",
            "不可否认，暗恋也许是我们经历过的最美好的感情",
            "从来没有一种坚持被辜负，请你相信，你的坚持，终将美好",
            "我将一切回忆掩埋，只想拥有一个美好的未来",
            "一个懂得定位自己的人，才是真的在过美好的人生路",
            "过去的日子即使夹着风尘却也觉得美好",
            "做最好的今天，回顾最好的昨天，迎接最美好的明天",
            "记忆中的美好已涣散，寻不回从前的十全十美",
            "我想我拥有你就是最开心的最美好的时光",
            "爱情最美好的是，在繁华处戛然而止",
            "世界太喧嚣，感谢那时候美好的你，爱上并不美好的我",
            "有些事情我们必须放手，才有精力去迎接更美好的生活",
            "初中最美好的事情莫过于一抬头就能见到喜欢的人",
            "喜欢你的名字你的笑，会让我想起曾经在一起的美好",
            "幸福就是平凡生活中的点点滴滴",
            "幸福永远存在于人类不安的追求中，而不存在于和谐于稳定之中",
            "幸福是生活美好了，家庭和睦了",
            "在通往幸福的路上，我一路狂奔",
            "幸福不在于你是谁或者你拥有什么，而仅仅取决于你的心态",
            "有很多人是用青春的幸福作了成功的代价",
            "远方而生活、而努力奋斗，其实也是一种人生方式的选择"
    };

    public static String createKey(){
        return String.valueOf(random.nextInt());
    }

    /**
     * 创建倒序index
     * @return
     */
    public static String createReverseKey(){
        return new StringBuffer(String.valueOf(random.nextLong())).reverse().toString();
    }

    /**
     * 闯将游戏id
     * @return
     */
    public static int createGameId(){
        return random.nextInt(gameRange);
    }

    /**
     * 创建游戏角色编号
     * @return
     */
    public static int createRoleId(){
        return random.nextInt(roleRange);
    }

    /**
     * 创建游戏玩家存活数量
     * @return
     */
    public static int createPlayerId(){
        return  random.nextInt(playerRange);
    }

    /**
     * 创建游戏玩家并发id
     * @return
     */
    public static int createSessionId(){
        return  random.nextInt(sessionRange);
    }

    /**
     * 创建地图id
     * @return
     */
    public static int createMapId(){
        return  random.nextInt(mapRange);
    }

    /**
     * 创建玩家行为
     * @return
     */
    public static int createActionType(){
        return  random.nextInt(actionTypeRange);
    }

    /**
     * 创建道具id
     * @return
     */
    public static int createItemId(){
        return  random.nextInt(itemRange);
    }

    /**
     * 创建怪物种类
     * @return
     */
    public static int createMonsterType(){
        return  random.nextInt(monsterTypeRange);
    }

    /**
     * 创建怪物id
     * @return
     */
    public static int createMonsterId(){
        return  random.nextInt(monsterIdRange);
    }

    /**
     * 产生敏感词语
     * @return
     */
    public static String getOneBlackWord(){
        String blackWord = "";
        if(random.nextDouble() <= probability){//产生垃圾词汇
            blackWord = blackWords[random.nextInt(blackWords.length)];
        }
        return  blackWord;
    }

    /**
     * 随机发送消息内容，里面百分十四十情况下会在句子中随机位置插入脏词语
     * @return
     */
    public static String createOneMessageContent(){
        String sentence = sentences[random.nextInt(sentences.length)];
        StringBuilder sBuilder = new StringBuilder(sentence);
        int index = random.nextInt(sentence.length());
        sentence = sBuilder.insert(index,getOneBlackWord()).toString();
        return sentence;
    }


    /**
     * 字符串是否是null
     * @param str
     * @return
     */
    public static boolean isNull(String str){
        return (null == str
                || str.isEmpty());
    }
}