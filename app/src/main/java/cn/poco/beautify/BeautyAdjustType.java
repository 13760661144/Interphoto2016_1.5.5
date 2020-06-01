package cn.poco.beautify;

/**
 * 颜色调整类型
 */
public enum BeautyAdjustType
{
	NONE(-1),
	TEMPERATURE(4),//色温
	BRIGHTNESS(5),	//亮度
	SATURABILITY(6),	//饱和度
	CONTRAST(7),	//对比度
	SHARPEN(8),		//锐化
	GRAMMATICALIZATION(9),	//虚化
	BETTER(10),	//增强

	HIGHTLIGHT(11), //高光
	SHADE(12),	//暗部
	HUE(13),	//色调
	DARKCORNER(14),		//暗角
	PARTICAL(15),	//颗粒
	FADE(16),	//褪色

	CURVE(17),	//	曲线
	CLIP(18),	//裁剪

	BEAUTY(19);	//肤色


	private int m_value;

	public int GetValue()
	{
		return m_value;
	}

	BeautyAdjustType(int type)
	{
		m_value = type;
	}

	public static BeautyAdjustType GetType(int value)
	{
		BeautyAdjustType out = NONE;

		BeautyAdjustType[] list = values();
		if(list != null)
		{
			for(int i = 0; i < list.length; i++)
			{
				if(list[i].GetValue() == value)
				{
					out = list[i];
					break;
				}
			}
		}

		return out;
	}
}
