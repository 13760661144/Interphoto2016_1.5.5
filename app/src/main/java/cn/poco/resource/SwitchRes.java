package cn.poco.resource;

/**
 * Created by admin on 2017/12/27.
 */

public class SwitchRes extends BaseRes
{
	public String mClassify;
	public String mTitle;
	public String mDescribe;
	public String mId;
	public String mTip;
	public int mTime;
	public boolean mUnlock = false;
	public SwitchRes()
	{
		super(ResType.SWITCH.GetValue());
	}

	@Override
	public String GetSaveParentPath()
	{
		return DownloadMgr.getInstance().OTHER_PATH;
	}

	@Override
	public void OnDownloadComplete(DownloadTaskThread.DownloadItem item, boolean isNet)
	{

	}
}
