package cn.poco.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import cn.poco.MaterialMgr2.MgrUtils;
import cn.poco.MaterialMgr2.ThemeIntroPage;
import cn.poco.MaterialMgr2.ThemeItemInfo;
import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.MySeekBar2;
import cn.poco.beautify.page.MasterIntroPage;
import cn.poco.beautify.site.MasterIntroPageSite;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.draglistview.DragListView;
import cn.poco.draglistview.MyDragItemAdapter;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.FilterRes;
import cn.poco.resource.FilterResMgr2;
import cn.poco.resource.ResType;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.ThemeRes;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.Tags;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * Created by: fwc
 * Date: 2017/6/7
 */
public class FilterLayout extends FrameLayout {

	private Context mContext;

	/**
	 * 滤镜
	 */
	private List<DragListItemInfo> mFilterDatas;
	private DragListView mDragListView;
	private RecyclerView mRecyclerView;
	private MyDragItemAdapter mFilterAdapter;
	private MyDragItemAdapter.MyDragItem mDragItem; // 拖动的item
	private DefaultItemAnimator mDefaultItemAnimator;

	private LinearLayout mSeekBarLayout;
	private MySeekBar2 mSeekBar;
	private TextView mSeekBarTip;

	private LinearLayout mBottomLayout;
	private ImageView mDownView;

	private View mBottomBgView;

	private ThemeIntroPage mIntroPage;


	protected MasterIntroPage mMasterPage;

	/**
	 * 外面传进来要选中的滤镜
	 */
	private int mSelUri = -1;

	/**
	 * 当前选中的滤镜
	 */
	private int mCurFilterUri = -1;
	private FilterRes mFilterRes;

	/**
	 * 隐藏
	 */
	private LinearLayout mDeleteLayout;
	private ImageView mDeleteIcon;
	private TextView mDeleteText;

	/**
	 * 判断滤镜item是否在隐藏的区域
	 */
	private boolean isDragInHideArea = false;

	private boolean mUiEnable = true;

	private AnimatorSet mAnimatorSet;

	private OnFilterControlListener mControlListener;

	private boolean isOpenRecommend = false;
	private boolean isOpenMasterIntro = false;

	private Bitmap mOriginFilterImage;

	private int mFilterAlphaProgress;

	public FilterLayout(@NonNull Context context, List<DragListItemInfo> filterDatas) {
		super(context);

		mContext = context;
		mFilterDatas = filterDatas;

		initViews();
	}

	private void initViews() {

		LayoutParams params;

		mBottomBgView = new View(mContext);
		mBottomBgView.setBackgroundColor(0x66000000);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(222));
		params.gravity = Gravity.BOTTOM;
		addView(mBottomBgView, params);

		if (mFilterDatas != null && mFilterDatas.size() > 1) {
			// 绘制无滤镜时显示的图
			DragListItemInfo info = mFilterDatas.get(1);
			info.text_bg_color_out = info.text_bg_color_over = 0;
			info.m_name = "";

			int size = ShareData.PxToDpi_xhdpi(140);
			mOriginFilterImage = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(mOriginFilterImage);
			canvas.drawColor(0xff181818);
			Bitmap temp = BitmapFactory.decodeResource(getResources(), R.drawable.camera_filter_none);

			float left = (size - temp.getWidth()) / 2f;
			float top = (size - temp.getHeight()) / 2f;
			canvas.drawBitmap(temp, left, top, null);

			info.m_logo = mOriginFilterImage;
		}

		mDragItem = new MyDragItemAdapter.MyDragItem(mContext, new ImageView(mContext));

		mDragListView = new DragListView(mContext);
		mDragListView.setCustomDragItem(mDragItem);
		mRecyclerView = mDragListView.getRecyclerView();

		mRecyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if (mSelUri != -1) {
					DragListItemInfo info;
					int index = mFilterAdapter.GetIndexByUri(mSelUri);
					if (index != -1) {
						info = mFilterDatas.get(index);
						onItemClick(null, info, index, true);
					}
					mSelUri = -1;
				}
				mRecyclerView.removeOnLayoutChangeListener(this);
			}
		});
		mDragListView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
		mDragListView.setCanDragHorizontally(true);
		mDragListView.setDragListCallback(mDragControlCB);
		mDragListView.setDragListListener(mDragListener);

		params = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mDragListView, params);

		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(61);
		mRecyclerView.setLayoutParams(params);
		mRecyclerView.setItemAnimator(null);
		mDefaultItemAnimator = new DefaultItemAnimator();

		mFilterAdapter = new MyDragItemAdapter(mContext, true);
		mFilterAdapter.showTitle(true);
		mFilterAdapter.setItemList(mFilterDatas);
		mFilterAdapter.SetOnClickCallback(mOnItemClickListener);
		mDragListView.setAdapter(mFilterAdapter, true);

		mDeleteLayout = new LinearLayout(mContext);
		mDeleteLayout.setGravity(Gravity.CENTER);
		mDeleteLayout.setOrientation(LinearLayout.VERTICAL);
		mDeleteLayout.setVisibility(GONE);
		mDeleteLayout.setBackgroundResource(R.drawable.framework_hide_bg_out);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		mDragListView.addView(mDeleteLayout, 0, params);
		{
			LinearLayout.LayoutParams params1;
			mDeleteIcon = new ImageView(mContext);
			mDeleteIcon.setImageResource(R.drawable.framework_delete_icon_out);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			mDeleteLayout.addView(mDeleteIcon, params1);

			mDeleteText = new TextView(mContext);
			mDeleteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mDeleteText.setTextColor(Color.WHITE);
			mDeleteText.setText(R.string.Delete);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params1.topMargin = ShareData.PxToDpi_xhdpi(20);
			mDeleteLayout.addView(mDeleteText, params1);
		}

		mSeekBarLayout = new LinearLayout(mContext);
		mSeekBarLayout.setOrientation(LinearLayout.VERTICAL);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(150);
		mSeekBarLayout.setLayoutParams(params);
		addView(mSeekBarLayout);
		{
			LinearLayout.LayoutParams params1;
			mSeekBarTip = new TextView(getContext());
			mSeekBarTip.setMaxLines(1);
			mSeekBarTip.setText("0");
			mSeekBarTip.setTextColor(Color.WHITE);
			mSeekBarTip.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.gravity = Gravity.START;
			params1.leftMargin = ShareData.PxToDpi_xhdpi(21);
			mSeekBarLayout.addView(mSeekBarTip, params1);

			mSeekBar = new MySeekBar2(getContext());
			mSeekBar.setMax(12);
			mSeekBar.SetDotNum(13);
			mSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
			mSeekBar.setProgress(0);
			params1 = new LinearLayout.LayoutParams(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40), LinearLayout.LayoutParams.WRAP_CONTENT);
			params1.topMargin = ShareData.PxToDpi_xhdpi(10);
			mSeekBarLayout.addView(mSeekBar, params1);
		}

		mSeekBarLayout.setVisibility(INVISIBLE);

		mBottomLayout = new LinearLayout(mContext);
		mBottomLayout.setOrientation(LinearLayout.VERTICAL);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(61));
		params.gravity = Gravity.BOTTOM;
		addView(mBottomLayout, params);
		{
			View line = new View(mContext);
			line.setBackgroundColor(0xff272727);
			LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
			params1.topMargin = ShareData.PxToDpi_xhdpi(20);
			mBottomLayout.addView(line, params1);

			FrameLayout frameLayout = new FrameLayout(mContext);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(40));
			mBottomLayout.addView(frameLayout, params1);
			{
				mDownView = new ImageView(mContext);
				mDownView.setScaleType(ImageView.ScaleType.CENTER);
				mDownView.setImageResource(R.drawable.beautify_effect_help_down);
				mDownView.setPadding(ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20), 0);
				params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				params.gravity = Gravity.CENTER_HORIZONTAL;
				frameLayout.addView(mDownView, params);
				mDownView.setOnClickListener(mOnClickListener);
			}
		}
	}

	public void setOnFilterControlListener(OnFilterControlListener controlListener) {
		mControlListener = controlListener;
	}

	/**
	 * 设置选中的滤镜
	 *
	 * @param selUri 选中滤镜的uri
	 */
	public void setSelUri(int selUri) {
		mSelUri = selUri;
	}

	public void setCurFilterUri(int curFilterUri) {
		mCurFilterUri = curFilterUri;
	}

	public void setCurFilterRes(FilterRes filterRes) {
		mFilterRes = filterRes;
	}

	/**
	 * 用于判断滤镜item是否可以拖动
	 */
	private DragListView.DragListCallback mDragControlCB = new DragListView.DragListCallback() {

		@Override
		public boolean canDragItemAtPosition(int dragPosition) {
			/*if (mFilterDatas != null && dragPosition >= 0 && dragPosition < mFilterDatas.size()) {
				return mFilterDatas.get(dragPosition).m_canDrag;
			}*/
			return false;
		}

		@Override
		public boolean canDropItemAtPosition(int dropPosition) {
			/*if (mFilterDatas != null && dropPosition >= 0 && dropPosition < mFilterDatas.size()) {
				return mFilterDatas.get(dropPosition).m_canDrop;
			}*/
			return false;
		}
	};

	/**
	 * 拖动事件监听回调
	 */
	private DragListView.DragListListener mDragListener = new DragListView.DragListListener() {

		@Override
		public void onItemDragStarted(int position) {
			isDragInHideArea = false;
			mRecyclerView.setItemAnimator(mDefaultItemAnimator);
			mDragListView.setBackgroundColor(0xb2000000);

			FilterRes filterRes = (FilterRes)mFilterDatas.get(position).m_ex;
			if (filterRes.m_type != BaseRes.TYPE_LOCAL_RES) {
				Utils.AlphaAnim(mDeleteLayout, true, 400);
			}
		}

		@Override
		public void onItemDragging(int itemPosition, float x, float y) {
			boolean lastChoose = isDragInHideArea;
			RectF rectF = new RectF(mDeleteLayout.getLeft(), mDeleteLayout.getTop(), mDeleteLayout.getRight(), mDeleteLayout.getBottom());
			if (x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom) {
				isDragInHideArea = true;
				if (mDragItem != null) {
					mDragItem.DoAlphaAnim(true);
				}
			} else {
				isDragInHideArea = false;
				if (mDragItem != null) {
					mDragItem.DoAlphaAnim(false);
				}
			}
			if (lastChoose == !isDragInHideArea) {
				if (isDragInHideArea) {
					mDeleteLayout.setBackgroundResource(R.drawable.framework_hide_bg_over);
					mDeleteIcon.setImageResource(R.drawable.framework_delete_icon_over);
					mDeleteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				} else {
					mDeleteLayout.setBackgroundResource(R.drawable.framework_hide_bg_out);
					mDeleteIcon.setImageResource(R.drawable.framework_delete_icon_out);
					mDeleteText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				}
			}
		}

		@Override
		public void onItemDragEnded(int fromPosition, int toPosition) {
			mRecyclerView.setItemAnimator(null);
			mDragListView.setBackgroundColor(0x00000000);
			if (mDeleteLayout.getVisibility() == View.VISIBLE) {
				Utils.AlphaAnim(mDeleteLayout, false, 400);
			}
			if (isDragInHideArea) {
				onHideItem(fromPosition);

				mFilterAdapter.removeItem(toPosition);
			} else if (fromPosition != toPosition) {
				onChangeItem(fromPosition, toPosition);
			}
		}
	};

	/**
	 * 隐藏item
	 *
	 * @param position item的位置
	 */
	private void onHideItem(int position) {
		DragListItemInfo itemInfo = mFilterDatas.remove(position);

		if (itemInfo != null) {
			FilterRes res = (FilterRes)itemInfo.m_ex;
			if (res.m_type != BaseRes.TYPE_LOCAL_RES) {

				res.m_isHide = !res.m_isHide;
				FilterResMgr2.getInstance().DeleteRes(getContext(), res);

				if (mCurFilterUri == res.m_id) {
					setSelItemByUri(DragListItemInfo.URI_ORIGIN);
					if (mSeekBarLayout.getVisibility() == View.VISIBLE) {
						hideSeekBar();
					}
				}

				MyBeautyStat.onClickByRes(R.string.滤镜列表页_删除滤镜);

				MyBeautyStat.onDeleteMaterial(String.valueOf(res.m_id), R.string.滤镜列表页);
			}
		}
	}

	/**
	 * 交换item的顺序
	 *
	 * @param fromPosition item的位置
	 * @param toPosition   item的位置
	 */
	private void onChangeItem(int fromPosition, int toPosition) {

		DragListItemInfo itemInfo = mFilterDatas.get(fromPosition);
		int fromPos = fromPosition;
		int toPos = toPosition;
		if (itemInfo != null) {
			fromPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		itemInfo = mFilterDatas.get(toPosition);
		if (itemInfo != null) {
			toPos = ResourceUtils.HasId(FilterResMgr2.getInstance().GetOrderArr(), itemInfo.m_uri);
		}
		ResourceUtils.ChangeOrderPosition(FilterResMgr2.getInstance().GetOrderArr(), fromPos, toPos);
		FilterResMgr2.getInstance().SaveOrderArr();
		if (mFilterDatas != null && mFilterDatas.size() > fromPosition && mFilterDatas.size() > toPosition) {
			itemInfo = mFilterDatas.remove(fromPosition);
			mFilterDatas.add(toPosition, itemInfo);
		}
	}

	private SeekBar.OnSeekBarChangeListener mSeekBarListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			updateSeekBarTip(progress, seekBar.getMax());
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

			if (mUiEnable || mControlListener != null) {
				mFilterAlphaProgress = seekBar.getProgress();
				int max = seekBar.getMax();
				mControlListener.adjustFilterAlpha(mFilterAlphaProgress * 1f / max);
			}

		}
	};

	/**
	 * 更新seekBar文字提示
	 *
	 * @param progress 进度
	 * @param max      最大值
	 */
	private void updateSeekBarTip(int progress, int max) {
		int w = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(40);
		int leftMargin = ShareData.PxToDpi_xhdpi(20);
		int seekBarThumbW = ShareData.PxToDpi_xhdpi(21);
		LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)mSeekBarTip.getLayoutParams();
		params1.leftMargin = (int)((w - (seekBarThumbW << 1)) * progress / (float)max + leftMargin + seekBarThumbW - ShareData.PxToDpi_xhdpi(35));
		mSeekBarTip.setLayoutParams(params1);

		String tip;
		if (progress > 0) {
			tip = "+" + progress;
		} else if (progress < 0) {
			tip = "" + progress;
		} else {
			tip = " " + progress;
		}
		mSeekBarTip.setText(tip);
	}

	/**
	 * 滤镜item单击
	 */
	private MyDragItemAdapter.OnItemClickListener mOnItemClickListener = new MyDragItemAdapter.OnItemClickListener() {

		@Override
		public void OnItemClick(View view, DragListItemInfo info, int index) {
			onItemClick(view, info, index, false);
		}

		@Override
		public void OnHeadClick(View view, DragListItemInfo info, int index) {
			onHeadClick(view, info, index);
		}
	};

	/**
	 * 缩略图列表每个item的点击事件
	 *
	 * @param view  view对象
	 * @param info  item信息
	 * @param index item下标
	 */
	public void onItemClick(View view, final DragListItemInfo info, final int index, boolean scroll) {

		if (scroll || mUiEnable) {

			if (info.m_isLock) {
				if (mCurFilterUri != info.m_uri && mCurFilterUri != DragListItemInfo.URI_ORIGIN) {
					setSelItemByUri(DragListItemInfo.URI_ORIGIN);
				}

				onHeadClick(view, info, index);
				return;
			}
			if (info.m_uri == DragListItemInfo.URI_MGR) {

				if (mControlListener != null) {
					mControlListener.onDownloadMore();
				}
				return;
			}
			switch (info.m_style) {

				case NORMAL: { // 可以正常使用的

					post(new Runnable() {
						@Override
						public void run() {
							// 有延迟
							if (mDragListView != null) {
								mDragListView.ScrollToCenter(index);
							}
						}
					});

					if (mCurFilterUri != info.m_uri) {
						setSelItemByUri(info.m_uri);

						if (mFilterAdapter != null) {
							post(new Runnable() {
								@Override
								public void run() {
									if (mFilterAdapter != null) {
										mFilterAdapter.SetSelByUri(info.m_uri);
									}
								}
							});
						}

						if (info.m_uri == DragListItemInfo.URI_ORIGIN && mSeekBarLayout.getVisibility() == VISIBLE) {
							hideSeekBar();
						}
					} else if (info.m_uri != DragListItemInfo.URI_ORIGIN && view != null) { // 如果选中不是原图（无滤镜效果）

						if (mSeekBarLayout.getVisibility() != VISIBLE) {
							updateSeekBarTip(mFilterAlphaProgress, mSeekBar.getMax());
							mSeekBar.setProgress(mFilterAlphaProgress);
							showSeekBar();
						} else {
							hideSeekBar();
						}
					}
					mCurFilterUri = info.m_uri;
					break;
				}
				case NEED_DOWNLOAD: { // 需要下载的
					if (info.m_ex instanceof ThemeRes) {
						HashMap<String, Object> params = new HashMap<>();
						ThemeItemInfo itemInfo = MgrUtils.GetThemeItemInfoByUri(getContext(), ((ThemeRes)info.m_ex).m_id, ResType.FILTER);
						params.put("data", itemInfo);
						if (view != null) {
							int[] location = new int[2];
							view.getLocationOnScreen(location);

							params.put("hasAnim", true);
							params.put("centerX", location[0]);
							params.put("centerY", location[1]);
							params.put("viewH", view.getHeight());
							params.put("viewW", view.getWidth());
						}
						openRecommend(params);
					}
				}
				default:
					break;
			}
		}
	}

	/**
	 * 显示seekBar
	 */
	private void showSeekBar() {

		if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
			return;
		}

		float downY = ShareData.PxToDpi_xhdpi(100);
		mAnimatorSet = new AnimatorSet();
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mRecyclerView, "translationY", 0, downY);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0, downY);
		mAnimatorSet.setDuration(350);
		mAnimatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mSeekBarLayout.setVisibility(VISIBLE);
			}
		});
		mAnimatorSet.playTogether(animator1, animator2);
		mAnimatorSet.start();

		MyBeautyStat.onClickByRes(R.string.滤镜列表页_滤镜不透明度);
	}

	/**
	 * 隐藏seekBar
	 */
	private void hideSeekBar() {
		if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
			return;
		}

		mSeekBarLayout.setVisibility(GONE);

		float upY = mRecyclerView.getTranslationY();
		mAnimatorSet = new AnimatorSet();
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(mRecyclerView, "translationY", upY, 0);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(mBottomLayout, "translationY", upY, 0);
		mAnimatorSet.setDuration(350);
		mAnimatorSet.playTogether(animator1, animator2);
		mAnimatorSet.start();
	}

	/**
	 * 重置位置
	 */
	public void resetPosition() {
		mSeekBarLayout.setVisibility(GONE);
		mRecyclerView.setTranslationY(0);
		mBottomLayout.setTranslationY(0);
	}

	/**
	 * 打开推荐位
	 *
	 * @param params 参数
	 */
	private void openRecommend(HashMap<String, Object> params) {

		isOpenRecommend = true;

		if (mIntroPage != null) {
			removeView(mIntroPage);
			mIntroPage.onClose();
			mIntroPage = null;
		}
		mIntroPage = new ThemeIntroPage(getContext(), mIntroSite);
		mIntroPage.SetData(params);
		addView(mIntroPage);

		if (mControlListener != null) {
			mControlListener.setPopupPage(true);
		}
	}

	private ThemeIntroPageSite2 mIntroSite = new ThemeIntroPageSite2() {

		@Override
		public void OnBack(HashMap<String, Object> params,Context context) {
			if (mIntroPage != null) {
				FilterLayout.this.removeView(mIntroPage);
				mIntroPage.onClose();
				mIntroPage = null;
			}

			isOpenRecommend = false;

			onPageResult(SiteID.THEME_INTRO_PAGE, params);

			if (mControlListener != null) {
				mControlListener.setPopupPage(false);
			}
		}

		@Override
		public void OnResourceUse(HashMap<String, Object> params,Context context) {
			super.OnResourceUse(params,context);
		}
	};

	public void onPageResult(int siteID, HashMap<String, Object> params) {

		if (siteID == SiteID.THEME_INTRO_PAGE || siteID == SiteID.THEME_PAGE) {
			if (params != null) {
				int sel_uri = -1;
				if (params.containsKey("id")) {
					sel_uri = (Integer)params.get("id");
				}
//				boolean needRefresh = false;
//				if (params.containsKey("need_refresh")) {
//					needRefresh = (Boolean)params.get("need_refresh");
//				}
//				if (needRefresh) {
//					updateListDatas();
//				}
				updateListDatas();

				if (sel_uri != -1) {
					DragListItemInfo info;
					int index = mFilterAdapter.GetIndexByUri(sel_uri);
					if (index != -1) {
						mUiEnable = true;
						info = mFilterDatas.get(index);
						onItemClick(null, info, index, false);
					}
				}

				if (mFilterAdapter != null) {
					int index = mFilterAdapter.GetIndexByUri(mCurFilterUri);
					mFilterAdapter.SetSelByIndex(index);
					if (index == -1) {
						setSelItemByUri(DragListItemInfo.URI_ORIGIN);
					}
				}

				if (mCurFilterUri == DragListItemInfo.URI_ORIGIN) {
					if (mSeekBarLayout.getVisibility() == View.VISIBLE) {
						hideSeekBar();
					}
				}
			}
		}
	}

	/**
	 * 更新滤镜列表数据
	 */
	private void updateListDatas() {

		if (mFilterDatas != null) {
			mFilterDatas.clear();
			mFilterDatas.addAll(BeautifyResMgr.GetFilterRess(mContext, false));
			if (mFilterDatas.size() > 1) {
				DragListItemInfo info = mFilterDatas.get(1);
				info.text_bg_color_out = info.text_bg_color_over = 0;
				info.m_name = "";
				info.m_logo = mOriginFilterImage;
			}
			if (mFilterAdapter != null) {
				mFilterAdapter.ReleaseMem();
				mFilterAdapter.setItemList(mFilterDatas);
				mFilterAdapter.SelCurUri();
				mFilterAdapter.notifyDataSetChanged();

				int index = mFilterAdapter.GetIndexByUri(mCurFilterUri);
				if (index < 0) {
					// 找不到
					if (mSeekBarLayout.getVisibility() == VISIBLE) {
						hideSeekBar();
					}
					if (mControlListener != null) {
						mControlListener.onSelectFilter(null, DragListItemInfo.URI_ORIGIN, 0);
					}
				}
			}
		}
	}

	/**
	 * 选中某滤镜
	 *
	 * @param uri 滤镜uri
	 */
	private void setSelItemByUri(int uri) {

		FilterRes filterRes = null;
		int index = -1;
		if (mFilterDatas != null) {
			int size = mFilterDatas.size();
			for (int i = 0; i < size; i++) {
				if (uri == mFilterDatas.get(i).m_uri) {
					filterRes = (FilterRes)mFilterDatas.get(i).m_ex;
					mFilterRes = filterRes;
					index = i;
					break;
				}
			}
		}

		if (filterRes != null) {
			mFilterAlphaProgress = (int)((filterRes.m_alpha / 100f) * mSeekBar.getMax() + 0.5);
			updateSeekBarTip(mFilterAlphaProgress, mSeekBar.getMax());
			mSeekBar.setProgress(mFilterAlphaProgress);
		}

		if (mControlListener != null) {
			mControlListener.onSelectFilter(filterRes, uri, index);
		}

		mCurFilterUri = uri;
	}

	/**
	 * 缩略图列表每个item头像的点击事件
	 *
	 * @param view  view对象
	 * @param info  item信息
	 * @param index item下标
	 */
	private void onHeadClick(View view, DragListItemInfo info, int index) {

		if (mUiEnable) {

			if (!info.m_isLock) {
				if (mCurFilterUri != info.m_uri) {
					onItemClick(view, info, index, false);
					return;
				}
			} else {
				if (mFilterAdapter != null) {
					mFilterAdapter.SetSelByUri(DragListItemInfo.URI_NONE);
				}
			}

			HashMap<String, Object> params = new HashMap<>();
			params.put("head_img", info.m_head);
			FilterRes data = (FilterRes)info.m_ex;
			params.put("top_img", data.m_coverImg);
			params.put("name", data.m_authorName);
			params.put("detail", data.m_authorInfo);
			params.put("intro", data.m_filterDetail);
			params.put("img_url", data.m_filterIntroUrl);
			if (info.m_isLock) {
				params.put("unlock_tag", Tags.UNLOCK_FILTER);
				params.put("lock", true);
				params.put("filter_id", info.m_uri);
			}
			params.put("share_url", data.m_shareUrl);
			params.put("share_title", data.m_shareTitle);

			if (view != null) {
				int[] location = new int[2];
				view.getLocationOnScreen(location);

				params.put("centerX", location[0]);
				params.put("centerY", location[1]);
				params.put("viewH", view.getHeight());
				params.put("viewW", view.getWidth());
			}

			openMasterPage(params);

			mCurFilterUri = info.m_uri;
		}
	}

	/**
	 * 打开大师介绍页面
	 *
	 * @param params 参数
	 */
	private void openMasterPage(HashMap<String, Object> params) {

		isOpenMasterIntro = true;

		if (mMasterPage != null) {
			removeView(mMasterPage);
			mMasterPage.onClose();
			mMasterPage = null;
		}
		mMasterPage = new MasterIntroPage(getContext(), mMasterSite);
		mMasterPage.SetData(params);
		addView(mMasterPage);

		if (mControlListener != null) {
			mControlListener.setPopupPage(true);
		}

		MyBeautyStat.onClickByRes(R.string.滤镜列表页_打开大师介绍页);
		MyBeautyStat.onPageStartByRes(R.string.镜头_大师介绍页);
	}

	private MasterIntroPageSite mMasterSite = new MasterIntroPageSite() {

		@Override
		public void onBack(HashMap<String, Object> params,Context context) {

			isOpenMasterIntro = false;

			if (mMasterPage != null) {
				FilterLayout.this.removeView(mMasterPage);
				mMasterPage.onClose();
				mMasterPage = null;
			}
			if (params != null) {

				int id = -1;
				boolean lock = true;
				if (params.get("id") != null) {
					id = (Integer)params.get("id");
				}
				if (params.get("lock") != null) {
					lock = (Boolean)params.get("lock");
				}
				if (id != -1 && !lock) {
					if (mFilterAdapter != null) {
						DragListItemInfo info;
						mFilterAdapter.Unlock(id);
						int index = mFilterAdapter.GetIndexByUri(id);
						if (index != -1) {
							info = mFilterDatas.get(index);
							onItemClick(null, info, index, false);
						}
					}
				}
			}

			if (mControlListener != null) {
				mControlListener.setPopupPage(false);
			}

			MyBeautyStat.onClickByRes(R.string.大师介绍页_退出大师介绍页);
			MyBeautyStat.onPageEndByRes(R.string.镜头_大师介绍页);
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mMasterPage != null) {
			mMasterPage.onActivityResult(requestCode, resultCode, data);
		}
		if (mIntroPage != null) {
			mIntroPage.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		if (mFilterDatas != null) {
			mFilterDatas.clear();
			mFilterDatas = null;
			mFilterAdapter.notifyDataSetChanged();
		}
		if (mFilterAdapter != null) {
			mFilterAdapter.ClearAll();
			mFilterAdapter = null;
		}
		if (mDragListView != null) {
			mDragListView.Clear();
			mDragListView.setAdapter(null, true);
			mDragListView.removeAllViews();
			mDragListView = null;
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!mUiEnable || mControlListener == null) {
				return;
			}

			if (v == mDownView) {
				mControlListener.onClickDown();
			}
		}
	};

	public boolean onBack() {

		if (isOpenRecommend) {
			mIntroSite.OnBack(null,getContext());
			return true;
		} else if (isOpenMasterIntro) {
			mMasterSite.onBack(null,getContext());
			return true;
		}
		return false;
	}

	public void setUiEnable(boolean uiEnable) {
		mUiEnable = uiEnable;
	}

	public interface OnFilterControlListener {
		void onDownloadMore();

		void onClickDown();

		void onSelectFilter(FilterRes filterRes, int uri, int index);

		void adjustFilterAlpha(float alpha);

		void setPopupPage(boolean isPopup);
	}
}
