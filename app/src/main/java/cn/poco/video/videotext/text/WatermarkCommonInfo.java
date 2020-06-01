package cn.poco.video.videotext.text;


/**
 * Created by admin on 2017/6/7.
 */

public class       WatermarkCommonInfo
{
	public int m_cid;
	public String m_con;	//常规、天气、时间
	public String m_pos;	//对齐方式
	public float m_offsetX;
	public float m_offsetY;
	public int baseAlpha;	//文字的初始alpha，

	public float m_shadowX = 3;
	public float m_shadowY = 3;
	public int m_shadowColor;
	public float m_shadowRadius = 1f;
	public int m_shadowAlpha;
	public int m_ncColor = 0;		//1不变色   0 变色

	public int animation_id;	//动画的分类
	public int animation_be;	//动画开始时间
	public int animation_time;	//动画时长
}
