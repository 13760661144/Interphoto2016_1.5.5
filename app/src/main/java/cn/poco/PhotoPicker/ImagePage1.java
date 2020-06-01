package cn.poco.PhotoPicker;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.MyImageLoader;

/**
 * Created by admin on 2016/4/25.
 */
public class ImagePage1 extends IPage
{
	private final int ITEM_SIZE = 117;
	private final int COLNUMBER = 3;
	private boolean mIsScrolling = false;
	private boolean mIdle = false;
	private GridView mGridView;
	private ImageAdapter mAdapter;
	private MyImageLoader mLoader;
	private String[] mFolders;
	private ProgressDialog mProgressDialog;
	private Handler mHandler = new Handler();
	private boolean mExited = false;
	private ArrayList<ImageStore.ImageInfo> mThumbs = new ArrayList<ImageStore.ImageInfo>();
	private OnImageSelectListener mOnImageSelectListener;
	private OnPreChooseImageListener mOnPreChooseImageListener;
	private OnItemLongClickListener mOnItemLongClickListener;
	public interface OnItemLongClickListener
	{
		void onLongClick(View v);
	};

	public interface OnImageSelectListener
	{
		void onSelected(ImageStore.ImageInfo[] imgs);
	};

	public interface OnPreChooseImageListener
	{
		boolean onPreChoose(ImageStore.ImageInfo[] imgs);
	};

	public ImagePage1(Context context, BaseSite site)
	{
		super(context, site);
	}

	@Override
	public void SetData(HashMap<String, Object> params)
	{

	}

	@Override
	public void onBack()
	{

	}

	protected void initialize(Context context)
	{
		mAdapter = new ImageAdapter();
		mLoader = new MyImageLoader();

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mGridView = new GridView(context);
		mGridView.setVerticalFadingEdgeEnabled(false);
		mGridView.setAdapter(mAdapter);
		ColorDrawable c = new ColorDrawable();
		c.setAlpha(0);
		mGridView.setSelector(c);
		mGridView.setCacheColorHint(0x00000000);
		addView(mGridView, params);
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
				final ArrayList<ImageStore.ImageInfo> items = loadFileListProc(mFolders);
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
							mThumbs = items;
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		}).start();
	}

	private ArrayList<ImageStore.ImageInfo> loadFileListProc(String[] folders)
	{
		ArrayList<ImageStore.ImageInfo> infos = ImageStore.getImages(getContext(), folders);
		return infos;
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
			mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
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

	class ImageAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			return mThumbs.size();
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return null;
		}
	}
}
