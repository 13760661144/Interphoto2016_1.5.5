package cn.poco.video.videoMusic;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.AudioStore;
import cn.poco.video.videoMusic.Mp3Info;
import cn.poco.video.site.SelectMusicSite;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by admin on 2017/6/19.
 */

public class SelectMusicPage extends IPage implements View.OnClickListener {

    private final int TAG = 1;
    List<AudioStore.AudioInfo> mList = new ArrayList<>();
    MusicAdapter adapter;
    private Context mContext;
    private FrameLayout.LayoutParams l;
    private RecyclerView mRecyclerView;
    private SelectMusicSite mSite;
    //头部
    private FrameLayout m_topBar;
    private ImageView m_backBtn;
    private ImageView popupMenu;
    private FrameLayout.LayoutParams fl;
    private RelativeLayout.LayoutParams layoutParams;
    private LinearLayout popUpBtn;
    private LinearLayout popUpRoot;
    private RelativeLayout latelyJoinItem, allItem, latelyItem, oftenPlayItem;
    private AnimatorSet popUpAnimSet;
    private AnimatorSet packUpAnimSet;
    private boolean isPopUp = false;
    private long TIME = 172800000 * 3;//6天的毫秒数
    private boolean isDoingAnim = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TAG:
                    mList = (List<AudioStore.AudioInfo>) msg.obj;
                    adapter.setData(mList);
                    break;
            }

        }
    };


    public SelectMusicPage(Context context, BaseSite site) {
        super(context, site);
        this.mContext = context;
        this.mSite = (SelectMusicSite) site;
        MyThread thread = new MyThread();
        thread.start();
        initView();
        initAnimation();
    }


    private void initView() {

        setBackgroundColor(Color.BLACK);

        l = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final RelativeLayout m_Layout = new RelativeLayout(mContext);
        m_Layout.setBackgroundColor(Color.BLACK);
        this.addView(m_Layout, l);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
        layoutParams.addRule(RelativeLayout.ALIGN_TOP);
        m_topBar = new FrameLayout(mContext);
        m_Layout.addView(m_topBar, layoutParams);

        m_backBtn = new ImageView(getContext());
        m_backBtn.setOnClickListener(this);
        m_backBtn.setTag(1);
        m_backBtn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        m_backBtn.setImageResource(R.drawable.framework_back_btn);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        m_backBtn.setLayoutParams(fl);
        m_topBar.setId(R.id.m_topBar);
        m_topBar.addView(m_backBtn);

        {
            popUpBtn = new LinearLayout(mContext);
            popUpBtn.setOnClickListener(this);
            popUpBtn.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams mP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView m_titleView = new TextView(mContext);
            m_titleView.setText(getContext().getResources().getString(R.string.select_music));
            m_titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            m_titleView.setLayoutParams(fl);
            popUpBtn.addView(m_titleView, mP);

            popupMenu = new ImageView(getContext());
            popupMenu.setOnClickListener(this);
            popupMenu.setTag(1);
            popupMenu.setScaleType(ImageView.ScaleType.CENTER);
            popupMenu.setImageResource(R.drawable.beautify_effect_help_down);
            mP = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(35), ViewGroup.LayoutParams.WRAP_CONTENT);
            mP.gravity = Gravity.CENTER;
            popUpBtn.addView(popupMenu, mP);

            fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.CENTER;
            m_topBar.addView(popUpBtn, fl);
        }
        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView text = new TextView(mContext);
        text.setTextColor(0xff666666);
        text.setText(getContext().getResources().getString(R.string.undetectable_music));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        m_Layout.addView(text, layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.m_topBar);
        mRecyclerView = new RecyclerView(mContext);
        //  MusicAdapter adapter = new MusicAdapter(VideoResMgr.getMusicInfo(mContext));
        // mList = AudioStore.getAudioInfos(mContext);
        if (AudioStore.getAudioInfos(mContext).size() == 0) {
            mRecyclerView.setVisibility(VISIBLE);
        } else {
            text.setVisibility(INVISIBLE);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        // mRecyclerView.setAdapter(adapter);
        adapter = new MusicAdapter();
        mRecyclerView.setAdapter(adapter);
        m_Layout.addView(mRecyclerView, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popUpRoot = new LinearLayout(mContext);
        popUpRoot.setVisibility(INVISIBLE);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.m_topBar);
        popUpRoot.setOrientation(LinearLayout.VERTICAL);
        m_Layout.addView(popUpRoot, layoutParams);

        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            latelyJoinItem = new RelativeLayout(mContext);
            latelyJoinItem.setOnClickListener(this);
            latelyJoinItem.setId(R.id.album_popup_root1);

            latelyJoinItem.setBackgroundColor(Color.parseColor("#1a1a1a"));
            ImageView iv_Image1 = new ImageView(mContext);
            iv_Image1.setImageResource(R.drawable.select_music_cd);
            iv_Image1.setId(R.id.album_popup_image1);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(ShareData.PxToDpi_xhdpi(30), 0, ShareData.PxToDpi_xhdpi(30), 0);
            latelyJoinItem.addView(iv_Image1, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView itemText1 = new TextView(mContext);
            itemText1.setText(R.string.recent_music);
            itemText1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.album_popup_image1);
            //   params.setMargins(ShareData.PxToDpi_xhdpi(30),0,0,0);
            latelyJoinItem.addView(itemText1, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(120));
            ImageView arrowImage = new ImageView(mContext);
            arrowImage.setImageResource(R.drawable.framework_right_arrow);
            params.setMargins(0, 0, ShareData.PxToDpi_xhdpi(30), 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.CENTER_VERTICAL);
            latelyJoinItem.addView(arrowImage, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(140));
            popUpRoot.addView(latelyJoinItem, params);

            View parting1 = new View(mContext);
            parting1.setBackgroundColor(Color.parseColor("#000000"));
            parting1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(1)));
            popUpRoot.addView(parting1);
        }

        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            allItem = new RelativeLayout(mContext);
            allItem.setOnClickListener(this);
            allItem.setId(R.id.album_popup_root2);
            allItem.setBackgroundColor(Color.parseColor("#1a1a1a"));
            ImageView iv_Image2 = new ImageView(mContext);
            iv_Image2.setImageResource(R.drawable.select_music_cd);
            iv_Image2.setId(R.id.album_popup_image2);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.setMargins(ShareData.PxToDpi_xhdpi(30), 0, ShareData.PxToDpi_xhdpi(30), 0);
            allItem.addView(iv_Image2, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView itemText2 = new TextView(mContext);
            itemText2.setText(R.string.all_music);
            itemText2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.album_popup_image2);
            //   params.setMargins(ShareData.PxToDpi_xhdpi(30),0,0,0);
            allItem.addView(itemText2, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(120));
            ImageView arrowImage2 = new ImageView(mContext);
            arrowImage2.setImageResource(R.drawable.framework_right_arrow);
            params.setMargins(0, 0, ShareData.PxToDpi_xhdpi(30), 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT | RelativeLayout.CENTER_VERTICAL);
            allItem.addView(arrowImage2, params);

            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(140));
            params.addRule(RelativeLayout.BELOW, R.id.m_topBar);
            popUpRoot.addView(allItem, params);
            View parting2 = new View(mContext);
            parting2.setBackgroundColor(Color.parseColor("#000000"));
            parting2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(1)));
            popUpRoot.addView(parting2);
        }

    }

    private void initAnimation() {
        ObjectAnimator hiddenAnim = ObjectAnimator.ofFloat(mRecyclerView, "Alpha", 1, 0);
        hiddenAnim.setDuration(300);
        ObjectAnimator popUpAnim = ObjectAnimator.ofFloat(popUpRoot, "translationY", ShareData.PxToDpi_hdpi(-500), ShareData.PxToDpi_hdpi(0));
        popUpAnim.setDuration(300);
        this.popUpAnimSet = new AnimatorSet();
        this.popUpAnimSet.playTogether(hiddenAnim, popUpAnim);
        this.popUpAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isDoingAnim = false;
                mRecyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                isDoingAnim = true;
                popUpRoot.setVisibility(View.VISIBLE);
            }
        });


        ObjectAnimator showAnim = ObjectAnimator.ofFloat(mRecyclerView, "Alpha", 0, 1);
        showAnim.setDuration(300);
        ObjectAnimator packUpAnim = ObjectAnimator.ofFloat(popUpRoot, "translationY", ShareData.PxToDpi_hdpi(0), ShareData.PxToDpi_hdpi(-500));
        packUpAnim.setDuration(300);
        packUpAnimSet = new AnimatorSet();
        packUpAnimSet.playTogether(packUpAnim, showAnim);
        packUpAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                popUpRoot.setVisibility(GONE);

                isDoingAnim = false;
            }

            @Override
            public void onAnimationStart(Animator animation) {
                isDoingAnim = true;
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v == popUpBtn) {
            if (isPopUp) {
                doPackUpAnim();
            } else {
                doPopUpAnim();
            }
            //最近加入
        } else if (v == latelyJoinItem) {
            adapter.setData(getRecentlyData());
            doPackUpAnim();
            //最喜爱的
        } else if (v == allItem) {
            adapter.setData(mList);
            doPackUpAnim();
            //最近播放过
        } else if (v == latelyItem) {
            doPackUpAnim();
            //最常播放
        } else if (v == oftenPlayItem) {
            doPackUpAnim();
        } else if (v == m_backBtn) {
            mSite.onBack(null,getContext());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(isDoingAnim){
            return true;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    private void doPackUpAnim() {
        isPopUp = false;
        popupMenu.setImageResource(R.drawable.beautify_effect_help_down);
        packUpAnimSet.start();
    }

    private void doPopUpAnim()
    {
        popUpAnimSet.start();
        popupMenu.setImageResource(R.drawable.beautify_effect_help_up);
        isPopUp = true;
    }


    @Override
    public void SetData(HashMap<String, Object> params) {

    }

    @Override
    public void onBack() {
        if(isPopUp) {
            doPackUpAnim();
        }else{
            popUpAnimSet.cancel();
            packUpAnimSet.cancel();
            mSite.onBack(null,getContext());
        }
    }

    public List<AudioStore.AudioInfo> getRecentlyData() {
        List<AudioStore.AudioInfo> temp = new ArrayList<>();
        if (mList != null) {
            long time = System.currentTimeMillis();
            for (int i = 0; i < mList.size(); i++) {
                if (time - TIME < (mList.get(i).getAddTime() * 1000)) {
                    temp.add(mList.get(i));

                }
            }
        }
        return temp;
    }


    public interface OnItemClick {
        void onItemClick(Mp3Info info, int index);
    }

    class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

        private List<AudioStore.AudioInfo> mInfos;
        private OnItemClick onItemClick;
        private View.OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v != null && v.getTag() != null) {
                    int pos = (int) v.getTag();
                    int duration = (int) mInfos.get(pos).getDuration();
                    if(duration == 0){
                        duration = (int) VideoUtils.getDurationFromVideo2( mInfos.get(pos).getPath());
                    }
                    if(duration == 0){
                        Toast.makeText(getContext(), R.string.music_mistake, Toast.LENGTH_SHORT).show();
                    }else {
                        mInfos.get(pos).setDuration(duration);
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("music", mInfos.get(pos));
                        mSite.onBack(data,getContext());
                    }
                }
            }
        };

        public MusicAdapter(List<AudioStore.AudioInfo> mInfos) {
            this.mInfos = mInfos;
        }

        public MusicAdapter() {
            super();
        }

        public void setData(List<AudioStore.AudioInfo> mInfos) {
            this.mInfos = mInfos;
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


            RelativeLayout relativeLayout = new RelativeLayout(mContext);
            relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(140)));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_xhdpi(140), ShareData.PxToDpi_xhdpi(140));
            ImageView imageView = new ImageView(mContext);
            imageView.setId(R.id.album_image);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            relativeLayout.addView(imageView, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView song = new TextView(mContext);
            song.setLines(1);
            song.setEllipsize(TextUtils.TruncateAt.END);
            song.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            song.setId(R.id.album_song);
            song.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            layoutParams.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20), 0, 0);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.album_image);
            relativeLayout.addView(song, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView singer = new TextView(mContext);
            singer.setLines(1);
            singer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.album_song);
            layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.album_image);
            layoutParams.setMargins(ShareData.PxToDpi_xhdpi(40), ShareData.PxToDpi_xhdpi(20), 0, 0);
            relativeLayout.addView(singer, layoutParams);

            MyViewHolder myViewHolder = new MyViewHolder(relativeLayout);
            myViewHolder.imageView = imageView;
            myViewHolder.singerNane = singer;
            myViewHolder.songName = song;

            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            //  Mp3Info info = mInfos.get(position);
            AudioStore.AudioInfo info = mInfos.get(position);
            holder.songName.setText(info.getTitle());
            holder.singerNane.setText(info.getArtist());
            Glide.with(mContext).load(info.getCoverPath()).placeholder(R.drawable.video_music_default_logo).into(holder.imageView);
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickListener);

        }

        @Override
        public int getItemCount() {
            if (mInfos != null) {
                return mInfos.size();
            } else {
                return 0;
            }
        }

        public void setOnItemClick(OnItemClick onItemClick) {
            this.onItemClick = onItemClick;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            private RelativeLayout mLayout;
            private ImageView imageView;
            private TextView songName;
            private TextView singerNane;

            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }

    }


    class MyThread extends Thread {
        @Override
        public void run() {
            List<AudioStore.AudioInfo> list = AudioStore.getAudioInfos(mContext);
            mHandler.obtainMessage(TAG, list).sendToTarget();
        }
    }
}
