package com.game.instance

import java.util

import com.game.RedisPool.RedisUtils
import com.game.util.Constant
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import scala.collection.{mutable, JavaConversions}
import scala.collection.JavaConversions._


/**
 * Created by YXD on 2018/8/17.
 * 敏感词汇广播列表
 */
object SensitiveVocabularyList {

  @volatile private var instance: Broadcast[mutable.Set[String]] = null

  def getInstance(sc: SparkContext): Broadcast[mutable.Set[String]] = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          //这里敏感词汇数据获取redis
          val sensitiveVocabularyList: util.Set[String] = RedisUtils.sMembers(Constant.SYSTEM_PREFIX + Constant.SENSITIVE_VOCABULARY_LIST_KEY)
          instance = sc.broadcast(JavaConversions.asScalaSet(sensitiveVocabularyList))
        }
      }
    }
    instance
  }
}
