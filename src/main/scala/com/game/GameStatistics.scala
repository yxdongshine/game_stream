package scala.com.game

import java.util

import com.alibaba.fastjson.{JSONObject, JSON}
import com.game.InnerGate.InnerConnectPool
import com.game.Mail.{MailBody, MailAccount, MailUtils}
import com.game.RedisPool.RedisUtils
import com.game.instance.{SensitiveVocabularyList, WhiteList}
import com.game.systeminfrastructure.SingleChannel
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
    .setMaster("local[*]")
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
        Seconds(Constant.BLACK_LIST_WINDOW_LENGTH_SECONDS),//窗口长度
        Seconds(Constant.BLACK_LIST_WINDOW_INTERVAL_SECONDS)//滑动距离
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

    /**
     * 实时过滤掉敏感词汇并调用转发session
     *  1.只要消息actionType=4发送消息的动作
     *  2.reduceByKeyAndWindow 这里需要减少延时性操作 window：3s；interval：2s
     *  3.从广播中获取敏感词汇
     *  4.如果含有就用特殊符号替换
     *  5.再转发session
     */
    messageFormattedFilterStream.filter(gm =>({
        Constant.ACTION_TYPE_SEND_MESSAGE == gm.actionType}))
    .foreachRDD(rdd =>{
      //这里获取分布式rdd记录能获取到，因为是广播变量
      val sensitiveVocabularyList:Broadcast[mutable.Set[String]] = SensitiveVocabularyList.getInstance(rdd.sparkContext)
      rdd.foreachPartition(partitionOfRecords =>{
        //这里优化 获取发送网关连接
        val innerGate = InnerConnectPool.getInstance()
        partitionOfRecords.map(record =>{
          /*if(sensitiveVocabularyList.value.exists(value =>{
              record.mesContent.contains(value)
          })){//如果存在替换原来的敏感词汇
            record
          }*/
          sensitiveVocabularyList.value.foreach(value =>{
            if(record.mesContent.contains(value)){
              record.mesContent.replace(value,Constant.SENSITIVE_VOCABULARY_REPLACE_CHARS)
            }
          })
          //然后每条消息都需要转发给网关服务网
          innerGate.sendMessage(record.key,record.index,record.gameId,record.playerId,
            record.roleId,record.sessionId,record.mapId,record.timeStamp,record.actionType,
            record.itemId,record.monsterType,record.monsterId,record.attackedRoleId,record.mesContent
          )
        })
      })
    })

    /**
     *判断是否存在挂机行为
     * 1.在过滤后的合法数据流里面得到挂机记录
     * 2.reduceByKeyAndWindow 统计每分钟内出现的次数
     *    注意：这里需要将value设置数值型
     * 3.过滤得到大于等于挂机记录判定值
     * 4.分区发送邮件通知
      */
    messageFormattedFilterStream.filter(gm =>{
      Constant.ACTION_TYPE_HANG_UP == gm.actionType
    })
    .map(gm =>{
      ((gm.gameId,gm.playerId,gm.roleId,gm.sessionId,gm.timeStamp,gm.actionType),1)
    })
    .reduceByKeyAndWindow(
        (beforeValue:Int,nowValue:Int) =>{
          beforeValue + nowValue
        },
        Seconds(Constant.HANG_UP_WINDOW_LENGTH_SECONDS),//窗口长度
        Seconds(Constant.HANG_UP_WINDOW_INTERVAL_SECONDS)//滑动间隔
      )
    .filter(tuple =>{//得到挂机人员
      tuple._2 >= Constant.HANG_UP_SEND_NUM
    })
    .foreachRDD(rdd =>{
      rdd match {
        case ((Int,Long,Long,Long,Long,Int),Int) =>{
          rdd.foreachPartition(partitionOfRecords =>{
            //这里调用邮件实体类
            val account = new MailAccount();
            account.setAccount(SingleChannel.getConfig("send_mail_account"));
            account.setPassword(SingleChannel.getConfig("send_mail_password"));
            account.setSmtpHost(Constant.SMTP_HOST);
            account.setSmtpPort(Constant.SMTP_PORT);
            val instance = MailUtils.newInstance(account)
            partitionOfRecords.map(record =>{
              //发送每一条邮件
              val mailBody = new MailBody();
              mailBody.setSubject(Constant.RECEIVE_MAIL_SUBJECT);
              val warnContent = Constant.RECEIVE_MAIL_CONTENT
                .replace("playerId",record._1._2.toString)
                .replace("timeStamp",record._1._5.toString)
                .replace("gameId",record._1._1.toString)
                .replace("roleId",record._1._3.toString)
              mailBody.setContent(warnContent)
              instance.send(SingleChannel.getConfig("send_mail_account"),mailBody)
            })
          })
        }
      }
    })



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