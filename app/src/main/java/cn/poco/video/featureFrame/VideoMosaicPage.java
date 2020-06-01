package cn.poco.video.featureFrame;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.BeautyAdjustType;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.SimpleListItem;
import cn.poco.draglistview.DragListView;
import cn.poco.framework.BaseSite;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.utils.FileUtil;
import cn.poco.utils.InterphotoDlg;
import cn.poco.utils.Utils;
import cn.poco.video.VideoResMgr;
import cn.poco.video.frames.VideoPage;
import cn.poco.video.model.VideoEntry;
import cn.poco.video.page.VideoModeWrapper;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.sequenceMosaics.AdapterDataInfo;
import cn.poco.video.sequenceMosaics.SequenceMosaicResMgr;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.sequenceMosaics.VideoEditTimeId;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.sequenceMosaics.VideoListAdapter;
import cn.poco.video.timeline2.TimelineLayout;

import static cn.poco.tianutils.ShareData.PxToDpi_xhdpi;

/**
 * 视频拼接
 */

public class VideoMosaicPage extends VideoPage
{
	private static final String TAG = "视频拼接";
	private static final int TIME_3M = 3 * 60 * 1000;
	private static final int TIME_10S = 10 * 1000;
	private VideoModeWrapper mVideoWrapper;	//视频美化首页
	private long mLastVideoTime;
	private boolean mHasBack = false;
	private boolean mOkBtn = false;
	private boolean mHasVideoChange = false;
	private boolean mHasTranChange = false;
	private ArrayList<VideoInfo> mVideoInfos;
	private ArrayList<VideoInfo> mTempInfos;
	private ArrayList<TransitionDataInfo> mTrans;
	private ArrayList<TransitionDataInfo> mTempTrans;

	private LinearLayout mTransitonFr;
	private SimpleBtnList100 mTransitionList;
	private ImageView mAwayBtn;

	private LinearLayout mVideoShowFr;
	private DragListView mVideoList;
	protected LinearLayout mHideFr;
	protected ImageView mHideIcon;
	protected TextView mHideText;
	protected boolean m_isChooseHideFr = false;
	private VideoListAdapter.VideoDragItem mDragItem;
	private VideoListAdapter mVideoAdapter;
	private SimpleBtnList100 mTimeList;	//选择视频总时间
	private int mMaxTime = TIME_3M;
	private int mCurSelTranIndex = 0;

	private LinearLayout mVideoEditFr;
	private FrameLayout mEditFr;
	private TimelineLayout mVideoClipList;
	private SimpleBtnList100 mVideoEditList;
	private LinearLayout mAdjustFr;
	private SimpleBtnList100 mAdjustList;
	private ImageView mAdjustAwayBtn;

	private LinearLayout mSeekBarFr;
	private TextView mSeekBarTip;
	private MySeekBar2 mSeekBar;
	private int mCurSelVideoIndex = 0;
	protected VideoResMgr.AdjustData mCurAdjustData;

//	private InterphotoDlg mDeleteTip;
	private InterphotoDlg m10ModeTip;

	private boolean mSeekBarShow = false;
	private boolean mHelpFlag= true;
	private Toast mToast;

	private boolean isDragCallPause;
	private VideoMosaicSite mSite;
	private cn.poco.video.view.ActionBar mActionBar;
	public VideoMosaicPage(@NonNull Context context, BaseSite baseSite, VideoModeWrapper videoWrapper)
	{
		super(context, baseSite);
		TongJiUtils.onPageStart(getContext(), TAG);
		mSite = (VideoMosaicSite) baseSite;
		mActionBar = videoWrapper.mActionBar;
		mVideoWrapper = videoWrapper;
		initUI();
	}

	private void initUI()
	{
	    mActionBar.setOnActionbarMenuItemClick(new cn.poco.video.view.ActionBar.onActionbarMenuItemClick() {
			@Override
			public void onItemClick(int id) {
			    if (id == cn.poco.video.view.ActionBar.LEFT_MENU_ITEM_CLICK) {
					mSite.onBack(getContext());
					if(!mHasBack){
//				Reset();
						mHasBack = true;
					}
				} else if (id == cn.poco.video.view.ActionBar.RIGHT_MENU_ITEM_CLICK) {
					mOkBtn = true;
//		mVideoWrapper.refreshWaterView();
					// // MyBeautyStat.onClickByRes(R.string.视频片段页_视频片段打钩);
					mSite.onBack(getContext());
				}
			}
		});

		FrameLayout.LayoutParams fl;
		mVideoShowFr = new LinearLayout(getContext());
		mVideoShowFr.setOrientation(LinearLayout.VERTICAL);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		mVideoShowFr.setLayoutParams(fl);
		this.addView(mVideoShowFr);
		{
			mVideoList = new DragListView(getContext());
			mDragItem = new VideoListAdapter.VideoDragItem(getContext());
			mVideoList.setCustomDragItem(mDragItem);
			final LinearLayoutManager lin = new LinearLayoutManager(getContext());
			lin.setOrientation(LinearLayoutManager.HORIZONTAL);
			mVideoList.setLayoutManager(lin);
			mVideoList.setCanDragHorizontally(true);
			mVideoList.setDragListCallback(m_dragControlCB);
			mVideoList.setDragListListener(m_dragListener);
			LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			mVideoList.setLayoutParams(ll);
			this.addView(mVideoList);

			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			fl.bottomMargin = ShareData.PxToDpi_xhdpi(95);
			mVideoList.getRecyclerView().setLayoutParams(fl);

			mHideFr = new LinearLayout(getContext());
			mHideFr.setGravity(Gravity.CENTER);
			mHideFr.setOrientation(LinearLayout.VERTICAL);
			mHideFr.setVisibility(GONE);
			mHideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
			fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			mHideFr.setLayoutParams(fl);
			mVideoList.addView(mHideFr, 0);
			{
				mHideIcon = new ImageView(getContext());
				mHideIcon.setImageResource(R.drawable.framework_delete_icon_out);
				ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				mHideIcon.setLayoutParams(ll);
				mHideFr.addView(mHideIcon);

				mHideText = new TextView(getContext());
				mHideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				mHideText.setTextColor(Color.WHITE);
				mHideText.setText(R.string.Delete);
				ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				ll.topMargin = ShareData.PxToDpi_xhdpi(20);
				mHideText.setLayoutParams(ll);
				mHideFr.addView(mHideText);
			}

			mVideoAdapter = new VideoListAdapter(getContext());
			mVideoAdapter.SetOnItemClickListener(new VideoListAdapter.OnItemClickListener()
			{
				@Override
				public void OnItemClick(View view, AdapterDataInfo info, int index)
				{
					//点击图片
					if(mVideoEditFr.getVisibility() == View.GONE && !isChangingPos)
					{
						// // MyBeautyStat.onClickByRes(R.string.视频片段页_切换至裁剪层);
						mVideoClipList.setVideoInfos(mVideoInfos, index);
						mCurSelVideoIndex = index;
						AnimShowView(mVideoEditFr, true);
						mVideoWrapper.hidePlayBtn();
						mActionBar.setRightImageBtnVisibility(View.GONE);
						mActionBar.setActionbarTitleIconVisibility(View.GONE);
						mVideoShowFr.setVisibility(GONE);
						mVideoList.setVisibility(GONE);
						setTitle(R.string.video_clip);
						mVideoWrapper.hideProgressTip();
//						mVideoWrapper.mVideoView.enterClipVideo(mCurSelVideoIndex);
					}
				}

				@Override
				public void OnIconClick(View view, AdapterDataInfo info, int index)
				{
					if(index < mVideoInfos.size() - 1 && mTransitonFr.getVisibility() == View.GONE)
					{
						mCurSelTranIndex = index;
						int listIndex = 0;
						ArrayList<SimpleBtnList100.Item> items = mTransitionList.GetDatas();
						for(SimpleBtnList100.Item item : items)
						{
							if((int)(((SimpleListItem)item).m_ex) == mTrans.get(mCurSelTranIndex).mID)
							{
								break;
							}
							listIndex ++;
						}
						// // MyBeautyStat.onClickByRes(R.string.视频片段页_打开转场动画);
						mTransitionList.SetSelByIndex(listIndex);
						AnimShowView(mTransitonFr, true);
						mVideoShowFr.setVisibility(GONE);
						mActionBar.setRightImageBtnVisibility(View.GONE);
						mActionBar.setActionbarTitleIconVisibility(View.GONE);
						mVideoList.setVisibility(GONE);
						setTitle(R.string.video_trans);
					}
					else if(index == mVideoInfos.size() - 1)
					{
//						mIsPause = mVideoWrapper.mVideoView.isPause();
						if(!mIsPause){
							mVideoWrapper.pauseAll();
						}
						int size = mVideoInfos.size();
						int video_times = 0;
						for(VideoInfo info1 : mVideoInfos)
						{
							video_times += info1.GetEndTime() - info1.GetStartTime();
						}
						// // MyBeautyStat.onClickByRes(R.string.视频片段页_添加视频);
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("video_len", size);
						params.put("video_times", mMaxTime - video_times);
//						params.put("show_choose_ratio", false);
                        mSite.onVideoAlbum(getContext(), params);
					}
				}
			});
			mVideoList.setAdapter(mVideoAdapter, true);

			mTimeList = new SimpleBtnList100(getContext());
			ArrayList<SimpleBtnList100.Item> items = SequenceMosaicResMgr.getTimeItems(getContext());
			mTimeList.SetData(items, new SimpleBtnList100.Callback()
			{
				@Override
				public void OnClick(SimpleBtnList100.Item view, int index)
				{
					onTimeItemClick(view, index);
				}
			});
			mTimeList.SetSelByIndex(0);
			ll = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
			mTimeList.setLayoutParams(ll);
			mVideoShowFr.addView(mTimeList);
		}

		initTransitonUI();

		initEditUI();

		m10ModeTip = new InterphotoDlg((Activity)getContext(), R.style.waitDialog);
		m10ModeTip.SetTitle(getResources().getString(R.string.mode10title));
		m10ModeTip.SetMessage(R.string.mode10content);
		m10ModeTip.setLeftRightBtnColor(0xffa6a6a6, 0xffa6a6a6);
		m10ModeTip.SetMessagePadding(ShareData.PxToDpi_xhdpi(75), ShareData.PxToDpi_xhdpi(75), 0, 0);
		m10ModeTip.SetNegativeBtnText(getResources().getString(R.string.ok));
		m10ModeTip.SetPositiveBtnText(getResources().getString(R.string.Cancel));
		m10ModeTip.setOnDlgClickCallback1(new InterphotoDlg.OnDlgClickCallback1()
		{
			@Override
			public void onRight()
			{
				// // MyBeautyStat.onClickByRes(R.string.视频片段页_10秒模式弹窗_取消按钮);
				m10ModeTip.dismiss();
			}

			@Override
			public void onLeft()
			{
				videoListAlpha(mVideoList);
				mTimeUri = VideoEditTimeId.TYPE_10S;
				for(VideoInfo info : mVideoInfos){
					info.mTimeType = mTimeUri;
					info.mHasEdit = true;
				}

				// // MyBeautyStat.onClickByRes(R.string.视频片段页_10秒模式弹窗_好的按钮);
				m10ModeTip.dismiss();

				mMaxTime = TIME_10S;
				ComputePerVideo(mVideoInfos, mMaxTime);
				checkTransition();
				mVideoClipList.setTenSecondMode(true);
				ClipVideos(true);
				mTimeList.SetSelByIndex(1);
				mVideoAdapter.notifyDataSetChanged();
			}
		});
		mToast = Toast.makeText(getContext(), R.string.video_tran_unavailable, Toast.LENGTH_SHORT);

//		mVideoWrapper.mVideoView.addOnPlayListener(mOnPlayListener);
//		mVideoWrapper.mVideoView.addOnPauseListener(mOnPauseListener);
	}

	private void initTransitonUI()
	{
		FrameLayout.LayoutParams fl;
		mTransitonFr = new LinearLayout(getContext());
		mTransitonFr.setVisibility(GONE);
		mTransitonFr.setOrientation(LinearLayout.VERTICAL);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		mTransitonFr.setLayoutParams(fl);
		this.addView(mTransitonFr);
		{
			LinearLayout.LayoutParams ll;
			mTransitionList = new SimpleBtnList100(getContext());
			ArrayList<SimpleBtnList100.Item> items = SequenceMosaicResMgr.getTransitionItems(getContext());
			mTransitionList.SetData(items, new SimpleBtnList100.Callback()
			{
				@Override
				public void OnClick(SimpleBtnList100.Item view, int index)
				{
					onTrasitionItemClick((SimpleListItem)view, index);
				}
			});
			ll = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(140));
			ll.bottomMargin = ShareData.PxToDpi_xhdpi(41);
			mTransitionList.setLayoutParams(ll);
			mTransitonFr.addView(mTransitionList);
		}
	}

	private void initEditUI()
	{
		FrameLayout.LayoutParams fl;
		mVideoEditFr = new LinearLayout(getContext());
		mVideoEditFr.setVisibility(GONE);
		mVideoEditFr.setOrientation(LinearLayout.VERTICAL);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		mVideoEditFr.setLayoutParams(fl);
		this.addView(mVideoEditFr);
		{
			LinearLayout.LayoutParams ll;
			mEditFr = new FrameLayout(getContext());
			ll = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(174));
			mEditFr.setLayoutParams(ll);
			mVideoEditFr.addView(mEditFr);
			{
//				InitEditList();

				/*ImageView tipLine = new ImageView(getContext());
				tipLine.setImageResource(R.drawable.video_edit_line);
				fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				fl.gravity = Gravity.CENTER;
				tipLine.setLayoutParams(fl);
				mEditFr.addView(tipLine);*/
			}

			mVideoEditList = new SimpleBtnList100(getContext());
			ArrayList<SimpleBtnList100.Item> items = SequenceMosaicResMgr.getEditItems(getContext());
			mVideoEditList.SetData(items, new SimpleBtnList100.Callback()
			{
				@Override
				public void OnClick(SimpleBtnList100.Item view, int index)
				{
					onEditItemClick(view, index);
				}
			});
			ll = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
			mVideoEditList.setLayoutParams(ll);
			mVideoEditFr.addView(mVideoEditList);
		}

		mSeekBarFr = new LinearLayout(getContext());
		mSeekBarFr.setVisibility(View.GONE);
		mSeekBarFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		fl.bottomMargin = ShareData.PxToDpi_xhdpi(160);
		mSeekBarFr.setLayoutParams(fl);
		this.addView(mSeekBarFr);
		{
			LinearLayout.LayoutParams ll;
			mSeekBarTip = new TextView(getContext());
			mSeekBarTip.setMaxLines(1);
			mSeekBarTip.setText("0");
			mSeekBarTip.setTextColor(Color.WHITE);
			mSeekBarTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.LEFT;
			ll.leftMargin = ShareData.PxToDpi_xhdpi(21);
			mSeekBarTip.setLayoutParams(ll);
			mSeekBarFr.addView(mSeekBarTip);

			mSeekBar = new MySeekBar2(getContext());
			mSeekBar.setMax(12);
			mSeekBar.SetDotNum(13);
			mSeekBar.setOnSeekBarChangeListener(mSeekBarLst);
			mSeekBar.setProgress(0);
			ll = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.topMargin = ShareData.PxToDpi_xhdpi(10);
			mSeekBar.setLayoutParams(ll);
			mSeekBarFr.addView(mSeekBar);
		}

		initAdjustUI();
	}

	private void InitEditList()
	{
		FrameLayout.LayoutParams fl;
		mVideoClipList = new TimelineLayout(getContext());
		mVideoClipList.setOnDragListener(new TimelineLayout.OnDragListener()
		{
			@Override
			public void onDragLeft(int index, long position)
			{
				VideoInfo info = mVideoInfos.get(index);
//				mVideoWrapper.showProgressTip(info.GetStartTime(), info.mDuration);
				if(info.GetStartTime() != position)
				{
					mHasVideoChange = true;
					info.mHasEdit = true;
					info.SetStartTime(position);

//					mVideoWrapper.mVideoView.clipVideo(index, info.GetStartTime(), info.GetEndTime());
//					mVideoWrapper.mVideoView.seekTo(index, 1);
				}
			}

			@Override
			public void onDragRight(int index, long position)
			{
				VideoInfo info = mVideoInfos.get(index);
//				mVideoWrapper.showProgressTip(info.GetEndTime(), info.mDuration);
				if(info.GetEndTime() != position)
				{
					mHasVideoChange = true;
					info.mHasEdit = true;
					info.SetEndTime(position);
//					mVideoWrapper.mVideoView.clipVideo(index, info.GetStartTime(), info.GetEndTime());
//					mVideoWrapper.mVideoView.seekTo(index, info.GetEndTime()-1);
				}
			}

			@Override
			public void onDragOverall(int index, long startPosition, long endPosition) {
				VideoInfo info = mVideoInfos.get(index);
				if(info.GetStartTime() != startPosition || info.GetEndTime() != endPosition)
				{
					mHasVideoChange = true;
					info.mHasEdit = true;
					info.SetStartTime(startPosition);
					info.SetEndTime(endPosition);
//					mVideoWrapper.mVideoView.clipVideo(index, info.GetStartTime(), info.GetEndTime());
//					mVideoWrapper.mVideoView.seekTo(index, 1);
				}
			}

			@Override
			public void onDragStart() {
//				isDragCallPause = mVideoWrapper.mVideoView.isPause();
			}

			@Override
			public void onDragStop() {

				mVideoWrapper.hideProgressTip();
				if (!isDragCallPause) {
//					mVideoWrapper.mVideoView.restart();
				}
			}

			@Override
			public void onStartMove()
			{
				mActionBar.setAlpha(0.5f);
				mVideoEditList.setAlpha(0.5f);
			}

			@Override
			public void onStopMove()
			{
				mActionBar.setAlpha(1f);
				mVideoEditList.setAlpha(1f);
			}
		});
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(128));
		fl.gravity = Gravity.TOP;
		fl.topMargin = ShareData.PxToDpi_xhdpi(23);
		mVideoClipList.setLayoutParams(fl);
		mEditFr.addView(mVideoClipList, 0);
		if(mTimeUri == VideoEditTimeId.TYPE_10S)
		{
			mVideoClipList.setTenSecondMode(true);
		}
		else
		{
			mVideoClipList.setTenSecondMode(false);
		}
	}

	private void initAdjustUI()
	{
		FrameLayout.LayoutParams fl;
		mAdjustFr = new LinearLayout(getContext());
		mAdjustFr.setVisibility(View.GONE);
		mAdjustFr.setBackgroundColor(0xff000000);
		mAdjustFr.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		mAdjustFr.setLayoutParams(fl);
		this.addView(mAdjustFr);
		{
			mAdjustList = new SimpleBtnList100(getContext());
			mAdjustList.SetData(VideoResMgr.getColorAdjustItems(getContext()), new SimpleBtnList100.Callback()
			{
				@Override
				public void OnClick(SimpleBtnList100.Item view, int index)
				{
					OnAdjustItemClick(view, index);
				}
			});
			LinearLayout.LayoutParams ll;
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(120));
			mAdjustList.setLayoutParams(ll);
			mAdjustFr.addView(mAdjustList);

			ImageView Line = new ImageView(getContext());
			Line.setBackgroundColor(0xff272727);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
			Line.setLayoutParams(ll);
			mAdjustFr.addView(Line);

			mAdjustAwayBtn = new ImageView(getContext());
			mAdjustAwayBtn.setScaleType(ImageView.ScaleType.CENTER);
			mAdjustAwayBtn.setOnClickListener(mBtnLst);
			mAdjustAwayBtn.setImageResource(R.drawable.beauty_color_adjust_down);
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
			mAdjustAwayBtn.setLayoutParams(ll);
			mAdjustFr.addView(mAdjustAwayBtn);
		}
	}

	private void OnAdjustItemClick(SimpleBtnList100.Item view, int index)
	{
		if(view != null){
			VideoResMgr.AdjustData data = (VideoResMgr.AdjustData) (((SimpleListItem) view).m_ex);

			if (mCurAdjustData == null || mCurAdjustData.m_type1 != data.m_type1) {
				TongJi2.AddCountByRes(getContext(), ((SimpleListItem) view).m_tjID);
				if (mAdjustList != null) {
					mAdjustList.SetSelByIndex(index);
					mAdjustList.ScrollToCenter(true);
				}
				adjustBtnClick(data);
				setTitle(((SimpleListItem) view).m_title);

			} else {
				String title = null;
				if (mSeekBarShow == true)
				{
					mSeekBarShow = false;
					mSeekBarFr.setVisibility(View.GONE);
					String text = getResources().getString(R.string.Adjust);
					title = text;
					mAdjustList.SetSelByIndex(-1);
				} else {
					mSeekBarShow = true;
					mSeekBarFr.setVisibility(View.VISIBLE);
					mAdjustList.SetSelByIndex(index);
				}
				if(title != null){
					setTitle(title);
				}
			}
		}
	}

	private void setTitle(String title)
	{
		mActionBar.getActionBarTitleView().setText(title);
	}

	private void setTitle(Integer title)
	{
		mActionBar.getActionBarTitleView().setText(title);
	}

	//动画显示view, 从下往上显示，从上往下收起
	private void AnimShowView(View view, boolean show)
	{
		if (view == null)
			return;
		view.clearAnimation();

		int start;
		int end;
		if (show)
		{
			view.setVisibility(View.VISIBLE);

			start = 1;
			end = 0;
		}
		else
		{
			view.setVisibility(View.GONE);

			start = 0;
			end = 1;
		}

		AnimationSet as;
		TranslateAnimation ta;
		as = new AnimationSet(true);
		ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
		ta.setDuration(200);
		as.addAnimation(ta);
		view.startAnimation(as);
	}

	private View.OnClickListener mBtnLst = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == mAwayBtn)
			{
				// // MyBeautyStat.onClickByRes(R.string.转场动画_退出转场动画);
				switch(mTrans.get(mCurSelTranIndex).mID){
					case TransitionItem.NONE:
					{
						// // MyBeautyStat.onClickByRes(R.string.转场动画_无转场);
						break;
					}
					case TransitionItem.ALPHA:
					{
						// // MyBeautyStat.onClickByRes(R.string.转场动画_交叉叠化);
						break;
					}
					case TransitionItem.BLACK:
					{
						// // MyBeautyStat.onClickByRes(R.string.转场动画_黑场过渡);
						break;
					}
					case TransitionItem.WHITE:
					{
						// // MyBeautyStat.onClickByRes(R.string.转场动画_白场过渡);
						break;
					}
					case TransitionItem.BLUR:
					{
						// // MyBeautyStat.onClickByRes(R.string.转场动画_模糊);
						break;
					}
				}
				AnimShowView(mTransitonFr, false);
				mVideoShowFr.setVisibility(VISIBLE);
				mActionBar.setActionbarTitleIconVisibility(View.VISIBLE);
				mVideoList.setVisibility(VISIBLE);
				mActionBar.setRightImageBtnVisibility(View.VISIBLE);
				setTitle(R.string.footage);
				mVideoAdapter.notifyDataSetChanged();
			}
			else if(v == mAdjustAwayBtn)
			{
				// // MyBeautyStat.onClickByRes(R.string.色彩调整页_退出色彩调整);
				mVideoWrapper.mActionBar.setLeftImageBtnVisibility(View.VISIBLE);
				AnimShowView(mAdjustFr, false);
				if(mSeekBarFr.getVisibility() == VISIBLE)
				{
					AnimShowView(mSeekBarFr, false);
				}
				AnimShowView(mVideoEditFr, true);
				mCurAdjustData = null;
				setTitle(R.string.video_clip);
				mVideoWrapper.hidePlayBtn();
			}
		}
	};

	private DragListView.DragListCallback m_dragControlCB = new DragListView.DragListCallback()
	{
		@Override
		public boolean canDragItemAtPosition(int dragPosition)
		{
			return !isChangingPos;
		}

		@Override
		public boolean canDropItemAtPosition(int dropPosition)
		{
			return true;
		}
	};

	/**
	 * 检查转场动画，碰到模糊和交叉的要判断前后视频是不是时间都大于2S，如果不大于就把转场设置成无
	 */
	private void checkTransition()
	{
		int tranIndex = 0;
		long preVideoTime = 0;
		long afterVideoTime = 0;
		int[] ids = new int[mTrans.size()];
		boolean needChange = false;
		for(TransitionDataInfo tran : mTrans){
			if(tran.mID == TransitionItem.ALPHA || tran.mID == TransitionItem.BLUR){

				if(tranIndex >= 0 && tranIndex < mVideoInfos.size()){
					VideoInfo info = mVideoInfos.get(tranIndex);

					preVideoTime = info.GetEndTime() - info.GetStartTime();
				}
				if(tranIndex + 1 >= 0 && tranIndex + 1 < mVideoInfos.size()){
					VideoInfo info = mVideoInfos.get(tranIndex + 1);

					afterVideoTime = info.GetEndTime() - info.GetStartTime();
				}
				if(preVideoTime < 2000 || afterVideoTime < 2000)
				{
					needChange = true;
					tran.mID = TransitionItem.NONE;
					tran.mRes = R.drawable.video_feature_clip_icon;
					mVideoAdapter.UpdateTransResData(tranIndex, tran);
				}
			}
			ids[tranIndex] = tran.mID;
			tranIndex ++;
		}
		if(needChange){
			mVideoWrapper.mVideoView.setTransitions(ids);
		}
	}

	private void DeleteVideo()
	{
		if(mVideoInfos != null && mVideoInfos.size() > 1){
			// // MyBeautyStat.onClickByRes(R.string.视频裁剪页_删除视频);
			mHasVideoChange = true;
			int key = mVideoInfos.remove(mCurSelVideoIndex).mUri;
			if(mCurSelVideoIndex >= 0 && mCurSelVideoIndex < mTrans.size())
			{
				mTrans.remove(mCurSelVideoIndex);
			}
			else
			{
				mTrans.remove(mCurSelVideoIndex - 1);
			}
			deleteVideo(mCurSelVideoIndex);
			mVideoWrapper.hideProgressTip();
			mVideoWrapper.resumeAll(true);
			mVideoAdapter.removeItemByKey(key);
			mVideoAdapter.notifyDataSetChanged();
			mCurSelVideoIndex--;
			mCurSelVideoIndex = mCurSelVideoIndex < 0 ? 0 : mCurSelVideoIndex;
		}
	}

	protected DragListView.DragListListener m_dragListener = new DragListView.DragListListener()
	{
		@Override
		public void onItemDragStarted(int position)
		{
			mCurSelVideoIndex = position;
			m_isChooseHideFr = false;
			mVideoList.setBackgroundColor(0xb2000000);
			if(mVideoInfos.size() > 1)
				Utils.AlphaAnim(mHideFr, true, 400);

			if(mVideoAdapter != null)
			{
				mVideoAdapter.ShowIcon(false);
			}
		}

		@Override
		public void onItemDragging(int itemPosition, float x, float y)
		{
			boolean laseChoose = m_isChooseHideFr;
			RectF rectF = new RectF(mHideFr.getLeft(), mHideFr.getTop(), mHideFr.getRight(), mHideFr.getBottom());
			if(x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom)
			{
				m_isChooseHideFr = true;
				if(mDragItem != null)
				{
					mDragItem.DoAlphaAnim(true);
				}
			}
			else
			{
				m_isChooseHideFr = false;
				if(mDragItem != null)
				{
					mDragItem.DoAlphaAnim(false);
				}
			}
			if(laseChoose == !m_isChooseHideFr)
			{
				if(m_isChooseHideFr)
				{
					mHideFr.setBackgroundResource(R.drawable.framework_hide_bg_over);
					mHideIcon.setImageResource(R.drawable.framework_delete_icon_over);
					mHideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				}
				else
				{
					mHideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
					mHideIcon.setImageResource(R.drawable.framework_delete_icon_out);
					mHideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				}
			}
		}

		@Override
		public void onItemDragEnded(int fromPosition, int toPosition)
		{
			mVideoList.setBackgroundColor(0x00000000);
			if(mVideoInfos.size() > 1)
				Utils.AlphaAnim(mHideFr, false, 400);

			mVideoAdapter.ShowIcon(true);
			if(m_isChooseHideFr)
			{
				DeleteVideo();
			}
			else
			{
				if(mVideoAdapter != null && fromPosition != toPosition)
				{
					isChangingPos = true;
					mHasVideoChange = true;
					mVideoAdapter.ChangeLastIndexTrans();
					mVideoAdapter.notifyDataSetChanged();
//					mVideoWrapper.mVideoView.changeVideoOrder(fromPosition, toPosition, null);
					mVideoInfos.clear();
					mTrans.clear();
					mVideoAdapter.GetVideosData(mVideoInfos, mTrans);
				}
			}

		}
	};

	private void onTrasitionItemClick(SimpleListItem view, int index)
	{
		if(view != null && mCanTransitionClick)
		{
			int curTran = (int)view.m_ex;
			long preVideoTime = 0;
			long afterVideoTime = 0;
			if(mCurSelTranIndex >= 0 && mCurSelTranIndex < mVideoInfos.size()){
				VideoInfo info = mVideoInfos.get(mCurSelTranIndex);

				preVideoTime = info.GetEndTime() - info.GetStartTime();
			}
			if(mCurSelTranIndex + 1 >= 0 && mCurSelTranIndex + 1 < mVideoInfos.size()){
				VideoInfo info = mVideoInfos.get(mCurSelTranIndex + 1);

				afterVideoTime = info.GetEndTime() - info.GetStartTime();
			}
			boolean needRefresh = true;
			switch(curTran)
			{
				case TransitionItem.NONE:
				{
					mTransitionList.SetSelByIndex(index);
					mTrans.get(mCurSelTranIndex).mID = curTran;
					mTrans.get(mCurSelTranIndex).mRes = R.drawable.video_feature_clip_icon;
					break;
				}
				case TransitionItem.ALPHA:
				{
					if(preVideoTime >= 2000 && afterVideoTime >= 2000)
					{
						mTransitionList.SetSelByIndex(index);
						mTrans.get(mCurSelTranIndex).mID = curTran;
						mTrans.get(mCurSelTranIndex).mRes = R.drawable.transition_overlap_out;
					}
					else{
						needRefresh = false;
						mToast.show();
					}
					break;
				}
				case TransitionItem.BLACK:
				{
					mTransitionList.SetSelByIndex(index);
					mTrans.get(mCurSelTranIndex).mID = curTran;
					mTrans.get(mCurSelTranIndex).mRes = R.drawable.transition_black_out;
					break;
				}
				case TransitionItem.WHITE:
				{
					mTransitionList.SetSelByIndex(index);
					mTrans.get(mCurSelTranIndex).mID = curTran;
					mTrans.get(mCurSelTranIndex).mRes = R.drawable.transition_white_out;
					break;
				}
				case TransitionItem.BLUR:
				{
					if(preVideoTime >= 2000 && afterVideoTime >= 2000)
					{
						mTransitionList.SetSelByIndex(index);
						mTrans.get(mCurSelTranIndex).mID = curTran;
						mTrans.get(mCurSelTranIndex).mRes = R.drawable.transition_fuzzy_out;
					}
					else{
						needRefresh = false;
						mToast.show();
					}
					break;
				}
			}
			if(mVideoAdapter != null && needRefresh)
			{
				mCanTransitionClick = false;
				mVideoAdapter.UpdateTransResData(mCurSelTranIndex, mTrans.get(mCurSelTranIndex));
				mVideoWrapper.mVideoView.setTransition(mCurSelTranIndex, curTran);
			}
		}
	}

	private int mTimeUri = VideoEditTimeId.TYPE_FREE;
	private void onTimeItemClick(SimpleBtnList100.Item view, int index)
	{
		if(view.m_uri != mTimeUri)
		{
			if(view.m_uri == VideoEditTimeId.TYPE_FREE)
			{
				mTimeUri = view.m_uri;
				for(VideoInfo info : mVideoInfos){
					info.mTimeType = mTimeUri;
					info.mHasEdit = true;
				}

				// // MyBeautyStat.onClickByRes(R.string.视频片段页_点击自由模式);
				videoListAlpha(mVideoList);
				mMaxTime = TIME_3M;
				mVideoClipList.setTenSecondMode(false);
				ClipVideos(true);
				mTimeList.SetSelByIndex(index);
				mVideoAdapter.notifyDataSetChanged();
			}
			else
			{
				// // MyBeautyStat.onClickByRes(R.string.视频片段页_点击10秒模式);
				if(TagMgr.CheckTag(getContext(), "10stip")){
					m10ModeTip.show();
					TagMgr.SetTag(getContext(), "10stip");
				}
				else{
					videoListAlpha(mVideoList);
					mTimeUri = VideoEditTimeId.TYPE_10S;
					for(VideoInfo info : mVideoInfos){
						info.mTimeType = mTimeUri;
						info.mHasEdit = true;
					}

					mMaxTime = TIME_10S;
					ComputePerVideo(mVideoInfos, mMaxTime);
					checkTransition();
					mVideoClipList.setTenSecondMode(true);
					ClipVideos(true);
					mTimeList.SetSelByIndex(1);
					mVideoAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	private void onEditItemClick(SimpleBtnList100.Item view, int index)
	{
		if(index == 0)
		{
//			mBtnLst.onClick(mAdjustAwayBtn);
		}
		else if(index == 1)
		{
			if(mAdjustFr.getVisibility() == View.GONE)
			{
				// // MyBeautyStat.onClickByRes(R.string.视频裁剪页_进入色彩调整);
				mVideoWrapper.mActionBar.setLeftImageBtnVisibility(View.GONE);
				AnimShowView(mAdjustFr, true);
				AnimShowView(mVideoEditFr, false);
				setTitle(R.string.Adjust);
				mAdjustList.SetSelByIndex(-1);
				mAdjustList.ScrollToCenter(false);
			}
		}
	}

	protected VideoResMgr.AdjustData InsertToAdjustList(VideoResMgr.AdjustData data)
	{
		VideoResMgr.AdjustData out = data;
		if (mVideoInfos != null && mCurSelVideoIndex >= 0 && mCurSelVideoIndex < mVideoInfos.size()) {
			VideoResMgr.AdjustData tempData;
			VideoInfo info = mVideoInfos.get(mCurSelVideoIndex);

			boolean flag = true;
			for (int i = 0; i < info.m_adjustData.size(); i++) {
				tempData = info.m_adjustData.get(i);
				if (data.m_type1 == tempData.m_type1) {
					out = tempData;
					flag = false;
					break;
				}
			}
			if (flag) {
				out = data.Clone();
				info.m_adjustData.add(out);
			}
		}
		return out;
	}

	protected void ReLayoutSeekBarTip(int progress, int max)
	{
		int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40);
		int leftMargin = ShareData.PxToDpi_xhdpi(20);
		int seekBarThumbW = ShareData.PxToDpi_xhdpi(21);
		LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) mSeekBarTip.getLayoutParams();
		ll.leftMargin = (int) ((w - (seekBarThumbW << 1)) * progress / (float) max + leftMargin + seekBarThumbW - ShareData.PxToDpi_xhdpi(35));
		mSeekBarTip.setLayoutParams(ll);
		String tip;
		if (mCurAdjustData != null)
		{
			switch (mCurAdjustData.m_type1)
			{
				case AdjustItem.BRIGHTNESS:
				case AdjustItem.CONTRAST:
				case AdjustItem.SATURATION:
				case AdjustItem.COLOR_BALANCE:
				case AdjustItem.WHITE_BALANCE:
					int progress1 = progress - 6;
					if (progress1 > 0) {
						tip = "+" + progress1;
					} else if (progress1 < 0) {
						tip = "" + progress1;
					} else {
						tip = "  " + progress1;
					}
					mSeekBarTip.setText(tip);
					break;
				case AdjustItem.DARK_CORNER:
				case AdjustItem.HIGHLIGHT:
				case AdjustItem.SHADOW:
				case AdjustItem.SHARPEN: {
					{
						if (progress > 0) {
							tip = "+" + progress;
						} else if (progress < 0) {
							tip = "" + progress;
						} else {
							tip = " " + progress;
						}
						mSeekBarTip.setText(tip);
						break;
					}
				}
				default:
					break;
			}
		}
	}

	protected void adjustBtnClick(VideoResMgr.AdjustData data) {
		mCurAdjustData = data;
		if (mCurAdjustData == null) {
			return;
		}
		mCurAdjustData = InsertToAdjustList(data);

		if (mCurAdjustData.m_type1 != BeautyAdjustType.NONE.GetValue()) {
			mSeekBarShow = true;
			mSeekBarFr.setVisibility(View.VISIBLE);
		} else {
			mSeekBarShow = false;
			mSeekBarFr.setVisibility(View.GONE);
		}
		mSeekBar.setProgress(0);
		int progress = 0;
		if (mSeekBarFr != null && mCurAdjustData.m_type1 != BeautyAdjustType.NONE.GetValue()) {
			if (mSeekBarFr.getVisibility() == View.GONE) {
				mSeekBarFr.setVisibility(View.VISIBLE);
			}
			int max = mSeekBar.getMax();

			switch (mCurAdjustData.m_type1) {
				case AdjustItem.BRIGHTNESS:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_亮度);
					progress = (int) (((mCurAdjustData.m_value + 30) / 60f) * max + 0.5f);
					break;
				case AdjustItem.CONTRAST:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_对比度);
					progress = (int) (((mCurAdjustData.m_value - 0.9f) / 0.3f) * max + 0.5f);
					break;
				case AdjustItem.SATURATION:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_饱和度);
					progress = (int) (((mCurAdjustData.m_value - 0.5f) / 1f) * max + 0.5f);
					break;
				case AdjustItem.WHITE_BALANCE: {
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_色温);
					progress = (int) (((mCurAdjustData.m_value + 0.35f) / 0.7f) * max + 0.5f);
					break;
				}
				case AdjustItem.SHARPEN:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_锐度);
					progress = (int) (mCurAdjustData.m_value * max + 0.5f);
					break;

				case AdjustItem.COLOR_BALANCE:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_色调);
					progress = (int) (((mCurAdjustData.m_value + 0.1f) / 0.2f) * max + 0.5f);
					break;

				case AdjustItem.DARK_CORNER:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_暗角);
					progress = (int) (mCurAdjustData.m_value * max + 0.5f);
					break;

				case AdjustItem.SHADOW:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_阴影补偿);
					progress = (int) (mCurAdjustData.m_value * max + 0.5f);
					break;

				case AdjustItem.HIGHLIGHT:
					// // MyBeautyStat.onClickByRes(R.string.色彩调整页_高光减淡);
					progress = (int) (mCurAdjustData.m_value * max + 0.5f);
					break;

				default:
					break;
			}
			mSeekBar.setProgress(progress);
			ReLayoutSeekBarTip(progress, max);
		}
	}

	protected SeekBar.OnSeekBarChangeListener mSeekBarLst = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
		{
			ReLayoutSeekBarTip(progress, seekBar.getMax());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar)
		{

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar)
		{
			int progress = seekBar.getProgress();
			int maxP = seekBar.getMax();
			if (mCurAdjustData != null && mVideoInfos != null &&
					mCurSelVideoIndex >= 0 && mCurSelVideoIndex < mVideoInfos.size())
			{
				AdjustItem adjustItem = null;
				switch (mCurAdjustData.m_type1) {
					case AdjustItem.BRIGHTNESS: {
						mCurAdjustData.m_value = -30 + (progress * 2 / (float) maxP) * 30;

						TongJi2.AddCountByRes(getContext(), R.integer.亮度);
						adjustItem = new AdjustItem(AdjustItem.BRIGHTNESS, mCurAdjustData.m_value);
						break;
					}
					case AdjustItem.CONTRAST: {
						mCurAdjustData.m_value = 0.9f + (progress / (float) maxP) * 0.3f;

						TongJi2.AddCountByRes(getContext(), R.integer.对比);
						adjustItem = new AdjustItem(AdjustItem.CONTRAST, mCurAdjustData.m_value);
						break;
					}
					case AdjustItem.SATURATION: {
						mCurAdjustData.m_value = 0.5f + ((progress / (float) maxP) * 1f);

						TongJi2.AddCountByRes(getContext(), R.integer.饱和度);
						adjustItem = new AdjustItem(AdjustItem.SATURATION, mCurAdjustData.m_value);
						break;
					}
					case AdjustItem.SHARPEN: {
						mCurAdjustData.m_value = progress / (float) maxP;

						TongJi2.AddCountByRes(getContext(), R.integer.锐化);
						adjustItem = new AdjustItem(AdjustItem.SHARPEN, mCurAdjustData.m_value);
						break;
					}

					case AdjustItem.COLOR_BALANCE: {
						mCurAdjustData.m_value = -0.1f + (progress * 2 / (float) maxP) * 0.1f;

						TongJi2.AddCountByRes(getContext(), R.integer.色调);
						adjustItem = new AdjustItem(AdjustItem.COLOR_BALANCE, mCurAdjustData.m_value);
						break;
					}
					case AdjustItem.WHITE_BALANCE:{
						mCurAdjustData.m_value = -0.1f + (progress * 2 / (float) maxP) * 0.1f;

						TongJi2.AddCountByRes(getContext(), R.integer.色温);
						adjustItem = new AdjustItem(AdjustItem.WHITE_BALANCE, mCurAdjustData.m_value);
						break;
					}

					case AdjustItem.HIGHLIGHT: {
						mCurAdjustData.m_value = ((progress / (float) maxP));

						TongJi2.AddCountByRes(getContext(), R.integer.高光);
						float finalValue = (1 - mCurAdjustData.m_value);
						finalValue = finalValue < 0 ? 0 : finalValue > 1 ? 1 : finalValue;
						adjustItem = new AdjustItem(AdjustItem.HIGHLIGHT, finalValue);
						break;
					}

					case AdjustItem.DARK_CORNER: {
						mCurAdjustData.m_value = ((progress / (float) maxP));

						TongJi2.AddCountByRes(getContext(), R.integer.暗角);
						adjustItem = new AdjustItem(AdjustItem.DARK_CORNER, mCurAdjustData.m_value);
						break;
					}

					case AdjustItem.SHADOW: {
						mCurAdjustData.m_value = ((progress / (float) maxP));

						TongJi2.AddCountByRes(getContext(), R.integer.阴影);
						adjustItem = new AdjustItem(AdjustItem.SHADOW, mCurAdjustData.m_value);
						break;
					}
					default:
						break;
				}
				if (adjustItem != null) {
					mHasVideoChange = true;
					mVideoWrapper.mVideoView.addAdjust(mCurSelVideoIndex, adjustItem);
				}
			}
		}
	};

	/**
	 * 重新计算每个视频的时长
	 */
	private void ComputePerVideo(ArrayList<VideoInfo> infos, int time)
	{
		if(infos != null && infos.size() > 0)
		{
			int size = infos.size();
			long perTime = time / size;
			ArrayList<VideoInfo> infos1 = new ArrayList<>();
			int time1 = 0;
			for(VideoInfo info : infos)
			{
				if(info.mDuration < perTime)
				{
					time1 += info.mDuration;
					info.SetStartTime(0);
					info.SetEndTime(info.mDuration);
				}
				else
				{
					infos1.add(info);
					info.SetStartTime(0);
					info.SetEndTime(perTime);
				}
			}
			if(time1 > 0)
			{
				int time2 = time - time1;
				ComputePerVideo(infos1, time2);
			}
		}
	}

//	private OnPauseListener mOnPauseListener = new OnPauseListener() {
//		@Override
//		public void onPause() {
//			if (mVideoEditFr.getVisibility() == VISIBLE && mVideoClipList != null) {
//				mVideoClipList.pauseVideo();
//			}
//		}
//
//		@Override
//		public void onResume() {
//			if (mVideoEditFr.getVisibility() == VISIBLE && mVideoClipList != null) {
//				mVideoClipList.resumeVideo();
//			}
//		}
//	};

//	private OnPlayListener mOnPlayListener = new OnPlayListener() {
//		@Override
//		public void onFirstStart() {
//
//		}
//
//		@Override
//		public void onRestart() {
//			if (mVideoEditFr.getVisibility() == VISIBLE && mVideoClipList != null) {
//				mVideoClipList.restartVideo();
//			}
//		}
//	};

	private boolean mCanTransitionClick = true;
//	private OnChangeTransitionListener mTransitionListener = new OnChangeTransitionListener()
//	{
//		@Override
//		public void onSeekToCompleted()
//		{
//			mCanTransitionClick = true;
//		}
//	};

	private boolean isChangingPos;		//是否正在执行改变顺序
//	private OnChangeVideoListener mChangeVideoListener = new OnChangeVideoListener()
//	{
//		@Override
//		public void onChangeCompleted()
//		{
//			isChangingPos = false;
//		}
//	};

	private void Reset()
	{
		if(mHasVideoChange)
		{
			int index = 0;
			mVideoInfos.clear();
			mVideoInfos.addAll(mTempInfos);
			mVideoWrapper.pauseAll();
			changeVideoNum();
			mVideoWrapper.resumeAll(true);
			for(VideoInfo info : mVideoInfos)
			{
				for(VideoResMgr.AdjustData data: info.m_adjustData)
				{
					AdjustItem adjustItem = new AdjustItem(data.m_type1, data.m_value);
					mVideoWrapper.mVideoView.addAdjust(index, adjustItem);
				}

				index ++;
			}
		}
		if(mHasTranChange)
		{
			mCanTransitionClick = false;
			int index = 0;
			mTrans.clear();
			mTrans.addAll(mTempTrans);
			for(TransitionDataInfo info : mTrans)
			{
				mVideoWrapper.mVideoView.setTransition(index, info.mID);
				index ++;
			}
		}
	}

	private void ClipVideos(boolean isRestart)
	{
		int index = 0;
		for(VideoInfo info : mVideoInfos)
		{
//			mVideoWrapper.mVideoView.clipVideo(index, info.GetStartTime(), info.GetEndTime());
			index++;
		}

		mVideoWrapper.resumeAll(isRestart);
	}


	protected synchronized Bitmap InitBkImg()
	{
		Bitmap bmp = mVideoWrapper.mVideoView.getFrame();
		return BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0xcc000000, 0x26000000);
	}

	boolean mIsPause = false;
	@Override
	public void onPause()
	{
//		mIsPause = mVideoWrapper.mVideoView.isPause();
		if(!mIsPause){
			mVideoWrapper.pauseAll();
		}
		TongJiUtils.onPagePause(getContext(), TAG);
	}

	@Override
	public void onResume()
	{
		for(int i = 0; i < mVideoInfos.size(); i ++){
			if(!FileUtil.isFileExists(mVideoInfos.get(i).mPath)){
				mHasVideoChange = true;
				mVideoInfos.remove(i);
				if(mTrans.size() > 0){
					if(i >= 0 && i < mTrans.size())
					{
						mTrans.remove(i);
					}
					else
					{
						mTrans.remove(i - 1);
					}
					deleteVideo(i);
//					mVideoClipList.deleteVideo(i);
					mVideoAdapter.removeItem(i);
					mCurSelVideoIndex = i < 0 ? 0 : i;
//					mVideoClipList.setSelectedIndex(i, 0);
				}

				i --;
			}
		}
		if(mVideoInfos.size() == 0){
			Toast.makeText(getContext(), "当前视频已经删除，请重新选择", Toast.LENGTH_SHORT).show();
			mSite.onBack(getContext());
		}
		if(!mIsPause){
			mVideoWrapper.resumeAll(false);
		}
//		mVideoWrapper.onResume();
		TongJiUtils.onPageResume(getContext(), TAG);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		return false;
	}

	@Override
	public void onClose()
	{
		if(mVideoClipList != null){
			mVideoClipList.releaseExecutor();
		}
		if(mVideoAdapter != null)
		{
			mVideoAdapter.release();
			mVideoAdapter = null;
		}
		if(mVideoList != null)
		{
			mVideoList.Clear();
			mVideoList.setAdapter(null, true);
			mVideoList = null;
		}
//		mVideoWrapper.mVideoView.removeOnPlayListener(mOnPlayListener);
//		mVideoWrapper.mVideoView.removeOnPauseListener(mOnPauseListener);
		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params)
	{
		if(siteID == SiteID.VIDEO_MOSAIC_HELP){
			if(!mIsPause){
				mVideoWrapper.resumeAll(false);
			}
			mHelpFlag = true;
		}
		if(siteID == SiteID.VIDEO_ALBUM && params != null)
		{
			Object obj = params.get("videos");
			if(obj != null)
			{
				ArrayList<VideoEntry> entries = (ArrayList<VideoEntry>)obj;
				boolean change = false;
				VideoInfo[] infos = new VideoInfo[entries.size()];
				int lastIndex = mVideoInfos.size();
				int index = 0;
				for(VideoEntry entry : entries)
				{
					change = true;
					VideoInfo info = new VideoInfo();
					info.mTimeType = mTimeUri;
					info.mHasEdit = true;
					info.mSelectStartTime = 0;
					info.mSelectEndTime = entry.mDuration;
					info.mDuration = entry.mDuration;
					info.mPath = info.mClipPath = entry.mOriginPath;
					mVideoInfos.add(info);

					infos[index] = info;

					TransitionDataInfo tran = new TransitionDataInfo();
					tran.mID = TransitionItem.NONE;
					tran.mRes = R.drawable.video_feature_clip_icon;
					mTrans.add(tran);

					index ++;
				}

				addVideo(infos);
				if(change)
				{
					mHasVideoChange = true;
					if(mVideoAdapter != null)
					{
						mVideoAdapter.SetItemDatas(mVideoInfos, mTrans);
						mVideoAdapter.notifyDataSetChanged();
						mVideoList.ScrollToCenter(lastIndex);
					}
//					mVideoWrapper.resumeAll(false);
				}
			}
		}
		if(siteID == SiteID.VIDEO_ALBUM){
			mVideoWrapper.hideProgressTip();
		}
	}

	private void changeVideoNum()
	{
		/*if(mVideoInfos != null)
		{
			List<PlayVideoInfo> playVideoInfos = new ArrayList<>();
			for(VideoInfo info : mVideoInfos)
			{
				playVideoInfos.add(DecodeUtils.getPlayVideoInfo(info.mClipPath));
			}
			mVideoWrapper.mVideoView.setVideoInfos(playVideoInfos);
		}*/
	}

	private void videoListAlpha(final View view){
		ValueAnimator alpha = ValueAnimator.ofFloat(1, 0, 1);
		alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float animatorValue = Float.valueOf(animation.getAnimatedValue() + "");
				view.setAlpha(animatorValue);
			}
		});
		alpha.setDuration(500);
		alpha.start();
	}

	private void deleteVideo(int index)
	{
		isChangingPos = true;
//		mVideoWrapper.mVideoView.deleteVideoInfo(index);
	}

	private void addVideo(VideoInfo... info)
	{
		isChangingPos = true;
//		mVideoWrapper.mVideoView.addVideoInfos(info);
//		mVideoWrapper.mIsPlaying = true;
	}


	/*
	 * videos ArrayList<VideoMosaicInfos>
	 * trans ArrayList<TransitionDataInfo> 没有就传空，用默认的
	 * @param params
	 *
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {
		// // MyBeautyStat.onPageStartByRes(R.string.视频片段页);
		mOkBtn = false;
		mHasBack = false;
		mHasVideoChange = false;
		if(params != null)
		{
			Object o = params.get("videos");
			if(o != null)
			{
				mVideoInfos = (ArrayList<VideoInfo>)o;
			}
			o = params.get("trans");
			if(o != null)
			{
				mTrans = (ArrayList<TransitionDataInfo>)o;
			}
			if(mVideoInfos == null)
			{
				mOkBtn = true;
//		mVideoWrapper.refreshWaterView();
				// // MyBeautyStat.onClickByRes(R.string.视频片段页_视频片段打钩);
				mSite.onBack(getContext());
			}
			if(mTrans == null)
			{
				mTrans = new ArrayList<>();
				int size = mVideoInfos.size() - 1;
				TransitionDataInfo info;
				for(int i = 0; i < size; i ++)
				{
					info = new TransitionDataInfo();
					info.mID = TransitionItem.NONE;
					info.mRes = R.drawable.video_feature_clip_icon;
					mTrans.add(info);
				}
			}

			InitEditList();

			if(mVideoAdapter != null)
			{
				mVideoList.setVisibility(VISIBLE);
				mVideoAdapter.SetItemDatas(mVideoInfos, mTrans);
				mVideoAdapter.notifyDataSetChanged();
			}
			if(mVideoInfos.size() > 10){
				mTimeList.setVisibility(GONE);
			}

			mLastVideoTime = mVideoWrapper.mVideoView.getTotalDuration();
//			mVideoWrapper.mVideoView.setOnChangeTransitionListener(mTransitionListener);
//			mVideoWrapper.mVideoView.setOnChangeVideoListener(mChangeVideoListener);

			mVideoWrapper.mActionBar.setActionbarTitleIconVisibility(View.VISIBLE);
			mVideoWrapper.mActionBar.setActionbarTitleClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(mHelpFlag)
					{
						mHelpFlag = false;
						HashMap<String, Object> params = new HashMap<>();
						Bitmap bk = InitBkImg();
						params.put("img", bk);
//						mIsPause = mVideoWrapper.mVideoView.isPause();
						if(!mIsPause){
							mVideoWrapper.pauseAll();
						}
						mSite.openMosaicHelp(getContext(), params);
					}
				}
			});

			if(TagMgr.CheckTag(getContext(), "first_enter_mosaic_help"))
			{
				postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if(mHelpFlag)
						{
							mHelpFlag = false;
//							mIsPause = mVideoWrapper.mVideoView.isPause();
							if(!mIsPause){
								mVideoWrapper.pauseAll();
							}
							HashMap<String, Object> params = new HashMap<>();
							Bitmap bk = InitBkImg();
							params.put("img", bk);
							mSite.openMosaicHelp(getContext(), params);
							TagMgr.SetTag(getContext(), "first_enter_mosaic_help");
						}
					}
				}, 400);
			}

		}

	}

	@Override
	public int getBottomPartHeight() {
		return PxToDpi_xhdpi(300);
	}

	@Override
	public void onBack() {
		if(m10ModeTip != null && m10ModeTip.isShowing())
		{
			m10ModeTip.dismiss();
			return;
		}
		if(mAdjustFr.getVisibility() == VISIBLE)
		{
			mBtnLst.onClick(mAdjustAwayBtn);
			return;
		}
		if(mVideoEditFr.getVisibility() == VISIBLE)
		{
			// // MyBeautyStat.onClickByRes(R.string.视频裁剪页_退回裁剪层);
			setTitle(R.string.footage);
			mVideoClipList.reset();
			AnimShowView(mVideoEditFr, false);
			mVideoShowFr.setVisibility(VISIBLE);
			mVideoWrapper.mActionBar.setRightImageBtnVisibility(View.VISIBLE);
			mVideoWrapper.mActionBar.setActionbarTitleIconVisibility(View.VISIBLE);
			mVideoList.setVisibility(VISIBLE);
			mVideoWrapper.hidePlayBtn();
			checkTransition();
			mVideoAdapter.notifyDataSetChanged();
//			mVideoWrapper.mVideoView.exitClipVideo(mCurSelVideoIndex);
			mVideoWrapper.resumeAll(true);
			return;
		}
		if(mTransitonFr.getVisibility() == VISIBLE)
		{
			mBtnLst.onClick(mAwayBtn);
			return;
		}
		if(!mHasBack && !mOkBtn){
//			Reset();
			mHasBack = true;
		}
		// // MyBeautyStat.onClickByRes(R.string.视频片段页_退出视频片段页);
		// // MyBeautyStat.onPageEndByRes(R.string.视频片段页);
		mVideoList.setVisibility(GONE);
		if(mVideoClipList != null){
			mVideoClipList.releaseExecutor();
			mEditFr.removeView(mVideoClipList);
			mVideoClipList = null;
		}
		mVideoWrapper.isCanSave = true;
		mVideoWrapper.isModify = true;
		mVideoWrapper.hasSave = true;
		mVideoWrapper.changeVideoTime(mHasVideoChange, mLastVideoTime, mVideoWrapper.mVideoView.getTotalDuration());
		mVideoWrapper.mActionBar.setActionbarTitleIconVisibility(View.GONE);
		mVideoWrapper.mActionBar.setActionbarTitleClickListener(null);
		return;
	}
}
