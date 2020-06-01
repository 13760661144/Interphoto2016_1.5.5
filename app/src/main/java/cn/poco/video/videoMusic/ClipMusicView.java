package cn.poco.video.videoMusic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.music.WaveBitmapFactory;
import cn.poco.music.WaveLineView;
import cn.poco.tianutils.ShareData;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by lgd on 2017/6/22.
 */

public class ClipMusicView extends FrameLayout {
    private static final String TAG = "VoiceFrequencyView";
    private ClipAreaView mClipAreaView;                     // 黄色裁剪区域，位置固定死
    private ClipHorizontalScrollView mClipScrollView;       // 音乐滚动区域父布局
    private TextView mClipTime;         //文字提示
    public final int MAX_CLIP_W = (int) (ShareData.m_screenWidth * 0.5f);
    public final int MIN_CLIP_W = (int) (ShareData.m_screenWidth * 0.1f);
//    public final float MIN_PRESENT = 0.025f;                //音乐每秒间隔最短SharaData.m_width *  MIN_PRESENT
//    private int mMusicInterval;  //音乐每秒的间隔，
    private int mSpan = ShareData.PxToDpi_xhdpi(10);  //音乐每秒的间隔，
    private int mWaveW;  // 段总宽度
    private int mClipW;  //黄色裁剪区域宽度
    private int mClipH;      // 黄色裁剪区域 和 音乐滚动区域的高度
    private FrequencyInfo mInfo;
    private int mScrollStartTime = 0;  //滚动条的裁剪开始时间
    private int mEmptyViewW;           // 滚动区域左右两边空view的宽度，填空使用，用于滚动

//    protected int mSpan;                 //每秒的间隔
//    protected int mWaveW;                //频段宽度
//    protected int mClipW;                //裁剪区域大小
//    protected int mClipH;                //裁剪区域高度
//    protected int mScrollStartTime = 0; //滚动条的裁剪时间（秒）
//    protected int mEmptyViewW;          //空白频段区域
//    protected int mTopMargin;           //顶部高度间距

    public ClipMusicView(@NonNull Context context, FrequencyInfo info) {
        super(context);
        this.mInfo = info;
        initData();
        init();
    }

    public void initData() {
        //根据视屏时间和音乐的时间 计算音乐每秒的间隔
//        float multiple = (mInfo.musicTime * MIN_PRESENT);
//        int span = 0;
//        if (multiple >= 2) {
//            float f = MIN_PRESENT - 0.001f * multiple;
//            if (f >= 0.01) {
//                span = (int) (ShareData.m_screenWidth * f);
//            } else {
//                span = (int) (ShareData.m_screenWidth * 0.01f);
//            }
//        } else {
//            span = (int) (ShareData.m_screenWidth * MIN_PRESENT);
//        }
//        mSpan = span;
//        mWaveW = mSpan * mInfo.musicTime;
//        mClipW = mSpan * mInfo.videoTime;
//        if(mClipW > mWaveW){
//            //视屏大于音乐长度，不允许滚动
//            if(mClipW > ShareData.m_screenWidth){
//                mClipW = ShareData.m_screenWidth;
//            }
//            mWaveW = mClipW;
//        }
//        mClipH = ShareData.PxToDpi_xhdpi(100);
//        mScrollStartTime = mInfo.startTime;
//        //头部和尾部需要增加空view    大小减去bpw/2
//        mEmptyViewW = (ShareData.m_screenWidth - mClipAreaW) / 2;

        mEmptyViewW = (ShareData.m_screenWidth - mClipW) / 2;
//        mClipW = ShareData.PxToDpi_xhdpi(354) - ClipAreaView.verticalLineW * 2;
        mClipW = mInfo.videoTime * mSpan;
        if(mClipW < MIN_CLIP_W){
            mClipW = MIN_CLIP_W;
        }else if(mClipW > MAX_CLIP_W){
            mClipW = MAX_CLIP_W;
        }
        if (mInfo.videoTime >= mInfo.musicTime)
        {
            //视屏大于音乐长度，不允许滚动
            mWaveW = mClipW;
            mSpan = mWaveW / mInfo.musicTime;
        }
        else
        {
            mWaveW = mSpan * (mInfo.musicTime - mInfo.videoTime) + mClipW;
        }
        mClipH = ShareData.PxToDpi_xhdpi(100);
        mScrollStartTime = mInfo.startTime;
        mEmptyViewW = (int) ((ShareData.m_screenWidth - mClipW) * 1.0f / 2 + 0.5f);//头部和尾部需要增加空view ,大小减去clipW/2
    }

    private void init() {
        LayoutParams params;
        int bottomMargin = ShareData.PxToDpi_xhdpi(26);

        mClipTime = new TextView(getContext());
        mClipTime.setText(transformTime(0, mInfo.videoTime));
        mClipTime.setTextColor(Color.WHITE);
        mClipTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        mClipTime.setMinHeight(ShareData.PxToDpi_xhdpi(25));
        mClipTime.setGravity(Gravity.CENTER);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.bottomMargin = bottomMargin + mClipH + ShareData.PxToDpi_xhdpi(15);
        addView(mClipTime, params);

        mClipScrollView = new ClipHorizontalScrollView(getContext());
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mClipH);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = bottomMargin;
        addView(mClipScrollView, params);
        {
            LinearLayout parent = new LinearLayout(getContext());
            parent.setOrientation(LinearLayout.HORIZONTAL);
            params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mClipScrollView.addView(parent, params);
            {
                LinearLayout.LayoutParams params1;
                View leftView = new View(getContext());
                params1 = new LinearLayout.LayoutParams(mEmptyViewW, ViewGroup.LayoutParams.MATCH_PARENT);
                parent.addView(leftView, params1);

                WaveBitmapFactory.WaveInfo info = getWaveInfo(mWaveW, mClipH - ClipAreaView.horizontalLineH * 2);
                if(info != null){
                    WaveLineView waveLineView = new WaveLineView(getContext(),info);
                    waveLineView.setZoom(1.3f);
                    LayoutParams layoutParams = new LayoutParams(mWaveW, ViewGroup.LayoutParams.MATCH_PARENT);
                    waveLineView.setLayoutParams(layoutParams);
                    parent.addView(waveLineView);
                }
                else {
                    ClipLineView lineView = new ClipLineView(getContext());
                    LayoutParams layoutParams = new LayoutParams(mWaveW, ViewGroup.LayoutParams.MATCH_PARENT);
                    lineView.setLayoutParams(layoutParams);
                    parent.addView(lineView);
                }

                View rightView = new View(getContext());
                params1 = new LinearLayout.LayoutParams(mEmptyViewW, ViewGroup.LayoutParams.MATCH_PARENT);
                parent.addView(rightView, params1);
            }
        }
        if(mWaveW > mClipW)
        {
            mClipScrollView.setScrollViewListener(mScrollViewListener);
        }
        mClipScrollView.setHorizontalScrollBarEnabled(false);

        //左边遮罩层，使非裁剪区域的白色线段变暗
        View leftMask = new View(getContext());
        leftMask.setBackgroundColor(mInfo.bkColor);
        leftMask.getBackground().setAlpha((int) (255 * 0.8f));
        params = new LayoutParams(mEmptyViewW, mClipH);
        params.gravity = Gravity.BOTTOM | Gravity.LEFT;
        params.bottomMargin = bottomMargin;
        addView(leftMask, params);

        //右边遮罩层，使非裁剪区域的白色线段变暗
        View rightMask = new View(getContext());
        rightMask.setBackgroundColor(mInfo.bkColor);
        rightMask.getBackground().setAlpha((int) (255 * 0.8f));
        params = new LayoutParams(mEmptyViewW, mClipH);
        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        params.bottomMargin = bottomMargin;
        addView(rightMask, params);

        mClipAreaView = new ClipAreaView(getContext());
//        int w = mClipW;
//        if (w > ShareData.m_screenWidth ) {
//            //最大屏幕宽度
//            w = ShareData.m_screenWidth;
//        }
//        if( w < ClipAreaView.verticalLineW * 2 ){
//            w = ClipAreaView.verticalLineW * 2;
//        }
        params = new LayoutParams(mClipW+ClipAreaView.verticalLineW * 2 , mClipH);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = bottomMargin;
        addView(mClipAreaView, params);

        mClipScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //第一次设置参数
                mClipScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mClipScrollView.setScrollX(mSpan * mScrollStartTime);
                mClipTime.setText(transformTime(mScrollStartTime, mScrollStartTime + mInfo.videoTime));
            }
        });
    }

    /**
     * 时间转化   00 : 00 格式
     * @param startTime
     * @param endTime
     * @return
     */
    private String transformTime(int startTime, int endTime) {
        StringBuilder stringBuilder = new StringBuilder();
        int min = startTime / 60;
        int sec = startTime % 60;
        if (min < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(String.valueOf(min));
        stringBuilder.append(":");
        if (sec < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(String.valueOf(sec));
        stringBuilder.append("-");

        min = endTime / 60;
        sec = endTime % 60;
        if (min < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(String.valueOf(min));
        stringBuilder.append(":");
        if (sec < 10) {
            stringBuilder.append("0");
        }
        stringBuilder.append(String.valueOf(sec));
        return stringBuilder.toString();
    }

    private ClipHorizontalScrollView.ScrollViewListener mScrollViewListener = new ClipHorizontalScrollView.ScrollViewListener() {
        @Override
        public void onScrollChanged(ClipHorizontalScrollView.ScrollType scrollType, int scrollX) {
            //根据滚动距离计算音乐裁剪时间
//            mScrollStartTime = (int) (scrollX * mInfo.musicTime * 1.0f / mWaveW + 0.5f);
            mScrollStartTime = (int) (scrollX * (mInfo.musicTime - mInfo.videoTime) * 1.0f / (mWaveW - mClipW) + 0.5f);
            mClipTime.setText(transformTime(mScrollStartTime, mScrollStartTime + mInfo.videoTime));
            if (scrollType == ClipHorizontalScrollView.ScrollType.IDLE) {
                //位移的最大距离为
                if (onCallBack != null) {
                    onCallBack.onStop(mScrollStartTime);
                }
            } else {
                if (onCallBack != null) {
                    onCallBack.onScroll(mScrollStartTime);
                }
            }

        }
    };

    private WaveBitmapFactory.WaveInfo getWaveInfo(int width,int height)
    {
        String path = mInfo.musicPath;
        if (!((path.endsWith(FileUtils.MP3_FORMAT) || path.endsWith(FileUtils.WAV_FORMAT)))) {
            path = FileUtils.getTempPath(FileUtils.WAV_FORMAT);
            boolean result = VideoUtils.changeToAac(mInfo.musicPath, path);
            if (!result) {
                return null;
            }
        }
        WaveBitmapFactory waveBitmapFactory = new WaveBitmapFactory(width, height);
        waveBitmapFactory.setZoom(1.3f);
        boolean b = waveBitmapFactory.setSoundFilePath(path);
        if (!b) {
            return null;
        };
        return waveBitmapFactory.getData();
    }

    private OnCallBack onCallBack;

    public void setOnCallBack(OnCallBack onCallBack) {
        this.onCallBack = onCallBack;
    }

    public interface OnCallBack {
        //音乐播放需要seekto 到这时间
        void onStop(int second);

        void onScroll(int mScrollStartTime);
    }

    /**
     * 全部以秒单位
     */
    static public class FrequencyInfo {
        public int musicTime;
        public int videoTime;
        public String musicPath;
        public int startTime;
        public String musicFormat;
        public int bkColor;
    }

    /**
     * 假的音频线段
     */
    public class ClipLineView extends View {
        private int[] lineHeights;
        private int span;
        private int strokeW;
        private Paint paint;

        public ClipLineView(Context context) {
            super(context);
            lineHeights = new int[]{ShareData.PxToDpi_xhdpi(16), ShareData.PxToDpi_xhdpi(34), ShareData.PxToDpi_xhdpi(50), ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(50),ShareData.PxToDpi_xhdpi(34),ShareData.PxToDpi_xhdpi(16)};
            span = ShareData.PxToDpi_xhdpi(5);
            strokeW = ShareData.PxToDpi_xhdpi(2);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int startX = 0;
            int lineH;
            int lineIndex = 0;
            while (strokeW * 2 + startX <= getWidth()) {
                int i = lineIndex % lineHeights.length;
                startX = span * lineIndex;
                lineH = lineHeights[i];
                canvas.drawRect(startX, (getHeight() - lineH) / 2, startX + strokeW, ((getHeight() + lineH) / 2), paint);
                lineIndex++;
            }

        }
    }

    /**
     * 黄色裁剪区域
     */
    protected static class ClipAreaView extends View {
        private Paint linePaint;
        private Paint bkPaint;
        private int lineStoreW;
        private static final int horizontalLineH = ShareData.PxToDpi_xhdpi(5);
        private static final int verticalLineW = ShareData.PxToDpi_xhdpi(20);
        private Rect rect1;
        private Rect rect2;
        private Rect rect3;
        private Rect rect4;

        public ClipAreaView(Context context) {
            super(context);
            init();
        }

        private void init() {
            bkPaint = new Paint();
            bkPaint.setColor(0xffffc433);
            bkPaint.setAntiAlias(true);

            lineStoreW = ShareData.PxToDpi_xhdpi(3);
            linePaint = new Paint();
            linePaint.setColor(Color.WHITE);
            linePaint.setAntiAlias(true);
            linePaint.setStrokeWidth(lineStoreW);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            //横向两条线
            rect1 = new Rect(0, 0, w, horizontalLineH);
            rect2 = new Rect(0, h - horizontalLineH, w, h);
            //垂直两条线
            rect3 = new Rect(0, 0, verticalLineW, h);
            rect4 = new Rect(w - verticalLineW, 0, w, h);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(rect1, bkPaint);
            canvas.drawRect(rect2, bkPaint);
            canvas.drawRect(rect3, bkPaint);
            canvas.drawRect(rect4, bkPaint);
        }
    }


}
