package cn.poco.video.sequenceMosaics;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.poco.draglistview.DragItem;
import cn.poco.draglistview.DragItemAdapter;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.GlideImageLoader;
import cn.poco.video.utils.FileUtils;

/**
 * Created by admin on 2017/10/18.
 */

public class VideoListAdapter extends DragItemAdapter<AdapterDataInfo, VideoListAdapter.ViewHolder>
{
	private long MAX_TIME = 3 * 60 * 1000 - 1000;
	private OnItemClickListener mItemClickListener;
	private Context mContext;
	private ArrayList<VideoInfo> mVideos;
	private ArrayList<TransitionDataInfo> mTrans;
	private boolean mShowIcon = true;
	private int mNormalPad = ShareData.PxToDpi_xhdpi(10);
	private int mHeadPad = ShareData.PxToDpi_xhdpi(30);
	private ExecutorService mExecutor;
	private MediaMetadataRetriever mMetadataRetriever;
	public VideoListAdapter(Context context)
	{
		super(true);
		mContext = context;
		setHasStableIds(true);
		mExecutor = Executors.newSingleThreadExecutor();
		mMetadataRetriever = new MediaMetadataRetriever();
	}

	public void SetOnItemClickListener(OnItemClickListener lis)
	{
		mItemClickListener = lis;
	}

	public void SetItemDatas(ArrayList<VideoInfo> videos, ArrayList<TransitionDataInfo> trans)
	{
		mVideos = videos;
		mTrans = trans;
		TransFormToAdapterInfo();
	}

	private void TransFormToAdapterInfo()
	{
		mItemList.clear();
		if(mVideos != null)
		{
			int index = 0;
			int transSize = 0;
			if(mTrans != null)
			{
				transSize = mTrans.size();
			}
			for(VideoInfo info : mVideos)
			{
				AdapterDataInfo data = new AdapterDataInfo();
				data.mEx = info;
				data.mThumbPath = info.mPath;
				data.mUri = info.mUri;
				if(mTrans != null && index < transSize)
				{
					data.mTrans = mTrans.get(index);
					data.mIcon = data.mTrans.mRes;
				}
				else
				{
					TransitionDataInfo info1 = new TransitionDataInfo();
					data.mTrans = info1;
					data.mIcon = data.mTrans.mRes;
				}
				mItemList.add(data);
				index ++;
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

	@Override
	public VideoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		VideoItem item = new VideoItem(mContext);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		item.setLayoutParams(params);
		VideoListAdapter.ViewHolder holder = new VideoListAdapter.ViewHolder(item, item.mThumb);
		return holder;
	}

	@Override
	public void onBindViewHolder(VideoListAdapter.ViewHolder holder, final int position)
	{
		super.onBindViewHolder(holder, position);
		AdapterDataInfo info = mItemList.get(position);
		((VideoItem)holder.itemView).mIcon.setVisibility(View.VISIBLE);
		int size = mItemList.size();
		if(position == size - 1){
			long totalTime = 0;
			int type = VideoEditTimeId.TYPE_FREE;
			VideoInfo videoInfo;
			for(AdapterDataInfo dataInfo : mItemList){
				videoInfo = (VideoInfo)dataInfo.mEx;
				type = videoInfo.mTimeType;
				totalTime += videoInfo.GetEndTime() - videoInfo.GetStartTime();
			}
			long maxTime = MAX_TIME;
			if(type != VideoEditTimeId.TYPE_FREE)
			{
				maxTime = 9000;
			}
			if(size == 10 || totalTime >= maxTime){
				((VideoItem)holder.itemView).mIcon.setVisibility(View.GONE);
			}
		}

		if(position == 0 && getItemCount() > 4)
		{
			holder.itemView.setPadding(mHeadPad, 0, mNormalPad, 0);
		}
		else
		{
			holder.itemView.setPadding(mNormalPad, 0, mNormalPad, 0);
		}
//		Uri uri = Uri.fromFile(new File(info.mThumbPath));
		long startTime = 0;
		if(info.mEx instanceof VideoInfo){
			startTime = ((VideoInfo)info.mEx).GetStartTime();
		}
		String dir = FileUtils.getVideoFrameDir(info.mThumbPath);
		String url = dir + "/thumb_" + startTime + ".img";
		if(!FileUtil.isFileExists(url))
		{
			if(mExecutor != null)
			{
				mExecutor.submit(new ThumbRunnable(mContext, mMetadataRetriever, info.mThumbPath, position, startTime, url, new ThumbRunnable.ThumbCallback()
				{
					@Override
					public void onComplete(int positon)
					{
						notifyItemChanged(position);
					}
				}));
			}
		}
		else {
			GlideImageLoader.LoadImg(((VideoItem)holder.itemView).mThumb, mContext, url, false);
		}

		if(position == size - 1){
			GlideImageLoader.LoadImg(((VideoItem)holder.itemView).mIcon, mContext, info.mAddIcon, false);
		}else {
			GlideImageLoader.LoadImg(((VideoItem)holder.itemView).mIcon, mContext, info.mIcon, false);
		}
		((VideoItem)holder.itemView).setmIconVisible(mShowIcon);
	}

	@Override
	public long getItemId(int position)
	{
		return mItemList.get(position).mUri;
	}

	public static interface OnItemClickListener
	{
		public void OnItemClick(View view, AdapterDataInfo info, int index);

		public void OnIconClick(View view, AdapterDataInfo info, int index);
	}

	protected class ViewHolder extends DragItemAdapter.ViewHolder
	{
		public ViewHolder(View itemView, View grabView)
		{
			super(itemView, grabView);
			if(itemView instanceof VideoItem)
			{
				((VideoItem)itemView).mIcon.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(mItemClickListener != null)
						{
							int position = getAdapterPosition();
							AdapterDataInfo info = mItemList.get(position);

							mItemClickListener.OnIconClick(v, info, position);
						}
					}
				});
				((VideoItem)itemView).mThumb.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(mItemClickListener != null)
						{
							int position = getAdapterPosition();
							AdapterDataInfo info = mItemList.get(position);

							mItemClickListener.OnItemClick(v, info, position);
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
			if(clickedView instanceof VideoItem)
			{
				VideoItem clickItem = (VideoItem)clickedView;
				Bitmap bitmap = Bitmap.createBitmap(clickItem.mThumb.getWidth(), clickItem.mThumb.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				clickItem.mThumb.draw(canvas);
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
			VideoItem showView;
			if(clickedView instanceof VideoItem)
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
