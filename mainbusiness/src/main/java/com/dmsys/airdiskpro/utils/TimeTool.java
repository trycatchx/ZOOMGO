package com.dmsys.airdiskpro.utils;

import android.content.Context;

import com.dmsys.mainbusiness.R;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeTool {
/**
 * 高效判断两个时间（毫秒，long）是不是同一天
 */
	   public static final int SECONDS_IN_DAY = 60 * 60 * 24;
	    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;
	 
	    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
	        final long interval = ms1 - ms2;
	        return interval < MILLIS_IN_DAY
	                && interval > -1L * MILLIS_IN_DAY
	                && toDay(ms1) == toDay(ms2);
	    }
	 
	    private static long toDay(long millis) {
	        return (millis + TimeZone.getDefault().getOffset(millis)) / MILLIS_IN_DAY;
	    }
	    
	    
		public static String formatPicDate(Context mContext,long mLastModify) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mLastModify);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			int mWay = calendar.get(Calendar.DAY_OF_WEEK);  
			String week = "";
			switch(mWay) {
			case 1:week = mContext.getString(R.string.DM_Sunday);break;
			case 2:week = mContext.getString(R.string.DM_Monday);break;
			case 3:week = mContext.getString(R.string.DM_Tuesday);break;
			case 4:week = mContext.getString(R.string.DM_Wednesday);break;
			case 5:week = mContext.getString(R.string.DM_Thursday);break;
			case 6:week = mContext.getString(R.string.DM_Friday);break;
			case 7:week = mContext.getString(R.string.DM_Saturday);break;
			}
			String date = year + "-" + (month + 1) + "-" + day + " "+week;
			return date;
		}
		
		/**
		 * 
		 * @param seconds
		 * @return
		 *  0    -> 0 sec
		 *	5    -> 5 sec
	        60   -> 1 min
			65   -> 1 min 05 sec
			3600 -> 1 h
			3601 -> 1 h 01 sec
			3660 -> 1 h 01
			3661 -> 1 h 01 min 01 sec
			108000 -> 30 h
		 */
		public static String convertSeconds(Context context,int seconds) {
			  int h = seconds/ 3600;
			    int m = (seconds % 3600) / 60;
			    int s = seconds % 60;
			    String sh = (h > 0 ? String.valueOf(h) + " " + context.getString(R.string.DM_File_Operate_Remain_Time_Hour) : "");
			    String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + " " + context.getString(R.string.DM_File_Operate_Remain_Time_Min)) : "");
			    String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + " " + context.getString(R.string.DM_File_Operate_Remain_Time_Sec));
			    return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "") + ss;
		}
	  
	

}
