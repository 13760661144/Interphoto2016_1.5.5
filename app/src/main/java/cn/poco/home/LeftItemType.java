package cn.poco.home;

/**
 * Created by admin on 2017/12/27.
 */

public enum LeftItemType
{
	NONE(-1),
	MAGAZINE(0),
	MERTIRAL(1),
	RECOMMENT(2),
	SETTING(3);
	private final int m_type;
	LeftItemType(int type)
	{
		m_type = type;
	}

}
