package com.itcall.batch.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtil {

	private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

	public static final String[] PARSEPATTONS = {"yyyy-MM-dd", "yyyyMMdd", "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"};
	public static final String[] PARSEPATTERSFORTIME = {"yyyyMMddHHmmss"};
	public static final String[] PARSEPATTERSFORTIME1 = {"yyyyMMddHHmm"};
	public static final String[] PARSEPATTERSFORTIME4 = {"yyyyMMddHHmmssSS"};
	public static final String DATEFORMAT = "yyyy-MM-dd";
	public static final String DATEFORMAT2 = "yyyyMMdd";
	public static final String DATEFORMATFORTIME = "yyyyMMddHHmmss";
	public static final String DATEFORMATFORTIME1 = "yyyyMMddHHmm";
	public static final String DATEFORMATFORTIME2 = "yyyy-MM-dd HH:mm";
	public static final String DATEFORMATFORTIME3 = "yyyyMMddHH:mm:ss";
	public static final String DATEFORMATFORTIME4 = "yyyyMMddHHmmssSS";
	public static final String DATEFORMATFORTIME5 = "yyyy-MM-dd HH:mm:ss";
	public static final String DATEFORMATFORTIME6 = "yyyyMMddHHmmss";
	
    public static enum Const {
        PATTERN_DATE_FORMAT("(\\{\\#([yMdHmsS]*)\\#\\})"),
        FILE_PATH_SEPARATOR("/"),
        FILE_EXT_SPLITTER("."),
        YYYYMMDDHHMMSSSSS("yyyyMMddHHmmssSSS"),
        YYYYMMDDHHMMSS("yyyyMMddHHmmss");
        
        private String definition;
        Const(String definition) {
            this.definition = definition;
        }
        
        public String getV() {
            return definition;
        }
    }

	
	public static Date addSeconds(Date date,int amount){
		return DateUtils.addSeconds(date, amount);
	}
	
	public static Date addDays(Date date,int amount){
		return DateUtils.addDays(date, amount);
	}
	
	public static Date addMonths(Date date,int amount){
		return DateUtils.addMonths(date, amount);
	}
	
	public static Date addYears(Date date,int amount){
		return DateUtils.addYears(date, amount);
	}
	
	public static Date truncate(Date date,int field){
		return DateUtils.truncate(date, field);
	}
	
	public static Date getLastDayOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
	    int lastDate = calendar.getActualMaximum(Calendar.DATE);

	    calendar.set(Calendar.DATE, lastDate);
	    
	    return calendar.getTime();
	}
	
	public static Integer getDiffOfDate(Date beginDate,Date endDate){
		long diff = endDate.getTime() - beginDate.getTime();
		
        return (int) (diff / (24 * 60 * 60 * 1000));
	}
	
	/**
     * 현재 일자 리턴
     * @return String yyyyMMddHHMMss로 포멧된 Date문자열
     */
    public static String getCurrentDatetime() {
        return getCurrentDatetime(Const.YYYYMMDDHHMMSS.getV(), 0);
    }

    /**
     * 현재 일자 리턴
     * @param amount 날짜 계산시 사용(0:현재, -1:전일, 1:내일)
     * @return yyyyMMddHHMMss로 포멧된 Date문자열
     */
    public static String getCurrentDatetime(int amount) {
        return getCurrentDatetime(Const.YYYYMMDDHHMMSS.getV(), amount);
    }

    /**
     * 현재 일자 리턴
     * @param dateFormatStr Date format 문자열
     * @return format된 Date 문자열
     */
    public static String getCurrentDatetime(String dateFormatStr) {
        return getCurrentDatetime(dateFormatStr, 0);
    }

    /**
     * 현재 일자 리턴
     * @param dateFormatStr Date format 문자열
     * @param amount 날짜 계산시 사용(0:현재, -1:전일, 1:내일)
     * @return format된 Date 문자열
     */
    public static String getCurrentDatetime(String dateFormatStr, int amount) {
        String dateStr = null;
        DateFormat formatter = new SimpleDateFormat(dateFormatStr, Locale.KOREA);
        Date date = new Date();
        if( amount != 0 ) {
            date = DateUtils.addDays(date, amount);
        }
        
        dateStr = formatter.format(date);
        
        return dateStr;
    }
    
    /**
     * 현재 일자 리턴
     * @param dateFormatStr Date format 문자열
     * @param amount 날짜 계산시 사용(0:현재, -1:전일, 1:내일)
     * @return format된 Date 문자열
     */
    public static String getCurrentDatetimeMinute(String dateFormatStr, int amount) {
        String dateStr = null;
        DateFormat formatter = new SimpleDateFormat(dateFormatStr, Locale.KOREA);
        Date date = new Date();
        if( amount != 0 ) {
            date = DateUtils.addMinutes(date, amount);
        }
        
        dateStr = formatter.format(date);
        
        return dateStr;
    }

	/**
	 * 날자 포멧 변경
	 *
	 * @param dateString
	 * @param from
	 * @param to
	 * @return
	 */
    public static String convDateFormat(String dateString , String from, String to) {

		String toDateString = null;
		try {
			SimpleDateFormat fromFmt = new SimpleDateFormat(from,Locale.KOREA);
			SimpleDateFormat toFmt = new SimpleDateFormat(to,Locale.KOREA);
			Date fromDate = fromFmt.parse(dateString);
			toDateString = toFmt.format(fromDate);
		} catch (ParseException e) {
			LOG.error("@@ {}", e.getMessage());
		}
		return toDateString;
	}
}
