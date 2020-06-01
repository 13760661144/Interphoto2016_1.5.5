package cn.poco.video.timeline2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/11/16
 */
public class VideoFrameAdapter extends RecyclerView.Adapter<VideoFrameAdapter.ViewHolder> {

	private Context mContext;
	private List<String> mItems;

	private int mFrameWidth;

	public VideoFrameAdapter(Context context, List<String> items) {
		mContext = context;
		mItems = items;

		mFrameWidth = ShareData.PxToDpi_xhdpi(40);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ImageView imageView = new ImageView(parent.getContext());
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(mFrameWidth, ViewGroup.LayoutParams.MATCH_PARENT);
		imageView.setLayoutParams(params);
		return new ViewHolder(imageView);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		String path = mItems.get(position);
		ImageView imageView = (ImageView)holder.itemView;
		if (TimelineTask.EMPTY_PATH.equals(path)) {
			imageView.setVisibility(View.INVISIBLE);
		} else {
			imageView.setVisibility(View.VISIBLE);
			Glide.with(mContext).load(path).into(imageView);
		}
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
		}
	}
}
