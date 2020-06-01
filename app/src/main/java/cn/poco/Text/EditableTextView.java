package cn.poco.Text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.view.MotionEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.poco.beautify.BeautifyViewV3;
import cn.poco.display.CoreViewV3;
import cn.poco.graphics.ShapeEx;
import cn.poco.imagecore.ProcessorV2;
import cn.poco.resource.MyLogoRes;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.tianutils.SimpleTimer;
import cn.poco.utils.Utils;

/**
 * 可编辑文字
 */
public class EditableTextView extends BeautifyViewV3
{
	private static final int OPERATE_FREE = 1;
	private static final int OPERATE_ALL = 2;

	protected int m_operate = OPERATE_ALL; //主要针对文字，可以拆分模块和合并模块
	protected boolean m_longPressEnabled = true;	//控制是否可以长按拆分

	protected boolean m_isCompare = false; //对比
	protected OnMotifyListener m_onMotifiListener;

	public int def_color_chooser_res = 0; //换文字按钮
	protected ShapeEx m_colorChooserBtn;
	protected boolean m_hasColorChooserBtn = true;
	protected boolean m_colorChooserBtnVisible = false;

	public int def_delete_res = 0;	//删除按钮
	protected ShapeEx m_deleteBtn;
	protected boolean m_deleteBtnVisible = false;

	public int def_save_res = 0;	//保存按钮
	protected ShapeEx m_saveBtn;
	protected boolean m_saveBtnVisible = false;

	protected ShapeEx m_divideBtn;	//拆分按钮
	public int def_divide_res = 0;
	protected boolean m_divideBtnVisible = false;

	protected ShapeEx m_mergeBtn;	//合并按钮
	public int def_merge_res = 0;
	protected boolean m_mergeBtnVisible = false;

	protected Matrix m_oldMatrix = new Matrix();
	protected float m_centerX;	//旋转、缩放中心点
	protected float m_centerY;
	//	protected ShapeEx5 m_parent;		//如果文字拆分，这个相当于当前文字的父view
	protected boolean m_parentFlag = false;		//从文字组合状态长按拆分，整体的框架每个模块显示白框1S，为true显示父view的白框
	protected boolean m_showAllChoose = false;

	protected boolean m_absorbing = false;    //是否正在取色
	protected ShapeEx m_absorbIcon;
	protected boolean m_showAbsorbIcon = true;
	public int def_absorb_res = 0;	//删除按钮
	private Bitmap m_absorbBmp = null;
	private int m_radius = ShareData.PxToDpi_xhdpi(100);
	private int m_curColor;
	private int m_lastColor;

	private int m_lastSelPendant = -1;
	private int m_lastSelTextItem = -1;	//上次选中拆分状态下的文字

	/*protected boolean m_drawSpaceLine = false;		//是否启动网格线动画
	protected int def_rect_color = 0x66ffffff; //外框矩形颜色
	protected int def_line_color = 0x33ffffff; //里面的线颜色
	private SimpleTimer m_spaceLineAnim;
	private int m_ratio = 0;*/

	public EditableTextView(Context context, int frW, int frH)
	{
		super(context, frW, frH);
		CommonUtils.CancelViewGPU(this);
	}

	@Override
	public void InitData(CoreViewV3.ControlCallback cb)
	{
		super.InitData(cb);

		m_colorChooserBtn = InitBtn(def_color_chooser_res);
		m_deleteBtn = InitBtn(def_delete_res);
		m_saveBtn = InitBtn(def_save_res);
		m_divideBtn = InitBtn(def_divide_res);
		m_mergeBtn = InitBtn(def_merge_res);
		m_absorbIcon = InitBtn(def_absorb_res);
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

	public void SetLongPressEnabled(boolean longPressEnabled)
	{
		m_longPressEnabled = longPressEnabled;
	}

	public void SetAbsorbing(boolean absorb)
	{
		m_absorbing = absorb;
		UpdateUI();
	}

	@Override
	protected void DrawToCanvas(Canvas canvas, int mode)
	{
		canvas.save();

		canvas.setDrawFilter(temp_filter);

		//控制渲染矩形
		ClipStage(canvas);

		//画背景
		DrawBK(canvas, m_bk, m_bkColor);

		//画图片
		DrawItem(canvas, m_img);

		//画边框
		DrawItem(canvas, m_frame);

		if (!m_isCompare)
		{
//			if(m_drawSpaceLine)
//			{
////				DrawSpaceLine(canvas);
//			}

			//画装饰
			int len = m_pendantArr.size();
			for (int i = 0; i < len; i++)
			{
				ShapeEx item = m_pendantArr.get(i);
				if(item instanceof ShapeEx5)
				{
					DrawItem2(canvas, (ShapeEx5)item);
				}
				else
				{
					DrawItem(canvas, item);
				}

			}

			//画选中框和按钮
			if ((m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()) || m_showAllChoose)
			{
				ShapeEx5 parent = null;
				ShapeEx temp = null;
				if(m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size()){
					temp = m_pendantArr.get(m_pendantCurSel);
				}else if(m_showAllChoose)
				{
					temp = GetPendantByIndex(0);
				}
				if(temp instanceof ShapeEx5)
				{
					parent = (ShapeEx5)temp;
					//画文字选中框
					((ShapeEx5)temp).DrawRect(canvas);
					if(m_operate == OPERATE_FREE)
					{
						temp = ((ShapeEx5)temp).GetCurModule();
					}
				}
				if(m_parentFlag && m_operate == OPERATE_FREE && parent != null)
				{
					//画选中框
					int size = parent.GetModuleCount();
					for(int i = 0; i < size; i ++)
					{
						ShapeEx3 item = parent.GetModule(i);
						if(item != null)
						{
							if(!m_isTouch || m_isFrameAnim)
							{
								DrawRect(canvas, item);
							}
						}
					}
				}
				if(temp != null)
				{
					//画选中框
					if(!m_isTouch || m_isFrameAnim)
					{
						DrawRect(canvas, temp);
					}

					//画按钮
					if (!m_isTouch && !m_absorbing)
					{
						DrawButtons(canvas, temp);
					}

					//
					if (!m_absorbing && m_isTouch)
					{
						if(temp instanceof ShapeEx5)
						{
							drawTipLine(canvas, (ShapeEx5)temp);
						}
						else if(parent != null && parent instanceof ShapeEx5)
						{
							drawTipLine(canvas, parent);
						}
						else if(temp instanceof ShapeEx4)
						{
							drawTipLine(canvas, temp);
						}
					}
				}

			}

			if(m_absorbing && m_isTouch)
			{
				DrawAbsorbIcon(canvas);
			}
		}

		canvas.restore();
	}

	protected void DrawItem2(Canvas canvas, ShapeEx5 item)
	{
		canvas.save();

		item.draw(canvas);

		canvas.restore();
	}

	protected void DrawOutputItem2(Canvas canvas, ShapeEx5 item, Matrix matrix)
	{
		canvas.save();

		item.DrawOutput(canvas, matrix);

		canvas.restore();
	}

	protected SimpleTimer m_timer;
	protected void startAnim(final ShapeEx5 item)
	{
		cancelAnim();
		item.StartTextAnim();
		m_timer = new SimpleTimer(500, 4, new SimpleTimer.TimerEventListener()
		{

			@Override
			public void OnTimer(int currentCount)
			{
				item.DoTextAnim();
				UpdateUI();
			}
		});
		m_timer.Start();
	}

	protected void cancelAnim()
	{
		if (m_timer != null)
		{
			m_timer.Cancel();
			m_timer = null;
		}
	}

	protected SimpleTimer m_frameTimer;
	protected boolean m_isFrameAnim = false;
	protected void startFrameAnim()
	{
		cancelFrameAnim();
		m_isFrameAnim = true;
		m_frameTimer = new SimpleTimer(500, 5, new SimpleTimer.TimerEventListener()
		{
			@Override
			public void OnTimer(int currentCount)
			{
				m_parentFlag = !m_parentFlag;
				UpdateUI();
			}
		});
		m_frameTimer.Start();
	}

	protected void cancelFrameAnim()
	{
		m_isFrameAnim = false;
		if (m_frameTimer != null)
		{
			m_frameTimer.Cancel();
			m_frameTimer = null;
		}
	}

	/**
	 *	网格线动画
	 * @param start [0-255]
	 * @param end [0-255]
	 *//*
	private void startSpaceLineAnim(final int start, final int end)
	{
		stopSpaceLineAnim();
		m_spaceLineAnim = new SimpleTimer(10, 25, new SimpleTimer.TimerEventListener()
		{
			@Override
			public void OnTimer(int currentCount)
			{
				m_ratio = (end - start) * currentCount / 25;
				if(start > end)
				{
					m_ratio = Math.abs(start - end) + m_ratio;
					if(m_ratio == 0)
					{
						m_drawSpaceLine = false;
					}
				}
				UpdateUI();
			}
		});
		m_spaceLineAnim.Start();
	}

	private void stopSpaceLineAnim()
	{
		if (m_spaceLineAnim != null)
		{
			m_spaceLineAnim.Cancel();
			m_spaceLineAnim = null;
		}
	}*/

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

		float[] pos = GetShapeRealPoints(canvas, m_img, false);
		float[] pos1;
		float img_centerX = (pos[0] + pos[4]) / 2;
		float img_centerY = (pos[1] + pos[5]) / 2;
		float img_well_left = pos[0] + (pos[4] - pos[0]) / 3;
		float img_well_right = pos[4] - (pos[4] - pos[0]) / 3;
		float img_well_top = pos[1] + (pos[5] - pos[1]) / 3;
		float img_well_bottom = pos[5] - (pos[5] - pos[1]) / 3;
		float x1, y1, x2, y2;
		float tip_area = 2.5f;

		if(m_operate == OPERATE_ALL || shape instanceof ShapeEx4)
		{
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
		else if(shape instanceof ShapeEx5)
		{
			ShapeEx5 item = (ShapeEx5)shape;
			ShapeEx3 child = item.GetCurModule();
			if(child != null)
			{
				pos1 = GetShapeRealPoints(canvas, child, false);
				float centerX = (pos1[0] + pos1[4]) / 2;
				float centerY = (pos1[1] + pos1[5]) / 2;
				boolean tag = false;
				//图片中心线
				drawVLine(canvas, centerX, img_centerX, tip_area, pos[1], pos[7]);
				drawHLine(canvas, centerY, img_centerY, tip_area, pos[0], pos[2]);

				//井字线
				drawHLine(canvas, centerY, img_well_top, tip_area, pos[0], pos[2]);
				drawHLine(canvas, centerY, img_well_bottom, tip_area, pos[0], pos[2]);
				drawVLine(canvas, centerX, img_well_left, tip_area, pos[1], pos[7]);
				drawVLine(canvas, centerX, img_well_right, tip_area, pos[1], pos[7]);

				if(!tag)
				{
					//其它模块中心线
					int size = item.GetModuleCount();
					int curModule = item.GetCurSelModule();
					float childDegree = child.m_degree + item.m_degree;
					ShapeEx3 module;
					float[] pos2;
					for(int i = 0; i < size; i ++)
					{
						boolean flag = false;
						if(i != curModule)
						{
							module = item.GetModule(i);
							float degree = module.m_degree + item.m_degree;
							pos2 = GetShapeRealPoints(canvas, module, false);
							//上下左右
							if(Math.abs(degree) < 1 && Math.abs(childDegree) < 1)
							{
								if(Math.abs(pos1[1] - pos2[1]) < tip_area || Math.abs(pos1[7] - pos2[1]) < tip_area)
								{
									x1 = pos[0];
									x2 = pos[2];
									y1 = y2 = pos2[1];
									drawLine(canvas, x1, y1, x2, y2, temp_paint);
									flag = true;
								}
								else if(Math.abs(pos1[7] - pos2[7]) < tip_area || Math.abs(pos1[1] - pos2[7]) < tip_area)
								{
									x1 = pos[0];
									x2 = pos[2];
									y1 = y2 = pos2[7];
									drawLine(canvas, x1, y1, x2, y2, temp_paint);
									flag = true;
								}
								if(Math.abs(pos1[0] - pos2[0]) < tip_area || Math.abs(pos1[2] - pos2[0]) < tip_area)
								{
									x1 = x2 = pos2[0];
									y1 = pos[1];
									y2 = pos[7];
									drawLine(canvas, x1, y1, x2, y2, temp_paint);
									flag = true;
								}
								else if(Math.abs(pos1[0] - pos2[2]) < tip_area || Math.abs(pos1[2] - pos2[2]) < tip_area)
								{
									x1 = x2 = pos2[2];
									y1 = pos[1];
									y2 = pos[7];
									drawLine(canvas, x1, y1, x2, y2, temp_paint);
									flag = true;
								}
							}
							float McenterX = (pos2[0] + pos2[4]) / 2;
							float McenterY = (pos2[1] + pos2[5]) / 2;
							//横线
							if(Math.abs(McenterY - centerY) < tip_area)
							{
								x1 = pos[0];
								x2 = pos[2];
								y1 = y2 = McenterY;
								drawLine(canvas, x1, y1, x2, y2, temp_paint);
								flag = true;
							}
							if(Math.abs(McenterX - centerX) < tip_area)
							{
								x1 = x2 = McenterX;
								y1 = pos[1];
								y2 = pos[7];
								drawLine(canvas, x1, y1, x2, y2, temp_paint);
								flag = true;
							}
						}
						if(flag)
						{
							break;
						}
					}
				}
			}
		}
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

	/**
	 * 外框矩形
	 * @param item
	 * @param hasGap
	 * @return
	 */
	public float[] GetShapeRealPoints(Canvas canvas, ShapeEx item, boolean hasGap)
	{
		float[] out = new float[8];

		if(item instanceof ShapeEx5)
		{
			m_hasColorChooserBtn = true;
			temp_src = ((ShapeEx5)item).GetRect();
			canvas.save();
			canvas.concat(item.m_matrix);
			canvas.getMatrix(temp_matrix);
			canvas.restore();
			temp_matrix.mapPoints(out, temp_src);
			if(hasGap){
				out = GetFixRectPoints(item, out);
			}

		}
		else if(item instanceof ShapeEx3)
		{
			m_hasColorChooserBtn = true;
			ShapeEx5 shape = ((ShapeEx3)item).m_parent;

			canvas.save();
			canvas.concat(shape.m_matrix);
			canvas.concat(item.m_matrix);
			canvas.getMatrix(temp_matrix);
			canvas.restore();
			out = ((ShapeEx3)item).getRectPoints(((ShapeEx3)item).m_bmpRect, temp_matrix);

			if(hasGap){
				out = GetFixRectPoints(item, out);
			}
		}
		else if(item != null)
		{
			m_hasColorChooserBtn = false;
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
				out = GetFixRectPoints(item, out);
			}
		}
		return out;
	}

	protected float[] GetFixRectPoints(ShapeEx item, float[] out)
	{
		float DEF_GAP = 0;
		int minW = 1024;
		int minH = 1024;
		if (m_deleteBtn != null)
		{
			DEF_GAP = m_deleteBtn.m_centerX;
			minW = m_deleteBtn.m_w;
			minH = m_deleteBtn.m_h;
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
				if(item instanceof ShapeEx3)
				{
					out = ((ShapeEx3)item).getRectPoints(((ShapeEx3)item).m_bmpRect, temp_matrix);
				}
				else
				{
					temp_matrix.mapPoints(out, temp_src);
				}
				return out;
			}

			Matrix matrix = new Matrix();
			matrix.preConcat(temp_matrix);
			float[] tempP = new float[8];
			matrix.postScale(s1, s1, (out[0] + out[4]) / 2f, (out[1] + out[5]) / 2f);
			if(item instanceof ShapeEx3)
			{
				tempP = ((ShapeEx3)item).getRectPoints(((ShapeEx3)item).m_bmpRect, matrix);
			}
			else
			{
				matrix.mapPoints(tempP, temp_src);
			}

			matrix.reset();
			matrix.postConcat(temp_matrix);
			matrix.postScale(s2, s2, (out[0] + out[4]) / 2f, (out[1] + out[5]) / 2f);
			float[] tempP1 = new float[8];
			if(item instanceof ShapeEx3)
			{
				tempP1 = ((ShapeEx3)item).getRectPoints(((ShapeEx3)item).m_bmpRect, matrix);
			}
			else
			{
				matrix.mapPoints(tempP1, temp_src);
			}

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
		if(m_parentFlag)
		{
			canvas.drawPath(temp_path, temp_paint);
		}

		temp_paint.setStyle(Paint.Style.STROKE);
		temp_paint.setColor(0xA0FFFFFF);
		canvas.drawPath(temp_path, temp_paint);
	}

	@Override
	protected void DrawButtons(Canvas canvas, ShapeEx item)
	{
		m_colorChooserBtnVisible = false;
		m_deleteBtnVisible = false;
		m_saveBtnVisible = false;
		m_divideBtnVisible = false;
		m_mergeBtnVisible = false;
		if (item != null)
		{
			//移动到正确位置
			temp_dst = GetShapeRealPoints(canvas, item, true);

			//右下
			if (m_rotationBtn != null && m_hasRotationBtn)
			{
				temp_point_src[0] = temp_dst[4];
				temp_point_src[1] = temp_dst[5];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_rotationBtn.m_x = temp_point_dst[0] - m_rotationBtn.m_centerX;
				m_rotationBtn.m_y = temp_point_dst[1] - m_rotationBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_rotationBtn);
				canvas.drawBitmap(m_rotationBtn.m_bmp, temp_matrix, temp_paint);
			}
			//上下顺序不能调换
			if (m_nzoomBtn != null && m_hasZoomBtn)
			{
				temp_point_src[0] = temp_dst[0];
				temp_point_src[1] = temp_dst[1];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_nzoomBtn.m_x = temp_point_dst[0] - m_nzoomBtn.m_centerX;
				m_nzoomBtn.m_y = temp_point_dst[1] - m_nzoomBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_nzoomBtn);
				canvas.drawBitmap(m_nzoomBtn.m_bmp, temp_matrix, temp_paint);
			}
			//右上
			if (m_colorChooserBtn != null && m_hasColorChooserBtn) {

				temp_point_src[0] = temp_dst[2];
				temp_point_src[1] = temp_dst[3];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_colorChooserBtn.m_x = temp_point_dst[0] - m_colorChooserBtn.m_centerX;
				m_colorChooserBtn.m_y = temp_point_dst[1] - m_colorChooserBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_colorChooserBtn);
				canvas.drawBitmap(m_colorChooserBtn.m_bmp, temp_matrix, temp_paint);
			}
			//右上
			boolean showSaveBtn = false;
			if((item instanceof ShapeEx4) && ((ShapeEx4) item).m_isLocal == false)
			{
				showSaveBtn = true;
			}
			if(item instanceof ShapeEx5)
			{
				showSaveBtn = true;
			}
			if(item instanceof ShapeEx3)
			{
				ShapeEx5 shape = ((ShapeEx3)item).m_parent;
				if(shape != null && shape.GetModuleCount() == 1)
				{
					showSaveBtn = true;
				}
			}
			if (m_saveBtn != null && showSaveBtn) {
				m_saveBtnVisible = true;
				temp_point_src[0] = temp_dst[2];
				temp_point_src[1] = temp_dst[3];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_saveBtn.m_x = temp_point_dst[0] - m_saveBtn.m_centerX;
				m_saveBtn.m_y = temp_point_dst[1] - m_saveBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_saveBtn);
				canvas.drawBitmap(m_saveBtn.m_bmp, temp_matrix, temp_paint);
			}
			//左上
			if (m_deleteBtn != null)
			{
				m_deleteBtnVisible = true;

				temp_point_src[0] = temp_dst[0];
				temp_point_src[1] = temp_dst[1];
				GetLogicPos(temp_point_dst, temp_point_src);
				m_deleteBtn.m_x = temp_point_dst[0] - m_deleteBtn.m_centerX;
				m_deleteBtn.m_y = temp_point_dst[1] - m_deleteBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_deleteBtn);
				canvas.drawBitmap(m_deleteBtn.m_bmp, temp_matrix, temp_paint);
			}
			//左下
			if(m_divideBtn != null && (item instanceof ShapeEx5) && m_operate == OPERATE_ALL && ((ShapeEx5)item).GetModuleCount() > 1)
			{
				m_divideBtnVisible = true;
				temp_point_src[0] = temp_dst[6];
				temp_point_src[1] = temp_dst[7];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_divideBtn.m_x = temp_point_dst[0] - m_divideBtn.m_centerX;
				m_divideBtn.m_y = temp_point_dst[1] - m_divideBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_divideBtn);
				canvas.drawBitmap(m_divideBtn.m_bmp, temp_matrix, temp_paint);
			}

			if(m_mergeBtn != null && (item instanceof ShapeEx3) && m_operate == OPERATE_FREE && ((ShapeEx3)item).m_parent.GetModuleCount() > 1)
			{
				m_mergeBtnVisible = true;

				temp_point_src[0] = temp_dst[6];
				temp_point_src[1] = temp_dst[7];
				GetLogicPos(temp_point_dst, temp_point_src);

				m_mergeBtn.m_x = temp_point_dst[0] - m_mergeBtn.m_centerX;
				m_mergeBtn.m_y = temp_point_dst[1] - m_mergeBtn.m_centerY;

				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				GetShowMatrixNoScale(temp_matrix, m_mergeBtn);
				canvas.drawBitmap(m_mergeBtn.m_bmp, temp_matrix, temp_paint);
			}
		}
	}

	protected void DrawAbsorbIcon(Canvas canvas)
	{
		if(m_showAbsorbIcon && m_absorbIcon != null)
		{
			temp_paint.reset();
			temp_paint.setShadowLayer(0.5f, 2, 2, 0xaa000000);
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			GetShowMatrix(temp_matrix, m_absorbIcon);
			canvas.save();
			canvas.drawBitmap(m_absorbIcon.m_bmp, temp_matrix, temp_paint);
			canvas.restore();

			temp_paint.reset();
			temp_paint.setStyle(Paint.Style.STROKE);
			temp_paint.setStrokeWidth(ShareData.PxToDpi_xhdpi(25));
			temp_paint.setColor(m_curColor);
			temp_paint.setAntiAlias(true);
			temp_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			temp_paint.setDither(true);
			// 设定阴影(柔边, X 轴位移, Y 轴位移, 阴影颜色)
			temp_paint.setShadowLayer(3f, 1, 1, 0xaa000000);
			float x = m_absorbIcon.m_centerX + m_absorbIcon.m_x;
			float y = m_absorbIcon.m_centerY + m_absorbIcon.m_y;
			RectF rect = new RectF(x - m_radius, y - m_radius, x + m_radius, y + m_radius);
			canvas.save();
			canvas.drawArc(rect, 180, 360, false, temp_paint);
			canvas.restore();

			canvas.save();
			temp_paint.setColor(m_lastColor);
			canvas.drawArc(rect, 0, 180, false, temp_paint);
			canvas.restore();
		}
	}

	/**
	 * 画网格线
	 * @param canvas
	 *//*
	protected void DrawSpaceLine(Canvas canvas)
	{
		float def_rect_width = 2f; //外框矩形宽度
		float def_line_width = 2f; //里面线的宽度
		int areaCount = 12;

		int alpha = Painter.GetAlpha(def_line_color);
		int lineColor = Painter.SetColorAlpha(m_ratio, alpha, def_line_color);

		alpha = Painter.GetAlpha(def_rect_color);
		int rectColor = Painter.SetColorAlpha(m_ratio, alpha, def_rect_color);

		if(m_ratio > 0)
		{
			temp_paint.reset();
			temp_paint.setStrokeCap(Paint.Cap.SQUARE);
			temp_paint.setStrokeJoin(Paint.Join.BEVEL);
			temp_paint.setStyle(Paint.Style.STROKE);
			temp_paint.setStrokeWidth(def_line_width);
			temp_paint.setColor(lineColor);
			temp_paint.setAntiAlias(true);

			float[] pos = GetShapeRealPoints(canvas, m_img, false);

			float width = pos[2] - pos[0];
			float height = pos[7] - pos[1];
			float temp = height / (float)areaCount;
			float x1, y1, x2, y2;
			//横向
			for(int i = 1; i < areaCount ; i ++)
			{
				x1 = pos[0];
				y1 = y2 = pos[1] + temp * i;
				x2 = pos[2];
				if(i % 4 == 0)
				{
					temp_paint.setStrokeWidth(def_rect_width);
					temp_paint.setColor(rectColor);
				}
				else
				{
					temp_paint.setStrokeWidth(def_line_width);
					temp_paint.setColor(lineColor);
				}
				canvas.save();
				canvas.drawLine(x1, y1, x2, y2, temp_paint);
				canvas.restore();
			}
			//纵向
			temp = width / areaCount;
			for(int i = 1; i < areaCount ; i ++)
			{
				y1 = pos[1];
				y2 = pos[7];
				x1 = x2 = pos[0] + temp * i;
				if(i % 4 == 0)
				{
					temp_paint.setStrokeWidth(def_rect_width);
					temp_paint.setColor(rectColor);
				}
				else
				{
					temp_paint.setStrokeWidth(def_line_width);
					temp_paint.setColor(lineColor);
				}
				canvas.save();
				canvas.drawLine(x1, y1, x2, y2, temp_paint);
				canvas.restore();
			}
		}
	}*/

	protected boolean m_isSingleTouch = true;
	protected boolean m_longClick = false;
	protected boolean m_isCancelLongClick = false;
	protected CheckForLongPress m_checkLongPress;
	//	protected CheckForFrameDismiss m_checkFrameDismiss;
	Handler m_handler = new Handler();

	private void removeLongPressCallback()
	{
		if (m_checkLongPress != null)
		{
			m_handler.removeCallbacks(m_checkLongPress);
			m_checkLongPress = null;
		}
	}

	private void checkForLongPressClick()
	{
		if(m_longPressEnabled)
		{
			if (m_checkLongPress == null)
			{
				m_checkLongPress = new CheckForLongPress();
			}
			m_handler.postDelayed(m_checkLongPress, 500);
		}
	}

	@Override
	protected void EvenDown(MotionEvent event)
	{
		if(m_absorbing){
			return;
		}
		m_isSingleTouch = false;
		m_isCancelLongClick = true;
		removeLongPressCallback();

		super.EvenDown(event);

		if(m_operate == OPERATE_FREE && m_target instanceof  ShapeEx5)
		{
			((ShapeEx5)m_target).GetSelectIndex(m_downX, m_downY);
			m_target = ((ShapeEx5)m_target).GetCurModule();
		}
		if(m_target instanceof ShapeEx5)
		{
			Init_MRZ_Data5(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
			this.invalidate();
		}
		else if(m_target instanceof ShapeEx3)
		{
			Init_MRZ_Data3(m_target, m_downX1, m_downY1, m_downX2, m_downY2);
			this.invalidate();
		}
	}

	@Override
	protected void EvenUp(MotionEvent event)
	{
		m_isSingleTouch = true;
		m_isCancelLongClick = true;
		removeLongPressCallback();
		super.EvenUp(event);
	}

	@Override
	protected void EvenMove(MotionEvent event)
	{
		if(m_absorbing){
			OddMove(event);
			return;
		}
		m_isCancelLongClick = true;
		removeLongPressCallback();
		if(m_isTouch && m_target != null && m_operateMode == MODE_PENDANT)
		{
			if(m_target instanceof ShapeEx5)
			{
				Run_MRZ5(m_target, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
				this.invalidate();
				return;
			}
			else if(m_target instanceof ShapeEx3)
			{
				Run_MRZ3(m_target, event.getX(0), event.getY(0), event.getX(1), event.getY(1));
			}
		}
		super.EvenMove(event);

	}

	@Override
	protected void OddMove(MotionEvent event)
	{
		if(m_absorbing && m_absorbBmp != null)
		{
			m_showAbsorbIcon = true;
			Run_M(m_absorbIcon, event.getX(), event.getY());

			m_curColor = GetCurAbsorbColor();
			UpdateColor(m_curColor);
			return;
		}

		float upX = event.getX();
		float upY = event.getY();
		float dx = Math.abs(upX - m_downX);
		float dy = Math.abs(upY - m_downY);
		if (dx > 20 || dy > 20)
		{
			// 移动超过阈值，则表示移动了
			m_isCancelLongClick = true;
			removeLongPressCallback();
		}

		if(m_isTouch && m_target != null && m_operateMode == MODE_PENDANT)
		{
			if(m_target instanceof ShapeEx5 || m_target instanceof ShapeEx3)
			{
				if(m_isOddCtrl && m_oddCtrlType == CTRL_R_Z)
				{
					Run_RZ35(m_target, temp_showCX, temp_showCY, event.getX(), event.getY());
				}
				else
				{
					if(m_target instanceof ShapeEx3)
					{
						Run_M3(m_target, event.getX(), event.getY());
					}
					else if(m_target instanceof ShapeEx5)
					{
						Run_M5(m_target, event.getX(), event.getY());
					}
				}
				//更新界面
				this.invalidate();
				return;
			}
		}

		super.OddMove(event);
	}

	@Override
	protected void OddDown(MotionEvent event)
	{
//		m_drawSpaceLine = false;
		if(m_absorbing)
		{
			m_isTouch = true;
			m_showAbsorbIcon = true;
			m_absorbBmp = Bitmap.createBitmap(m_origin.m_w, m_origin.m_h, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(m_absorbBmp);
			m_isCompare = true;
			DrawToCanvas(canvas, m_operateMode);
			m_isCompare = false;

			m_absorbIcon.m_x = m_downX - m_absorbIcon.m_centerX - 50;
			m_absorbIcon.m_y = m_downY - m_absorbIcon.m_centerY - 50;
			Init_M_Data(m_absorbIcon, m_downX, m_downY);

			m_lastColor = GetCurShowColor();
			m_curColor = GetCurAbsorbColor();
			UpdateColor(m_curColor);

			return;
		}

		m_isTouch = true;
		m_isCancelLongClick = false;
		m_showAllChoose = false;
		checkForLongPressClick();
		if (m_operateMode == MODE_FRAME)
		{
			if (m_frame == null)
			{
				m_target = null;
				return;
			}
		}

		switch (m_operateMode)
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
				if (m_pendantCurSel >= 0)
				{
					//判断是否选中旋转放大按钮
					if (m_rotationBtn != null && IsClickBtn(m_rotationBtn, m_downX, m_downY))
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_target = m_pendantArr.get(m_pendantCurSel);
						m_isOddCtrl = true;
						m_oddCtrlType = CTRL_R_Z;
						if(m_operate == OPERATE_FREE && m_target instanceof ShapeEx5)
						{
							m_target = ((ShapeEx5)m_target).GetCurModule();
						}
						if(m_target != null){
							if(!(m_target instanceof ShapeEx3) && !(m_target instanceof ShapeEx5))
							{
								float[] src = {m_target.m_x + m_target.m_centerX, m_target.m_y + m_target.m_centerY};
								float[] dst = new float[2];
								GetShowPos(dst, src);
								temp_showCX = dst[0];
								temp_showCY = dst[1];
								Init_RZ_Data(m_target, temp_showCX, temp_showCY, m_downX, m_downY);
							}
							else{
//								m_drawSpaceLine = true;
//								startSpaceLineAnim(0, 255);
								Canvas canvas = new Canvas();
								float[] points = GetShapeRealPoints(canvas, m_target, true);
								temp_showCX = points[0];
								temp_showCY = points[1];
								if(m_target instanceof ShapeEx3)
								{
									Init_RZ_Data3(m_target, temp_showCX, temp_showCY, m_downX, m_downY);
								}
								else if(m_target instanceof ShapeEx5)
								{
									Init_RZ_Data5(m_target, temp_showCX, temp_showCY, m_downX, m_downY);
								}
							}
						}
						return;
					}

					//判断是否选中非比例缩放按钮
					if (m_nzoomBtn != null && IsClickBtn(m_nzoomBtn, m_downX, m_downY))
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_target = m_pendantArr.get(m_pendantCurSel);
						m_isOddCtrl = true;
						m_oddCtrlType = CTRL_NZ;
						Init_NZ_Data(m_target, m_downX, m_downY);

						return;
					}

					//判断是否选中颜色选择按钮
					if (m_colorChooserBtn != null && IsClickBtn(m_colorChooserBtn, m_downX, m_downY) && m_colorChooserBtnVisible)
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_target = m_colorChooserBtn;
						ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
						if (m_onMotifiListener != null && temp instanceof ShapeEx5) {
							m_onMotifiListener.onChooseColor(true, ((ShapeEx5) temp).GetCurColor(), ((ShapeEx5) temp).GetCurShadowAlpha());
						}
						return;
					}

					//判断是否选中保存按钮
					if (m_saveBtn != null && IsClickBtn(m_saveBtn, m_downX, m_downY) && m_saveBtnVisible)
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_target = m_saveBtn;
						if (m_onMotifiListener != null) {
							ShapeEx tempShape = m_pendantArr.get(m_pendantCurSel);
							Object bmp = null;
							if(tempShape instanceof ShapeEx5)
							{
								bmp = GetOutputText1(tempShape);
							}
							m_onMotifiListener.onSave(m_pendantArr.get(m_pendantCurSel), bmp);
						}
						return;
					}

					if (m_divideBtn != null && IsClickBtn(m_divideBtn, m_downX, m_downY) && m_divideBtnVisible)
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						ShapeEx item = m_pendantArr.get(m_pendantCurSel);
						if(item != null && item instanceof ShapeEx5 && m_operate == OPERATE_ALL && ((ShapeEx5)item).GetModuleCount() > 1)
						{
							m_parentFlag = true;
							startFrameAnim();
							((ShapeEx5)item).reset();
							m_operate = OPERATE_FREE;
							if (m_onMotifiListener != null) {
								m_onMotifiListener.onChooseColor(false, -1, 0);
							}
							EditableTextView.this.invalidate();
						}
						if (m_onMotifiListener != null) {
							m_onMotifiListener.onDevide();
						}
						return;
					}

					if (m_mergeBtn != null && IsClickBtn(m_mergeBtn, m_downX, m_downY) && m_mergeBtnVisible)
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_operate = OPERATE_ALL;

						m_target = m_pendantArr.get(m_pendantCurSel);
						Init_M_Data5(m_target, m_downX, m_downY);
						m_showAllChoose = true;
						EditableTextView.this.invalidate();
						if (m_onMotifiListener != null) {
							m_onMotifiListener.onDevide();
						}
						if (m_onMotifiListener != null) {
							m_onMotifiListener.onChooseColor(true, ((ShapeEx5) m_target).GetCurColor(), ((ShapeEx5) m_target).GetCurShadowAlpha());
						}

						if (m_onMotifiListener != null) {
							m_onMotifiListener.onMerge();
						}
						m_cb.SelectPendant(m_pendantCurSel);
						return;
					}

					//判断是否选中删除按钮
					if (m_deleteBtn != null && IsClickBtn(m_deleteBtn, m_downX, m_downY) && m_deleteBtnVisible)
					{
						m_isCancelLongClick = true;
						removeLongPressCallback();

						m_target = m_deleteBtn;
						if(m_operate == OPERATE_FREE)
						{
							ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
							if(temp instanceof ShapeEx5)
							{
								((ShapeEx5)temp).delete();
								if(((ShapeEx5)temp).GetModuleCount() == 0)
								{
									if (m_onMotifiListener != null) {
										m_onMotifiListener.onDelete(m_pendantArr.get(m_pendantCurSel), m_pendantCurSel);
									}
								}
								else {
									this.invalidate();
								}
							}
						}
						else
						{
							if (m_onMotifiListener != null) {
								m_onMotifiListener.onDelete(m_pendantArr.get(m_pendantCurSel), m_pendantCurSel);
							}
						}
						return;
					}
				}

				int index = GetSelectIndex(m_pendantArr, m_downX, m_downY);
				if (index >= 0)
				{
					boolean flag = false;
					boolean onlyCurModule = false;
					if (index != m_pendantCurSel)
					{
						flag = true;
					}
					m_target = m_pendantArr.get(index);
					m_pendantArr.remove(index);
					m_pendantArr.add(m_target);

					if(m_target instanceof ShapeEx5)
					{
//						m_drawSpaceLine = true;
//						startSpaceLineAnim(0, 255);
						ShapeEx5 temp = ((ShapeEx5)m_target);
						temp.ResetSelFontIndex();
						if(m_operate == OPERATE_FREE)
						{
							onlyCurModule = true;
							int index1 = ((ShapeEx5)m_target).GetSelectIndex(m_downX, m_downY);
							if(index1 >= 0)
							{
								if(index1 != ((ShapeEx5)m_target).GetCurModuleIndex())
								{
									flag = true;
								}
							}
							((ShapeEx5)m_target).setCurModuleIndex(index1);
							m_target = ((ShapeEx5)m_target).GetModule(index1);
						}
						if(flag){
							temp.SetAnimCurCount(false, onlyCurModule);
							startAnim(temp);
						}else {
							temp.SetAnimCurCount(true, onlyCurModule);
						}
					}

					m_pendantCurSel = m_pendantArr.size() - 1;
					m_isOddCtrl = false;
					if(m_target != null)
					{
						if(m_target instanceof ShapeEx3)
						{
							Init_M_Data3(m_target, m_downX, m_downY);
							if (m_onMotifiListener != null) {
								m_onMotifiListener.onChooseColor(true, ((ShapeEx3) m_target).GetCurColor(), ((ShapeEx3) m_target).GetCurShadowAlpha());
							}
						}
						else if(m_target instanceof ShapeEx5)
						{
							Init_M_Data5(m_target, m_downX, m_downY);
							if (m_onMotifiListener != null) {
								m_onMotifiListener.onChooseColor(true, ((ShapeEx5) m_target).GetCurColor(), ((ShapeEx5) m_target).GetCurShadowAlpha());
							}
						}
						else
						{
							if (m_onMotifiListener != null) {
								m_onMotifiListener.onChooseColor(false, -1, 0);
							}
							Init_M_Data(m_target, m_downX, m_downY);
						}

						//通知主界面选中信息
						m_cb.SelectPendant(m_pendantCurSel);

						//更新界面
						this.invalidate();
					}
				}
				else
				{
					if (m_pendantCurSel >= 0)
					{
						m_pendantCurSel = -1;
						//通知主界面选中信息
						m_cb.SelectPendant(m_pendantCurSel);

						//更新界面
						this.invalidate();
					}
					if (m_onMotifiListener != null) {
						m_onMotifiListener.onChooseColor(false, -1, 0);
					}
					m_isOddCtrl = false;
					m_target = null;
				}

				if (m_cb instanceof ControlCallback) {
					((ControlCallback) m_cb).TouchImage(true);
				}
				break;
			}

			case MODE_IMAGE:
			default:
				m_target = null;
				break;
		}

		if (m_operateMode == MODE_IMAGE && m_cb instanceof ControlCallback) {
			((ControlCallback) m_cb).TouchImage(true);
		}
	}

	public int GetCurShowColor()
	{
		int color = 0;
		if (m_pendantCurSel >= 0 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx temp = m_pendantArr.get(m_pendantCurSel);
			if(temp instanceof ShapeEx5)
			{
				if(m_operate == OPERATE_FREE)
				{
					temp = ((ShapeEx5)temp).GetCurModule();
					if(temp instanceof ShapeEx3)
					{
						color = ((ShapeEx3)temp).GetCurColor();
					}
				}
				else
				{
					color = ((ShapeEx5)temp).GetCurColor();
				}
			}
		}
		return color;
	}

	protected int GetCurAbsorbColor()
	{
		int color = m_curColor;
		if(m_absorbBmp != null && m_absorbIcon != null)
		{
			int x = (int)(m_absorbIcon.m_x + m_absorbIcon.m_centerX);
			int y = (int)(m_absorbIcon.m_y + m_absorbIcon.m_centerY);
			int left = 0, right = m_absorbBmp.getWidth(), top = 0, bottom = m_absorbBmp.getHeight();
			if(m_img != null)
			{
				float[] src = {m_img.m_x + m_img.m_centerX, m_img.m_y + m_img.m_centerY};
				float[] dst = new float[2];
				GetShowPos(dst, src);
				int left1 = (int)(dst[0] - m_img.m_scaleX * m_img.m_w / 2);
				int right1 = (int)(dst[0] + m_img.m_scaleX * m_img.m_w / 2);
				int top1 = (int)(dst[1] - m_img.m_scaleY * m_img.m_h / 2);
				int bottom1 = (int)(dst[1] + m_img.m_scaleY * m_img.m_h / 2);
				left = Math.max(left1, left);
				right = Math.min(right1, right);
				top = Math.max(top, top1);
				bottom = Math.min(bottom1, bottom);
			}
			if(x >= left && x < right && y >= top && y < bottom)
			{
				color = m_absorbBmp.getPixel(x, y);
			}
		}
		return color;
	}

	protected void GetLogicPoints(ShapeEx5 item, float[] dst, float[] src)
	{
		if(item != null)
		{
			Canvas canvas = new Canvas();
			canvas.save();
			canvas.concat(item.m_matrix);
			canvas.getMatrix(temp_matrix);
			canvas.restore();

			Matrix matrix = new Matrix();
			temp_matrix.invert(matrix);
			matrix.mapPoints(dst, src);
		}
	}

	@Override
	protected int GetSelectIndex(ArrayList<? extends ShapeEx> arr, float x, float y)
	{
		int index = -1;

		ShapeEx item;
		float[] values = new float[9];

		int len = arr.size();
		for(int i = len - 1; i >= 0; i--)
		{
			item = arr.get(i);
			if(item instanceof ShapeEx5)
			{
				temp_src = ((ShapeEx5)item).GetRect();
				Canvas canvas = new Canvas();
				canvas.save();
				canvas.concat(item.m_matrix);
				canvas.getMatrix(temp_matrix);
				canvas.restore();
				if(Utils.isSelected(temp_matrix, temp_src, x, y))
				{
					index = i;
					break;
				}
			}
			else
			{
				GetShowMatrix(item.m_matrix, item);
				item.m_matrix.getValues(values);

				if(ProcessorV2.IsSelectTarget(values, item.m_w, item.m_h, x, y))
				{
					index = i;
					break;
				}
			}
		}
		return index;
	}

	@Override
	protected void OddUp(MotionEvent event)
	{
//		if(m_drawSpaceLine)
//		{
//			startSpaceLineAnim(255, 0);
//		}
		if(m_absorbing)
		{
			m_isTouch = false;
			m_absorbBmp = null;
			m_showAbsorbIcon = false;
			UpdateUI();
			return;
		}

		super.OddUp(event);
		removeLongPressCallback();
		float upX = event.getX();
		float upY = event.getY();
		float dx = Math.abs(upX - m_downX);
		float dy = Math.abs(upY - m_downY);

		if (m_operateMode == MODE_PENDANT && m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
			AdjustShape(shape);
//			LineAbsorb(shape);

			UpdateUI();
		}

		if (m_operateMode == MODE_PENDANT && dx < 20 && dy < 20 && m_isSingleTouch == true && m_lastSelPendant == m_pendantCurSel)
		{
			if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
			{
				ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
				if (shape != null && shape instanceof ShapeEx5)
				{
					boolean selOnly = m_operate == OPERATE_FREE ? true : false;
					FontInfo font = ((ShapeEx5)shape).GetCurSelFont(upX, upY, selOnly);
					if(font != null)
					{
						if(selOnly && m_lastSelTextItem != ((ShapeEx5)shape).GetCurSelModule())
						{
							m_lastSelTextItem = ((ShapeEx5)shape).GetCurSelModule();
						}
						else
						{
							((ShapeEx5)shape).cancelAnim();
							((ShapeEx5)shape).SetAnimCurCount(true, true);
							cancelAnim();
							String text = font.m_showText;
							if (m_onMotifiListener != null)
							{
								m_onMotifiListener.onClick(text, upY);
							}
						}
					}
				}
			}
		}
		m_lastSelPendant = m_pendantCurSel;
	}

	protected void AdjustShape(ShapeEx shape)
	{
		if(shape == null)
			return;
		float flag = 0;
		ShapeEx item = null;
		if(m_operate == OPERATE_FREE && shape instanceof ShapeEx5)
		{
			item = shape;
			shape = ((ShapeEx5)shape).GetCurModule();

			if(shape == null)
				return;

			flag = shape.m_degree + ((ShapeEx5)item).m_degree;
			if (Math.abs(flag) < 7)
			{
				shape.m_degree = -((ShapeEx5)item).m_degree;
			}
			else
			{
				flag = shape.m_degree + ((ShapeEx5)item).m_degree - 360;
				if (Math.abs(flag) < 7)
				{
					shape.m_degree = 360 - (((ShapeEx5)item).m_degree - 360);
				}
			}
		}
		else
		{
			flag = shape.m_degree;
			if (Math.abs(flag) < 7)
			{
				shape.m_degree = 0;
			}
			else
			{
				flag = shape.m_degree - 360;
				if (Math.abs(flag) < 7)
				{
					shape.m_degree = 360;
				}
			}
		}

//		System.out.println("degree: " + shape.m_degree);
//		System.out.println("shape: " + shape);

		Canvas canvas = new Canvas();
		float[] points = GetShapeRealPoints(canvas, shape, false);
		float centerX = (points[0] + points[4]) / 2;
		float centerY = (points[1] + points[5]) / 2;

		float[] src = new float[]{centerX, centerY};
		float[] dst = new float[]{src[0], src[1]};
		if(shape instanceof ShapeEx3 && item != null)
		{
			GetLogicPoints((ShapeEx5)item, dst, src);
		}

		if((shape instanceof ShapeEx5 || shape instanceof ShapeEx3) && Math.abs(flag) < 7)
		{
			shape.m_matrix.postRotate(-flag, dst[0], dst[1]);
		}

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
		if(shape instanceof ShapeEx3 && item != null)
		{
			GetLogicPoints((ShapeEx5)item, tran1, tran1);
		}
		float[] src2 = new float[]{minX, minY, maxX, maxY};
		float[] tran2 = new float[]{minX, 0, maxX, 0, 0, minY, 0, maxY};
		if(shape instanceof ShapeEx3 && item != null)
		{
			GetLogicPoints((ShapeEx5)item, tran2, tran2);
		}
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

	/**
	 * 中线吸附
	 */
	private void LineAbsorb(ShapeEx shape)
	{
		if(m_operate == OPERATE_FREE)
			return;
		if(shape == null && !(shape instanceof ShapeEx5))
			return;
		if(m_operate == OPERATE_FREE && shape instanceof ShapeEx5)
		{
			shape = ((ShapeEx5)shape).GetCurModule();
		}
		Canvas canvas = new Canvas();
		float[] points = GetShapeRealPoints(canvas, shape, false);
		float centerX = (points[0] + points[4]) / 2;
		float centerY = (points[1] + points[5]) / 2;

		float originCenterX = m_origin.m_x + m_origin.m_centerX * m_origin.m_scaleX;
		float originCenterY = m_origin.m_y + m_origin.m_centerY * m_origin.m_scaleY;

		float tranX = originCenterX - centerX;
		float tranY = originCenterY - centerY;
		if(Math.abs(tranX) >= 30)
		{
			tranX = 0;
		}
		if(Math.abs(tranY) >= 30)
		{
			tranY = 0;
		}
		if(tranX != 0 || tranY != 0)
		{
			shape.m_matrix.postTranslate(tranX, tranY);
		}

	}

	protected PointF getPendantPoints(MyTextInfo info, ShapeEx item)
	{
		String align = info.align;
		int width = item.m_w;
		int height = item.m_h;
		float viewWidth = m_viewport.m_w * m_viewport.m_scaleX;
		float viewHeight = m_viewport.m_h * m_viewport.m_scaleY;
		if (info.m_editable)
		{
			if ((((width / 2) >= (viewWidth - 1)) || ((height / 2) >= viewHeight - 1)))
			{
				item.m_scaleX = item.m_scaleY = 1 * ShareData.m_screenWidth / 720f;
			}
			else
			{
				item.m_scaleX = item.m_scaleY = 1;
			}
		}
		else
		{
			item.m_scaleX = item.m_scaleY = 0.84f * ShareData.m_screenWidth / 720f;
		}
		float[] dst = new float[2];
		dst[0] = m_viewport.m_x + m_viewport.m_centerX - m_viewport.m_centerX * m_viewport.m_scaleX;
		dst[1] = m_viewport.m_y + m_viewport.m_centerY - m_viewport.m_centerY * m_viewport.m_scaleY;
		float[] offsets = new float[]{info.offsetX, info.offsetY};
		float[] viewSizes = new float[]{viewWidth, viewHeight};
		int[] bmpSizes = new int[]{width, height};
		PointF point;
		if(info.m_editable)
		{
			point = getEditablePoints(dst, offsets, align, viewSizes, bmpSizes, item);
		}
		else
		{
			point = getUnEditablePoints(dst, offsets, align, viewSizes, bmpSizes, item);
		}

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

	protected PointF getUnEditablePoints(float[] dst, float[] offsets, String align, float[] viewSize, int[] bmpSize, ShapeEx item)
	{
		PointF point = new PointF();
		float originX = dst[0];
		float originY = dst[1];
		float width = bmpSize[0] * item.m_scaleX;
		float height = bmpSize[1] * item.m_scaleY;
		float viewWidth = viewSize[0];
		float viewHeight = viewSize[1];
		float offsetX = offsets[0] / 100f * viewWidth;
		float offsetY = offsets[1] / 100f * viewHeight;
		float dx = item.m_centerX * item.m_scaleX - item.m_centerX;
		float dy = item.m_centerY * item.m_scaleY - item.m_centerY;
//		Log.v(TAG, "width: " + width + "item.m_scaleY: " + item.m_scaleY);
//		Log.v(TAG, "dx: " + dx + "dy: " + dy);
		if (align.equals("righttop"))
		{
			originX += viewWidth - width;
			point.x = originX - offsetX;
			point.y = originY + offsetY;
		}
		else if (align.equals("top"))
		{
			originX += (viewWidth - width) / 2f;
			point.x = originX + offsetX;
			point.y = originY + offsetY;
		}
		else if (align.equals("left"))
		{
			originY += (viewHeight - height) / 2f;
			point.x = originX + offsetX;
			point.y = originY + offsetY;
		}
		else if (align.equals("middle"))
		{
			originX += (viewWidth - width) / 2f;
			originY += (viewHeight - height) / 2f;
			point.x = originX + offsetX;
			point.y = originY + offsetY;
		}
		else if (align.equals("right"))
		{
			originX += viewWidth - width;
			originY += (viewHeight - height) / 2f;
			point.x = originX - offsetX;
			point.y = originY + offsetY;
		}
		else if (align.equals("leftbottom"))
		{
			originY += viewHeight - height;
			point.x = originX + offsetX;
			point.y = originY - offsetY;
		}
		else if (align.equals("bottom"))
		{
			originX += (viewWidth - width) / 2f;
			originY += viewHeight - height;
			point.x = originX + offsetX;
			point.y = originY - offsetY;
		}
		else if (align.equals("rightbottom"))
		{
			originX += viewWidth - width;
			originY += viewHeight - height;
			point.x = originX - offsetX;
			point.y = originY - offsetY;
		}
		else
		{
			point.x = originX + offsetX;
			point.y = originY + offsetY;
		}
		point.x = point.x + dx;
		point.y = point.y + dy;
		return point;
	}

	public void SetCompareFlag(boolean flag)
	{
		m_isCompare = flag;
	}

	public ShapeEx DelPendantByIndex(int index)
	{
		ShapeEx out = null;

		if (index >= 0 && index < m_pendantArr.size())
		{
			out = m_pendantArr.remove(index);
			if (out.m_bmp != null)
			{
				out.m_bmp.recycle();
				out.m_bmp = null;
			}

			m_pendantCurSel = m_pendantArr.size() - 1;
			m_cb.SelectPendant(m_pendantCurSel);
		}
		return out;
	}

	public int GetPendantLen()
	{
		return m_pendantArr.size();
	}

	@Override
	public void DelAllPendant()
	{
		if(m_pendantArr != null)
		{
			ShapeEx temp;
			int size = m_pendantArr.size();
			for(int i = 0; i < size; i ++)
			{
				temp = m_pendantArr.remove(i);
				if (temp.m_bmp != null)
				{
					temp.m_bmp.recycle();
					temp.m_bmp = null;
				}
			}
			m_pendantCurSel = -1;
			m_cb.SelectPendant(m_pendantCurSel);
		}
	}

	public int GetCurPendantIndex()
	{
		return m_pendantCurSel;
	}

	public ShapeEx GetPendantByIndex(int index)
	{
		ShapeEx out = null;

		if (index >= 0 && index < m_pendantArr.size())
		{
			out = m_pendantArr.get(index);
		}
		return out;
	}

	@Override
	public int AddPendant(Object info, Bitmap bmp)
	{
		m_operate = OPERATE_ALL;
		if(GetPendantIdleNum() <= 0)
			return -1;
		if (info != null && info instanceof MyTextInfo)
		{
			MyTextInfo text = (MyTextInfo) info;
			//可编辑
			if(text.m_editable)
			{
				ShapeEx5 item;
				if(temp_shape != null && temp_shape instanceof ShapeEx5)
				{
					item = (ShapeEx5)temp_shape;
					temp_shape = null;
				}
				else
				{
					item = new ShapeEx5(getContext());
					item.m_ex = text;
					item.SetData(text);
					item.InitShapeInfo();
					item.UpdateAlpha(text.m_alpha, false);
					if(text.m_matrixValue != null && text.m_matrixValue.length == 9)
					{
						item.MAX_SCALE = 20f;
						item.MIN_SCALE = 0.5f;
						item.m_matrix.setValues(text.m_matrixValue);
						AdjustShape(item);
					}
					else
					{
						item.UpdateChildMatrix();
						PointF point = getPendantPoints(text, item);
						item.m_x = point.x;
						item.m_y = point.y;
						item.DEF_SCALE = item.m_scaleX;
						{
							item.MAX_SCALE = 20f;

							item.MIN_SCALE = 0.5f;
						}
						GetShowMatrix(item.m_matrix, item);
					}
				}
				m_pendantArr.add(item);
				return m_pendantArr.size() - 1;
			}
			else
			{
				ShapeEx item = new ShapeEx();
				Bitmap item_bmp;
				if(bmp != null)
				{
					item_bmp = bmp;
				}
				else
				{
					item_bmp = m_cb.MakeShowPendant(info, m_origin.m_w, m_origin.m_h);
				}
				if(item_bmp == null)
					return -1;
				if(temp_shape != null)
				{
					item.Set(temp_shape);
					item.m_bmp = item_bmp;
					temp_shape = null;
				}
				else
				{
					item.m_bmp = item_bmp;
					item.m_w = item.m_bmp.getWidth();
					item.m_h = item.m_bmp.getHeight();
					item.m_centerX = (float)item.m_w / 2f;
					item.m_centerY = (float)item.m_h / 2f;
					PointF point = getPendantPoints(text, item);
					item.m_x = point.x;
					item.m_y = point.y;
					item.m_ex = info;

					//控制缩放比例
					item.DEF_SCALE = item.m_scaleX;
					{
						float scale1 = (float)m_origin.m_w * def_pendant_max_scale / (float)item.m_w;
						float scale2 = (float)m_origin.m_h * def_pendant_max_scale / (float)item.m_h;
						item.MAX_SCALE = (scale1 > scale2) ? scale2 : scale1;

						scale1 = (float)m_origin.m_w * def_pendant_min_scale / (float)item.m_w;
						scale2 = (float)m_origin.m_h * def_pendant_min_scale / (float)item.m_h;
						item.MIN_SCALE = (scale1 > scale2) ? scale2 : scale1;
					}
					GetShowMatrix(item.m_matrix, item);
				}

				m_pendantArr.add(item);
				return m_pendantArr.size() - 1;
			}
		}
		else if (info instanceof MyLogoRes)
		{
			ShapeEx4 item = new ShapeEx4();
			if (((MyLogoRes) info).m_name == null || ((MyLogoRes) info).m_name.equals(""))
			{
				item.m_isLocal = false;
			}
			if (bmp != null)
			{
				item.m_bmp = bmp;
			}
			else
			{
				item.m_bmp = m_cb.MakeShowPendant(info, m_origin.m_w, m_origin.m_h);
			}
			if (item.m_bmp != null)
			{
				item.m_w = item.m_bmp.getWidth();
				item.m_h = item.m_bmp.getHeight();
			}
			item.m_centerX = (float) item.m_w / 2f;
			item.m_centerY = (float) item.m_h / 2f;
			item.m_x = (float) m_origin.m_w / 2f - item.m_centerX;
			item.m_y = (float) m_origin.m_h / 2f - item.m_centerY;
			item.m_scaleX = item.m_scaleY = ((MyLogoRes)info).m_scale;
			item.m_ex = info;

			//控制缩放比例
			item.DEF_SCALE = item.m_scaleX;
			{
				float scale1 = (float) m_origin.m_w * def_pendant_max_scale / (float) item.m_w;
				float scale2 = (float) m_origin.m_h * def_pendant_max_scale / (float) item.m_h;
				item.MAX_SCALE = (scale1 > scale2) ? scale2 : scale1;

				scale1 = (float) m_origin.m_w * def_pendant_min_scale / (float) item.m_w;
				scale2 = (float) m_origin.m_h * def_pendant_min_scale / (float) item.m_h;
				item.MIN_SCALE = (scale1 > scale2) ? scale2 : scale1;
			}
			GetShowMatrix(item.m_matrix, item);
			m_pendantArr.add(item);
			return m_pendantArr.size() - 1;
		}
		return super.AddPendant(info, bmp);
	}

	private ShapeEx temp_shape; //返回编辑用
	public int AddPendant3(ShapeEx item)
	{
		if (item.m_bmp == null || item.m_bmp.isRecycled())
		{
			temp_shape = item;
			return AddPendant(item.m_ex, null);
		}
		else
		{
			if (GetPendantIdleNum() > 0)
			{
				m_pendantArr.add(item);

				return m_pendantArr.size() - 1;
			}
		}
		return -1;
	}

	public void UpdateText(String text)
	{
		if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
			if(shape != null && shape instanceof ShapeEx5)
			{
				((ShapeEx5)shape).UpdateText(text);
			}
		}
		UpdateUI();
	}

	public int UpdateColor(int color)
	{
		int out = -1;
		if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
			boolean selOnly = m_operate == OPERATE_FREE ? true : false;
			if(shape != null && shape instanceof ShapeEx5)
			{
				out = ((ShapeEx5)shape).UpdateColor(color, selOnly);
			}
		}
		UpdateUI();
		return out;
	}

	/**
	 * 0-255
	 * @param alpha
	 */
	public int SetAlpha(int alpha)
	{
		int color = -1;
		if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
			boolean selOnly = m_operate == OPERATE_FREE ? true : false;
			if(shape != null && shape instanceof ShapeEx5)
			{
				color = ((ShapeEx5)shape).UpdateAlpha(alpha, selOnly);
			}
		}
		UpdateUI();
		return color;
	}

	/**
	 * 0-255
	 * @param alpha
	 */
	public int SetShadowAlpha(int alpha)
	{
		int outAlpha = 0xff;
		if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);
			boolean selOnly = m_operate == OPERATE_FREE ? true : false;
			if(shape != null && shape instanceof ShapeEx5)
			{
				outAlpha = ((ShapeEx5)shape).UpdateShadowAlpha(alpha, selOnly);
			}
		}
		UpdateUI();
		return outAlpha;
	}

	public void ResetModuleOperate()
	{
		if (m_pendantCurSel != -1 && m_pendantCurSel < m_pendantArr.size())
		{
			ShapeEx shape = m_pendantArr.get(m_pendantCurSel);

			if(shape != null && shape instanceof ShapeEx5)
			{
				((ShapeEx5)shape).ResetModuleOperate();
			}
		}
		UpdateUI();
	}

	@Override
	public Bitmap GetOutputBmp(int size)
	{
		float whscale = (float)m_viewport.m_w / (float)m_viewport.m_h;
		float outW = size;
		float outH = outW / whscale;
		if(outH > size)
		{
			outH = size;
			outW = outH * whscale;
		}
		ShapeEx backup = (ShapeEx)m_origin.Clone();

		//设置输出位置
		m_origin.m_scaleX = outW / (float)m_viewport.m_w / m_viewport.m_scaleX;
		m_origin.m_scaleY = m_origin.m_scaleX;
		m_origin.m_x = (int)outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
		m_origin.m_y = (int)outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;

		float tempW;
		float tempH;
		Bitmap imgBmp = null;
		if(m_img != null)
		{
			tempW = m_origin.m_scaleX * m_img.m_scaleX * m_img.m_w;
			tempH = m_origin.m_scaleY * m_img.m_scaleY * m_img.m_h;
			imgBmp = m_cb.MakeOutputImg(m_img.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
			if(imgBmp != null && imgBmp.getWidth() > 0 && imgBmp.getHeight() > 0)
			{
				if(Math.max(tempW, tempH) > Math.max(imgBmp.getWidth(), imgBmp.getHeight()))
				{
					//修正(图片小于输出size)
					m_origin.m_scaleX = (float)imgBmp.getWidth() / (m_img.m_scaleX * m_img.m_w);
					m_origin.m_scaleY = m_origin.m_scaleX;
					outW = imgBmp.getWidth();
					outH = imgBmp.getHeight();
					/*outW = m_origin.m_scaleX * (float)m_viewport.m_w * m_viewport.m_scaleX;
					outH = outW / whscale;*/
					m_origin.m_x = (int)outW / 2f - (m_viewport.m_x + m_viewport.m_centerX - m_origin.m_centerX) * m_origin.m_scaleX - m_origin.m_centerX;
					m_origin.m_y = (int)outH / 2f - (m_viewport.m_y + m_viewport.m_centerY - m_origin.m_centerY) * m_origin.m_scaleY - m_origin.m_centerY;
				}
			}
			else
			{
				imgBmp = null;
			}
		}

		Bitmap outBmp = Bitmap.createBitmap((int)outW, (int)outH, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(outBmp);
		canvas.setDrawFilter(temp_filter);

		Bitmap tempBmp;
		canvas.drawColor(m_bkColor);
		if(m_bk != null)
		{
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			tempBmp = m_cb.MakeOutputBK(m_bk.m_ex, (int)outW, (int)outH);
			BitmapShader shader = new BitmapShader(tempBmp, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
			temp_paint.setShader(shader);
			canvas.drawRect(0, 0, outW, outH, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_img != null && imgBmp != null)
		{
			tempBmp = imgBmp;
			imgBmp = null;
			GetOutputMatrix(temp_matrix, m_img, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		if(m_frame != null)
		{
			tempW = m_origin.m_scaleX * m_frame.m_scaleX * m_frame.m_w;
			tempH = m_origin.m_scaleY * m_frame.m_scaleY * m_frame.m_h;
			tempBmp = m_cb.MakeOutputFrame(m_frame.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
			GetOutputMatrix(temp_matrix, m_frame, tempBmp);
			temp_paint.reset();
			temp_paint.setAntiAlias(true);
			temp_paint.setFilterBitmap(true);
			canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
			tempBmp.recycle();
			tempBmp = null;
		}

		int len = m_pendantArr.size();
		ShapeEx temp;
		for(int i = 0; i < len; i++)
		{
			temp = m_pendantArr.get(i);
			if(temp instanceof ShapeEx5)
			{
				temp_matrix.set(temp.m_matrix);
				Canvas canvas1 = new Canvas();
				float[] points = GetShapeRealPoints(canvas1, temp, false);
				float[] src = {(points[0] + points[4]) / 2f, (points[1] + points[5]) / 2f};
				float[] dst = new float[2];
				GetShowPos(dst, src);

				temp_matrix.postTranslate(dst[0] - src[0], dst[1] - src[1]);
				temp_matrix.postScale(m_origin.m_scaleX / backup.m_scaleX, m_origin.m_scaleY / backup.m_scaleY, dst[0], dst[1]);

				DrawOutputItem2(canvas, (ShapeEx5)temp, temp_matrix);
			}
			else
			{
				tempW = m_origin.m_scaleX * temp.m_scaleX * temp.m_w;
				tempH = m_origin.m_scaleY * temp.m_scaleY * temp.m_h;
				tempBmp = m_cb.MakeOutputPendant(temp.m_ex, (int)(tempW + 0.5), (int)(tempH + 0.5));
				GetOutputMatrix(temp_matrix, temp, tempBmp);
				temp_paint.reset();
				temp_paint.setAntiAlias(true);
				temp_paint.setFilterBitmap(true);
				canvas.drawBitmap(tempBmp, temp_matrix, temp_paint);
				tempBmp.recycle();
				tempBmp = null;
			}
		}

		m_origin.Set(backup);

		return outBmp;
	}

	/**
	 *
	 * @param shapeEx	仅生成Bitmap
	 * @return
	 */
	public Bitmap GetOutputText(ShapeEx shapeEx)
	{
		Bitmap out = null;
		if(shapeEx != null && shapeEx instanceof ShapeEx5)
		{
			Canvas canvas = new Canvas();
			if(m_operate != OPERATE_FREE)
			{
				float[] src = GetShapeRealPoints(canvas, shapeEx, false);
				float[] dst = src;
				Utils.RoundPoint(dst, src);
				RectF rect = new RectF(dst[0], dst[1], dst[4], dst[5]);
				out = Bitmap.createBitmap((int)(rect.width() + 0.5f), (int)(rect.height() + 0.5f), Bitmap.Config.ARGB_8888);
				Canvas canvas1 = new Canvas(out);

				canvas.save();
				canvas.concat(shapeEx.m_matrix);
				canvas.getMatrix(temp_matrix);
				canvas.restore();

				temp_matrix.postTranslate(-dst[0], -dst[1]);

				DrawOutputItem2(canvas1, (ShapeEx5)shapeEx, temp_matrix);
			}
			else
			{
				ShapeEx3 shape = ((ShapeEx5)shapeEx).GetCurModule();
				float[] src = GetShapeRealPoints(canvas, shape, false);
				float[] dst = src;
				Utils.RoundPoint(dst, src);
				RectF rect = new RectF(dst[0], dst[1], dst[4], dst[5]);
				out = Bitmap.createBitmap((int)(rect.width() + 0.5f), (int)(rect.height() + 0.5f), Bitmap.Config.ARGB_8888);
				Canvas canvas1 = new Canvas(out);

				if(shape != null)
				{
					canvas.save();
					canvas.concat(shapeEx.m_matrix);
					canvas.concat(shape.m_matrix);
					canvas.getMatrix(temp_matrix);
					canvas.restore();
					temp_matrix.postTranslate(-dst[0], -dst[1]);

					canvas1.save();
					canvas1.concat(temp_matrix);
					shape.draw(canvas1);
					canvas1.restore();
				}

			}
		}

		return  out;
	}

	/**
	 *
	 * @param shapeEx	生成可编辑的json
	 * @return
	 */
	public JSONObject GetOutputText1(ShapeEx shapeEx)
	{
		JSONObject out = null;
		if(shapeEx != null && shapeEx instanceof ShapeEx5)
		{
			try
			{
				out = new JSONObject();
				JSONArray ts = new JSONArray();
				out.put("ts", ts);
				if(m_operate != OPERATE_FREE)
				{
					int count = ((ShapeEx5)shapeEx).GetModuleCount();
					for(int i = 0; i < count; i ++)
					{
						ShapeEx3 item = ((ShapeEx5)shapeEx).GetModule(i);
						MakeTextItemJson(ts, item);
					}
				}
				else
				{
					ShapeEx3 item = ((ShapeEx5)shapeEx).GetCurModule();
					MakeTextItemJson(ts, item);
				}
				out.put("matrix", MakeMatrixPosStr(shapeEx.m_matrix));
				out.put("alpha", ((ShapeEx5)shapeEx).GetAlpha());
			}catch(JSONException ex)
			{
				ex.printStackTrace();
			}


		}
		return out;
	}

	protected void MakeTextItemJson(JSONArray ts, ShapeEx3 item)
	{
		if(ts != null)
		{
			try
			{
				JSONObject json;
				if(item != null && item.m_textInfo != null)
				{
					if(item.m_textInfo.m_fontsInfo != null && !item.m_textInfo.m_fontsInfo.isEmpty())
					{
						int size = item.m_textInfo.m_fontsInfo.size();
						for(int k = 0; k < size; k++)
						{
							json = new JSONObject();
							json.put("class", "文字");
							json.put("font", item.m_textInfo.m_fontsInfo.get(k).m_font);
							json.put("size", item.m_textInfo.m_fontsInfo.get(k).m_fontSize);
							json.put("wordspace", item.m_textInfo.m_fontsInfo.get(k).m_wordspace + "");
							json.put("verticalspacing", item.m_textInfo.m_fontsInfo.get(k).m_verticalspacing + "");
							int color = item.m_textInfo.m_fontsInfo.get(k).m_fontColor;
							int alpha = item.m_textInfo.m_fontsInfo.get(k).baseAlpha;
							json.put("color", Integer.toHexString(Painter.SetColorAlpha(alpha, color)));
							json.put("typeset", item.m_textInfo.m_fontsInfo.get(k).m_typeSet);
							json.put("con", item.m_textInfo.m_fontsInfo.get(k).m_con);
							json.put("cid", item.m_textInfo.m_fontsInfo.get(k).m_cid + "");
							json.put("pos", item.m_textInfo.m_fontsInfo.get(k).m_pos);
							json.put("offset_x", item.m_textInfo.m_fontsInfo.get(k).m_offsetX + "");
							json.put("offset_y", item.m_textInfo.m_fontsInfo.get(k).m_offsetY + "");
							json.put("maxNum", item.m_textInfo.m_fontsInfo.get(k).m_maxNum + "");
							json.put("maxLine", item.m_textInfo.m_fontsInfo.get(k).m_maxLine + "");
							json.put("align", item.m_textInfo.m_fontsInfo.get(k).m_align);
							JSONArray wenan = new JSONArray();
							wenan.put(item.m_textInfo.m_fontsInfo.get(k).m_showText);
							json.put("wenan", wenan);
							json.put("shadow_c", Integer.toHexString(item.m_textInfo.m_fontsInfo.get(k).m_shadowColor));
							json.put("shadow_x", item.m_textInfo.m_fontsInfo.get(k).m_shadowX);
							json.put("shadow_y", item.m_textInfo.m_fontsInfo.get(k).m_shadowY);
							json.put("shadow_r", item.m_textInfo.m_fontsInfo.get(k).m_shadowRadius);
							json.put("nc_color", Integer.toHexString(item.m_textInfo.m_fontsInfo.get(k).m_ncColor));
							json.put("matrix", MakeMatrixPosStr(item.m_matrix));
							ts.put(json);
						}
					}

					if(item.m_textInfo.m_imgInfo != null && !item.m_textInfo.m_imgInfo.isEmpty())
					{
						int size = item.m_textInfo.m_imgInfo.size();
						for(int k = 0; k < size; k++)
						{
							json = new JSONObject();
							json.put("class", "图片");
							json.put("file", item.m_textInfo.m_imgInfo.get(k).m_imgFile);
							int color = item.m_textInfo.m_imgInfo.get(k).paint_color;
							int alpha = item.m_textInfo.m_imgInfo.get(k).baseAlpha;
							json.put("color", Integer.toHexString(Painter.SetColorAlpha(alpha, color)));
							json.put("cid", item.m_textInfo.m_imgInfo.get(k).m_cid + "");
							json.put("con", item.m_textInfo.m_imgInfo.get(k).m_con);
							json.put("pos", item.m_textInfo.m_imgInfo.get(k).m_pos);
							json.put("offset_x", item.m_textInfo.m_imgInfo.get(k).m_offsetX + "");
							json.put("offset_y", item.m_textInfo.m_imgInfo.get(k).m_offsetY + "");
							json.put("shadow_c", Integer.toHexString(item.m_textInfo.m_imgInfo.get(k).m_shadowColor));
							json.put("shadow_x", item.m_textInfo.m_imgInfo.get(k).m_shadowX + "");
							json.put("shadow_y", item.m_textInfo.m_imgInfo.get(k).m_shadowY + "");
							json.put("shadow_r", item.m_textInfo.m_imgInfo.get(k).m_shadowRadius + "");
							json.put("nc_color", Integer.toHexString(item.m_textInfo.m_imgInfo.get(k).m_ncColor));
							json.put("matrix", MakeMatrixPosStr(item.m_matrix));
							ts.put(json);
						}
					}
				}
			}catch(JSONException e)
			{
				e.printStackTrace();
			}
		}
	}


	private String MakeMatrixPosStr(Matrix matrix)
	{
		String posStr = "";
		if(matrix != null)
		{
			float[] matrix_pos = new float[9];
			matrix.getValues(matrix_pos);
			for(int i = 0; i < matrix_pos.length; i ++)
			{
				if(i == matrix_pos.length - 1)
				{
					posStr += matrix_pos[i];
				}
				else
				{
					posStr += matrix_pos[i] + ",";
				}
			}
		}
		return posStr;
	}

	@Override
	public void ReleaseMem()
	{
		super.ReleaseMem();

		cancelAnim();
		m_isCancelLongClick = true;
		removeLongPressCallback();
		m_onMotifiListener = null;
		cancelFrameAnim();
//		stopSpaceLineAnim();
	}

	public void setOnMotifyListener(OnMotifyListener lis)
	{
		m_onMotifiListener = lis;
	}

	private class CheckForLongPress implements Runnable
	{
		@Override
		public void run()
		{
			if(m_isCancelLongClick == false)
			{
				m_longClick = true;
			}
			if(m_longClick)
			{
				ShapeEx item = GetCurrentSelPendantItem();
				if(item != null && item instanceof ShapeEx5 && m_operate == OPERATE_ALL && ((ShapeEx5)item).GetModuleCount() > 1)
				{
					int index = ((ShapeEx5)item).GetSelectIndex(m_downX, m_downY);
					if(index >= 0)
					{
						m_parentFlag = true;
						startFrameAnim();
						m_operate = OPERATE_FREE;
						if (m_onMotifiListener != null) {
							m_onMotifiListener.onChooseColor(false, -1, 0);
						}
						((ShapeEx5)item).reset();

						m_target = null;
						EditableTextView.this.invalidate();
					}
				}
				else if(m_operate == OPERATE_FREE)
				{
					if(item == null)
					{
						m_operate = OPERATE_ALL;
						m_showAllChoose = true;
						if (m_onMotifiListener != null) {
							m_onMotifiListener.onMerge();
						}
						EditableTextView.this.invalidate();
					}
					else if(item instanceof ShapeEx5)
					{
						int index = ((ShapeEx5)item).GetSelectIndex(m_downX, m_downY);
						if(index < 0)
						{
							m_operate = OPERATE_ALL;
							m_target = item;

							if (m_onMotifiListener != null) {
								m_onMotifiListener.onMerge();
							}
							Init_M_Data5(m_target, m_downX, m_downY);
							EditableTextView.this.invalidate();
						}
					}
				}
				UpdateUI();
			}
		}
	}

	/**
	 * 初始化移动
	 * 针对文字的，{@link ShapeEx3}
	 * @param target
	 * @param x
	 * @param y
	 */
	protected void Init_M_Data3(ShapeEx target, float x, float y)
	{
		m_oldMatrix.set(target.m_matrix);
		float[] src = new float[]{x, y};
		float[] dst = new float[2];
		GetLogicPoints(((ShapeEx3)target).m_parent, dst, src);
		x = dst[0];
		y = dst[1];
		super.Init_M_Data(target, x, y);
	}

	/**
	 * 初始化移动
	 * 针对文字的，{@link ShapeEx5}
	 * @param target
	 * @param x
	 * @param y
	 */
	protected void Init_M_Data5(ShapeEx target, float x, float y)
	{
		m_oldMatrix.set(target.m_matrix);
		super.Init_M_Data(target, x, y);
	}

	/**
	 * 子元件移动
	 * 针对文字的，{@link ShapeEx3}
	 * @param target
	 * @param x
	 * @param y
	 */
	protected void Run_M3(ShapeEx target, float x, float y)
	{
		target.m_matrix.set(m_oldMatrix);
		float[] src = new float[]{x, y};
		float[] dst = new float[2];
		GetLogicPoints(((ShapeEx3)target).m_parent, dst, src);
		x = dst[0];
		y = dst[1];
		Run_M(target.m_matrix, x, y);
		super.Run_M2(target, x, y);
	}

	/**
	 * 子元件移动
	 * 针对文字的，{@link ShapeEx5}
	 * @param target
	 * @param x
	 * @param y
	 */
	protected void Run_M5(ShapeEx target, float x, float y)
	{
		target.m_matrix.set(m_oldMatrix);
		Run_M(target.m_matrix, x, y);
		super.Run_M2(target, x, y);
	}

	/**
	 * 子元件移动
	 * @param matrix
	 * @param x
	 * @param y
	 */
	protected void Run_M(Matrix matrix, float x, float y)
	{
		matrix.postTranslate(x - m_gammaX, y - m_gammaY);
	}

	/**
	 * 初始化旋转 {@link ShapeEx5}
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_R_Data5(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		m_oldMatrix.set(target.m_matrix);
		m_centerX = (x1 + x2) / 2;
		m_centerY = (y1 + y2) / 2;
		super.Init_R_Data(target, x1, y1, x2, y2);
	}

	/**
	 * 初始化旋转 {@link ShapeEx3}
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_R_Data3(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_R_Data5(target, x1, y1, x2, y2);
		float[] src = new float[]{m_centerX, m_centerY};
		float[] dst = new float[2];
		GetLogicPoints(((ShapeEx3)target).m_parent, dst, src);
		m_centerX = dst[0];
		m_centerY = dst[1];
	}

	protected void Run_R(ShapeEx target, Matrix matrix, float x1, float y1, float x2, float y2)
	{
		float tempAngle;
		if(x1 - x2 == 0)
		{
			if(y1 >= y2)
			{
				tempAngle = 90;
			}
			else
			{
				tempAngle = -90;
			}
		}
		else if(y1 - y2 != 0)
		{
			tempAngle = (float)Math.toDegrees(Math.atan(((double)(y1 - y2)) / (x1 - x2)));
			if(x1 < x2)
			{
				tempAngle += 180;
			}
		}
		else
		{
			if(x1 >= x2)
			{
				tempAngle = 0;
			}
			else
			{
				tempAngle = 180;
			}
		}
		target.m_degree = m_oldDegree + tempAngle - m_beta;
		matrix.postRotate(tempAngle - m_beta, m_centerX, m_centerY);
	}

	/**
	 * 初始化缩放 {@link ShapeEx5}
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_Z_Data5(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		m_oldMatrix.set(target.m_matrix);
		m_centerX = (x1 + x2) / 2;
		m_centerY = (y1 + y2) / 2;
		super.Init_Z_Data(target, x1, y1, x2, y2);
	}

	/**
	 * 初始化缩放 {@link ShapeEx3}
	 * @param target
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	protected void Init_Z_Data3(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_Z_Data5(target, x1, y1, x2, y2);
		float[] src = new float[]{m_centerX, m_centerY};
		float[] dst = new float[2];
		GetLogicPoints(((ShapeEx3)target).m_parent, dst, src);
		m_centerX = dst[0];
		m_centerY = dst[1];
	}

	protected void Run_Z(ShapeEx target, Matrix matrix, float x1, float y1, float x2, float y2)
	{
		float tempDist = ImageUtils.Spacing(x1 - x2, y1 - y2);
		if(tempDist > 10)
		{
			float scale = tempDist / m_delta;
			matrix.postScale(scale, scale, m_centerX, m_centerY);
			float scaleX = m_oldScaleX * scale;
			float scaleY = m_oldScaleY * scale;
			float tempScaleX = scaleX;
			float tempScaleY = scaleY;
			if(scaleX > target.MAX_SCALE)
			{
				scaleX = target.MAX_SCALE;
				scaleY = scaleX / m_oldScaleX * m_oldScaleY;
			}
			if(scaleY > target.MAX_SCALE)
			{
				scaleY = target.MAX_SCALE;
				scaleX = scaleY / m_oldScaleY * m_oldScaleX;
			}
			if(scaleX < target.MIN_SCALE)
			{
				scaleX = target.MIN_SCALE;
				scaleY = scaleX / m_oldScaleX * m_oldScaleY;
			}
			if(scaleY < target.MIN_SCALE)
			{
				scaleY = target.MIN_SCALE;
				scaleX = scaleY / m_oldScaleY * m_oldScaleX;
			}
			target.SetScaleXY(scaleX, scaleY);

			matrix.postScale(scaleX / tempScaleX, scaleY / tempScaleY, m_centerX, m_centerY);
		}
	}

	protected void Init_RZ_Data3(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_R_Data3(target, x1, y1, x2, y2);
		Init_Z_Data3(target, x1, y1, x2, y2);
	}

	protected void Init_RZ_Data5(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_R_Data5(target, x1, y1, x2, y2);
		Init_Z_Data5(target, x1, y1, x2, y2);
	}

	protected void Init_MRZ_Data3(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_M_Data3(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
		Init_R_Data3(target, x1, y1, x2, y2);
		Init_Z_Data3(target, x1, y1, x2, y2);
	}

	protected void Init_MRZ_Data5(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		Init_M_Data5(target, (x1 + x2) / 2f, (y1 + y2) / 2f);
		Init_R_Data5(target, x1, y1, x2, y2);
		Init_Z_Data5(target, x1, y1, x2, y2);
	}

	protected void Run_RZ35(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		target.m_matrix.set(m_oldMatrix);
		Run_R(target, target.m_matrix, x1, y1, x2, y2);
		Run_Z(target, target.m_matrix, x1, y1, x2, y2);
	}

	protected void Run_MRZ5(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		target.m_matrix.set(m_oldMatrix);
		Run_R(target, target.m_matrix, x1, y1, x2, y2);
		Run_Z(target, target.m_matrix, x1, y1, x2, y2);
		Run_M(target.m_matrix, (x1 + x2) / 2f, (y1 + y2) / 2f);
	}

	protected void Run_MRZ3(ShapeEx target, float x1, float y1, float x2, float y2)
	{
		target.m_matrix.set(m_oldMatrix);
		Run_R(target, target.m_matrix, x1, y1, x2, y2);
		Run_Z(target, target.m_matrix, x1, y1, x2, y2);

		float[] src = new float[]{(x1 + x2) / 2f, (y1 + y2) / 2f};
		float[] dst = new float[2];
		GetLogicPoints(((ShapeEx3)target).m_parent, dst, src);
		float x = dst[0];
		float y = dst[1];
		Run_M(target.m_matrix, x, y);
	}

	public interface OnMotifyListener
	{
		public void onClick(String text, float y);

		public void onChooseColor(boolean show, int color, int shadowAlpha);

		public void onSave(ShapeEx shape, Object saveBmp);

		public void onDelete(ShapeEx shape, int index);

		/**
		 * 拆分
		 */
		public void onDevide();

		/**
		 * 组合
		 */
		public void onMerge();
	}

}
