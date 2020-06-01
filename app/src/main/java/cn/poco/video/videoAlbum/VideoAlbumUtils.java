package cn.poco.video.videoAlbum;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import cn.poco.beautify.WaitDialog1;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.VideoConfig;
import cn.poco.video.decode.DecodeUtils;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.AndroidUtil;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by lgd on 2018/1/10.
 */

public class VideoAlbumUtils
{
    static public boolean isVideoValid(Context context, VideoEntry params, int curNum, int usableTime)
    {
        boolean isFileExist = FileUtils.isFileExist(params.mMediaPath);
        boolean isSizeValid = VideoUtils.isVideoRatioValid(params.mWidth, params.mHeight) && VideoUtils.isSupport(params.mWidth, params.mHeight);
        boolean isFormatValid = VideoUtils.isVideoFormatValid(params.mMediaPath) && DecodeUtils.checkVideo(params.mMediaPath);
        boolean isWithinLimit = curNum < VideoConfig.MAX_NUM;
        boolean isWithinDuration = params.mDuration < usableTime;
        boolean isSurportVideo = DecodeUtils.checkAudio(params.mMediaPath);

        boolean isValid = isFileExist && isSizeValid && isFormatValid && isWithinLimit && isWithinDuration && isSurportVideo;


        if (!isFileExist)
        {
            Toast.makeText(context, context.getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show();
        }
        // 目前只支持MP4格式的视频
        else if (!isFormatValid)
        {
            Toast.makeText(context, context.getString(R.string.video_format_unsupported), Toast.LENGTH_SHORT).show();
        } else if (!isSizeValid)
        {
            Toast.makeText(context, context.getString(R.string.video_ratio_unsupported), Toast.LENGTH_SHORT).show();
        } else if (!isWithinLimit)
        {
            final InterphotoDlg numDialog = new InterphotoDlg((Activity) context, R.style.waitDialog);
            numDialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
            numDialog.SetTitle(context.getString(R.string.video_beyond_limit));
            numDialog.SetBtnType(InterphotoDlg.POSITIVE);
            numDialog.setCancelable(true);
            numDialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback()
            {
                @Override
                public void onOK()
                {
                    numDialog.dismiss();
                }

                @Override
                public void onCancel()
                {
                    numDialog.dismiss();
                }
            });
            numDialog.show();
        } else if (!isWithinDuration)
        {
            Toast.makeText(context, R.string.video_too_large, Toast.LENGTH_SHORT).show();
        } else if (!isSurportVideo) {
            Toast.makeText(context, R.string.video_audioTrack_absent, Toast.LENGTH_SHORT).show();
        }
        return isValid;
    }

    public static String getData(long duration)
    {
        long minute = (duration / 60000) % 60;
        long second = duration / 1000 % 60;
        long tenthSecond = (duration / 100) % 10;
        String s = String.format(Locale.getDefault(), "%02d:%02d.%1d", minute, second, tenthSecond);
        return s;
    }

    public static int getTotleTime(List<VideoEntry> entries)
    {
        int totalDuration = 0;
        for (int i = 0; i < entries.size(); i++)
        {
            totalDuration += entries.get(i).mDuration;
        }
        return totalDuration;
    }

    public static List<VideoInfo> transformVideoInfo(List<VideoEntry> videoEntries)
    {
        List<VideoInfo> videoInfos = new ArrayList<>();
        for (int i = 0; i < videoEntries.size(); i++)
        {
            VideoInfo info = videoEntries.get(i).transferToVideoInfo();
            videoInfos.add(info);
        }
        return videoInfos;
    }

    /**
     * 根据模式和时间判断是否裁剪视频，需要裁剪有线程调用
     *
     * @param context
     * @param videoEntries
     * @param is10sMode
     * @param callBack
     */
    public static void transformVideoInfo(Context context, List<VideoEntry> videoEntries, boolean is10sMode, final VideoTransformCallBack callBack)
    {
        //检查合法性
        List<VideoEntry> tempEntries = new ArrayList<>();
        for (int i = 0; i < videoEntries.size(); i++)
        {
            if(DecodeUtils.checkVideo(videoEntries.get(i).mMediaPath)){
                tempEntries.add(videoEntries.get(i));
            }
        }
        List<VideoInfo> videoInfos;
        if (is10sMode && getTotleTime(tempEntries) > VideoConfig.DURATION_10S_MODE)
        {
            averageVideoPath(context, tempEntries, callBack);
        } else
        {
            videoInfos = transformVideoInfo(tempEntries);
            if (callBack != null)
            {
                callBack.onFinish(videoInfos);
            }
        }
    }

    public static void averageVideoPath(final Context context, final List<VideoEntry> videoEntries, final VideoTransformCallBack callBack)
    {
        //10s模式均分视频
        long totalSkinDuration = 0;
        long averageDuration = VideoConfig.DURATION_10S_MODE / videoEntries.size();
        //筛选时间不足平均时间的视频，跳过，剩下的再均分
        final HashSet<Integer> clipIndexs = new HashSet<>();
        for (int i = 0; i < videoEntries.size(); i++)
        {
            VideoEntry info = videoEntries.get(i);
            if (info.mDuration <= averageDuration)
            {
                totalSkinDuration += info.mDuration;
            }else{
                clipIndexs.add(i);
            }
        }
        averageDuration = (VideoConfig.DURATION_10S_MODE - totalSkinDuration) /clipIndexs.size();
        final long finalAverageDuration = averageDuration;

        final WaitDialog1 dialog1 = new WaitDialog1(context, R.style.waitDialog);
        dialog1.setMessage(context.getResources().getString(R.string.processing));
        dialog1.show();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final ArrayList<VideoInfo> videoInfos = new ArrayList<>();
                for (int i = 0; i < videoEntries.size(); i++)
                {
                    VideoEntry videoEntry = videoEntries.get(i);
                    VideoInfo info = videoEntry.transferToVideoInfo();
                    if (clipIndexs.contains(i))
                    {
                        String outPath = VideoUtils.clipVideo(videoEntry.mMediaPath,finalAverageDuration);
                        if (!outPath.equals(videoEntry.mMediaPath))
                        {
                            info.mClipPath = outPath;
                            info.mSelectStartTime = 0;
                            info.mSelectEndTime = finalAverageDuration;
                        }
                    }
                    videoInfos.add(info);
                }
                AndroidUtil.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        dialog1.dismiss();
                        if (callBack != null)
                        {
                            callBack.onFinish(videoInfos);
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 进入沉浸模式
     */
    public static void enterImmersiveMode(Context context) {
        if(ShareData.m_ratio > 0.58)
        {       //16 : 9 0.5625      华为0.6081081
            View mDecorView = null;
            if (mDecorView == null)
            {
                mDecorView = ((Activity) context).getWindow().getDecorView();
            }
            if (mDecorView != null)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    mDecorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        }
    }

    /**
     * 退出沉浸模式
     */
    public static void exitImmersiveMode(Context context) {
        if(ShareData.m_ratio > 0.58)
        {
            View mDecorView = null;
            if (mDecorView == null)
            {
                mDecorView = ((Activity) context).getWindow().getDecorView();
            }
            if (mDecorView != null)
            {
                mDecorView.setSystemUiVisibility(0);
            }
        }
    }

    public interface VideoTransformCallBack
    {
        void onFinish(List<VideoInfo> videoInfos);
    }
}
