package cn.poco.beautify;

public enum BeautifyClipType
{
	NONE(-1),
	CLIP(1),
	ROTATION(2),
	ADJUST_H(4),
	ADJUST_V(8);
	private final int m_value;

	BeautifyClipType(int value)
	{
		m_value = value;
	}

	public int GetValue()
	{
		return m_value;
	}

	public static BeautifyClipType GetType(int value)
	{
		BeautifyClipType out = NONE;

		BeautifyClipType[] list = values();
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
