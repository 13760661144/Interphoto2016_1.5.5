package cn.poco.video.helper;

/**
 * Created by Shine on 2017/7/10.
 */

public class VideoMediator {

    /**
     *
     * @param videoDuration 视频时长,单位为毫秒
     * @param millSeconds 时间为毫秒
     * @return 转换成0-1的进度输出
     */
    public static float transferDurationToProgress(long videoDuration, long millSeconds) {
        if (videoDuration < 0) {
            throw new IllegalArgumentException("videoDuration is less than zero");
        }

        if (millSeconds < 0) {
            throw new IllegalArgumentException("millSeconds is less than zero");
        }
        float progress = (float)millSeconds / (float)videoDuration;
        float finalProgress = progress > 1 ? 1 : progress;
        return finalProgress;
    }

    /**
     *
     * @param progress 进度(0 - 1)
     * @param videoLength 视频长度，单位为毫秒
     * @return 返回进度对应的视频时刻,单位为毫秒
     */
    public static long transferProgressToVideoFrameTime(float progress, long videoLength) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress is less than zero");
        }

        if (videoLength < 0) {
            throw new IllegalArgumentException("the videoLength is less than zero");
        }

        long time = (long)((progress * videoLength) + 0.5f);
        time = time > videoLength ? videoLength : time < 0 ? 0 : time;
        return time;
    }


    public static float[] transferVideoTimeToProgress(long startTime, long endTime, long videoLength) {
        float[] result = new float[2];
        result[0] = startTime / (float)videoLength;
        result[1] = endTime / (float)videoLength;

        if (result[0] > result[1]) {
            throw new IllegalArgumentException("start time is larger than end time");
        }

        if (result[0] < 0) {
            result[0] = 0;
        }

        if (result[1] > 1) {
            result[1] = 1;
        }
        return result;
    }


}
