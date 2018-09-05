package com.game.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
/**
 * Created by YXD on 2018/8/11.
 */
public class DateUtil {

    public static final String FORMAT_SECOND = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_MINUTE = "yyyy-MM-dd HH:mm";

    public synchronized static Long getSystemTime(){
        return System.currentTimeMillis();
    }

    /**
     * 当前时间的秒
     * @return
     */
    public synchronized static Long getTime(String formatStr){
        long secondTime = System.currentTimeMillis();
        try {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(formatStr);
            String dateStr = localSimpleDateFormat.format(secondTime);
            secondTime = localSimpleDateFormat.parse(dateStr).getTime();
        }catch  (Exception e) {
            e.printStackTrace();
        }
        return secondTime;
    }

    public static String sub(Date paramDate1, Date paramDate2) {
        long l1 = paramDate1.getTime() / 1000L;
        long l2 = paramDate2.getTime() / 1000L;
        long l3 = l2 - l1;
        long l4 = l3 / 24L / 60L / 60L;
        long l5 = (l3 - (l4 * 24L * 60L * 60L)) / 3600L;
        long l6 = (l3 - (l4 * 24L * 60L * 60L) - (l5 * 60L * 60L)) / 60L;
        long l7 = l3 - (l4 * 24L * 60L * 60L) - (l5 * 60L * 60L) - (l6 * 60L);
        String str1 = l4 + "天 ";
        String str2 = l5 + "小时 ";
        String str3 = l6 + "分钟 ";
        String str4 = l7 + "秒";
        String str5 = str1 + str2 + str3 + str4;
        return str5;
    }

    public static int getProcess(Date paramDate1, Date paramDate2) {
        long l1 = paramDate1.getTime() / 1000L;
        long l2 = paramDate2.getTime() / 1000L;
        long l3 = now().getTime() / 1000L;
        long l4 = l2 - l3;
        long l5 = l2 - l1;
        Double localDouble = Double.valueOf(l4 * 100.0D / l5);
        int i = 100 - localDouble.intValue();
        if (i > 100)
            i = 100;
        if (i < 0)
            i = 0;
        return i;
    }

    public static String getStr(Date paramDate) {
        if (paramDate == null)
            return null;
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return localSimpleDateFormat.format(paramDate);
    }

    public static String getStr(long paramLong) {
        long l = now().getTime();
        Date localDate = new Date(l + paramLong);
        return getStr(localDate);
    }

    public static String nowStr() {
        return getStr(now());
    }

    public static String nowStrYYYYMMddHHmmssSSS() {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                "yyyyMMddHHmmssSSS");
        String str = localSimpleDateFormat.format(now());
        return str;
    }

    public static String nowStr2() {
        return getStr(new Date());
    }

    public static long nowTime() {
        Date localDate = now();
        long l = localDate.getTime();
        return l;
    }

    public static Date now() {
        return new Date();
    }

    public static Date getDate(String paramString) {
        Date localDate = null;
        try {
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            localDate = localSimpleDateFormat.parse(paramString);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return localDate;
    }

    public static int getInt(double paramDouble) {
        return new Double(paramDouble).intValue();
    }

    public static String uuid() {
        UUID localUUID = UUID.randomUUID();
        return localUUID.toString();
    }

    public static boolean isNull(String paramString) {
        return ((paramString == null) || ("".equals(paramString)) || (paramString
                .equalsIgnoreCase("null")));
    }

    public static String nullToEmpty(String paramString) {
        if ((paramString == null) || ("".equals(paramString))
                || (paramString.equalsIgnoreCase("null")))
            return "";
        return paramString;
    }

    public static String emptyToNull(String paramString) {
        if ((paramString == null) || ("".equals(paramString))
                || (paramString.equalsIgnoreCase("null")))
            return null;
        return paramString;
    }

    public static Date adjust(Date paramDate) {
        long l1 = nowTime();
        long l2 = paramDate.getTime();
        long l3 = new Date().getTime();
        long l4 = l2 + l3 - l1;
        Date localDate = new Date(l4);
        return localDate;
    }

    public static String nowLocal() {
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS");
        String str = localSimpleDateFormat.format(new Date());
        return str;
    }

    public static void main(String[] paramArrayOfString) throws Exception {
       /* SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        Date localDate1 = localSimpleDateFormat.parse("2012-03-01 10:00:00");
        Date localDate2 = localSimpleDateFormat.parse("2012-03-01 10:06:00");
        System.out.println(getProcess(localDate1, localDate2));
        System.out.println(nowLocal());*/
        System.out.println(getTime(FORMAT_SECOND));
    }

}
