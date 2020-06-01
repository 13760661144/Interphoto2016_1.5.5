package cn.poco.draglistview;

/**
 * 参考文字、光效、滤镜下面的缩略图列表
 */
public class DragListItemInfo
{
	public static final int URI_NONE = 0xFFFFFFF0;

	public static final int URI_ORIGIN = 0xFFFFFFFA;
	public static final int URI_MYTEXT = 0xFFFFFFFE;
	public static final int URI_MGR = 0xFFFFFFFC;
	public static final int URI_RECOMMENT = 0xFF000000;	//推荐位ID
	public static final int URI_LOCAL_MUSIC = 0xFFFFFF11;
	public static final int URI_VIDEO_TEXT_NONE = 0xFFFFFF12;
	public static final int URI_MUSIC_NONE = 0xFFFFFF13;

	public int m_uri = URI_NONE; //必须唯一
	public String m_key = "";

	public Object m_logo;
	public Object m_head;	//头像
	public String m_name;
	public String m_author;
	public int text_bg_color_out;
	public int text_bg_color_over;
	public int m_progress = 100;
	public boolean m_canDrag = false;
	public boolean m_canDrop = false;
	public boolean m_selected;
	public boolean m_isRecomment = false;

	public boolean m_selectAnim = false;
	public int m_outHeight;
	public int m_overHeight;

	public boolean m_isHideEditLogo;      //  视频小于水印时间不显示编辑图标
	public enum Style
	{
		//正常
		NORMAL(0),
		//需要下载
		NEED_DOWNLOAD(1),
		//下载中
		LOADING(2),
		//等待下载
		WAIT(3),
		//下载失败
		FAIL(4),
		//新下载
		NEW(5);

		private final int m_value;

		Style(int value)
		{
			m_value = value;
		}

		public int GetValue()
		{
			return m_value;
		}
	}

	public Style m_style = Style.NORMAL;
	public boolean m_isLock = false;

	public Object m_ex;
}
