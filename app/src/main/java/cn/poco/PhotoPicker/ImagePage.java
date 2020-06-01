package cn.poco.PhotoPicker;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import cn.poco.PhotoPicker.ImageStore.ImageInfo;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.JniUtils;

public class ImagePage extends IPage
{
	public ImagePage(Context context, BaseSite site)
	{
		super(context, site);

		initialize(context);
	}

	private boolean mExited = false;
	private final int ITEM_SIZE = 117;
	private int mCacheSize = 25;
	private ListView mListView;
	private String[] mFolders;
	private ImageAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	private ArrayList<ListItemInfo> mListItemInfos = new ArrayList<ListItemInfo>();
	private OnImageSelectListener mOnImageSelectListener;
	private OnPreChooseImageListener mOnPreChooseImageListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	private boolean mIsScrolling = false;
	private boolean mIdle = false;
	private final int COLNUMBER = 4;

	public interface OnItemLongClickListener
	{
		void onLongClick(View v);
	};

	public interface OnImageSelectListener
	{
		void onSelected(ImageInfo[] imgs);
	};

	public interface OnPreChooseImageListener
	{
		boolean onPreChoose(ImageInfo[] imgs);
	};

	private void initialize(Context context)
	{
		//setBackgroundDrawable(getResources().getDrawable(R.drawable.photofactory_bk));

		mAdapter = new ImageAdapter();

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mListView = new ListView(context);
		mListView.setVerticalFadingEdgeEnabled(false);
		mListView.setAdapter(mAdapter);
		mListView.setDividerHeight(0);
		ColorDrawable c = new ColorDrawable();
		c.setAlpha(0);
		mListView.setSelector(c);
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

	public void setOnImageSelectListener(OnImageSelectListener l)
	{
		mOnImageSelectListener = l;
	}

	public void setOnPreChooseImageListener(OnPreChooseImageListener l)
	{
		mOnPreChooseImageListener = l;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener l)
	{
		mOnItemLongClickListener = l;
	}

	public void setSelected(ImageInfo img, boolean selected)
	{
		ListItem listItem;
		ThumbItem thumbItem;
		ListItemInfo info;
		if(img != null)
		{
			img.selected = selected;
		}
		int count = mListView.getChildCount();
		for(int i = 0; i < count; i++)
		{
			listItem = (ListItem)mListView.getChildAt(i);
			info = listItem.getItemInfo();
			if(info == null)
				continue;
			int find = -1;
			for(int j = 0; j < info.thumbs.size(); j++)
			{
				if(info.thumbs.get(j).img == img)
				{
					find = j;
					break;
				}
			}
			if(find != -1)
			{
				thumbItem = listItem.getThumbItem(find);
				if(thumbItem != null)
				{
					thumbItem.setChecked(selected);
				}
				break;
			}
		}
	}

	public void reload()
	{
		boolean changed = false;
		ListItemInfo info;
		ThumbInfo thumbInfo;
		int size = mListItemInfos.size();
		BREAK: for(int i = 0; i < size; i++)
		{
			info = mListItemInfos.get(i);
			for(int j = 0; j < info.thumbs.size(); j++)
			{
				thumbInfo = info.thumbs.get(j);
				if(thumbInfo.img.deleted == true)
				{
					changed = true;
					break BREAK;
				}
			}
		}
		mExited = false;
		if(changed == true)
		{
			updateList();
		}
		else
		{
			mAdapter.notifyDataSetChanged();
		}
	}

	public void clear()
	{
		ImageStore.clear(false);
		synchronized(mCacheImages)
		{
			mCacheImages.clear();
		}
		mExited = true;
	}

	public void clearSelected()
	{
		ListItem listItem = null;
		ThumbItem thumbItem = null;
		int count = mListView.getChildCount();
		for(int i = 0; i < count; i++)
		{
			listItem = (ListItem)mListView.getChildAt(i);
			int size = listItem.mThumbItems.length;
			for(int j = 0; j < size; j++)
			{
				thumbItem = listItem.getThumbItem(j);
				if(thumbItem != null)
				{
					thumbItem.setChecked(false);
				}
			}
		}
	}

	public ArrayList<ImageInfo> getImages()
	{
		ListItemInfo info;
		ThumbInfo thumbInfo;
		ArrayList<ImageInfo> imgs = new ArrayList<ImageInfo>();
		int size = mListItemInfos.size();
		for(int i = 0; i < size; i++)
		{
			info = mListItemInfos.get(i);
			for(int j = 0; j < info.thumbs.size(); j++)
			{
				thumbInfo = info.thumbs.get(j);
				if(thumbInfo.img.deleted == false)
				{
					imgs.add(thumbInfo.img);
				}
			}
		}
		return imgs;
	}

	public void refresh()
	{
		loadFiles(mFolders);
	}

	public void loadFiles(final String[] folders)
	{
		mFolders = folders;
		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		//mProgressDialog = VideoProgressDialog.show(getContext(), "", "正在加载图片列表...");
		//mProgressDialog.setProgressStyle(VideoProgressDialog.STYLE_SPINNER);
		//mProgressDialog.show();
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				final ArrayList<ListItemInfo> items = loadFileListProc(mFolders);
				mHandler.post(new Runnable()
				{
					@Override
					public void run()
					{
						if(mProgressDialog != null)
						{
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}
						if(mExited == true)
							return;
						if(items != null)
						{
							mListItemInfos = items;
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}).start();
	}

	private Runnable mDeleteCb = null;

	public void delSelImgs(Runnable cb)
	{
		mDeleteCb = cb;
		final int count = ImageStore.getSelCount();
		if(count > 0)
		{
			final Dialog dlg;
			dlg = new Dialog(getContext(), R.style.dialog);
			dlg.setContentView(new alertView(getContext(), new DialogInterface.OnClickListener()
			{

				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if(which == 0)
					{
						if(mProgressDialog != null)
						{
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}
						mProgressDialog = ProgressDialog.show(getContext(), "", getResources().getString(R.string.deletingPhotos) + count);
						mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						mProgressDialog.show();
						deleteSelImgs();
					}
					else if(which == 1)
					{
					}

					dlg.cancel();
				}
			}));
			/*			dlg.setTitle("提示");
			dlg.setMessage("已选中"+count+"张图片，确定要删除吗？");
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mProgressDialog != null)
					{
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
					mProgressDialog = VideoProgressDialog.show(getContext(), "", "正在删除图片...0/"+count);
					mProgressDialog.setProgressStyle(VideoProgressDialog.STYLE_SPINNER);
					mProgressDialog.show();
					deleteSelImgs();
				}
			});
			dlg.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", (DialogInterface.OnClickListener)null);*/
			dlg.show();
		}
	}

	class alertView extends LinearLayout implements View.OnClickListener
	{

		DialogInterface.OnClickListener lst;
		ImageView btnDel;
		ImageView btnCan;

		public alertView(Context context, DialogInterface.OnClickListener lst)
		{
			super(context);

			this.lst = lst;

			LinearLayout child;
			LinearLayout.LayoutParams ll;

			this.setOrientation(LinearLayout.VERTICAL);
//			this.setBackgroundResource(R.drawable.album_dlg_delete_bg);

			TextView temp = new TextView(getContext());
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER;
			ll.topMargin = ShareData.PxToDpi(40);
			temp.setLayoutParams(ll);
			temp.setText(getResources().getString(R.string.deleteItemTips));
			temp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
			temp.setTextColor(0xffc6c6cc);
			this.addView(temp);

			child = new LinearLayout(getContext());
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.BOTTOM;
			ll.topMargin = ShareData.PxToDpi(80);
			child.setLayoutParams(ll);
			child.setOrientation(LinearLayout.HORIZONTAL);
			this.addView(child);

			btnDel = new ImageView(getContext());
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.LEFT | Gravity.BOTTOM;
			ll.leftMargin = ShareData.PxToDpi(20);
			btnDel.setLayoutParams(ll);
//			btnDel.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_dlg_delete_confirm, R.drawable.album_dlg_delete_confirm_hover));
			btnDel.setOnClickListener(this);
			child.addView(btnDel);

			btnCan = new ImageView(getContext());
			ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.RIGHT | Gravity.BOTTOM;
			ll.leftMargin = ShareData.PxToDpi(20);
			btnCan.setLayoutParams(ll);
//			btnCan.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_dlg_delete_cancle, R.drawable.album_dlg_delete_cancle_hover));
			btnCan.setOnClickListener(this);
			child.addView(btnCan);
		}

		@Override
		public void onClick(View v)
		{
			if(v == btnDel)
			{
				lst.onClick(null, 0);
			}
			else if(v == btnCan)
			{
				lst.onClick(null, 1);
			}
		}
	}

	private void deleteSelImgs()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ImageStore.deleteSelImgs(getContext(), mProgressCb);
				Handler handler = new Handler(Looper.getMainLooper());
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						if(mProgressDialog != null)
						{
							mProgressDialog.dismiss();
							mProgressDialog = null;
						}
						if(mDeleteCb != null)
						{
							mDeleteCb.run();
						}
						updateList();
					}
				});
			}
		}).start();

	}

	private void updateList()
	{
		ListItemInfo info;
		ThumbInfo thumbInfo;
		ArrayList<ImageInfo> imgs = new ArrayList<ImageInfo>();
		int size = mListItemInfos.size();
		for(int i = 0; i < size; i++)
		{
			info = mListItemInfos.get(i);
			for(int j = 0; j < info.thumbs.size(); j++)
			{
				thumbInfo = info.thumbs.get(j);
				if(thumbInfo.img.deleted == false)
				{
					imgs.add(thumbInfo.img);
				}
			}
		}
		int iPre = 0;
		ArrayList<ListItemInfo> items = new ArrayList<ListItemInfo>();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		ListItemInfo itemInfo;
		Date d = new Date();
		int yearNow = calendar.get(Calendar.YEAR);
		int monthNow = calendar.get(Calendar.MONTH);
		int dayNow = calendar.get(Calendar.DAY_OF_MONTH);
		long lastModified = 0;
		ImageInfo img;
		size = imgs.size();
		for(int i = 0; i < size;)
		{
			img = imgs.get(i);
			calendar.setTimeInMillis(img.lastModified * 1000);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			if(calendar.getTimeInMillis() / 1000 != lastModified)
			{
				lastModified = calendar.getTimeInMillis() / 1000;
				itemInfo = new ListItemInfo();
				int dayOffset = dayNow - dayOfMonth;
				d.setTime(img.lastModified * 1000);
				if(year == yearNow && month == monthNow && dayOffset < 2 && dayOffset >= 0)
				{
					itemInfo.title = DAYS[dayOffset];
					itemInfo.title2 = df.format(d);
					itemInfo.title2 += "(" + WEEKS[dayOfWeek] + ")";
				}
				else
				{
					itemInfo.title = df.format(d);
					itemInfo.title2 = "(" + WEEKS[dayOfWeek] + ")";
				}
				itemInfo.type = ListItemInfo.TYPE_TITLE;
				items.add(itemInfo);
			}
			BREAK: for(; i < size;)
			{
				itemInfo = new ListItemInfo();
				itemInfo.type = ListItemInfo.TYPE_ICON;
				iPre = i;
				for(int k = 0; k < COLNUMBER; k++)
				{
					if(i < size)
					{
						img = imgs.get(i);
						calendar.setTimeInMillis(img.lastModified * 1000);
						calendar.set(Calendar.HOUR, 0);
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
						if(calendar.getTimeInMillis() / 1000 == lastModified)
						{
							thumbInfo = new ThumbInfo();
							thumbInfo.img = img;
							itemInfo.thumbs.add(thumbInfo);
							i++;
						}
						else
						{
							if(i > iPre)
							{
								items.add(itemInfo);
							}
							break BREAK;
						}
					}
				}
				if(i > iPre)
				{
					items.add(itemInfo);
				}
			}
		}
		mListItemInfos = items;
		mAdapter.notifyDataSetChanged();
	}

	private ImageStore.ProgressListener mProgressCb = new ImageStore.ProgressListener()
	{

		@Override
		public void onProgress(final int size, final int index)
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					if(mProgressDialog != null)
					{
						mProgressDialog.setMessage(getResources().getString(R.string.deletingPhotosOther) + index + "/" + size);
					}
				}
			});
		}

	};

	private ArrayList<ListItemInfo> loadFileListProc(String[] folders)
	{
		ArrayList<ImageInfo> group = null;
		ImageInfo imgInfo = null;
		ArrayList<ImageInfo> imgs = ImageStore.getImages(getContext(), folders);
		if(imgs == null)
			return null;
		ArrayList<ListItemInfo> items = new ArrayList<ListItemInfo>();
		ImageInfo[] aimgs = imgs.toArray(new ImageInfo[imgs.size()]);
		ArrayList<ArrayList<ImageInfo>> groups = new ArrayList<ArrayList<ImageInfo>>();
		HashMap<Long, ArrayList<ImageInfo>> maps = new HashMap<Long, ArrayList<ImageInfo>>();
		long date;
		Date d = new Date();
		int size = aimgs.length;
		for(int i = 0; i < size; i++)
		{
			imgInfo = aimgs[i];
			if(JniUtils.imgFilter(imgInfo.image))
			{
				continue;
			}
			d.setTime(imgInfo.lastModified * 1000);
			d.setHours(0);
			d.setMinutes(0);
			d.setSeconds(0);
			date = d.getTime();
			group = maps.get(date);
			if(group == null)
			{
				group = new ArrayList<ImageInfo>();
				maps.put(date, group);
				groups.add(group);
			}
			group.add(imgInfo);
		}
		Collections.sort(groups, new Comparator<ArrayList<ImageInfo>>()
		{
			@Override
			public int compare(ArrayList<ImageInfo> object1, ArrayList<ImageInfo> object2)
			{
				if(object1.size() > 0 && object2.size() > 0)
				{
					return object1.get(0).lastModified < object2.get(0).lastModified ? 1 : -1;
				}
				return 0;
			}
		});
		Calendar calendar = Calendar.getInstance();
		int yearNow = calendar.get(Calendar.YEAR);
		int monthNow = calendar.get(Calendar.MONTH);
		int dayNow = calendar.get(Calendar.DAY_OF_MONTH);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
		for(ArrayList<ImageInfo> g : groups)
		{
			if(g != null && (size = g.size()) > 0)
			{
				date = g.get(0).lastModified * 1000;
				calendar.setTimeInMillis(date);
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
				int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

				ListItemInfo itemInfo = new ListItemInfo();
				int dayOffset = dayNow - dayOfMonth;
				d.setTime(date);
				if(year == yearNow && month == monthNow && dayOffset < 2 && dayOffset >= 0)
				{
					itemInfo.title = DAYS[dayOffset];
					itemInfo.title2 = df.format(d);
					itemInfo.title2 += "(" + WEEKS[dayOfWeek] + ")";
				}
				else
				{
					itemInfo.title = df.format(d);
					itemInfo.title2 = "(" + WEEKS[dayOfWeek] + ")";
				}
				itemInfo.type = ListItemInfo.TYPE_TITLE;
				items.add(itemInfo);
				for(int i = 0; i < size;)
				{
					itemInfo = new ListItemInfo();
					itemInfo.type = ListItemInfo.TYPE_ICON;
					items.add(itemInfo);
					for(int j = 0; j < COLNUMBER; j++)
					{
						if(i < size)
						{
							ThumbInfo info = new ThumbInfo();
							info.img = g.get(i);
							itemInfo.thumbs.add(info);
							i++;
						}
					}
				}
			}
		}
		return items;
	}

	private String WEEKS[] = new String[]{"周一", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
	private String DAYS[] = new String[]{"今天", "昨天"};

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		if(w > 0 && h > 0)
		{
			mCacheSize = (h / ITEM_SIZE + 1) * COLNUMBER;
		}
	}

	public static int getRealPixel2(int pxSrc)
	{
		return (int)(pxSrc * ShareData.m_screenWidth / 480);
	}

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
		private LinearLayout mIconGroup;
		private RelativeLayout mTitleBar;
		//private ImageView mLine;
		private TextView mTxTitle;
		private TextView mTxTitle1;
		private ThumbItem[] mThumbItems = new ThumbItem[COLNUMBER];

		public void initialize(Context context)
		{
			//setBackgroundDrawable(getResources().getDrawable(R.drawable.photofactory_bk));
			//setBackgroundColor(0xff333333);

			int space = (ShareData.m_screenWidth - getRealPixel2(ITEM_SIZE) * COLNUMBER) / COLNUMBER;

			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			params.topMargin = ShareData.PxToDpi_hdpi(space);
			params.bottomMargin = ShareData.PxToDpi_hdpi(space);
			mIconGroup = new LinearLayout(context);
			addView(mIconGroup, params);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			//params.addRule(RelativeLayout.BELOW, 1);
			params.topMargin = ShareData.PxToDpi_hdpi(20);
			params.bottomMargin = ShareData.PxToDpi_hdpi(10);
			mTitleBar = new RelativeLayout(context);
			addView(mTitleBar, params);
			mTitleBar.setVisibility(GONE);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.bottomMargin = ShareData.PxToDpi_hdpi(10);
			mTxTitle = new TextView(context);
			mTitleBar.addView(mTxTitle, params);
			mTxTitle.setTextSize(20);
			mTxTitle.setTextColor(0xffffffff);
			mTxTitle.setId(R.id.photo_picker_title2);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.photo_picker_title2);
			params.leftMargin = ShareData.PxToDpi_hdpi(5);
			params.bottomMargin = ShareData.PxToDpi_hdpi(10);
			mTxTitle1 = new TextView(context);
			mTitleBar.addView(mTxTitle1, params);
			mTxTitle1.setTextSize(14);
			mTxTitle1.setTextColor(0xff808080);

			LinearLayout.LayoutParams lparams = null;
			ThumbItem item = null;
			for(int i = 0; i < mThumbItems.length; i++)
			{
				lparams = new LinearLayout.LayoutParams(getRealPixel2(ITEM_SIZE), getRealPixel2(ITEM_SIZE));
				lparams.weight = 1;
				if(i != 0)
				{
					lparams.leftMargin = ShareData.PxToDpi_hdpi(space);
				}
				item = new ThumbItem(context);
				mIconGroup.addView(item, lparams);
				item.setOnClickListener(mOnItemClickListener);
				item.setLongClickable(true);
				item.setOnLongClickListener(mOnLongClickListener);
				mThumbItems[i] = item;
			}
		}

		public void hideLine()
		{
			//mLine.setVisibility(GONE);
		}

		public ListItemInfo getItemInfo()
		{
			return mItemInfo;
		}

		public void setItemInfo(ListItemInfo info)
		{
			ThumbInfo thumbInfo;
			mItemInfo = info;
			if(info.type == ListItemInfo.TYPE_ICON)
			{
				mTitleBar.setVisibility(GONE);
				mIconGroup.setVisibility(VISIBLE);
				//mLine.setVisibility(GONE);
				ThumbItem item = null;
				for(int i = 0; i < mThumbItems.length; i++)
				{
					item = mThumbItems[i];
					if(i < info.thumbs.size())
					{
						thumbInfo = info.thumbs.get(i);
						item.setImageBitmap(getItemBitmap(item, thumbInfo));
						item.setChecked(thumbInfo.img.selected);
						item.setVisibility(VISIBLE);
					}
					else
					{
						item.setVisibility(INVISIBLE);
					}
				}
			}
			else if(info.type == ListItemInfo.TYPE_TITLE)
			{
				mTitleBar.setVisibility(VISIBLE);
				mIconGroup.setVisibility(GONE);
				mTxTitle.setText(info.title);
				//mLine.setVisibility(VISIBLE);
				if(info.title2 != null)
				{
					mTxTitle1.setText(info.title2);
				}
			}
		}

		public ThumbItem getThumbItem(int index)
		{
			if(index >= 0 && index < mThumbItems.length)
			{
				return mThumbItems[index];
			}
			return null;
		}

		private boolean mIsLongClick = false;
		private OnLongClickListener mOnLongClickListener = new OnLongClickListener()
		{

			@Override
			public boolean onLongClick(View v)
			{
				/*if(mOnItemLongClickListener != null)
				{
					mOnItemLongClickListener.onLongClick(v);
				}
				mIsLongClick = true;
				for(int i = 0; i < mThumbItems.length; i++)
				{
					if(mThumbItems[i] == v)
					{
						ThumbInfo item = mItemInfo.thumbs.get(i);
						item.img.selected = true;
						mThumbItems[i].setChecked(true);
						break;
					}
				}*/
				return false;
			}

		};

		private OnClickListener mOnItemClickListener = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mIsLongClick)
				{
					mIsLongClick = false;
					return;
				}
				if(mItemInfo != null)
				{
					for(int i = 0; i < mThumbItems.length; i++)
					{
						if(mThumbItems[i] == v)
						{
							ThumbInfo item = mItemInfo.thumbs.get(i);
							boolean handled = false;
							if(mOnPreChooseImageListener != null)
							{
								handled = mOnPreChooseImageListener.onPreChoose(new ImageInfo[]{item.img});
							}
							if(handled == false)
							{
								item.img.selected = !item.img.selected;
								mThumbItems[i].setChecked(item.img.selected);
								if(mOnImageSelectListener != null)
								{
									mOnImageSelectListener.onSelected(new ImageInfo[]{item.img});
								}
							}
							break;
						}
					}
				}
			}
		};
	}

	private class ThumbItem extends RelativeLayout
	{

		public ThumbItem(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			initialize(context);
		}

		public ThumbItem(Context context)
		{
			super(context);
			initialize(context);
		}

		public ThumbItem(Context context, AttributeSet attrs, int defStyle)
		{
			super(context, attrs, defStyle);
			initialize(context);
		}

		private ImageView mImage;
		private ImageView mIvSelected;

		public void initialize(Context context)
		{
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			mImage = new ImageView(context);
			mImage.setScaleType(ScaleType.CENTER_CROP);
			addView(mImage, params);
			//mImage.setId(1);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.setMargins(0, 0, ShareData.PxToDpi_hdpi(10), ShareData.PxToDpi_hdpi(10));
			mIvSelected = new ImageView(context);
			addView(mIvSelected, params);
			mIvSelected.setImageResource(R.drawable.album_selected_icon);
			mIvSelected.setClickable(false);
			mIvSelected.setVisibility(GONE);
			//mIvSelected.setId(2);
		}

		public void setImageBitmap(final Bitmap bmp)
		{
			mImage.setImageBitmap(bmp);
		}

		public void setChecked(boolean checked)
		{
			mIvSelected.setVisibility(checked == true ? VISIBLE : GONE);
		}
	}

	//
	class ImageAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return mListItemInfos.size();
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
			item.setItemInfo(mListItemInfos.get(position));
			if(position == 0)
			{
				item.hideLine();
			}
			return convertView;
		}

	}

	//
	class ThumbInfo
	{
		public ImageInfo img;
	}

	//
	static class ListItemInfo
	{
		public static final int TYPE_ICON = 1;
		public static final int TYPE_TITLE = 2;
		public ArrayList<ThumbInfo> thumbs = new ArrayList<ThumbInfo>();
		public String title;
		public String title2;
		public int type = TYPE_ICON;
	}

	//取缩略图
	ArrayList<CacheImage> mCacheImages = new ArrayList<CacheImage>();

	private Bitmap getItemBitmap(ThumbItem item, ThumbInfo info)
	{
		CacheImage cache;
		synchronized(mCacheImages)
		{
			int count = mCacheImages.size();
			for(int i = 0; i < count; i++)
			{
				cache = mCacheImages.get(i);
				if(cache.thumbInfo == info)
				{
					return cache.bmp;
				}
			}
			if(mCacheImages.size() >= mCacheSize)
			{
				int remove = 0;
				cache = mCacheImages.get(remove);
				cache.bmp = null;
				mCacheImages.remove(remove);
			}
			mCounter = mCacheImages.size() - getItemCount();
			if(mCounter < 0)
				mCounter = 0;
			cache = new CacheImage();
			cache.thumbInfo = info;
			mCacheImages.add(cache);
			startLoader();
			if(info.img.bytes != null && mNeedFastLoad == true)
			{
				startFastLoader();
			}
		}
		return null;
	}

	private int getItemCount()
	{
		int s = mListView.getFirstVisiblePosition();
		int e = mListView.getLastVisiblePosition();
		int rowNumber = Math.abs(e - s + 1);
		int count = 15;
		if(rowNumber > 0)
		{
			count = rowNumber * COLNUMBER;
		}
		return count;
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
			mIdle = false;
			CacheImage img;
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
					final ThumbInfo thumbInfo = img.thumbInfo;
					if(thumbInfo.img.bytes == null)
					{
						mNeedFastLoad = true;
					}
					else
					{
						mNeedFastLoad = false;
					}
					img.bmp = ImageStore.getThumbnail(getContext(), thumbInfo.img);
					final Bitmap bmp = img.bmp;
					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(mExited == true)
								return;
							updateItemBitmap(thumbInfo, bmp);
						}
					});
					try
					{
						Thread.sleep(1);
					}
					catch(InterruptedException e)
					{
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

	private void updateItemBitmap(ThumbInfo thumbInfo, Bitmap bmp)
	{
		ListItem listItem;
		ThumbItem thumbItem;
		ListItemInfo info;
		int count = mListView.getChildCount();
		for(int i = 0; i < count; i++)
		{
			listItem = (ListItem)mListView.getChildAt(i);
			info = listItem.getItemInfo();
			if(info == null)
				continue;
			int find = -1;
			for(int j = 0; j < info.thumbs.size(); j++)
			{
				if(info.thumbs.get(j) == thumbInfo)
				{
					find = j;
					break;
				}
			}
			if(find != -1)
			{
				thumbItem = listItem.getThumbItem(find);
				if(thumbItem != null)
				{
					thumbItem.setImageBitmap(bmp);
				}
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
						if(img.loaded == false && img.thumbInfo.img.bytes != null)
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
					final ThumbInfo thumbInfo = img.thumbInfo;
					img.bmp = ImageStore.getThumbnail(getContext(), thumbInfo.img);
					final Bitmap bmp = img.bmp;
					mHandler.post(new Runnable()
					{
						@Override
						public void run()
						{
							if(mExited == true)
								return;
							updateItemBitmap(thumbInfo, bmp);
						}
					});
					try
					{
						Thread.sleep(1);
					}
					catch(InterruptedException e)
					{
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
			ThumbInfo thumb;
			int index = mLastVisiblePosition;
			int count = 0;
			while(true)
			{
				if(index >= 0 && index < mListItemInfos.size())
				{
					info = mListItemInfos.get(index);
					for(int i = 0; i < info.thumbs.size(); i++)
					{
						thumb = info.thumbs.get(i);
						if(thumb.img.bytes == null)
						{
							ImageStore.makeCacheBitmap(getContext(), thumb.img);
						}
						if(mExited == true || mIdle == false)
							break;
					}

				}
				count++;
				if(count >= mListItemInfos.size())
				{
					mCachedAll = true;
					break;
				}
				index = (index + 1) % mListItemInfos.size();
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
		public ThumbInfo thumbInfo;
		public Bitmap bmp = null;
		public boolean loaded = false;
	}

	@Override
	public void onClose()
	{
		mExited = true;
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
