package com.daxiangce123.android.util;

import android.annotation.SuppressLint;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@SuppressLint("SimpleDateFormat")
public class TimeUtil {
    public static final String TAG = "TimeUtil";

    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final long MILLIS_IN_DAY = 1000L * SECONDS_IN_DAY;

    // /**
    // *
    // * @time Apr 18, 2014
    // *
    // * @param time
    // * @param srcID
    // * GMT+0
    // * @param destID
    // * GMT+8
    // * @return
    // */
    // private static long getTargetMills(long time, String srcID, String
    // destID) {
    // if (Utils.isEmpty(srcID)) {
    // srcID = "GMT+0";
    // }
    // long srcRawOffset = TimeZone.getTimeZone(srcID).getRawOffset();
    // long tarRawOffset = 0;
    // // if (!Utils.isEmpty(destID)) {
    // // tarRawOffset = TimeZone.getTimeZone(destID).getRawOffset();
    // // } else {
    // // tarRawOffset = TimeZone.getDefault().getRawOffset();
    // // }
    // Long sourceRelativelyGMT = time - srcRawOffset;
    // Long targetTime = sourceRelativelyGMT + tarRawOffset;
    // return targetTime;
    // }

    public static boolean isSameDayOfMillis(final long ms1, final long ms2) {
        final long interval = ms1 - ms2;
        return interval < MILLIS_IN_DAY && interval > -1L * MILLIS_IN_DAY && toDay(ms1) == toDay(ms2);
    }

    @SuppressWarnings("deprecation")
    public static int getYear(long timeInMills) {
        if (timeInMills <= 0) {
            return -1;
        }
        try {
            Date date = new Date(timeInMills);
            return date.getYear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("deprecation")
    public static int getDay(long timeInMills) {
        if (timeInMills <= 0) {
            return -1;
        }
        try {
            Date date = new Date(timeInMills);
            return date.getDate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * UTC time in MILLS
     */
    public static long toDay(long millis) {
        return (millis + TimeZone.getDefault().getRawOffset()) / MILLIS_IN_DAY;//
    }

    /**
     * DEFAULT is UTC time
     */
    public static long toDay(String date, String format) {
        return toDay(date, format, "UTC");//
    }

    /**
     * @param timeZoneID if null, it'll use current timeZone
     */
    public static long toDay(String date, String format, String timeZoneID) {
        long millis = toLong(date, format, timeZoneID);
        return toDay(millis);//
    }

    /**
     * DEFAULT is UTC time
     */
    public static long toLong(String time, String format) {
        return toLong(time, format, "UTC");
    }

    /**
     * @param time
     * @param format
     * @param timeZoneID if null, it'll use current timeZone
     * @return
     */
    public static long toLong(String time, String format, String timeZoneID) {
        if (Utils.isEmpty(time)) {
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        long result = 0;
        try {
            if (Utils.isEmpty(timeZoneID)) {
                timeZoneID = getCurrentUTC();
            }
            sdf.setTimeZone(TimeZone.getTimeZone(timeZoneID));
            Date date = sdf.parse(time);
            result = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param time
     * @param format
     * @time Aug 25, 2014
     * @see #toLong(String, String)
     */
    @Deprecated
    public static long formatTime(String time, String format) {
        return toLong(time, format);
    }

    public static String formatTime(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static final String humanizeDateTime(String utcTime, String utcFormat) {
        if (Utils.isEmpty(utcTime) || Utils.isEmpty(utcFormat)) {
            return utcTime;
        }
        final long timeInMills = toLong(utcTime, utcFormat);
        if (timeInMills < 0) {
            return null;
        }
        final int timeInYear = getYear(timeInMills);
        final int nowInYear = Calendar.getInstance().get(Calendar.YEAR) - 1900;
        if (nowInYear > timeInYear) {
            return formatTime(timeInMills, Utils.getString(R.string.date_format_yyyy_year_m_month_d_day_with_time));
        }
        final long deltaDays = Math.abs((toDay(System.currentTimeMillis()) - toDay(timeInMills)));
        if (deltaDays >= 3) {
            return formatTime(timeInMills, Utils.getString(R.string.date_format_m_M_d_D_with_time));
        } else if (deltaDays == 2) {
            return formatTime(timeInMills, Utils.getString(R.string.bef_yesterday_with_time));
        } else if (deltaDays == 1) {
            return formatTime(timeInMills, Utils.getString(R.string.yesterday_with_time));
        }
        return humainzeTime(timeInMills);
    }

    public static final String humainzeTime(long timeInMills) {
        if (timeInMills < 0) {
            return null;
        }
        long deltaInMills = System.currentTimeMillis() - timeInMills;
        deltaInMills = deltaInMills < 0 ? 0 : deltaInMills;

        String ago = Utils.getString(R.string.ago);
        long num = 0;
        String unit = null;
        if (deltaInMills < Consts.MIN_IN_MILLS) {
//			num = deltaInMills / Consts.SEC_IN_MILLS;
//			unit = Utils.getString(R.string.second);
            return Utils.getString(R.string.just_now);
        } else if (deltaInMills < Consts.HOU_IN_MILLS) {
            num = deltaInMills / Consts.MIN_IN_MILLS;
            unit = Utils.getString(R.string.minute);
        } else if (deltaInMills < Consts.HOU_IN_MILLS * 5) {
            num = deltaInMills / Consts.HOU_IN_MILLS;
            unit = Utils.getString(R.string.hour);
        } else {
            return formatTime(timeInMills, Utils.getString(R.string.time_format_H_hour_m_minus));
        }
        return num + unit + ago;
    }

    public static final String humanizeDate(String utcTime, String utcFormat) {
        if (Utils.isEmpty(utcTime) || Utils.isEmpty(utcFormat)) {
            return utcTime;
        }
        long timeInMills = toLong(utcTime, utcFormat);
        return humanizeDate(timeInMills);
    }

    public static final String humanizeDate(long timeInMills) {
        if (timeInMills < 0) {
            return null;
        }
//        int timeInYear = getYear(timeInMills);
//        int nowInYear = Calendar.getInstance().get(Calendar.YEAR) - 1900;
//        if (nowInYear > timeInYear) {
//            return formatTime(timeInMills, Utils.getString(R.string.date_format_yyyy_year_m_month_d_day));
//        }
        long deltaDays = Math.abs((toDay(System.currentTimeMillis()) - toDay(timeInMills)));
        if (deltaDays >= 3) {
            return formatTime(timeInMills, Utils.getString(R.string.date_format_m_M_d_D));
        } else if (deltaDays == 2) {
            return Utils.getString(R.string.bef_yesterday);
        } else if (deltaDays == 1) {
            return Utils.getString(R.string.yesterday);
        }
        return Utils.getString(R.string.today);
    }

    public static final boolean dayTime(long timeInMills) {
        String hourStr = formatTime(timeInMills, "H");
        int hour = 0;
        try {
            hour = Integer.parseInt(hourStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (hour >= 7 && hour <= 18) {
            return true;
        }
        return false;
    }

    /**
     * @return for example GMT+8
     * @time Apr 18, 2014
     */
    public static final String getCurrentUTC() {
        int id = TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000);
        if (id > 0) {
            return "UTC+" + id;
        } else {
            return "UTC" + id;
        }
    }

    public static final String getCurrentUTCTime() {
        SimpleDateFormat f = new SimpleDateFormat(Consts.SERVER_UTC_FORMAT);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return (f.format(new Date()));
    }


    public static final String getLocalTime(String utcTime, String utcTimePatten, String localTimePatten){
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }

}
