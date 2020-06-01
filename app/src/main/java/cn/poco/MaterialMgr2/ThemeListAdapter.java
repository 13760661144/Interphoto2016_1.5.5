package cn.poco.MaterialMgr2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Random;

import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.GlideImageLoader;

/**
 * Created by admin on 2016/9/7.
 */
public class ThemeListAdapter extends RecyclerView.Adapter<ThemeListAdapter.ViewHolder>
{
	private ArrayList<ThemeItemInfo> m_datas;
	private Context m_context;
	private int m_imgW;
	private int m_imgH;
	private int m_headW;
	private int m_headH;
	private int m_viewH;
	private ItemClickListener m_itemClickCB;
	private int[] bgs = new int[]{0xff2e2e2e, 0xff252525, 0xff373737, 0xff2d2d2d, 0xff2f2f2f};
	private ArrayList<ThemeItemInfo> m_cacheList = new ArrayList<>();
	private int m_maxLoadCount = 10;

	public ThemeListAdapter(Context context)
	{
		m_context = context;
		m_datas = new ArrayList<>();
		m_imgW = ShareData.m_screenWidth;
		m_imgH = m_imgW;
		m_headW = m_headH = ShareData.PxToDpi_xhdpi(80);
		m_viewH = ShareData.PxToDpi_xhdpi(380);
	}

	public void SetDatas(ArrayList<?> datas)
	{
		m_datas.clear();
		if(datas != null)
		{
			int len = datas.size();
			for(int i = 0; i < len; i++)
			{
				m_datas.add((ThemeItemInfo)datas.get(i));
			}
		}
	}

	public void SetOnItemClickListener(ItemClickListener lst)
	{
		m_itemClickCB = lst;
	}

	public void release()
	{
		if(m_resDownloadCb != null)
		{
			m_resDownloadCb.ClearAll();
			m_resDownloadCb = null;
		}
		if(m_cacheList != null)
		{
			m_cacheList.clear();
		}
		m_datas.clear();
		m_itemClickCB = null;
		notifyDataSetChanged();
		ClearCache();
	}

	public void ClearCache()
	{
		GlideImageLoader.Clear(m_context);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		ThemeItemView view = new ThemeItemView(m_context);
		view.m_imgW = m_imgW;
		view.m_imgH = m_imgH;
		view.m_viewW = m_imgW;
		view.m_viewH = m_viewH;
		view.m_headW = m_headW;
		view.m_headH = m_headH;
		view.InitUI();

		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		ThemeItemInfo info = m_datas.get(position);
		final ThemeItemView view = (ThemeItemView)holder.itemView;
		view.SetData(info,position);
		Random radom = new Random();
		int index = radom.nextInt(5);
		view.m_img.setBackgroundColor(bgs[index]);
		if(info.m_themeRes.m_type == BaseRes.TYPE_NETWORK_URL)
		{
			if(info.m_downloadId == -1)
			{
				info.m_downloadId = DownloadMgr.getInstance().DownloadRes(info.m_themeRes, m_resDownloadCb);
				addToCache(info);
			}
			view.m_img.setImageBitmap(null);
			view.m_headImg.setImageBitmap(null);
		}
		else
		{
			GlideImageLoader.LoadImg(view.m_img, m_context, info.m_themeRes.m_icon, false);
			GlideImageLoader.LoadCircleImg(view.m_headImg, m_context, info.m_themeRes.m_dashiIcon, ShareData.PxToDpi_xhdpi(2), false);
		}
	}

	private synchronized void addToCache(ThemeItemInfo item)
	{
		m_cacheList.add(item);
		if(m_cacheList.size() > m_maxLoadCount)
		{
			removeFromCache();
		}
	}

	private synchronized void removeFromCache()
	{
		if(m_cacheList.isEmpty() == false)
		{
			ThemeItemInfo info = m_cacheList.remove(0);
			if(info != null)
			{
				if(info.m_downloadId != -1)
				{
					DownloadMgr.getInstance().CancelDownload(info.m_downloadId);
					info.m_downloadId = -1;
				}
			}
		}
	}

	protected MgrUtils.MyDownloadCB m_resDownloadCb = new MgrUtils.MyDownloadCB(new MgrUtils.MyCB() {

		@Override
		public void OnGroupComplete(int downloadId, IDownload[] resArr) {
		}

		@Override
		public void OnFail(int downloadId, IDownload res) {
		}

		@Override
		public void OnGroupFail(int downloadId, IDownload[] resArr)
		{

		}

		@Override
		public void OnProgress(int downloadId, IDownload res, int progress)
		{
		}

		@Override
		public void OnComplete(int downloadId, IDownload res)
		{
			if(m_datas != null)
			{
				int size = m_datas.size();
				for(int i = 0; i < size; i ++)
				{
					if(m_datas.get(i).m_downloadId == downloadId)
					{
						notifyItemChanged(i);
					}
				}
			}
		}

		@Override
		public void OnGroupProgress(int downloadId, IDownload[] resArr, int progress)
		{
		}
	});

	@Override
	public int getItemCount()
	{
		return m_datas.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder
	{
		public ViewHolder(View itemView)
		{
			super(itemView);

			itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(m_itemClickCB != null)
					{
						int position = getAdapterPosition();
						if(position >= 0 && position < m_datas.size())
						{
							ThemeItemInfo itemInfo = m_datas.get(position);
							m_itemClickCB.onItemClick(v, itemInfo, position);
						}
					}
				}
			});

			if(itemView instanceof ThemeItemView)
			{
				ThemeItemView view = (ThemeItemView)itemView;
				view.m_stateText.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(m_itemClickCB != null)
						{
							int position = getAdapterPosition();
							ThemeItemInfo itemInfo = m_datas.get(position);
							m_itemClickCB.onItemClick(v, itemInfo, position);
						}
					}
				});
			}
		}
	}

	public static interface ItemClickListener
	{
		public void onItemClick(View v, ThemeItemInfo info, int position);

		public void OnStateClick(View v, ThemeItemInfo info, int position);
	}
}
