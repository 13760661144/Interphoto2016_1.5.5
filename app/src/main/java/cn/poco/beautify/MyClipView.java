package cn.poco.beautify;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;

import cn.poco.display.ClipView;
import cn.poco.display.ClipViewV2;
import cn.poco.tianutils.ShareData;

public class MyClipView extends ClipView
{
	private float def_btn_size; //按钮的大小
	private float def_btn_h2; //四侧按钮的宽度
	private int def_btn_color = 0xffffffff; //按钮颜色
	private float def_over_scale = 1.25f; //按下时按钮的放大倍数

	private Paint temp_paint = new Paint();
	protected float m_degreeH;
	protected float m_degreeV;
	protected Camera m_camera;
	protected float m_depthZ;

	protected Bitmap m_orgBmp;
	protected float m_microDegree;
	protected int m_areaCount = 3;

	public MyClipView(Activity ac, int frW, int frH, Callback cb)
	{
		super(ac, frW, frH, cb);
		m_camera = new Camera();
		m_depthZ = 90f;

		def_btn_size = (ShareData.PxToDpi_xhdpi(48) + 1) / 2 * 2;
		def_btn_h2 = (ShareData.PxToDpi_xhdpi(4) + 1) / 2 * 2;

		def_rect_color = 0xffffffff;
		m_degreeH = 0;
	}

	public void setAreaCount(int count)
	{
		m_areaCount = count;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.save();

		if (m_bmp != null && !m_bmp.isRecycled()) {
			if (m_isAnim && !m_tween.M1IsFinish()) {
				float a = m_tween.M1GetPos();
				temp_matrix.set(m_animMatrix);
				temp_matrix.postScale((m_animScaleX - 1) * a + 1, (m_animScaleY - 1) * a + 1, m_x + m_centerX, m_y + m_centerY);
				temp_matrix.postRotate(m_animDegree * a, m_x + m_centerX, m_y + m_centerY);
				canvas.drawBitmap(m_bmp, temp_matrix, null);
				this.invalidate();
			} else {
				m_isAnim = false;

				//画图
				canvas.setDrawFilter(temp_filter);
				temp_matrix.reset();
				temp_matrix.postScale(m_scaleX, m_scaleY, m_centerX, m_centerY);
				temp_matrix.postRotate(m_degree, m_centerX, m_centerY);
				temp_matrix.postTranslate(m_x, m_y);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				canvas.drawBitmap(m_bmp, temp_matrix, temp_paint);

				//画mask
				float w2 = Math.abs(m_w * m_scaleX) / 2f;
				float h2 = Math.abs(m_h * m_scaleY) / 2f;
				if (m_degree % 180 != 0) {
					w2 += h2;
					h2 = w2 - h2;
					w2 -= h2;
				}
				//此方法不兼容GPU加速
				//temp_path.reset();
				//temp_path.setFillType(Path.FillType.EVEN_ODD);
				//temp_path.addRect(m_x + m_centerX - w2, m_y + m_centerY - h2, m_x + m_centerX + w2, m_y + m_centerY + h2, Path.Direction.CCW);
				//temp_path.addRect(m_pos[0], m_pos[1], m_pos[2], m_pos[3], Path.Direction.CCW);
				//temp_paint.reset();
				//temp_paint.setAntiAlias(true);
				//temp_paint.setFilterBitmap(true);
				//temp_paint.setStyle(Style.FILL);
				//temp_paint.setColor(def_mask_color);
				//canvas.drawPath(temp_path, temp_paint);
				//----------------------------------
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				temp_paint.setStyle(Style.FILL);
				temp_paint.setColor(def_mask_color);
				canvas.drawRect(m_x + m_centerX - w2, m_y + m_centerY - h2, m_pos[0], m_y + m_centerY + h2, temp_paint);
				canvas.drawRect(m_pos[0], m_y + m_centerY - h2, m_pos[2], m_pos[1], temp_paint);
				canvas.drawRect(m_pos[2], m_y + m_centerY - h2, m_x + m_centerX + w2, m_y + m_centerY + h2, temp_paint);
				canvas.drawRect(m_pos[0], m_pos[3], m_pos[2], m_y + m_centerY + h2, temp_paint);

				//画分割线
				if (def_line_width > 0) {
					temp_paint.reset();
					temp_paint.setStrokeCap(Paint.Cap.SQUARE);
					temp_paint.setStrokeJoin(Paint.Join.BEVEL);
					temp_paint.setStyle(Style.STROKE);
					temp_paint.setStrokeWidth(def_line_width);
					temp_paint.setColor(def_line_color);
					temp_paint.setPathEffect(temp_effect);
					temp_paint.setAntiAlias(true);

					float width = m_pos[2] - m_pos[0];
					float height = m_pos[3] - m_pos[1];
					float temp = height / (float)m_areaCount;
					float x1, y1, x2, y2;
					//横向
					for(int i = 1; i < m_areaCount; i ++)
					{
						x1 = m_pos[0];
						y1 = y2 = m_pos[1] + temp * i;
						x2 = m_pos[2];
						if(i % 2 == 0)
						{
							temp_paint.setStrokeWidth(def_rect_width);
							temp_paint.setColor(def_rect_color);
						}
						else
						{
							temp_paint.setStrokeWidth(def_line_width);
							temp_paint.setColor(def_line_color);
						}
						canvas.drawLine(x1, y1, x2, y2, temp_paint);
					}
					//纵向
					temp = width / m_areaCount;
					for(int i = 1; i < m_areaCount; i ++)
					{
						y1 = m_pos[1];
						y2 = m_pos[3];
						x1 = x2 = m_pos[0] + temp * i;
						if(i % 2 == 0)
						{
							temp_paint.setStrokeWidth(def_rect_width);
							temp_paint.setColor(def_rect_color);
						}
						else
						{
							temp_paint.setStrokeWidth(def_line_width);
							temp_paint.setColor(def_line_color);
						}
						canvas.drawLine(x1, y1, x2, y2, temp_paint);
					}
				}

				//画框
				if (def_rect_width > 0) {
					temp_paint.reset();
					temp_paint.setStrokeCap(Paint.Cap.SQUARE);
					temp_paint.setStrokeJoin(Paint.Join.BEVEL);
					temp_paint.setStyle(Style.STROKE);
					temp_paint.setStrokeWidth(def_rect_width);
					temp_paint.setColor(def_rect_color);
					canvas.drawRect(m_pos[0], m_pos[1], m_pos[2], m_pos[3], temp_paint);
				}

				//画按钮
				canvas.save();
				canvas.translate(m_pos[0], m_pos[1]);
				if (m_touchIndex == 0) {
					DrawBtnLT(canvas, true);
				} else {
					DrawBtnLT(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate((m_pos[0] + m_pos[2]) / 2f, m_pos[1]);
				if (m_touchIndex == 1) {
					DrawBtnCT(canvas, true);
				} else {
					DrawBtnCT(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate(m_pos[2], m_pos[1]);
				if (m_touchIndex == 2) {
					DrawBtnRT(canvas, true);
				} else {
					DrawBtnRT(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate(m_pos[2], (m_pos[1] + m_pos[3]) / 2f);
				if (m_touchIndex == 3) {
					DrawBtnRC(canvas, true);
				} else {
					DrawBtnRC(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate(m_pos[2], m_pos[3]);
				if (m_touchIndex == 4) {
					DrawBtnRB(canvas, true);
				} else {
					DrawBtnRB(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate((m_pos[0] + m_pos[2]) / 2f, m_pos[3]);
				if (m_touchIndex == 5) {
					DrawBtnCB(canvas, true);
				} else {
					DrawBtnCB(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate(m_pos[0], m_pos[3]);
				if (m_touchIndex == 6) {
					DrawBtnLB(canvas, true);
				} else {
					DrawBtnLB(canvas, false);
				}
				canvas.restore();
				canvas.save();
				canvas.translate(m_pos[0], (m_pos[1] + m_pos[3]) / 2f);
				if (m_touchIndex == 7) {
					DrawBtnLC(canvas, true);
				} else {
					DrawBtnLC(canvas, false);
				}
				canvas.restore();
			}
		}

		canvas.restore();
	}

	@Override
	public void SetImg(Object info, Bitmap bmp)
	{
		m_info = info;
		if (bmp != null) {
			m_bmp = bmp;
		} else {
			m_bmp = m_cb.MakeShowImg(info, (int) (m_frW - def_btn_size), (int) (m_frH - def_btn_size));
		}
		if (m_bmp == null) {
			this.invalidate();
			return;
		}

		m_w = m_bmp.getWidth();
		m_h = m_bmp.getHeight();
		m_centerX = (float) m_w / 2f;
		m_centerY = (float) m_h / 2f;
		m_x = (m_frW - m_w) / 2f;
		m_y = (m_frH - m_h) / 2f;
		{
			float scale1 = (float) (m_frW - def_btn_size) / (float) m_w;
			float scale2 = (float) (m_frH - def_btn_size) / (float) m_h;
			m_scaleX = (scale1 > scale2) ? scale2 : scale1;
			m_scaleY = m_scaleX;
		}
		m_degree = 0;
		m_touchIndex = -1;

		SetClipWHScale(-1);

		this.invalidate();

		if(m_bmp != null)
		{
			m_orgBmp = m_bmp;
			m_bmp = Bitmap.createBitmap(m_orgBmp.getWidth(), m_orgBmp.getHeight(), Bitmap.Config.ARGB_8888);
			DrawClipDegree(m_bmp, m_orgBmp, m_microDegree, m_degreeH, m_degreeV);
		}
	}

	public void SetMicroDegree(float degree)
	{
		m_microDegree = degree;
		DrawClipDegree(m_bmp, m_orgBmp, m_microDegree, m_degreeH, m_degreeV);
	}

	public void SetDegreeH(float degree)
	{
		m_degreeH = degree;
		DrawClipDegree(m_bmp, m_orgBmp, m_microDegree, m_degreeH, m_degreeV);
	}

	public void SetDegreeV(float degree)
	{
		m_degreeV = degree;
		DrawClipDegree(m_bmp, m_orgBmp, m_microDegree, m_degreeH, m_degreeV);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void DrawClipDegree(Bitmap dst, Bitmap src, float degree, float degreeH, float degreeV)
	{
		if(dst != null && src != null)
		{
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setFilterBitmap(true);

			Canvas c;

			Matrix m = new Matrix();
			Bitmap temp = Bitmap.createBitmap(dst.getWidth(), dst.getHeight(), Bitmap.Config.ARGB_8888);
			c = new Canvas(temp);
			//清理
			c.drawColor(0, PorterDuff.Mode.CLEAR);
			c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			m.reset();
			float scale = ClipViewV2.GetScale(src.getWidth(), src.getHeight(), (int)degree);
			scale = scale * dst.getWidth() / src.getWidth();
			m.postTranslate((dst.getWidth() - src.getWidth()) / 2f, (dst.getHeight() - src.getHeight()) / 2f);
			m.postScale(scale, scale, dst.getWidth() / 2f, dst.getHeight() / 2f);
			m.postRotate(degree, dst.getWidth() / 2f, dst.getHeight() / 2f);
			c.drawBitmap(src, m, p);

			float tranX = 0;
			float tranY = 0;
			float orgLocation = -8.0f;
			float depthW = orgLocation * dst.getWidth() / (float)m_w;
			float depthH = orgLocation * dst.getHeight() / (float)m_h;

			Bitmap temp1 = Bitmap.createBitmap(dst.getWidth(), dst.getHeight(), Bitmap.Config.ARGB_8888);
			c = new Canvas(temp1);
			//清理
			c.drawColor(0, PorterDuff.Mode.CLEAR);
			c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			m.reset();

			float depthX = 0f;
			if(degreeH > 0)
			{
				tranX = -temp1.getWidth();
				tranY = -temp1.getHeight() / 2f;
				depthX = -(float)Math.abs((Math.PI * degreeH / 180));
			}
			else if(degreeH < 0)
			{
				tranX = 0;
				tranY = -temp1.getHeight() / 2f;
				depthX = (float)Math.abs((Math.PI * degreeH / 180));
			}
			m_camera.save();
			m_camera.setLocation(depthX, 0, depthW);
//			m_camera.translate(0.0f, 0.0f, depthW);
			m_camera.rotateY(degreeH);
			m_camera.getMatrix(m);
			m.preTranslate(tranX, tranY);
			m.postTranslate(-tranX, -tranY);
			m_camera.restore();
			c.drawBitmap(temp, m, p);
			temp.recycle();

			c = new Canvas(dst);
			//清理
			c.drawColor(0, PorterDuff.Mode.CLEAR);
			c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			m.reset();

			float depthV = 0;
			if(degreeV > 0)
			{
				tranX = -dst.getWidth() / 2f;
				tranY = 0;
				depthV = -(float)Math.abs((Math.PI * degreeV / 180));
			}
			else if(degreeV < 0)
			{
				tranX = -dst.getWidth() / 2f;
				tranY = -dst.getHeight();
				depthV = (float)Math.abs((Math.PI * degreeV / 180));
			}
			m_camera.save();
			m_camera.setLocation(0, depthV, depthH);
			m_camera.rotateX(degreeV);
			m_camera.getMatrix(m);
			m.preTranslate(tranX, tranY);
			m.postTranslate(-tranX, -tranY);
			m_camera.restore();
			c.drawBitmap(temp1, m, p);
			temp1.recycle();
		}
	}

	@Override
	public Bitmap GetClipBmp(int size)
	{
		Bitmap out = null;
		if(size > 0)
		{
			int d = (int)Math.abs(m_microDegree % 180);
			if(d != 0)
			{
				float scale = ClipViewV2.GetScale(m_w, m_h, (int)m_microDegree);
				size = (int)(size * scale);
				if(size < 1)
				{
					size = 1;
				}
			}
			out = super.GetClipBmp(size);
		}
		return out;
	}

	@Override
	public Bitmap GetClipBmp(Bitmap org)
	{
		Bitmap out = null;

		if(org != null)
		{
			int d = (int)Math.abs(m_microDegree % 180);
			if(d != 0 || m_degreeH != 0 || m_degreeV != 0)
			{
				float scale = ClipViewV2.GetInvertScale(org.getWidth(), org.getHeight(), (int)m_microDegree);
				int w = (int)(org.getWidth() * scale);
				if(w < 1)
				{
					w = 1;
				}
				int h = (int)(org.getHeight() * scale);
				if(h < 1)
				{
					h = 1;
				}
				Bitmap temp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				DrawClipDegree(temp, org, m_microDegree, m_degreeH, m_degreeV);

				out = super.GetClipBmp(temp);
				temp.recycle();
			}
			else
			{
				out = super.GetClipBmp(org);
			}
		}

		return out;
	}

	@Override
	public void DrawBtnLT(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-w2, -w2, r, w2, temp_paint);
		canvas.drawRect(-w2, -w2, w2, r, temp_paint);
	}

	@Override
	public void DrawBtnCT(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-r, -w2, r, w2, temp_paint);
	}

	@Override
	public void DrawBtnRT(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-r, -w2, w2, w2, temp_paint);
		canvas.drawRect(-w2, -w2, w2, r, temp_paint);
	}

	@Override
	public void DrawBtnRC(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-w2, -r, w2, r, temp_paint);
	}

	@Override
	public void DrawBtnRB(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-r, -w2, w2, w2, temp_paint);
		canvas.drawRect(-w2, -r, w2, w2, temp_paint);
	}

	@Override
	public void DrawBtnCB(Canvas canvas, boolean isTouch)
	{
		DrawBtnCT(canvas, isTouch);
	}

	@Override
	public void DrawBtnLB(Canvas canvas, boolean isTouch)
	{
		temp_paint.reset();
		temp_paint.setAntiAlias(true);
		temp_paint.setFilterBitmap(true);
		temp_paint.setStyle(Style.FILL);
		temp_paint.setColor(def_btn_color);

		float r = def_btn_size / 2f;
		if(isTouch)
		{
			r *= def_over_scale;
		}
		r = (int)(r + 0.5f);

		float w2 = def_btn_h2;
		if(isTouch)
		{
			w2 *= def_over_scale;
		}
		w2 = (int)(w2 + 0.5f);

		canvas.drawRect(-w2, -w2, r, w2, temp_paint);
		canvas.drawRect(-w2, -r, w2, w2, temp_paint);
	}

	@Override
	public void DrawBtnLC(Canvas canvas, boolean isTouch)
	{
		DrawBtnRC(canvas, isTouch);
	}
}
