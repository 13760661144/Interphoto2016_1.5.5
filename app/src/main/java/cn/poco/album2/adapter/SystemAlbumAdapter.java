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
import cn.poco.album2.model.PhotoInfo;
import cn.poco.album2.utils.PhotoUtils;
import cn.poco.album2.view.EmptyView;
import cn.poco.album2.view.PhotoView;
import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class SystemAlbumAdapter extends RecyclerView.Adapter<SystemAlbumAdapter.ViewHolder> {

	private static final int TYPE_EMPTY = 0x1;
	private static final int TYPE_ITEM = 0x2;

	private Context mContext;

	private List<PhotoInfo> mItems;

	private OnItemClickListener mOnItemClickListener;

	private OnSelectListener mOnSelectListener;

	private int mMode;
	private static final int LOOK = 0;
	private static final int SELECT = 1;

	private List<PhotoInfo> mSelected = new ArrayList<>();

	private boolean mSingleSelect;

	public SystemAlbumAdapter(Context context, List<PhotoInfo> items, boolean singleSelect) {
		mContext = context;
		mItems = new ArrayList<>();

		if (items != null) {
			mItems.addAll(items);
		}

		mSingleSelect = singleSelect;
	}

	/**
	 * 添加图片数据
	 */
	public void addItems(List<PhotoInfo> photoInfos) {
		int positionStart = mItems.size();
		mItems.addAll(photoInfos);
		notifyItemRangeInserted(positionStart, photoInfos.size());
	}

	/**
	 * 更新图片数据
	 */
	public void updatePhoto(List<PhotoInfo> photoInfos) {
		mItems.clear();
		if (photoInfos != null) {
			mItems.addAll(photoInfos);
		}
		notifyDataSetChanged();
	}

	/**
	 * 删除图片
	 */
	public void delete(List<Integer> indexList) {
		for (int index : indexList) {
			mItems.remove(index);
			notifyItemRemoved(index);
		}
	}

	/**
	 * 滑动选择回调
	 * @param start 开始选中的下标
	 * @param end 最后选中的下标
	 * @param isSelected 是否选中
	 */
	public void selectChange(int start, int end, boolean isSelected) {

		PhotoInfo photoInfo;
		for (int i = start; i <= end; i++) {
			photoInfo = mItems.get(i);

			if (mSelected.contains(photoInfo)) {
				mSelected.remove(photoInfo);
				photoInfo.setSelected(false);
			} else {
				mSelected.add(photoInfo);
				photoInfo.setSelected(true);
			}

			notifyItemChanged(i);
		}

		if (mOnSelectListener != null) {
			mOnSelectListener.onSelect(mSelected.size());
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		if (viewType == TYPE_EMPTY) {
			EmptyView emptyView = new EmptyView(mContext);
			emptyView.setText(R.string.system_album_empty_tip);
			RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			emptyView.setLayoutParams(params);
			return new ViewHolder(emptyView);
		}

		return new ViewHolder(new PhotoView(mContext));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		if (!isEmpty()) {
			final PhotoView photoView = (PhotoView)holder.itemView;

			final PhotoInfo photoInfo = mItems.get(position);
			String path = photoInfo.getImagePath();
			if (path.endsWith(".gif")) {
				Glide.with(mContext).load(path).asBitmap().dontAnimate().thumbnail(0.5f).into(photoView.getImageView());
			} else {
				Glide.with(mContext).load(path).dontAnimate().thumbnail(0.5f).diskCacheStrategy(DiskCacheStrategy.RESULT).into(photoView.getImageView());
			}

			if (mMode == LOOK) {
				photoView.cancelSelect();
			} else if (mMode == SELECT) {
				photoView.startSelect();
				photoView.setSelect(photoInfo.isSelected());
			}

			photoView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mMode == LOOK) {
						if (mOnItemClickListener != null) {
							mOnItemClickListener.onItemClick(v, position, photoInfo.getImagePath());
						}
					} else if (mMode == SELECT) {
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

	private void isSelect(PhotoInfo photoInfo, PhotoView photoView) {
		if (mSelected.contains(photoInfo)) {
			photoView.setSelect(false);
			mSelected.remove(photoInfo);
			photoInfo.setSelected(false);
		} else {
			if (PhotoUtils.validatePhoto(mContext, photoInfo.getImagePath())) {
				photoView.setSelect(true);
				mSelected.add(photoInfo);
				photoInfo.setSelected(true);
			}
		}
	}

	public void addSelectedPhotoInfo(PhotoInfo photoInfo) {
		mSelected.add(photoInfo);
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

		for (PhotoInfo info : mSelected) {
			info.setSelected(false);
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
	 * 获取选中的图片的路径列表
	 *
	 * @return 路径列表
	 */
	public List<String> getSelectPhotoPaths() {

		List<String> mPaths = new ArrayList<>();

		for (PhotoInfo info : mSelected) {
			mPaths.add(info.getImagePath());
		}
		mSelected.clear();

		return mPaths;
	}

	/**
	 * 获取选中的图片
	 * @param indexList 用于保存选中图片的下标
	 */
	public List<ImageStore.ImageInfo> getSelectPhotos(List<Integer> indexList) {

		if (indexList == null) {
			indexList = new ArrayList<>();
		} else {
			indexList.clear();
		}

		List<ImageStore.ImageInfo> imageInfos = new ArrayList<>();
		for (PhotoInfo info : mSelected) {
			indexList.add(mItems.indexOf(info));
			imageInfos.add(PhotoUtils.change(info));
		}

		Collections.sort(indexList, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});

		return imageInfos;
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
