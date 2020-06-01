package cn.poco.beautify;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * 裁剪界面刻度尺
 */
public class ScaleAttached extends View
{
	protected int m_showDotNum;
	protected int m_maxDotNum;
	protected float m_divide;
	protected int m_divideText;
	protected int m_centerText;

	protected float m_progress;
	protected float m_cacheProgress;
	protected Bitmap m_dot1;
	protected Bitmap m_dot2;
	protected Bitmap m_thumb;
	protected RectF m_thumbRect;
	protected float m_textSize;
	protected int m_textColor;
	protected int m_textBottomMargin;

	protected OnAttachedChangeListener m_listener;

	protected float m_downX;
	protected float m_dis;
	protected float m_moveLen;

	protected Paint m_paint;
	protected Paint m_paint1;
	protected Matrix m_matrix;
	public ScaleAttached(Context context)
	{
		super(context);
		init();
	}

	public ScaleAttached(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public ScaleAttached(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		init();
	}

	protected void init()
	{
		m_dot1 = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(5), ShareData.PxToDpi_xhdpi(20), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(m_dot1);
		canvas.drawColor(0xff999999);
		m_dot2 = Bitmap.createBitmap(ShareData.PxToDpi_xhdpi(2), ShareData.PxToDpi_xhdpi(10), Bitmap.Config.ARGB_8888);
		canvas = new Canvas(m_dot2);
		canvas.drawColor(0xff999999);

		m_thumb = BitmapFactory.decodeResource(getResources(), R.drawable.clip_seekbar_thumb);
		m_paint = new Paint();
		m_paint1 = new Paint();
		m_matrix = new Matrix();
	}

	public void SetMax(int max)
	{
		m_maxDotNum = max;
	}

	public synchronized void SetProgress(float progress)
	{
		m_progress = progress;
	}

	public synchronized float GetProgress()
	{
		return m_progress;
	}

	public void SetShowDotNum(int showDotNum)
	{
		m_showDotNum = showDotNum;
	}

	public void setTextSize(int size)
	{
		setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
	}

	public void SetDivide(int divide)
	{
		m_divideText = divide;
	}

	public void setTextSize(int unit, int size)
	{
		Context c = getContext();
		Resources r;

		if (c == null)
			r = Resources.getSystem();
		else
			r = c.getResources();

		m_textSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
	}

	public void setTextColor(int color)
	{
		m_textColor = color;
	}

	public void SetTextBottomMargin(int margin)
	{
		m_textBottomMargin = margin;
	}

	public void SetOnAttachedChangeListener(OnAttachedChangeListener lis)
	{
		m_listener = lis;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		//画文字
		float textY = getPaddingTop() + m_paint1.descent() - m_paint1.ascent();

		m_paint.reset();
		m_paint.setAntiAlias(true);
		float x = m_thumbRect.left;
		int y = (int)(m_thumbRect.top + (m_thumbRect.bottom - m_thumbRect.top) / 2f + 0.5f);
		float alpha1 = 255 * 2 / (m_showDotNum);
		int progress = (int)(m_progress + 0.5);
		//左边
		int min = progress - m_showDotNum / 2;
		if(min < 0)
		{
			min = 0;
		}
		for(int i = progress - 1; i >= min; i --)
		{
			int alpha = (int)((m_showDotNum / 2 - (progress - i)) * alpha1);
			m_paint.setAlpha(alpha);
			if(i % 5 == 0)
			{
				DrawDot(canvas, (int)(x - m_divide * (m_progress - i)  + 0.5f), y);

				if((i - m_maxDotNum / 2) % 10 == 0)
				{
					int text = m_centerText + (i - m_maxDotNum / 2) / 10 * m_divideText;
					m_paint1.setAlpha(alpha);
					canvas.drawText(text + "°", (x - m_divide * (m_progress - i)  + 0.5f), textY, m_paint1);
				}
			}
			else
			{
				DrawDot2(canvas, (int)(x - m_divide * (m_progress - i) + 0.5f), y);
			}
		}

		//右边
		int max = progress + m_showDotNum / 2;
		if(max > m_maxDotNum)
		{
			max = m_maxDotNum;
		}
		for(int i = progress + 1; i <= max; i ++)
		{
			int alpha = (int)((m_showDotNum / 2 - (i - progress)) * alpha1);
			m_paint.setAlpha(alpha);
			if(i % 5 == 0)
			{
				DrawDot(canvas, (int)(x + m_divide * (i - m_progress)  + 0.5f), y);

				if((i - m_maxDotNum / 2) % 10 == 0)
				{
					int text = m_centerText + (i - m_maxDotNum / 2) / 10 * m_divideText;
					m_paint1.setAlpha(alpha);
					canvas.drawText(text + "°", (x + m_divide * (i - m_progress) + 0.5f), textY, m_paint1);
				}
			}
			else
			{
				DrawDot2(canvas, (int)(x + m_divide * (i - m_progress) + 0.5f), y);
			}
		}

		m_paint.setAlpha(255);
		if(progress % 5 == 0)
		{
			DrawDot(canvas, (int)(x + m_divide * (progress - m_progress) + 0.5f), y);
		}
		else
		{
			DrawDot2(canvas, (int)(x + m_divide * (progress - m_progress) + 0.5f), y);
		}
		if((progress - m_maxDotNum / 2) % 10 == 0)
		{
			int text = m_centerText + (progress - m_maxDotNum / 2) / 10 * m_divideText;
			m_paint1.setAlpha(255);
			canvas.drawText(text + "°", (x + m_divide * (progress - m_progress) + 0.5f), textY, m_paint1);
		}

		m_matrix.reset();
		m_matrix.setTranslate(m_thumbRect.left, m_thumbRect.top);
		canvas.drawBitmap(m_thumb, m_matrix, m_paint);
	}

	protected void DrawDot(Canvas canvas, int cx, int cy)
	{
		if(m_dot1 != null && !m_dot1.isRecycled())
		{
			m_matrix.reset();
			m_matrix.postTranslate(cx, cy - (int)(m_dot1.getHeight() / 2f + 0.5f));
			canvas.drawBitmap(m_dot1, m_matrix, m_paint);
		}
	}

	protected void DrawDot2(Canvas canvas, int cx, int cy)
	{
		if(m_dot2 != null && !m_dot2.isRecycled())
		{
			m_matrix.reset();
			m_matrix.postTranslate(cx, cy - (int)(m_dot2.getHeight() / 2f + 0.5f));
			canvas.drawBitmap(m_dot2, m_matrix, m_paint);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		if(m_maxDotNum <= 1)
		{
			return super.dispatchTouchEvent(event);
		}
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			{
				OnDown(event);
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				OnMove(event);
				break;
			}
			case MotionEvent.ACTION_UP:
			{
				OnUp();
				break;
			}
		}
		return true;
	}

	protected void OnDown(MotionEvent event)
	{
		m_downX = event.getX();
		m_dis = 0;
		m_cacheProgress = m_progress;
	}

	protected void OnMove(MotionEvent event)
	{
		float curX = event.getX();
		m_dis = m_downX - curX;
		float num = m_dis / m_divide;
		if(num != 0)
		{
			m_progress = m_cacheProgress + num;
			if(m_progress <= 0)
			{
				m_progress = 0;
			}
			if(m_progress >= m_maxDotNum)
			{
				m_progress = m_maxDotNum;
			}
			if(m_listener != null)
			{
				m_listener.OnProgress(m_progress, m_maxDotNum);
			}
		}

		m_moveLen = m_dis;
		this.invalidate();
	}

	protected void OnUp()
	{
		int progress = (int)(m_progress + 0.5f);
		m_progress = progress;
		postInvalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		m_paint.reset();
		m_paint.setTextSize(m_textSize);
		float height1 = m_paint.descent() - m_paint.ascent();
		int height = (int)(m_thumb.getHeight() + m_textBottomMargin + height1 + 0.5f);
		int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

		super.onMeasure(widthMeasureSpec, heightSpec);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		update();
	}

	protected void update()
	{
		int w = getWidth();
		int h = getHeight();
		if(w == 0 || h == 0)
			return;

		m_paint.reset();
		m_paint.setTextSize(m_textSize);
		float height = m_paint.descent() - m_paint.ascent();
		float left = (w - m_thumb.getWidth()) / 2f;
		float top = m_textBottomMargin + height + getPaddingTop();
		m_thumbRect = new RectF(left, top, left + m_thumb.getWidth(), top + m_thumb.getHeight());

		m_divide = (w - getPaddingLeft() - getPaddingRight()) / (float)m_showDotNum;

		m_centerText = 0;

		m_paint1.reset();
		m_paint1.setTextSize(m_textSize);
		m_paint1.setTextAlign(Paint.Align.CENTER);
		m_paint1.setColor(m_textColor);
		m_paint1.setAntiAlias(true);
		if(m_progress <= 0)
		{
			m_progress = 0;
		}
		else if(m_progress > m_maxDotNum)
		{
			m_progress = m_maxDotNum;
		}

		if(m_listener != null)
		{
			m_listener.OnProgress(m_progress, m_maxDotNum);
		}
	}

	public interface OnAttachedChangeListener
	{
		public void OnProgress(float progress, int max);
	}
}
