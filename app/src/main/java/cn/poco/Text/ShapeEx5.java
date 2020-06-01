package cn.poco.Text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.poco.graphics.ShapeEx;
import cn.poco.utils.Utils;

/**
 * 文字最终版，可拆分
 */
public class ShapeEx5 extends ShapeEx
{
	protected ArrayList<ShapeEx3> m_modules = new ArrayList<>();
	private Matrix temp_matrix = new Matrix();

	private Context m_context;
	private int m_curSelModule = -1;
	private int m_curSelTextIndex = -1; //主要用于文字修改
	public RectF m_bmpRect = new RectF(0, 0, 0, 0);

	private boolean m_showTextRect = false; //文字选中虚线框
	protected boolean m_drawOnlyCurModule = false;
	private boolean m_showTextAnim = false;

	private int m_color = -1;	//当前文字的标记颜色，主要用于颜色切换的时候颜色列表的颜色选中
	private int m_shadowAlpha = -1;
	private int m_textAlpha = 255;

	public ShapeEx5(Context context)
	{
		m_context = context;
	}

	public void SetData(MyTextInfo textInfo)
	{
		m_modules.clear();
		HashMap<Integer, MyTextInfo> textInfos = new HashMap<>();
		ArrayList<Integer> m_keySet = new ArrayList<>();

		if(textInfo != null)
		{
			if(textInfo.m_imgInfo != null)
			{
				MyTextInfo tempInfo;
				int len = textInfo.m_imgInfo.size();
				ImgInfo info;
				for(int i = 0; i < len; i ++)
				{
					info = textInfo.m_imgInfo.get(i);
					if(textInfos.get(info.m_cid) == null)
					{
						tempInfo = new MyTextInfo();
						tempInfo.Set(textInfo);
						tempInfo.m_fontsInfo = new ArrayList<>();
						tempInfo.m_imgInfo = new ArrayList<>();
						textInfos.put(info.m_cid, tempInfo);

						m_keySet.add(info.m_cid);
					}
					else {
						tempInfo = textInfos.get(info.m_cid);
					}
					if(tempInfo.m_imgInfo == null)
					{
						tempInfo.m_imgInfo = new ArrayList<>();
					}
					tempInfo.m_imgInfo.add(info);
				}
			}
			if(textInfo.m_fontsInfo != null)
			{
				MyTextInfo tempInfo;
				FontInfo info;
				int len = textInfo.m_fontsInfo.size();
				for(int i = 0; i < len; i ++)
				{
					info = textInfo.m_fontsInfo.get(i);
					if(textInfos.get(info.m_cid) == null)
					{
						tempInfo = new MyTextInfo();
						tempInfo.Set(textInfo);
						tempInfo.m_fontsInfo = new ArrayList<>();
						tempInfo.m_imgInfo = new ArrayList<>();
						textInfos.put(info.m_cid, tempInfo);

						m_keySet.add(info.m_cid);
					}
					else {
						tempInfo = textInfos.get(info.m_cid);
					}
					if(tempInfo.m_fontsInfo == null)
					{
						tempInfo.m_fontsInfo = new ArrayList<>();
					}
					tempInfo.m_fontsInfo.add(info);
				}
			}
			int size = m_keySet.size();
			for(int i = 0; i < size; i ++)
			{
				int key = m_keySet.get(i);
				MyTextInfo info = textInfos.get(key);
				if(info == null)
				{
					continue;
				}
				ShapeEx3 shapeEx3 = new ShapeEx3(m_context, this);
				shapeEx3.SetCID(key);
				shapeEx3.SetTextInfo(info);
				shapeEx3.MAX_SCALE = 10f;
				if(info.m_fontsInfo != null && info.m_fontsInfo.size() > 0)
				{
					float[] matrixValue = info.m_fontsInfo.get(0).m_matrixValue;
					if(matrixValue != null && matrixValue.length == 9)
					{
						shapeEx3.m_matrix.setValues(matrixValue);
					}
				}
				else if(info.m_imgInfo != null && info.m_imgInfo.size() > 0)
				{
					float[] matrixValue = info.m_imgInfo.get(0).m_matrixValue;
					if(matrixValue != null && matrixValue.length == 9)
					{
						shapeEx3.m_matrix.setValues(matrixValue);
					}
				}

				m_modules.add(shapeEx3);
			}
			/*for (Iterator it = textInfos.entrySet().iterator(); it.hasNext();) {
				Map.Entry e = (Map.Entry) it.next();
				MyTextInfo info = (MyTextInfo)e.getValue();
				int key = (Integer)e.getKey();
				ShapeEx3 shapeEx3 = new ShapeEx3(m_context, this);
				shapeEx3.SetCID(key);
				shapeEx3.SetTextInfo(info);
				shapeEx3.MAX_SCALE = 10f;
				m_modules.add(shapeEx3);
			}*/
		}
	}

	@Override
	public void Set(ShapeEx item)
	{
		super.Set(item);
		if(item instanceof ShapeEx5)
		{
			this.m_bmpRect = ((ShapeEx5)item).m_bmpRect;
			this.m_modules = ((ShapeEx5)item).m_modules;
		}
	}

	public void SetData(ArrayList<ShapeEx3> modules)
	{
		m_modules = modules;
	}

	public void InitShapeInfo()
	{
		int index = 0;
		for (ShapeEx3 item: m_modules) {
			item.GetShapeInfo(item.GetTextInfo());
			if(index == 0)
			{
				m_bmpRect.set(item.m_bmpRect);
			}
			else
			{
				m_bmpRect.union(item.m_bmpRect);
			}
			index ++;
		}
		m_w = (int)m_bmpRect.width();
		m_h = (int)m_bmpRect.height();
		m_centerY = m_h / 2f;
		m_centerX = m_w / 2f;
	}

	protected void UpdateCurShape()
	{
		RectF tempRect = new RectF();
		tempRect.set(m_bmpRect);

		int index = 0;
		for (ShapeEx3 item: m_modules) {
			if(index == 0)
			{
				m_bmpRect.set(item.m_bmpRect);
			}
			else
			{
				m_bmpRect.union(item.m_bmpRect);
			}
			index ++;
		}
		m_bmpRect.offset(tempRect.left - m_bmpRect.left, tempRect.top - m_bmpRect.top);
		m_w = (int)m_bmpRect.width();
		m_h = (int)m_bmpRect.height();
		m_centerY = m_h / 2f;
		m_centerX = m_w / 2f;
	}

	public void UpdateChildMatrix()
	{
		for (ShapeEx3 item: m_modules) {
			GetShowMatrix(item.m_matrix, item);
		}
	}

	public void draw(Canvas canvas)
	{
		ShapeEx3 item;
		int size = m_modules.size();

		for(int i = 0; i < size; i ++)
		{
			item = m_modules.get(i);
			canvas.save();
			canvas.concat(m_matrix);
			canvas.concat(item.m_matrix);
			item.draw(canvas);
			canvas.restore();
		}
	}

	public void DrawOutput(Canvas canvas, Matrix outMatrix)
	{
		ShapeEx3 item;
		int size = m_modules.size();

		for(int i = 0; i < size; i ++)
		{
			item = m_modules.get(i);
			canvas.save();
			canvas.concat(outMatrix);
			canvas.concat(item.m_matrix);
			item.draw(canvas);
			canvas.restore();
		}
	}

	public float[] GetRect()
	{
		ShapeEx3 item;
		float[] out = new float[8];
		int size = m_modules.size();
		for(int i = 0; i < size; i ++)
		{
			item = m_modules.get(i);
			if (item != null)
			{
				float[] dsts = item.getRectPoints(item.m_bmpRect, item.m_matrix);

				if(i == 0)
				{
					out = dsts;
				}
				Utils.RoundPoint(out, dsts);
			}
		}
		return out;
	}


	public ShapeEx3 GetCurModule()
	{
		if(m_curSelModule >= 0 && m_curSelModule < m_modules.size())
		{
			return m_modules.get(m_curSelModule);
		}
		return null;
	}

	public int GetCurModuleIndex()
	{
		return m_curSelModule;
	}

	public void setCurModuleIndex(int index)
	{
		m_curSelModule = index;
	}

	public ShapeEx3 GetModule(int index)
	{
		if(index >= 0 && index < m_modules.size())
		{
			return m_modules.get(index);
		}
		return null;
	}

	public int GetModuleCount()
	{
		if(m_modules != null)
			return m_modules.size();
		return 0;
	}

	public int GetSelectIndex(float x, float y)
	{
		int index = -1;
		int size = m_modules.size();
		ShapeEx3 item;
		for(int i = size - 1; i >= 0; i--)
		{
			item = m_modules.get(i);

			if(IsClickRect(item.m_bmpRect, item, x, y))
			{
				index = i;
				break;
			}
		}
		return index;
	}

	public int GetCurSelModule()
	{
		return m_curSelModule;
	}

	public ShapeEx3 delete()
	{
		ShapeEx3 out = null;
		if(m_curSelModule > -1 && m_curSelModule < m_modules.size())
		{
			out = m_modules.remove(m_curSelModule);
			int size = m_modules.size();
			m_curSelModule = size - 1;
			UpdateCurShape();
			UpdateChildMatrix();
		}
		return out;
	}

	public void DrawRect(Canvas canvas)
	{
		ShapeEx3 item;
		if(m_drawOnlyCurModule)
		{
			item = GetCurModule();
			if(item != null)
			{
				DrawRect(canvas, item);
			}
		}
		else
		{
			int size = m_modules.size();
			for(int i = 0; i < size; i++)
			{
				item = m_modules.get(i);
				DrawRect(canvas, item);
			}
		}
	}

	public void DrawRect(Canvas canvas, ShapeEx3 item)
	{
		if(item != null)
		{
			canvas.save();
			canvas.concat(m_matrix);
			item.drawRect(canvas, m_curSelTextIndex, item.m_matrix, m_showTextRect);
			canvas.restore();
		}
	}

	public void SetAnimCurCount(boolean show, boolean flag)
	{
		m_showTextRect = show;
		m_drawOnlyCurModule = flag;
	}

	public void StartTextAnim()
	{
		m_showTextAnim = true;
	}

	public void DoTextAnim()
	{
		if(m_showTextAnim)
		{
			m_showTextRect = !m_showTextRect;
		}
	}

	public void cancelAnim()
	{
		m_showTextAnim = false;
	}

	/**
	 *
	 * @return	当前文字的颜色，取第一个文字颜色
	 */
	public int GetCurColor()
	{
		if(m_color == -1)
		{
			if(m_modules.size() >= 1)
			{
				ShapeEx3 item = m_modules.get(0);
				m_color = item.GetCurColor();
			}
		}
		m_color = Painter.SetColorAlpha(m_textAlpha, m_color);
		return m_color;
	}

	public int GetCurShadowAlpha()
	{
		if(m_shadowAlpha == -1)
		{
			if(m_modules.size() >= 1)
			{
				ShapeEx3 item = m_modules.get(0);
				m_shadowAlpha = item.GetCurShadowAlpha();
			}
		}
		return m_shadowAlpha;
	}

	public void UpdateText(String text)
	{
		ShapeEx3 item = GetCurModule();
		if(item != null)
		{
			item.UpdateText(text, m_curSelTextIndex);
		}
	}

	public void ResetModuleOperate()
	{
		int size = m_modules.size();
		ShapeEx3 item;
		for(int i = 0; i < size; i ++)
		{
			item = m_modules.get(i);
			if(item != null)
			{
				item.m_matrix.reset();
			}
		}
	}

	/**
	 *
	 * @param color
	 * @param selOnly	是否只是改变当前模块的颜色
	 */
	public int UpdateColor(int color, boolean selOnly)
	{
		int size = m_modules.size();
		ShapeEx3 item;
		if(selOnly)
		{
			item = GetCurModule();
			if(item != null)
			{
				m_color = item.UpdateColor(color, selOnly);
			}
		}
		else
		{
			for(int i = 0; i < size; i ++)
			{
				item = m_modules.get(i);
				if(item != null)
				{
					m_color = item.UpdateColor(color, selOnly);
				}
			}
		}
		return m_color;
	}

	public int UpdateAlpha(int alpha, boolean selOnly)
	{
		int size = m_modules.size();
		ShapeEx3 item;
		if(selOnly)
		{
			item = GetCurModule();
			if(item != null)
			{
				m_color = item.UpdateAlpha(alpha, m_textAlpha, selOnly);
			}
		}
		else
		{
			m_textAlpha = alpha;
			for(int i = 0; i < size; i ++)
			{
				item = m_modules.get(i);
				if(item != null)
				{
					m_color = item.UpdateAlpha(alpha, m_textAlpha, selOnly);
				}
			}
		}
		return m_color;
	}

	public int GetAlpha()
	{
		return m_textAlpha;
	}

	public int UpdateShadowAlpha(int alpha, boolean selOnly)
	{
		m_shadowAlpha = alpha;
		int size = m_modules.size();
		ShapeEx3 item;
		if(selOnly)
		{
			item = GetCurModule();
			if(item != null)
			{
				item.UpdateShadowAlpha(alpha);
			}
		}
		else
		{
			for(int i = 0; i < size; i ++)
			{
				item = m_modules.get(i);
				if(item != null)
				{
					item.UpdateShadowAlpha(alpha);
				}
			}
		}
		return m_shadowAlpha;
	}

	public void ClearFontPaint()
	{
		for (ShapeEx3 item: m_modules) {
			item.ClearFontPaint();
		}
	}

	public void ResetSelFontIndex()
	{
		m_curSelTextIndex = -1;
	}

	/**
	 * 初始化部分信息，主要用于拆分合并
	 */
	public void reset()
	{
		m_curSelModule = -1;
		if(m_modules != null)
		{
			ShapeEx3 item;
			int size = m_modules.size();
			for(int i = 0; i < size; i ++)
			{
				item = m_modules.get(i);
				item.resetColorAndAlpha();
			}
		}
		m_shadowAlpha = -1;
		m_color = -1;
	}

	public FontInfo GetCurSelFont(float x, float y, boolean selOnly)
	{
		FontInfo curFont = null;
		int size = m_modules.size();
		ShapeEx3 shape;
		if(selOnly)
		{
			shape = GetCurModule();
			curFont = isFontClick(shape, x, y);
			return curFont;
		}
		for(int i = 0; i < size; i ++)
		{
			shape = m_modules.get(i);
			curFont = isFontClick(shape, x, y);
			if(curFont != null)
			{
				m_curSelModule = i;
				return curFont;
			}
		}

		return curFont;
	}

	protected FontInfo isFontClick(ShapeEx3 shape, float x, float y)
	{
		m_curSelTextIndex = -1;
		FontInfo curFont = null;
		if(shape != null && shape.m_textInfo != null
				&& shape.m_textInfo.m_fontsInfo != null)
		{
			for(Iterator it = shape.m_textRects.entrySet().iterator(); it.hasNext();)
			{
				Map.Entry e = (Map.Entry) it.next();
				RectF rect = (RectF)e.getValue();
				Integer key = (Integer)e.getKey();
				if(IsClickRect(rect, shape, x, y))
				{
					m_curSelTextIndex = key;
					if(m_curSelTextIndex >= 0 && m_curSelTextIndex <= shape.m_textInfo.m_fontsInfo.size())
					{
						curFont = shape.m_textInfo.m_fontsInfo.get(m_curSelTextIndex);
						return curFont;
					}
				}
			}
		}
		return curFont;
	}

	protected boolean IsClickRect(RectF rect, ShapeEx3 shape, float x, float y)
	{
		boolean out = false;

		if (rect != null && shape != null)
		{
			Canvas canvas = new Canvas();
			canvas.save();
			canvas.concat(m_matrix);
			canvas.concat(shape.m_matrix);
			canvas.getMatrix(temp_matrix);
			canvas.restore();
			float[] dsts = new float[8];
			dsts[0] = rect.left;
			dsts[1] = rect.top;
			dsts[2] = rect.right;
			dsts[3] = rect.top;
			dsts[4] = rect.right;
			dsts[5] = rect.bottom;
			dsts[6] = rect.left;
			dsts[7] = rect.bottom;

			if(Utils.isSelected(temp_matrix, dsts, x, y))
			{
				out = true;
			}
		}
		return out;
	}

	protected void GetShowMatrix(Matrix matrix, ShapeEx3 item)
	{
		matrix.reset();

		float[] dst = {item.m_x + item.m_centerX, item.m_y + item.m_centerY};

		matrix.postTranslate(dst[0] - item.m_centerX, dst[1] - item.m_centerY);
		matrix.postTranslate(-m_bmpRect.left, -m_bmpRect.top);
		matrix.postScale(item.m_scaleX, item.m_scaleY, dst[0], dst[1]);
		matrix.postRotate(item.m_degree, dst[0], dst[1]);
	}
}
