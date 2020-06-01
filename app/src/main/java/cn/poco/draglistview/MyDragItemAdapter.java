package cn.poco.draglistview;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.io.File;
import java.io.InputStream;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.MyImageLoader;
import cn.poco.utils.Utils;

/**
 * RecyclerView 用到的Adapter
 */
public class MyDragItemAdapter extends DragItemAdapter<DragListItemInfo, MyDragItemAdapter.DragHolder>
{
	protected int m_thumbW;
	protected int m_thumbH;
	protected int m_headW;
	protected int m_headH;
	protected int m_defRoundSize;

	protected Context m_context;
	protected MyImageLoader m_loader;
	private int m_curSelIndex = -1;
	private int m_curSelUri = DragListItemInfo.URI_NONE;
	private OnItemClickListener m_clickListener;
	protected boolean m_showTitle = true;

	public MyDragItemAdapter(Context context, boolean dragOnLongPress)
	{
		super(dragOnLongPress);
		m_thumbW = m_thumbH = ShareData.PxToDpi_xhdpi(140);
		m_headW = m_headH = ShareData.PxToDpi_xhdpi(60);
		m_defRoundSize = ShareData.PxToDpi_xhdpi(10);
		m_context = context;
		m_loader = new MyImageLoader();
		setHasStableIds(true);
	}

	@Override
	public DragHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		MyListItem item = MakeItem(m_context);
		DragHolder holder = new DragHolder(item, item.m_thumb);
		return holder;
	}

	@Override
	public void onBindViewHolder(DragHolder holder, int position)
	{
		super.onBindViewHolder(holder, position);
		DragListItemInfo info = mItemList.get(position);
		String key = info.m_uri + info.m_key + "_thumb";

		final MyListItem item = (MyListItem)holder.itemView;
		item.SetData(info);
		item.ShowTitle(m_showTitle);
		if(info.m_style == DragListItemInfo.Style.LOADING)
		{
			if(info.m_progress < 30){
				item.SetProgress(30);
			}
			else{

				item.SetProgress(info.m_progress);
			}
		}
		else
		{
			item.SetProgress(0);
		}
		Bitmap bmp = m_loader.loadBmp(new MyImageLoader.LoadItem(key, info), new MyImageLoader.ImageLoadCallback()
		{
			@Override
			public void onLoadFinished(Bitmap bmp, Object res)
			{
				item.SetThumb(bmp);
			}

			@Override
			public Bitmap makeBmp(Object res)
			{
				return MakeThumbBmp((DragListItemInfo)res);
			}
		});
		item.SetThumb(bmp);
		if(info.m_selected && info.m_uri != DragListItemInfo.URI_ORIGIN)
		{
			bmp = null;
			if(info.text_bg_color_over != 0)
			{
				bmp = ImageUtils.MakeColorRoundBmp(info.text_bg_color_over, m_thumbW, m_thumbH, m_defRoundSize);
			}
			if(bmp != null)
			{
				item.SetOver(bmp);
			}

			if(info.m_head != null)
			{
				key = info.m_uri + info.m_key + "_head";
				bmp = m_loader.loadBmp(new MyImageLoader.LoadItem(key, info), new MyImageLoader.ImageLoadCallback()
				{
					@Override
					public void onLoadFinished(Bitmap bmp, Object res)
					{
						item.SetHead(bmp);
					}

					@Override
					public Bitmap makeBmp(Object res)
					{
						return MakeHeadBmp(((DragListItemInfo)res).m_head);
					}
				});
				item.SetHead(bmp);
			}
			else
			{
				item.SetHead(null);
			}
		}
		else
		{
			bmp = null;
			if(info.text_bg_color_out != 0)
			{
				bmp = MakeBgOut(info.text_bg_color_out);
				if(bmp != null)
				{
					item.SetOut(bmp);
				}
			}
			else
			{
				item.SetOut(bmp);
			}
		}
		item.ShowLock(info.m_isLock);
		item.ShowRecomment(info.m_isRecomment);
	}

	@Override
	public void onViewDetachedFromWindow(DragHolder holder)
	{
		super.onViewDetachedFromWindow(holder);
		holder.m_view.m_titleFr.clearAnimation();
	}

	public  MyListItem MakeItem(Context context)
	{
		MyListItem item = new MyListItem(context);
		item.m_thumbW = item.m_thumbH = ShareData.PxToDpi_xhdpi(140);
		item.m_headW = item.m_headH = ShareData.PxToDpi_xhdpi(60);
		item.m_textPadding = ShareData.PxToDpi_xhdpi(10);
		item.m_textSize = 11;
		item.m_authorSize = 9;
		item.m_thumbTopMargin = ShareData.PxToDpi_xhdpi(20);
		item.m_lockMargin = ShareData.PxToDpi_xhdpi(5);
		item.m_textColorOut = item.m_textColorOver = 0xffffffff;
		item.m_leftMargin = item.m_rigthMargin = ShareData.PxToDpi_xhdpi(10);
		item.m_tipBottomMargin = ShareData.PxToDpi_xhdpi(10);
		item.m_roundSize = 2;
		item.Init();
		return item;
	}

	protected Bitmap MakeThumbBmp(DragListItemInfo res)
	{
		Bitmap out = null;

		if(res != null)
		{
			if(res.m_uri == DragListItemInfo.URI_MYTEXT)
			{
				out = MakeMyTextBmp(m_context);
			}
			else if(res.m_uri == DragListItemInfo.URI_MGR)
			{
				out = MakeDownloadMoreBmp(m_context);
				return out;
			}
			else if(res.m_logo instanceof String)
			{
				File file = new File((String)res.m_logo);
				if(file.exists())
				{
					int rotate = 0;
					if(res.m_uri == DragListItemInfo.URI_ORIGIN)
					{
						rotate = CommonUtils.GetImgInfo((String)res.m_logo)[0];
					}
					Bitmap temp = Utils.DecodeImage(m_context, res.m_logo, rotate, -1, m_thumbW, m_thumbH);
					out = MakeBmpV2.CreateBitmapV2(temp, rotate, 0, -1, m_thumbW, m_thumbH, Bitmap.Config.ARGB_8888);
				}
				else {
					try
					{
						InputStream is = m_context.getAssets().open((String)res.m_logo);
						out = BitmapFactory.decodeStream(is);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(res.m_logo instanceof Bitmap)
			{
				out = (Bitmap)res.m_logo;
			}
			else
			{
				Bitmap temp = Utils.DecodeImage(m_context, res.m_logo, 0, -1, m_thumbW, m_thumbH);
				out = MakeBmpV2.CreateBitmapV2(temp, 0, 0, -1, m_thumbW, m_thumbH, Bitmap.Config.ARGB_8888);
			}
			if(out != null)
			{
				Bitmap temp = out;
				out = ImageUtils.MakeRoundBmp(temp, m_thumbW, m_thumbH, m_defRoundSize);
			}
		}

		return out;
	}

	public Bitmap MakeMyTextBmp(Context context)
	{
		String text = context.getResources().getString(R.string.Mine);
		int bmpSize = ShareData.PxToDpi_xhdpi(140);
		Bitmap temp = Bitmap.createBitmap(bmpSize, bmpSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(temp);
		canvas.drawColor(0xff434a54);
		Paint pt = new Paint();
		pt.setAntiAlias(true);
		pt.setColor(Color.WHITE);
		pt.setTextSize(ShareData.PxToDpi_xhdpi(20));
		float textWidth = pt.measureText(text);
		float textHeight = pt.descent() - pt.ascent();
		float space = ShareData.PxToDpi_xhdpi(5);
		float imgSize = ShareData.PxToDpi_xhdpi(48);
		float imgX = (bmpSize - imgSize) / 2f;
		float imgY = (bmpSize - imgSize - space - textHeight) / 2f;
		float textX = (bmpSize - textWidth) / 2f;
		float textY = imgY + space + imgSize + textHeight;
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.beauty_text_my_btn);
//		System.out.println("size1: " + imgSize);
//		System.out.println("size2: " + bmp.getHeight());
		Matrix matrix = new Matrix();
		matrix.setTranslate(imgX, imgY);
		canvas.drawBitmap(bmp, matrix, null);
		canvas.drawText(text, textX, textY, pt);
		return temp;
	}

	public Bitmap MakeDownloadMoreBmp(Context context)
	{
		int bmpWidth = ShareData.PxToDpi_xhdpi(140);
		int bmpHeight = ShareData.PxToDpi_xhdpi(140);
		Bitmap temp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(temp);
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.beauty_mgr_logo);
		Matrix matrix = new Matrix();
		matrix.setTranslate((bmpWidth - bmp.getWidth()) / 2, (bmpHeight - bmp.getHeight()) / 2);
		canvas.drawColor(0xffffc433);
		canvas.drawBitmap(bmp, matrix, null);
		temp = ImageUtils.MakeRoundBmp(temp, m_thumbW, m_thumbH, m_defRoundSize);
		return temp;
	}

	public Bitmap MakeHeadBmp(Object res)
	{
		Bitmap out = null;
		Bitmap temp = null;

		if(res != null)
		{
			if(res instanceof Integer)
			{
				temp = BitmapFactory.decodeResource(m_context.getResources(), (Integer)res);
			}
			else if(res instanceof String && ((String)res).length() > 0)
			{
				File file = new File((String)res);
				if(file.exists())
				{
					temp = BitmapFactory.decodeFile((String)res);
				}
				else
				{
					try
					{
						InputStream is = m_context.getAssets().open((String)res);
						temp = BitmapFactory.decodeStream(is);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			else if(res instanceof Bitmap)
			{
				temp = (Bitmap)res;
			}
			if(temp != null)
			{
				Bitmap temp1  = MakeBmpV2.CreateBitmapV2(temp, 0, 0, -1, m_headW, m_headH, Bitmap.Config.ARGB_8888);
				out = cn.poco.utils.ImageUtil.makeCircleBmp(temp1, 2, 0xffffffff);
			}
		}

		return out;
	}

	protected Bitmap MakeBgOut(int color)
	{
		Bitmap out = null;
		Bitmap temp = ImageUtils.MakeColorRoundBmp(color, m_thumbW, m_thumbH / 2, 0);
		if(temp != null)
		{
			out = cn.poco.utils.ImageUtil.MakeDiffCornerRoundBmp(temp, 0, 0, m_defRoundSize, m_defRoundSize);
		}
		return out;
	}

	@Override
	public long getItemId(int position)
	{
		return mItemList.get(position).m_uri;
	}

	@Override
	public int getPositionForItemId(long id)
	{
		return super.getPositionForItemId(id);
	}

	public void SetSelByIndex(int index)
	{
		if(m_curSelIndex != -1 && m_curSelIndex < getItemCount())
		{
			DragListItemInfo info = mItemList.get(m_curSelIndex);
			if(info != null  && info.m_uri == m_curSelUri
					&& info.m_uri != DragListItemInfo.URI_ORIGIN && info.m_uri != DragListItemInfo.URI_MYTEXT)
			{
				info.m_selected = false;
				info.m_selectAnim = true;
			}
		}
		m_curSelIndex = index;
		if(m_curSelIndex != -1 && m_curSelIndex < getItemCount())
		{
			DragListItemInfo info = mItemList.get(m_curSelIndex);
			m_curSelUri = info.m_uri;
			if(info != null && info.m_uri != DragListItemInfo.URI_ORIGIN && info.m_uri != DragListItemInfo.URI_MYTEXT)
			{
				info.m_selected = true;
				info.m_selectAnim = true;
			}
		}
		notifyDataSetChanged();
	}

	public void SetOnClickCallback(OnItemClickListener lis)
	{
		m_clickListener = lis;
	}

	public int SetSelByUri(int uri)
	{
		m_curSelIndex = GetIndexByUri(m_curSelUri);
		int index = GetIndexByUri(uri);
		SetSelByIndex(index);
		return index;
	}

	/**
	 *
	 * @return
	 */
	public void SelCurUri()
	{
		m_curSelIndex = GetIndexByUri(m_curSelUri);
		if(m_curSelIndex != -1 && m_curSelIndex < getItemCount())
		{
			DragListItemInfo info = mItemList.get(m_curSelIndex);
			if(info != null  && info.m_uri == m_curSelUri
					&& info.m_uri != DragListItemInfo.URI_ORIGIN && info.m_uri != DragListItemInfo.URI_MYTEXT)
			{
				info.m_selected = true;
				info.m_selectAnim = false;
			}
		}
	}

	public void SetItemStyleByUri(int uri, DragListItemInfo.Style style)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			DragListItemInfo info = mItemList.get(index);
			info.m_style = style;
			notifyItemChanged(index);
		}
	}

	public void Unlock(int uri)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			DragListItemInfo info = mItemList.get(index);
			info.m_isLock = false;
			notifyItemChanged(index);
		}
	}

	public void Lock(int uri)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			DragListItemInfo info = mItemList.get(index);
			info.m_isLock = true;
			notifyItemChanged(index);
		}
	}

	public void SetRecomment(int uri, boolean recomment)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			DragListItemInfo info = mItemList.get(index);
			info.m_isRecomment = recomment;
			notifyItemChanged(index);
		}
	}

	public DragListItemInfo GetItemInfoByUri(int uri)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			return mItemList.get(index);
		}
		return null;
	}

	public void SetItemProgress(int uri, int progress)
	{
		int index = GetIndexByUri(uri);
		if(index >= 0)
		{
			DragListItemInfo info = mItemList.get(index);
			info.m_progress = progress;
			notifyItemChanged(index);
		}
	}

	public int GetIndexByUri(int uri)
	{
		if(mItemList != null)
		{
			int count = getItemCount();
			for(int i = 0; i < count; i ++)
			{
				if(mItemList.get(i).m_uri == uri)
				{
					return i;
				}
			}
		}
		return -1;
	}

	public void showTitle(boolean show)
	{
		m_showTitle = show;
	}

	public void ReleaseMem()
	{
		if(m_loader != null)
		{
			m_loader.releaseMem1();
		}
	}

	public void ClearAll()
	{
		if(m_loader != null)
		{
			m_loader.releaseMem();
		}
		if(mItemList != null)
		{
			mItemList.clear();
			mItemList = null;
			notifyDataSetChanged();
		}
		m_context = null;
		m_loader = null;
		m_clickListener = null;
		setDragStartedListener(null);
	}

	public class DragHolder extends DragItemAdapter.ViewHolder
	{
		MyListItem m_view;
		public DragHolder(View itemView, View grabView)
		{
			super(itemView, grabView);
			m_view = (MyListItem)itemView;
			if(m_view != null)
			{
				m_view.m_thumb.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(m_clickListener != null)
						{
							m_clickListener.OnItemClick(m_view, m_view.GetData(), getAdapterPosition());
						}
					}
				});
				m_view.m_head.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(m_clickListener != null)
						{
							m_clickListener.OnHeadClick(m_view, m_view.GetData(), getAdapterPosition());
						}
					}
				});
			}
		}


		@Override
		public void onItemClicked(View view)
		{
			super.onItemClicked(view);
		}

		@Override
		public boolean onItemLongClicked(View view)
		{
			Vibrator vib = (Vibrator)m_context.getSystemService(Service.VIBRATOR_SERVICE);
			if(vib != null)
			{
				vib.vibrate(30);
			}
			return super.onItemLongClicked(view);
		}
	}

	public static interface OnItemClickListener
	{
		public void OnItemClick(View view, DragListItemInfo info, int index);
		public void OnHeadClick(View view, DragListItemInfo info, int index);
	}

	public static class MyDragItem extends DragItem {
		protected int recycleViewY;
		protected boolean m_alphaAnim = false;

		public void DoAlphaAnim(boolean flag)
		{
			m_alphaAnim = flag;
		}

		public MyDragItem(Context context)
		{
			super(context);
		}

		public MyDragItem(Context context, View view)
		{
			super(context, view);
			recycleViewY = ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(300);
		}

		@Override
		public void onBindDragView(View clickedView, View dragView) {
			int padding = ShareData.PxToDpi_xhdpi(3);
			dragView.setPadding(padding, padding, padding, padding);
			if(clickedView instanceof MyListItem)
			{
				MyListItem clickItem = (MyListItem)clickedView;
				Bitmap bitmap = Bitmap.createBitmap(clickItem.m_container.getWidth(), clickItem.m_container.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				clickItem.m_container.draw(canvas);
				if(dragView instanceof ImageView)
				{
					((ImageView)dragView).setImageBitmap(bitmap);
				}
				Bitmap bmp = ImageUtils.MakeColorRoundBmp(0xffffc433, bitmap.getWidth() + padding, bitmap.getHeight() + padding, ShareData.PxToDpi_xhdpi(10));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					dragView.setBackground(new BitmapDrawable(clickedView.getResources(), bmp));
				} else {
					//noinspection deprecation
					dragView.setBackgroundDrawable(new BitmapDrawable(clickedView.getResources(), bmp));
				}
			}
		}

		@Override
		protected void endDrag(View parent, View endToView, AnimatorListenerAdapter listener)
		{
			if(m_alphaAnim)
			{
				if(mDragView != null)
				{
					PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 0f);
					ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mDragView, alpha);
					anim.setInterpolator(new DecelerateInterpolator());
					anim.setDuration(ANIMATION_DURATION);
					anim.addListener(listener);
					anim.start();
				}
			}
			else
			{
				super.endDrag(parent, endToView, listener);
			}
		}

		@Override
		public void onStartDragAnimation(View dragView)
		{
			dragView.setAlpha(1f);
		}
	}
}
