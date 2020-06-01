package cn.poco.beautify;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import cn.poco.graffiti.PointCollect;
import cn.poco.image.filter;
import cn.poco.tianutils.ShareData;

/**
 * 曲线
 */
public class CurveView extends View
{
	public static final int MODE_RED = 1;
	public static final int MODE_BLUE = 2;
	public static final int MODE_GREEN = 3;
	public static final int MODE_RGB = 4;
	public static final int MAX_CTRL_COUNT = 7;
	protected Callback m_cb;
	protected int m_viewSize;	//view的宽高
	protected int m_radius;	//控制点的半径
	protected float m_downX;
	protected float m_downY;
	protected int m_curIndex = -1;

	private Paint temp_paint = new Paint();
	private Paint temp_paint1 = new Paint();
	protected PaintFlagsDrawFilter temp_filter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

	protected PointF[] m_baseLinePoints;	//基线坐标
	protected float[] m_divide; //用于划分区域（目前总共划分5块区域）

	public ControlInfo r;	//红
	public ControlInfo g;	//绿
	public ControlInfo b;	//蓝
	public ControlInfo rgb;

	protected int m_mode = MODE_RGB;
	protected float m_offsetX;
	protected float m_offsetY;
	protected boolean m_longClick;
	protected boolean m_isMove;
	protected boolean m_isChecking;
	protected CheckForLongPress m_checkLongPress;
	Handler m_handler = new Handler();

	public CurveView(Context context, int viewSize)
	{
		super(context);
		setLayerType(LAYER_TYPE_SOFTWARE, null);
		ShareData.InitData(context);
		m_viewSize = viewSize;
		m_radius = ShareData.PxToDpi_xhdpi(12);
		m_offsetX = m_radius;
		m_offsetY = m_radius;
		InitCurveInfo();
	}

	protected void InitCurveInfo()
	{
		m_baseLinePoints = new PointF[5];
		m_divide = new float[5];
		float space = m_viewSize / 4f;
		for(int i = 0; i < m_baseLinePoints.length; i ++)
		{
			m_divide[i] = space * i;
			m_baseLinePoints[i] = new PointF(space * i, m_viewSize - space * i);
		}

		r = new ControlInfo();
		r.line_color = Color.RED;
		r.m_orgPoints.add(new PointF(0, m_viewSize));
		r.m_orgPoints.add(new PointF(m_viewSize, 0));
		r.m_ctrlPoints.addAll(r.m_orgPoints);

		g = new ControlInfo();
		g.line_color = Color.GREEN;
		g.m_orgPoints.add(new PointF(0, m_viewSize));
		g.m_orgPoints.add(new PointF(m_viewSize, 0));
		g.m_ctrlPoints.addAll(g.m_orgPoints);

		b = new ControlInfo();
		b.line_color = Color.BLUE;
		b.m_orgPoints.add(new PointF(0, m_viewSize));
		b.m_orgPoints.add(new PointF(m_viewSize, 0));
		b.m_ctrlPoints.addAll(b.m_orgPoints);

		rgb = new ControlInfo();
		rgb.line_color = Color.WHITE;
		rgb.m_orgPoints.add(new PointF(0, m_viewSize));
		rgb.m_orgPoints.add(new PointF(m_viewSize, 0));
		rgb.m_ctrlPoints.addAll(rgb.m_orgPoints);
		ComputePathData(rgb);
		ComputePathData(r);
		ComputePathData(g);
		ComputePathData(b);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		canvas.setDrawFilter(temp_filter);

		canvas.save();
		canvas.translate(m_offsetX + m_radius / 2f, m_offsetY + m_radius / 2f);

		//画网格线
		temp_paint.reset();
		temp_paint.setColor(0x99ffffff);
		temp_paint.setAntiAlias(true);
		temp_paint.setStyle(Paint.Style.STROKE);
		temp_paint.setStrokeWidth(1);
		float x0, y0, x1, y1;
		//画横线
		for(int i = 0; i < m_divide.length; i ++)
		{
			if(i == 0 || i == m_divide.length - 1)
			{
				temp_paint.setStrokeWidth(2);
				temp_paint.setColor(0xffffffff);
			}
			else
			{
				temp_paint.setStrokeWidth(1);
				temp_paint.setColor(0x99ffffff);
			}
			x0 = m_divide[0];
			y0 = m_divide[i];
			x1 = m_divide[m_divide.length - 1];
			y1 = m_divide[i];
			canvas.drawLine(x0, y0, x1, y1, temp_paint);
		}

		//画竖线
		for(int i = 0; i < m_divide.length; i ++)
		{
			if(i == 0 || i == m_divide.length - 1)
			{
				temp_paint.setStrokeWidth(2);
				temp_paint.setColor(0xffffffff);
			}
			else
			{
				temp_paint.setStrokeWidth(1);
				temp_paint.setColor(0x99ffffff);
			}
			x0 = m_divide[i];
			y0 = m_divide[0];
			x1 = m_divide[i];
			y1 = m_divide[m_divide.length - 1];
			canvas.drawLine(x0, y0, x1, y1, temp_paint);
		}

		//画基线
		temp_paint.reset();
		temp_paint.setColor(0x99ffffff);
		temp_paint.setAntiAlias(true);
		temp_paint.setStyle(Paint.Style.STROKE);
		temp_paint.setStrokeWidth(1);
		canvas.drawLine(m_baseLinePoints[0].x, m_baseLinePoints[0].y, m_baseLinePoints[m_baseLinePoints.length - 1].x, m_baseLinePoints[m_baseLinePoints.length - 1].y, temp_paint);

		//画控制线
		if(m_mode == MODE_RED)
		{
			drawCtrlLine(canvas, r, true);
		}
		else if(m_mode == MODE_GREEN)
		{
			drawCtrlLine(canvas, g, true);
		}
		else if(m_mode == MODE_BLUE)
		{
			drawCtrlLine(canvas, b, true);
		}
		else
		{
			if(!r.m_ctrlPoints.equals(r.m_orgPoints))
			{
				drawCtrlLine(canvas, r, false);
			}
			if(!g.m_ctrlPoints.equals(g.m_orgPoints))
			{
				drawCtrlLine(canvas, g, false);
			}
			if(!b.m_ctrlPoints.equals(b.m_orgPoints))
			{
				drawCtrlLine(canvas, b, false);
			}
			drawCtrlLine(canvas, rgb, true);
		}

		//画控制点
		if(m_mode == MODE_RED)
		{
			drawCtrlPoints(canvas, r);
		}
		else if(m_mode == MODE_GREEN)
		{
			drawCtrlPoints(canvas, g);
		}
		else if(m_mode == MODE_BLUE)
		{
			drawCtrlPoints(canvas, b);
		}
		else
		{
			drawCtrlPoints(canvas, rgb);
		}

		canvas.restore();
	}

	/**
	 * 画控制线
	 * @param canvas
	 * @param info
	 */
	protected synchronized void drawCtrlLine(Canvas canvas, ControlInfo info, boolean isCtrlLine)
	{
		ComputePathData(info);

		temp_paint.reset();
		temp_paint.setColor(info.line_color);
		temp_paint.setAntiAlias(true);
		temp_paint.setDither(true);
		temp_paint.setStyle(Paint.Style.STROKE);
		if(isCtrlLine)
		{
			temp_paint.setShadowLayer(3, 2, 2, 0x30000000);
			temp_paint.setStrokeWidth(ShareData.PxToDpi_xhdpi(4));
		}
		else
		{
			temp_paint.setStrokeWidth(ShareData.PxToDpi_xhdpi(3));
		}
		canvas.drawPath(info.m_path, temp_paint);
	}

	protected synchronized void drawCtrlPoints(Canvas canvas, ControlInfo info)
	{
		temp_paint.reset();
		temp_paint.setColor(info.line_color);
		temp_paint.setAntiAlias(true);
		temp_paint.setStyle(Paint.Style.FILL);

		int size = info.m_ctrlPoints.size();
		for(int i = 0; i < size; i ++)
		{
			if(m_curIndex == i)
			{
				temp_paint1.reset();
				temp_paint1.setColor(info.line_color);
				temp_paint1.setAntiAlias(true);
				temp_paint1.setStyle(Paint.Style.STROKE);
				temp_paint1.setStrokeWidth(ShareData.PxToDpi_xhdpi(2));
				canvas.drawCircle(info.m_ctrlPoints.get(i).x, info.m_ctrlPoints.get(i).y, m_radius * 2 / 3, temp_paint);
				canvas.drawCircle(info.m_ctrlPoints.get(i).x, info.m_ctrlPoints.get(i).y, m_radius, temp_paint1);
			}
			else
			{
				canvas.drawCircle(info.m_ctrlPoints.get(i).x, info.m_ctrlPoints.get(i).y, m_radius, temp_paint);
			}
		}
	}

	protected synchronized void ComputePathData(ControlInfo info)
	{
		float[] controlPoints = new float[info.m_ctrlPoints.size() * 2];
		int size = info.m_ctrlPoints.size();
		for(int i = 0; i < size; i ++)
		{
			controlPoints[i * 2] = info.m_ctrlPoints.get(i).x / m_viewSize;
			controlPoints[i * 2 + 1] = info.m_ctrlPoints.get(i).y / m_viewSize;
		}
		int[] points = new int[m_viewSize * 2];
		filter.CurvesCreate(controlPoints, size, points, m_viewSize, m_viewSize);

		float ctrlX;
		float ctrlY;
		PointCollect collect = new PointCollect();
		collect.SetDensity(3);
		collect.CreatePointsBuffer();
		for(int i = 0; i < points.length; i = i + 2)
		{
			ctrlX = points[i];
			ctrlY = points[i + 1];
			collect.AddPoint(ctrlX, ctrlY);
		}
		info.m_pathPoints = collect.GetPoints();

		info.m_path.reset();
		ctrlX = info.m_pathPoints.get(0).x;
		ctrlY = info.m_pathPoints.get(0).y;
		info.m_path.moveTo(ctrlX, ctrlY);
		int len = info.m_pathPoints.size();
		for(int i = 1; i < len; i ++)
		{
			info.m_path.lineTo(info.m_pathPoints.get(i).x, info.m_pathPoints.get(i).y);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
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
				m_isMove = true;
				removeLongPressCallback();
				switch(m_mode)
				{
					case MODE_RED:
					{
						OnUp(r);
						break;
					}
					case MODE_GREEN:
					{
						OnUp(g);
						break;
					}
					case MODE_BLUE:
					{
						OnUp(b);
						break;
					}
					case MODE_RGB:
					{
						OnUp(rgb);
						break;
					}
				}
				break;
			}
		}
		return true;
	}

	protected void OnDown(MotionEvent event)
	{
		m_curIndex = -1;
		m_downX = event.getX();
		m_downY = event.getY();
		switch(m_mode)
		{
			case MODE_RED:
			{
				AddPoint(r);
				break;
			}
			case MODE_GREEN:
			{
				AddPoint(g);
				break;
			}
			case MODE_BLUE:
			{
				AddPoint(b);
				break;
			}
			case MODE_RGB:
			{
				AddPoint(rgb);
				break;
			}
		}
		if(m_curIndex != -1)
		{
			m_isMove = false;
			m_longClick = false;
			m_isChecking = true;
			checkForLongClick();
		}
	}

	private void removeLongPressCallback()
	{
		if (m_checkLongPress != null)
		{
			m_handler.removeCallbacks(m_checkLongPress);
			m_checkLongPress = null;
		}
	}

	private void checkForLongClick()
	{
		if (m_checkLongPress == null)
		{
			m_checkLongPress = new CheckForLongPress();
		}
		m_handler.postDelayed(m_checkLongPress, 500);
	}

	protected synchronized void OnUp(ControlInfo info)
	{
		if(m_curIndex != -1 && m_curIndex < info.m_ctrlPoints.size())
		{
			PointF pointF = info.m_ctrlPoints.get(m_curIndex);
			if(m_cb != null)
			{
				m_cb.OnUp(pointF, info.m_ctrlPoints);
			}
		}
	}

	protected synchronized void AddPoint(ControlInfo info)
	{
		//判断是否在path附近
		double minDis = 0xffffff;
		double curDis;
		float ctrlX;
		float ctrlY;
		int len = info.m_pathPoints.size();
		for(int i = 0; i < len; i ++)
		{
			ctrlX = info.m_pathPoints.get(i).x;
			ctrlY = info.m_pathPoints.get(i).y;
			curDis = Math.sqrt((ctrlX - m_downX) * (ctrlX - m_downX) + (ctrlY - m_downY) * (ctrlY - m_downY));
			if(curDis < minDis)
			{
				minDis = curDis;
			}
		}
		if(minDis > m_radius * 4)
		{
			m_curIndex = -1;
			return;
		}

		if(m_downX < 0)
		{
			m_downX = 0;
		}
		if(m_downX > m_viewSize)
		{
			m_downX = m_viewSize;
		}
		if(m_downY < 0)
		{
			m_downY = 0;
		}
		if(m_downY > m_viewSize)
		{
			m_downY = m_viewSize;
		}

		int size = info.m_ctrlPoints.size();
		PointF pointF;
		minDis = 0xffffff;
		m_curIndex = 0;
		for(int i = 0; i < size; i ++)
		{
			ctrlX = info.m_ctrlPoints.get(i).x;
			ctrlY = info.m_ctrlPoints.get(i).y;
			curDis = Math.sqrt((ctrlX - m_downX) * (ctrlX - m_downX) + (ctrlY - m_downY) * (ctrlY - m_downY));
			if(curDis < minDis)
			{
				minDis = curDis;
				m_curIndex = i;
			}
		}
		if(minDis > m_radius * 4)
		{
			if(size < MAX_CTRL_COUNT)
			{
				pointF = new PointF(m_downX, m_downY);
				int size1 = info.m_ctrlPoints.size();
				boolean add = true;
				for(int i = 0; i < size1; i ++)
				{
					if(Math.abs(info.m_ctrlPoints.get(i).x - m_downX) < m_radius * 2)
					{
						add = false;
					}
				}
				if(add)
				{
					if(m_curIndex == 0 && (info.m_ctrlPoints.get(m_curIndex).x - m_downX) < 0)
					{
						m_curIndex ++;
					}
					else
					{
						ctrlX = info.m_ctrlPoints.get(m_curIndex).x;
						if((m_downX - ctrlX) > 0)
						{
							m_curIndex ++;
						}
					}
					info.m_ctrlPoints.add(m_curIndex, pointF);

					//重新排序
					PointF temp = info.m_ctrlPoints.get(m_curIndex);
					int orderLen = info.m_ctrlPoints.size();
					PointF[] order = new PointF[orderLen];
					info.m_ctrlPoints.toArray(order);
					PointF temp1;
					for(int i = 0; i < orderLen; i ++)
					{
						for(int j = i + 1; j < orderLen; j ++)
						{
							if(order[j].x < order[i].x)
							{
								temp1 = order[j];
								order[j] = order[i];
								order[i] = temp1;
							}
						}
					}

					info.m_ctrlPoints.clear();
					for(int i = 0; i < orderLen; i ++)
					{
						info.m_ctrlPoints.add(order[i]);
						if(order[i].x == temp.x)
						{
							m_curIndex = i;
						}
					}

				}
			}
			else
			{
				m_curIndex = -1;
				return;
			}
		}
		if(m_cb != null)
		{
			m_cb.OnDown(info.m_ctrlPoints.get(m_curIndex), info.m_ctrlPoints);
		}
		this.invalidate();
	}

	protected void OnMove(MotionEvent event)
	{
		if(m_curIndex < 0)
			return;

		float curX = event.getX();
		float curY = event.getY();
		if(m_isChecking && m_isMove == false)
		{
			if (Math.abs(m_downX - curX) > 20 || Math.abs(m_downY - curY) > 20)
			{
				// 移动超过阈值，则表示移动了
				m_isMove = true;
				removeLongPressCallback();
			}
			return;
		}

		switch(m_mode)
		{
			case MODE_RED:
			{
				MovePoints(r, curX, curY);
				break;
			}
			case MODE_GREEN:
			{
				MovePoints(g, curX, curY);
				break;
			}
			case MODE_BLUE:
			{
				MovePoints(b, curX, curY);
				break;
			}
			case MODE_RGB:
			{
				MovePoints(rgb, curX, curY);
				break;
			}
		}
	}

	protected synchronized boolean DeletePoint(ControlInfo info)
	{
		int size = info.m_ctrlPoints.size();
		if(size <= 2)
			return false;
		if(m_curIndex >= 0 && m_curIndex < size)
		{
			info.m_ctrlPoints.remove(m_curIndex);
			m_curIndex = -1;
			if(m_cb != null)
			{
				m_cb.OnUp(null, info.m_ctrlPoints);
			}
			CurveView.this.invalidate();
			return true;
		}
		return false;
	}

	protected synchronized void MovePoints(ControlInfo info, float curX, float curY)
	{
		if(curX < -4 * m_radius || curX > m_viewSize + 4 * m_radius)
		{
			if(DeletePoint(info))
			{
				return;
			}
		}
		//控制移动区域
		if(curX < 0)
		{
			curX = 0;
		}
		if(curX > m_viewSize)
		{
			curX = m_viewSize;
		}
		if(curY < 0)
		{
			curY = 0;
		}
		if(curY > m_viewSize)
		{
			curY = m_viewSize;
		}
		int size = info.m_ctrlPoints.size();
		PointF temp;
		if(m_curIndex == 0)
		{
			temp = info.m_ctrlPoints.get(1);
			if(curX > temp.x - m_radius * 2)
			{
				curX = temp.x - m_radius * 2;
			}
		}
		else if(m_curIndex == size - 1)
		{
			temp = info.m_ctrlPoints.get(size - 2);
			if(curX < temp.x + m_radius * 2)
			{
				curX = temp.x + m_radius * 2;
			}
		}
		else
		{
			temp = info.m_ctrlPoints.get(m_curIndex - 1);
			if(curX < temp.x + m_radius * 2)
			{
				curX = temp.x + m_radius * 2;
			}
			temp = info.m_ctrlPoints.get(m_curIndex + 1);
			if(curX > temp.x - m_radius * 2)
			{
				curX = temp.x - m_radius * 2;
			}
		}
		PointF pointF = new PointF(curX, curY);
		info.m_ctrlPoints.set(m_curIndex, pointF);
		if(m_cb != null)
		{
			m_cb.OnMove(pointF, info.m_ctrlPoints);
		}
		this.invalidate();
	}

	public void SetMode(int mode)
	{
		m_mode = mode;
		m_curIndex = -1;
		switch(m_mode)
		{
			case MODE_RED:
			{
				if(m_cb != null)
				{
					m_cb.OnUp(null, r.m_ctrlPoints);
				}
				break;
			}
			case MODE_GREEN:
			{
				if(m_cb != null)
				{
					m_cb.OnUp(null, g.m_ctrlPoints);
				}
				break;
			}
			case MODE_BLUE:
			{
				if(m_cb != null)
				{
					m_cb.OnUp(null, b.m_ctrlPoints);
				}
				break;
			}
			case MODE_RGB:
			{
				if(m_cb != null)
				{
					m_cb.OnUp(null, rgb.m_ctrlPoints);
				}
				break;
			}
		}
		this.invalidate();
	}

	public synchronized void Reset()
	{
		m_curIndex = -1;
		switch(m_mode)
		{
			case MODE_RED:
			{
				r.m_ctrlPoints.clear();
				r.m_ctrlPoints.addAll(r.m_orgPoints);
				ComputePathData(r);
				if(m_cb != null)
				{
					m_cb.OnMove(null, r.m_ctrlPoints);
				}
				break;
			}
			case MODE_GREEN:
			{
				g.m_ctrlPoints.clear();
				g.m_ctrlPoints.addAll(g.m_orgPoints);
				ComputePathData(g);
				if(m_cb != null)
				{
					m_cb.OnMove(null, g.m_ctrlPoints);
				}
				break;
			}
			case MODE_BLUE:
			{
				b.m_ctrlPoints.clear();
				b.m_ctrlPoints.addAll(b.m_orgPoints);
				ComputePathData(b);
				if(m_cb != null)
				{
					m_cb.OnMove(null, b.m_ctrlPoints);
				}
				break;
			}
			case MODE_RGB:
			{
				rgb.m_ctrlPoints.clear();
				rgb.m_ctrlPoints.addAll(rgb.m_orgPoints);
				ComputePathData(rgb);
				if(m_cb != null)
				{
					m_cb.OnMove(null, rgb.m_ctrlPoints);
				}
				break;
			}
		}
		this.invalidate();
	}

	public void setCallback(Callback cb)
	{
		m_cb = cb;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthSpec = MeasureSpec.makeMeasureSpec(m_viewSize + m_radius * 3, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec((int)(m_viewSize + m_offsetY + m_radius * 2), MeasureSpec.EXACTLY);;
		super.onMeasure(widthSpec, heightSpec);
	}

	private class CheckForLongPress implements Runnable
	{

		@Override
		public void run()
		{
			if(m_isMove == false)
			{
				m_longClick = true;
			}
			if(m_longClick)
			{
				boolean isDelete = false;
				switch(m_mode)
				{
					case MODE_RED:
					{
						isDelete = DeletePoint(r);
						break;
					}
					case MODE_GREEN:
					{
						isDelete = DeletePoint(g);
						break;
					}
					case MODE_BLUE:
					{
						isDelete = DeletePoint(b);
						break;
					}
					case MODE_RGB:
					{
						isDelete = DeletePoint(rgb);
						break;
					}
				}
				if(isDelete)
				{

					return;
				}
			}
			m_isChecking = false;
		}
	}

	public static interface Callback
	{
		public void OnDown(PointF point, ArrayList<PointF> m_ctrlPoints);
		public void OnMove(PointF point, ArrayList<PointF> m_ctrlPoints);
		public void OnUp(PointF point, ArrayList<PointF> m_ctrlPoints);
	}

	public static class ControlInfo
	{
		public int m_id;
		public int line_color;
		public Path m_path = new Path();
		public ArrayList<PointF> m_pathPoints = new ArrayList<>();
		public ArrayList<PointF> m_orgPoints = new ArrayList<>();
		public ArrayList<PointF> m_ctrlPoints = new ArrayList<>();
	}
}
