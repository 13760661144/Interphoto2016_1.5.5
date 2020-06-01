package cn.poco.MaterialMgr2;

import java.util.ArrayList;

import cn.poco.resource.BaseRes;
import cn.poco.resource.ResType;
import cn.poco.resource.ThemeRes;

/**
 * Created by admin on 2016/9/8.
 */
public class ThemeItemInfo
{
	public static final int URI_NONE = 0xFFFFFFF0;
	public static final int PREPARE = 201;	//需要下载，但是还没开始下载
	public static final int LOADING = 202;	//正在下载
	public static final int COMPLETE = 203;	//不需要下载或者下载完成
	public static final int CONTINUE = 204;	//需要下载，但是已经下载完成一部分

	public int m_uri = URI_NONE; //必须唯一
	public int m_state = PREPARE;
	public ThemeRes m_themeRes;
	public ArrayList<BaseRes> m_resArr;
	public int[] m_idArr;
	public ResType m_type;
	public String m_key;
	public boolean m_lock = false;

	public int m_progress = 0;	//下载进度
	public int m_downloadId = -1;
	public Object m_ex;
}
