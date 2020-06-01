package cn.poco.adMaster;

import android.content.Context;

import com.adnonstop.admasterlibs.AbsAdMaster;
import com.adnonstop.admasterlibs.data.AbsAdRes;
import com.adnonstop.admasterlibs.data.AdPackage;
import com.adnonstop.resourcelibs.CallbackHolder;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import cn.poco.framework.EventCenter;
import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.utils.Utils;

/**
 * Created by admin on 2017/8/23.
 */

public class AdMaster extends AbsAdMaster
{
	private static AdMaster m_master;
	private ArrayList<BootImgRes> m_bootResArr = new ArrayList<>();
	private ArrayList<ClickAdRes> m_clickResArr = new ArrayList<>();
	private String[] mTjStr;
	private CallbackHolder mHolder = new CallbackHolder(null);
	private AdMaster(Context context)
	{
		super(AppInterface.GetInstance(context));
	}

	public synchronized static AdMaster getInstance(Context context)
	{
		if(m_master == null){
			m_master = new AdMaster(context);
		}
		return m_master;
	}

	public synchronized static void clearInstance()
	{
		m_master = null;
	}

	public void Init(Context context)
	{
		mHolder.SetEventFlag(true);
		m_bootResArr.clear();
		m_clickResArr.clear();
		ArrayList<AdPackage> res = sync_ar_GetCloudCacheRes(context, null, mHolder);
		getBootResArr(m_bootResArr, res, true);
		getClickResArr(m_clickResArr, res, true);
		getTJStr(res);

		EventCenter.addListener(s_lst, true);
	}

	public ArrayList<BootImgRes> getBootDatas()
	{
		return m_bootResArr;
	}

	public BootImgRes GetOneLocalBootImgRes()
	{
		BootImgRes out = null;

		if(m_bootResArr != null)
		{
			ArrayList<BootImgRes> arr = new ArrayList<BootImgRes>();
			BootImgRes temp;
			int total = 0;
			int len = m_bootResArr.size();
			for(int i = 0; i < len; i++)
			{
				temp = m_bootResArr.get(i);
				if(temp.m_type != BaseRes.TYPE_NETWORK_URL)
				{
					total += temp.mProbability;
					arr.add(temp);
				}
			}
			int ran = (int)(total * Math.random());
			total = 0;
			len = arr.size();
			if(len > 0)
			{
				for(int i = 0; i < len; i++)
				{
					temp = arr.get(i);
					total += temp.mProbability;
					if(ran < total)
					{
						out = temp;
						break;
					}
				}
			}
		}

		return out;
	}

	public static String BuildBootVideoStr(BootImgRes res)
	{
		StringBuilder builder = new StringBuilder();
		if(res != null)
		{
			builder.append(res.mBeginTime);
			builder.append("_");
			builder.append(res.mClick);
			builder.append("_");
			builder.append(res.mProbability);
			builder.append("_");
			builder.append(res.mPlayTimes);
			builder.append("_");
			builder.append(res.mEndTime);
			builder.append("_");
			builder.append(res.mShowTime);
			if(res.url_adm != null && res.url_adm.length > 0)
			{
				for(int i = 0; i < res.url_adm.length; i ++)
				{
					builder.append("_");
					builder.append(res.url_adm[i]);
				}
			}
			if(res.mShowTjs != null && res.mShowTjs.length > 0)
			{
				for(int i = 0; i < res.mShowTjs.length; i ++)
				{
					builder.append("_");
					builder.append(res.mShowTjs[i]);
				}
			}
			if(res.mClickTjs != null && res.mClickTjs.length > 0)
			{
				for(int i = 0; i < res.mClickTjs.length; i ++)
				{
					builder.append("_");
					builder.append(res.mClickTjs[i]);
				}
			}
		}
		return builder.toString();
	}

	public ArrayList<ClickAdRes> getClickResArr()
	{
		return m_clickResArr;
	}

	public String[] getTJStr()
	{
		return mTjStr;
	}

	private ArrayList<AbsAdRes> getAndDownloadResArr(ArrayList<AdPackage> res, String key, boolean faseDownload)
	{
		ArrayList<AbsAdRes> out = null;
		if(res != null && res.size() > 0)
		{
			out = res.get(0).GetAdByPos(key);
			if(out != null)
			{
				int len = out.size();
				for(int i = 0; i < len; i++)
				{
					if(faseDownload)
					{
						DownloadMgr.getInstance().FastDownloadRes(out.get(i), true);
					}
					else
					{
						DownloadMgr.getInstance().DownloadRes(out.get(i), null);
					}
				}
			}
		}
		return out;
	}

	private void getBootResArr(ArrayList<BootImgRes> dst, ArrayList<AdPackage> res, boolean fastDownload)
	{
		if(res != null && res.size() > 0 && dst != null)
		{
			dst.clear();
			ArrayList<AbsAdRes> tempArr = getAndDownloadResArr(res, "boot", fastDownload);
			if(tempArr != null && tempArr.size() > 0)
			{
				int size = tempArr.size();
				for(int i = 0; i < size; i ++)
				{
					dst.add((BootImgRes)tempArr.get(i));
				}
			}
		}
	}

	private void getClickResArr(ArrayList<ClickAdRes> dst, ArrayList<AdPackage> res, boolean fastDownload)
	{
		if(res != null && res.size() > 0 && dst != null)
		{
			dst.clear();
			ArrayList<AbsAdRes> tempArr = getAndDownloadResArr(res, "channel", fastDownload);
			if(tempArr != null && tempArr.size() > 0)
			{
				int size = tempArr.size();
				for(int i = 0; i < size; i ++)
				{
					dst.add((ClickAdRes)tempArr.get(i));
				}
			}
			orderBusiness(dst);
		}
	}

	public void getTJStr(ArrayList<AdPackage> res)
	{
		if(res != null && res.size() > 0)
		{
			boolean flag = false;
			if(mTjStr == null)
			{
				flag = true;
			}
			mTjStr = res.get(0).mAdMonitor;
			if(flag){
				Utils.SendTj(MyFramework2App.getInstance().getApplicationContext(), mTjStr);
			}
		}
	}

	private static void orderBusiness(ArrayList<ClickAdRes> res)
	{
		if(res != null && res.size() > 0)
		{
			int size = res.size();

			ClickAdRes bisRes1;
			ClickAdRes bisRes2;
			for(int i = 0; i < size; i ++)
			{
				for(int j = size - 1; j > i; j--)
				{
					if(res.get(j).m_insertIndex < res.get(j - 1).m_insertIndex)
					{
						bisRes1 = res.get(j - 1);
						bisRes2 = res.get(j);
						res.set(j - 1, bisRes2);
						res.set(j, bisRes1);
					}
				}
			}
		}
	}

	@Override
	protected int GetLocalEventId()
	{
		return 0;
	}

	@Override
	protected int GetSdcardEventId()
	{
		return 0;
	}

	@Override
	protected int GetCloudEventId()
	{
		return EventID.BUSINESS_CLOUD_OK;
	}

	@Override
	protected String GetAdPosition()
	{
		return "boot,channel";
	}

	@Override
	protected String GetCloudCachePath(Context context)
	{
		return DownloadMgr.getInstance().BUSINESS_PATH + "/business.xxxx";
	}

	@Override
	protected AbsAdRes DecodeAdRes(JSONObject json)
	{
		AbsAdRes out;

		{
			out = new BootImgRes();
			if(!out.Decode(json))
			{
				out = null;
			}
		}
		if(out == null)
		{
			out = new ClickAdRes();
			if(!out.Decode(json))
			{
				out = null;
			}
		}

		return out;
	}

	protected void BuildBootNetArr(ArrayList<BootImgRes> dst, ArrayList<BootImgRes> src)
	{
		if(dst != null && src != null)
		{
			BootImgRes srcTemp;
			BootImgRes dstTemp;
			Class cls = BootImgRes.class;
			Field[] fields = cls.getDeclaredFields();
			int size = dst.size();
			for(int i = 0; i < size; i ++)
			{
				dstTemp = dst.get(i);
				srcTemp = GetItem(src, dstTemp.mAdId);
				if(srcTemp != null)
				{
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.mShowOk = srcTemp.mShowOk;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.mAdm = srcTemp.mAdm;
					dstTemp.mReplayBtn = srcTemp.mReplayBtn;

					for(int k = 0; k < fields.length; k++)
					{
						try
						{
							Object value = fields[k].get(dstTemp);
							fields[k].set(srcTemp, value);
						}
						catch(Exception e1)
						{
						}
					}
					dst.set(i, srcTemp);
				}
			}
		}
	}

	public static <T extends AbsAdRes> T GetItem(ArrayList<T> resArr, String id)
	{
		T out = null;

		int index = HasItem(resArr, id);
		if(index >= 0)
		{
			out = resArr.get(index);
		}

		return out;
	}

	public static <T extends AbsAdRes> int HasItem(ArrayList<T> resArr, String id)
	{
		int out = -1;

		if(resArr != null && id != null)
		{
			int len = resArr.size();
			for(int i = 0; i < len; i++)
			{
				if(id.equals(resArr.get(i).mAdId))
				{
					out = i;
					break;
				}
			}
		}

		return out;
	}

	protected void BuildClickNetArr(ArrayList<ClickAdRes> dst, ArrayList<ClickAdRes> src)
	{
		if(dst != null && src != null)
		{
			ClickAdRes srcTemp;
			ClickAdRes dstTemp;
			Class cls = ClickAdRes.class;
			Field[] fields = cls.getDeclaredFields();
			int size = dst.size();
			for(int i = 0; i < size; i ++)
			{
				dstTemp = dst.get(i);
				srcTemp = GetItem(src, dstTemp.mAdId);
				if(srcTemp != null)
				{
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.mShowOk = srcTemp.mShowOk;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.mAdm = srcTemp.mAdm;

					for(int k = 0; k < fields.length; k++)
					{
						try
						{
							Object value = fields[k].get(dstTemp);
							fields[k].set(srcTemp, value);
						}
						catch(Exception e1)
						{
						}
					}
					dst.set(i, srcTemp);
				}
			}
		}
	}

	protected EventCenter.OnEventListener s_lst = new EventCenter.OnEventListener()
	{
		@Override
		public void onEvent(int eventId, Object[] params)
		{
			if(EventID.BUSINESS_CLOUD_OK == eventId)
			{
				if(params != null && params.length > 0)
				{
					if(params[0] != null)
					{
						ArrayList<AdPackage> res = sync_ar_GetCloudCacheRes(MyFramework2App.getInstance().getApplicationContext(), null, mHolder);
						ArrayList<BootImgRes> bootTempArr = new ArrayList<>();
						getBootResArr(bootTempArr, res, false);
						BuildBootNetArr(bootTempArr, m_bootResArr);
						m_bootResArr = bootTempArr;

						ArrayList<ClickAdRes> clickTempArr = new ArrayList<>();
						getClickResArr(clickTempArr, res, false);
						BuildClickNetArr(clickTempArr, m_clickResArr);
						m_clickResArr = clickTempArr;
						getTJStr(res);
					}
				}
			}
		}
	};
}
