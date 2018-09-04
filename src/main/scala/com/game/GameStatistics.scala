package scala.com.game

import java.util
import com.alibaba.fastjson.{JSONObject, JSON}
import com.game.InnerGate.InnerConnectPool
import com.game.Mail.{MailBody, MailAccount, MailUtils}
import com.game.RedisPool.RedisUtils
import com.game.bean.GameTarget
import com.game.instance.{LogInstance, SensitiveVocabularyList, WhiteList}
import com.game.mysql.Jdbc.Dao.GameTargetDao
import com.game.mysql.Jdbc.Impl.GameTargetImpl
import com.game.mysql.dbcp.DBManager
import com.game.systeminfrastructure.SingleChannel
import com.game.util.{DateUtil, Constant}
import kafka.serializer.StringDecoder
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{rdd, SparkConf}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{KafkaUtils}

import scala.collection.immutable.IndexedSeq
import scala.collection.{JavaConversions, mutable}
import org.apache.kafka.clients.consumer.ConsumerConfig

/**
 * Created by YXD on 2018/8/28.
 */
object GameStatistics {

  def createSsc():StreamingContext ={
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(Constant.APP_NAME)
      //.set("spark.default.parallelism","60")
      .set("spark.streaming.receiver.writeAheadLog.enable","true")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    conf.registerKryoClasses(Array(classOf[GameMessage]))

    val ssc = new StreamingContext(conf,Seconds(Constant.BATCH_SECONDS))
    //设置Checkpointing 主要checkpoint 配置等
    ssc.checkpoint(Constant.CHECK_POINT_PATH)
    ssc
  }

  def initKafka(): (Map[String, String], Set[String]) = {
    val params = Map(
      "metadata.broker.list" ->Constant.KAFKA_CONNECT,
      //"zookeeper.connect" -> Constant.ZOOKEEPER_CONNECT,
      "group.id" -> Constant.GROUP_ID
      //"auto.offset.reset" -> "latest"
    )
    val topicsSet = Set("gamestream")
    (params,topicsSet)
  }

  /**
   * 格式化数据流
   * @param messagedStream
   * @return
   */
  def messageFormattedStreamFun(messagedStream: DStream[String]): DStream[GameMessage] ={
    messagedStream.map(line => {
      // 解析成GameMessage类
      val jsonObj: JSONObject = JSON.parseObject(line.toString)
      /* jsonObj match {
         case jsonObj.isInstanceOf =>{
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
       }*/
      if (jsonObj.isEmpty) {
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
      }
    })
      .filter(_.isDefined) //不为空(None)的元素
      .map(_.get)
  }

  /**
   * 判定黑名单操作
   * /**
   * 1.将每一条记录value设置为1
   * 2.窗口实时统计
   * 3.元祖 value大于X次的数据为恶意操作
   * 4.广播过滤掉白名单
   * 5.分区写入redis
   */
   * @param messageFormattedStream
   */
  def determineBlackListFun(messageFormattedStream: DStream[GameMessage]) = {
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
          rdd.filter(rdd => !whitePlayerList.value.contains(rdd._1.toString))
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
                //LogInstance.getInstance().info(""+record._1)
              })
              if(!blackPlayerList.isEmpty){
                // 这里更新黑名单
                RedisUtils.sAdd(Constant.SYSTEM_PREFIX + Constant.BLACK_LIST_KEY,JavaConversions.asJavaSet(blackPlayerList))
              }
            }
          )
        }
      )
  }

  /**
   *  1.transform 将流内数据转换 获取RedisUtils 黑名单列表数据
   *  2.match 模式匹配 直接过路
   *    注意：foreachRDD foreachPartition 只是在输出action优化，其他转换因子都不需要
   */
  def messageFormattedFilterStreamFun(messageFormattedStream: DStream[GameMessage]): DStream[GameMessage] = {
    messageFormattedStream.transform(
      rdd => {
        val blackPlayerList: util.Set[String] = RedisUtils.sMembers(Constant.SYSTEM_PREFIX + Constant.BLACK_LIST_KEY)
        rdd.filter(rdd => !blackPlayerList.contains(rdd.playerId))
      }
    )
  }

  /**
   *  1.只要消息actionType=4发送消息的动作
   *  2.从广播中获取敏感词汇
   *  3.如果含有就用特殊符号替换
   *  4.再转发session
   */
  def filterBlackWordsFun(messageFormattedFilterStream: DStream[GameMessage]) = {
    messageFormattedFilterStream.filter(
       gm => { Constant.ACTION_TYPE_SEND_MESSAGE == gm.actionType }
    )
      .foreachRDD(rdd =>{
      //这里获取分布式rdd记录能获取到，因为是广播变量
      val sensitiveVocabularyList:Broadcast[mutable.Set[String]] = SensitiveVocabularyList.getInstance(rdd.sparkContext)
      rdd.foreachPartition(partitionOfRecords =>{
        //这里优化 获取发送网关连接
        val innerGate = InnerConnectPool.getInstance()
        partitionOfRecords.foreach(record =>{
          var message = record.mesContent
          sensitiveVocabularyList.value.foreach(value =>{
            if(record.mesContent.contains(value)){
              message = record.mesContent.replace(value,Constant.SENSITIVE_VOCABULARY_REPLACE_CHARS)
            }
          })
          //然后每条消息都需要转发给网关服务网
          innerGate.sendMessage(record.key,record.index,record.gameId,record.playerId,
            record.roleId,record.sessionId,record.mapId,record.timeStamp,record.actionType,
            record.itemId,record.monsterType,record.monsterId,record.attackedRoleId,message
          )
        })
      })
    })
  }

  /**
   * 1.在过滤后的合法数据流里面得到挂机记录
   * 2.reduceByKeyAndWindow 统计每分钟内出现的次数
   *    注意：这里需要将value设置数值型
   * 3.过滤得到大于等于挂机记录判定值
   * 4.分区发送邮件通知
   */
  def filterHuangUpFun(messageFormattedFilterStream: DStream[GameMessage]) = {
    messageFormattedFilterStream.filter(gm =>{
      Constant.ACTION_TYPE_HANG_UP == gm.actionType
    })
      .map(gm =>{
      ((gm.gameId,gm.sessionId,gm.playerId),1)
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
      rdd.foreachPartition(partitionOfRecords =>{
        //这里调用邮件实体类
        val account = new MailAccount();
        account.setAccount(SingleChannel.getConfig("send_mail_account"));
        account.setPassword(SingleChannel.getConfig("send_mail_password"));
        account.setSmtpHost(Constant.SMTP_HOST);
        account.setSmtpPort(Constant.SMTP_PORT);
        val instance = MailUtils.newInstance(account)
        partitionOfRecords.foreach(record =>{
          LogInstance.getInstance.info(record.toString())
          //发送每一条邮件
          val mailBody = new MailBody();
          mailBody.setSubject(Constant.RECEIVE_MAIL_SUBJECT);
          val warnContent = Constant.RECEIVE_MAIL_CONTENT
            .replace("playerId",record._1._3.toString)
            .replace("gameId",record._1._1.toString)
          mailBody.setContent(warnContent)
          //instance.send(SingleChannel.getConfig("send_mail_account"),mailBody)
        })
      })
    })
  }

  /**
   * 实时数据图展示
   *   某游戏某时刻玩家在线数量
   */
  def realTimePlayerTargetFun(messageFormattedFilterStream: DStream[GameMessage]) = {
    messageFormattedFilterStream.map(rdd =>{
      ((rdd.gameId,rdd.sessionId,rdd.playerId),1)
    })
      .reduceByKeyAndWindow(
        (beforeValue:Int , nowValue:Int) => {
          beforeValue + nowValue
        },
        Seconds(Constant.REAL_TIME_TARGET_WINDOW_LENGTH_SECONDS),
        Seconds(Constant.REAL_TIME_TARGET_INTERVAL_SECONDS)
      )
      .map(rdd =>{
      (rdd._1._1, 1)//游戏gameid game数量
    })
      .reduceByKey((beforeValue:Int , nowValue:Int) =>{beforeValue + nowValue})
      .foreachRDD(rdd =>{
      rdd.foreachPartition(partitionRecords =>{
        //优化方式
        val conn = DBManager.getConn
        partitionRecords.foreach(record => {
          val gt = {
            new GameTarget(record._1, Constant.TARGET_TYPE_PLAYER, 0L, record._2, DateUtil.getSystemTime,"")
          }
          val gtDao = new GameTargetImpl()
          gtDao.add(gt,conn)
        })
        //这里关闭连接池
        DBManager.closeConn(conn)
      })
    })
  }

  /**
   * 实时数据图展示
   *   某游戏某时刻某角色使用数量
   */
  def realTimeRoleTargetFun(messageFormattedFilterStream: DStream[GameMessage]) = {
    messageFormattedFilterStream.map(rdd =>{
      ((rdd.gameId,rdd.sessionId),rdd.roleId)
    })
      .groupByKeyAndWindow(
        Seconds(Constant.REAL_TIME_TARGET_WINDOW_LENGTH_SECONDS),
        Seconds(Constant.REAL_TIME_TARGET_INTERVAL_SECONDS)
      )
      .map(rdd =>{
      ((rdd._1._1,rdd._2.toList.distinct.head),1)
    })
      .reduceByKeyAndWindow(
        (beforeValue:Int,nowValue:Int) =>{
          beforeValue + nowValue
        },
        Seconds(Constant.REAL_TIME_TARGET_WINDOW_LENGTH_SECONDS),
        Seconds(Constant.REAL_TIME_TARGET_INTERVAL_SECONDS)
      )
      .foreachRDD(rdd =>{
      rdd.foreachPartition(partitionRecords =>{
        //优化方式
        val conn = DBManager.getConn
        partitionRecords.foreach(record => {
          val gt = {
            new GameTarget(record._1._1, Constant.TARGET_TYPE_ROLE, record._1._2, record._2, DateUtil.getSystemTime,"")
          }
          val gtDao = new GameTargetImpl()
          gtDao.add(gt,conn)
        })
        //这里关闭连接池
        DBManager.closeConn(conn)
      })
    })
  }

  /**
   *
   * @param messageFormattedFilterStream
   */
  def realTimeSumSesssionTargetFun(messageFormattedFilterStream: DStream[GameMessage]) = {
    messageFormattedFilterStream.map(rdd =>{
      ((rdd.gameId,rdd.sessionId,rdd.playerId),1)
    })
      .reduceByKeyAndWindow(
        (beforeValue:Int,nowValue:Int) =>{
           1
        },
        Seconds(Constant.REAL_TIME_TARGET_WINDOW_LENGTH_SECONDS),
        Seconds(Constant.REAL_TIME_TARGET_INTERVAL_SECONDS)
      )
      .updateStateByKey((values: Seq[Int], state: Option[(Long, Int)]) =>{
      // 1. 获取当前key传递的值
      val currentValue = values.sum

      // 2. 获取状态值
      val preValue = state.getOrElse((0L, 0))._1
      val preState = state.getOrElse((0L, 0))._2
      // 3. 更新状态值
      if (currentValue == 0) {
        Some((preValue + currentValue, preState + 1))
      } else {
        // 有新数据
        Some((preValue + currentValue, 0))
      }
    })
      .map(rdd =>{
      (rdd._1,rdd._2._1)
    })
      .foreachRDD(rdd =>{
      rdd.foreachPartition(partitionRecords => {
        partitionRecords.foreach(record => {
          //因为在线人数指标只会一个值
          val key = Constant.SYSTEM_PREFIX + Constant.REAL_TIME_TARGET_SESSION_NUMBER_KEY +
            record._1._1 + Constant.SYSTEM_DELIMITED_SYMBOL +record._1._2
          RedisUtils.set(key,record._2.toString)
        })
      })
    })
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
    //dStream.checkpoint(Seconds(5 * Constant.BATCH_SECONDS))
    //对于数据流来说 目前只需要value，针对分区的key暂时不处理
    val messagedStream: DStream[String] = dStream.map(_._2)
    //第一步消息格式化
    val messageFormattedStream: DStream[GameMessage] = messageFormattedStreamFun(messagedStream)
    //messageFormattedStream.persist(StorageLevel.MEMORY_AND_DISK)
    /**
     * 先测试大数据量下kafka数据丢失问题
     */
    /*messageFormattedStream.foreachRDD(rdd =>{
      rdd.foreachPartition(partitionRecords =>{
        partitionRecords.foreach(record =>{
          LogInstance.getInstance().info(""+record.index)
        })
      })
    })*/
    //第二步判定黑名单操作
    determineBlackListFun(messageFormattedStream)
    //第三步将黑名单数据过滤掉
    val messageFormattedFilterStream: DStream[GameMessage] = messageFormattedFilterStreamFun(messageFormattedStream)
    messageFormattedFilterStream.persist(StorageLevel.MEMORY_AND_DISK);//后面多次用到格式化后的数据 这里采用缓存
    //第四步实时过滤掉敏感词汇并调用转发session
    filterBlackWordsFun(messageFormattedFilterStream)
    //第五步判断是否存在挂机行为
    filterHuangUpFun(messageFormattedFilterStream)
    //第六步实时在线玩家游戏number
    realTimePlayerTargetFun(messageFormattedFilterStream)
    //第七步实时在线角色受欢迎度 数量
    //realTimeRoleTargetFun(messageFormattedFilterStream)
    //第八步实时统计该游戏累积玩家数量
    //realTimeSumSesssionTargetFun(messageFormattedFilterStream)
    //
    //最后也要返回StreamingContext
    ssc
  }

  def main(args: Array[String]): Unit = {
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