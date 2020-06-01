package cn.poco.video.videoSpeed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.beautify.WaitDialog1;
import cn.poco.framework.BaseSite;
import cn.poco.interphoto2.R;
import cn.poco.login.util.LoginOtherUtil;
import cn.poco.setting.SettingSliderBtn;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.VideoConfig;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.process.ClipVideoTask;
import cn.poco.video.process.OnProcessListener;
import cn.poco.video.process.ReverseTask;
import cn.poco.video.process.SpeedTask;
import cn.poco.video.process.ThreadPool;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoAlbum.VideoAlbumUtils;
import cn.poco.video.videoFeature.clip.VideoFramesPage;
import cn.poco.video.videoFeature.data.VideoSpeedInfo;
import cn.poco.video.view.ActionBar;

/**
 * Created by lgd on 2018/1/18.
 * <p>
 * 速率和倒放是对原始整个视频做处理，然后再裁剪出来
 *
 */

public class VideoSpeedPage extends VideoFramesPage
{
    private static final String TAG = "VideoSpeedPage";
    private SettingSliderBtn mUpendSliderBtn;
    private TextView durationView;
    private SpeedSeekBar mSeekBar;
    private SpeedTimeView timeTextView;
    private VideoSpeedSite mSite;
    private VideoModeWrapper mWrapper;
    private GLVideoView mVideoView;
    private int mOriginalDuration;
    private WaitDialog1 mProgressDialog;
    private VideoSpeedInfo mSpeedVideoInfo;
    private VideoInfo mVideoInfo;
    private final String mOriginalPath;
    private final float mOriginalVolume;
    private boolean mCurIsReverse = false; //是否倒放
    private float mCurSpeed = 1.0f;
    private boolean mCurIsSilence;

    private LinearLayout mVoiceSwitch;
    private TextView mVoiceTip;
    private ImageView mSwitchLogo;

    private VideoSpeedInfo mClipVideoInfo = new VideoSpeedInfo();
    private boolean mIsUseClip = true;                              //做速率和倒放是对整个视频做，如果视频裁被剪过，则要再做裁剪

    private int mMarginW = ShareData.PxToDpi_xhdpi(41);
    public VideoSpeedPage(Context context, BaseSite baseSite, VideoModeWrapper wrapper)
    {
        super(context, baseSite);

        MyBeautyStat.onPageStartByRes(R.string.视频速率页);

        mWrapper = wrapper;
        mVideoView = mWrapper.mVideoView;
        mVideoInfo = mWrapper.mCurrentBeautifiedVideo;
        mOriginalVolume = mVideoView.getVolume();
        mOriginalPath = mVideoInfo.mClipPath;
        mCurIsReverse = mVideoInfo.mIsReverse;
        mCurIsSilence = mVideoInfo.mIsSilenceWhileSpeed;
        mCurSpeed = mVideoInfo.mCurSpeed;
//        if(mOriginalDuration == mVideoInfo.mDuration){
        mIsUseClip = mVideoInfo.isHasClipped();
        mSite = (VideoSpeedSite) baseSite;
        init();
        initDate();
    }
    private void initDate()
    {
        mWrapper.mActionBar.setOnActionbarMenuItemClick(new ActionBar.onActionbarMenuItemClick()
        {
            @Override
            public void onItemClick(int id)
            {
                super.onItemClick(id);
                if (id == ActionBar.LEFT_MENU_ITEM_CLICK)
                {
                    onBack();
                } else if (id == ActionBar.RIGHT_MENU_ITEM_CLICK)
                {
                    long curDuration = getReadClipDuration(mCurSpeed);
                    if (curDuration > VideoConfig.DURATION_LIMIT - 15 && !isExceedLimit(curDuration))
                    {
                        MyBeautyStat.onClickByRes(R.string.视频速率页_保存视频速率);
                        if(mCurSpeed == 1f){
                            MyBeautyStat.onClickByRes(R.string.视频速率页_正常);
                            // 正常的速率1和倒放都不静音
                            MyBeautyStat.onClickByRes(R.string.视频速率页_关闭静音);
                            if(mCurIsReverse){
                                MyBeautyStat.onClickByRes(R.string.视频速率页_开启倒放);
                            }else{
                                MyBeautyStat.onClickByRes(R.string.视频速率页_关闭倒放);
                            }
                        }else{
                            if (mCurSpeed == 1.0f / 4)
                            {
                                MyBeautyStat.onClickByRes(R.string.视频速率页_慢速4);
                            } else if (mCurSpeed == 1.0f / 2)
                            {
                                MyBeautyStat.onClickByRes(R.string.视频速率页_慢速2);
                            } else if (mCurSpeed == 2f)
                            {
                                MyBeautyStat.onClickByRes(R.string.视频速率页_快速2);

                            } else if (mCurSpeed == 4f)
                            {
                                MyBeautyStat.onClickByRes(R.string.视频速率页_快速4);
                            }
                            if(mCurIsReverse){
                                MyBeautyStat.onClickByRes(R.string.视频速率页_开启倒放);
                                //倒放默认开启静音
                                MyBeautyStat.onClickByRes(R.string.视频速率页_开启静音);
                            }else{
                                MyBeautyStat.onClickByRes(R.string.视频速率页_关闭倒放);
                                if(mCurIsSilence){
                                    MyBeautyStat.onClickByRes(R.string.视频速率页_开启静音);
                                }else{
                                    MyBeautyStat.onClickByRes(R.string.视频速率页_关闭静音);
                                }
                            }
                        }

                        mWrapper.isModify = true;
                        boolean isRefresh = !mVideoInfo.mClipPath.equals(mClipVideoInfo.getVideoPath(mCurIsReverse,mCurSpeed));
                        mVideoInfo.mPath = mSpeedVideoInfo.getVideoPath(mCurIsReverse, mCurSpeed);
                        mVideoInfo.mClipPath = mClipVideoInfo.getVideoPath(mCurIsReverse,mCurSpeed);
                        mVideoInfo.mIsSilenceWhileSpeed = mCurIsSilence;
                        mVideoInfo.mSelectStartTime = getRealStartTime(mCurSpeed);
                        mVideoInfo.mSelectEndTime =getRealEndTime(mCurSpeed);
                        mVideoInfo.mDuration = getReadTotalDuration(mCurSpeed);
                        if(mCurIsReverse != mVideoInfo.mIsReverse){
                            //比之前相反的播放，  这两个值也要翻转
                            long temp =  mVideoInfo.mSelectStartTime;
                            mVideoInfo.mSelectStartTime = mVideoInfo.mDuration - mVideoInfo.mSelectEndTime;
                            mVideoInfo.mSelectEndTime = mVideoInfo.mDuration - temp;
                        }
                        mVideoInfo.mIsReverse = mCurIsReverse;
                        mVideoInfo.mCurSpeed = mCurSpeed;
                        //保存使用的速率文件，防止被删除
                        mSpeedVideoInfo.putUsedPath(mVideoInfo.mPath);
//                        mVideoView.changeVideoPath(path);
                        mWrapper.refreshCurVideoDuration();
                        mSite.onBack(getContext(),isRefresh);
                    } else
                    {
                        final InterphotoDlg numDialog = new InterphotoDlg((Activity) getContext(), R.style.waitDialog);
                        numDialog.getWindow().setWindowAnimations(R.style.PopupAnimation);
                        if(curDuration < VideoConfig.DURATION_LIMIT)
                        {
                            String text = getResources().getString(R.string.video_too_short);
                            if(LoginOtherUtil.isChineseLanguage(getContext()))
                            {
                                if (mCurSpeed == 2f)
                                {
                                    text = "原视频时长不足2秒，无法选择该速率";
                                } else if (mCurSpeed == 4f)
                                {
                                    text = "原视频时长不足4秒，无法选择该速率";
                                }
                            }
                            numDialog.SetTitle(text);
                        }else{
                            numDialog.SetTitle(getContext().getString(R.string.video_too_large));
                        }
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
                    }

                }
            }
        });

        String key = mVideoInfo.mParentPath;
        if (mWrapper.mSpeedVideoInfos.containsKey(key))
        {
            mSpeedVideoInfo = mWrapper.mSpeedVideoInfos.get(key);
        } else
        {
            mSpeedVideoInfo = new VideoSpeedInfo();
            mSpeedVideoInfo.putVideoPath(false, 1, key);
            mWrapper.mSpeedVideoInfos.put(key, mSpeedVideoInfo);
        }
        //设置相册路径被使用的速率文件，防止被缓存删除
        ArrayList<String> videoPaths = new ArrayList<>();
        for (int i = 0; i < mWrapper.mVideoInfos.size(); i++)
        {
            if(mWrapper.mVideoInfos.get(i).mParentPath.equals(key)){
                videoPaths.add(mWrapper.mVideoInfos.get(i).mPath);
            }
        }
        mSpeedVideoInfo.resetUsedPath(videoPaths);

        for (int i = 0; i < speedParams.length; i++)
        {
            if (mCurSpeed == speedParams[i])
            {
                mSeekBar.setProgress(i);
                m_seekBarListener.onProgressChanged(mSeekBar,i,true);
            }
        }
        mClipVideoInfo.putVideoPath(mCurIsReverse,mCurSpeed,mOriginalPath);
        mUpendSliderBtn.setSwitchStatus(mCurIsReverse);
        setSilenceState(mCurIsSilence,mCurSpeed);
        setVoiceBtnState(mCurSpeed,mCurIsReverse);
    }


    private void init()
    {
        LayoutParams fl;
        LinearLayout.LayoutParams ll;
//        setPadding(ShareData.PxToDpi_xhdpi(41), 0, ShareData.PxToDpi_xhdpi(41), 0);


        LinearLayout lLayout1 = new LinearLayout(getContext());
        lLayout1.setGravity(Gravity.CENTER_VERTICAL);
        lLayout1.setOrientation(LinearLayout.HORIZONTAL);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.setMargins(mMarginW,0,mMarginW,0);
        fl.topMargin = ShareData.PxToDpi_xhdpi(105);
        addView(lLayout1, fl);
        {
            TextView tip = new TextView(getContext());
            tip.setTextColor(0xff666666);
            tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            tip.setText(R.string.video_fragment_duration);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lLayout1.addView(tip, ll);

            durationView = new TextView(getContext());
            durationView.setTextColor(0xffffffff);
            durationView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            durationView.setText("00:03.0");
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lLayout1.addView(durationView, ll);
        }

        LinearLayout lLayout2 = new LinearLayout(getContext());
        lLayout2.setGravity(Gravity.CENTER_VERTICAL);
        lLayout2.setOrientation(LinearLayout.HORIZONTAL);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.setMargins(mMarginW,0,mMarginW,0);
        fl.topMargin = ShareData.PxToDpi_xhdpi(105);
        fl.gravity = Gravity.RIGHT;
        addView(lLayout2, fl);
        {
            TextView tip = new TextView(getContext());
            tip.setTextColor(0xffffffff);
            tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f);
            tip.setText(R.string.video_upend);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lLayout2.addView(tip, ll);

            mUpendSliderBtn = new SettingSliderBtn(getContext());
            mUpendSliderBtn.setOnSwitchListener(new SettingSliderBtn.OnSwitchListener()
            {
                @Override
                public void onSwitch(View v, boolean on)
                {
                    setVoiceBtnState(mCurSpeed,on);
                    doReverseVideoTask(on, mCurSpeed);
                }
            });
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = ShareData.PxToDpi_xhdpi(22);
            lLayout2.addView(mUpendSliderBtn, ll);
        }

        TextView slowTip = new TextView(getContext());
        slowTip.setTextColor(0xff666666);
        slowTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
        slowTip.setText(R.string.video_slow);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.setMargins(mMarginW,0,mMarginW,0);
        fl.topMargin = ShareData.PxToDpi_xhdpi(222);
        addView(slowTip, fl);

        TextView quickTip = new TextView(getContext());
        quickTip.setTextColor(0xff666666);
        quickTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
        quickTip.setText(R.string.video_fast);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT);
        fl.setMargins(mMarginW,0,mMarginW,0);
        fl.topMargin = ShareData.PxToDpi_xhdpi(222);
        addView(quickTip, fl);

        mSeekBar = new SpeedSeekBar(getContext());
        mSeekBar.setMax(5);
        mSeekBar.setOnSeekBarChangeListener(m_seekBarListener);
//        mSeekBar.setMinimumHeight(ShareData.PxToDpi_xhdpi(48+40));
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(48+40));//加上40容易滑动
        fl.setMargins(mMarginW,0,mMarginW,0);
        fl.topMargin = ShareData.PxToDpi_xhdpi(270 - 20);  //减去40/2的居中
        mSeekBar.setLayoutParams(fl);
        addView(mSeekBar);

        timeTextView = new SpeedTimeView(getContext());
        timeTextView.setPadding(mMarginW +mSeekBar.getSelectedBigR(),0, mMarginW +mSeekBar.getSelectedBigR(),0);
//        timeTextView.setMinimumHeight(ShareData.PxToDpi_xhdpi(39));
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ShareData.PxToDpi_xhdpi(80));
        fl.topMargin = ShareData.PxToDpi_xhdpi(325);
        addView(timeTextView, fl);


        mVoiceSwitch = new LinearLayout(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.TOP;
        fl.topMargin = mVideoView.getSurfaceTop();
        fl.leftMargin = mVideoView.getSurfaceLeft();
        int padding = ShareData.PxToDpi_xhdpi(15);
        mVoiceSwitch.setPadding(padding, padding, padding, padding);
        mVoiceSwitch.setOrientation(LinearLayout.HORIZONTAL);
        mVoiceSwitch.setGravity(Gravity.CENTER);
        mVoiceSwitch.setLayoutParams(fl);
        mVoiceSwitch.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCurIsSilence = !mCurIsSilence;
//                if(mCurIsSilence){
//                    MyBeautyStat.onClickByRes(R.string.视频速率页_开启静音);
//                }else{
//                    MyBeautyStat.onClickByRes(R.string.视频速率页_关闭静音);
//                }
                setSilenceState(mCurIsSilence,mCurSpeed);
            }
        });
        mVoiceSwitch.setVisibility(View.GONE);
        {
            mSwitchLogo = new ImageView(getContext());
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_on);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mSwitchLogo.setLayoutParams(ll);
            mVoiceSwitch.addView(mSwitchLogo);

            mVoiceTip = new TextView(getContext());
            mVoiceTip.setText(R.string.bk_music_on);
            mVoiceTip.setTextColor(Color.WHITE);
            mVoiceTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.leftMargin = ShareData.PxToDpi_xhdpi(5);
            mVoiceTip.setLayoutParams(ll);
            mVoiceSwitch.addView(mVoiceTip);
        }
        mVideoView.addView(mVoiceSwitch, fl);

        mProgressDialog = new WaitDialog1(getContext(), R.style.waitDialog);
        mProgressDialog.setMessage(getResources().getString(R.string.processing));
    }

    public void setVoiceBtnState(float curSpeed,boolean isReverse)
    {
        if(curSpeed == 1 || isReverse || mWrapper.isVideoSilence() ){
            mVoiceSwitch.setVisibility(View.GONE);
        }else{
            mVoiceSwitch.setVisibility(View.VISIBLE);
        }
    }

    public void setSilenceState(boolean isSilence,float curSpeed)
    {
        if (isSilence)
        {
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_off);
            mVoiceTip.setText(R.string.video_speed_silence);
        } else
        {
            mSwitchLogo.setImageResource(R.drawable.video_music_voice_on);
            mVoiceTip.setText(R.string.video_speed_silence);
        }
        if(!isSilence || curSpeed == 1){
            mVideoView.openVolume();
//            mVideoView.setVolume(mOriginalVolume);
        }else{
            mVideoView.closeVolume();
//            mVideoView.setVolume(0);
        }
    }

    private void onSpeedTaskFinish(String path,float speed)
    {
        mCurSpeed = speed;
        mVideoView.changeVideoPath(path);
        setSilenceState(mCurIsSilence,mCurSpeed);
    }

    private void doSpeedTask(final boolean isReverse, final float speed)
    {
        String path = mClipVideoInfo.getVideoPath(isReverse,speed);
        if(isVideoExists(path)){
            onSpeedTaskFinish(path,speed);
        }else{
            VideoSpeedTask task = new VideoSpeedTask(isReverse, speed, new OnProcessListener()
            {
                @Override
                public void onStart()
                {
                    mWrapper.pauseAll();
                    mProgressDialog.show();
                }

                @Override
                public void onFinish()
                {
                    mProgressDialog.dismiss();
                    String finalPath = mClipVideoInfo.getVideoPath(isReverse,speed);
                    onSpeedTaskFinish(finalPath,speed);
                }

                @Override
                public void onError(String message)
                {
                    mProgressDialog.dismiss();
                    int index = getSpeedIndex(mCurSpeed);
                    mSeekBar.setProgress(index);
                    Toast.makeText(getContext(), R.string.video_modify_failed, Toast.LENGTH_SHORT).show();
                }
            });
            ThreadPool.getInstance().execute(task);
        }
    }

    private void onReverseVideoFinish(String path,boolean isReverse)
    {
//        if(isReverse){
//            MyBeautyStat.onClickByRes(R.string.视频速率页_开启倒放);
//        }else{
//            MyBeautyStat.onClickByRes(R.string.视频速率页_关闭倒放);
//        }
        mCurIsReverse = isReverse;
        mVideoView.changeVideoPath(path);
    }

    public void doReverseVideoTask(final boolean isReverse, final float speed)
    {
        String path = mClipVideoInfo.getVideoPath(isReverse,speed);
        if(isVideoExists(path)){
            onReverseVideoFinish(path,isReverse);
        }else{
            VideoSpeedTask task = new VideoSpeedTask(isReverse, speed, new OnProcessListener()
            {
                @Override
                public void onStart()
                {
                    mWrapper.pauseAll();
                    mProgressDialog.show();
                }

                @Override
                public void onFinish()
                {
                    mProgressDialog.dismiss();
                    String finalPath = mClipVideoInfo.getVideoPath(isReverse,speed);
                    onReverseVideoFinish(finalPath,isReverse);
                }

                @Override
                public void onError(String message)
                {
                    mProgressDialog.dismiss();
                    mUpendSliderBtn.setSwitchStatus(!isReverse);
                    setVoiceBtnState(mCurSpeed,!isReverse);
                    Toast.makeText(getContext(), R.string.video_modify_failed, Toast.LENGTH_SHORT).show();
                }
            });
            ThreadPool.getInstance().execute(task);
        }
    }

    protected boolean mUiEnable = true;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        if (mUiEnable)
        {
            return super.onInterceptTouchEvent(ev);
        } else
        {
            return true;
        }
    }

    private int getSpeedIndex(float speed)
    {
        int index = -1;
        for (int i = 0; i < speedParams.length; i++)
        {
            if(speed == speedParams[i]){
                index = i;
                break;
            }
        }
        return index;
    }

    private boolean isExceedLimit(final long curDuration)
    {
        if(mWrapper.getVideosDuration()-mVideoInfo.GetClipTime()+curDuration > VideoConfig.DURATION_FREE_MODE){
            return true;
        }else{
            return false;
        }
    }

    float[] speedParams = new float[]{1.0f / 4, 1.0f / 2, 1, 2, 4};
    private SpeedSeekBar.OnSeekBarChangeListener m_seekBarListener = new SpeedSeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onCenterChanged(SpeedSeekBar seekBar, int centerX)
        {
            if(timeTextView != null){
                timeTextView.setCenterX(centerX + mMarginW);
            }
        }

        @Override
        public void onProgressChanged(SpeedSeekBar seekBar, int progress, boolean fromUser)
        {
            if (progress >= 0 && progress < speedParams.length)
            {
                float speed = speedParams[progress];
                long finalDuration = getReadClipDuration(speed);
                durationView.setText(VideoAlbumUtils.getData(finalDuration));
                if (finalDuration >= VideoConfig.DURATION_LIMIT && !isExceedLimit(finalDuration) || speed == 1)
                {
                    durationView.setTextColor(Color.WHITE);
                } else
                {
                    durationView.setTextColor(Color.RED);
                }
                setVoiceBtnState(speed,mCurIsReverse);
            }
        }

        @Override
        public void onStopTrackingTouch(SpeedSeekBar seekBar)
        {
            int progress = seekBar.getProgress();
            if (progress >= 0 && progress < speedParams.length)
            {
                float speed = speedParams[progress];
                setVoiceBtnState(speed,mCurIsReverse);
                if(mCurSpeed != speed)
                {
                    doSpeedTask(mCurIsReverse, speedParams[progress]);
                }
            }
        }
    };


    @Override
    public void SetData(HashMap<String, Object> params)
    {
//        if (params != null)
//        {
//            if (params.containsKey("videoIndex"))
//            {
//                videoIndex = (int) params.get("videoIndex");
//            }
//        }
    }

    @Override
    public void onClose()
    {
//        mSpeedVideoInfo.deleteCacheFile();
        if (mVoiceSwitch.getParent() == mVideoView)
        {
            mVideoView.removeView(mVoiceSwitch);
        }
        setSilenceState(mVideoInfo.mIsSilenceWhileSpeed,mVideoInfo.mCurSpeed);
        if(mIsUseClip)
        {
            mClipVideoInfo.deleteAllTempFile(mVideoInfo.mClipPath);
        }
//        mSpeedVideoInfo.deleteCacheFile(mSpeedVideoInfo.getVideoPath(mVideoInfo.taskIsReverse,mVideoInfo.mCurSpeed));
        mSpeedVideoInfo.deleteCacheFile();

        MyBeautyStat.onPageEndByRes(R.string.视频速率页);
    }

    @Override
    public void onBack()
    {
        MyBeautyStat.onClickByRes(R.string.视频速率页_退出视频速率);

        if(mCurSpeed != mVideoInfo.mCurSpeed || mCurIsReverse != mVideoInfo.mIsReverse)
        {
            mVideoView.changeVideoPath(mOriginalPath);
        }
        mSite.onBack(getContext(),false);
    }

    /**
     *  做速率和倒序操作，  然后再裁剪
     */
    class VideoSpeedTask implements Runnable
    {
        private float taskSpeed;
        private boolean taskIsReverse;
        private OnProcessListener mListener;
        public VideoSpeedTask(boolean isReverse, float speed,OnProcessListener listener)
        {
            super();
            this.taskSpeed = speed;
            taskIsReverse = isReverse;
            mListener = listener;

        }
        @Override
        public void run()
        {
            onStart();
            String speedPath = mSpeedVideoInfo.getVideoPath(taskIsReverse, taskSpeed);
            //没有对应的缓存，找到速度1做
            if (!isVideoExists(speedPath))
            {
                String normalSpeedPath = mSpeedVideoInfo.getVideoPath(taskIsReverse,1);
                //如果倒序没有速度1做，找回原始视频做倒序
                if(!isVideoExists(normalSpeedPath)){
                    String originalPath = mSpeedVideoInfo.getVideoPath(!taskIsReverse,1);
                    if(!isVideoExists(originalPath)){
                        onError();
                        return;
                    }
                    normalSpeedPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
                    ReverseTask reverseTask = new ReverseTask(originalPath,normalSpeedPath);
                    reverseTask.run();
                    if(!isVideoExists(normalSpeedPath)){
                        onError();
                        return;
                    }
                    mSpeedVideoInfo.putVideoPath(taskIsReverse,1,normalSpeedPath);
                }
                speedPath = mSpeedVideoInfo.getVideoPath(taskIsReverse, taskSpeed);
                if(speedPath == null)
                {
                    speedPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
                    SpeedTask speedTask = new SpeedTask(normalSpeedPath, taskSpeed, speedPath);
                    speedTask.run();
                    if (!isVideoExists(speedPath))
                    {
                        onError();
                        return;
                    }
                    mSpeedVideoInfo.putVideoPath(taskIsReverse, taskSpeed, speedPath);
                }
            }
            if(mIsUseClip)
            {
                String finalOutPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
                long startTime = getRealStartTime(taskSpeed);
                long endTime = getRealEndTime(taskSpeed);
                if(mVideoInfo.mIsReverse != taskIsReverse){
                    long temp = startTime;
                    startTime = getReadTotalDuration(taskSpeed) - endTime;
                    endTime = getReadTotalDuration(taskSpeed) - temp;
                }

                ClipVideoTask clipVideoTask = new ClipVideoTask(getContext(), speedPath, finalOutPath, startTime, endTime);
                clipVideoTask.run();
                if (isVideoExists(finalOutPath))
                {
                    mClipVideoInfo.putVideoPath(taskIsReverse, taskSpeed, finalOutPath);
                }else{
                    onError();
                }
            }else{
                mClipVideoInfo.putVideoPath(taskIsReverse, taskSpeed, speedPath);
            }
            onFinish();
        }

        private void onStart()
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    mListener.onStart();
                }
            });
        }

        private void onError()
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    mListener.onError(null);
                }
            });
        }

        private void onFinish()
        {
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    mListener.onFinish();
                }
            });
        }
    }

    private long getRealStartTime(float curSpeed)
    {
        return (long) (mVideoInfo.mSelectStartTime * mVideoInfo.mCurSpeed / curSpeed);
    }
    private long getRealEndTime(float curSpeed)
    {
        return (long) (mVideoInfo.mSelectEndTime * mVideoInfo.mCurSpeed / curSpeed);
    }

    private long getReadClipDuration(float curSpeed)
    {
        return (long) (mVideoInfo.GetClipTime() * mVideoInfo.mCurSpeed / curSpeed);
    }

    private long getReadTotalDuration(float curSpeed)
    {
        return (long) (mVideoInfo.mDuration * mVideoInfo.mCurSpeed / curSpeed);
    }



    public boolean isVideoExists(String path)
    {
        return !TextUtils.isEmpty(path) && new File(path).exists();
    }

}
