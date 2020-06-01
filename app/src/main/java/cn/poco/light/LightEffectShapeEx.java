package cn.poco.light;

import cn.poco.graphics.ShapeEx;

/**
 * Created by admin on 2017/7/28.
 */

public class LightEffectShapeEx extends ShapeEx
{
	public int m_alpha = 120; //0-120
	public int m_mode;

	public void Set(LightEffectShapeEx item)
	{
		super.Set(item);
		m_alpha = item.m_alpha;
		m_mode = item.m_mode;
	}
}
