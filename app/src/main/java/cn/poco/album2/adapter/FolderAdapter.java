package cn.poco.album2.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import cn.poco.album2.model.FolderInfo;
import cn.poco.album2.view.FolderItem;
import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

	private static final int COLOR_GRAY = 0xff666666;

	private Context mContext;
	private List<FolderInfo> mItems;

	private int mCurrentPos;

	private OnFolderClickListener mOnFolderClickListener;

	public FolderAdapter(Context context, List<FolderInfo> items, int currentPos) {

		mContext = context;
		mItems = new ArrayList<>();
		if (items != null) {
			mItems.addAll(items);
		}

		mCurrentPos = currentPos;
	}

	public void dataChange(List<FolderInfo> infos, int currentPos) {
		mItems.clear();
		if (infos != null) {
			mItems.addAll(infos);
		}
		mCurrentPos = currentPos;
		notifyDataSetChanged();
	}

	/**
	 * 删除指定文件夹
	 * @param index 文件夹下标
	 */
	public void delete(int index) {
		if (index >= 0 && index < mItems.size()) {
			mItems.remove(index);
			notifyItemRemoved(index);

			if (index == mCurrentPos) {
				mCurrentPos = 0;
				notifyItemChanged(mCurrentPos);
			}
		}
	}

	/**
	 * 添加Camera文件夹
	 * @param folderInfo 文件夹信息
	 */
	public void addCameraAlbum(FolderInfo folderInfo) {
		if ("Camera".equals(folderInfo.getName())) {
			mItems.add(1, folderInfo);
			notifyItemInserted(1);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(new FolderItem(mContext));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {

		FolderItem folderItem = (FolderItem)holder.itemView;
		FolderInfo folderInfo = mItems.get(position);
		String path = folderInfo.getCover();

		if (!TextUtils.isEmpty(path)) {
			if (path.endsWith(".gif")) {
				Glide.with(mContext).load(path).asBitmap().dontAnimate().thumbnail(0.5f).into(folderItem.imageView);
			} else {
				Glide.with(mContext).load(path).dontAnimate().thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESULT).into(folderItem.imageView);
			}
		}

		folderItem.folderName.setText(folderInfo.getName());
		folderItem.photoNumber.setText(String.valueOf(folderInfo.getCount()));

		if (position == mCurrentPos) {
			folderItem.folderName.setTextColor(Color.WHITE);
			folderItem.photoNumber.setTextColor(Color.WHITE);
			folderItem.nextView.setImageResource(R.drawable.album_next_current);
		} else {

			folderItem.folderName.setTextColor(COLOR_GRAY);
			folderItem.photoNumber.setTextColor(COLOR_GRAY);
			folderItem.nextView.setImageResource(R.drawable.album_next_default);
		}

		folderItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOnFolderClickListener != null) {
					mOnFolderClickListener.onClick(v, position);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public void setCurrentPos(int currentPos) {

		if (mCurrentPos != currentPos) {
			final int last = mCurrentPos;
			mCurrentPos = currentPos;
			notifyItemChanged(last);
			notifyItemChanged(mCurrentPos);
		}
	}

	public void setOnFolderClickListener(OnFolderClickListener listener) {
		mOnFolderClickListener = listener;
	}

	static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View itemView) {
			super(itemView);
		}
	}

	public interface OnFolderClickListener {
		void onClick(View view, int position);
	}
}
