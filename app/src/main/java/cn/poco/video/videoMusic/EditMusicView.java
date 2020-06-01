package cn.poco.video.videoMusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.poco.interphoto2.R;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.ShareData;

/**
 * Created by lgd on 2017/8/21.
 */

public class EditMusicView extends FrameLayout {

    protected LinearLayout mTopLl;
    private ImageView mClipBtn;
    private ImageView mBkVolumeBtn;
    private ImageView mMusicVolumeBtn;
    private EditInfo mInfo;

    public static final int MODE_CLIP = 1;
    public static final int MODE_BK_VOLUME = 2;
    public static final int MODE_MUSIC_VOLUME = 3;
    private int mCurMode = MODE_CLIP;

    private ClipMusicView mClipView;
    private SelectVolumeView mBkVolumeView;
    private SelectVolumeView mMusicVolumeView;
    private int btnFrH;
    private int bottomH;
    private int bkColor = 0xff212121;
    private int normalColor = 0xff111111;
    private View mEmptyTopView;
    private View mBottomBk;

    public EditMusicView(@NonNull Context context, EditInfo info) {
        super(context);
        this.mInfo = info;
        mInfo.clipInfo.bkColor = bkColor;
        init();
    }

    private void init() {
        LayoutParams fl;
        LinearLayout.LayoutParams ll;
        btnFrH = ShareData.PxToDpi_xhdpi(70);
        bottomH = ShareData.PxToDpi_xhdpi(184);

        mEmptyTopView = new View(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.bottomMargin = btnFrH + bottomH;
        mEmptyTopView.setOnClickListener(mOnClickListener);
        addView(mEmptyTopView, fl);

        mBottomBk = new View(getContext());
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnFrH + bottomH);
        fl.gravity = Gravity.BOTTOM;
        mBottomBk.setBackgroundColor(bkColor);
        addView(mBottomBk, fl);


        mTopLl = new LinearLayout(getContext());
        mTopLl.setOrientation(LinearLayout.HORIZONTAL);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, btnFrH);
        fl.gravity = Gravity.BOTTOM;
        fl.bottomMargin = bottomH;
        addView(mTopLl, fl);
        {
            mClipBtn = new ImageView(getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    super.setSelected(selected);
                    if (selected) {
                        mClipBtn.setBackgroundColor(bkColor);
                        mClipBtn.setImageResource(R.drawable.video_music_clip_selected);
                    } else {
                        mClipBtn.setBackgroundColor(normalColor);
                        mClipBtn.setImageResource(R.drawable.video_music_clip_default);
                    }
                }
            };
            mClipBtn.setSelected(true);
            mClipBtn.setScaleType(ImageView.ScaleType.CENTER);
            mClipBtn.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ll.weight = 1;
            mTopLl.addView(mClipBtn, ll);

            mMusicVolumeBtn = new ImageView(getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    super.setSelected(selected);
                    if (selected) {
                        mMusicVolumeBtn.setBackgroundColor(bkColor);
                        mMusicVolumeBtn.setImageResource(R.drawable.video_music_volume_selected);
                    } else {
                        mMusicVolumeBtn.setBackgroundColor(normalColor);
                        mMusicVolumeBtn.setImageResource(R.drawable.video_music_volume_default);
                    }
                }
            };
            mMusicVolumeBtn.setSelected(false);
            mMusicVolumeBtn.setScaleType(ImageView.ScaleType.CENTER);
            mMusicVolumeBtn.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ll.weight = 1;
            mTopLl.addView(mMusicVolumeBtn, ll);

            mBkVolumeBtn = new ImageView(getContext()) {
                @Override
                public void setSelected(boolean selected) {
                    super.setSelected(selected);
                    if (selected) {
                        mBkVolumeBtn.setBackgroundColor(bkColor);
                        mBkVolumeBtn.setImageResource(R.drawable.video_music_original_sound_selected);
                    } else {
                        mBkVolumeBtn.setBackgroundColor(normalColor);
                        mBkVolumeBtn.setImageResource(R.drawable.video_music_original_sound_default);
                    }
                }
            };
            mBkVolumeBtn.setSelected(false);
            mBkVolumeBtn.setScaleType(ImageView.ScaleType.CENTER);
            mBkVolumeBtn.setOnClickListener(mOnClickListener);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ll.weight = 1;
            mTopLl.addView(mBkVolumeBtn, ll);

        }

        mClipView = new ClipMusicView(getContext(), mInfo.clipInfo);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomH);
        fl.gravity = Gravity.BOTTOM;
        addView(mClipView, fl);
        mClipView.setOnCallBack(mClipCallBack);
//        mClipView.setOnCallBack(new ClipMusicView.OnCallBack() {
//            @Override
//            public void onStop(int second) {
//
//                TongJi2.AddCountByRes(getContext(), R.integer.音乐_音乐选择_刻度条);
//                if (mOnCallBack != null) {
//                    mOnCallBack.onMusicClip(second);
//                }
//            }
//
//            @Override
//            public void onScroll(int mScrollStartTime) {
//
//            }
//        });


        mMusicVolumeView = new SelectVolumeView(getContext(), bkColor, mInfo.musicVolume);
        mMusicVolumeView.setVisibility(View.INVISIBLE);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomH);
        fl.gravity = Gravity.BOTTOM;
        addView(mMusicVolumeView, fl);
        mMusicVolumeView.setOnCallBack(new SelectVolumeView.OnCallBack() {
            @Override
            public void onStop(float volume) {
                TongJi2.AddCountByRes(getContext(), R.integer.音乐_音乐音量_刻度条);
                if (mOnCallBack != null) {
                    mOnCallBack.onMusicVolume(volume);
                }
            }

            @Override
            public void onScroll(float volume) {

            }

            @Override
            public void onClick(float volume) {
                if (mOnCallBack != null) {
                    mOnCallBack.onMusicVolume(volume);
                }
            }
        });

        mBkVolumeView = new SelectVolumeView(getContext(), bkColor, mInfo.bkVoiceVolume);
        mBkVolumeView.setVisibility(View.INVISIBLE);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomH);
        fl.gravity = Gravity.BOTTOM;
        addView(mBkVolumeView, fl);
        mBkVolumeView.setOnCallBack(new SelectVolumeView.OnCallBack() {
            @Override
            public void onStop(float volume) {
                TongJi2.AddCountByRes(getContext(), R.integer.音乐_视频音量_刻度条);
                if (mOnCallBack != null) {
                    mOnCallBack.onBkVolume(volume);
                }
            }

            @Override
            public void onScroll(float volume) {

            }

            @Override
            public void onClick(float volume) {
                if (mOnCallBack != null) {
                    mOnCallBack.onBkVolume(volume);
                }
            }
        });

        setViewMode(MODE_CLIP);
    }

    private ClipMusicView.OnCallBack mClipCallBack = new ClipMusicView.OnCallBack() {
        @Override
        public void onStop(int second) {
            TongJi2.AddCountByRes(getContext(), R.integer.音乐_音乐选择_刻度条);
            if (mOnCallBack != null) {
                mOnCallBack.onMusicClip(second);
            }
        }

        @Override
        public void onScroll(int mScrollStartTime) {

        }
    };

    private ViewGroup.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mEmptyTopView) {
                if (mOnCallBack != null) {
                    mOnCallBack.onBack();
                }
            } else if (v == mClipBtn) {
                TongJi2.AddCountByRes(getContext(), R.integer.音乐_进度控制按钮);
                MyBeautyStat.onClickByRes(R.string.音乐调整_点击音乐片段);
                setViewMode(MODE_CLIP);
            } else if (v == mBkVolumeBtn) {
                TongJi2.AddCountByRes(getContext(), R.integer.音乐_视频音量);
                MyBeautyStat.onClickByRes(R.string.音乐调整_点击视频音量);
                setViewMode(MODE_BK_VOLUME);
            } else if (v == mMusicVolumeBtn) {
                TongJi2.AddCountByRes(getContext(), R.integer.音乐_音乐音量);
                MyBeautyStat.onClickByRes(R.string.音乐调整_点击音乐音量);
                setViewMode(MODE_MUSIC_VOLUME);
            }
        }
    };

    public static class EditInfo {
        public ClipMusicView.FrequencyInfo clipInfo;
        public float bkVoiceVolume;
        public float musicVolume;
    }

    public void setViewMode(int mode) {
        mCurMode = mode;
        if (mOnCallBack != null) {
            mOnCallBack.onMode(mCurMode);
        }
        switch (mode) {
            case MODE_CLIP:
                mClipBtn.setSelected(true);
                mBkVolumeBtn.setSelected(false);
                mMusicVolumeBtn.setSelected(false);
                mClipView.setVisibility(View.VISIBLE);
                mBkVolumeView.setVisibility(View.INVISIBLE);
                mMusicVolumeView.setVisibility(View.INVISIBLE);
                break;
            case MODE_BK_VOLUME:
                mClipBtn.setSelected(false);
                mBkVolumeBtn.setSelected(true);
                mMusicVolumeBtn.setSelected(false);
                mClipView.setVisibility(View.INVISIBLE);
                mBkVolumeView.setVisibility(View.VISIBLE);
                mBkVolumeView.updateVolume();
                mMusicVolumeView.setVisibility(View.INVISIBLE);
                break;
            case MODE_MUSIC_VOLUME:
                mClipBtn.setSelected(false);
                mBkVolumeBtn.setSelected(false);
                mMusicVolumeBtn.setSelected(true);
                mClipView.setVisibility(View.INVISIBLE);
                mBkVolumeView.setVisibility(View.INVISIBLE);
                mMusicVolumeView.setVisibility(View.VISIBLE);
                mMusicVolumeView.updateVolume();
                break;
        }
    }

    private OnCallBack mOnCallBack;

    public void setOnCallBack(OnCallBack onCallBack) {
        this.mOnCallBack = onCallBack;
        if (mOnCallBack != null) {
            mOnCallBack.onMode(mCurMode);
        }
    }

    public interface OnCallBack {
        void onMusicClip(int startTime);

        void onBkVolume(float volume);

        void onMusicVolume(float volume);

        void onBack();

        void onMode(int mode);  // 1 音乐选择，2 视频音量， 3音乐音量
    }

    public void refreshInfo(EditInfo info) {
        info.clipInfo.bkColor = bkColor;
        if (!info.clipInfo.musicPath.equals(mInfo.clipInfo.musicPath)) {
            LayoutParams fl;
            removeView(mClipView);
            mClipView = new ClipMusicView(getContext(), info.clipInfo);
            fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, bottomH);
            fl.gravity = Gravity.BOTTOM;
            addView(mClipView, fl);
            mClipView.setOnCallBack(mClipCallBack);
            setViewMode(MODE_CLIP);
        }
        mInfo = info;
        mBkVolumeView.setVolume(info.bkVoiceVolume);
        mMusicVolumeView.setVolume(info.musicVolume);

        if (mOnCallBack != null) {
            mOnCallBack.onMode(mCurMode);
        }
    }
}
