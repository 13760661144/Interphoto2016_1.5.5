package cn.poco.video.videoAlbum;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.video.model.VideoEntry;


/**
 * Created by Shine on 2017/5/15.
 */

public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final static int NORMAL = 1;
    private final static int CAMERA = 2;
    private Context mContext;
    private List<VideoEntry> mVideoList = new ArrayList<>();
    private HashMap<String, Integer> mJointOrders;
    private boolean isShowCamera = false;
    public VideoAdapter(Context context, List<VideoEntry> list)
    {
        mContext = context;
        if(list != null){
            mVideoList.clear();
            mVideoList.addAll(list);
        }
        mJointOrders = new HashMap<>();
    }

    public void updateList(List<VideoEntry> list)
    {
        if(list != null)
        {
            mVideoList.clear();
            if (isShowCamera)
            {
                mVideoList.add(null);
            }
            this.mVideoList.addAll(list);
            int minH = ShareData.m_screenHeight - ShareData.PxToDpi_xhdpi(180) - ShareData.PxToDpi_xhdpi(210);
            int itemH = (mVideoList.size() / 3 + 1) * ShareData.m_screenWidth / 3;
            if (itemH > minH)
            {
                //多出来的部分setBottomPadding
                mBottomPadding = itemH - minH;
                if (mBottomPadding > ShareData.PxToDpi_xhdpi(210))
                {
                    mBottomPadding = ShareData.PxToDpi_xhdpi(210);
                }
            }
            notifyDataSetChanged();
        }
    }
    //设置最后一个item的padding值
    private int mBottomPadding;


    public void clearSelectedItem()
    {
        mJointOrders.clear();
        notifyDataSetChanged();
    }
    public void setSelectVideos(ArrayList<VideoEntry> selectVideos)
    {
        for (int i = 0; i < selectVideos.size(); i++)
        {
            mJointOrders.put(selectVideos.get(i).mMediaPath,i+1);
        }
        notifyDataSetChanged();
    }


    public void cancelSelectItem(String uri)
    {
        if (mJointOrders.containsKey(uri))
        {
            int unSelectedOrder = mJointOrders.get(uri);
            mJointOrders.remove(uri);
            Iterator iterator = mJointOrders.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = (String) entry.getKey();
                int values = (int) entry.getValue();
                if (values > unSelectedOrder)
                {
                    mJointOrders.put(key, values - 1); //后面的全部减1
                }
            }
            notifyDataSetChanged();
        }
    }

    public void setSelectItem(String uri)
    {
        mJointOrders.put(uri, mJointOrders.size() + 1);
        int position = 0;
        for (int i = 0; i < mVideoList.size(); i++)
        {
            if(mVideoList.get(i) != null && mVideoList.get(i).mMediaPath.equals(uri) )
            {
                position = i;
            }
        }
        notifyItemChanged(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        RecyclerView.ViewHolder viewHolder = null;
        if(viewType == CAMERA)
        {
            ImageView logo = new ImageView(mContext){
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
                {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    int size = getMeasuredWidth();
                    setMeasuredDimension(size, size);
                }
            };
            logo.setBackgroundColor(0xff111111);
            logo.setScaleType(ImageView.ScaleType.CENTER);
            logo.setImageResource(R.drawable.homepage_camera_btn_out);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            logo.setLayoutParams(params);
            viewHolder = new CameraViewHolder(logo);
        }else{
            final VideoCell videoCell = new VideoCell(mContext);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            videoCell.setLayoutParams(params);
            viewHolder = new VideoViewHolder(videoCell);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        if (holder.itemView instanceof VideoCell && mVideoList.get(position) != null)
        {
            VideoEntry videoData = mVideoList.get(position);
            VideoCell currentCell = (VideoCell) holder.itemView;
            //后面的几个view设置padding增加高度
            int paddingNum = mVideoList.size() % 3;
            if (paddingNum == 0)
            {
                paddingNum = 3;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) currentCell.getLayoutParams();
            if (position >= mVideoList.size() - paddingNum)
            {
                params.bottomMargin = mBottomPadding;
            } else
            {
                params.bottomMargin = 0;
            }
            currentCell.setLayoutParams(params);
            currentCell.setVideoData(videoData);
            currentCell.setTag(position);
            currentCell.setOnClickListener(mOnClickListener);
            currentCell.getOrderView().setTag(position);
            currentCell.getOrderView().setOnClickListener(mSelectListener);
            if(mJointOrders.containsKey(videoData.mMediaPath))
            {
                currentCell.setJointOrder(mJointOrders.get(videoData.mMediaPath));
            }else{
                currentCell.setJointOrder(0);
            }
        }else{
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickListener);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(isShowCamera && position == 0){
            return CAMERA;
        }else{
            return NORMAL;
        }
    }

    public void setShowCamera(boolean showCamera)
    {
        isShowCamera = showCamera;
        if (isShowCamera)
        {
            mVideoList.add(0,null);
            notifyDataSetChanged();
        }
    }

    private View.OnClickListener mSelectListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v.getTag() != null){
                int position = (int) v.getTag();
                VideoEntry videoData = mVideoList.get(position);
                if(mOnItemListener != null){
                    boolean isSelected = mOnItemListener.onItemSelected(videoData,position);
                    if(isSelected){
//                        int size = mJointOrders.size() + 1;
//                        mJointOrders.put(videoData.mMediaPath,size);
//                        ((VideoCell)v).setJointOrder(size);
                        mJointOrders.put(videoData.mMediaPath,mJointOrders.size()+1);
                        notifyItemChanged(position);
                    }else{
                        if (mJointOrders.containsKey(videoData.mMediaPath))
                        {
                            int unSelectedOrder = mJointOrders.get(videoData.mMediaPath);
                            mJointOrders.remove(videoData.mMediaPath);
                            Iterator iterator = mJointOrders.entrySet().iterator();
                            while (iterator.hasNext())
                            {
                                Map.Entry entry = (Map.Entry) iterator.next();
                                String key = (String) entry.getKey();
                                int values = (int) entry.getValue();
                                if (values > unSelectedOrder)
                                {
                                    mJointOrders.put(key, values - 1); //后面的全部减1
                                }
                            }
                            notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v.getTag() != null){
                int position = (int) v.getTag();
                if(mOnItemListener != null){
                    if(position == 0 && isShowCamera)
                    {
                        mOnItemListener.onCamera();
                    }else{
                        mOnItemListener.onItemClick(mVideoList.get(position), position);
                    }
                }
            }
        }
    };


    @Override
    public int getItemCount()
    {
        return mVideoList.size();
    }


    private static class CameraViewHolder extends RecyclerView.ViewHolder
    {
        public CameraViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder
    {
        public VideoViewHolder(View itemView)
        {
            super(itemView);
        }
    }

    private OnItemListener mOnItemListener;

    public void setOnItemListener(OnItemListener onItemListener)
    {
        this.mOnItemListener = onItemListener;
    }

    public interface OnItemListener
    {
        void onCamera();

        void onItemClick(VideoEntry videoEntry, int position);

        /**
         *
         * @param videoEntry
         * @param position
         * @return  //返回是否选择
         */
        boolean onItemSelected(VideoEntry videoEntry, int position);
    }
}
