package cn.poco.video.page;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

import cn.poco.album2.utils.T;
import cn.poco.blogcore.InstagramBlog;
import cn.poco.blogcore.SinaBlog;
import cn.poco.blogcore.Tools;
import cn.poco.blogcore.WeiXinBlog;
import cn.poco.capture2.site.CapturePageSite2;
import cn.poco.framework.MyFramework;
import cn.poco.home.site.HomePageSite;
import cn.poco.interphoto2.R;
import cn.poco.share.ShareTools;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.InterphotoDlg;
import cn.poco.video.save.SaveParams;
import cn.poco.video.site.VideoAlbumSite4;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by admin on 2018/1/15.
 */

public class ShareVideoView extends FrameLayout {

    private static final String TAG = "ShareVideoView";
    private static final int CIRCLE = 1;
    private static final int MOMENTS = 2;
    private static final int WECHAT = 3;
    private static final int INSTAGRAM = 4;
    private static final int SINA = 5;
    private static final int NONE = 0;
    private static final long WECHAT_FILE_MAX_SIZE = 1024 * 1024 * 10;
    private LinearLayout itemLayout;
    private int itemWidth = ShareData.PxToDpi_xhdpi(140);
    private int itemHeigth = ShareData.PxToDpi_xhdpi(127);
    private Button button;
    private Boolean isOpenCamera = false;
    private Context mContext;
    private String videoPath;
    private SaveParams mSaveParams;
    private Boolean supportShareToCircle;
    private MyBeautyStat.BlogType blogType = null;
    private IsShareCircleCallBack isShareCircleCallBack;
    private boolean isShareCircle = false;
    private int[] imgs = {
            R.drawable.beauty_share_circle,
            R.drawable.beauty_share_friend,
            R.drawable.beauty_share_weixin,
            R.drawable.beauty_share_sina,
            R.drawable.beauty_share_ins

    };
    private String[] texts = {
            getResources().getString(R.string.save_video_share_zaiyiqi),
            getResources().getString(R.string.save_video_share_pengyouquan),
            getResources().getString(R.string.save_video_share_weixinhaoyou),
            getResources().getString(R.string.save_video_share_sina),
            getResources().getString(R.string.save_video_share_instagram)


    };

    /**
     * 第三方分享
     *
     * @param outputPath 视频路径
     */
    private int mShareMode = NONE;
    private ShareTools mShareTools = new ShareTools(getContext());
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == button) {
                if (isOpenCamera) {
                    MyBeautyStat.onClickByRes(R.string.保存与分享_返回录像);
                    MyFramework.SITE_BackAndOpen(getContext(), HomePageSite.class, CapturePageSite2.class, null, AUTOFILL_TYPE_NONE);

                } else {
                    MyBeautyStat.onClickByRes(R.string.保存与分享_返回视频相册);
                    MyFramework.SITE_BackAndOpen(getContext(), HomePageSite.class, VideoAlbumSite4.class, null, AUTOFILL_TYPE_NONE);
                }
            } else {
                int id = (int) v.getTag();
                switch (id) {
                    case R.drawable.beauty_share_circle:
                        mShareMode = CIRCLE;
                        isShareCircle = true;
                        onShare(videoPath);
                        break;
                    case R.drawable.beauty_share_friend:
                        mShareMode = MOMENTS;
                        isShareCircle = false;
                        onShare(videoPath);
                        break;
                    case R.drawable.beauty_share_weixin:
                        mShareMode = WECHAT;
                        isShareCircle = false;
                        onShare(videoPath);
                        break;
                    case R.drawable.beauty_share_ins:
                        isShareCircle = false;
                        mShareMode = INSTAGRAM;
                        onShare(videoPath);
                        break;
                    case R.drawable.beauty_share_sina:
                        isShareCircle = false;
                        mShareMode = SINA;
                        onShare(videoPath);
                        break;
                }
                if (isShareCircleCallBack != null){
                    isShareCircleCallBack.isShareCircle(isShareCircle);
                }


            }

        }
    };

    public ShareVideoView(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public void setData(Boolean isFromCamera, String p, SaveParams saveParams,Boolean isSupport) {
        videoPath = p;
        mSaveParams = saveParams;
        supportShareToCircle = isSupport;
        if (isFromCamera) {
            isOpenCamera = isFromCamera;
        } else {
            isOpenCamera = isFromCamera;
        }
    }

    private void initView() {

        setBackgroundColor(0x66000000);
        itemLayout = addLineItems(imgs, texts);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = ShareData.PxToDpi_xhdpi(51);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        this.addView(itemLayout, lp);

        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        View view = new View(getContext());
        view.setBackgroundColor(0x0dffffff);
        lp.topMargin = ShareData.PxToDpi_xhdpi(228);
        this.addView(view, lp);

        lp = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(260), ShareData.PxToDpi_xhdpi(80));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        button = new Button(getContext());
        button.setText(getResources().getString(R.string.share_video_finish));
        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        button.setBackgroundColor(0xffffc433);
        button.setOnClickListener(onClickListener);
        lp.topMargin = ShareData.PxToDpi_xhdpi(297);
        this.addView(button, lp);

    }

    private LinearLayout addLineItems(int[] imgs, String[] texts) {
        LinearLayout parent = new LinearLayout(getContext());
        parent.setOrientation(LinearLayout.HORIZONTAL);
        int size = Math.min(imgs.length, texts.length);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(itemWidth, itemHeigth);
        ll.gravity = Gravity.CENTER;
        for (int i = 0; i < size; i++) {
            Drawable drawable = getResources().getDrawable(imgs[i]);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            TextView textView = new TextView(getContext());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f);
            textView.setGravity(Gravity.CENTER);
            textView.setText(texts[i]);
            textView.setCompoundDrawablePadding(16);
            textView.setCompoundDrawables(null, drawable, null, null);
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setOnClickListener(onClickListener);
            textView.setTag(imgs[i]);
            textView.setLayoutParams(ll);
            parent.addView(textView);
        }
        return parent;
    }

    private int getCircleVersionCode() {
        try {
            PackageInfo e = this.mContext.getPackageManager().getPackageInfo(ShareTools.CIRCLE_PACKAGE_NAME, 0);
            return e.versionCode;
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    private void onShare(String outputPath) {

        MyBeautyStat.onClickByRes(R.string.视频美化_保存与分享_分享);
        switch (mShareMode) {
            case CIRCLE: {
                if (!Tools.checkApkExist(mContext, ShareTools.CIRCLE_PACKAGE_NAME)) {
                    showDialog(R.string.tip, R.string.installCircleTips);
                } else if (getCircleVersionCode() < 30) {
                    showDialog(R.string.tip, R.string.circle_unsupport_video);
                } else {
                    if (supportShareToCircle){
                        blogType = MyBeautyStat.BlogType.在一起;
                        mShareTools.sendToCircle(outputPath, "", false, new ShareTools.SendCompletedListener() {
                            @Override
                            public void result(int result) {
                                showShareResult(result);
                            }
                        });
                        shareTongji(blogType);
                    }else {
                        showDialog(R.string.tip, R.string.circle_share_tip);
                    }

                }
                break;
            }
            case MOMENTS: {
                if (!Tools.checkApkExist(mContext, WeiXinBlog.WX_PACKAGE_NAME)) {
                    showDialog(R.string.tip, R.string.share_to_wechat_client_tip);
                } else {
                    blogType = MyBeautyStat.BlogType.朋友圈;

                    shareTongji(blogType);
                    final InterphotoDlg dialog = new InterphotoDlg((Activity) mContext, R.style.waitDialog);
                    dialog.SetTitle(R.string.open_wechat_tip, true);
                    dialog.SetBtnType(InterphotoDlg.POSITIVE);
                    dialog.SetPositiveBtnText(R.string.open_wechat_ok);
                    dialog.setOnOutsideClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setOnDlgClickCallback(new InterphotoDlg.OnDlgClickCallback() {
                        @Override
                        public void onOK() {
                            mShareTools.openWeiXinWithIntent();
                            dialog.dismiss();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                    dialog.setCancelable(true);
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    });
                    dialog.show();
                }

                break;
            }
            case WECHAT: {

                if (!Tools.checkApkExist(mContext, WeiXinBlog.WX_PACKAGE_NAME)) {
                    showDialog(R.string.tip, R.string.share_to_wechat_client_tip);
                } else {
                    blogType = MyBeautyStat.BlogType.微信好友;
                    long length = new File(outputPath).length();
                    if (length < WECHAT_FILE_MAX_SIZE) {
                        Bitmap bitmap = VideoUtils.decodeFrameByTime(outputPath, 0);
                        String title = mContext.getResources().getString(R.string.share_video_weichat_title);
                        mShareTools.sendFileToWeiXin(outputPath, title, bitmap, new ShareTools.SendCompletedListener() {
                            @Override
                            public void result(int result) {
                                showShareResult(result);
                            }
                        });
                        shareTongji(blogType);
                    } else {
                        mShareTools.openWeiXinWithIntent();
                    }
                    shareTongji(blogType);
                }
                break;
            }
            case INSTAGRAM: {
                if (!Tools.checkApkExist(mContext, InstagramBlog.INSTAGRAM_PACKAGE_NAME)) {
                    showDialog(R.string.tip, R.string.installInstagramTips);
                } else {
                    blogType = MyBeautyStat.BlogType.instagram;
                    mShareTools.openInstagramWithIntent();
                    shareTongji(blogType);
                }
                break;
            }
            case SINA: {
                if (!Tools.checkApkExist(mContext, SinaBlog.SINA_PACKAGE_NAME)) {
                    showDialog(R.string.tip, R.string.installSinaTips);
                } else {
                    blogType = MyBeautyStat.BlogType.微博;
                    mShareTools.sendVideoToSina(getResources().getString(R.string.share_video_weichat_title), videoPath, new ShareTools.SendCompletedListener() {
                        @Override
                        public void result(int result) {
                            showShareResult(result);

                        }
                    });
                    shareTongji(blogType);
                }
            }
        }

        mShareMode = NONE;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mShareTools!= null){
            mShareTools.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void showDialog(@StringRes int title, @StringRes int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, null);
        builder.create().show();
    }
    private void shareTongji(MyBeautyStat.BlogType blogType) {
        if (mSaveParams != null) {
            String filterId = "0000";
//            if (mSaveParams.filterItem != null) {
//                filterId = mSaveParams.filterId;
//            }
            String musicId = "0000";
            if (mSaveParams.musicPath != null) {
                musicId = mSaveParams.musicId;
            }
            String textId = "0000";
            if (mSaveParams.videoText != null) {
                textId = mSaveParams.textId;
            }
            MyBeautyStat.onShareCompleteByRes(blogType, R.string.视频_保存与分享, filterId, musicId, textId);
        }
    }

    /**
     * 显示分享结果Toast
     *
     * @param result 分享返回结果
     */
    private void showShareResult(int result) {
        if (result == ShareTools.SUCCESS) {
            T.showShort(mContext, R.string.share_success);
        } else if (result == ShareTools.CANCEL) {
            T.showShort(mContext, R.string.cancelshare);
        } else if (result == ShareTools.FAIL) {
            T.showShort(mContext, R.string.shareFailed);
        }
    }
    public void setIsShareCircleCallBack(IsShareCircleCallBack circleCallBack){
        this.isShareCircleCallBack = circleCallBack;

    }

    public  interface IsShareCircleCallBack{
        void isShareCircle(boolean isShareCircle);
    }



}
