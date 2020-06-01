package cn.poco.video.videotext;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.LockRes;
import cn.poco.resource.MusicRes;
import cn.poco.resource.ResType;
import cn.poco.resource.VideoTextRes;
import cn.poco.share.SendWXAPI;
import cn.poco.share.SharePage;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.site.TextUnLockPageSite;


/**
 * Created by admin on 2017/6/6.
 */
@Deprecated
public class DownloadResPage extends IPage implements View.OnClickListener {
    private static final String TAG = "DownloadMusicPage";

    private int MODE_MUSIC = 0;
    private int MODE_VIDEO_TEXT = 1;
    private int mode = MODE_MUSIC;

    private static final int STATE_PREPARE = 0;   //准备状态
    private static final int STATE_DOWNLOAD = 1;  //下载中
    private static final int STATE_FINISH = 2;   //下载完成
    private static final int STATE_ERROR = 3;   //下载失败
    private static final int STATE_LOCK = 4;   //解锁状态
    private static final int STATE_LOCK_SUCESS = 6;   //解锁成功
    private static final int STATE_LOCK_ERROR = 7;   //解锁状态
    private int mState = STATE_LOCK;
    Bitmap bitmap;
    ImageView topView;
    //   private DownloadMusicSite mSite;
    private Context mContext;
    private RelativeLayout mLayout;
    private RelativeLayout.LayoutParams mParams;
    private DownLoadProgressView mView;
    private ImageView backView;
    private TextView downloadText;
    private int mLayoutBackgroundColor = Color.parseColor("#3A3A3A");
    private int nameTextColor = Color.parseColor("#ffffff");
    private int singerTextColor = Color.parseColor("#aaaaaa");
    private BaseRes m_res;
    private View bk;
    private ImageView headView;
    private TextView nameText;
    private TextView singerText;
    private ImageView playView;

    private TextUnLockPageSite mSite;

    private boolean mUiEnabled = true;
    private int m_lockType = LockRes.SHARE_TYPE_NONE;
    private Object m_shareImge;
    private String m_shareLink;
    private String m_shareContent;
    public DownloadResPage(Context context, BaseSite site) {
        super(context, site);
        mSite = (TextUnLockPageSite) site;
        this.mContext = getContext();
    }

    private int resUri;

    @Override
    public void SetData(HashMap<String, Object> params) {
        if (params != null) {
            String path = (String) params.get("img");
            if (!TextUtils.isEmpty(path)) {
                setBackground(new BitmapDrawable(BitmapFactory.decodeFile(path)));
            }
            BaseRes res = (BaseRes) params.get("res");
            resUri = res.m_id;
            if (res instanceof VideoTextRes) {
                mode = MODE_VIDEO_TEXT;
                m_lockType = ((VideoTextRes) res).lockType;
                m_shareImge = ((VideoTextRes) res).url_shareImg;
                m_shareContent = ((VideoTextRes) res).shareTitle;
                m_shareLink = ((VideoTextRes) res).m_shareLink;
            } else if (res instanceof MusicRes) {
                mode = MODE_MUSIC;
                m_lockType = ((MusicRes) res).lockType;
                m_shareImge = ((MusicRes) res).url_shareImg;
                m_shareContent = ((MusicRes) res).shareTitle;
                m_shareLink = ((MusicRes) res).m_shareLink;
            }
            this.m_res = res;
        }
        initView();
        setViewMode();
        setState();
    }


    private void initView() {
        setBackgroundColor(0xbf000000);

        mParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLayout = new RelativeLayout(mContext);
        this.addView(mLayout, mParams);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(500));
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        relativeLayout.setBackgroundColor(mLayoutBackgroundColor);
        relativeLayout.setId(R.id.dwonloadRootView);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.setMargins(0, ShareData.PxToDpi_xhdpi(150), 0, 0);
        mLayout.addView(relativeLayout, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(200));
        topView = new ImageView(mContext);
        topView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        // topView.setBackgroundColor(Color.GREEN);
        relativeLayout.addView(topView, layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(160), ShareData.PxToDpi_xhdpi(160));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ImageView centerView = new ImageView(mContext);
        centerView.setId(R.id.centerView);
        layoutParams.setMargins(0, ShareData.PxToDpi_xhdpi(120), 0, 0);
        centerView.setBackgroundColor(Color.WHITE);
        relativeLayout.addView(centerView, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(140), ShareData.PxToDpi_xhdpi(140));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        headView = new ImageView(mContext);
        layoutParams.setMargins(0, ShareData.PxToDpi_xhdpi(130), 0, 0);
        headView.setBackgroundColor(Color.RED);
        relativeLayout.addView(headView, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        playView = new ImageView(mContext);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.centerView);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.topMargin = ShareData.PxToDpi_xhdpi(-30);
        Log.v("margin", " " + ShareData.PxToDpi_xhdpi(50));
        playView.setId(R.id.playView);
        playView.setImageResource(R.drawable.video_music_play);
        relativeLayout.addView(playView, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nameText = new TextView(mContext);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.playView);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        nameText.setPadding(0, ShareData.PxToDpi_xhdpi(60), 0, 0);
        nameText.setTextSize(14);
        nameText.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        nameText.setTextColor(nameTextColor);
        relativeLayout.addView(nameText, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        singerText = new TextView(mContext);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.playView);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        singerText.setPadding(0, ShareData.PxToDpi_xhdpi(110), 0, 0);
        singerText.setTextSize(12);
        singerText.setTextColor(singerTextColor);
        relativeLayout.addView(singerText, layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(80));
        bk = new View(mContext);
        bk.setBackgroundColor(Color.parseColor("#ffc433"));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.dwonloadRootView);
//        bk.setOnClickListener(this);
        mLayout.addView(bk, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(80));
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.dwonloadRootView);
        mView = new DownLoadProgressView(mContext);
        mView.setMaxProgress(100);
        mView.setOnClickListener(this);
        mLayout.addView(mView, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        downloadText = new TextView(mContext);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.dwonloadRootView);
        downloadText.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, 0);
        downloadText.setTextColor(Color.parseColor("#ffffff"));
        downloadText.setTextSize(12);
        downloadText.setText(R.string.download);
        downloadText.getPaint().setFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        mLayout.addView(downloadText, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        backView = new ImageView(mContext);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.setMargins(0, 0, 0, ShareData.PxToDpi_xhdpi(150));
        backView.setImageResource(R.drawable.video_music_recycle);
        backView.setOnClickListener(this);
        mLayout.addView(backView, layoutParams);
    }

    public void setViewMode() {
        if (mode == MODE_MUSIC) {
            MusicRes res = (MusicRes) m_res;
            Glide.with(mContext).load(res.m_thumb).into(headView);
            getBitmap(res.m_thumb);
            nameText.setText(res.m_name);
            singerText.setText(res.author);

        } else if (mode == MODE_VIDEO_TEXT) {
            VideoTextRes res = (VideoTextRes) m_res;
            getBitmap(res.m_thumb);
            Glide.with(mContext).load(res.m_thumb).into(headView);
            nameText.setText(res.m_name);
            singerText.setText(res.author);
            playView.setVisibility(View.GONE);
        }
    }

    private void getBitmap(Object url) {

        Glide.with(mContext).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                bitmap = BeautifyResMgr.MakeBkBmp(resource, ShareData.PxToDpi_xhdpi(500), ShareData.PxToDpi_xhdpi(200));
                topView.setImageBitmap(bitmap);
            }
        });

    }


    public void setState() {
        int mode = DownloadMgr.getInstance().GetStateById(m_res.m_id, m_res.getClass());
        switch (mode) {
            case 0:
//                mState = STATE_PREPARE;
                switch (m_lockType) {
                    case LockRes.SHARE_TYPE_MARKET:
                        downloadText.setText("市场评分解锁");
//                            m_view.SetWeixinTip(R.string.unlock_share_to_market);
//                            m_view.SetBtnState(RecomDisplayUIV2.BTN_STATE_UNLOCK);
                        break;
                    case LockRes.SHARE_TYPE_WEIXIN:
                        downloadText.setText("转发朋友圈解锁");
//                            m_view.SetWeixinTip(R.string.unlock_share_to_weixin);
//                            m_view.SetBtnState(RecomDisplayUIV2.BTN_STATE_UNLOCK);
                        break;
                    default:
//                            m_view.SetWeixinTip(R.string.unlock_share_to_weixin);
//                            m_view.SetBtnState(RecomDisplayUIV2.BTN_STATE_DOWNLOAD);
                        break;
                }
                break;
            case 1:
            case 2:
                startDownLoad();
                break;

        }
    }

    @Override
    public void onClick(View v) {
        if (mUiEnabled) {
            if (v == mView) {
                if (mState == STATE_LOCK) {
                    onUnLock();
                } else if (mState == STATE_PREPARE || mState == STATE_ERROR) {
                    startDownLoad();
                } else if (mState == STATE_FINISH) {
                }
            } else if (v == backView) {
                mUiEnabled = false;
                mSite.onBack(getContext());
            }
        }
    }

    public void onUnLock() {
        switch (m_lockType) {
            case LockRes.SHARE_TYPE_MARKET: {
                OpenMarket(getContext(), ResType.MUSIC.GetValue(), m_res.m_id);
                unlockSuccess();
                break;
            }

            case LockRes.SHARE_TYPE_WEIXIN: {
                String url = null;
                if (m_shareLink!= null && m_shareLink.length() > 0) {
                    url = m_shareLink;
                }
                SharePage.unlockResourceByWeiXin(getContext(), m_res.m_name, m_shareContent, url, MakeWXLogo(m_shareImge), false, m_wxcb);
                break;
            }
        }
    }

    private SendWXAPI.WXCallListener m_wxcb = new SendWXAPI.WXCallListener() {
        @Override
        public void onCallFinish(int result) {
            if (result != BaseResp.ErrCode.ERR_USER_CANCEL) {
                unlockSuccess();
//                     if (mre == ResType.MUSIC.GetValue() || m_type == ResType.AUDIO_TEXT.GetValue()) {
//                         ClearThemeLockFlag(m_themeID);
//                     }
//                     UnlockSuccess();
            }
        }
    };
//     if (result != BaseResp.ErrCode.ERR_USER_CANCEL) {
//        if (m_type == ResType.MUSIC.GetValue() || m_type == ResType.AUDIO_TEXT.GetValue() ) {
//            ClearThemeLockFlag(m_themeID);
//        }
//        UnlockSuccess();
//    }

    public void unlockSuccess() {
        if (m_res instanceof VideoTextRes) {
            ((VideoTextRes) m_res).lockType = LockRes.SHARE_TYPE_NONE;
        } else if (m_res instanceof MusicRes) {
            ((VideoTextRes) m_res).lockType = LockRes.SHARE_TYPE_NONE;
        }
        mState = STATE_DOWNLOAD;
        startDownLoad();
    }

    public static void OpenMarket(Context context, int resType, int themeID) {
        try {
            Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Throwable e) {
            Toast.makeText(context, "还没有安装安卓市场，请先安装", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        if (resType == ResType.AUDIO_TEXT.GetValue() || resType == ResType.MUSIC.GetValue()) {
            ClearThemeLockFlag(themeID);
        }
    }

    /**
     * 清理主题锁
     *
     * @param themeID
     */
    public static void ClearThemeLockFlag(int themeID) {
        TagMgr.SetTag(MyFramework2App.getInstance().getApplicationContext(), Tags.THEME_UNLOCK + themeID);
    }

    public static Bitmap MakeWXLogo(Object res) {
        Bitmap out = null;

        if (res != null) {
            out = Utils.DecodeImage(MyFramework2App.getInstance().getApplicationContext(), res, 0, -1, -1, -1);
        }
        if (out == null) {
            out = BitmapFactory.decodeResource(MyFramework2App.getInstance().getApplicationContext().getResources(), R.mipmap.ic_launcher);
        }
        if (out != null) {
            if (out.getWidth() > 180 || out.getHeight() > 180) {
                out = MakeBmp.CreateBitmap(out, 180, 180, -1, 0, Bitmap.Config.ARGB_8888);
            }
        }
        return out;
    }


    private void startDownLoad() {
        mState = STATE_DOWNLOAD;
        downloadText.setText(R.string.downloading);
        mState = STATE_DOWNLOAD;
        mView.setProgress(0);
        DownloadMgr.getInstance().DownloadRes(new IDownload[]{m_res}, false, downCallback2);
    }

    private AbsDownloadMgr.Callback2 downCallback2 = new AbsDownloadMgr.Callback2() {
        @Override
        public void OnGroupComplete(int downloadId, IDownload[] resArr) {
//                Log.i(TAG, "OnGroupComplete1: "+mUiEnabled);
            if (mUiEnabled) {
//                Log.i(TAG, "OnGroupComplete2: "+mUiEnabled);
                mUiEnabled = false;
                mState = STATE_FINISH;
                downloadText.setText(R.string.Done);
                mView.setProgress(100);
                DownloadResPage.this.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("resUri", resUri);
                        mSite.onFinish(getContext(), data);
                    }
                }, 100);
            }
        }

        @Override
        public void OnGroupFail(int downloadId, IDownload[] resArr) {
//                Log.i(TAG, "OnGroupFail: ");
            if (mUiEnabled) {
                mState = STATE_ERROR;
                downloadText.setText(R.string.downloadFailed);
            }
        }

        @Override
        public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress) {
//                Log.i(TAG, "OnGroupProgress: ");

        }

        @Override
        public void OnProgress(int downloadId, IDownload res, int progress) {
//                Log.i(TAG, "OnProgress: "+progress);
            if (mUiEnabled) {
                mState = STATE_DOWNLOAD;
                mView.setProgress(progress);
            }
        }

        @Override
        public void OnComplete(int downloadId, IDownload res) {
//                Log.i(TAG, "OnComplete: "+m_res.m_id+" :"+downloadId);

        }

        @Override
        public void OnFail(int downloadId, IDownload res) {
//                Log.i(TAG, "OnFail: ");
        }
    };

    @Override
    public void onBack() {
        backView.performClick();
    }

    @Override
    public void onClose() {
        super.onClose();
        downCallback2 = null;
    }

    //进度条
    public class DownLoadProgressView extends View {
        private int PROGRESS_PASSED_COLOR = Color.parseColor("#816C37");
        private Paint mPaint;
        private float mPercent = 0;
        private int MAX_PERCENT = 0;

        public DownLoadProgressView(Context context) {
            super(context);
            initData();
        }

        public DownLoadProgressView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public DownLoadProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        private void initData() {
            mPaint = new Paint();
            mPaint.setColor(PROGRESS_PASSED_COLOR);
            mPaint.setAlpha(135);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setDither(true);  //防抖动
            mPaint.setAntiAlias(true); //放锯齿
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int startX = (int) (mPercent * 1.0f / MAX_PERCENT * getWidth());
            canvas.drawRect(startX, 0, getWidth(), getHeight(), mPaint);
        }

        public void setProgress(int progress) {
            if (progress < 0) {
                progress = 0;
            } else if (progress > MAX_PERCENT) {
                progress = MAX_PERCENT;
            }
            this.mPercent = progress;
            invalidate();
        }

        public void setMaxProgress(int maxProgress) {
            this.MAX_PERCENT = maxProgress;
        }

    }


//    //进度条
//    public class DownLoadProgressView extends View {
//        private int PROGRESS_TEXT_SIZE = 56;
//        private int PROGRESS_BACKGROUND_COLOR = Color.parseColor("#ffc433");
//        private int PROGRESS_PASSED_COLOR = Color.parseColor("#816C37");
//        private Paint mPaint;
//        private float mPercent = 0;
//        private int MAX_PERCENT = 0;
//
//
//        public DownLoadProgressView(Context context) {
//            super(context);
//            initData();
//        }
//
//        public DownLoadProgressView(Context context, @Nullable AttributeSet attrs) {
//            super(context, attrs);
//        }
//
//        public DownLoadProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//            super(context, attrs, defStyleAttr);
//        }
//
//        private void initData() {
//            mPaint = new Paint();
//
//            mPaint.setAlpha(255);
//            mPaint.setStyle(Paint.Style.FILL);
//            mPaint.setDither(true);  //防抖动
//            mPaint.setAntiAlias(true); //放锯齿
//
//
//        }
//
//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//            //获取尺寸
//            int size = MeasureSpec.getSize(heightMeasureSpec);
//            size = (int) (size + PROGRESS_TEXT_SIZE + 10);
//            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//            ;
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//            //背景
//            mPaint.setColor(PROGRESS_BACKGROUND_COLOR);
//            float w = getWidth();
//            float h = getHeight() - PROGRESS_TEXT_SIZE - 10;
//            canvas.drawRect(0, 0, w, h, mPaint);
//
//            //前景
//            mPaint.setColor(PROGRESS_PASSED_COLOR);
//            mPaint.setAlpha(135);
//
//            float t = mPercent - MAX_PERCENT;
//
//            canvas.drawRect(t, 0, w, h, mPaint);
//
//        }
//
//        public void setProgress(int progress) {
//            this.mPercent = progress;
//            Log.v("progress", "" + progress);
//            invalidate();
//        }
//
//        public void setMaxProgress(int maxPregress) {
//            this.MAX_PERCENT = maxPregress;
//        }
//
//    }
}
