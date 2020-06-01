package cn.poco.video.videotext;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.video.sequenceMosaics.VideoInfo;

/**
 * Created by lgd on 2017/10/24.
 */

public class VideoTextDisplayView extends IPage
{
    private LinearLayout mDispalyLL;
    private FrameLayout mBottomFr;
    private int mBottomH;
    private TextView start;
    private TextView whole;
    private TextView end;
    private int mMode;
    private WatermarkTimeLineView mTimeLineView;
    private List<VideoInfo> mVideoInfos;
    private long mVideosTime; //视频事件
    private long mTextTime; //水印动画时间
    private long mOriginalStartTime; //开始时间
    private ImageView mBack;
    private long mOriginalStayTime;  //动画结束后持续时间
    private long mCurStartTime;   //当前开始时间
    private long mCurEndTime;   //当前结束时间
    private FrameLayout mTipFr;

    public VideoTextDisplayView(@NonNull Context context,int bottomH, DisPlayCallBack callBack)
    {
        super(context,null);
        this.mCallBack = callBack;
        mBottomH = bottomH;
        initUi();
    }

    private void initUi()
    {
        setOnClickListener(mOnClickListener);
        if (mBottomH == 0)
        {
            mBottomH = ShareData.PxToDpi_xhdpi(254);
        }
        LayoutParams fl;
        LinearLayout.LayoutParams ll;
        mDispalyLL = new LinearLayout(getContext());
        mDispalyLL.setOrientation(LinearLayout.HORIZONTAL);
        mDispalyLL.setGravity(Gravity.CENTER);
        mDispalyLL.setClickable(true);
        mDispalyLL.setBackgroundColor(0xff212121);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomH,Gravity.BOTTOM);
        addView(mDispalyLL, fl);
        {
            start = new TextView(getContext())
            {
                @Override
                public void setSelected(boolean selected)
                {
                    super.setSelected(selected);
                    Drawable drawable;
                    if (selected)
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_more);
                    } else
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_start_default);
                    }
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    start.setCompoundDrawables(null, drawable, null, null);
                }
            };
            start.setCompoundDrawablePadding(ShareData.PxToDpi_xhdpi(30));
            start.setGravity(Gravity.CENTER);
            start.setTextColor(Color.WHITE);
            start.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            start.setText(R.string.text_start);
            start.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.weight = 1;
            mDispalyLL.addView(start, ll);

            whole = new TextView(getContext())
            {
                @Override
                public void setSelected(boolean selected)
                {
                    super.setSelected(selected);
                    Drawable drawable;
                    if (selected)
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_more);
                    } else
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_all_default);
                    }
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    whole.setCompoundDrawables(null, drawable, null, null);
                }
            };
            whole.setCompoundDrawablePadding(ShareData.PxToDpi_xhdpi(30));
            whole.setTextColor(Color.WHITE);
            whole.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            whole.setText(R.string.text_whole);
            whole.setGravity(Gravity.CENTER);
            whole.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.weight = 1;
            mDispalyLL.addView(whole, ll);

            end = new TextView(getContext())
            {
                @Override
                public void setSelected(boolean selected)
                {
                    super.setSelected(selected);
                    Drawable drawable;
                    if (selected)
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_more);
                    } else
                    {
                        drawable = getResources().getDrawable(R.drawable.video_text_end_default);
                    }
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    end.setCompoundDrawables(null, drawable, null, null);
                }
            };
            end.setCompoundDrawablePadding(ShareData.PxToDpi_xhdpi(30));
            end.setTextColor(Color.WHITE);
            end.setGravity(Gravity.CENTER);
            end.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            end.setText(R.string.text_end);
            end.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.weight = 1;
            mDispalyLL.addView(end, ll);

            setSelectMode(mMode);
        }
        if(mCallBack != null){
            mCallBack.setTitle(getResources().getString(R.string.approach_sequenc));
        }
        if(TagMgr.GetTagIntValue(getContext(), Tags.VIDEO_DISPLAY_TIP) == 0){
//        if(true){
            TagMgr.SetTag(getContext(),Tags.VIDEO_DISPLAY_TIP);
            mTipFr = new FrameLayout(getContext());
            mTipFr.setBackgroundColor(0x80000000);
            mTipFr.setOnClickListener(mOnClickListener);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mTipFr,fl);
            {
                int lineH = ShareData.PxToDpi_xhdpi(100);
                TextView tip = new TextView(getContext());
                tip.setText(R.string.text_control_tip);
                tip.setTextColor(0xbfffffff);
                tip.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f);
                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                fl.rightMargin = ShareData.m_screenWidth/3;
                fl.bottomMargin = mBottomH * 3/ 4 + lineH + ShareData.PxToDpi_xhdpi(10);
                mTipFr.addView(tip,fl);

                ImageView line = new ImageView(getContext());
                line.setRotation(180);
                line.setImageResource(R.drawable.video_tutorial_arrow);
                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                fl.gravity = Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM;
                fl.rightMargin = ShareData.m_screenWidth/3;
                fl.bottomMargin = mBottomH * 3/ 4;
                mTipFr.addView(line,fl);
            }
        }
    }

    @Override
    public void SetData(HashMap<String, Object> params)
    {
        if(params != null){
            if(params.containsKey("videos")){
                mVideoInfos = (List<VideoInfo>) params.get("videos");
            }
            if(params.containsKey("videosTime")){
                mVideosTime = (long) params.get("videosTime");
            }
            if(params.containsKey("textTime")){
                mTextTime = (long) params.get("textTime");
            }
            if(params.containsKey("startTime")){
                mOriginalStartTime = (long) params.get("startTime");
            }
            if(params.containsKey("stayTime")){
                mOriginalStayTime = (long) params.get("stayTime");
            }
            if(params.containsKey("type")){
                mMode = (int) params.get("type");
            }
            mCurStartTime = mOriginalStartTime;
            if(mVideosTime < mTextTime){
                mCurEndTime = mVideosTime;
            }else
            {
                mCurEndTime = mOriginalStartTime + mTextTime + mOriginalStayTime;
            }
            setSelectMode(mMode);
        }
    }

    @Override
    public void onClose()
    {
        if(mTimeLineView != null){
            mTimeLineView.clear();
        }
    }

    @Override
    public void onBack()
    {
        if(mTipFr != null){
            removeView(mTipFr);
            mTipFr = null;
        }else
        {
            if (mDispalyLL.getVisibility() == View.VISIBLE)
            {
                if (mCallBack != null)
                {
                    mCallBack.setTitle(getResources().getString(R.string.video_text));
                    mCallBack.onBack();
                }
            } else
            {
                closeTimeView();
            }
        }
    }

    private void resetCurTime(int mode)
    {
        if(mVideosTime < mTextTime){
            mCurStartTime = 0;
            mCurEndTime = mVideosTime;
        }else
        {
            switch (mode)
            {
                case VideoTextPage.TYPE_START:
                    mCurStartTime = 0;
                    mCurEndTime = mTextTime + 0;
                    break;
                case VideoTextPage.TYPE_ALL:
                    mCurStartTime = 0;
                    mCurEndTime = mVideosTime;
                    break;
                case VideoTextPage.TYPE_END:
                default:
                    mCurStartTime = mVideosTime - mTextTime;
                    mCurEndTime = mVideosTime;
                    break;
            }
        }
        if(mTimeLineListener != null){
            mTimeLineListener.onWaterMarkTimeChange(mCurStartTime,mCurEndTime);
        }
    }


    private void showTimeView(int mode)
    {
        if(mCallBack != null){
            mCallBack.setTitle(getResources().getString(R.string.play_control));
        }
        if(mBottomFr == null){
            mBottomFr = new FrameLayout(getContext());
            mBottomFr.setClickable(true);
            LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomH,Gravity.BOTTOM);
            addView(mBottomFr, fl);
            {
                TextView textView = new TextView(getContext());
                textView.setTextColor(0xff666666);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10);
                textView.setText(R.string.text_edit_tip);
                textView.setGravity(Gravity.CENTER);
                textView.setMinHeight(ShareData.PxToDpi_xhdpi(91));
                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL);
                mBottomFr.addView(textView,fl);

                mTimeLineView = new WatermarkTimeLineView(getContext());
//                mTimeLineView.
                mTimeLineView.setVideoInfoList(mVideoInfos);
                fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.BOTTOM);
                fl.bottomMargin = ShareData.PxToDpi_xhdpi(42);
//                fl.leftMargin = ShareData.PxToDpi_xhdpi(120);
//                fl.rightMargin = ShareData.PxToDpi_xhdpi(120);
                mBottomFr.addView(mTimeLineView,fl);
                mTimeLineView.setWatermarkTimeLineListener(mTimeLineListener);

//                mBack = new ImageView(getContext());
//                mBack.setImageResource(R.drawable.video_text_back);
//                mBack.setOnClickListener(mOnClickListener);
//                fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,Gravity.BOTTOM);
//                fl.bottomMargin = ShareData.PxToDpi_xhdpi(88);
//                fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
//                mBottomFr.addView(mBack, fl);
            }
        }
        long start = 0;
        long end = mVideosTime;
        WatermarkTimeLineView.TimeLineMode timeLineMode = null;
        switch (mode)
        {
            case VideoTextPage.TYPE_START:
                timeLineMode = WatermarkTimeLineView.TimeLineMode.BEGINNING;
                break;
            case VideoTextPage.TYPE_ALL:
                timeLineMode = WatermarkTimeLineView.TimeLineMode.WHOLE;
                break;
            case VideoTextPage.TYPE_END:
            default:
                timeLineMode = WatermarkTimeLineView.TimeLineMode.END;
                break;
        }
        start = mCurStartTime;
        end = mCurEndTime;
//        if(mOriginalDisplayType == mode){
//            start = mOriginalStartTime;
//            end = mTextTime+mOriginalStayTime;
//        }
        mTimeLineView.setTime(start,end,mTextTime,timeLineMode);
        mDispalyLL.setVisibility(View.GONE);
        mBottomFr.setVisibility(View.VISIBLE);

    }

    private void closeTimeView()
    {
        if(mCallBack != null){
            mCallBack.setTitle(getResources().getString(R.string.approach_sequenc));
        }
        mDispalyLL.setVisibility(View.VISIBLE);
        mBottomFr.setVisibility(View.GONE);

    }

    private void setSelectMode(int mode)
    {
        switch (mode)
        {
            case VideoTextPage.TYPE_START:
                start.setSelected(true);
                whole.setSelected(false);
                end.setSelected(false);
                break;
            case VideoTextPage.TYPE_ALL:
                start.setSelected(false);
                whole.setSelected(true);
                end.setSelected(false);
                break;
            case VideoTextPage.TYPE_END:
            default:
                start.setSelected(false);
                whole.setSelected(false);
                end.setSelected(true);
                break;
        }
        if(mMode != mode){
            if(mCallBack != null){
                mCallBack.onShowType(mode);
            }
        }
        this.mMode = mode;
    }

    private View.OnClickListener mOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == VideoTextDisplayView.this || v == mTipFr){
                onBack();
            }else if(v == start)
            {
                if(mMode == VideoTextPage.TYPE_START)
                {
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_进入细调进场);
                    showTimeView(VideoTextPage.TYPE_START);
                }else{
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_进场);
                    resetCurTime(VideoTextPage.TYPE_START);
                }
                setSelectMode(VideoTextPage.TYPE_START);
            } else if (v == whole)
            {
                if(mMode == VideoTextPage.TYPE_ALL)
                {
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_进入细调全场);
                    showTimeView(VideoTextPage.TYPE_ALL);
                }else
                {
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_全场);
                    resetCurTime(VideoTextPage.TYPE_ALL);
                }
                setSelectMode(VideoTextPage.TYPE_ALL);
            } else if (v == end)
            {
                if(mMode == VideoTextPage.TYPE_END)
                {
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_进入细调出场);
                    showTimeView(VideoTextPage.TYPE_END);
                }else{
                    MyBeautyStat.onClickByRes(R.string.选中视频水印_出场);
                    resetCurTime(VideoTextPage.TYPE_END);
                }
                setSelectMode(VideoTextPage.TYPE_END);

            }else if(v == mBack){
                onBack();
            }
        }
    };

    private WatermarkTimeLineView.WaterTimeLineListener mTimeLineListener = new WatermarkTimeLineView.WaterTimeLineListener() {
        @Override
        public void onWaterMarkTimeChange(long startTime, long endTime) {
            mCurStartTime = startTime;
            mCurEndTime = endTime;
            long stayTime = 0;
            if(endTime - startTime >= mTextTime){
                stayTime = endTime - startTime - mTextTime;
            }
            if (mCallBack != null) {
                mCallBack.onTimeChang(startTime,stayTime);
            }
        }
        @Override
        public void onBack()
        {
            VideoTextDisplayView.this.onBack();
        }
    };



    private DisPlayCallBack mCallBack;

    public void setDisPlayCallBack(DisPlayCallBack callBack)
    {
        this.mCallBack = callBack;
    }

    public interface DisPlayCallBack
    {
        void onShowType(int type);

        void onTimeChang(long startTime, long stayTime);

        void onBack();

        void setTitle(String title);
    }
}
