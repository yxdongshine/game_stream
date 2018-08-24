package com.game.util;

import java.util.Random;
public class IDGenerator {
	public static final int idlen = 32;
	static Random random = new Random(System.currentTimeMillis());

	public static synchronized String newGUID() {
		String str1 = DateUtil.nowStrYYYYMMddHHmmssSSS();
		String str2 = Math.abs(random.nextLong()) + "";
		String str3 = str1 + str2;
		int i = 32 - str3.length();
		for (int j = 0; j < i; ++j)
			str3 = str3 + "0";
		if (str3.length() > 32)
			str3 = str3.substring(0, 32);
		return str3;
	}

	public static synchronized String newNo(String paramString) {
		return paramString + newGUID().substring(paramString.length());
	}

	public static void main(String[] paramArrayOfString) {
		String str1 = newGUID();
		String str2 = newNo("MQ");
		System.out.println(str1);
		System.out.println(str1.length());
		System.out.println(str2);
		System.out.println(str2.length());
	}
}
