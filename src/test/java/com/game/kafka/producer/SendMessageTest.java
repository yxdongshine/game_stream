package com.game.kafka.producer;

import com.game.Task.SendDataTask;
import com.game.executors.ThreadPool;

/**
 * Created by YXD on 2018/8/13.
 */
public class SendMessageTest {

    public static void main(String[] args) throws Exception{
        multThreadSendDataTest(4,1L,100000L,10);
    }

    public static void multThreadSendDataTest(int threadNum,Long start,Long end,int batchNum) throws Exception{
        Long singleThreadSendNum = (end - start)/threadNum;
        for (int i = 0; i < threadNum; i++) {
            SendDataTask sdTask = new SendDataTask();
            sdTask.setNum(batchNum);//batchNum条发送一次
            Long threadStartNum = start + i * singleThreadSendNum;
            Long threadEndNum = threadStartNum + singleThreadSendNum;
            sdTask.setIndexStart(threadStartNum);
            sdTask.setIndexStart(threadEndNum);
            ThreadPool.getInstance().addTask(sdTask);
        }
    }
}
