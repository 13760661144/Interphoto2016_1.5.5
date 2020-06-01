package cn.poco.Text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import cn.poco.tianutils.ShareData;
public class ColorChangeLayout extends FrameLayout {

	private FrameLayout mMainContainer;
	private LinearLayout mContantView;
	private ImageView mBGImage;
	private int obbLineSize = 6;
	private int evenLineSize = 5;
	private MyItemView mSelectedItem;
	private ArrayList<ColorItemInfo> mRess;
	private ItemOnClickListener mItemOnClickListener;
	private int letMargins;
	public ColorChangeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public ColorChangeLayout(Context context,AttributeSet attrs){
		super(context, attrs);
	}
	public ColorChangeLayout(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}
	public void setDatas(ArrayList<ColorItemInfo> ress){
		if(ress != null && ress.size()>0){
			int len = ress.size();
			int size = len;
			int lineCount = 0;
			while(size > 0)
			{
				if(lineCount == 0)
				{
					size -= obbLineSize;
				}
				else if(lineCount % 2 == 1)
				{
					size -= obbLineSize;
				}
				else
				{
					size -= evenLineSize;
				}
				lineCount++;
			}
			//System.out.println("ress.length: " + ress.size());

			LayoutParams fl_lp;
			mMainContainer = new FrameLayout(getContext());
			mMainContainer.setBackgroundColor(0xff222222);
			mMainContainer.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					if(mItemOnClickListener != null)
					{
						mItemOnClickListener.onDownClick();
					}
				}
			});
			mMainContainer.setOnTouchListener(new OnTouchListener()
			{

				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					// TODO Auto-generated method stub
					return true;
				}
			});
			fl_lp = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(lineCount * 80 +106));
			fl_lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			mMainContainer.setLayoutParams(fl_lp);
			this.addView(mMainContainer);

			mBGImage = new ImageView(getContext());
			fl_lp = new LayoutParams(ShareData.m_screenWidth, ShareData.m_screenHeight);
			fl_lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			mBGImage.setLayoutParams(fl_lp);
			mMainContainer.addView(mBGImage);

			letMargins = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(80 * 6)) / 7;

			mRess = ress;
			mContantView = new LinearLayout(getContext());
			mContantView.setOrientation(LinearLayout.VERTICAL);
			mContantView.setGravity(Gravity.CENTER_HORIZONTAL);
			fl_lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl_lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			fl_lp.setMargins(0, ShareData.PxToDpi_xhdpi(48), 0, ShareData.PxToDpi_xhdpi(48));
			mContantView.setLayoutParams(fl_lp);
			mMainContainer.addView(mContantView);

			LinearLayout rowContainer = new LinearLayout(getContext());
			rowContainer.setOrientation(LinearLayout.HORIZONTAL);
			rowContainer.setGravity(Gravity.CENTER_VERTICAL);
			LinearLayout.LayoutParams ll_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			rowContainer.setLayoutParams(ll_lp);
			mContantView.addView(rowContainer);
			int lineItemCount = mContantView.getChildCount() % 2 == 0 ? evenLineSize : obbLineSize;

			MyItemView itemView = new MyItemView(getContext());
			itemView.setData(ress.get(0));
			ll_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			itemView.setLayoutParams(ll_lp);
			rowContainer.addView(itemView);

			for(int i = 1; i < len;i++){

				if(rowContainer.getChildCount() >= lineItemCount){

					rowContainer = new LinearLayout(getContext());
					rowContainer.setOrientation(LinearLayout.HORIZONTAL);
					rowContainer.setGravity(Gravity.CENTER_VERTICAL);
					ll_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					ll_lp.topMargin = ShareData.PxToDpi_xhdpi(10);
					rowContainer.setLayoutParams(ll_lp);
					mContantView.addView(rowContainer);
					lineItemCount = mContantView.getChildCount() % 2 == 0 ? evenLineSize : obbLineSize;

					//每行第一个Item
					itemView = new MyItemView(getContext());
					itemView.setData(ress.get(i));
					ll_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					itemView.setLayoutParams(ll_lp);
					rowContainer.addView(itemView);

				}else{
					itemView = new MyItemView(getContext());
					itemView.setData(ress.get(i));
					ll_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					ll_lp.setMargins(letMargins, 0, 0, 0);
					itemView.setLayoutParams(ll_lp);
					rowContainer.addView(itemView);
				}
			}
			mSelectedItem = (MyItemView) findViewById(ress.get(0).m_id);
			mSelectedItem.setSelected(true);
		}
	}
	public void mySetBgBitmap(Bitmap bmp){
		if(mBGImage != null){
			mBGImage.setImageBitmap(bmp);
		}
	}
	public void setSelecteItemByIndex(int index){
		if(mSelectedItem != null)
		{
			mSelectedItem.setSelected(false);
		}
		mSelectedItem = (MyItemView)findViewById(mRess.get(index).m_id);
		mSelectedItem.setSelected(true);
	}

	public void setSelectedItemByColor(String color)
	{
		if(color == null || color.equals(""))
			setSelecteItemByIndex(0);
		else if(mRess != null)
		{
			int size = mRess.size();
			for(int i = 0; i < size; i ++)
			{
				if(mRess.get(i).m_showColor.contains(color))
				{
					setSelecteItemByIndex(i);
					break;
				}
			}
		}
	}

	public void setOnItemClickListener(ItemOnClickListener itemOnClickListener)
	{
		mItemOnClickListener = itemOnClickListener;
	}

	public void clearAll()
	{
		mItemOnClickListener = null;
	}


	public class MyItemView extends ImageView implements OnClickListener{

		private int mNormalRes;
		private int mTransitionRes;
		private int mSelectedRes;
		private ColorItemInfo mObj;
		private int color = 0xffffffff;
		private int m_clickCount = 0;
		public MyItemView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		public MyItemView(Context context, AttributeSet attrs){
			super(context,attrs);
		}
		public MyItemView(Context context, AttributeSet attrs, int defStyle){
			super(context, attrs, defStyle);
		}

		public void setData(ColorItemInfo data){
			this.setId(data.m_id);
			mNormalRes = data.m_normalRes;
			mTransitionRes = data.m_transitionRes;
			mSelectedRes = data.m_selectedRes;
			mObj = data;
			color = Painter.GetColor(data.m_showColor, 0xff);
			setOnClickListener(this);

			Bitmap tempBmp = BitmapFactory.decodeResource(getResources(), mNormalRes);
			Bitmap bmp = Bitmap.createBitmap(tempBmp.getWidth(),tempBmp.getHeight(),Config.ARGB_8888);
			Canvas canvas = new Canvas(bmp);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			canvas.drawColor(color | 0xFF000000);
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			canvas.drawBitmap(tempBmp, new Matrix(), paint);
			setImageBitmap(bmp);
		}

		public void setSelected(boolean isSelected){
			if(isSelected && (m_clickCount == 0 || m_clickCount == 2))
			{
				setImageResource(mTransitionRes);
				m_clickCount ++;
				if(m_clickCount > 2)
				{
					m_clickCount = 1;
				}
			}
			else
			{
				m_clickCount ++;
				if(m_clickCount > 2)
				{
					m_clickCount = 1;
				}
				if(!isSelected){
					m_clickCount = 0;
				}
				Bitmap tempBmp = BitmapFactory.decodeResource(getResources(), isSelected? mSelectedRes : mNormalRes);
				Bitmap bmp = Bitmap.createBitmap(tempBmp.getWidth(), tempBmp.getHeight(), Config.ARGB_8888);
				Canvas canvas = new Canvas(bmp);
				canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
				canvas.drawColor(color | 0xFF000000);
				Paint paint = new Paint();
				paint.setFilterBitmap(true);
				paint.setAntiAlias(true);
				paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
				canvas.drawBitmap(tempBmp, new Matrix(), paint);
				setImageBitmap(bmp);
			}
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mSelectedItem != this){
				if(m_clickCount == 0){
					if(mItemOnClickListener != null){
						mItemOnClickListener.onColorItemClick(mObj);
					}
				}
				else if(m_clickCount == 1){
					if(mItemOnClickListener != null){
						mItemOnClickListener.onColorItemDoubleClick();
					}
				}
				else{
					if(mItemOnClickListener != null){
						mItemOnClickListener.onColorItemTripleClick();
					}
				}
				setSelected(true);
				mSelectedItem.setSelected(false);
				mSelectedItem = this;

			}
			else if(m_clickCount == 1)
			{
				setSelected(true);
				if(mItemOnClickListener != null){
					mItemOnClickListener.onColorItemDoubleClick();
				}
			}
			else if(m_clickCount == 2)
			{
				setSelected(true);
				if(mItemOnClickListener != null){
					mItemOnClickListener.onColorItemTripleClick();
				}
			}
		}

	}

	public interface ItemOnClickListener{
		public void onDownClick();
		public void onColorItemClick(Object result);
		public void onColorItemDoubleClick();
		public void onColorItemTripleClick();
	}
}
