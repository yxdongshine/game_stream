package com.game.kafka.producer;

/**
 * Created by YXD on 2018/8/11.
 */
import com.game.bean.GameMessage;
import com.game.util.Constant;
import com.game.util.DateUtil;
import groovy.transform.Synchronized;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import kafka.javaapi.producer.Producer;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class kafkaProducer {

    //构造私有
    private kafkaProducer(){

    }
    private static Producer<String, String> producer = null;
    private static kafkaProducer reportProducer = null;
    /**
     * 单利获取生产者
     * @return
     */
    public synchronized static kafkaProducer getReportProducerInstance(){
        if(null == reportProducer){
            reportProducer = new kafkaProducer();
            if(null == producer){
                producer = new Producer(init());
            }
        }
        return  reportProducer;
    }

    /**
     * 初始化kafka发送参数
     */
    public static ProducerConfig init(){
        Properties props = new Properties();
        props.put("metadata.broker.list", Constant.KAFKA_CONNECT);
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        if(Constant.isSync){//同步
            props.put("producer.type", "sync");
            props.put("request.required.acks", "1");//表示至少一个broker响应
        }else{
            props.put("producer.type", "async");
            props.put("request.required.acks", "1");//表示至少一个broker响应
            props.put("queue.buffering.max.ms", "5000");//本地缓冲最大时间
            props.put("queue.buffering.max.messages", "10000");//本地缓冲最大存储条数
            props.put("queue.enqueue.timeout.ms", "-1");//当本地缓冲满时候等待不丢弃数据
            props.put("batch.num.messages", "500");//一次最大发送条数
        }
        ProducerConfig config = new ProducerConfig(props);
        return config;
    }


    /**
     * 下面提供两种种发送消息方式
     */

    /**
     * 单条发送消息
     * @param gameMessage
     */
    public void sendSingleMessage(GameMessage gameMessage){
        KeyedMessage<String, String> data = new KeyedMessage<String, String>(Constant.TOPIC_NAME, gameMessage.getKey(), gameMessage.toJsonStr());
        producer.send(data);
    }

    /**
     * 多条消息发送
     * @param gmList
     */
    public void sendListMessage(List<GameMessage> gmList){
        List<KeyedMessage<String, String>> keyedMessageList = new ArrayList<KeyedMessage<String, String>>();
        for (int i=0; i< gmList.size(); i++ ){
            GameMessage gameMessage = gmList.get(i);
            KeyedMessage<String, String> KeyedMessage = new KeyedMessage<String, String>(Constant.TOPIC_NAME, gameMessage.getKey(), gameMessage.toJsonStr());
            keyedMessageList.add(KeyedMessage);
        }
        //批量发送
        producer.send(keyedMessageList);
        Constant.setSumMessNumber(Long.parseLong(keyedMessageList.size()+""));
        System.out.println("本次发送条数：" + keyedMessageList.size() + "   ####当前累积条数：" + Constant.getSumMessNumber());
    }


}
