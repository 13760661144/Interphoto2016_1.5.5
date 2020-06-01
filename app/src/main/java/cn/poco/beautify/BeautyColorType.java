package cn.poco.beautify;
/**颜色类型*/
public enum BeautyColorType
{
	NONE(-1),
	FILTER(1),//颜色滤镜
	ADJUST(2),	//调整
	LIGHT(3);	//光效
	private int m_value;
	public int GetValue()
	{
		return m_value;
	}
	
	BeautyColorType(int value)
	{
		m_value = value;
	}

}
