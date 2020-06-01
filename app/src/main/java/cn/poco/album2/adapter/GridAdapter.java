package cn.poco.album2.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.view.PhotoView;

/**
 * Created by: fwc
 * Date: 2017/8/2
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

	private Context mContext;

	private List<ImageStore.ImageInfo> mItems;

	public GridAdapter(Context context, List<ImageStore.ImageInfo> items) {
		mContext = context;
		mItems = items;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(new PhotoView(mContext));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		PhotoView photoView = (PhotoView)holder.itemView;
		String path = mItems.get(position).image;
		if (path.endsWith(".gif")) {
			Glide.with(mContext).load(path).asBitmap().dontAnimate().thumbnail(0.5f).into(photoView.getImageView());
		} else {
			Glide.with(mContext).load(path).dontAnimate().thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESULT).into(photoView.getImageView());
		}
	}

	@Override
	public int getItemCount() {
		return mItems == null ? 0 : mItems.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
		}
	}
}
