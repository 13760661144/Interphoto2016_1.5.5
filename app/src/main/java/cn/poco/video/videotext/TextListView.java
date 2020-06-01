package cn.poco.video.videotext;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.poco.draglistview.DragListItemInfo;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.ResourceUtils;
import cn.poco.resource.VideoTextRes;
import cn.poco.resource.VideoTextResMgr2;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.video.VideoResMgr;
import cn.poco.video.view.BaseBottomView;

/**
 * Created by lgd on 2018/1/3.
 */

abstract public class TextListView extends BaseBottomView
{
    public int m_curUri;
    private int mResListType = 1;
    public TextListView(@NonNull Context context,int listType)
    {
        super(context);
        mResListType = listType;
        initData();
        initView();
        m_listAdapter.showTitle(false);
    }

    protected List<DragListItemInfo> initItemList(int resListType)
    {
        this.mResListType = resListType;
        List<DragListItemInfo> listdatas = VideoResMgr.getVedioTextRess(getContext(), mResListType);
        return listdatas;
    }

    @Override
    protected List<DragListItemInfo> initItemList()
    {
        return initItemList(mResListType);
//        List<DragListItemInfo> listdatas = VideoResMgr.getVedioTextRess(getContext(), mResListType);
//        return listdatas;
    }

    @Override
    protected void OnHideItem(int position)
    {
        DragListItemInfo itemInfo = m_listDatas.get(position);
        if (itemInfo != null && itemInfo.m_ex instanceof VideoTextRes) {
            VideoTextRes res = (VideoTextRes)itemInfo.m_ex;
            if(res.m_type != BaseRes.TYPE_LOCAL_RES)
            {
                if(mResListType == 1)
                {
                    MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.视频水印);
                    MyBeautyStat.onClickByRes(R.string.视频水印_删除水印);
                }else{
                    MyBeautyStat.onDeleteMaterial(res.m_tjId + "", R.string.视频创意);
                    MyBeautyStat.onClickByRes(R.string.视频创意_删除创意);
                }
                m_listDatas.remove(position);
                m_listAdapter.notifyDataSetChanged();
                if (m_curUri == res.m_id) {
                    cancelSelect();
                }
                VideoTextResMgr2.getInstance().DeleteRes(getContext(), res);
            }
        }
    }

    protected void cancelSelect()
    {
        m_listAdapter.SetSelByIndex(-1);
        m_curUri = 0;
    }

    @Override
    protected void OnChangeItem(int fromPosition, int toPosition)
    {
        ArrayList<Integer> orders = VideoTextResMgr2.getInstance().GetOrderArr1().get(mResListType);
        DragListItemInfo itemInfo = m_listDatas.get(fromPosition);
        int fromPos = fromPosition;
        int toPos = toPosition;
        if(itemInfo != null)
        {
            fromPos = ResourceUtils.HasId(orders, itemInfo.m_uri);
        }
        itemInfo = m_listDatas.get(toPosition);
        if(itemInfo != null)
        {
            toPos = ResourceUtils.HasId(orders, itemInfo.m_uri);
        }
        ResourceUtils.ChangeOrderPosition(orders, fromPos, toPos);
        VideoTextResMgr2.getInstance().SaveOrderArr();
        if(m_listDatas != null && m_listDatas.size() > fromPosition && m_listDatas.size() > toPosition)
        {
            itemInfo = m_listDatas.remove(fromPosition);
            m_listDatas.add(toPosition, itemInfo);
        }
    }

    @Override
    protected boolean canDelete(int position)
    {
        DragListItemInfo itemInfo = m_listDatas.get(position);
        if (itemInfo != null)
        {
            BaseRes res = (BaseRes) itemInfo.m_ex;
            if (res.m_type != BaseRes.TYPE_LOCAL_RES)
            {
                return true;
            }
        }
        return false;
    }

    protected int setSelItemByUri(int uri)
    {
        int index = -1;
        if (m_listDatas != null)
        {
            index = m_listAdapter.GetIndexByUri(uri);
        }
        if (index >= 0)
        {
            m_curUri = uri;
            m_listAdapter.SetSelByUri(uri);
            m_resList.ScrollToCenter(index);
        } else
        {
            m_resList.ScrollToCenter(0);
        }
        return index;
    }

    protected DragListItemInfo getItemByUri(int uri)
    {
        DragListItemInfo info = null;
        int index = -1;
        if (m_listDatas != null)
        {
            index = m_listAdapter.GetIndexByUri(uri);
        }
        if (index >= 0)
        {
            info = m_listDatas.get(index);
        }
        return info;
    }

    @Override
    protected void onHeadClick(View v, DragListItemInfo info, int index)
    {
        super.onHeadClick(v, info, index);
    }


    protected boolean isContainsUri(int uri)
    {
        boolean b = false;
        for (int i = 0; i < m_listDatas.size(); i++)
        {
            if(m_listDatas.get(i).m_uri == uri){
                b = true;
                break;
            }
        }
        return b;
    }


    @Deprecated
    private void downRes(DragListItemInfo info, final boolean isUse)
    {
        info.m_style = DragListItemInfo.Style.LOADING;
        info.m_progress = 0;
        if (m_listAdapter != null)
        {
            m_listAdapter.notifyDataSetChanged();
        }
        DownloadMgr.getInstance().DownloadRes((IDownload) info.m_ex, new AbsDownloadMgr.Callback()
        {
            @Override
            public void OnProgress(int downloadId, IDownload res, int progress)
            {
                if (m_listAdapter != null)
                {
                    m_listAdapter.SetItemProgress(((VideoTextRes) res).m_id, progress);
                }
            }

            @Override
            public void OnComplete(int downloadId, IDownload res)
            {
                if (m_listAdapter != null)
                {
                    m_listAdapter.SetItemStyleByUri(((VideoTextRes) res).m_id, DragListItemInfo.Style.NORMAL);
                    if (isUse)
                    {
//                        setSelItemByUri(((VideoTextRes) res).m_id, true);
                    }
                }
            }

            @Override
            public void OnFail(int downloadId, IDownload res)
            {
                if (m_listAdapter != null)
                {
                    Toast.makeText(getContext(), getResources().getString(R.string.Ooops), Toast.LENGTH_SHORT).show();
                    m_listAdapter.SetItemStyleByUri(((VideoTextRes) res).m_id, DragListItemInfo.Style.NEED_DOWNLOAD);
                }
            }
        });
    }
}
