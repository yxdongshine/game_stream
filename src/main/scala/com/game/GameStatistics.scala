package scala.com.game

import java.util

import com.alibaba.fastjson.{JSONObject, JSON}
import com.game.RedisPool.RedisUtils
import com.game.instance.WhiteList
import com.game.util.Constant
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{KafkaUtils}

import scala.collection.immutable.IndexedSeq
import scala.collection.{JavaConversions, mutable}

/**
 * Created by YXD on 2018/8/11.
 *
 * 指标类容：
 *  1.实时更新某玩家恶意攻击-黑名单操作
 *    （playerid） 我们规定在一分钟之内点击数超过X次就为恶意攻击
 *    （广播变量-排除白名单）
 *  2.过滤掉黑名单产生的行为
 *  3.实时过滤消息内容敏感词汇（实时消息频道推送及屏蔽敏感词汇）
 *  4.实时统计是否存在挂机行为（实时邮件通知）
 *  5.某个游戏某个玩家使用某个角色 游戏时间
 *    某个游戏某个玩家游戏时间
 *    某个游戏的活跃时间
 *  6.某个游戏某个玩家行为特征分析
 *      攻击主宰时间差及数量
 *      攻击暴君时间差及数量
 *      攻击怪物数量
 *      攻击人物数量
 *      购买道具数量
 */
class GameStatistics {

  def createSsc():StreamingContext ={
    val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName(Constant.APP_NAME)
    val ssc = new StreamingContext(conf,Seconds(Constant.BATCH_SECONDS))
    //设置Checkpointing 主要checkpoint 配置等
    ssc.checkpoint(Constant.CHECK_POINT_PATH)
    ssc
  }

  def initKafka(): (Map[String, String], Set[String]) = {
    val params = Map(
      "zookeeper.connect" -> Constant.ZOOKEEPER_CONNECT,
      "group.id" -> Constant.GROUP_ID
    )
    val topicsSet = Set("gamestream")
    (params,topicsSet)
  }

  /**
   * 处理的数据流及checkpoint配置 rdd数据及操作方法等
   * @return
   */
  def createCheckPointStreamContext():StreamingContext ={
    val ssc = createSsc
    val pt = initKafka()
    //创建stream
    val dStream:InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, pt._1,pt._2)
    //下面开始处理流 同时也是为了rdd 和 元数据的checkpoint
    //设置stream checkpoint时间
    dStream.checkpoint(Seconds(5 * Constant.BATCH_SECONDS))
    //对于数据流来说 目前只需要value，针对分区的key暂时不处理
    val messagedStream: DStream[String] = dStream.map(_._2)
    //第一步消息格式化
    val messageFormattedStream: DStream[GameMessage] = messagedStream.map(line => {
        // 解析成GameMessage类
        val jsonObj: JSONObject = JSON.parseObject(line.toString)
        jsonObj match {
          case JSONObject =>{
            Some(
              GameMessage(
                jsonObj.getString("key"),
                jsonObj.getLong("index"),
                jsonObj.getInteger("gameId"),
                jsonObj.getLong("playerId"),
                jsonObj.getLong("roleId"),
                jsonObj.getLong("sessionId"),
                jsonObj.getLong("mapId"),
                jsonObj.getLong("timeStamp"),
                jsonObj.getInteger("actionType"),
                jsonObj.getLong("itemId"),
                jsonObj.getInteger("monsterType"),
                jsonObj.getLong("monsterId"),
                jsonObj.getLong("attackedRoleId"),
                jsonObj.getString("mesContent")
              )
            )
          }
          case _ => None
        }
        /*if (jsonObj.isEmpty) {
          None
        } else {
          Some(
            GameMessage(
              jsonObj.getString("key"),
              jsonObj.getLong("index"),
              jsonObj.getInteger("gameId"),
              jsonObj.getLong("playerId"),
              jsonObj.getLong("roleId"),
              jsonObj.getLong("sessionId"),
              jsonObj.getLong("mapId"),
              jsonObj.getLong("timeStamp"),
              jsonObj.getInteger("actionType"),
              jsonObj.getLong("itemId"),
              jsonObj.getInteger("monsterType"),
              jsonObj.getLong("monsterId"),
              jsonObj.getLong("attackedRoleId"),
              jsonObj.getString("mesContent")
            )
          )
        }*/
    })
    .filter(_.isDefined) //不为空(None)的元素
    .map(_.get)

    //判定黑名单操作
    /**
     * 1.将每一条记录value设置为1
     * 2.窗口实时统计
     * 3.元祖 value大于X次的数据为恶意操作
     * 4.广播过滤掉白名单
     * 5.分区写入redis
     */
    messageFormattedStream.map(gm => {
      (gm.playerId,1)
    })
    .reduceByKeyAndWindow(
        (beforeValue:Int,afterValue:Int) =>{ beforeValue + afterValue},
        Seconds(Constant.WINDOW_LENGTH_SECONDS),//窗口长度
        Seconds(Constant.WINDOW_INTERVAL_SECONDS)//滑动距离
      )
    .filter(tuple => tuple._2 >= Constant.MALICIOUS_ATTACK_PLAYER_SEND_NUM)
    .transform(//.foreachRDD( //这里是可以用foreachRDD 但是只能用一次，后面保存操作没法用
      rdd => {
        //获取白名单
        val whitePlayerList: Broadcast[mutable.Set[String]] = WhiteList.getInstance(rdd.sparkContext)
        rdd match {
          case (Long, Int) => {
            rdd.filter(rdd => whitePlayerList.value.contains(rdd._1.toString))
          }
        }
      }
      )
    .foreachRDD(//这里就是每个区含有的黑名单数据
      rdd => {
        rdd.foreachPartition(
          partitionOfRecords =>{
            //优化方式一：这里优化每个区获取一次redis连接

            //优化方式二：整个分区组成集合写入redis
            var blackPlayerList:mutable.Set[String] = mutable.Set()
            partitionOfRecords.foreach(record => {
              blackPlayerList.+=(record._1.toString)
            })
            // 这里更新黑名单
            RedisUtils.sAdd(Constant.SYSTEM_PREFIX + Constant.BLACK_LIST_KEY,JavaConversions.asJavaSet(blackPlayerList))
          }
        )
      }
      )

    /**
     * 将黑名单数据过滤掉
     *  1.transform 将流内数据转换 获取RedisUtils 黑名单列表数据
     *  2.match 模式匹配 直接过路
     *    注意：foreachRDD foreachPartition 只是在输出action优化，其他转换因子都不需要
     *
     */
    val messageFormattedFilterStream: DStream[GameMessage] = messageFormattedStream.transform(
      rdd => {
        val blackPlayerList: util.Set[String] = RedisUtils.sMembers(Constant.SYSTEM_PREFIX + Constant.BLACK_LIST_KEY)
        rdd match {
          case GameMessage => {
            rdd.filter(rdd => blackPlayerList.contains(rdd.playerId))
          }
        }
      }
    )


    //
    //
    //最后也要返回StreamingContext
    ssc
  }

  def main(args: Array[String]) {
    //checkpoint目录和创建的匿名函数
    val ssc = StreamingContext.getOrCreate(Constant.CHECK_POINT_PATH,() => createCheckPointStreamContext())
    ssc.start();//启动实时流
    ssc.awaitTermination();//等待认为中断 一直监听
  }
}


/**
 * 消息实体类
 */
case class GameMessage(
                        key: String ,
                        index: Long,
                        gameId: Int ,
                        playerId: Long ,
                        roleId: Long ,
                        sessionId: Long ,
                        mapId: Long ,
                        timeStamp: Long ,
                        actionType: Int ,
                        itemId: Long ,
                        monsterType: Int ,
                        monsterId: Long ,
                        attackedRoleId: Long,
                        mesContent: String
                        )