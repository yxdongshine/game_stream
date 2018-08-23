package com.game.log;

import com.ecaray.util.IDGenerator;
import com.ecaray.util.StringUtil;

public class ReqUtils {
	private static ThreadLocal<String> rid = new ThreadLocal();
	public static String ridKey = "_rid";

	public static String getRId() {
		return ((String) rid.get());
	}

	public static void initRId(ParaMap paramParaMap) {
		String str = paramParaMap.getString(ridKey);
		if (StringUtil.isNull(str))
			str = IDGenerator.newNo("R");
		rid.set(str);
	}
}
