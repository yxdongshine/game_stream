package com.game.log;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
public class LogClient extends TimerTask{
	private static Timer timer = new Timer();
	private static Stack<ParaMap> stack = new Stack();
	private ParaMap localObject;
	public static void addLog(ParaMap paramParaMap) {
		stack.push(paramParaMap);
	}

	public static void start() {
		LogClient localLogClient = new LogClient();
		timer.scheduleAtFixedRate(localLogClient, 0L, 1000L);
	}

	public void run() {
		try {
			if (stack.isEmpty())
				return;
			StringBuffer localStringBuffer = new StringBuffer();
			while (!(stack.isEmpty())) {
				localObject = (ParaMap) stack.pop();
				localStringBuffer.append(((ParaMap) localObject).toString());
			}
			Object localObject = localStringBuffer.toString();
			String str ="logUrl"
					+ "/upClient?module=base&service=LogServer&method=write";
			//HttpManager.getData(str, (String) localObject);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	public static void main(String[] paramArrayOfString) throws Exception {
		ParaMap localParaMap = new ParaMap();
		localParaMap.put("rid", Long.valueOf(System.currentTimeMillis()));
		localParaMap.put("levle", "INFO");
		localParaMap.put("content", "...AAA...");
		localParaMap.put("catalog", "bill");
		addLog(localParaMap);
		start();
	}
}
