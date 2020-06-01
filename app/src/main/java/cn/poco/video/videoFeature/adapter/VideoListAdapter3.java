package cn.poco.video.videoFeature.adapter;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.poco.draglistview.DragItem;
import cn.poco.draglistview.DragItemAdapter;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.GlideImageLoader;
import cn.poco.utils.ImageUtil;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.page.ProcessMode;
import cn.poco.video.sequenceMosaics.AdapterDataInfo;
import cn.poco.video.sequenceMosaics.ThumbRunnable;
import cn.poco.video.sequenceMosaics.TransitionDataInfo;
import cn.poco.video.sequenceMosaics.VideoEditTimeId;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.videoFeature.cell.VideoCoverCell;

/**
 * Created by Simon Meng on 2018/1/12.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class VideoListAdapter3 extends DragItemAdapter<AdapterDataInfo, VideoListAdapter3.ViewHolder> {
    private final static int HEADER = 0;
    private final static int NORMAL = 1;
    private final static int FOOTER = 2;

    private long MAX_TIME = 3 * 60 * 1000 - 1000;
    private OnItemClickListener mItemClickListener;
    private Context mContext;
    private ArrayList<VideoInfo> mVideosWithPlaceHolder = new ArrayList<>();
    private boolean mShowIcon = true;
    public int mNormalPad = ShareData.PxToDpi_xhdpi(10);
    // 1.5.5版本去除了padding;
    private int mPaddingVertical = ShareData.PxToDpi_xhdpi(2);
    private ExecutorService mExecutor;
    private MediaMetadataRetriever mMetadataRetriever;
    public HashMap<Integer, String> mMapIndexToBitmapPath = new HashMap<>();
    private boolean mShowAddIcon = true;

    public VideoListAdapter3(Context context)
    {
        super(true);
        mContext = context;
        setHasStableIds(true);
        mExecutor = Executors.newSingleThreadExecutor();
        mMetadataRetriever = new MediaMetadataRetriever();
    }

    public void showAddIcon(boolean show)
    {
        mShowAddIcon = show;
    }


    public void SetOnItemClickListener(OnItemClickListener lis)
    {
        mItemClickListener = lis;
    }

    public void SetItemDatas(ArrayList<VideoInfo> videos)
    {
        VideoInfo headerForScroll = new VideoInfo();
        VideoInfo footerForScroll = new VideoInfo();
        mVideosWithPlaceHolder.clear();
        mVideosWithPlaceHolder.addAll(videos);
        mVideosWithPlaceHolder.add(0, headerForScroll);
        mVideosWithPlaceHolder.add(footerForScroll);
        TransFormToAdapterInfo();
    }

    private void TransFormToAdapterInfo()
    {
        mItemList.clear();
        if(mVideosWithPlaceHolder != null)
        {
            for(VideoInfo info : mVideosWithPlaceHolder)
            {
                AdapterDataInfo data = new AdapterDataInfo();
                data.mEx = info;
//                data.mThumbPath = info.mPath;
                data.mThumbPath = info.getVideoPath(ProcessMode.Normal);
                data.mUri = info.mUri;
                TransitionDataInfo info1 = new TransitionDataInfo();
                data.mTrans = info1;
                data.mIcon = data.mTrans.mRes;
                data.mDuration = info.GetClipTime();
                mItemList.add(data);
            }
        }
    }



    public boolean CanDrag(int pos)
    {
        return true;
    }

    public boolean CanDrop(int pos)
    {
        return CanDrag(pos);
    }

    public void GetVideosData(ArrayList<VideoInfo> datas, ArrayList<TransitionDataInfo> trans)
    {
        if(mItemList != null && datas != null)
        {
            int size = mItemList.size();
            int index = 0;
            for(AdapterDataInfo info : mItemList)
            {
                datas.add((VideoInfo)info.mEx);
                if(index < size){
                    trans.add(info.mTrans);
                }
            }
        }
    }

    public void UpdateTransResData(int index, TransitionDataInfo tran)
    {
        if(mItemList != null && index >= 0 && index < mItemList.size())
        {
            AdapterDataInfo data = mItemList.get(index);
            data.mIcon = tran.mRes;
            data.mTrans = tran;
            notifyItemChanged(index);
        }
    }

    /**
     * 部分条件不允许显示添加按钮
     * @return
     */
    public boolean checkShowAdd()
    {
        boolean flag = false;
        if(mItemList != null)
        {
            int size = mItemList.size();
            long totalTime = 0;
            int type = VideoEditTimeId.TYPE_FREE;
            VideoInfo videoInfo;
            for(AdapterDataInfo dataInfo : mItemList){
                videoInfo = (VideoInfo)dataInfo.mEx;
                type = videoInfo.mTimeType;
                totalTime += videoInfo.GetClipTime();
            }
            long maxTime = MAX_TIME;
            if(type != VideoEditTimeId.TYPE_FREE)
            {
                maxTime = 9000;
            }
            if(size == 12 || totalTime >= maxTime){
                flag = false;
            }
            else {
                flag = true;
            }
        }
        return flag;
    }

    public Object removeItemByKey(int key)
    {
        int index = 0;
        for(AdapterDataInfo info : mItemList)
        {
            if(((VideoInfo)info.mEx).mUri == key)
            {
                return removeItem(index);
            }
            index ++;
        }
        return null;
    }

    @Override
    public Object removeItem(int pos)
    {
        boolean flag = false;
        if(mItemList != null && pos == mItemList.size() - 1)
        {
            flag = true;
        }
        Object object = super.removeItem(pos);
        if(flag && mItemList != null && mItemList.size() > 0)
        {
            AdapterDataInfo info = mItemList.get(mItemList.size() - 1);
            TransitionDataInfo tran = new TransitionDataInfo();
            info.mTrans = tran;
            notifyItemRemoved(mItemList.size() - 1);
        }
        return object;
    }

    @Override
    public void changeItemPosition(int fromPos, int toPos)
    {
        super.changeItemPosition(fromPos, toPos);
    }

    public void ShowIcon(boolean show)
    {
        mShowIcon = show;
    }

    public void ChangeLastIndexTrans()
    {
        if(mItemList != null)
        {
            int size = mItemList.size();
            TransitionDataInfo tran = new TransitionDataInfo();
            mItemList.get(size - 1).mTrans = tran;
            mItemList.get(size - 1).mIcon = tran.mRes;
        }
    }

    public int getVisibleWidth() {

        int size = mItemList.size() - 2;
        int width = mNormalPad + size * ShareData.PxToDpi_xhdpi(170) + (size - 1) * mNormalPad * 2;
        return width;
    }

    public int getHeadPlaceHoldelrWidth() {
        return mHeadWidth;
    }

    private int mHeadWidth;
    private View mFooterView;
    @Override
    public VideoListAdapter3.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        ViewGroup.LayoutParams params;
        ViewHolder viewHolder;
        if (viewType == HEADER) {
            View headerView = new View(parent.getContext());
            headerView.setTag("header");
            headerView.setClickable(false);
            mHeadWidth = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(6)) / 2 + ShareData.PxToDpi_xhdpi(6) - mNormalPad;
            params = new ViewGroup.LayoutParams(mHeadWidth, ShareData.PxToDpi_xhdpi(160));
            headerView.setLayoutParams(params);
            viewHolder = new ViewHolder(headerView, headerView);
            return viewHolder;
        } else if (viewType == FOOTER) {
            ImageView footerView = new ImageView(parent.getContext());
            mFooterView = footerView;
            footerView.setTag("footer");
            footerView.setClickable(false);
//            params = new ViewGroup.LayoutParams(ShareData.m_screenWidth / 2 - ShareData.PxToDpi_xhdpi(80), ShareData.PxToDpi_xhdpi(160));
//            footerView.setLayoutParams(params);
            viewHolder = new ViewHolder(footerView, footerView);
            return viewHolder;
        } else {
            VideoCoverCell videoCell = new VideoCoverCell(parent.getContext());
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            videoCell.setLayoutParams(params);
            viewHolder = new ViewHolder(videoCell, videoCell);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(VideoListAdapter3.ViewHolder holder, final int position)
    {
        super.onBindViewHolder(holder, position);
        AdapterDataInfo info = mItemList.get(position);
        int size = mItemList.size();
        boolean flag = true;
        if(position == size - 2 || position == size - 1){
            flag =  checkShowAdd();
            if(!flag && position == size - 2){
                ((VideoCoverCell)holder.itemView).setmIconVisible(false);
            }
        }

        if (!(position == 0 || position == mItemList.size() - 1)) {
            //必须设置tag为position，判断用
            holder.itemView.setTag(position);
            if(flag)
            {
                if(!mShowAddIcon)
                {
                    ((VideoCoverCell)holder.itemView).setmIconVisible(false);
                }
                else
                {
                    if (position == size - 2) {
                        ((VideoCoverCell) holder.itemView).setmIconVisible(true);
                    } else {
                        ((VideoCoverCell)holder.itemView).setmIconVisible(false);
                    }
                }
            }
            if (position == 1) {
                holder.itemView.setPadding(mNormalPad, mPaddingVertical, mNormalPad, mPaddingVertical);
            } else if (position == mItemList.size() - 2){
                holder.itemView.setPadding(mNormalPad, mPaddingVertical, 0, mPaddingVertical);
            } else {
                holder.itemView.setPadding(mNormalPad, 0, mNormalPad, 0);
            }
            long startTime = 0;
//            ((VideoCoverCell) holder.itemView).getCover().setImageBitmap(null);
            /*if (info.mEx instanceof VideoInfo) {
                startTime = ((VideoInfo) info.mEx).GetStartTime();
            }*/
            String dir = FileUtils.getVideoFrameDir(info.mThumbPath);
            String url = dir + "/thumb_" + startTime + ".img";
            if (!FileUtil.isFileExists(url)) {
                if (mExecutor != null) {
                    mExecutor.submit(new ThumbRunnable(mContext, mMetadataRetriever, info.mThumbPath, position, startTime, url, new ThumbRunnable.ThumbCallback() {
                        @Override
                        public void onComplete(int positon) {
                            notifyItemChanged(position);
                        }
                    }));
                }
            } else {
                GlideImageLoader.LoadImg(((VideoCoverCell) holder.itemView).getCover(), mContext, url, false);
            }
            mMapIndexToBitmapPath.put(position - 1, url);
            if (position == size - 2) {
                GlideImageLoader.LoadImg(((VideoCoverCell) holder.itemView).getAddBtn(), mContext, info.mAddIcon, false);
            } else {
                GlideImageLoader.LoadImg(((VideoCoverCell)holder.itemView).getAddBtn(), mContext, info.mIcon, false);
            }
            ((VideoCoverCell)holder.itemView).setSelectedEffect(info.mSelected);
            ((VideoCoverCell)holder.itemView).getDurationView().setText(TimeFormatter.makeVideoProgressLayoutDurationText(info.mDuration));
        }
        if(position == size - 1)
        {
            if (!mShowAddIcon || !flag) {
                Bitmap bmp = ImageUtil.createBitmapByColor(0x00ffffff, ShareData.m_screenWidth / 2, ShareData.PxToDpi_xhdpi(160));
                ((ImageView)holder.itemView).setImageBitmap(bmp);
            } else {
                Bitmap bmp = ImageUtil.createBitmapByColor(0x00ffffff, ShareData.m_screenWidth / 2 - ShareData.PxToDpi_xhdpi(80), ShareData.PxToDpi_xhdpi(160));
                ((ImageView)holder.itemView).setImageBitmap(bmp);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADER;
        } else if (position == mItemList.size() - 1){
            return FOOTER;
        } else {
            return NORMAL;
        }
    }

    @Override
    public long getItemId(int position)
    {
        return mItemList.get(position).mUri;
    }

    public interface OnItemClickListener
    {
        void OnItemClick(View view, AdapterDataInfo info, int index);

        void OnIconClick(View view, AdapterDataInfo info, int index);
    }

    protected class ViewHolder extends DragItemAdapter.ViewHolder
    {
        public ViewHolder(View itemView, View grabView)
        {
            super(itemView, grabView);
            if(itemView instanceof VideoCoverCell)
            {
                ((VideoCoverCell)itemView).getAddBtn().setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(mItemClickListener != null)
                        {
                            int position = getAdapterPosition();
                            if(position >= 0 && position < mItemList.size())
                            {
                                AdapterDataInfo info = mItemList.get(position);
                                mItemClickListener.OnIconClick(v, info, position);
                            }
                        }
                    }
                });
                ((VideoCoverCell)itemView).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if(mItemClickListener != null)
                        {
                            int position = getAdapterPosition();
                            if(position >= 0 && position < mItemList.size())
                            {
                                AdapterDataInfo info = mItemList.get(position);
                                mItemClickListener.OnItemClick(v, info, position);
                            }

                        }
                    }
                });
            }
        }

        @Override
        public void onItemClicked(View view)
        {
            super.onItemClicked(view);
        }

        @Override
        public boolean onItemLongClicked(View view)
        {
            Vibrator vib = (Vibrator)mContext.getSystemService(Service.VIBRATOR_SERVICE);
            if(vib != null)
            {
                vib.vibrate(30);
            }
            return super.onItemLongClicked(view);
        }

        @Override
        public boolean onItemTouch(View view, MotionEvent event) {
            return true;
        }
    }

    public void release()
    {
        if(mExecutor != null){
            mExecutor.shutdownNow();
        }
        if(mMetadataRetriever != null){
            mMetadataRetriever.release();
        }
    }

    public static class VideoDragItem extends DragItem {

        public VideoDragItem(Context context)
        {
            super(context);
            mDragView = new ImageView(context);
            ((ImageView)mDragView).setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        protected boolean mAlphaAnim = false;

        public void DoAlphaAnim(boolean flag)
        {
            mAlphaAnim = flag;
        }

        public VideoDragItem(Context context, View view)
        {
            super(context, view);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            if(clickedView instanceof VideoCoverCell)
            {
                VideoCoverCell clickItem = (VideoCoverCell)clickedView;
                Bitmap bitmap = Bitmap.createBitmap(clickItem.getCover().getWidth(), clickItem.getCover().getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                clickItem.getCover().draw(canvas);
                if(dragView instanceof ImageView){
                    ((ImageView)mDragView).setImageBitmap(bitmap);
                }
                else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        dragView.setBackground(new BitmapDrawable(clickedView.getResources(), bitmap));
                    } else {
                        //noinspection deprecation
                        dragView.setBackgroundDrawable(new BitmapDrawable(clickedView.getResources(), bitmap));
                    }
                }
            }
        }

        @Override
        public void onMeasureDragView(View clickedView, View dragView)
        {
            VideoCoverCell showView;
            if(clickedView instanceof VideoCoverCell)
            {
//				showView = ((VideoItem)clickedView).mThumb;
//				showView = ((VideoItem)clickedView);
                int width = ShareData.PxToDpi_xhdpi(148);
                int height = width;
                dragView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
                int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                dragView.measure(widthSpec, heightSpec);
            }
        }

        @Override
        protected void endDrag(View parent, View endToView, AnimatorListenerAdapter listener)
        {
            if(mAlphaAnim)
            {
                if(mDragView != null)
                {
                    PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 0f);
                    ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mDragView, alpha);
                    anim.setInterpolator(new DecelerateInterpolator());
                    anim.setDuration(ANIMATION_DURATION);
                    anim.addListener(listener);
                    anim.start();
                }
            }
            else
            {
                super.endDrag(parent, endToView, listener);
            }
        }

        @Override
        public void onStartDragAnimation(View dragView)
        {
            dragView.setAlpha(1f);
        }
    }
}
