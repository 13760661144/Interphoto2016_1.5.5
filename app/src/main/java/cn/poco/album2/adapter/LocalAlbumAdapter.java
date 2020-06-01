package cn.poco.album2.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.view.EmptyView;
import cn.poco.album2.view.PhotoView;
import cn.poco.interphoto2.R;
import cn.poco.statistics.TongJi2;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class LocalAlbumAdapter extends RecyclerView.Adapter<LocalAlbumAdapter.ViewHolder> {

	private static final int TYPE_EMPTY = 0x1;
	private static final int TYPE_ITEM = 0x2;

	private Context mContext;

	private List<ImageStore.ImageInfo> mItems;

	private int mMode;
	private static final int LOOK = 0;
	private static final int SELECT = 1;

	private List<ImageStore.ImageInfo> mSelected = new ArrayList<>();

	private boolean mSingleSelect;

	private OnItemClickListener mOnItemClickListener;

	private OnSelectListener mOnSelectListener;

	public LocalAlbumAdapter(Context context, List<ImageStore.ImageInfo> items, boolean singleSelect) {
		mContext = context;
		mItems = new ArrayList<>();

		if (items != null) {
			mItems.addAll(items);
		}

		mSingleSelect = singleSelect;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		if (viewType == TYPE_EMPTY) {
			EmptyView emptyView = new EmptyView(parent.getContext());
			emptyView.setText(R.string.app_album_empty_tip);
			RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			emptyView.setLayoutParams(params);
			return new ViewHolder(emptyView);
		}

		return new ViewHolder(new PhotoView(parent.getContext()));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (!isEmpty()) {

			final ImageStore.ImageInfo photoInfo = mItems.get(position);

			final PhotoView photoView = (PhotoView)holder.itemView;

			photoView.setEdit(!photoInfo.isSaved);
			String path = photoInfo.image;
			if (path.endsWith(".gif")) {
				Glide.with(mContext).load(path).asBitmap().dontAnimate().thumbnail(0.5f).into(photoView.getImageView());
			} else {
				Glide.with(mContext).load(path).dontAnimate().thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESULT).into(photoView.getImageView());
			}

			if (mMode == LOOK) {
				photoView.cancelSelect();
			} else if (mMode == SELECT) {
				photoView.startSelect();
				photoView.setSelect(photoInfo.selected);
			}

			photoView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mMode == LOOK) {
						if (mOnItemClickListener != null) {
							mOnItemClickListener.onItemClick(v, position, photoInfo.image);
						}
					} else if (mMode == SELECT) {
						TongJi2.AddCountByRes(mContext, R.integer.InterPhoto_选择照片);
						isSelect(photoInfo, photoView);
						if (mOnSelectListener != null) {
							mOnSelectListener.onSelect(mSelected.size());
						}
					}
				}
			});

			photoView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {

					if (mSingleSelect) {
						return false;
					}

					if (mMode == LOOK) {
						mMode = SELECT;
					}

					isSelect(photoInfo, photoView);

					if (mOnItemClickListener != null) {
						mOnItemClickListener.onItemLongClick(position);
					}

					if (mOnSelectListener != null) {
						mOnSelectListener.onSelect(mSelected.size());
					}

					return true;

				}
			});
		}
	}

	private void isSelect(ImageStore.ImageInfo photoInfo, PhotoView photoView) {
		if (mSelected.contains(photoInfo)) {
			photoView.setSelect(false);
			mSelected.remove(photoInfo);
			photoInfo.selected = false;
		} else {
			photoView.setSelect(true);
			mSelected.add(photoInfo);
			photoInfo.selected = true;
		}
	}

	@Override
	public int getItemCount() {
		return isEmpty() ? 1 : mItems.size();
	}

	@Override
	public int getItemViewType(int position) {

		if (isEmpty()) {
			return TYPE_EMPTY;
		}
		return TYPE_ITEM;
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
		if (manager instanceof GridLayoutManager) {
			final GridLayoutManager gridManager = ((GridLayoutManager)manager);
			gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					return getItemViewType(position) == TYPE_EMPTY ? gridManager.getSpanCount() : 1;
				}
			});
		}
	}

	/**
	 * 判断数据是否为空
	 */
	public boolean isEmpty() {
		return mItems == null || mItems.isEmpty();
	}

	public int getSize() {
		return mItems.size();
	}

	public ImageStore.ImageInfo getItem(int position) {
		if (position >= 0 && position < mItems.size()) {
			return mItems.get(position);
		}

		return null;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
	}

	public void setOnSelectListener(OnSelectListener listener) {
		mOnSelectListener = listener;
	}

	/**
	 * 取消选择操作
	 */
	public void cancelSelect() {

		for (ImageStore.ImageInfo info : mSelected) {
			info.selected = false;
		}
		mSelected.clear();
		mMode = LOOK;

		notifyDataSetChanged();
	}

	/**
	 * 开始选择操作
	 */
	public void startSelect() {

		mMode = SELECT;
		if (mOnSelectListener != null) {
			mOnSelectListener.onSelect(0);
		}
		notifyDataSetChanged();
	}

	/**
	 * 获取选中的图片
	 *
	 * @param indexList 用于保存选中图片的下标
	 */
	public List<ImageStore.ImageInfo> getSelectPhotos(List<Integer> indexList) {

		if (indexList != null) {
			indexList.clear();

			for (ImageStore.ImageInfo info : mSelected) {
				indexList.add(mItems.indexOf(info));
			}

			Collections.sort(indexList, new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return o2 - o1;
				}
			});
		}

		return mSelected;
	}

	/**
	 * 进入粘贴效果操作
	 */
	public void enterPasteEffect() {
		for (ImageStore.ImageInfo info : mSelected) {
			info.selected = false;
		}
		if (mOnSelectListener != null) {
			mOnSelectListener.onSelect(0);
		}
		mSelected.clear();
		notifyDataSetChanged();
	}

	/**
	 * 删除指定的图片信息
	 *
	 * @param indexList 图片下标列表
	 */
	public void delete(List<Integer> indexList) {
		for (int index : indexList) {
			mItems.remove(index);
			notifyItemRemoved(index);
		}
	}

	/**
	 * 修改指定的图片信息
	 *
	 * @param indexList 图片下标列表
	 */
	public void change(List<Integer> indexList) {
		for (int index : indexList) {
			notifyItemChanged(index);
		}
	}

	/**
	 * 插入图片信息
	 *
	 * @param imageInfos 图片信息
	 */
	public void insert(List<ImageStore.ImageInfo> imageInfos) {
		if (imageInfos != null) {
			mItems.addAll(0, imageInfos);
			notifyItemRangeInserted(0, imageInfos.size());
		}
	}

	/**
	 * 滑动选择回调
	 *
	 * @param start      开始选中的下标
	 * @param end        最后选中的下标
	 * @param isSelected 是否选中
	 */
	public void selectChange(int start, int end, boolean isSelected) {

		ImageStore.ImageInfo imageInfo;
		for (int i = start; i <= end; i++) {
			imageInfo = mItems.get(i);

			if (mSelected.contains(imageInfo)) {
				mSelected.remove(imageInfo);
				imageInfo.selected = false;
			} else {
				mSelected.add(imageInfo);
				imageInfo.selected = true;
			}

			notifyItemChanged(i);
		}

		if (mOnSelectListener != null) {
			mOnSelectListener.onSelect(mSelected.size());
		}
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
		}
	}

	public interface OnItemClickListener {

		void onItemClick(View view, int position, String path);

		void onItemLongClick(int position);
	}

	public interface OnSelectListener {
		void onSelect(int count);
	}
}
