package cn.poco.draglistview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
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
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * RecyclerView 每个item
 */
public class MyListItem extends FrameLayout
{
	public int m_thumbW;
	public int m_thumbH;
	public int m_headW;
	public int m_headH;
	public int m_roundSize;	//progress 的描边大小
	public int m_thumbTopMargin;
	public int m_textPadding;
	public int m_textColorOut;
	public int m_textColorOver;
	public int m_textSize;
	public int m_authorSize;
	public int m_lockMargin;
	public int m_topMargin;
	public int m_bottomMargin;
	public int m_leftMargin;
	public int m_rigthMargin;
	public int m_tipBottomMargin;

	protected LinearLayout m_titleFr;
	protected TextView m_name;
	protected TextView m_author;
	protected ImageView m_tip;
	protected ImageView m_head;
	protected ImageView m_lock;
	protected ImageView m_thumb;
	protected ProgressView m_progressView;
	protected FrameLayout m_container;
	protected ImageView m_recommentImg;

	protected DragListItemInfo m_data;
	protected boolean m_showTitle;
	private PorterDuffXfermode m_mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

	public MyListItem(Context context)
	{
		super(context);
	}

	public MyListItem(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public MyListItem(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
	}

	public void Init()
	{
		setPadding(m_leftMargin, m_topMargin, m_rigthMargin, m_bottomMargin);
		FrameLayout.LayoutParams fl;
		LinearLayout.LayoutParams ll;
		m_progressView = new ProgressView(getContext());
		fl = new LayoutParams(m_thumbW + m_roundSize * 2, m_thumbH + m_roundSize * 2);
		fl.gravity = Gravity.CENTER_HORIZONTAL;
		fl.topMargin = m_thumbTopMargin - m_roundSize;
		m_progressView.setLayoutParams(fl);
		this.addView(m_progressView);

		m_container = new FrameLayout(getContext());
		fl = new LayoutParams(m_thumbW, m_thumbH);
		fl.gravity = Gravity.CENTER_HORIZONTAL;
		fl.topMargin = m_thumbTopMargin;
		m_container.setLayoutParams(fl);
		this.addView(m_container);

		m_thumb = new ImageView(getContext());
		fl = new LayoutParams(m_thumbW, m_thumbH);
		fl.gravity = Gravity.BOTTOM;
		m_thumb.setLayoutParams(fl);
		m_container.addView(m_thumb);

		m_recommentImg = new ImageView(getContext());
		m_recommentImg.setVisibility(GONE);
		m_recommentImg.setImageResource(R.drawable.photofactory_item_recomment);
		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP;
		fl.leftMargin = ShareData.PxToDpi_xhdpi(10);
		m_recommentImg.setLayoutParams(fl);
		m_container.addView(m_recommentImg);

		m_titleFr = new LinearLayout(getContext());
		m_titleFr.setPadding(0, m_textPadding, 0, m_textPadding);
		m_titleFr.setOrientation(LinearLayout.VERTICAL);
		m_titleFr.setGravity(Gravity.BOTTOM);
		fl = new LayoutParams(m_thumbW, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		m_titleFr.setLayoutParams(fl);
		m_container.addView(m_titleFr);
		{
			m_name = new TextView(getContext());
			m_name.setGravity(Gravity.CENTER);
			m_name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
			m_name.setTextColor(m_textColorOut);
			ll = new LinearLayout.LayoutParams(m_thumbW, LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			m_name.setLayoutParams(ll);
			m_titleFr.addView(m_name);

			m_author = new TextView(getContext());
			m_author.setGravity(Gravity.CENTER);
			m_author.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_authorSize);
			m_author.setTextColor(m_textColorOut);
			ll = new LinearLayout.LayoutParams(m_thumbW, LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			m_author.setLayoutParams(ll);
			m_titleFr.addView(m_author);

			m_tip = new ImageView(getContext());
			m_tip.setImageResource(R.drawable.framework_item_over_icon);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.CENTER_HORIZONTAL;
			ll.bottomMargin = m_tipBottomMargin - m_textPadding;
			ll.topMargin = m_tipBottomMargin * 2;
			m_tip.setLayoutParams(ll);
			m_titleFr.addView(m_tip);
		}

		m_head = new ImageView(getContext());
		fl = new LayoutParams(m_headW, m_headH);
		fl.gravity = Gravity.CENTER_HORIZONTAL;
		m_head.setLayoutParams(fl);
		this.addView(m_head);

		m_lock = new ImageView(getContext());
		m_lock.setVisibility(View.GONE);
		m_lock.setImageResource(R.drawable.photofactory_item_lock);
		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.TOP | Gravity.RIGHT;
		fl.topMargin = m_lockMargin + m_thumbTopMargin;
		fl.rightMargin = m_lockMargin;
		m_lock.setLayoutParams(fl);
		this.addView(m_lock);
	}

	public void SetData(DragListItemInfo data)
	{
		m_data = data;
		if(m_data != null)
		{
			m_data.m_overHeight = m_thumbH;
		}
	}

	public DragListItemInfo GetData()
	{
		return m_data;
	}

	public void SetThumb(Bitmap bmp)
	{
		m_thumb.setImageBitmap(bmp);
	}

	public void SetHead(Bitmap bmp)
	{
		if(bmp == null)
			m_head.setVisibility(GONE);
		else{
			m_head.setVisibility(VISIBLE);
		}
		m_head.setImageBitmap(bmp);
	}

	public void ShowTitle(boolean show)
	{
		m_showTitle = show;
	}

	public void SetOut(Bitmap outBg)
	{
		if(m_data != null)
		{
			if(!m_showTitle)
			{
				m_name.setVisibility(GONE);
				m_author.setVisibility(GONE);
			}
			else
			{
				m_name.setVisibility(VISIBLE);
			}

			m_name.setText(m_data.m_name);
			m_author.setText(m_data.m_author);
			if(m_data.m_author != null && m_data.m_author.length() > 0)
			{
				m_author.setVisibility(VISIBLE);
			}
			else
			{
				m_author.setVisibility(GONE);
			}

			m_head.setVisibility(GONE);
			if(outBg != null)
			{
				m_titleFr.setBackgroundDrawable(new BitmapDrawable(outBg));
			}
			else
			{
				m_titleFr.setBackgroundColor(0x00000000);
			}
			m_tip.setVisibility(View.GONE);
			m_name.setTextColor(m_textColorOut);
			m_author.setTextColor(m_textColorOut);
			ReLayoutTitleFr(false, m_data.m_selectAnim);
			m_data.m_selectAnim = false;
		}
	}

	public void SetProgress(int progress)
	{
		m_progressView.setProgress(progress);
	}

	public void SetOver(Bitmap overBg)
	{
		if(m_data != null)
		{
			if(!m_showTitle)
			{
				m_tip.setVisibility(GONE);
				m_name.setVisibility(GONE);
				m_author.setVisibility(GONE);
			}
			else
			{
				m_tip.setVisibility(View.VISIBLE);
				m_name.setVisibility(VISIBLE);
			}

			m_name.setText(m_data.m_name);
			m_author.setText(m_data.m_author);
			if(m_data.m_author != null && m_data.m_author.length() > 0)
			{
				m_author.setVisibility(VISIBLE);
			}
			else
			{
				m_author.setVisibility(GONE);
			}

			if(overBg != null)
			{
				m_titleFr.setBackgroundDrawable(new BitmapDrawable(overBg));
			}
			else
			{
				m_titleFr.setBackgroundColor(0x00000000);
			}
			m_head.setVisibility(VISIBLE);
			m_name.setTextColor(m_textColorOver);
			m_author.setTextColor(m_textColorOver);
			ReLayoutTitleFr(true, m_data.m_selectAnim);
			m_data.m_selectAnim = false;
		}
	}

	protected void ReLayoutTitleFr(final boolean isOver, boolean hasAnim)
	{
		m_titleFr.clearAnimation();

		FrameLayout.LayoutParams fl;
		m_titleFr.setVisibility(VISIBLE);
		if(!m_showTitle && !hasAnim && !isOver)
		{
			m_titleFr.setVisibility(GONE);
		}
		if(isOver || (!m_showTitle && hasAnim))
		{
			fl = new LayoutParams(m_thumbW, m_thumbH);
			fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
			m_titleFr.setLayoutParams(fl);
		}
		else if(m_showTitle)
		{
			fl = new LayoutParams(m_thumbW, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			m_titleFr.setLayoutParams(fl);
		}

		if(hasAnim)
		{
			AnimationSet as;
			TranslateAnimation ta;
			float start;
			float end;
			int duration = 100;
			if(isOver)
			{
				if(m_showTitle)
				{
					start = -m_data.m_outHeight + m_data.m_overHeight;
					end = 0;
				}
				else
				{
					start = m_thumbH;
					end = 0;
				}
			}
			else
			{
				if(m_showTitle)
				{
					start = -(m_tipBottomMargin * 3 - m_textPadding + ShareData.PxToDpi_xhdpi(20));
					end = 0;
				}
				else
				{
					start = 0;
					end = m_thumbH;
				}
			}

			as = new AnimationSet(true);
			ta = new TranslateAnimation(0, 0, start, end);
			ta.setDuration(duration);

			as.addAnimation(ta);
			m_titleFr.startAnimation(as);
			as.setAnimationListener(new Animation.AnimationListener()
			{
				@Override
				public void onAnimationStart(Animation animation)
				{

				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					m_titleFr.clearAnimation();
					if(!m_showTitle && !isOver)
					{
						m_titleFr.setVisibility(GONE);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{

				}
			});
		}
	}

	public void ShowLock(boolean show)
	{
		if(show)
		{
			m_lock.setVisibility(View.VISIBLE);
		}
		else
		{
			m_lock.setVisibility(View.GONE);
		}
	}

	public void ShowRecomment(boolean show)
	{
		if(show)
		{
			m_recommentImg.setVisibility(View.VISIBLE);
		}
		else
		{
			m_recommentImg.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		super.onLayout(changed, left, top, right, bottom);
		int height = m_titleFr.getHeight();
		if(m_data != null && height != 0 && height < m_thumbH)
		{
			m_data.m_outHeight = height;
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility)
	{
		if(m_titleFr != null)
		{
			int height = m_titleFr.getHeight();
			if(height != m_thumbH)
			{
//				m_outHeight = height;
			}
		}
		super.onWindowVisibilityChanged(visibility);
	}

	public class ProgressView extends View
	{
		private Paint temp_paint;
		private Path temp_path;
		private int m_progress;
		private int roundSize;	//圆角大小

		public ProgressView(Context context)
		{
			super(context);
			temp_paint = new Paint();
			temp_path = new Path();
			roundSize = ShareData.PxToDpi_xhdpi(10);
			setLayerType(LAYER_TYPE_SOFTWARE, null);
		}

		public void setProgress(int progress)
		{
			m_progress = progress;
			ProgressView.this.invalidate();
		}

		@Override
		protected void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);

			if(m_progress > 0)
			{
				roundSize = 0;
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setColor(0xff000000);
				temp_paint.setStrokeWidth(m_roundSize * 3);
				temp_paint.setDither(true);
				temp_paint.setStrokeCap(Paint.Cap.ROUND);
				temp_path.reset();
				int width = getWidth();
				int height = getHeight();
				int perimeter = 2 * width + 2 * height;
				int pro = perimeter * m_progress / 100;
				if(pro <= width)
				{
					temp_path.moveTo(0, 0);
					temp_path.quadTo(0, 0, 0, 0);
					temp_path.lineTo(0 + pro, 0);
				}
				else if(pro > width && pro <= width + height)
				{
					pro = pro - width;
					temp_path.moveTo(0, 0);
					temp_path.quadTo(0, 0, 0, 0);
					temp_path.lineTo(0 + width, 0);
					temp_path.quadTo(0 + width, 0, 0 + width, 0 + roundSize);
					temp_path.lineTo(0 + width, 0 + pro);
				}
				else if(pro > width + height && pro <= width * 2 + height)
				{
					pro = pro - width - height;
					temp_path.moveTo(0, 0);
					temp_path.quadTo(0, 0, 0, 0);
					temp_path.lineTo(0 + width, 0);
					temp_path.quadTo(0 + width, 0, 0 + width, 0);
					temp_path.lineTo(0 + width, 0 + height);
					temp_path.quadTo(0 + width, 0 + height, 0 + width, 0 + height);
					temp_path.lineTo(0 + width - pro, 0 + height);
				}
				else
				{
					pro = pro - 2 * width - height;
					temp_path.moveTo(0, 0);
					temp_path.quadTo(0, 0, 0 , 0);
					temp_path.lineTo(0 + width, 0);
					temp_path.quadTo(0 + width, 0, 0 + width, 0);
					temp_path.lineTo(0 + width, 0 + height);
					temp_path.quadTo(0 + width, 0 + height, 0 + width, 0 + height);
					temp_path.lineTo(0, 0 + height);
					temp_path.quadTo(0, 0 + height, 0, 0 + height);
					temp_path.lineTo(0, 0 + height - pro);
				}
				canvas.drawPath(temp_path, temp_paint);

				roundSize = ShareData.PxToDpi_xhdpi(10);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				temp_paint.setXfermode(m_mode);
				temp_paint.setColor(0xffffc433);
				canvas.drawRoundRect(new RectF(0, 0, width, height), roundSize, roundSize, temp_paint);
			}
		}
	}
}
