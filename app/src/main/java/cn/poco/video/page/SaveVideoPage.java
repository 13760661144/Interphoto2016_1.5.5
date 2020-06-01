package cn.poco.video.page;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.view.GestureDetectorCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cn.poco.album2.view.ProgressView;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.video.save.SaveParams;
import cn.poco.video.save.SaveThread;
import cn.poco.video.site.SaveVideoSite;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.view.VideoSurface;


/**
 * Created by admin on 2018/1/24.
 */

public class SaveVideoPage extends IPage {
    private final String TAG = "SaveVideoPage";
    private final long circleSupportDuration = 2000;
    int mImageContainerH = 0;
    int mImageContainerW = 0;
    String videoFile = "";
    Boolean isSaving = true;
    private SaveParams saveParams;
    private SaveVideoSite mSite;
    private ProgressView mProgressView;
    private TextView m_titleView, saveText, hintView;
    private FrameLayout m_topBar;
    private ImageView m_backBtn;
    private ImageView m_homeBtn;
    private FrameLayout mBottomLayout, mImageContainer;
    private ImageView mImage;
    private int mBottomHeight;
    private FrameLayout surfaceContainer;
    private ShareVideoView shareVideoView;
    private VideoSurface mVideoSurface;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;
    private float mOldX = 0;
    private int duration;
    private boolean isFromCamera;
    private Bitmap coverBmp, backgroundBmp;
    private SaveThread mSaveThread;
    private VideoUtils.VideoInfo mVideoInfo;
    private int position;
    private Boolean isSupportShareToCircle;
    private ShareVideoView.IsShareCircleCallBack shareCircleCallBack;
    private Boolean isShareCircle = false;
    private GestureDetectorCompat gestureDetectorCompat;
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == m_backBtn) {
                if (isSaving) {
                    mSaveThread.requestExit();
                } else {
                    MyBeautyStat.onClickByRes(R.string.视频美化_保存与分享_退出保存与分享);
                    mSite.onBack(getContext(), null);
                }
            }
            if (v == m_homeBtn) {
                MyBeautyStat.onClickByRes(R.string.保存与分享_返回首页);
                MyFramework.SITE_BackTo(getContext(), HomePageSite.class, null, Framework2.ANIM_TRANSITION);
            }
        }
    };

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldX = event.getRawX();
                    mMediaPlayer.pause();
                    break;
                case MotionEvent.ACTION_MOVE:
                    long position = mMediaPlayer.getCurrentPosition();


                    if ((event.getRawX() - mOldX) > 0) {
                        position += 200;
                        if (position > duration) {
                            position = duration;
                        }
                        mMediaPlayer.seekTo((int) position);
                    } else {
                        position -= 200;
                        if (position < 0) {
                            position = 0;
                        }
                        mMediaPlayer.seekTo((int) position);
                    }
                    mOldX = event.getRawX();

                    break;
                case MotionEvent.ACTION_UP:
                    mMediaPlayer.start();

                    break;

            }
            return true;
        }
    };
    private SaveThread.OnSaveListener mOnSaveListener = new SaveThread.OnSaveListener() {

        @Override
        public void onStart() {
            hintView.setVisibility(VISIBLE);
            isSaving = true;
        }

        @Override
        public void onProgress(float progress) {
            mProgressView.setProgress(progress);

        }

        @Override
        public void onCancel() {
            FileUtils.delete(saveParams.outputPath);
            mSite.onBack(getContext(), null);
        }

        @Override
        public void onFinish() {
            mProgressView.setProgress(100);
            hintView.setVisibility(GONE);
            m_titleView.setVisibility(VISIBLE);
            m_homeBtn.setVisibility(VISIBLE);
            mImageContainer.setVisibility(GONE);
            onVideoSaveFinish(saveParams.outputPath);

            mVideoInfo = VideoUtils.getVideoInfo(saveParams.outputPath);
            initSurface(saveParams.outputPath);
            videoFile = saveParams.outputPath;
            shareVideoView.setData(isFromCamera, saveParams.outputPath, saveParams, isSupportShareToCircle);
            shareVideoViewAnim(shareVideoView);
        }
    };
    private Boolean isSurfaceCreated = false;

    public SaveVideoPage(Context context, BaseSite site) {
        super(context, site);
        mSite = (SaveVideoSite) site;
        mBottomHeight = ShareData.PxToDpi_xhdpi(80);

        initUI();
    }

    /**
     * 保存结束
     *
     * @param outputPath 视频路径
     */
    private void onSaveStatistic(String outputPath) {
        if (saveParams != null) {
            String filterId = null;
//            if (saveParams.filterItem != null) {
//                filterId = mSaveParams.filterId;
//            }

            String musicId = "0000";
            if (saveParams.musicPath != null) {
                musicId = saveParams.musicId;
            }
            String textId = "0000";
            if (saveParams.videoText != null) {
                textId = saveParams.textId;
            }
            long duration = VideoUtils.getDurationFromVideo2(outputPath);
            boolean silence = saveParams.videoVolume == 0;
            MyBeautyStat.onVideoSave(filterId, musicId, silence, textId, saveParams.videoInfos.size(), duration, R.string.视频_保存与分享);
        }
    }

    private void initUI() {

        LayoutParams params;
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        params.gravity = Gravity.TOP;
        m_topBar = new FrameLayout(getContext());
        this.addView(m_topBar, params);

        m_backBtn = new ImageView(getContext());
        m_backBtn.setOnClickListener(onClickListener);
        m_backBtn.setTag(1);
        m_backBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        m_backBtn.setImageResource(R.drawable.framework_back_btn);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        m_backBtn.setLayoutParams(params);
        m_topBar.setId(R.id.m_topBar);
        m_topBar.addView(m_backBtn);

        {
            m_titleView = new TextView(getContext());
            m_titleView.setText(getResources().getString(R.string.share_video_title));
            m_titleView.setVisibility(GONE);
            m_titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            m_titleView.setLayoutParams(params);
            m_topBar.addView(m_titleView);

            m_homeBtn = new ImageView(getContext());
            m_homeBtn.setVisibility(GONE);
            m_homeBtn.setOnClickListener(onClickListener);
            m_homeBtn.setTag(1);
            m_homeBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            m_homeBtn.setImageResource(R.drawable.camera_back_home);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            m_homeBtn.setLayoutParams(params);
            m_topBar.addView(m_homeBtn);
        }

        mImageContainer = new FrameLayout(getContext());
        mImageContainer.setBackgroundColor(Color.BLACK);
        addView(mImageContainer);

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mImage = new ImageView(getContext());
        mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        params.gravity = Gravity.CENTER;
        mImageContainer.addView(mImage);


        //底部进度条
        mBottomLayout = new FrameLayout(getContext());
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomHeight);
        params.gravity = Gravity.BOTTOM;
        addView(mBottomLayout, params);
        {
            mProgressView = new ProgressView(getContext());
            mProgressView.setNormalColor(0x33ffc433);
            mProgressView.setProgressColor(0xffffc433);
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mBottomLayout.addView(mProgressView, params);

            saveText = new TextView(getContext());
            saveText.setText(R.string.video_saving);
            saveText.setTextColor(Color.WHITE);
            saveText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            mBottomLayout.addView(saveText, params);
        }

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hintView = new TextView(getContext());
        hintView.setVisibility(GONE);
        hintView.setText(getResources().getString(R.string.share_video_hint_save));
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        int botton = (int) (ShareData.getScreenH() / 2) - ShareData.PxToDpi_xhdpi(350);
        params.bottomMargin = ShareData.PxToDpi_xhdpi(botton);
        addView(hintView, params);


        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(444));
        shareVideoView = new ShareVideoView(getContext());
        shareVideoView.setVisibility(GONE);
        params.gravity = Gravity.BOTTOM;
        addView(shareVideoView, params);

        shareCircleCallBack = new ShareVideoView.IsShareCircleCallBack() {
            @Override
            public void isShareCircle(boolean shareCircle) {
                isShareCircle = shareCircle;
            }
        };
        shareVideoView.setIsShareCircleCallBack(shareCircleCallBack);
    }

    private void initSurface(String videoPath) {

        LayoutParams params;
        surfaceContainer = new FrameLayout(getContext());
        params = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(640), ShareData.PxToDpi_xhdpi(640));
        surfaceContainer.setBackgroundColor(Color.BLACK);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = ShareData.PxToDpi_xhdpi(100);
        addView(surfaceContainer, params);

        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mVideoSurface = new VideoSurface(getContext());
        mVideoSurface.setVideoMeasuredDimension(mVideoInfo.width, mVideoInfo.height);
        mVideoSurface.setOnTouchListener(onTouchListener);
        mVideoSurface.setPadding(0, 0, 0, ShareData.PxToDpi_xhdpi(21));
        createSurfaceHolder();
/*        mSurfaceHolder = mVideoSurface.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SaveVideoPage.SurfaceViewCallback());*/
        //mVideoSurface.setOnTouchListener(onTouchListener);
        surfaceContainer.addView(mVideoSurface, params);

        try {

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                    mMediaPlayer.setLooping(true);
                    duration = mMediaPlayer.getDuration();

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (shareVideoView != null) {
            shareVideoView.onActivityResult(requestCode, requestCode, data);
        }

        return super.onActivityResult(requestCode, resultCode, data);
    }

    private void createSurfaceHolder() {
        mSurfaceHolder = mVideoSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceViewCallback());

    }

    /**
     * 视频保存成功
     *
     * @param outputPath 视频路径
     */
    private void onVideoSaveFinish(String outputPath) {
        isSaving = false;
        mProgressView.setVisibility(GONE);
        saveText.setVisibility(GONE);

        VideoUtils.addVideoToMedia(getContext(), outputPath);
        onSaveStatistic(outputPath);
    }

    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            if (params.containsKey("maskBitmap")) {
                backgroundBmp = (Bitmap) params.get("maskBitmap");
                setBackground(new BitmapDrawable(backgroundBmp));
            }
            if (params.containsKey("width")) {
                mImageContainerW = (int) params.get("width");
            }
            if (params.containsKey("height")) {
                mImageContainerH = (int) params.get("height");
            }
            if (params.containsKey("coverBitmap")) {
                coverBmp = (Bitmap) params.get("coverBitmap");
            }
            if (params.containsKey("params")) {
                saveParams = (SaveParams) params.get("params");
            }
            if (params.containsKey("isFromCamera")) {
                isFromCamera = (boolean) params.get("isFromCamera");
            }
            if (params.containsKey("videoSaveDuration")) {
                long duration = (long) params.get("videoSaveDuration");
                isSupportShareToCircle = duration - circleSupportDuration > 0;

            }
        }
        LayoutParams lp = new LayoutParams(ShareData.getScreenW(), ShareData.getScreenW());
        lp.topMargin = ShareData.PxToDpi_xhdpi(80);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        mImageContainer.setLayoutParams(lp);


        if (coverBmp.getWidth() > coverBmp.getHeight()) {
            lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, coverBmp.getHeight());
            lp.gravity = Gravity.CENTER;
            mImage.setLayoutParams(lp);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImage.setImageBitmap(coverBmp);
        } else if (coverBmp.getWidth() < coverBmp.getHeight()) {
            lp = new LayoutParams(coverBmp.getWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.CENTER;
            mImage.setLayoutParams(lp);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImage.setImageBitmap(coverBmp);
        } else if (coverBmp.getWidth() == coverBmp.getHeight()) {
            lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.gravity = Gravity.CENTER;
            mImage.setLayoutParams(lp);
            mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImage.setImageBitmap(coverBmp);
        }


        saveParams.outputPath = VideoUtils.getVideoSavePath(getContext());
        setVideoLogo(saveParams);
        mSaveThread = SaveThread.start(getContext(), saveParams, mOnSaveListener);
        shrinkVideoView(mImageContainer);

    }

    private void setVideoLogo(SaveParams params) {
        params.logoPath = null;
        String videoLogo = SettingInfoMgr.GetSettingInfo(getContext()).getVideoLogo();
        if (videoLogo != null) {
            File file = new File(videoLogo);
            if (file.exists()) {
                params.logoPath = videoLogo;
            }
        }

        if (params.logoPath == null) {
            params.videoLogo = getVideoLogo(videoLogo);
        }
    }

    /**
     * 获取水印logo的资源id
     */
    private int getVideoLogo(String logoString) {
        int logoIndex = logoString == null ? 0 : -1;
        try {
            logoIndex = Integer.valueOf(logoString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (logoIndex == -1) {
            return 0;
        }

        int resId = 0;
        switch (logoIndex) {
            case 0:
                resId = R.drawable.interphpoto_logo_1;
                break;
            case 1:
                resId = R.drawable.interphpoto_logo_2;
                break;
            case 2:
                resId = R.drawable.interphpoto_logo_3;
                break;
            case 3:
                resId = R.drawable.interphpoto_logo_4;
                break;
            case 4:
                resId = R.drawable.interphpoto_logo_5;
                break;
            case 5:
                resId = R.drawable.interphpoto_logo_6;
                break;
            case 6:
                resId = R.drawable.interphpoto_logo_7;
                break;
        }

        return resId;
    }

    private void shareVideoViewAnim(View view) {
        MyBeautyStat.onClickByRes(R.string.视频美化_保存与分享_分享);
        int translationY = ShareData.PxToDpi_xhdpi(444);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", translationY, 0);
        objectAnimator.setDuration(500);

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                shareVideoView.setVisibility(VISIBLE);
            }
        });
        objectAnimator.start();

    }

    private void shrinkVideoView(View view) {

        int fixed = ShareData.PxToDpi_xhdpi(640);
        float ratio = 0;
        ratio = fixed * 1.8f / ShareData.getScreenH();
        int translationY = (int) (mImageContainerH - mImageContainerH * ratio) / 2;
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, ratio);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, ratio);
        ObjectAnimator translaY = ObjectAnimator.ofFloat(view, "translationY", 0, -translationY);

        animatorSet.setDuration(500);
        animatorSet.play(scaleX).with(scaleY).with(translaY);
        animatorSet.start();

    }


    @Override
    public void onBack() {
        if (isSaving) {
            mSaveThread.requestExit();

        } else {
            MyBeautyStat.onClickByRes(R.string.视频美化_保存与分享_退出保存与分享);
            mSite.onBack(getContext(), null);
        }

    }

    private void play(final int currentPosition) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(videoFile);
            mMediaPlayer.setDisplay(mVideoSurface.getHolder());
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.seekTo(currentPosition);
                    mMediaPlayer.start();

                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClose() {
        super.onClose();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVideoSurface != null) {
            mVideoSurface = null;
        }
    }

    @Override
    public void onResume() {

        if (isShareCircle) {
            isShareCircle = false;
        } else {
            if (mMediaPlayer != null) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        play(position);
                    }
                }, 10);

            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            position = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.pause();

        }

    }


    private class SurfaceViewCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mMediaPlayer.setDisplay(holder);
            isSurfaceCreated = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mVideoSurface.setVideoMeasuredDimension(mVideoInfo.width, mVideoInfo.height);
            mVideoSurface.requestLayout();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isSurfaceCreated = false;
        }
    }

/*
    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mMediaPlayer.pause();
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            long position = mMediaPlayer.getCurrentPosition();
            if (distanceX <0){
                position += 200;
                if (position > duration) {
                    position = duration;
                }
                mMediaPlayer.seekTo((int) position);
                Log.i(TAG, "onScroll:右" );
            }else {
                position -= 200;
                if (position < 0) {
                    position = 0;
                }
                mMediaPlayer.seekTo((int) position);
                Log.i(TAG, "onScroll: 左" );
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };
*/


}
