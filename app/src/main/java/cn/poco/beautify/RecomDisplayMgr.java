package cn.poco.beautify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseResp;

import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.LockRes;
import cn.poco.resource.ResType;
import cn.poco.share.SendWXAPI;
import cn.poco.share.SharePage;
import cn.poco.system.TagMgr;
import cn.poco.system.Tags;
import cn.poco.tianutils.MakeBmp;
import cn.poco.utils.Utils;

public class RecomDisplayMgr
{
	protected RecomDisplayUI m_view;

	protected BaseRes m_res;
	protected int m_resType;

	protected MyDownloadCallback m_dlcb;
	protected MyWXCallback m_wxcb;
	protected RecomDisplayUI.Callback m_recomcb;
	protected RecomDisplayMgr.Callback m_cb;

	protected boolean m_recycle = false;

	public static interface Callback
	{
		public void UnlockSuccess(BaseRes res);

		public void OnCloseBtn();

		public void OnBtn(int state);

		public void OnClose();
	}

	public RecomDisplayMgr(Context context, BaseRes res, int resType, RecomDisplayMgr.Callback cb)
	{
		m_res = res;
		m_resType = resType;
		m_cb = cb;
		m_dlcb = new MyDownloadCallback(this, m_res.m_id, m_resType);
		m_wxcb = new MyWXCallback(this, m_res.m_id, m_resType);
		m_recomcb = new RecomDisplayUI.Callback()
		{
			@Override
			public void OnCloseBtn()
			{
				OnCancel();

				if(m_cb != null)
				{
					m_cb.OnCloseBtn();
				}
			}

			@Override
			public void OnBtn(int state)
			{
				switch(state)
				{
					case RecomDisplayUI.BTN_STATE_UNLOCK:
					{
						if(m_res instanceof LockRes)
						{
							switch(((LockRes)m_res).m_shareType)
							{
								case LockRes.SHARE_TYPE_MARKET:
								{
									OpenMarket(m_resType, m_res.m_id);
									if(m_view != null)
									{
										m_view.SetBtnState(RecomDisplayUI.BTN_STATE_DOWNLOAD);
									}
									if(m_recomcb != null)
									{
										m_recomcb.OnBtn(RecomDisplayUI.BTN_STATE_DOWNLOAD);
									}
									if(m_cb != null)
									{
										m_cb.UnlockSuccess(m_res);
									}
									break;
								}

								case LockRes.SHARE_TYPE_WEIXIN:
								{
									String url = null;
									if(((LockRes)m_res).m_shareLink != null && ((LockRes)m_res).m_shareLink.length() > 0)
									{
										url = ((LockRes)m_res).m_shareLink;
									}
									SharePage.unlockResourceByWeiXin(MyFramework2App.getInstance().getApplicationContext(), ((LockRes)m_res).m_shareContent, null, url, MakeWXLogo(((LockRes)m_res).m_shareImg), false, m_wxcb);
									break;
								}

								default:
									break;
							}
						}
						break;
					}

					case RecomDisplayUI.BTN_STATE_DOWNLOAD:
					{
						if(m_res != null)
						{
							if(m_resType == ResType.TEXT.GetValue())
							{

							}
							else
							{
								OnCancel();
							}
						}
						break;
					}

					default:
						break;
				}

				if(m_cb != null)
				{
					m_cb.OnBtn(state);
				}
			}

			@Override
			public void OnClose()
			{
				ClearAll();
				m_recycle = true;
				if(m_cb != null)
				{
					m_cb.OnClose();
				}
			}
		};

		m_view = new RecomDisplayUI((Activity)context, m_recomcb);
	}

	public void Create()
	{
		if(m_view != null)
		{
			m_view.CreateUI();
			m_view.SetImg(null);
			if(m_res != null)
			{
				if(m_res.m_type == BaseRes.TYPE_NETWORK_URL)
				{
					//下载资源
					m_view.SetImgState(RecomDisplayUI.IMG_STATE_LOADING);

					DownloadMgr.getInstance().DownloadRes(m_res, m_dlcb);
				}
				else
				{
					m_view.SetImgState(RecomDisplayUI.IMG_STATE_COMPLETE);

					if(m_res instanceof LockRes)
					{
						m_view.SetImg(((LockRes)m_res).m_showImg);
					}
				}
			}
			else
			{
				m_view.SetImgState(RecomDisplayUI.IMG_STATE_LOADING);
			}
			if(m_res instanceof LockRes)
			{
				switch(((LockRes)m_res).m_shareType)
				{
					case LockRes.SHARE_TYPE_MARKET:
					case LockRes.SHARE_TYPE_WEIXIN:
						m_view.SetBtnState(RecomDisplayUI.BTN_STATE_UNLOCK);
						break;

					default:
						m_view.SetBtnState(RecomDisplayUI.BTN_STATE_DOWNLOAD);
						break;
				}
			}

			if(m_view != null)
			{
				if(m_res instanceof LockRes)
				{
					m_view.SetContent(m_res.m_name, ((LockRes)m_res).m_showContent);
				}
			}
		}
	}

	public void Show(FrameLayout fr)
	{
		if(m_view != null && !IsShow())
		{
			m_view.Show(fr);
		}
	}

	public boolean IsShow()
	{
		boolean out = false;

		if(m_view != null)
		{
			out = m_view.IsShow();
		}

		return out;
	}

	public boolean IsRecycle()
	{
		return m_recycle;
	}

	public void OnCancel()
	{
		if(m_view != null)
		{
			m_view.OnCancel();
		}
	}

	/**
	 * LockRes和RecommendRes用单个下载，资源素材用数组下载，不然有BUG
	 */
	protected static class MyDownloadCallback implements DownloadMgr.Callback2
	{
		public RecomDisplayMgr m_thiz;
		public int m_themeID;
		public int m_type;

		public MyDownloadCallback(RecomDisplayMgr thiz, int themeID, int type)
		{
			m_thiz = thiz;
			m_themeID = themeID;
			m_type = type;
		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(res instanceof LockRes)
			{
				if(m_thiz != null)
				{
					m_thiz.m_view.SetImgState(RecomDisplayUI.IMG_STATE_COMPLETE);
					m_thiz.m_view.SetImg(((LockRes)res).m_showImg);
				}
			}
		}

		@Override
		public void OnFail(int downloadId, IDownload res)
		{
			// TODO Auto-generated method stub
		}

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr)
		{
			//添加到显示列表和new
			if(m_type == ResType.TEXT.GetValue())
			{
				//清理推荐
				ClearTextLockFlag(m_themeID);
			}

			if(m_thiz != null && m_thiz.m_view != null)
			{
				m_thiz.m_view.OnCancel();
			}
		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{
			if(m_thiz != null && m_thiz.m_view != null)
			{
				m_thiz.m_view.SetBtnState(RecomDisplayUI.BTN_STATE_DOWNLOAD);
			}
		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{
			// TODO Auto-generated method stub
		}

		public void ClearAll()
		{
			m_thiz = null;
		}
	}

	protected static class MyWXCallback implements SendWXAPI.WXCallListener
	{
		public RecomDisplayMgr m_thiz;
		public int m_themeID;
		public int m_type;

		public MyWXCallback(RecomDisplayMgr thiz, int themeID, int type)
		{
			m_thiz = thiz;
			m_themeID = themeID;
			m_type = type;
		}

		@Override
		public void onCallFinish(int result)
		{
			if(result != BaseResp.ErrCode.ERR_USER_CANCEL)
			{
				if(m_type == ResType.TEXT.GetValue())
				{
					ClearTextLockFlag(m_themeID);
				}

				if(m_thiz != null && m_thiz.m_view != null)
				{
					m_thiz.m_view.SetBtnState(RecomDisplayUI.BTN_STATE_DOWNLOAD);
					if(m_thiz.m_recomcb != null)
					{
						m_thiz.m_recomcb.OnBtn(RecomDisplayUI.BTN_STATE_DOWNLOAD);
					}
					if(m_thiz.m_cb != null)
					{
						m_thiz.m_cb.UnlockSuccess(m_thiz.m_res);
					}
				}
			}
		}

		public void ClearAll()
		{
			m_thiz = null;
		}
	}

	/**
	 * 清理主题锁
	 * 
	 * @param themeID
	 */
	public static void ClearTextLockFlag(int themeID)
	{
		TagMgr.SetTag(MyFramework2App.getInstance().getApplicationContext(), Tags.TEXT_UNLOCK + themeID);
	}

	public static void OpenMarket(int resType, int themeID)
	{
		try
		{
			Uri uri = Uri.parse("market://details?id=" + MyFramework2App.getInstance().getApplicationContext().getPackageName());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MyFramework2App.getInstance().getApplicationContext().startActivity(intent);
		}
		catch(Throwable e)
		{
			Toast.makeText(MyFramework2App.getInstance().getApplicationContext(), MyFramework2App.getInstance().getApplicationContext().getResources().getString(R.string.installplayStoreTips), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		if(resType == ResType.TEXT.GetValue())
		{
			ClearTextLockFlag(themeID);
		}
	}

	public static Bitmap MakeWXLogo(Object res)
	{
		Bitmap out = null;

		if(res != null)
		{
			if(res instanceof Bitmap)
			{
				out = (Bitmap)res;
			}
			else
			{
				out = Utils.DecodeImage(MyFramework2App.getInstance().getApplicationContext(), res, 0, -1, -1, -1);
			}
		}
		if(out == null)
		{
			out = BitmapFactory.decodeResource(MyFramework2App.getInstance().getApplicationContext().getResources(), R.mipmap.ic_launcher);
		}
		if(out != null)
		{
			if(out.getWidth() > 150 || out.getHeight() > 150)
			{
				out = MakeBmp.CreateBitmap(out, 150, 150, -1, 0, Config.ARGB_8888);
			}
		}

		return out;
	}

	public void ClearAll()
	{
		if(m_dlcb != null)
		{
			m_dlcb.ClearAll();
			m_dlcb = null;
		}
		if(m_wxcb != null)
		{
			m_wxcb.ClearAll();
			m_wxcb = null;
		}

		if(m_view != null)
		{
			m_view.ClearAll();
			m_view = null;
		}
	}
}
