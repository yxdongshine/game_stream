package scala.com.game

import com.game.util.Constant
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{KafkaUtils, StreamDataManager}

/**
 * Created by YXD on 2018/8/11.
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
