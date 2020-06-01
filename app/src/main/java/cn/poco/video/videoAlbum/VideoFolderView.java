package cn.poco.video.videoAlbum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import cn.poco.album2.adapter.FolderAdapter;
import cn.poco.album2.model.FolderInfo;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.helper.controller.MediaController;

/**
 * Created by lgd on 2018/1/3.
 */

public class VideoFolderView extends FrameLayout
{
    private View bk;
    private TextView mFolderName;
    private LinearLayout mBottomLL;
    private ImageView mFolderIndicator;
    private RecyclerView mFolderList;
    private FolderAdapter mFolderAdapter;
    private boolean isDoingAnim = false;
    private boolean mIsFold = true;
    private AnimatorSet mUnFoldAnim;
    private AnimatorSet mFoldAnim;
    private int ListH = ShareData.PxToDpi_xhdpi(531);
    private int mFolderIndex = 0;
    private View mLine;

    public VideoFolderView(@NonNull Context context)
    {
        super(context);
        initUi();
    }

    private void initUi()
    {
        LayoutParams fl;
        LinearLayout.LayoutParams ll;
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bk = new View(getContext());
        bk.setOnClickListener(mOnClickListener);
        bk.setVisibility(View.GONE);
        bk.setBackgroundColor(Color.BLACK);
        bk.getBackground().setAlpha((int) (0.6f * 255));
        addView(bk,fl);

        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ListH,Gravity.BOTTOM);
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(80);
        mFolderList = new RecyclerView(getContext());
        mFolderList.setBackgroundColor(0xcc000000);
        mFolderList.setPadding(0,ShareData.PxToDpi_xhdpi(20),0,ShareData.PxToDpi_xhdpi(20));
        mFolderList.setHasFixedSize(true);
        mFolderList.setBackgroundColor(Color.BLACK);
        mFolderList.setOverScrollMode(OVER_SCROLL_NEVER);
        mFolderList.setLayoutManager(new LinearLayoutManager(getContext()));
//        mFolderList.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
//        mFolderList.setClipToPadding(false);
        mFolderList.setVisibility(INVISIBLE);
        addView(mFolderList,fl);

        mLine = new View(getContext());
        mLine.setVisibility(View.GONE);
        mLine.setBackgroundColor(0xff3b3b3b);
        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,1,Gravity.BOTTOM);
        fl.bottomMargin = ShareData.PxToDpi_xhdpi(80)+1;
        addView(mLine,fl);

        fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80), Gravity.BOTTOM);
        mBottomLL = new LinearLayout(getContext());
        mBottomLL.setBackgroundColor(0xcc000000);
        mBottomLL.setGravity(Gravity.CENTER);
        mBottomLL.setOrientation(LinearLayout.HORIZONTAL);
        mBottomLL.setOnClickListener(mOnClickListener);
        addView(mBottomLL,fl);
        {
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER;
            mFolderName = new TextView(getContext());
            mFolderName.setText(R.string.video_album);
            mFolderName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,13f);
            mFolderName.setTextColor(Color.WHITE);
            mBottomLL.addView(mFolderName);

            mFolderIndicator = new ImageView(getContext());
            mFolderIndicator.setImageResource(R.drawable.album_ic_up);
            ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ll.gravity = Gravity.CENTER;
            ll.leftMargin= ShareData.PxToDpi_xhdpi(14);
            mBottomLL.addView(mFolderIndicator,ll);
        }

        int duration = 200;
        mUnFoldAnim = new AnimatorSet();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(bk,"alpha",0,1);
        ObjectAnimator translation = ObjectAnimator.ofFloat(mFolderList,"translationY",ListH,0);
        mUnFoldAnim.setDuration(duration);
        mUnFoldAnim.playTogether(alpha,translation);
        mUnFoldAnim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                mLine.setVisibility(View.VISIBLE);
                bk.setVisibility(View.VISIBLE);
                mFolderList.setVisibility(View.VISIBLE);
                isDoingAnim = true;
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {
                isDoingAnim = false;
            }
        });

        mFoldAnim = new AnimatorSet();
        alpha = ObjectAnimator.ofFloat(bk,"alpha",1,0);
        translation = ObjectAnimator.ofFloat(mFolderList,"translationY",0,ListH);
        mFoldAnim.setDuration(duration);
        mFoldAnim.playTogether(alpha,translation);
        mFoldAnim.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation)
            {
                mLine.setVisibility(View.GONE);
                bk.setVisibility(View.GONE);
                mFolderList.setVisibility(View.GONE);
                isDoingAnim = false;
            }

            @Override
            public void onAnimationStart(Animator animation)
            {
                isDoingAnim = true;
            }
        });

    }

    private View.OnClickListener mOnClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == mBottomLL){
                setFoldState(!mIsFold);
            }else if(v == bk){
                setFoldState(true);
            }
        }
    };

    private void setFoldState(boolean isFold)
    {
        if(!isDoingAnim){
            mIsFold = isFold;
            if(isFold){
                mFolderList.scrollToPosition(mFolderIndex);
                mFolderIndicator.setImageResource(R.drawable.album_ic_up);
                mFoldAnim.start();
            }else{
                mFolderIndicator.setImageResource(R.drawable.album_ic_down);
                mUnFoldAnim.start();
            }
        }
    }

    public boolean onBack()
    {
        if(isDoingAnim || !mIsFold){
            setFoldState(true);
            return true;
        }else{
            return false;
        }
    }

    private OnFolderCallBack mOnFolderCallBack;

    public void setOnFolderCallBack(OnFolderCallBack onFolderCallBack)
    {
        this.mOnFolderCallBack = onFolderCallBack;
    }

    public interface OnFolderCallBack
    {
        void onChange(int folderIndex);
    }

    public void setFolderInfos(List<FolderInfo> folderInfos,int position)
    {
//        List<FolderInfo> data = new ArrayList<>();
//        FolderInfo tempInfo;
//        for (int i = 0; i < folderInfos.size(); i++)
//        {
//            tempInfo = folderInfos.get(i);
//
//            if (tempInfo.getCount() <= 0)
//            {
//                if (position == i)
//                {
//                    position = 0;
//                } else if (position > i)
//                {
//                    position--;
//                }
//            } else
//            {
//                data.add(tempInfo);
//            }
//        }
//        if (data.isEmpty())
//        {
//            data.add(folderInfos.get(0));
//        }
//        mFolderAdapter = new FolderAdapter(getContext(), PhotoStore.getInstance(getContext()).getFolderInfos(), mFolderIndex);
        mFolderIndex = position;
        mFolderName.setText(folderInfos.get(position).getName());
        mFolderAdapter = new FolderAdapter(getContext(), folderInfos, mFolderIndex);
        mFolderAdapter.setOnFolderClickListener(new FolderAdapter.OnFolderClickListener()
        {
            @Override
            public void onClick(View view, int position)
            {
                setFoldState(true);
                if (mFolderIndex != position)
                {
                    mFolderIndex = position;
//                    mFolderName.setText(folderInfos.get(position).getName());
                    mFolderAdapter.setCurrentPos(position);
//                    FolderInfo folderInfo = PhotoStore.getInstance(getContext()).getFolderInfo(position);
                    FolderInfo folderInfo = MediaController.getInstance(getContext()).getFolderInfo(position);
                    mFolderName.setText(folderInfo.getName());
                    if(mOnFolderCallBack != null){
                        mOnFolderCallBack.onChange(position);
                    }
                }
            }
        });
        mFolderList.setAdapter(mFolderAdapter);
    }

}
