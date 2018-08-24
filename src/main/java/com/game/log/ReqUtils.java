package com.game.log;

import com.game.util.IDGenerator;
import com.game.util.StrUtil;

public class ReqUtils {
	private static ThreadLocal<String> rid = new ThreadLocal();
	public static String ridKey = "_rid";

	public static String getRId() {
		return ((String) rid.get());
	}

	public static void initRId(ParaMap paramParaMap) {
		String str = paramParaMap.getString(ridKey);
		if (StrUtil.isNull(str))
			str = IDGenerator.newNo("R");
		rid.set(str);
	}
}
