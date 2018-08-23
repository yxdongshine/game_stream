package com.game.log;

import java.util.Hashtable;
import org.apache.log4j.Logger;

public class Logging {
	private Logger logger;
	private String name;
	private static Hashtable<String, Logger> loggerMap = new Hashtable();
	private LogServerService logService = new LogServerService();

	private Logging(String paramString) {
		this.name = paramString;
		this.logger = ((Logger) loggerMap.get(paramString));
		if (this.logger != null)
			return;
		this.logger = Logger.getLogger(paramString);
		loggerMap.put(paramString, this.logger);
	}

	public void setName(String paramString) {
		this.name = paramString;
	}

	public static Logging getLogging(String paramString) {
		return new Logging(paramString);
	}

	public void debug(String paramString) {
		internal("DEBUG", paramString);
	}

	public void error(String paramString) {
		internal("ERROR", paramString);
	}

	public void fatal(String paramString) {
		internal("FATAL", paramString);
	}

	public void info(String paramString) {
		internal("INFO", paramString);
	}

	private void internal(String paramString1, String paramString2) {
		String str = "log4j";
		StackTraceElement[] arrayOfStackTraceElement = Thread.currentThread()
				.getStackTrace();
		ParaMap localParaMap = new ParaMap();
		localParaMap.put("rid", ReqUtils.getRId());
		localParaMap.put("level", paramString1);
		if (arrayOfStackTraceElement.length >= 3) {
			localParaMap.put("rpath",
					arrayOfStackTraceElement[3].getClassName() + "."
							+ arrayOfStackTraceElement[3].getMethodName());
			localParaMap.put("rline", Integer
					.valueOf(arrayOfStackTraceElement[3].getLineNumber()));
		}
		localParaMap.put("catalog", this.name);
		localParaMap.put("ts", Long.valueOf(System.currentTimeMillis()));
		localParaMap.put("content", paramString2);
		if ("remote".equals(str))
			LogClient.addLog(localParaMap);
		else if ("log4j".equals(str))
			try {
				this.logService.appendToFile(localParaMap.toString());
			} catch (Exception localException) {
				localException.printStackTrace();
			}
		else
			System.out.println(paramString2);
	}

	public static void main(String[] paramArrayOfString) throws Exception {
		Logging localLogging = getLogging("orderInfo/泊位号");
		localLogging.fatal("mmmmmmmmmmmmmm");
		LogClient.start();
		Thread.sleep(2147483647L);
	}
}
