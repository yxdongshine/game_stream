package com.game.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.game.bean.GameMessage;
import com.game.data.Simulation;
import com.game.kafka.producer.kafkaProducer;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SendDataTask implements Runnable {

	private Long indexStart;//开始索引
	private Long indexEnd;//开始索引
	private int num;//发送批次间隔
	
	public void run() {
		// TODO Auto-generated method stub
		List<GameMessage> gmList = new ArrayList<GameMessage>();
		for (Long K = indexStart; K <= indexEnd ; K++) {
			gmList.add(Simulation.createGameMessage(K));
			if( 0 == K % num
					&& 0 < gmList.size()){
				//如果批次间隔次数就发送一次
				kafkaProducer.getReportProducerInstance().sendListMessage(gmList);
				// 清空gmList
				gmList.clear();
			}
			/*if(K == (indexEnd-1)) {//最后10条也要发送
				kafkaProducer.getReportProducerInstance().sendListMessage(gmList);
				gmList.clear();
			}*/
		}

	}

	public Long getIndexStart() {
		return indexStart;
	}

	public void setIndexStart(Long indexStart) {
		this.indexStart = indexStart;
	}

	public Long getIndexEnd() {
		return indexEnd;
	}

	public void setIndexEnd(Long indexEnd) {
		this.indexEnd = indexEnd;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	

}
