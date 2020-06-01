package cn.poco.beautify;

import android.graphics.PorterDuff;

import cn.poco.graphics.ShapeEx;

/**
 * 可移动光效
 */
public class ShapeEx2 extends ShapeEx
{
	public int m_alpha = 120; //0-120
	public PorterDuff.Mode m_mode = PorterDuff.Mode.SCREEN;	//滤色
}
