package cn.poco.video.videotext.text;

import java.util.ArrayList;

/**
 * 整块水印的info
 */

public class WaterMarkInfo
{
	public static final int EFFECT_NONE = 0;	//固定的图片，不根据文字的位置
	public static final int EFFECT_1 = 10001;	//图片在文字左右两边
	public static final int EFFECT_2 = 10002;	//图片在文字上下
	public static final int EFFECT_3 = 10010;	//根据文本长度与高度，色块自适应纵向横向拉伸
	public static final int EFFECT_4 = 10011;	//根据文本长度与高度，线框自适应纵向横向拉伸
	public static final int EFFECT_5 = 10012;	//单行情况下适配文本长度延伸，多行情况下第二行自动加线。
	public static final int EFFECT_6 = 10003;	//横向铺满
	public static final int EFFECT_7 = 10004;	//图片动画+文字

//	public static final int EFFECT_1_DISTANCE = 35;
//	public static final int EFFECT_1_MIN_DISTANCE = 70;

//	public static final int EFFECT_2_DISTANCE = 10;
//	public static final int EFFECT_2_MIN_DISTANCE = 20;

	public static final int EFFECT_3_DISTANCE_X = 140;
	public static final int EFFECT_3_DISTANCE_Y = 110;

	public static final int EFFECT_4_DISTANCE_X = 70;
	public static final int EFFECT_4_DISTANCE_Y = 105;

	public static final int EFFECT_5_DISTANCE = 20;

	public ArrayList<CharInfo> m_fontsInfo;
	public ArrayList<ImageInfo> m_imgInfo;

	public String align;
	public float offsetX;
	public float offsetY;
	public int animation_id;	//动画的类型
	public int animation_be;	//动画延迟开始时间
	public int animation_time;	//动画持续时间
	public int animation_hold_time; //动画停留在最后一帧的停留时间
	public int effect_id;	//图片类型（动画帧、可延伸）
	public ArrayList<ElementsAnimInfo> m_animInfo = new ArrayList<>();

	public String res_path;
}
