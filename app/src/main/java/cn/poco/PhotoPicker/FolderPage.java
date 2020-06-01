package cn.poco.PhotoPicker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.PhotoPicker.ImageStore.FolderInfo;
import cn.poco.PhotoPicker.ImageStore.ImageInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class FolderPage extends IPage
{
	public FolderPage(Context context, BaseSite site)
	{
		super(context, site);

		initialize(context);
	}

	private boolean mExited = false;
	private int mCacheSize = 16;
	private GridView mListView;
	private ImageAdapter mAdapter;
	//private VideoProgressDialog mProgressDialog;
	private OnItemClickListener mItemClickListener;
	private static int sLastFirstSelection;
	private boolean mIsScrolling = false;
	private boolean mIdle = false;
	private int mItemSize = 200;

	public interface OnItemClickListener
	{
		void onItemClick(FolderInfo info);
	};

	private static ArrayList<ListItemInfo> sListItemInfos = new ArrayList<ListItemInfo>();

	private void initialize(Context context)
	{
		ShareData.InitData((Activity)context);
		mAdapter = new ImageAdapter();

		mItemSize = (ShareData.m_screenWidth - ShareData.PxToDpi_hdpi(30)) / 2;

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mListView = new GridView(context);
		mListView.setVerticalFadingEdgeEnabled(false);
		mListView.setAdapter(mAdapter);
		mListView.setNumColumns(2);
		mListView.setVerticalSpacing(ShareData.PxToDpi_hdpi(30));
		mListView.setCacheColorHint(0x00000000);
		addView(mListView, params);
		mListView.setOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState)
			{

				mIsScrolling = true;
				mIdle = false;
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE)
				{
					sLastFirstSelection = mListView.getFirstVisiblePosition();
					if(sLastFirstSelection >= 0 && sLastFirstSelection < sListItemInfos.size())
					{
						ListItemInfo info = sListItemInfos.get(sLastFirstSelection);
						ListItem listItem;
						int count = mListView.getChildCount();
						for(int i = 0; i < count; i++)
						{
							listItem = (ListItem)mListView.getChildAt(i);
							if(listItem.getItemInfo() == info)
							{
								if(listItem.getTop() < -(listItem.getHeight() * 0.4))
								{
									if(sLastFirstSelection < sListItemInfos.size() - 1)
									{
										sLastFirstSelection++;
									}
								}
								break;
							}
						}
					}
					mIsScrolling = false;
					idleProcess();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
			{
			}
		});
	}

	public void setOnItemClickListener(OnItemClickListener listener)
	{
		mItemClickListener = listener;
	}

	public static void clearGlobalCache()
	{
		if(sListItemInfos != null)
		{
			sListItemInfos.clear();
		}
	}

	public void loadFolders()
	{
		//if(mProgressDialog != null)
		//{
		//	mProgressDialog.dismiss();
		//	mProgressDialog = null;
		//}
		//mProgressDialog = VideoProgressDialog.show(getContext(), "", "正在加载图片列表...");
		//mProgressDialog.setProgressStyle(VideoProgressDialog.STYLE_SPINNER);
		//mProgressDialog.show();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final ArrayList<ListItemInfo> items = loadFolderListProc();
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						//if(mProgressDialog != null)
						//{
						//	mProgressDialog.dismiss();
						//	mProgressDialog = null;
						//}
						if(mExited == true || items == null)
							return;
						sListItemInfos = items;
						sLastFirstSelection = 0;
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}).start();
	}

	private ArrayList<ListItemInfo> loadFolderListProc()
	{
		FolderInfo folderInfo = null;
		ArrayList<FolderInfo> folders = ImageStore.getFolders(getContext());
		if(folders == null)
			return null;
		ArrayList<ListItemInfo> items = new ArrayList<ListItemInfo>();
		int size = folders.size();
		for(int i = 0; i < size; i++)
		{
			folderInfo = folders.get(i);
			ListItemInfo info = new ListItemInfo();
			info.folderInfo = folderInfo;
			items.add(info);
		}
		return items;
	}

	private OnClickListener mOnItemClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(mItemClickListener != null)
			{
				ListItem item = (ListItem)v;
				ListItemInfo info = item.getItemInfo();
				if(info != null)
				{
					mItemClickListener.onItemClick(info.folderInfo);
				}
			}
		}

	};

	private class ListItem extends RelativeLayout
	{
		public ListItem(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			initialize(context);
		}

		public ListItem(Context context)
		{
			super(context);
			initialize(context);
		}

		public ListItem(Context context, AttributeSet attrs, int defStyle)
		{
			super(context, attrs, defStyle);
			initialize(context);
		}

		private ListItemInfo mItemInfo;
		private RelativeLayout mTitleBar;
		private TextView mTxTitle;
		private TextView mTxNumber;
		private ImageView[] mIcons = new ImageView[4];
		private LinearLayout mLines[] = new LinearLayout[2];
		private LinearLayout mIconHolder;

		public void initialize(Context context)
		{
			setOnClickListener(mOnItemClickListener);

			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.topMargin = ShareData.PxToDpi_hdpi(5);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			RelativeLayout container = new RelativeLayout(context);
			addView(container, params);

			params = new LayoutParams(mItemSize, mItemSize);
			mIconHolder = new LinearLayout(context);
			mIconHolder.setOrientation(LinearLayout.VERTICAL);
			mIconHolder.setBackgroundResource(R.drawable.album_foldericon_bg);
			mIconHolder.setPadding(ShareData.PxToDpi_hdpi(5), ShareData.PxToDpi_hdpi(5), ShareData.PxToDpi_hdpi(5), ShareData.PxToDpi_hdpi(5));
			container.addView(mIconHolder, params);
			mIconHolder.setId(R.id.photo_picker_icon);

			for(int i = 0; i < 2; i++)
			{
				LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
				lparams.weight = 1;
				if(i > 0)
				{
					lparams.topMargin = ShareData.PxToDpi_hdpi(5);
				}
				mLines[i] = new LinearLayout(context);
				mLines[i].setOrientation(LinearLayout.HORIZONTAL);
				mIconHolder.addView(mLines[i], lparams);
				for(int j = 0; j < 2; j++)
				{
					lparams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
					lparams.weight = 1;
					if(j > 0)
					{
						lparams.leftMargin = ShareData.PxToDpi_hdpi(5);
					}
					ImageView imgv = new ImageView(context);
					imgv.setScaleType(ScaleType.CENTER_CROP);
					mLines[i].addView(imgv, lparams);
					mIcons[i * 2 + j] = imgv;
				}
			}

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, R.id.photo_picker_icon);
			mTitleBar = new RelativeLayout(context);
			container.addView(mTitleBar, params);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mTxTitle = new TextView(context);
			mTitleBar.addView(mTxTitle, params);
			mTxTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			mTxTitle.setTextColor(0xffffffff);
			mTxTitle.setId(R.id.photo_picker_title);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.photo_picker_title);
			mTxNumber = new TextView(context);
			mTitleBar.addView(mTxNumber, params);
			mTxNumber.setTextColor(0xff808080);
			mTxNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			mTxNumber.setText("(0)");
			mTxNumber.setId(R.id.photo_picker_number);
		}

		public ListItemInfo getItemInfo()
		{
			return mItemInfo;
		}

		public void setImageBitmap(Bitmap bmp, int index)
		{
			if(index >= 0 && index < mIcons.length)
			{
				if(mItemInfo != null && mItemInfo.folderInfo.imgs.size() == 2)
				{
					index = index == 1 ? 2 : 0;
					mIcons[index].setImageBitmap(bmp);
				}
				else
				{
					mIcons[index].setImageBitmap(bmp);
				}
			}
		}

		public void setItemInfo(ListItemInfo info)
		{
			if(info != null)
			{
				mItemInfo = info;
				mTxTitle.setText(info.folderInfo.folder);
				mTxNumber.setText("(" + info.folderInfo.imgs.size() + ")");
				if(info.folderInfo.folder != null && info.folderInfo.folder.equalsIgnoreCase("EZShare"))
				{
					mTxTitle.setTextColor(0xffc90000);
					mTxNumber.setTextColor(0xffD3B172);
					mTxNumber.setText("(" + getResources().getString(R.string.ImportedText)+ info.folderInfo.imgs.size() + ")");
				}
				else
				{
					mTxTitle.setTextColor(0xffffffff);
					mTxNumber.setTextColor(0xff808080);
				}
				mLines[0].setVisibility(VISIBLE);
				mLines[1].setVisibility(VISIBLE);
				mIcons[0].setVisibility(VISIBLE);
				mIcons[1].setVisibility(VISIBLE);
				mIcons[2].setVisibility(VISIBLE);
				mIcons[3].setVisibility(VISIBLE);
				int size = info.folderInfo.imgs.size();
				if(size < 4)
				{
					switch(size)
					{
						case 3:
							mIcons[3].setVisibility(GONE);
							break;
						case 2:
							mIcons[3].setVisibility(GONE);
							mIcons[1].setVisibility(GONE);
							break;
						case 1:
							mLines[1].setVisibility(GONE);
							mIcons[1].setVisibility(GONE);
							break;
					}
				}
				for(int i = 0; i < mIcons.length; i++)
				{
					Bitmap bmp = getItemBitmap(info, i);
					setImageBitmap(bmp, i);
				}
			}
		}
	}

	//
	class ImageAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return sListItemInfos.size();
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				convertView = new ListItem(getContext());
			}
			ListItem item = (ListItem)convertView;
			item.setItemInfo(sListItemInfos.get(position));
			return convertView;
		}

	}

	//
	static class ListItemInfo
	{
		public FolderInfo folderInfo;
	}

	//取缩略图
	ArrayList<CacheImage> mCacheImages = new ArrayList<CacheImage>();

	private Bitmap getItemBitmap(ListItemInfo info, int index)
	{
		CacheImage cache;
		synchronized(mCacheImages)
		{
			int count = mCacheImages.size();
			for(int i = 0; i < count; i++)
			{
				cache = mCacheImages.get(i);
				if(cache.info == info)
				{
					if(index >= 0 && index < cache.bmps.length)
					{
						if(cache.bmps[index] != null)
						{
							return cache.bmps[index];
						}
					}
					return null;
				}
			}
			//			PLog.out("getItemBitmap");
			if(mCacheImages.size() >= mCacheSize)
			{
				int remove = 0;
				cache = mCacheImages.get(remove);
				for(int i = 0; i < cache.bmps.length; i++)
				{
					cache.bmps[i] = null;
				}
				mCacheImages.remove(remove);
			}
			mCounter = mCacheImages.size() - getItemCount();
			if(mCounter < 0)
				mCounter = 0;
			cache = new CacheImage();
			cache.info = info;
			mCacheImages.add(cache);
			startLoader();
			if(mNeedFastLoad == true && info.folderInfo != null && info.folderInfo.imgs.size() > 0 && info.folderInfo.imgs.get(0).bytes != null)
			{
				startFastLoader();
			}
		}
		return null;
	}

	private int getItemCount()
	{
		int rowNumber = ShareData.m_screenHeight / mItemSize;
		return rowNumber;
	}

	//队列加载
	private Handler mHandler = new Handler();
	private boolean mStarted = false;
	private int mCounter = 0;
	private boolean mNeedFastLoad = false;

	private void startLoader()
	{
		if(mStarted == false)
		{
			new Thread(mLoadThumbRunnable).start();
			mStarted = true;
		}
	}

	private Runnable mLoadThumbRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			CacheImage img;
			ImageInfo imageInfo;
			mIdle = false;
			while(true)
			{
				synchronized(mCacheImages)
				{
					boolean finished = true;
					int count = mCacheImages.size();
					for(int j = 0; j < count; j++)
					{
						img = mCacheImages.get(j);
						if(img.loaded == false)
						{
							finished = false;
							break;
						}
					}
					if(finished == true)
					{
						break;
					}
					mCounter = (mCounter + 1) % mCacheImages.size();
					img = mCacheImages.get(mCounter);
				}
				if(img.loaded == false)
				{
					img.loaded = true;
					final ListItemInfo info = img.info;
					for(int i = 0; i < img.bmps.length && i < info.folderInfo.imgs.size(); i++)
					{
						if(info.folderInfo != null && info.folderInfo.imgs.size() > 0)
						{
							imageInfo = info.folderInfo.imgs.get(i);
							if(imageInfo.bytes == null)
							{
								mNeedFastLoad = true;
							}
							else
							{
								mNeedFastLoad = false;
							}
							img.bmps[i] = ImageStore.getThumbnail(getContext(), imageInfo);
						}
						final int index = i;
						final Bitmap bmp = img.bmps[i];
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								if(mExited == true)
									return;
								updateItemBitmap(info, bmp, index);
							}
						});
						try
						{
							Thread.sleep(10);
						}
						catch(InterruptedException e)
						{
						}
					}
				}
				if(mExited == true)
				{
					break;
				}
			}
			mStarted = false;
			idleProcess();
		}
	};

	private void updateItemBitmap(ListItemInfo itemInfo, Bitmap bmp, int index)
	{
		ListItem listItem;
		int count = mListView.getChildCount();
		for(int i = 0; i < count; i++)
		{
			listItem = (ListItem)mListView.getChildAt(i);
			if(listItem.getItemInfo() == itemInfo)
			{
				listItem.setImageBitmap(bmp, index);
				break;
			}
		}
	}

	private boolean mFastLoadStarted = false;

	private void startFastLoader()
	{
		if(mFastLoadStarted == false)
		{
			new Thread(mFastLoadRunnable).start();
			mFastLoadStarted = true;
		}
	}

	private Runnable mFastLoadRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			mIdle = false;
			ImageInfo imgInfo = null;
			CacheImage img = null;
			while(true)
			{
				synchronized(mCacheImages)
				{
					boolean finished = true;
					int count = mCacheImages.size();
					for(int j = 0; j < count; j++)
					{
						img = mCacheImages.get(j);
						imgInfo = null;
						if(img.info.folderInfo != null && img.info.folderInfo.imgs.size() > 0)
						{
							imgInfo = img.info.folderInfo.imgs.get(0);
						}
						if(img.loaded == false && imgInfo != null && imgInfo.bytes != null)
						{
							finished = false;
							break;
						}
					}
					if(finished == true)
					{
						break;
					}
				}
				if(img == null)
					break;
				if(img.loaded == false)
				{
					img.loaded = true;
					final ListItemInfo info = img.info;
					for(int i = 0; i < img.bmps.length && i < info.folderInfo.imgs.size(); i++)
					{
						img.bmps[i] = ImageStore.getThumbnail(getContext(), info.folderInfo.imgs.get(i));
						final int index = i;
						final Bitmap bmp = img.bmps[i];
						mHandler.post(new Runnable()
						{
							@Override
							public void run()
							{
								if(mExited == true)
									return;
								updateItemBitmap(info, bmp, index);
							}
						});
						try
						{
							Thread.sleep(10);
						}
						catch(InterruptedException e)
						{
						}
					}
				}
				if(mExited == true)
				{
					break;
				}
			}
			mFastLoadStarted = false;
			idleProcess();
		}
	};

	private int mLastVisiblePosition = 0;

	private void idleProcess()
	{
		if(mStarted == false && mFastLoadStarted == false && mExited == false && mIsScrolling == false)
		{
			mIdle = true;
			mLastVisiblePosition = mListView.getLastVisiblePosition();
			startCacheProc();
		}
	}

	private boolean mCachedAll = false;
	private boolean mCacheStarted = false;

	private void startCacheProc()
	{
		if(mCacheStarted == false && mCachedAll == false)
		{
			new Thread(mLoadCacheRunnable).start();
			mCacheStarted = true;
		}
	}

	private Runnable mLoadCacheRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			ListItemInfo info;
			int index = mLastVisiblePosition;
			int count = 0;
			while(true)
			{
				if(index >= 0 && index < sListItemInfos.size())
				{
					info = sListItemInfos.get(index);
					if(info.folderInfo != null && info.folderInfo.imgs.size() > 0)
					{
						for(int i = 0; i < 4 && i < info.folderInfo.imgs.size(); i++)
						{
							ImageInfo imgInfo = info.folderInfo.imgs.get(i);
							if(imgInfo.bytes == null)
							{
								ImageStore.makeCacheBitmap(getContext(), imgInfo);
							}
						}
					}
				}
				count++;
				if(count >= sListItemInfos.size())
				{
					mCachedAll = true;
					break;
				}
				index = (index + 1) % sListItemInfos.size();
				if(mExited == true || mIdle == false)
					break;
				try
				{
					Thread.sleep(1);
				}
				catch(InterruptedException e)
				{
				}
			}
			mCacheStarted = false;
		}

	};

	private class CacheImage
	{
		public ListItemInfo info;
		public Bitmap bmps[] = new Bitmap[4];
		public boolean loaded = false;
	}

	@Override
	public void onClose()
	{
		mExited = true;
	}

	public void onRestore()
	{
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(sLastFirstSelection);
	}

	@Override
	public void onDestroy()
	{
		mExited = true;
	}

	@Override
	public void SetData(HashMap<String, Object> params)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onBack()
	{
		// TODO Auto-generated method stub
	}
}
