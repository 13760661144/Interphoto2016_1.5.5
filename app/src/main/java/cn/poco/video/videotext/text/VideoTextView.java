package cn.poco.video.videotext.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import cn.poco.Text.MyTextInfo;
import cn.poco.display.CoreViewV3;
import cn.poco.graphics.ShapeEx;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * 视频可编辑水印显示的view
 */

public class VideoTextView extends CoreViewV3
{
	private int m_outWidth;
	private int m_outHeight;
	private boolean m_isOutput = false;

	private boolean m_showLastFrame = false;
	private int m_videoTime = -1;	//表示视频水印显示在最后
	private PorterDuffXfermode m_mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
	private float m_dx;
	private float m_dy;
	private float m_scale = 1;
	private Matrix m_viewSizeMatrix;
	private int m_curTime;	//对应视频时间

	public int def_edit_res = 0;	//编辑按钮
	protected ShapeEx m_editBtn;
	protected boolean m_editBtnVisible = false;

	protected boolean m_hasChoose = false;

	public VideoTextView(Context context, int frW, int frH)
	{
		super(context, frW, frH);
	}

	@Override
	public void InitData(ControlCallback cb)
	{
		super.InitData(cb);
		m_viewSizeMatrix = new Matrix();

		GetShowMatrix(m_viewport.m_matrix, m_viewport);

		GetShowMatrix(m_origin.m_matrix, m_origin);

		m_editBtn = InitBtn(def_edit_res);
	}

	public void setControlCallBack(ControlCallback cb)
	{
		m_cb = cb;
	}


	protected ShapeEx InitBtn(int res)
	{
		ShapeEx shape = null;
		if(res != 0)
		{
			shape = new ShapeEx();
			Bitmap bmp = BitmapFactory.decodeResource(getResources(), res);
			shape.m_bmp = bmp;
			shape.m_w = bmp.getWidth();
			shape.m_h = bmp.getHeight();
			shape.m_centerX = (float) shape.m_w / 2f;
			shape.m_centerY = (float) shape.m_h / 2f;
		}
		return shape;
	}

	private void UpdateViewSize(float scale, float dx, float dy)
	{
		setScaleX(scale);
		setScaleY(scale);
	}

	public void setViewScale(float scale)
	{
		m_scale = scale;
		UpdateViewSize(m_scale, m_dx, m_dy);
	}

	public void setViewTranslate(float dx, float dy)
	{
		m_dx = dx;
		m_dy = dy;
		UpdateViewSize(m_scale, m_dx, m_dy);
	}

	/**
	 * 设置水印在视频上的起始时间，必须大于0
	 * @param startTime
	 */
	public void setStartTime(int startTime)
	{
		if (m_pendantArr != null && m_pendantArr.size() > 0)
		{
			for(ShapeEx item : m_pendantArr)
			{
				if(item instanceof VideoText)
				{
					((VideoText)item).setStartTime(startTime);
				}
			}
		}
	}

	/**
	 * 设置水印的停留时间，必须大于0
	 * @param stayTime
	 */
	public void setStayTime(int stayTime)
	{
		if (m_pendantArr != null && m_pendantArr.size() > 0)
		{
			for(ShapeEx item : m_pendantArr)
			{
				if(item instanceof VideoText)
				{
					((VideoText)item).setStayTime(stayTime);
				}
			}
		}
	}

	/**
	 * 当前视频播放的时间
	 * @param time
	 */
	public synchronized void setCurTime(int time)
	{
		m_curTime = time;

		VideoText item;
		if (m_pendantArr != null && m_pendantArr.size() > 0)
		{
			int size = m_pendantArr.size();
			for(int i = 0; i < size; i ++)
			{
				item = (VideoText)m_pendantArr.get(i);
				item.setCurTime(time);
				item.m_bmp = item.GetOutBmp(item.m_textInfo);
				onItemAnim(item);
			}
		}

		if(!m_isOutput)
		{
			UpdateUI();
		}
	}

	public long GetCurTextTotalTime()
	{
		long time = 0;
		long temp_time = 0;
		if (m_pendantArr != null)
		{
			for(ShapeEx shapeEx : m_pendantArr)
			{
				temp_time = ((VideoText)shapeEx).GetTotalTime();
				time = temp_time > time ? temp_time : time;

				temp_time = GetAnimTime((VideoText)shapeEx);
				time = temp_time > time ? temp_time : time;
			}
		}
		return time;
	}

	/**
	 * 拿到当前水印后台录入的时间
	 * @return
	 */
	public long GetTextTotalTimeNoStayTime()
	{
		long time = 0;
		long temp_time = 0;
		if (m_pendantArr != null)
		{
			for(ShapeEx shapeEx : m_pendantArr)
			{
				temp_time = ((VideoText)shapeEx).GetTotalTimeNoStayTime();
				time = temp_time > time ? temp_time : time;

				temp_time = GetAnimTimeNoStayTime((VideoText)shapeEx);
				time = temp_time > time ? temp_time : time;
			}
		}
		return time;
	}

	private void onItemAnim(VideoText item)
	{
		if(!initAnimData(item))
		{
			return;
		}
		if(m_videoTime > 0)
		{
			item.m_animStartTime = m_videoTime - GetAnimTime(item);
		}
		if(item.m_animStartTime <= 0)
		{
			item.m_animStartTime = 0;
		}

		int runTime = m_curTime - item.m_animStartTime;		//当前动画时间
		int d = item.m_textInfo.animation_be;	//延迟开始时间

		if(runTime < d)
		{
			initAnimData(item);
		}
		else
		{
			int animTime = runTime - d;
			if(animTime > CharInfo.ANIM_TIME)
			{
				animTime = animTime - item.m_animStayTime;
				if(animTime <= CharInfo.ANIM_TIME)
				{
					animTime = CharInfo.ANIM_TIME;
				}
			}
			if(item.m_textInfo.animation_id == VideoText.ANIM1 || item.m_textInfo.animation_id == VideoText.ANIM2)
			{
				float alphaRate = animTime / (float)item.m_textInfo.animation_time;
//				float rate = VideoTextView.m_interPolator.getInterpolation(alphaRate);
				item.m_animW = (int)(item.m_w * alphaRate);
			}
			int index = animTime / 40;	//当前位置，按照1S 25帧计算；
			int size = item.m_textInfo.m_animInfo.size();
			float width = m_origin.m_w * m_origin.m_scaleX;
			float height = m_origin.m_h * m_origin.m_scaleY;
			if(m_isOutput)
			{
				width = m_outWidth;
				height = m_outHeight;
			}
			ElementsAnimInfo mirror = null;
			if(CharInfo.ANIM_INDEX < size)
			{
				mirror = item.m_textInfo.m_animInfo.get(CharInfo.ANIM_INDEX);
			}
			if(mirror != null)
			{
				if(index <= CharInfo.ANIM_INDEX)
				{
					ElementsAnimInfo info = item.m_textInfo.m_animInfo.get(index);
					item.m_showAnimDx = (info.m_x - mirror.m_x) * width;
					item.m_showAnimDy = (info.m_y - mirror.m_y) * height;
					item.m_animScaleX = info.m_scaleX;
					item.m_animScaleY = info.m_scaleY;
					item.m_animRotate = info.m_rotate;
					item.m_animAlpha = info.m_alpha;
				}
				else if(index > CharInfo.ANIM_INDEX)
				{
					if(m_showLastFrame)
					{
						item.m_animAlpha = 255;
					}
					else
					{
						if(index < size)
						{
							ElementsAnimInfo info = item.m_textInfo.m_animInfo.get(index);
							item.m_showAnimDx = (info.m_x - mirror.m_x) * width;
							item.m_showAnimDy = (info.m_y - mirror.m_y) * height;
							item.m_animScaleX = info.m_scaleX;
							item.m_animScaleY = info.m_scaleY;
							item.m_animRotate = info.m_rotate;
							item.m_animAlpha = info.m_alpha;
						}
					}
				}
			}
			else
			{
				item.m_animAlpha = 255;
			}
		}
	}

	/**
	 * 外框矩形
	 * @param item
	 * @param hasGap
	 * @return
	 */
	public float[] GetShapeRealPoints(Canvas canvas, ShapeEx item, boolean hasGap)
	{
		float[] out = new float[8];
		if(item != null)
		{
			canvas.save();
			GetShowMatrix(temp_matrix, item);
			canvas.concat(temp_matrix);
			canvas.getMatrix(temp_matrix);
			canvas.restore();

			temp_src[0] = 0;
			temp_src[1] = 0;
			temp_src[2] = item.m_w;
			temp_src[3] = 0;
			temp_src[4] = item.m_w;
			temp_src[5] = item.m_h;
			temp_src[6] = 0;
			temp_src[7] = item.m_h;
			temp_matrix.mapPoints(out, temp_src);

			if(hasGap){
				out = GetFixRectPoints(out);
			}
		}
		return out;
	}

	protected float[] GetFixRectPoints(float[] out)
	{
		float DEF_GAP = 0;
		int minW = 1024;
		int minH = 1024;
		if (m_editBtn != null)
		{
			DEF_GAP = m_editBtn.m_centerX;
			minW = m_editBtn.m_w;
			minH = m_editBtn.m_h;
		}
		float d = ImageUtils.Spacing(out[0] - out[4], out[1] - out[5]) / 2f;
		if(d > 0 && DEF_GAP > 0)
		{
			float s = (d + DEF_GAP) / d;

			float w = ImageUtils.Spacing(out[0] - out[2], out[1] - out[3]);
			float h = ImageUtils.Spacing(out[0] - out[6], out[1] - out[7]);
			float s1 = minW / w;
			float s2 = minH / h;
			s1 = Math.max(s1, s);
			s2 = Math.max(s2, s);
			if(s1 == s2)
			{
				temp_matrix.postScale(s1, s1, (out[0] + out[4]) / 2f, (out[1] + out[5]) / 2f);
				temp_matrix.mapPoints(out, temp_src);
				return out;
			}

			Matrix matrix = new Matrix();
			matrix.preConcat(temp_matrix);
			float[] tempP = new float[8];
			matrix.postScale(s1, s1, (out[0] + out[4]) / 2f, (out[1] + out[5]) / 2f);
			matrix.mapPoints(tempP, temp_src);

			matrix.reset();
			matrix.postConcat(temp_matrix);
			matrix.postScale(s2, s2, (out[0] + out[4]) / 2f, (out[1] + out[5]) / 2f);
			float[] tempP1 = new float[8];
			matrix.mapPoints(tempP1, temp_src);

			float[] line1 = new float[]{tempP[0], tempP[1], tempP[6], tempP[7]};
			float[] line2 = new float[]{tempP[2], tempP[3], tempP[4], tempP[5]};
			float[] line3 = new float[]{tempP1[0], tempP1[1], tempP1[2], tempP1[3]};
			float[] line4 = new float[]{tempP1[4], tempP1[5], tempP1[6], tempP1[7]};
			float[] outValue = Utils.CrossPoints(line1, line3);
			if(outValue != null)
			{
				out[0] = outValue[0];
				out[1] = outValue[1];
			}
			outValue = Utils.CrossPoints(line1, line4);
			if(outValue != null)
			{
				out[6] = outValue[0];
				out[7] = outValue[1];
			}
			outValue = Utils.CrossPoints(line2, line4);
			if(outValue != null)
			{
				out[4] = outValue[0];
				out[5] = outValue[1];
			}
			outValue = Utils.CrossPoints(line2, line3);
			if(outValue != null)
			{
				out[2] = outValue[0];
				out[3] = outValue[1];
			}
		}
		return out;
	}

	@Override
	protected void DrawToCanvas(Canvas canvas, int mode)
	{
		canvas.save();

//		canvas.concat(m_viewSizeMatrix);

		canvas.setDrawFilter(temp_filter);

		//控制渲染矩形
		ClipStage(canvas);

		//画背景
//		m_bkColor = 0x99aabbcc;
		DrawBK(canvas, m_bk, m_bkColor);

		//画图片
		DrawItem(canvas, m_img);

		//画边框
		DrawItem(canvas, m_frame);

		//画装饰
		int len = m_pendantArr.size();
		for(int i = 0; i < len; i++)
		{
			final ShapeEx item = m_pendantArr.get(i);
			if(item instanceof VideoText)
			{
				DrawItem2(canvas, (VideoText)item);
			}
			else
			{
				DrawItem(canvas, item);
			}
		}

		//画选中框和按钮
		if(m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
			//画选中框
			DrawRect(canvas, temp);

			//显示单手旋转放大按钮
			if(!m_isTouch)
			{
				DrawButtons(canvas, temp);
			}

			if (m_isTouch)
			{
				drawTipLine(canvas, temp);
			}
		}

		canvas.restore();
	}

	private boolean needShowEdit = false;
	@Override
	protected void OddDown(MotionEvent event)
	{
		needShowEdit = false;
		m_isTouch = true;

		switch(m_operateMode)
		{
			case MODE_ALL:
				m_target = m_origin;
				Init_M_Data(m_target, m_downX, m_downY);
				break;

			case MODE_FRAME:
				m_target = m_img;
				Init_M_Data(m_target, m_downX, m_downY);
				break;

			case MODE_PENDANT:
			{
				if(m_pendantCurSel >= 0)
				{
					//判断是否选中保存按钮
					if (m_editBtn != null && IsClickBtn(m_editBtn, m_downX, m_downY) && m_editBtnVisible)
					{
						m_target = m_editBtn;
						if(m_cb instanceof VideoTextCallback)
						{
							((VideoTextCallback)m_cb).OnEditBtn();
						}
						return;
					}
				}
				int index;
				if(m_pendantCurSel == -1){
					index = 0;
				}
				else {
					index = GetSelectIndex(m_pendantArr, m_downX, m_downY);
				}
				if(index >= 0 && index < m_pendantArr.size())
				{
					m_target = m_pendantArr.get(index);
					m_pendantArr.remove(index);
					m_pendantArr.add(m_target);
					m_hasChoose = true;
					if(index != m_pendantCurSel){
						m_target = null;
						m_pendantCurSel = m_pendantArr.size() - 1;
					}
					else{
						m_pendantCurSel = m_pendantArr.size() - 1;
						m_isOddCtrl = false;
						needShowEdit = true;
						Init_M_Data(m_target, m_downX, m_downY);
						//通知主界面选中信息
						m_cb.SelectPendant(m_pendantCurSel);
						//更新界面
						this.invalidate();
					}
				}
				else
				{
					m_hasChoose = false;
					if(m_pendantCurSel >= 0)
					{
						m_pendantCurSel = -1;
						//通知主界面选中信息
						m_cb.SelectPendant(m_pendantCurSel);

						//更新界面
						this.invalidate();
					}
					m_isOddCtrl = false;
					m_target = null;
				}
				break;
			}

			case MODE_IMAGE:
			default:
				m_target = null;
				break;
		}
	}

	@Override
	protected void OddMove(MotionEvent event)
	{
		float dx = event.getX() - m_downX;
		float dy = event.getY() - m_downY;
		if(Math.abs(dx) >= 10 || Math.abs(dy) >= 10)
		{
			needShowEdit = false;
		}
		super.OddMove(event);
	}

	/**
	 * 画提示线
	 */
	protected void drawTipLine(Canvas canvas, ShapeEx shape)
	{
		temp_paint.reset();
		temp_paint.setStrokeCap(Paint.Cap.SQUARE);
		temp_paint.setStrokeJoin(Paint.Join.BEVEL);
		temp_paint.setStyle(Paint.Style.STROKE);
		temp_paint.setStrokeWidth(2);
		temp_paint.setColor(0xffffc433);
		temp_paint.setAntiAlias(true);

		float[] pos = GetShapeRealPoints(canvas, m_viewport, false);
		float[] pos1;
		float img_centerX = (pos[0] + pos[4]) / 2;
		float img_centerY = (pos[1] + pos[5]) / 2;
		float img_well_left = pos[0] + (pos[4] - pos[0]) / 3;
		float img_well_right = pos[4] - (pos[4] - pos[0]) / 3;
		float img_well_top = pos[1] + (pos[5] - pos[1]) / 3;
		float img_well_bottom = pos[5] - (pos[5] - pos[1]) / 3;
		float tip_area = 2.5f;

		pos1 = GetShapeRealPoints(canvas, shape, false);
		float centerX = (pos1[0] + pos1[4]) / 2;
		float centerY = (pos1[1] + pos1[5]) / 2;
		//图片中心线
		drawVLine(canvas, centerX, img_centerX, tip_area, pos[1], pos[7]);
		drawHLine(canvas, centerY, img_centerY, tip_area, pos[0], pos[2]);

		//井字线
		drawHLine(canvas, centerY, img_well_top, tip_area, pos[0], pos[2]);
		drawHLine(canvas, centerY, img_well_bottom, tip_area, pos[0], pos[2]);
		drawVLine(canvas, centerX, img_well_left, tip_area, pos[1], pos[7]);
		drawVLine(canvas, centerX, img_well_right, tip_area, pos[1], pos[7]);
	}

	/**
	 * 画竖线
	 * @param canvas
	 * @param autoX	手指移动的素材的X
	 * @param staticX	固定的素材的X
	 * @param tip_area	范围
	 * @param y1	y方向的起始坐标
	 * @param y2	y方向的结束坐标
	 */
	private boolean drawVLine(Canvas canvas, float autoX, float staticX, float tip_area, float y1, float y2)
	{
		float x1, x2;
		boolean flag = false;
		if(Math.abs(autoX - staticX) < tip_area)
		{
			//竖线
			x1 = x2 = staticX;
			drawLine(canvas, x1, y1, x2, y2, temp_paint);
			flag = true;
		}
		return flag;
	}

	/**
	 *  画横线
	 */
	private boolean drawHLine(Canvas canvas, float autoY, float staticY, float tip_area, float x1, float x2)
	{
		float y1, y2;
		boolean flag = false;
		if(Math.abs(autoY - staticY) < tip_area)
		{
			y1 = y2 = staticY;
//			System.out.println("drawH");
			drawLine(canvas, x1, y1, x2, y2, temp_paint);
			flag = true;
		}
		return flag;
	}

	private void drawLine(Canvas canvas, float x1, float y1, float x2, float y2, Paint p)
	{
		canvas.save();
		canvas.drawLine(x1, y1, x2, y2, p);
		canvas.restore();
	}

	protected void DrawItem2(Canvas canvas, VideoText item)
	{
		if(item != null && item.m_bmp != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			temp_paint.setAlpha(item.m_animAlpha);
			GetAnimMatrix(temp_matrix, item);
			canvas.save();
			canvas.drawBitmap(GetShowBmp(item), temp_matrix, temp_paint);
			canvas.restore();
		}
	}

	@Override
	protected void DrawRect(Canvas canvas, ShapeEx item)
	{
		temp_dst = GetShapeRealPoints(canvas, item, true);
		temp_path.reset();
		temp_path.moveTo(temp_dst[0], temp_dst[1]);
		temp_path.lineTo(temp_dst[2], temp_dst[3]);
		temp_path.lineTo(temp_dst[4], temp_dst[5]);
		temp_path.lineTo(temp_dst[6], temp_dst[7]);
		temp_path.close();

		temp_paint.reset();
		temp_paint.setStyle(Paint.Style.FILL);
		temp_paint.setColor(0x33FFFFFF);
		temp_paint.setStrokeCap(Paint.Cap.SQUARE);
		temp_paint.setStrokeJoin(Paint.Join.MITER);
		temp_paint.setStrokeWidth(2);
		temp_paint.setAntiAlias(true);

		temp_paint.setStyle(Paint.Style.STROKE);
		temp_paint.setColor(0xA0FFFFFF);
		canvas.drawPath(temp_path, temp_paint);
	}

	@Override
	protected void DrawButtons(Canvas canvas, ShapeEx item)
	{
		m_editBtnVisible = false;
		if (m_editBtn != null && item instanceof  VideoText && ((VideoText)item).m_editable) {
			m_editBtnVisible = true;
			temp_point_src[0] = temp_dst[2];
			temp_point_src[1] = temp_dst[3];
			GetLogicPos(temp_point_dst, temp_point_src);

			m_editBtn.m_x = temp_point_dst[0] - m_editBtn.m_centerX;
			m_editBtn.m_y = temp_point_dst[1] - m_editBtn.m_centerY;

			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			GetShowMatrixNoScale(temp_matrix, m_editBtn);
			canvas.drawBitmap(m_editBtn.m_bmp, temp_matrix, temp_paint);
		}
	}

	/**
	 * 拿到当前显示的那个水印的bitmap
	 * @param item
	 * @return
	 */
	public Bitmap GetShowBmp(VideoText item)
	{
		Bitmap out = null;
		if(item != null && item.m_textInfo != null)
		{
			if(item.m_textInfo.animation_id == VideoText.ANIM2)
			{
				out = Bitmap.createBitmap(item.m_w, item.m_h, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(out);

				Paint pt = new Paint();
				pt.setColor(0xFFFFFFFF);
				pt.setAntiAlias(true);
				pt.setFilterBitmap(true);
				pt.setStyle(Paint.Style.FILL);
				RectF rectF = new RectF(0, 0, item.m_animW, item.m_h);
				canvas.drawRect(rectF, pt);

				pt.reset();
				pt.setAntiAlias(true);
				pt.setFilterBitmap(true);
				pt.setXfermode(m_mode);
				Matrix m = new Matrix();
				canvas.drawBitmap(item.m_bmp, m, pt);
			}
			else if(item.m_textInfo.animation_id == VideoText.ANIM1)
			{
				out = Bitmap.createBitmap(item.m_w, item.m_h, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(out);

				Paint pt = new Paint();
				pt.setColor(0xFFFFFFFF);
				pt.setAntiAlias(true);
				pt.setFilterBitmap(true);
				pt.setStyle(Paint.Style.FILL);
				RectF rectF = new RectF(item.m_w - item.m_animW, 0, item.m_w, item.m_h);
				canvas.drawRect(rectF, pt);

				pt.reset();
				pt.setAntiAlias(true);
				pt.setFilterBitmap(true);
				pt.setXfermode(m_mode);
				Matrix m = new Matrix();
				canvas.drawBitmap(item.m_bmp, m, pt);
			}
			else
			{
				out = item.m_bmp;
			}
		}
		return out;
	}

	protected void GetAnimMatrix(Matrix matrix, VideoText item)
	{
		matrix.reset();
		GetShowMatrix(item.m_matrix, item);
		matrix.set(item.m_matrix);
//		matrix.postConcat(m_viewSizeMatrix);
		temp_src[0] = 0;
		temp_src[1] = 0;
		temp_src[2] = item.m_w;
		temp_src[3] = 0;
		temp_src[4] = item.m_w;
		temp_src[5] = item.m_h;
		temp_src[6] = 0;
		temp_src[7] = item.m_h;
		matrix.mapPoints(temp_dst, temp_src);
		float dx = (temp_dst[0] + temp_dst[4]) / 2;
		float dy = (temp_dst[1] + temp_dst[5]) / 2;

		matrix.postTranslate(item.m_showAnimDx, item.m_showAnimDy);
		matrix.postScale(item.m_animScaleX, item.m_animScaleY, dx, dy);
		matrix.postRotate(item.m_animRotate, dx, dy);
	}

	/***
	 * 添加水印
	 * @param info
	 * @return
	 */
	public int AddWatermark(WaterMarkInfo info)
	{
		if(GetPendantIdleNum() > 0 && info != null)
		{
			VideoText item = new VideoText(getContext(), info, m_viewport.m_w, m_viewport.m_h);
			item.m_ex = info;
			item.m_bmp = item.GetOutBmp(info);
			PointF point = getPendantPoints(info, item);
			item.m_x = point.x;
			item.m_y = point.y;
			if(info.m_fontsInfo == null || info.m_fontsInfo.size() == 0)
			{
				item.m_editable = false;
			}
			else
			{
				item.m_editable = true;
			}
			GetShowMatrix(item.m_matrix, item);
			item.setAnimCallback(m_animCB);
			m_pendantArr.add(item);
			initAnimData(item);
			return m_pendantArr.size() - 1;
		}

		return -1;
	}

	/**
	 * 添加水印
	 * @param info
	 * @return
	 */
	public int AddVideoText(VideoText info)
	{
		if(GetPendantIdleNum() > 0 && info != null)
		{
			GetShowMatrix(info.m_matrix, info);
			info.setAnimCallback(m_animCB);
			m_pendantArr.add(info);
			initAnimData(info);
			return m_pendantArr.size() - 1;
		}
		return -1;
	}

	public VideoText getVideText()
	{
		if(m_pendantArr != null && m_pendantArr.size() > 0)
		{
			return (VideoText) m_pendantArr.get(0);
		}
		return null;
	}

	/**
	 * 删除当前水印，不能用{@link #DelAllPendant()} {@link #DelPendant()}删除视频水印, 以上两个方法没有释放内存的操作， 容易造成内存溢出
	 * @return
	 */
	public VideoText DeleteWatermark()
	{
		if(m_pendantArr != null && m_pendantArr.size() > 0)
		{
			VideoText item =  (VideoText)m_pendantArr.remove(0);
			item.ReleaseMem();
			UpdateUI();
			m_pendantCurSel = -1;
			return item;
		}
		return null;
	}

	@Override
	public void DelAllPendant()
	{
		super.DelAllPendant();
	}

	protected boolean initAnimData(VideoText item)
	{
		boolean isAnim = true;
		if(item.m_textInfo.animation_id == VideoText.ANIM_NONE)
		{
			isAnim = false;
			UpdateUI();
		}
		else
		{
			item.m_animAlpha = 0;
			item.m_animW = 0;
		}
		return isAnim;
	}

	/**
	 * 获取当前水印的时间(包括用户调整停留时间)
	 * @param item
	 * @return
	 */
	private int GetAnimTime(VideoText item)
	{
		int time = item.m_textInfo.animation_be;
		if(item.m_textInfo != null && item.m_textInfo.m_animInfo != null)
		{
			int size = item.m_textInfo.m_animInfo.size();
			time += (int)(size / 25f * 1000);
		}
		time += 40 + item.m_animStayTime;
		return time;
	}

	private int GetAnimTimeNoStayTime(VideoText item)
	{
		int time = item.m_textInfo.animation_be;
		if(item.m_textInfo != null && item.m_textInfo.m_animInfo != null)
		{
			int size = item.m_textInfo.m_animInfo.size();
			time += (int)(size / 25f * 1000);
		}
		time += 40;
		return time;
	}

	protected VideoText.AnimCallback m_animCB = new VideoText.AnimCallback()
	{
		@Override
		public void onUpdate(VideoText item)
		{
			item.m_bmp = item.GetOutBmp(item.m_textInfo);
			UpdateUI();
		}

		@Override
		public void onEnd(VideoText item)
		{
			item.m_bmp = item.GetOutBmp(item.m_textInfo);
			UpdateUI();
		}
	};

	public void ShowLastFrame(boolean show)
	{
		m_showLastFrame = show;
		if(m_pendantArr.size() > 0)
		{
			VideoText item = (VideoText)m_pendantArr.get(0);
			if(item != null)
			{
				item.ShowLastFrame(show);
			}
		}
	}

	/**
	 * 当水印在视频结尾的时候添加的时候，计算水印延迟开始时间
	 * @param time	-1 不延迟
	 */
	public void SetVideoTime(int time)
	{
		m_videoTime = time;
		if (m_pendantArr != null && m_pendantArr.size() > 0)
		{
			for(ShapeEx item : m_pendantArr)
			{
				if(item instanceof VideoText)
				{
					((VideoText)item).SetVideoTime(time);
					if(time > 0)
					{
						((VideoText)item).setStartTime(0);
					}
				}
			}
		}
	}

	/**
	 * 更新当前水印某一行的水印
	 * @param index	水印所代表的id
	 * @param text
	 * @return
	 */
	public Bitmap UpdateText(int index, String text)
	{
		if(m_pendantArr.size() > 0)
		{
			VideoText item = (VideoText)m_pendantArr.get(0);
			if(item != null)
			{
				float lastCX = item.m_centerX;
				float lastCY = item.m_centerY;
				item.m_bmp = item.UpdateText(text, index);
				float dx = item.m_centerX - lastCX;
				float dy = item.m_centerY - lastCY;
				String align = item.m_textInfo.align;
				if (align.equals(MyTextInfo.ALIGN_TOP_RIGHT))
				{
					item.m_x = item.m_x - dx;
				}
				else if (align.equals(MyTextInfo.ALIGN_TOP_CENTER))
				{
					item.m_x = item.m_x - dx / 2f;
				}
				else if (align.equals(MyTextInfo.ALIGN_CENTER_LEFT))
				{
					item.m_y = item.m_y - dy / 2f;
				}
				else if (align.equals(MyTextInfo.ALIGN_CENTER))
				{
					item.m_x = item.m_x - dx / 2f;
					item.m_y = item.m_y - dy / 2f;
				}
				else if (align.equals(MyTextInfo.ALIGN_CENTER_RIGHT))
				{
					item.m_y = item.m_y - dy / 2f;
					item.m_x = item.m_x - dx;
				}
				else if (align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
				{
					item.m_y = item.m_y - dy;
				}
				else if (align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
				{
					item.m_y = item.m_y - dy;
					item.m_x = item.m_x - dx / 2f;
				}
				else if (align.equals(MyTextInfo.ALIGN_BOTTOM_RIGHT))
				{
					item.m_y = item.m_y - dy;
					item.m_x = item.m_x - dx;
				}
				item.setAnimCallback(m_animCB);
				initAnimData(item);
				return item.m_bmp;
			}
		}
		return null;
	}

	public void SetOutTextInfo(WaterMarkInfo info, int width, int height, float scale)
	{
		m_pendantArr.clear();
		VideoText item = new VideoText(getContext(), info, m_outWidth, m_outHeight);
		item.m_ex = info;
		m_outHeight = height;
		m_outWidth = width;
		m_isOutput = true;

		item.m_bmp = item.GetOutBmp(m_outWidth, m_outHeight, scale, item.m_textInfo);
		PointF point = getOutPendantPoints(item.m_textInfo, item);
		item.m_x = point.x;
		item.m_y = point.y;
		InitOutMatrix(item);
		item.setAnimCallback(m_animCB);
		m_pendantArr.add(item);
		initAnimData(item);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(m_uiEnabled && isClickable() && getVisibility() == View.VISIBLE)
		{
			switch(event.getAction())
			{
				case MotionEvent.ACTION_MOVE:
				{
					OddMove(event);

					break;
				}

				case MotionEvent.ACTION_DOWN:
				{
					m_downX = event.getX();
					m_downY = event.getY();
					OddDown(event);
					break;
				}

				case MotionEvent.ACTION_OUTSIDE:
				case MotionEvent.ACTION_UP:
				{
					//System.out.println("ACTION_UP");
					//System.out.println(event.getX() + "-" + event.getY());
					OddUp(event);
					break;
				}
			}
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 设置输出的水印信息
	 * @param info
	 * @param width
	 * @param height
	 * @param org
	 */
	public void SetOutTextInfo(VideoText info, int width, int height, ShapeEx org)
	{
		if(info != null && org != null)
		{
			VideoText item = new VideoText(getContext(), (WaterMarkInfo)info.m_ex, width, height);
			int tempW = info.m_w;
			int tempH = info.m_h;
			float[] src = {info.m_x + info.m_centerX, info.m_y + info.m_centerY};

			float[] dst1 = new float[2];
			float scale = width / (org.m_w * org.m_scaleX);
			temp_matrix.reset();
			temp_matrix.postConcat(org.m_matrix);
			temp_matrix.postScale(scale, scale);
			temp_matrix.mapPoints(dst1, src);

			m_outHeight = height;
			m_outWidth = width;
			m_isOutput = true;

			item.m_bmp = item.GetOutBmp(m_outWidth, m_outHeight, 1, info.m_textInfo);

			item.m_x = dst1[0] - item.m_centerX;
			item.m_y = dst1[1] - item.m_centerY;
			item.m_scaleX = info.m_scaleX * tempW * scale / info.m_w;
			item.m_scaleY = info.m_scaleY * tempH * scale / info.m_h;
			item.m_degree = info.m_degree;
			item.m_matrix.reset();
			item.m_matrix.postTranslate(dst1[0] - item.m_centerX, dst1[1] - item.m_centerY);
			item.m_matrix.postScale(item.m_scaleX, item.m_scaleY, dst1[0], dst1[1]);
			item.m_matrix.postRotate(item.m_degree, dst1[0], dst1[1]);
			item.setStartTime(info.m_animStartTime);
			item.setStayTime(info.m_animStayTime);

			item.setAnimCallback(m_animCB);
			m_pendantArr.add(item);
			initAnimData(item);
		}
	}

	/**
	 * 拿到输出的水印图片
	 * @param time
	 * @return
	 */
	public synchronized VideoText GetOutTextBmp(int time)
	{
		setCurTime(time);
		if(m_pendantArr != null && m_pendantArr.size() > 0)
		{
			return (VideoText)m_pendantArr.get(0);
		}
		return null;
	}

	@Override
	protected void OddUp(MotionEvent event)
	{
		if(m_isTouch)
		{
			if (m_operateMode == MODE_PENDANT)
			{
				if(m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size()){
					ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
					AdjustShape(shape);

					UpdateUI();
				}

				if(m_pendantCurSel >= 0)
				{
					if(m_cb instanceof VideoTextCallback)
					{
						((VideoTextCallback)m_cb).onDragEnd();
					}
				}
			}
			if(needShowEdit && m_editBtnVisible)
			{
				if(m_cb instanceof VideoTextCallback)
				{
					((VideoTextCallback)m_cb).OnEditBtn();
				}
			}
			if(m_cb instanceof VideoTextCallback)
			{
				((VideoTextCallback)m_cb).OnViewTouch(m_hasChoose);
			}


			super.OddUp(event);
		}

	}

	@Override
	public void SetSelPendant(int index)
	{
		super.SetSelPendant(index);
		if(m_pendantCurSel >= 0)
		{
			m_hasChoose = true;
			needShowEdit = true;
		}else{
			m_hasChoose = false;
			needShowEdit = false;
		}
		UpdateUI();
	}

	/**
	 * 放手如果超过屏幕某个区域自动回弹
	 * @param shape
	 */
	protected void AdjustShape(ShapeEx shape)
	{
		if(shape == null)
			return;
//		System.out.println("degree: " + shape.m_degree);
//		System.out.println("shape: " + shape);

		Canvas canvas = new Canvas();
		float[] points = GetShapeRealPoints(canvas, shape, false);
		float centerX = (points[0] + points[4]) / 2;
		float centerY = (points[1] + points[5]) / 2;

		float[] src = new float[]{centerX, centerY};
		float[] dst = new float[]{src[0], src[1]};

		//判断是否超出区域
		float limit;
		if(m_viewport.m_w > m_viewport.m_h)
		{
			limit = def_limit_sacle * m_viewport.m_w * m_viewport.m_scaleX;
		}
		else
		{
			limit = def_limit_sacle * m_viewport.m_h * m_viewport.m_scaleY;
		}
		float w2 = m_viewport.m_centerX * m_viewport.m_scaleX;
		float h2 = m_viewport.m_centerY * m_viewport.m_scaleY;
		float left = m_viewport.m_x + m_viewport.m_centerX - w2;
		float top = m_viewport.m_y + m_viewport.m_centerY - h2;
		float right = m_viewport.m_x + m_viewport.m_centerX + w2;
		float bottom = m_viewport.m_y + m_viewport.m_centerY + h2;
		float imgw2 = ImageUtils.Spacing(points[0] - points[2], points[1] - points[3]) / 2f;
		float imgh2 = ImageUtils.Spacing(points[0] - points[6], points[1] - points[7]) / 2f;

		if(imgw2 > limit)
		{
			left -= imgw2 - limit;
			right += imgw2 - limit;
		}

		if(imgh2 > limit)
		{
			top -= imgh2 - limit;
			bottom += imgh2 - limit;
		}

		float cx = centerX;
		float cy = centerY;
		float minX = cx - imgw2 / 2f;
		float minY = cy - imgh2 / 2f;
		float maxX = cx + imgw2 / 2f;
		float maxY = cy + imgh2 / 2f;
		float[] src1 = new float[]{left, top, right, bottom};
		float[] tran1 = new float[]{left, 0, right, 0, 0, top, 0, bottom};

		float[] src2 = new float[]{minX, minY, maxX, maxY};
		float[] tran2 = new float[]{minX, 0, maxX, 0, 0, minY, 0, maxY};

		float[] result = new float[]{src1[0] - src2[0], src1[1] - src2[1], src1[2] - src2[2], src1[3] - src2[3]};

		if(cx < left)
		{
			shape.m_matrix.postTranslate(result[0], tran1[1] - tran2[1]);
			shape.m_x = shape.m_x + result[0];
			shape.m_y = shape.m_y + tran1[1] - tran2[1];
		}
		else if(cx > right)
		{
			shape.m_matrix.postTranslate(result[2], tran1[3] - tran2[3]);
			shape.m_x = shape.m_x + result[2];
			shape.m_y = shape.m_y + tran1[3] - tran2[3];
		}
		if(cy < top)
		{
			shape.m_matrix.postTranslate(tran1[4] - tran2[4], result[1]);
			shape.m_y = shape.m_y + result[1];
			shape.m_x = shape.m_x + tran1[4] - tran2[4];
		}
		else if(cy > bottom)
		{
			shape.m_matrix.postTranslate(tran1[6] - tran2[6], result[3]);
			shape.m_y = shape.m_y + result[3];
			shape.m_x = shape.m_x + tran1[4] - tran2[4];
		}
	}

	protected void InitOutMatrix(VideoText item)
	{
		item.m_matrix.reset();
		float[] dst = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};

		item.m_matrix.postTranslate(dst[0] - item.m_centerX, dst[1] - item.m_centerY);
		item.m_matrix.postScale(item.m_scaleX, item.m_scaleY, dst[0], dst[1]);
		item.m_matrix.postRotate(item.m_degree, dst[0], dst[1]);
	}

	/**
	 * 根据后台给的数据计算水印初始位置
	 * @param info
	 * @param item
	 * @return
	 */
	protected PointF getPendantPoints(WaterMarkInfo info, ShapeEx item)
	{
		String align = info.align;
		int width = item.m_w;
		int height = item.m_h;
		float viewWidth = m_viewport.m_w * m_viewport.m_scaleX;
		float viewHeight = m_viewport.m_h * m_viewport.m_scaleY;
		if ((((width / 2) >= (viewWidth - 1)) || ((height / 2) >= viewHeight - 1)))
		{
			item.m_scaleX = item.m_scaleY = 1 * ShareData.m_screenWidth / 720f;
		}
		else
		{
			item.m_scaleX = item.m_scaleY = 1;
		}
		float[] dst = new float[2];
		dst[0] = m_viewport.m_x + m_viewport.m_centerX - m_viewport.m_centerX * m_viewport.m_scaleX;
		dst[1] = m_viewport.m_y + m_viewport.m_centerY - m_viewport.m_centerY * m_viewport.m_scaleY;
		float[] offsets = new float[]{info.offsetX, info.offsetY};
		float[] viewSizes = new float[]{viewWidth, viewHeight};
		int[] bmpSizes = new int[]{width, height};
		PointF point = getEditablePoints(dst, offsets, align, viewSizes, bmpSizes, item);
		return point;
	}

	protected PointF getOutPendantPoints(WaterMarkInfo info, ShapeEx item)
	{
		String align = info.align;
		int width = item.m_w;
		int height = item.m_h;

		float viewWidth = m_outWidth;
		float viewHeight = m_outHeight;
		float[] dst = new float[2];
		dst[0] = 0;
		dst[1] = 0;
		float[] offsets = new float[]{info.offsetX, info.offsetY};
		float[] viewSizes = new float[]{viewWidth, viewHeight};
		int[] bmpSizes = new int[]{width, height};
		PointF point = getEditablePoints(dst, offsets, align, viewSizes, bmpSizes, item);
		return point;
	}

	protected PointF getEditablePoints(float[] dst, float[] offsets, String align, float[] viewSize, int[] bmpSize, ShapeEx item)
	{
		PointF point = new PointF();
		float originX = dst[0];
		float originY = dst[1];
		float width = bmpSize[0] * item.m_scaleX;
		float height = bmpSize[1] * item.m_scaleY;
		float viewWidth = viewSize[0];
		float viewHeight = viewSize[1];
		float offsetX = offsets[0];
		float offsetY = offsets[1];
		float dx = item.m_centerX * item.m_scaleX - item.m_centerX;
		float dy = item.m_centerY * item.m_scaleY - item.m_centerY;
		if (align.equals(MyTextInfo.ALIGN_TOP_RIGHT))
		{
			originX += viewWidth - width;
		}
		else if (align.equals(MyTextInfo.ALIGN_TOP_CENTER))
		{
			originX += (viewWidth - width) / 2f;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER_LEFT))
		{
			originY += (viewHeight - height) / 2f;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER))
		{
			originX += (viewWidth - width) / 2f;
			originY += (viewHeight - height) / 2f;
		}
		else if (align.equals(MyTextInfo.ALIGN_CENTER_RIGHT))
		{
			originX += viewWidth - width;
			originY += (viewHeight - height) / 2f;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_LEFT))
		{
			originY += viewHeight - height;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_CENTER))
		{
			originX += (viewWidth - width) / 2f;
			originY += viewHeight - height;
		}
		else if (align.equals(MyTextInfo.ALIGN_BOTTOM_RIGHT))
		{
			originX += viewWidth - width;
			originY += viewHeight - height;
		}
		point.x = originX + offsetX * viewWidth * 1024 / 640 + dx;
		point.y = originY + offsetY * viewHeight * 1024 / 640 + dy;
		return point;
	}

	public static interface VideoTextCallback extends ControlCallback
	{
		public void OnViewTouch(boolean hasChoose);

		public void OnEditBtn();

		public void onDragEnd();
	}
}
