package cn.poco.resource;

public enum ResType
{
	FRAME(0x2),
	CARD(0x4),
	DECORATE(0x8),
	PUZZLE_BK(0x10),
	PUZZLE_TEMPLATE(0x20),
	THEME(0x40),
	LOCK(0x200),
	BANNER(0x800),
	TEXT(0x1000),
	LIGHT_EFFECT(0x2000),
	BOOT_IMG(0x4000),
	BUSINESS(0x8000),
	FONT(0x10000),
	MY_LOGO(0x20000),
	RECOMMEND(0x40000),
	APP(0x80000),
	FILTER(0x90000),
	TEXT_WATERMARK(0x91000),
	TEXT_ATTITUTE(0x92000),
	MUSIC(0x93000),
	AUDIO_TEXT(0x94000),
    VIEDO_ORIGINALITY(0x95000),
    VIEDO_WATERMARK(0x96000),
	SWITCH(0x97000)
    ;



    private final int m_value;

	private ResType(int value)
	{
		m_value = value;
	}

	public int GetValue()
	{
		return m_value;
	}

	public static ResType GetType(int value)
	{
		ResType out = null;

		ResType[] list = values();
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
