package cn.poco.beautify;

public enum BeautifyModuleType
{
	NONE(-1),
	CLIP(1),
	COLOR(2),
	TEXT(4),
	FRAME(8),
	FILTER(16),
	EFFECT(32)
	;
	private final int m_value;

	BeautifyModuleType(int value)
	{
		m_value = value;
	}

	public int GetValue()
	{
		return m_value;
	}

	public static BeautifyModuleType GetType(int value)
	{
		BeautifyModuleType out = NONE;

		BeautifyModuleType[] list = values();
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
