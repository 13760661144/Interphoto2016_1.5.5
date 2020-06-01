package cn.poco.album2;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.adapter.FolderAdapter;
import cn.poco.album2.adapter.LocalAlbumAdapter;
import cn.poco.album2.adapter.PhotoPagerAdapter;
import cn.poco.album2.adapter.SystemAlbumAdapter;
import cn.poco.album2.dialog.MemoryTipDialog;
import cn.poco.album2.dialog.PasteTipDialog;
import cn.poco.album2.model.FolderInfo;
import cn.poco.album2.model.MemoryInfo;
import cn.poco.album2.site.AlbumSite;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.album2.utils.FileUtils;
import cn.poco.album2.utils.PhotoUtils;
import cn.poco.album2.utils.SDCardUtils;
import cn.poco.album2.utils.T;
import cn.poco.album2.view.LocalAlbumView;
import cn.poco.album2.view.MyViewPager;
import cn.poco.album2.view.ProgressView;
import cn.poco.album2.view.SelectEffectView;
import cn.poco.album2.view.SystemAlbumView;
import cn.poco.album2.view.TabIndicator;
import cn.poco.beautify.BeautifyHandler;
import cn.poco.beautify.DeleteDlg;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.SiteID;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.statistics.TongJiUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

import static cn.poco.beautify.BeautifyHandler.SaveExifInfoToImg;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class AlbumPage extends IPage implements View.OnClickListener {

	private static final String TAG = "相册";

	private static final int COLOR_GRAY = 0xffaaaaaa;

	/**
	 * 用于判断当前是否是复制了滤镜效果
	 */
	public static boolean sCopy;
	public static CopyEffect sCopyEffect;

	public static int sLastFolderIndex;

	/**
	 * 标记是否是本地相册
	 * true: 本地相册
	 * false: 系统相册
	 */
	public static boolean sLocalAlbum;

	public static Position sSystemPosition;
	public static Position sLocalPosition;

	private Context mContext;
	private AlbumSite mSite;

	private FrameLayout mTopLayout;
	private ImageView mBackView;
	private TextView mSelectText;

	private MyViewPager mViewPager;
	private SystemAlbumView mSystemAlbumView;
	private LocalAlbumView mLocalAlbumView;

	private LinearLayout mBottomLayout;
	private FrameLayout mSystemTab;
	private TextView mSystemAlbumName;
	private ImageView mFolderIndicator;
	private FrameLayout mInterPhotoTab;
	private TextView mInterPhotoName;
	private TabIndicator mTabIndicator;

	private FrameLayout mSelectTopBar;
	private TextView mSelectTitle;
	private TextView mSelectCancel;

	private FrameLayout mSelectBottomBar;
	private SelectEffectView mCopyEffect;
	private SelectEffectView mPasteEffect;
	private ImageView mDeleteView;
	private ImageView mSaveView;

	private View mBackground;
	private RecyclerView mFolderList;

	private int mTopHeight;
	private int mBottomHeight;

	private int mCurrentPosition = 0;

	private int mMode = NORMAL;
	private static final int NORMAL = 0;
	private static final int SELECT = 1;
	private static final int FOLDER = 2;

	private PhotoStore mPhotoStore;

	private int mFolderIndex;
	private FolderAdapter mFolderAdapter;

	private boolean mUiEnabled = true;

	private boolean mSingleSelect = false;

	/**
	 * 解决重复选择的问题
	 */
	private boolean hasSelected = false;

	/**
	 * 删除时弹框
	 */
	private DeleteDlg mDeleteDlg;
	private DeleteDlg mDeleteDlg1;

	private DeleteInSystemTask mDeleteInSystemTask;
	private DeleteInAlbumTask mDeleteInAlbumTask;
	private List<Integer> mIndexList = new ArrayList<>();

	private ProgressDialog mDeleteDialog;

	private boolean isRunningAnimation = false;

	private Dialog mDialog;
	private ProgressView mProgressView;
	private SaveTask mSaveTask;

	private List<ImageStore.ImageInfo> mPasteImageInfos = new ArrayList<>();
	private List<ImageStore.ImageInfo> mPasteResultInfos = new ArrayList<>();
	private PasteTask mPasteTask;
	private PasteTask2 mPasteTask2;

	/**
	 * 将多选按钮置灰
	 */
	private boolean mHideChoose = false;

	/**
	 * 用于标记左右切换（隐藏CacheView）
	 */
	private boolean mChangePosition;

	private MemoryTipDialog mMemoryTipDialog;

	public AlbumPage(Context context, BaseSite site) {
		super(context, site);

		mContext = context;
		mSite = (AlbumSite) site;

		TongJiUtils.onPageStart(getContext(), TAG);

		init();
	}

	private void init() {
		mTopHeight = mBottomHeight = ShareData.PxToDpi_xhdpi(80);

		mPhotoStore = PhotoStore.getInstance(mContext);
		mPhotoStore.clearCache();
		LocalStore.init(mContext);

		initViews();
	}

	private void initViews() {

		setBackgroundColor(0xcc000000);

		LayoutParams params;

		mTopLayout = new FrameLayout(mContext);
		mTopLayout.setOnClickListener(this);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		addView(mTopLayout, params);
		{
			mBackView = new ImageView(mContext);
			mBackView.setImageResource(R.drawable.framework_back_btn);
			mBackView.setOnClickListener(this);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_VERTICAL;
			mTopLayout.addView(mBackView, params);

			mSelectText = new TextView(mContext);
			mSelectText.setTextColor(Color.WHITE);
			mSelectText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mSelectText.setText(R.string.select);
			mSelectText.setPadding(ShareData.PxToDpi_xhdpi(40), 0, ShareData.PxToDpi_xhdpi(40), 0);
			mSelectText.setGravity(Gravity.CENTER);
			mSelectText.setOnClickListener(this);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.END;
			mTopLayout.addView(mSelectText, params);
		}

		mSelectTopBar = new FrameLayout(mContext);
		mSelectTopBar.setClickable(true);
		mSelectTopBar.setVisibility(INVISIBLE);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mTopHeight);
		addView(mSelectTopBar, params);
		{
			mSelectTitle = new TextView(mContext);
			mSelectTitle.setTextColor(Color.WHITE);
			mSelectTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			mSelectTopBar.addView(mSelectTitle, params);

			mSelectCancel = new TextView(mContext);
			mSelectCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			mSelectCancel.setTextColor(Color.WHITE);
			mSelectCancel.setPadding(ShareData.PxToDpi_xhdpi(40), 0, ShareData.PxToDpi_xhdpi(40), 0);
			mSelectCancel.setText(R.string.Cancel);
			mSelectCancel.setGravity(Gravity.CENTER);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.END;
			mSelectTopBar.addView(mSelectCancel, params);
			mSelectCancel.setOnClickListener(this);
		}

		mViewPager = new MyViewPager(mContext);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.topMargin = params.bottomMargin = mTopHeight;
		addView(mViewPager, params);

		mSystemAlbumView = new SystemAlbumView(mContext, mSite);
		mLocalAlbumView = new LocalAlbumView(mContext, mSite);
		List<View> views = new ArrayList<>();
		views.add(mSystemAlbumView);
		views.add(mLocalAlbumView);

		mViewPager.setAdapter(new PhotoPagerAdapter(views));
		mViewPager.addOnPageChangeListener(mOnPageChangeListener);

		mBackground = new View(mContext);
		mBackground.setOnClickListener(this);
		mBackground.setBackgroundColor(0xcc000000);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		params.bottomMargin = mBottomHeight;
		addView(mBackground, params);
		mBackground.setVisibility(INVISIBLE);

		mFolderList = new RecyclerView(mContext);
		mFolderList.setHasFixedSize(true);
		mFolderList.setBackgroundColor(Color.BLACK);
		mFolderList.setOverScrollMode(OVER_SCROLL_NEVER);
		mFolderList.setLayoutManager(new LinearLayoutManager(mContext));
		mFolderList.setPadding(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(20));
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(530), Gravity.BOTTOM);
		params.bottomMargin = mBottomHeight;
		addView(mFolderList, params);
		mFolderList.setVisibility(INVISIBLE);

		mBottomLayout = new LinearLayout(mContext);
		mBottomLayout.setBackgroundColor(0xcc000000);
		mBottomLayout.setOrientation(LinearLayout.VERTICAL);
		mBottomLayout.setClickable(true);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomHeight);
		params.gravity = Gravity.BOTTOM;
		addView(mBottomLayout, params);
		{
			LinearLayout.LayoutParams params1;

			LinearLayout layout = new LinearLayout(mContext);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(76));
			mBottomLayout.addView(layout, params1);
			{
				mSystemTab = new FrameLayout(mContext);
				mSystemTab.setOnClickListener(this);
				params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
				layout.addView(mSystemTab, params1);
				{
					LinearLayout layout1 = new LinearLayout(mContext);
					layout1.setOrientation(LinearLayout.HORIZONTAL);
					params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
					mSystemTab.addView(layout1, params);
					{
						mSystemAlbumName = new TextView(mContext);
						mSystemAlbumName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
						mSystemAlbumName.setTextColor(Color.WHITE);
						mSystemAlbumName.setText(R.string.albums);
						params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						params1.gravity = Gravity.CENTER_VERTICAL;
						layout1.addView(mSystemAlbumName, params1);

						mFolderIndicator = new ImageView(mContext);
						mFolderIndicator.setImageResource(R.drawable.album_ic_up);
						params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
						params1.leftMargin = params1.rightMargin = ShareData.PxToDpi_xhdpi(14);
						params1.gravity = Gravity.CENTER_VERTICAL;
						layout1.addView(mFolderIndicator, params1);
					}
				}

				mInterPhotoTab = new FrameLayout(mContext);
				mInterPhotoTab.setOnClickListener(this);
				params1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
				layout.addView(mInterPhotoTab, params1);
				{
					mInterPhotoName = new TextView(mContext);
					mInterPhotoName.setText(R.string.interphoto);
					mInterPhotoName.setTextColor(0xffaaaaaa);
					mInterPhotoName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
					mInterPhotoTab.addView(mInterPhotoName, params);
				}
			}

			mTabIndicator = new TabIndicator(mContext);
			params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(4));
			mBottomLayout.addView(mTabIndicator, params1);
		}

		mSelectBottomBar = new FrameLayout(mContext);
		mSelectBottomBar.setBackgroundColor(0xcc000000);
		mSelectBottomBar.setClickable(true);
		mSelectBottomBar.setVisibility(INVISIBLE);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mBottomHeight, Gravity.BOTTOM);
		addView(mSelectBottomBar, params);
		{
			LinearLayout layout = new LinearLayout(mContext);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.leftMargin = ShareData.PxToDpi_xhdpi(10);
			mSelectBottomBar.addView(layout, params);
			{
				LinearLayout.LayoutParams params1;

				mCopyEffect = new SelectEffectView(mContext);
				mCopyEffect.setText(R.string.Copy);
				mCopyEffect.getTextView().setOnClickListener(this);
				params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				params1.leftMargin = ShareData.PxToDpi_xhdpi(20);
				layout.addView(mCopyEffect, params1);

				mPasteEffect = new SelectEffectView(mContext);
				mPasteEffect.setText(R.string.Paste);
				mPasteEffect.getTextView().setOnClickListener(this);
				params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				params1.leftMargin = ShareData.PxToDpi_xhdpi(20);
				layout.addView(mPasteEffect, params1);
			}

			mDeleteView = new ImageView(mContext);
			mDeleteView.setImageResource(R.drawable.album_delete_disable);
			mDeleteView.setOnClickListener(this);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params.rightMargin = ShareData.PxToDpi_xhdpi(30);
			mSelectBottomBar.addView(mDeleteView, params);

			mSaveView = new ImageView(mContext);
			mSaveView.setImageResource(R.drawable.album_save_disable);
			mSaveView.setOnClickListener(this);
			params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
			params.rightMargin = ShareData.PxToDpi_xhdpi(140);
			mSelectBottomBar.addView(mSaveView, params);
		}

		View line = new View(mContext);
		line.setBackgroundColor(0xff3b3b3b);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, Gravity.BOTTOM);
		params.bottomMargin = mBottomHeight;
		addView(line, params);

		mDeleteDialog = new ProgressDialog(mContext);
		mDeleteDialog.setMessage(mContext.getResources().getString(R.string.deleting));
		mDeleteDialog.setCancelable(false);
	}

	/**
	 *
	 * @param params 参数
	 *               from_home: boolean 是否来自首页
	 *               single_select: 是否单选
	 *               hide_multi_choose: 将多选按钮置灰
	 */
	@Override
	public void SetData(HashMap<String, Object> params) {

		boolean toSystemAlbum = true;
		if (params != null) {
			if (!mSite.m_myParams.isEmpty()) {
				params.clear();
				params.putAll(mSite.m_myParams);
				mSite.m_myParams.clear();
			}

			Boolean b = (Boolean)params.get("from_home");
			if (b != null && b) {
				toSystemAlbum = false;
			}

			b = (Boolean)params.get("single_select");
			if (b != null) {
				mSingleSelect = b;

				if (mSingleSelect) {
					mSelectText.setVisibility(GONE);
				}
			}

			Object o = params.get("hide_multi_choose");
			if (o instanceof Boolean) {
				mHideChoose = (Boolean) o;

				if (mHideChoose) {
					mSite.m_myParams.putAll(params);
					mSite.m_myParams.remove("hide_multi_choose");

					mSelectText.setEnabled(false);
					mSelectText.setTextColor(0xff666666);
				}
			}
		}

		if (toSystemAlbum) {
			sLastFolderIndex = 0;
			sLocalAlbum = false;
		}

		mFolderIndex = sLastFolderIndex;

		mPhotoStore.addLoadCompleteListener(mLoadCompleteListener);

		initMemoryTipDialog();
	}

	/**
	 * 初始化数据
	 */
	private void initDatas() {

		List<FolderInfo> folderInfos = mPhotoStore.getFolderInfos();

		List<FolderInfo> data = new ArrayList<>();
		FolderInfo tempInfo;
		for (int i = 0; i < folderInfos.size(); i++) {
			tempInfo = folderInfos.get(i);

			if (tempInfo.getCount() <= 0) {
				if (mFolderIndex == i) {
					mFolderIndex = 0;
				} else if (mFolderIndex > i) {
					mFolderIndex--;
				}
			} else {
				data.add(tempInfo);
			}
		}

		if (data.isEmpty()) {
			data.add(folderInfos.get(0));
			mPhotoStore.forceUpdate();
		}
		mPhotoStore.setFolderInfos(data);

		mFolderAdapter = new FolderAdapter(mContext, mPhotoStore.getFolderInfos(), mFolderIndex);
		mFolderAdapter.setOnFolderClickListener(new FolderAdapter.OnFolderClickListener() {
			@Override
			public void onClick(View view, int position) {
				if (mUiEnabled) {
					hideFolderList();
					if (mFolderIndex != position) {
						mFolderIndex = sLastFolderIndex = position;
						FolderInfo folderInfo = mPhotoStore.getFolderInfo(position);
						mFolderAdapter.setCurrentPos(position);
						mSystemAlbumView.updatePhotos(mPhotoStore.getPhotoInfos(folderInfo.getName(), 0));
						mSystemAlbumView.setFolderIndex(position);
						mSystemAlbumView.backToTop();
						mSystemAlbumName.setText(folderInfo.getName());
					}
				}
			}
		});
		mFolderList.setAdapter(mFolderAdapter);

		FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);

		sLastFolderIndex = mFolderIndex;
		mSystemAlbumView.setFolderIndex(mFolderIndex);
		mSystemAlbumName.setText(folderInfo.getName());

		mSystemAlbumView.setPhotoList(mPhotoStore.getPhotoInfos(folderInfo.getName(), 0), mSingleSelect);
		mSystemAlbumView.setOnItemClickListener(new SystemAlbumAdapter.OnItemClickListener() {

			@Override
			public void onItemClick(View view, int position, String path) {
				if (mUiEnabled && !hasSelected && PhotoUtils.validatePhoto(mContext, path)) {
					hasSelected = true;
					AlbumPage.sLocalAlbum = false;
					AlbumPage.sSystemPosition = mSystemAlbumView.getPosition();
					String folderName = mPhotoStore.getFolderInfo(mFolderIndex).getName();
					mSite.onSelectPhoto(mContext, folderName, path, false);
				}
			}

			@Override
			public void onItemLongClick(int position) {

				if (mUiEnabled) {
					if (mMode != SELECT) {
						select();
					}
					mSystemAlbumView.startDragSelection(position);

					MyBeautyStat.onClickByRes(R.string.照片相册页_长按进入多选);
				}
			}
		});
		mSystemAlbumView.setOnSelectListener(new SystemAlbumAdapter.OnSelectListener() {
			@Override
			public void onSelect(int count) {
				hasSystemSelect(count != 0);
				String title = mContext.getResources().getQuantityString(R.plurals.photo_selected, count, count);
				mSelectTitle.setText(title);
			}
		});

		mLocalAlbumView.setPhotoList(LocalStore.getLocalAlbumList(), mSingleSelect);
		mLocalAlbumView.setOnItemClickListener(new LocalAlbumAdapter.OnItemClickListener() {

			@Override
			public void onItemClick(View view, int position, String path) {
				if (!mUiEnabled || hasSelected) {
					return;
				}
				hasSelected = true;

				AlbumPage.sLocalAlbum = true;
				AlbumPage.sLocalPosition = mLocalAlbumView.getPosition();
				mSite.onSelectPhoto(mContext, null, path, true);
			}

			@Override
			public void onItemLongClick(int position) {
				if (mUiEnabled) {
					if (mMode != SELECT) {
						select();
					}
					mLocalAlbumView.startDragSelection(position);

					MyBeautyStat.onClickByRes(R.string.照片相册页_长按进入多选);
				}
			}
		});

		mLocalAlbumView.setOnSelectListener(new LocalAlbumAdapter.OnSelectListener() {
			@Override
			public void onSelect(int count) {
				hasLocalSelect(count != 0, count == 1);

				String title = mContext.getResources().getQuantityString(R.plurals.photo_selected, count, count);
				mSelectTitle.setText(title);
			}
		});

		if (sLocalAlbum) {
			mTabIndicator.post(new Runnable() {
				@Override
				public void run() {
					mViewPager.setCurrentItem(1);
				}
			});
			TongJi2.AddCountByRes(mContext, R.integer.InterPhoto);
			MyBeautyStat.onPageStartByRes(R.string.InterPhoto);
		} else {
			TongJi2.AddCountByRes(mContext, R.integer.系统相册);
			MyBeautyStat.onPageStartByRes(R.string.照片相册页);
		}

		restorePostion();
	}

	/**
	 * 初始化SD卡不足提示框
	 */
	private void initMemoryTipDialog() {

		long availableSize = SDCardUtils.getSDCardAvailableSize();
		if (availableSize < SDCardUtils.DEFAULT_SIZE) {
			mMemoryTipDialog = new MemoryTipDialog(mContext);
			mMemoryTipDialog.setOnClickListener(new MemoryTipDialog.OnClickListener() {
				@Override
				public void onCheck() {
					TongJi2.AddCountByRes(mContext, R.integer.查看手机空间);
					mMemoryTipDialog.dismiss();
					try {
						Intent intent = new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
						mContext.startActivity(intent);
					} catch (Throwable throwable) {
						T.showShort(mContext, R.string.can_not_open_memory_page);
					}
				}

				@Override
				public void onClear() {
					TongJi2.AddCountByRes(mContext, R.integer.清理InterPhoto);
					mMemoryTipDialog.dismiss();
					mSite.openAlbumCachePage(mContext);
				}
			});
			mMemoryTipDialog.show();
		}
	}

	/**
	 * 恢复到指定位置
	 */
	private void restorePostion() {
		if (sSystemPosition != null) {
			mSystemAlbumView.restorePosition(sSystemPosition);
		}

		if (sLocalPosition != null) {
			mLocalAlbumView.restorePosition(sLocalPosition);
		}
	}

	/**
	 * 系统相册选中回调处理方法
	 *
	 * @param selected 是否选中图片
	 */
	private void hasSystemSelect(boolean selected) {
		if (selected) {
			mDeleteView.setImageResource(R.drawable.album_delete_enable);
			mDeleteView.setEnabled(true);
			if (sCopy) {
				mPasteEffect.setImageAlpha(1f);
				mPasteEffect.setMyEnable(true);
			}
		} else {
			mDeleteView.setImageResource(R.drawable.album_delete_disable);
			mDeleteView.setEnabled(false);
			if (sCopy) {
				mPasteEffect.setImageAlpha(0.1f);
				mPasteEffect.setImageBitmap(sCopyEffect.image);
				mPasteEffect.setMyEnable(false);
			}
		}

		if (!sCopy) {
			mPasteEffect.setImageAlpha(1f);
			mPasteEffect.setImageResource(R.drawable.album_add_copy);
			mPasteEffect.setMyEnable(false);
		}
	}

	/**
	 * 本地相册选中回调处理方法
	 * @param selected 是否选中图片
	 * @param single 是否单选
	 */
	private void hasLocalSelect(boolean selected, boolean single) {

		if (selected) {
			mDeleteView.setImageResource(R.drawable.album_delete_enable);
			mDeleteView.setEnabled(true);
			mSaveView.setImageResource(R.drawable.album_save_enable);
			mSaveView.setEnabled(true);

			if (sCopy) {
				mPasteEffect.setImageAlpha(1f);
				mPasteEffect.setMyEnable(true);
			}

			if (single && copyEnabled()) {
				mCopyEffect.setMyEnable(true);
				int size = 0;
				String path = null;
				for (ImageStore.ImageInfo info : mLocalAlbumView.getSelectPhotos(null)) {
					if (!TextUtils.isEmpty(info.effect)) {
						size++;
						path = info.image;
					}
				}

				if (size == 1 && path != null) {
					Glide.with(mContext).load(path).dontAnimate().thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESULT).into(mCopyEffect.getImageView());
				} else {
					mCopyEffect.setImageResource(R.drawable.album_add_copy);
				}

			} else {
				mCopyEffect.setMyEnable(false);
				mCopyEffect.setImageResource(R.drawable.album_add_copy);
			}
		} else {
			mDeleteView.setImageResource(R.drawable.album_delete_disable);
			mDeleteView.setEnabled(false);
			mSaveView.setImageResource(R.drawable.album_save_disable);
			mSaveView.setEnabled(false);
			if (sCopy) {
				mPasteEffect.setImageAlpha(0.1f);
				mPasteEffect.setMyEnable(false);
				mPasteEffect.setImageBitmap(sCopyEffect.image);
			}

			mCopyEffect.setMyEnable(false);
			mCopyEffect.setImageResource(R.drawable.album_add_copy);
		}
	}

	/**
	 * 判断是否可以复制
	 */
	private boolean copyEnabled() {
		boolean enabled = false;
		for (ImageStore.ImageInfo imageInfo : mLocalAlbumView.getSelectPhotos(null)) {
			if (!TextUtils.isEmpty(imageInfo.effect)) {
				enabled = true;
			}
		}

		return enabled;
	}

	private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (position != 1 || positionOffset != 0.0f) {
				mTabIndicator.setProgress(positionOffset);
			}

			if (position == 0 && positionOffset == 0.0f) {
				hideCacheView(1);
			} else if (position == 1 && positionOffset == 0.0f) {
				hideCacheView(0);
			}
		}

		@Override
		public void onPageSelected(int position) {

			if (mCurrentPosition != position) {
				mChangePosition = true;
			}

			changeTab(position);

			sLocalAlbum = position == 1;
		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}
	};

	/**
	 * 隐藏CacheView
	 */
	private void hideCacheView(int position) {
		if (mChangePosition) {
			mChangePosition = false;
			if (position == 0) {
				mSystemAlbumView.hideCacheView();
			} else if (position == 1) {
				mLocalAlbumView.hideCacheView();
			}
		}
	}

	private void changeTab(int position) {
		if (mCurrentPosition != position) {
			changeTabName(position, mSystemAlbumName, mInterPhotoName);
			mCurrentPosition = position;
		}

		if (position == 0) {
			mTabIndicator.setProgress(0);
			setSelectTextState(mSystemAlbumView.isEmpty());
		} else {
			mTabIndicator.setProgress(1);
			setSelectTextState(mLocalAlbumView.isEmpty());
		}
	}

	/**
	 * 设置选择文案的状态
	 * @param empty 数据是否为空
	 */
	private void setSelectTextState(boolean empty) {
		if (empty) {
			mSelectText.setEnabled(false);
			mSelectText.setTextColor(0xff666666);
		} else if (!mHideChoose) {
			mSelectText.setEnabled(true);
			mSelectText.setTextColor(0xffffffff);
		}
	}

	private void changeTabName(int position, TextView... tabs) {
		tabs[mCurrentPosition].setTextColor(COLOR_GRAY);
		tabs[position].setTextColor(Color.WHITE);
	}

	@Override
	public void onResume() {
		super.onResume();
		mPhotoStore.clearCache();
		FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
		mSystemAlbumView.updatePhotos(mPhotoStore.getPhotoInfos(folderInfo.getName(), 0));
		TongJiUtils.onPageResume(getContext(), TAG);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSystemAlbumView.onPause();
		TongJiUtils.onPagePause(getContext(), TAG);
	}

	@Override
	public void onBack() {

		if (mMode == SELECT) {
			cancelSelect();
		} else if (mMode == FOLDER) {
			hideFolderList();
		} else {
			sSystemPosition = null;
			sLocalPosition = null;

			MyBeautyStat.onClickByRes(R.string.照片相册页_退出照片相册);
			mSite.OnBack(mContext);
		}
	}

	@Override
	public void onClose() {

		if (mDeleteInSystemTask != null && !mDeleteInSystemTask.isCancelled()) {
			mDeleteInSystemTask.cancel(true);
			mDeleteInSystemTask = null;
		}

		if (mDeleteInAlbumTask != null && !mDeleteInAlbumTask.isCancelled()) {
			mDeleteInAlbumTask.cancel(true);
			mDeleteInAlbumTask = null;
		}

		if (mSaveTask != null && !mSaveTask.isCancelled()) {
			mSaveTask.cancel(true);
			mSaveTask = null;
		}

		if (mPasteTask != null && !mPasteTask.isCancelled()) {
			mPasteTask.cancel(true);
			mPasteTask = null;
		}

		if (mPasteTask2 != null && !mPasteTask2.isCancelled()) {
			mPasteTask2.cancel(true);
			mPasteTask2 = null;
		}

		mSystemAlbumView.onClose();
		mLocalAlbumView.onClose();

		mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
		mPhotoStore.removeLoadCompleteListener(mLoadCompleteListener);

		MemoryInfo.clear();
//		mPhotoStore.clearCache();
		Glide.get(mContext).clearMemory();

		if (mCurrentPosition == 0) {
			MyBeautyStat.onPageEndByRes(R.string.照片相册页);
		} else {
			MyBeautyStat.onPageStartByRes(R.string.InterPhoto);
		}

		TongJiUtils.onPageEnd(getContext(), TAG);
	}

	@Override
	public void onPageResult(int siteID, HashMap<String, Object> params) {
		super.onPageResult(siteID, params);

		if (siteID == SiteID.ALBUM_CACHE) {
			mSystemAlbumView.hideCacheView();
			mLocalAlbumView.hideCacheView();
		}
	}

	@Override
	public void onClick(View v) {

		if (!mUiEnabled) {
			return;
		}

		if (v == mSystemTab) {
			onFolderTabClick();
		} else if (v == mInterPhotoTab) {
			onInterPhotoTabClick();
		} else if (v == mBackground) {
			hideFolderList();
		} else if (v == mSelectText) {
			if (mSelectText.isEnabled()) {

				if (mCurrentPosition == 0) {
					TongJi2.AddCountByRes(mContext, R.integer.系统相册_选择);
					MyBeautyStat.onClickByRes(R.string.系统相册页_点击多选照片_系统相册);
				} else {
					MyBeautyStat.onClickByRes(R.string.InterPhoto_点击多选_InterPhoto);
				}

				select();
			}
		} else if (v == mSelectCancel) {
			cancelSelect();
		} else if (v == mBackView) {
			onBack();
		} else if (v == mDeleteView) {
			if (mDeleteView.isEnabled()) {

				if (mCurrentPosition == 0) {
					deleteInSystemAlbum();
					MyBeautyStat.onClickByRes(R.string.系统相册页_删除照片_系统相册);
					TongJi2.AddCountByRes(mContext, R.integer.系统相册_删除);
				} else {
					deleteInInterAlbum();
					MyBeautyStat.onClickByRes(R.string.InterPhoto_删除照片_InterPhoto);
					TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_删除);
				}
			}
		} else if (v == mSaveView) {
			if (mSaveView.isEnabled()) {
				mSaveTask = new SaveTask();
				mSaveTask.execute(mLocalAlbumView.getSelectPhotos(mIndexList).toArray(new ImageStore.ImageInfo[] {}));
				cancelSelect();

				MyBeautyStat.onClickByRes(R.string.InterPhoto_保存照片_InterPhoto);
				TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_保存);
			}
		} else if (v == mCopyEffect.getTextView()) {
			if (mCopyEffect.canClick()) {
				copyEffect();
				MyBeautyStat.onClickByRes(R.string.InterPhoto_复制效果_InterPhoto);
			}
		} else if (v == mPasteEffect.getTextView()) {
			if (mPasteEffect.canClick()) {
				if (mCurrentPosition == 0) {
					pasteEffectInSystem();
					MyBeautyStat.onClickByRes(R.string.系统相册页_粘贴效果_系统相册);
					TongJi2.AddCountByRes(mContext, R.integer.系统相册_粘贴效果);
				} else {
					pasteEffectInLocal();
					MyBeautyStat.onClickByRes(R.string.InterPhoto_粘贴效果_InterPhoto);
					TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_粘贴效果);
				}
			}
		} else if (v == mTopLayout) {
			if (mCurrentPosition == 0) {
				mSystemAlbumView.backToTop();
				sSystemPosition = null;
			} else {
				mLocalAlbumView.backToTop();
				sLocalPosition = null;
			}
		}
	}

	/**
	 * 复制效果操作
	 */
	private void copyEffect() {

		mCopyEffect.setMyEnable(false);
		mCopyEffect.setImageResource(R.drawable.album_add_copy);

		for (ImageStore.ImageInfo imageInfo : mLocalAlbumView.getSelectPhotos(null)) {
			if (sCopyEffect == null) {
				sCopyEffect = new CopyEffect();
			}
			if (sCopyEffect.image != null && !sCopyEffect.image.isRecycled()) {
				sCopyEffect.image.recycle();
			}

			int imageSize = ShareData.PxToDpi_xhdpi(56);
			sCopyEffect.image = Utils.DecodeImage(mContext, imageInfo.image, 0, 0, imageSize, imageSize);
			sCopyEffect.effect = imageInfo.effect;
		}

		mPasteEffect.setImageBitmap(sCopyEffect.image);
		mPasteEffect.setImageAlpha(0.1f);
		// 进入粘贴效果操作
		sCopy = true;
		mLocalAlbumView.enterPasteEffect();

		TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_复制效果);
	}

	/**
	 * 在系统相册中粘贴效果
	 */
	private void pasteEffectInSystem() {
		mPasteImageInfos.clear();
		mPasteImageInfos.addAll(mSystemAlbumView.getSelectPhotos(null));
		cancelSelect();
		mPasteTask = new PasteTask();
		mPasteTask.execute();
	}

	/**
	 * 在本地相册中粘贴效果
	 */
	private void pasteEffectInLocal() {
		mPasteImageInfos.clear();
		mPasteImageInfos.addAll(mLocalAlbumView.getSelectPhotos(mIndexList));
		int count = 0;
		for (ImageStore.ImageInfo imageInfo : mPasteImageInfos) {
			if (!imageInfo.isSaved) {
				count++;
			}
		}
		showTip(count);
	}

	private void showTip(int count) {
		final PasteTipDialog tipDialog = new PasteTipDialog(mContext);
		tipDialog.setOnBtnClickListener(new PasteTipDialog.OnBtnClickListener() {
			@Override
			public void onCancel() {
				tipDialog.dismiss();
			}

			@Override
			public void onConfirm() {
				cancelSelect();
				mPasteTask2 = new PasteTask2();
				mPasteTask2.execute();

				tipDialog.dismiss();
			}
		});
		tipDialog.setData(count, mPasteImageInfos);
		tipDialog.show();
	}

	private void onFolderTabClick() {
		if (mCurrentPosition != 0) {
			mViewPager.setCurrentItem(0);
			MyBeautyStat.onClickByRes(R.string.InterPhoto_切换至系统相册);

			MyBeautyStat.onPageEndByRes(R.string.InterPhoto);
			MyBeautyStat.onPageStartByRes(R.string.照片相册页);
		} else if (mMode == NORMAL) {
			if (!mPhotoStore.getPhotoInfos(null, 0).isEmpty()) {
				showFolderList();
			}
		} else if (mMode == FOLDER) {
			hideFolderList();
		}
	}

	private void onInterPhotoTabClick() {

		if (mCurrentPosition != 1) {
			mViewPager.setCurrentItem(1);
			if (mMode == FOLDER) {
				hideFolderList();
			}
			MyBeautyStat.onClickByRes(R.string.系统相册页_切换至InterPhoto相册);

			MyBeautyStat.onPageEndByRes(R.string.照片相册页);
			MyBeautyStat.onPageStartByRes(R.string.InterPhoto);
		}

	}

	/**
	 * 显示文件夹列表
	 */
	private void showFolderList() {

		if (isRunningAnimation) {
			return;
		}

		isRunningAnimation = true;

		mMode = FOLDER;

//		mSysGridView.autoClose();

		mFolderIndicator.setImageResource(R.drawable.album_ic_down);
		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_in_bottom);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mFolderList.setVisibility(VISIBLE);
				mBackground.setVisibility(VISIBLE);
				AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
				alphaAnimation.setDuration(200);
				mBackground.startAnimation(alphaAnimation);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				isRunningAnimation = false;
				MyBeautyStat.onPageStartByRes(R.string.系统相册页);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mFolderList.scrollToPosition(mFolderIndex);
		mFolderList.startAnimation(animation);

		MyBeautyStat.onClickByRes(R.string.系统相册页_打开系统相册列表);
	}

	/**
	 * 隐藏文件夹列表
	 */
	private void hideFolderList() {

		if (isRunningAnimation) {
			return;
		}

		isRunningAnimation = true;

		mMode = NORMAL;
		mFolderIndicator.setImageResource(R.drawable.album_ic_up);
		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_out_bottom);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
				alphaAnimation.setDuration(200);
				mBackground.startAnimation(alphaAnimation);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mBackground.setVisibility(INVISIBLE);
				mFolderList.setVisibility(INVISIBLE);

				isRunningAnimation = false;

				MyBeautyStat.onPageEndByRes(R.string.系统相册页);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		mFolderList.startAnimation(animation);
	}

	/**
	 * 进入选择模式
	 */
	private void select() {
		mMode = SELECT;

		mViewPager.setCanScroll(false);
		mSystemAlbumView.setSelectMode(true);
		mLocalAlbumView.setSelectMode(true);

		mSystemAlbumView.hideCacheViewWithAnim();
		mLocalAlbumView.hideCacheViewWithAnim();

		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_in_top);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mSelectTopBar.setVisibility(View.VISIBLE);
				mTopLayout.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mSelectTopBar.startAnimation(animation);

		if (mCurrentPosition == 0) {
			mCopyEffect.setVisibility(GONE);
			mSaveView.setVisibility(GONE);
		} else {
			mCopyEffect.setVisibility(VISIBLE);
			mSaveView.setVisibility(VISIBLE);
		}

		Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_in_bottom);
		animation1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mSelectBottomBar.setVisibility(VISIBLE);
				mBottomLayout.setVisibility(INVISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});

		mSelectBottomBar.startAnimation(animation1);

		if (mCurrentPosition == 0) {
			mSystemAlbumView.startSelect();
		} else {
			mLocalAlbumView.startSelect();
		}
	}

	/**
	 * 取消选择
	 */
	private void cancelSelect() {
		mMode = NORMAL;

		mViewPager.setCanScroll(true);
		mSystemAlbumView.setSelectMode(false);
		mLocalAlbumView.setSelectMode(false);

		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_out_top);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mTopLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSelectTopBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		mSelectTopBar.startAnimation(animation);

		Animation animation1 = AnimationUtils.loadAnimation(mContext, R.anim.cloudalbum_slide_out_bottom);
		animation1.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mSelectBottomBar.setVisibility(GONE);
				mBottomLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		mSelectBottomBar.startAnimation(animation1);

		if (mCurrentPosition == 0) {
			mSystemAlbumView.cancelSelect();
			TongJi2.AddCountByRes(mContext, R.integer.系统相册_取消);
			MyBeautyStat.onClickByRes(R.string.系统相册页_取消多选_系统相册);
		} else {
			mLocalAlbumView.cancelSelect();
			TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_取消);
			MyBeautyStat.onClickByRes(R.string.InterPhoto_取消多选_InterPhoto);
		}

		resetSelectBottomBar();
	}

	/**
	 * 重置选择底部栏的状态
	 */
	private void resetSelectBottomBar() {

		mCopyEffect.setImageAlpha(1.0f);
		mCopyEffect.setImageResource(R.drawable.album_add_copy);
		mCopyEffect.setMyEnable(false);

		mPasteEffect.setImageAlpha(1.0f);
		mPasteEffect.setImageResource(R.drawable.album_add_copy);
		mPasteEffect.setMyEnable(false);
	}

	private PhotoStore.ILoadComplete mLoadCompleteListener = new PhotoStore.ILoadComplete() {

		@Override
		public void onCompleted(final List<FolderInfo> folderInfos, boolean update) {
			if (!update || mFolderAdapter == null) {
				((Activity)mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initDatas();
					}
				});
			}
//			else if (!folderInfos.isEmpty() && folderInfos != mPhotoStore.getFolderInfos()){
//				((Activity)mContext).runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						mPhotoStore.setFolderInfos(folderInfos);
//						mFolderAdapter.dataChange(folderInfos, mFolderIndex);
//					}
//				});
//			}
		}
	};

	/**
	 * 系统相册删除图片
	 */
	private void deleteInSystemAlbum() {

		final List<ImageStore.ImageInfo> imageInfos = mSystemAlbumView.getSelectPhotos(mIndexList);
		if (imageInfos == null || imageInfos.isEmpty()) {
			return;
		}

		if (mDeleteDlg == null) {
			mDeleteDlg = new DeleteDlg((Activity)getContext(), R.style.waitDialog);
			mDeleteDlg.setOnDlgClickCallback(new DeleteDlg.OnDlgClickCallback() {

				@Override
				public void onDelete(Object res) {
					mDeleteDlg.dismiss();
					mDeleteInSystemTask = new DeleteInSystemTask();
					mDeleteInSystemTask.execute(imageInfos.toArray(new ImageStore.ImageInfo[] {}));
				}

				@Override
				public void onCancel() {
					mDeleteDlg.dismiss();
				}

				@Override
				public void onPageClick() {

				}
			});
		}

		if (mDeleteDlg != null) {
			mDeleteDlg.setData(imageInfos);
			mDeleteDlg.show();
		}
	}

	/**
	 * InterPhoto相册删除图片
	 */
	private void deleteInInterAlbum() {

		final List<ImageStore.ImageInfo> imageInfos = mLocalAlbumView.getSelectPhotos(mIndexList);
		if (imageInfos == null || imageInfos.isEmpty()) {
			return;
		}

		if (mDeleteDlg1 == null) {
			mDeleteDlg1 = new DeleteDlg((Activity)getContext(), R.style.waitDialog);
			mDeleteDlg1.setOnDlgClickCallback(new DeleteDlg.OnDlgClickCallback() {

				@Override
				public void onDelete(Object res) {
					mDeleteDlg1.dismiss();
					List<ImageStore.ImageInfo> photoInfos = mLocalAlbumView.getSelectPhotos(mIndexList);

					mDeleteInAlbumTask = new DeleteInAlbumTask();
					mDeleteInAlbumTask.execute(photoInfos.toArray(new ImageStore.ImageInfo[] {}));
				}

				@Override
				public void onCancel() {
					mDeleteDlg1.dismiss();
				}

				@Override
				public void onPageClick() {

				}
			});
		}

		if (mDeleteDlg1 != null) {
			mDeleteDlg1.setData(mLocalAlbumView.getSelectPhotos(null));
			mDeleteDlg1.show();
		}
	}

	@SuppressWarnings("all")
	private void showProgress() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.album_dialog_progress, null);
		mDialog = new Dialog(mContext);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCancelable(false);
		mDialog.setContentView(view);
		mProgressView = (ProgressView)view.findViewById(R.id.progress);
		mProgressView.setProgress(0);
		mDialog.show();
	}

	private class DeleteInSystemTask extends AsyncTask<ImageStore.ImageInfo, Void, Void> {

		@Override
		protected void onPreExecute() {
			mDeleteDialog.show();
		}

		@Override
		protected Void doInBackground(ImageStore.ImageInfo... params) {

			if (params != null && params.length > 0) {
				PhotoUtils.deletePhotos(mContext, Arrays.asList(params));
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mDeleteDialog.dismiss();

			FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
			if (folderInfo.getCount() <= 0) {
				mFolderAdapter.delete(mFolderIndex);
				mFolderIndex = sLastFolderIndex = 0;
				folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
				mSystemAlbumView.setFolderIndex(mFolderIndex);
				mSystemAlbumView.updatePhotos(mPhotoStore.getPhotoInfos(folderInfo.getName(), 0));
				mSystemAlbumName.setText(folderInfo.getName());
			} else {
				mSystemAlbumView.delete(mIndexList);

				// 通知相册列表改变
				if (mFolderIndex == 0) {
					mFolderAdapter.notifyDataSetChanged();
				} else {
					mFolderAdapter.notifyItemChanged(0);
					mFolderAdapter.notifyItemChanged(mFolderIndex);
				}
			}

			mIndexList.clear();

			Toast.makeText(getContext(), getResources().getString(R.string.deletedSuccess), Toast.LENGTH_SHORT).show();

			cancelSelect();
		}
	}

	private class DeleteInAlbumTask extends AsyncTask<ImageStore.ImageInfo, Void, Void> {

		@Override
		protected void onPreExecute() {
			FileUtils.init(mContext);
			mDeleteDialog.show();
		}

		@Override
		protected Void doInBackground(ImageStore.ImageInfo... params) {

			if (params != null && params.length > 0) {
				AlbumUtils.deleteImages(mContext, Arrays.asList(params));
			}

			boolean isEmpty = AlbumUtils.isDatabaseEmpty(mContext);
			if (isEmpty) {
				FileUtils.delete(FileUtils.PHOTO_DIR);
				MemoryInfo.interPhotoSize = 0;
			} else {
				MemoryInfo.interPhotoSize = AlbumUtils.getCacheSize(mContext);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mDeleteDialog.dismiss();

			mLocalAlbumView.delete(mIndexList);
			mIndexList.clear();

			Toast.makeText(getContext(), getResources().getString(R.string.deletedSuccess), Toast.LENGTH_SHORT).show();

			cancelSelect();

			if (LocalStore.getLocalAlbumList().isEmpty()) {
				mSelectText.setEnabled(false);
				mSelectText.setTextColor(0xff666666);
			}

			MemoryInfo.notifyChange();
		}
	}

	/**
	 * 保存任务
	 */
	private class SaveTask extends AsyncTask<ImageStore.ImageInfo, Integer, Void> {
		private int mCount;
		private boolean mAddDate;

		@Override
		protected void onPreExecute() {
			showProgress();

			mAddDate = SettingInfoMgr.GetSettingInfo(mContext).GetAddDateState();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mProgressView.setProgress(values[0] * 1.0f / mCount * 100);
		}

		@Override
		@SuppressWarnings("all")
		protected Void doInBackground(ImageStore.ImageInfo... params) {
			if (params == null || params.length == 0) {
				return null;
			}

			mCount = params.length;

			ImageStore.ImageInfo imageInfo;

			for (int i = 0; i < mCount; i++) {
				imageInfo = params[i];
				if (mAddDate) {
					ExifInterface orgExif = null;
					if (imageInfo.image != null) {
						try {
							orgExif = new ExifInterface(imageInfo.image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					Bitmap bmp = BeautifyHandler.MakeBmp2(mContext, imageInfo.image, -1, -1);
					if (bmp != null) {
						Utils.attachDate(bmp);
					}

					File file = new File(imageInfo.image);
					if (file.exists()) {
						file.delete();
					}
					imageInfo.image = Utils.SaveImg(mContext, bmp, imageInfo.image, 100, false);
					ExifInterface saveExif = null;
					if (imageInfo.image != null) {
						try {
							saveExif = new ExifInterface(imageInfo.image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					SaveExifInfoToImg(orgExif, saveExif);
				}

				AlbumUtils.save(mContext, imageInfo.id);

				publishProgress(i + 1);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mDialog.dismiss();

			if (mFolderIndex == 0 || mFolderIndex == 1) {
				mPhotoStore.clearCache();
				FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
				mSystemAlbumView.updatePhotos(mPhotoStore.getPhotoInfos(folderInfo.getName(), 0));
			}

			mLocalAlbumView.change(mIndexList);
			mIndexList.clear();

			if (mFolderAdapter.getItemCount() != mPhotoStore.getFolderInfos().size()) {
				mFolderAdapter.notifyItemChanged(0);
				mFolderAdapter.addCameraAlbum(mPhotoStore.getFolderInfo(1));
			} else {
				mFolderAdapter.notifyItemChanged(0);
				mFolderAdapter.notifyItemChanged(1);
			}

			mFolderIndex = sLastFolderIndex;
			mFolderAdapter.setCurrentPos(mFolderIndex);
			mSystemAlbumView.setFolderIndex(mFolderIndex);
			mSystemAlbumName.setText(mPhotoStore.getFolderInfo(mFolderIndex).getName());

			Toast.makeText(mContext, R.string.Saved, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 系统相册粘贴效果
	 */
	private class PasteTask extends AsyncTask<Void, Integer, Void> {

		private int mCount;

		@Override
		protected void onPreExecute() {
			Glide.get(mContext).clearMemory();
			showProgress();
			mCount = mPasteImageInfos.size();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mProgressView.setProgress(values[0] * 1.0f / mCount * 100);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap srcBitmap, destBitmap;
			ImageStore.ImageInfo imageInfo;
			String effect;
			for (int i = 0; i < mPasteImageInfos.size(); i++) {
				imageInfo = mPasteImageInfos.get(i);
				srcBitmap = BeautifyHandler.MakeBmp2(mContext, imageInfo.image, -1, -1);

				effect = sCopyEffect.effect;
				destBitmap = BeautifyHandler.DoEffects(mContext, effect, srcBitmap);

				if (destBitmap != null && effect != null) {
					mPasteResultInfos.add(AlbumUtils.insertImage(mContext, destBitmap, imageInfo.image, effect));
				}

				publishProgress(i + 1);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mDialog.dismiss();

			mLocalAlbumView.insert(mPasteResultInfos);
			mLocalAlbumView.backToTop();
			mPasteResultInfos.clear();
			mViewPager.setCurrentItem(1);
		}
	}

	private class PasteTask2 extends AsyncTask<Void, Integer, Void> {

		private int mCount;

		@Override
		protected void onPreExecute() {
			Glide.get(mContext).clearMemory();
			showProgress();
			mCount = mPasteImageInfos.size();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			mProgressView.setProgress(values[0] * 1.0f / mCount * 100);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap srcBitmap, destBitmap;
			ImageStore.ImageInfo imageInfo;
			String effect;
			for (int i = 0; i < mPasteImageInfos.size(); i++) {
				imageInfo = mPasteImageInfos.get(i);
				srcBitmap = BeautifyHandler.MakeBmp2(mContext, imageInfo.image, -1, -1);

				effect = sCopyEffect.effect;
				destBitmap = BeautifyHandler.DoEffects(mContext, effect, srcBitmap);
				if (destBitmap != null && effect != null) {
					AlbumUtils.saveEffect(mContext, imageInfo.id, destBitmap, imageInfo.image, effect);
				}
				publishProgress(i + 1);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			mDialog.dismiss();
			mLocalAlbumView.change(mIndexList);
			mIndexList.clear();
		}
	}

	public static class Position {
		public int position;
		public int offset;
	}

	public static class CopyEffect {
		public Bitmap image;
		public String effect;
	}
}
