package cn.poco.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class BottomBar extends FrameLayout
{
	public static final int ANIM_TIME = 1200;

	protected BkItem m_bk;
	protected BkItem m_newBk;

	protected BkItem m_cacheBk;

	public BottomBar(Context context)
	{
		super(context);

		Init();
	}

	public BottomBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		Init();
	}

	public BottomBar(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		Init();
	}

	protected void Init()
	{
		this.setWillNotDraw(false);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		long time = System.currentTimeMillis();
		if(m_bk != null)
		{
			DrawItem(canvas, m_bk, time);
		}
		if(m_newBk != null)
		{
			DrawItem(canvas, m_newBk, time);
			if(!m_newBk.m_hasAnim || m_newBk.m_startTime + ANIM_TIME <= time)
			{
				if(m_bk != null && m_bk.m_canRecycle && m_bk.m_bk != null)
				{
					m_bk.m_bk.recycle();
					m_bk.m_bk = null;
				}
				m_bk = m_newBk;
				m_newBk = null;
			}
			this.invalidate();
		}
		if(m_newBk == null && m_cacheBk != null)
		{
			m_newBk = m_cacheBk;
			m_cacheBk = null;
			m_newBk.m_startTime = time;
			this.invalidate();
		}
	}

	protected Matrix temp_matrix = new Matrix();
	protected Paint temp_paint = new Paint();

	protected void DrawItem(Canvas canvas, BkItem item, long time)
	{
		temp_paint.reset();
		long d = time - item.m_startTime;
		if(item.m_hasAnim && 0 < d && d < ANIM_TIME)
		{
			temp_paint.setAlpha((int)((float)d / (float)ANIM_TIME * 255 + 0.5f));
		}
		else
		{
			temp_paint.setAlpha(255);
		}

		if(item.m_bk != null)
		{
			float sx = (float)this.getWidth() / (float)item.m_bk.getWidth();
			float sy = (float)this.getHeight() / (float)item.m_bk.getHeight();
			temp_matrix.reset();
			temp_matrix.postScale(sx, sy);
			canvas.drawBitmap(item.m_bk, temp_matrix, temp_paint);
		}
	}

	public void ChangeBk(Bitmap bk, boolean canRecycle, boolean hasAnim)
	{
		if(m_cacheBk != null)
		{
			if(m_cacheBk.m_canRecycle)
			{
				if(m_cacheBk.m_bk != null)
				{
					m_cacheBk.m_bk.recycle();
					m_cacheBk.m_bk = null;
				}
			}
			m_cacheBk = null;
		}

		m_cacheBk = new BkItem();
		m_cacheBk.m_bk = bk;
		m_cacheBk.m_canRecycle = canRecycle;
		m_cacheBk.m_hasAnim = hasAnim;

		this.invalidate();
	}

	public static class BkItem
	{
		public Bitmap m_bk;
		public boolean m_canRecycle;
		public boolean m_hasAnim;

		public long m_startTime;
	}
}
