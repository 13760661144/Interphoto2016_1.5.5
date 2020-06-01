package cn.poco.video.helper;

import java.util.Locale;

/**
 * Created by Shine on 2017/5/17.
 */

public class TimeFormatter {
    private static final int MINUTE_BASE_UNIT = 60;
    private static final long MINUTE = 60 * 1000;
    private static final long SECOND = 1000;

    public static String toVideoDurationFormat(long millis) {
        float s = (millis / 1000f);
        int minute = (int)(s / MINUTE_BASE_UNIT);
        int second = (int)((s / (MINUTE_BASE_UNIT * 1.0f) - minute) * MINUTE_BASE_UNIT + 0.5f);
        if(second == 60){
            second = 0;
            minute +=1;
        }

        String minuteInString = minute >= 10 ? String.valueOf(minute) : minute >= 0 ? String.valueOf(0).concat(String.valueOf(minute)) : String.valueOf(minute);
        String secondInString = second >= 10 ? String.valueOf(second) : second >= 0 ? String.valueOf(0).concat(String.valueOf(second)) : String.valueOf(second);
        final String dividerSymbol = ":";
        String result = minuteInString.concat(dividerSymbol).concat(secondInString);
        return result;
    }

    /**
     * 生成表示视频选中区域的时间格式
     * @param changeTime
     * @param diff
     * @return
     */
    public static String makeTimeRangeFormatText(long changeTime, long diff) {
        String changeTimeInFormat = toVideoDurationFormat(changeTime);

        float changeTimeTotalSecond = (changeTime / 1000f);
        int changeTimeMinute = (int)(changeTimeTotalSecond / MINUTE_BASE_UNIT);
        int changeTimeSecond = (int)((changeTimeTotalSecond / (MINUTE_BASE_UNIT * 1.0f) - changeTimeMinute) * MINUTE_BASE_UNIT + 0.5f);

        float unChangeTimeSecond = changeTimeSecond + diff;
        int unChangeTimeMinute = (int)(unChangeTimeSecond / MINUTE_BASE_UNIT);
        int unChangeTimeSecondInt = (int)unChangeTimeSecond;

        String unChangeTimeMinuteInFormat = unChangeTimeMinute >= 10 ? String.valueOf(unChangeTimeMinute) : unChangeTimeMinute >= 0 ? String.valueOf(0).concat(String.valueOf(unChangeTimeMinute)) : String.valueOf(unChangeTimeMinute);
        String unChangeTimeSecondInFormat = unChangeTimeSecondInt >= 10 ? String.valueOf(unChangeTimeSecondInt) : unChangeTimeSecondInt >= 0 ? String.valueOf(0).concat(String.valueOf(unChangeTimeSecondInt)) : String.valueOf(unChangeTimeSecondInt);
        final String dividerSymbol = ":";
        String finalUnChngeTimeInFormat = unChangeTimeMinuteInFormat.concat(dividerSymbol).concat(unChangeTimeSecondInFormat);

        String result = changeTimeInFormat.concat(" - ").concat(finalUnChngeTimeInFormat);
        return result;
    }


    public static String makeVideoTimeDurationText(long duration) {
        String second = "S";
        float videoDuration = duration / 1000f;
        String videoDurationText = String.format("%.1f", videoDuration);
        String result = videoDurationText.concat(second);
        return result;
    }

    public static String makeVideoProgressLayoutDurationText(long duration) {
        String second = "''";
        float videoDuration = duration / 1000f;
        String videoDurationText = String.format("%.1f", videoDuration);
        String result = videoDurationText.concat(second);
        return result;
    }




    public static String formatTimeSpecifyToMills(long time, long totalTime, boolean useDash) {

        long timeInSecond = time / 1000;
        long minute = (timeInSecond / 60) % 60;
        long second = timeInSecond % 60;
        long millsecond = (time % 1000) / 100;

        long totalTimeInSecond = totalTime / 1000;
        long totalMinute = (totalTimeInSecond / 60) % 60;
        long totalSecond = totalTimeInSecond % 60;
        long totalMillSecond = (totalTime % 1000) / 100;

        if (useDash) {
            return String.format(Locale.getDefault(), "%02d:%02d.%1d-%02d:%02d.%1d", minute, second, millsecond, totalMinute, totalSecond, totalMillSecond);
        } else {
            return String.format(Locale.getDefault(), "%02d:%02d.%1d/%02d:%02d.%1d", minute, second, millsecond, totalMinute, totalSecond, totalMillSecond);
        }
    }


}
