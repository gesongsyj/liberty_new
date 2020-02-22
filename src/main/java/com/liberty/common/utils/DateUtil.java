package com.liberty.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间格式转化工具类
 * @author Administrator
 *
 */
public class DateUtil {
	/**
	 * 将Date类型的转换成String类型的
	 * @param date
	 * @param format
	 * @return
	 */
	public static String dateStr(Date date,String format){	
		 SimpleDateFormat sdf = new SimpleDateFormat(format);		
		return sdf.format(date);
		
	}

	/**
	 * 将String类型的转换成Date类型的
	 * @param dateStr
	 * @param format
	 * @return
	 */
	public static Date strDate(String dateStr,String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);		
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public static String getDate() {
		return dateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
	}
	
	public static String getDay() {
		return dateStr(new Date(), "yyyy-MM-dd");
	}

	public static String getDay(Date date) {
		return dateStr(date, "yyyy-MM-dd");
	}

	public static Date date2Day(Date date) {
		return strDate(dateStr(date, "yyyy-MM-dd"),"yyyy-MM-dd");
	}

	public static Date getNextDay(Date date) {
		return getSomeNextDay(date,1);
	}

	public static Date getSomeNextDay(Date date,int dayCount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, dayCount);
		return c.getTime();
	}

	public static Date getNext(Date date,int timeType,int timeCount) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(timeType, timeCount);
		return c.getTime();
	}

	public static Date getSomeDay(Date date,int offSet) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, offSet);
		return c.getTime();
	}
	
	public static Date getNextMonth(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, 1);
		return c.getTime();
	}
	
	public static Date getNextYear(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.YEAR, 1);
		return c.getTime();
	}
	
	public static long getNumberBetween(Date date1,Date date2,int mills) {
		// long difference = (date1.getTime()-date2.getTime())/86400000;
		long difference =  (date1.getTime()-date2.getTime())/mills;
        return Math.abs(difference);
	}
}
