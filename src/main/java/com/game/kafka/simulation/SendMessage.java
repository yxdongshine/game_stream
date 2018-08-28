package com.game.kafka.simulation;

import com.game.Task.SendDataTask;
import com.game.executors.ThreadPool;

/**
 * Created by YXD on 2018/8/13.
 */
public class SendMessage {

    public static void main(String[] args) throws Exception{
        multThreadSendDataTest(10,1L,100000000L,10);
    }

    public static void multThreadSendDataTest(int threadNum,Long start,Long end,int batchNum) throws Exception{
        Long singleThreadSendNum = (end - start + 1)/threadNum;
        for (int i = 0; i < threadNum; i++) {
            SendDataTask sdTask = new SendDataTask();
            sdTask.setNum(batchNum);//batchNum条发送一次
            Long threadStartNum = start + i * singleThreadSendNum;
            Long threadEndNum = (i + 1) * singleThreadSendNum;
            sdTask.setIndexStart(threadStartNum);
            sdTask.setIndexEnd(threadEndNum);
            ThreadPool.getInstance().addTask(sdTask);
        }
    }
}
