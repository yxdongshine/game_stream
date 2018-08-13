package scala.com.game

import com.game.util.Constant
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{KafkaUtils}

/**
 * Created by YXD on 2018/8/11.
 *
 * 指标类容：
 *  1.某段时间内某玩家恶意攻击-黑名单操作
 *    （playerid）
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
    val ssc = new StreamingContext(conf,Seconds(10))
    //设置Checkpointing
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

  def main(args: Array[String]) {
    val ssc = createSsc
    val pt = initKafka()
    //创建stream
    val dStream:InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, pt._1,pt._2)

  }
}
