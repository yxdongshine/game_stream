package com.game.log;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.game.util.StrUtil;
import org.apache.commons.io.FileUtils;

import com.alibaba.fastjson.JSONObject;

public class LogServerService {
	@SuppressWarnings("rawtypes")
	public HashMap write(HashMap paramParaMap) throws Exception {
		HashMap localParaMap = new HashMap();
		String str = (String) paramParaMap.get("content");
		appendToFile(str);
		return localParaMap;
	}

	public ParaMap log(ParaMap paramParaMap) throws Exception {
		ParaMap localParaMap = new ParaMap();
		String str1 = paramParaMap.getString("catalog");
		String str2 = paramParaMap.getString("content");
		String str3 = getFilePath(str1);
		File localFile = new File(str3);
		if (!(localFile.exists()))
			FileUtils.writeStringToFile(localFile, "");
		FileWriter localFileWriter = new FileWriter(str3, true);
		localFileWriter.write(str2 + "\n");
		localFileWriter.close();
		return localParaMap;
	}

	public void appendToFile(String paramString) throws Exception {
		JSONObject localJSONObject = JSONObject.parseObject(paramString);
		String str1 = localJSONObject.getString("catalog");
		String str2 =  "false";
		if ((str1.startsWith("framework"))
				&& (!("true".equals(str2.toLowerCase()))))
			return;
		String str3 = getFilePath(str1);
		File localFile = new File(str3);
		if (!(localFile.exists()))
			FileUtils.writeStringToFile(localFile, "");
		FileWriter localFileWriter = new FileWriter(str3, true);
		localFileWriter.write(formatContent(localJSONObject) + "\n");
		localFileWriter.close();
	}

	public String formatContent(JSONObject paramJSONObject) {
		StringBuffer localStringBuffer = new StringBuffer();
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
				"YYYY-MM-dd HH:mm:ss,SSS");
		localStringBuffer.append(localSimpleDateFormat.format(new Date(
				paramJSONObject.getLong("ts").longValue())));
		localStringBuffer.append(" " + paramJSONObject.getString("rid"));
		localStringBuffer.append(" [" + paramJSONObject.getString("rpath")
				+ ":" + paramJSONObject.getString("rline") + "]");
		localStringBuffer.append(" " + paramJSONObject.getString("level"));
		localStringBuffer.append(" " + paramJSONObject.getString("content"));
		return localStringBuffer.toString();
	}

	public String getFilePath(String paramString) {
		String str1 = "D:\\fileroot";
		if (StrUtil.isNull(str1))
			str1 = "/app/fileRoot/logs";
		SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
				"YYYYMMdd");
		String str2 = localSimpleDateFormat.format(new Date());
		String str3 = str1 + "/logs/" + str2 + "/" + paramString + ".txt";
		return str3;
	}

	public static void main(String[] paramArrayOfString) throws Exception {
		LogServerService localLogServerService = new LogServerService();
		ParaMap localParaMap = new ParaMap();
		localParaMap.put("rid", Long.valueOf(System.currentTimeMillis()));
		localParaMap.put("level", "INFO");
		localParaMap.put("rpath", "com.safdaf.dbdbd.method");
		localParaMap.put("rline", Integer.valueOf(100));
		localParaMap.put("content", "...AAA...");
		localParaMap.put("catalog", "trade");
		localParaMap.put("ts", Long.valueOf(System.currentTimeMillis()));
		localLogServerService.appendToFile(localParaMap.toString());
	}
}
