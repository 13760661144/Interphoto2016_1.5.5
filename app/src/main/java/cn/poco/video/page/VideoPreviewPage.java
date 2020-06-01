package cn.poco.video.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import cn.poco.capture2.AnimatorUtils;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.site.VideoPreviewSite;
import cn.poco.video.videoAlbum.VideoAlbumUtils;
import cn.poco.video.view.VideoSurface;

/**
 * Created by admin on 2018/1/8.
 */

public class VideoPreviewPage extends IPage {

    private static final String TAG = "VideoPreviewPage";
    Handler handler = new Handler();
    private Context context;
    private MediaPlayer mediaPlayer;
    private int mWidth, mHeight;
    private String videoPath;
    private VideoSurface surfaceView;
    private LayoutParams layoutParams;
    private SurfaceHolder surfaceHolder;
    private int duration;
    private float mOldX = 0;
    private FrameLayout mContainer;
    private VideoPreviewSite mSite;
    private ImageView backBtn;
    private TextView chooseVideo;
    private TextView currTime, totalTime;
    private HashMap<String, Object> videoData;
    private GestureDetectorCompat gestureDetectorCompat;

    private View mMaskView;

    private float mDownX;
    private float mDownPosition;
    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mOldX = event.getRawX();
                    mDownX = mOldX;
                    mDownPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float curX = event.getRawX();
                    long position = (long) (mDownPosition + ((curX - mDownX)) * duration * 2/ShareData.m_screenWidth); //半个屏幕距离可以控制完整个视频
                    if(position < 0){
                        position = 0;
                        mDownX += curX - mOldX;
                    }
                    if(position > duration){
                        position = duration;
                        mDownX += curX - mOldX;
                    }
                    mediaPlayer.seekTo((int) position);
                    mOldX = curX;
//                    long position = mediaPlayer.getCurrentPosition();
//                    if ((event.getRawX() - mOldX) > 0) {
//
//                        position += 200;
//                        if (position > duration) {
//                            position = duration;
//                        }
//                        mediaPlayer.seekTo((int) position);
//                    } else {
//                        position -= 200;
//                        if (position < 0) {
//                            position = 0;
//                        }
//                        mediaPlayer.seekTo((int) position);
//                    }
//                    mOldX = event.getRawX();

                    break;
                case MotionEvent.ACTION_UP:
                    mediaPlayer.start();

                    break;

            }
            return true;
        }
    };
    private int curNum = 0;
    private int usableTime = 0;
    private boolean isSelected = false; //视频是否被选择了
    private VideoEntry videoEntry;
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == backBtn) {
                onBack();
            } else if (v == chooseVideo) {
                if (videoEntry != null) {
                    if (isSelected) {
                        MyBeautyStat.onClickByRes(R.string.视频预览页_取消选择);
                        HashMap<String, Object> params = new HashMap<>();
                        params.put("video", videoEntry);
                        mSite.onBack(context, params);

                    } else {
                        if (VideoAlbumUtils.isVideoValid(context, videoEntry, curNum, usableTime)) {
                            HashMap<String, Object> params = new HashMap<>();
                            params.put("video", videoEntry);
                            mSite.onBack(context, params);
                            MyBeautyStat.onClickByRes(R.string.视频预览页_点击选择);
                        }
                    }
                } else {
                    mSite.onBack(context, null);
                }
            }

        }
    };
    private Runnable update;

    public VideoPreviewPage(Context mContext, BaseSite site) {
        super(mContext, site);
        context = mContext;
        mSite = (VideoPreviewSite) site;

        // mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/PingFangRegular.ttf");
        initView();

    }

    private String formatTime(long time) {
        long minute = (time / 60000) % 60;
        long second = (time / 1000) % 60;
        long millisecond = (time % 1000) / 100;

        return String.format(Locale.getDefault(), "%02d:%02d:%d", minute, second, millisecond);
    }

    private void initView() {

        setBackgroundColor(0xff0e0e0e);

        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        layoutParams.setMargins(0, 0, ShareData.PxToDpi_xhdpi(40), 0);
        RelativeLayout topBar = new RelativeLayout(context);
        this.addView(topBar, layoutParams);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        backBtn = new ImageView(context);
        backBtn.setOnClickListener(onClickListener);
        backBtn.setImageResource(R.drawable.framework_back_btn);
        topBar.addView(backBtn, rl);

        rl = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        chooseVideo = new TextView(context);
        chooseVideo.setGravity(Gravity.CENTER);
        chooseVideo.setOnClickListener(onClickListener);
        chooseVideo.setPadding(ShareData.PxToDpi_xhdpi(10), 0, ShareData.PxToDpi_xhdpi(10), 0);
        chooseVideo.setTextColor(0xffffffff);
        chooseVideo.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        topBar.addView(chooseVideo, rl);


        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, ShareData.PxToDpi_xhdpi(150), 0, 0);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textView.setTextColor(0xff666666);
        textView.setText(getResources().getString(R.string.video_preview_hint));
        this.addView(textView, layoutParams);

        layoutParams = new LayoutParams(ShareData.PxToDpi_xhdpi(640), ShareData.PxToDpi_xhdpi(640));
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        mContainer = new FrameLayout(context);
        mContainer.setBackgroundColor(Color.BLACK);
        layoutParams.setMargins(ShareData.PxToDpi_xhdpi(0), ShareData.PxToDpi_xhdpi(203), ShareData.PxToDpi_xhdpi(0), 0);
        this.addView(mContainer, layoutParams);


        layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(893);
        this.addView(linearLayout, layoutParams);

        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        currTime = new TextView(getContext());
        // currTime.setTypeface(mTypeface);
        currTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        currTime.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        currTime.setTextColor(0xff999999);
        linearLayout.addView(currTime, ll);
        totalTime = new TextView(getContext());
        //totalTime.setTypeface(mTypeface);
        totalTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        totalTime.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        totalTime.setTextColor(0xffffffff);
        linearLayout.addView(totalTime, ll);
    }

    private void intiPlayer() {

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        //   gestureDetectorCompat = new GestureDetectorCompat(getContext(),gestureListener);
        surfaceView = new VideoSurface(context);
        //    surfaceView = new VideoSurface(context,gestureDetectorCompat);
        surfaceView.setVideoMeasuredDimension(mWidth, mHeight);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceViewCallback());
        surfaceView.setOnTouchListener(onTouchListener);
        mContainer.addView(surfaceView, layoutParams);

        mMaskView = new View(context);
        mMaskView.setBackgroundColor(Color.BLACK);
        layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mMaskView);


    }

    private void startTime() {
        update = new Runnable() {
            @Override
            public void run() {
                currTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.post(update);
            }
        };
        handler.post(update);
    }


    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            videoData = params;
            videoEntry = (VideoEntry) videoData.get("video");
            videoPath = videoEntry.mMediaPath;
            mWidth = videoEntry.mWidth;
            mHeight = videoEntry.mHeight;
            if (params.containsKey("cur_num")) {
                curNum = (int) params.get("cur_num");
            }
            if (params.containsKey("usable_time")) {
                usableTime = (int) params.get("usable_time");
            }
            if (params.containsKey("is_selected")) {
                isSelected = (boolean) params.get("is_selected");
            }
            if (params.containsKey("bk")) {
                Bitmap bitmap = (Bitmap) params.get("bk");
                if (bitmap != null) {
                    setBackground(new BitmapDrawable(bitmap));
                } else {
                    setBackgroundColor(0xff0e0e0e);
                }
            }
        }
        if (isSelected) {
            chooseVideo.setText(getResources().getString(R.string.video_preview_deselect));
        } else {
            chooseVideo.setText(getResources().getString(R.string.video_preview_select));
        }
        intiPlayer();

    }


    @Override
    public void onBack() {
        mSite.onBack(context);
        MyBeautyStat.onClickByRes(R.string.视频预览页_退出预览页);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

    }

    @Override
    public void onClose() {
        super.onClose();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (handler != null) {
            handler.removeCallbacks(update);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }


    private class SurfaceViewCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {

                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(videoPath);
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                        mediaPlayer.setLooping(true);
                        duration = mediaPlayer.getDuration();
                        totalTime.setText("/" + formatTime(mediaPlayer.getDuration()));
                        startTime();
                    }
                });
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            AnimatorUtils.removeView(mContainer, mMaskView, 0);
                            Log.i(TAG, "surfaceCreated:移除 ");
                        }
                        return true;
                    }
                });
                Log.i(TAG, "surfaceCreated:创建成功 ");
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            surfaceView.setVideoMeasuredDimension(mWidth, mHeight);
            surfaceView.requestLayout();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    }
/*    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mediaPlayer.pause();
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
            long position = mediaPlayer.getCurrentPosition();
            if (distanceX <0){
                position += 200;
                if (position > duration) {
                    position = duration;
                }
                mediaPlayer.seekTo((int) position);

                Log.i(TAG, "onScroll:右" );
            }else {
                position -= 200;
                if (position < 0) {
                    position = 0;
                }
                mediaPlayer.seekTo((int) position);

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
    };*/
}
