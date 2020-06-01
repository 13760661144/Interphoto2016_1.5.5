package cn.poco.album2.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.AlbumPage;
import cn.poco.album2.PhotoStore;
import cn.poco.album2.adapter.SystemAlbumAdapter;
import cn.poco.album2.listener.DragSelectTouchListener;
import cn.poco.album2.model.FolderInfo;
import cn.poco.album2.model.PhotoInfo;
import cn.poco.album2.site.AlbumSite;
import cn.poco.album2.utils.PhotoGridDivide;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class SystemAlbumView extends FrameLayout {

	private Context mContext;

	private RecyclerView mRecyclerView;
	protected List<PhotoInfo> mItems;
	private SystemAlbumAdapter mAlbumAdapter;
	private GridLayoutManager mGridLayoutManager;

	private PhotoStore mPhotoStore;
	private int mFolderIndex = 0;

	private boolean mLoading = false;

	private AlbumSite mAlbumSite;
	private CacheView mCacheView;
	private int mCacheViewHeight;

	private Scroller mScroller;
	private static final int DURATION = 500;

	/**
	 * 是否选择模式
	 */
	private boolean isSelectMode = false;

	/**
	 * 滑动选中listener
	 */
	private DragSelectTouchListener mTouchListener;

	private boolean mHideCacheView = false;

	private List<String> mSelectedPaths;

	public SystemAlbumView(@NonNull Context context, AlbumSite albumSite) {
		super(context);

		mContext = context;

		mAlbumSite = albumSite;

		mPhotoStore = PhotoStore.getInstance(mContext);

		mCacheViewHeight = ShareData.PxToDpi_xhdpi(330);

		mScroller = new Scroller(mContext);

		initViews();
	}

	private void initViews() {
		LayoutParams params;

		mRecyclerView = new RecyclerView(mContext);
		mGridLayoutManager = new GridLayoutManager(mContext, 3);
		mRecyclerView.setLayoutManager(mGridLayoutManager);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
		mRecyclerView.addItemDecoration(new PhotoGridDivide(ShareData.PxToDpi_xhdpi(4), ShareData.PxToDpi_xhdpi(4), false));
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		addView(mRecyclerView, params);

		mCacheView = new CacheView(mContext, mAlbumSite);
		params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mCacheViewHeight);
		addView(mCacheView, params);
		mCacheView.setTranslationY(-mCacheViewHeight);
		mCacheView.startScan();

		// 解决闪屏问题
		((SimpleItemAnimator)mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
		mTouchListener = new DragSelectTouchListener();
		mTouchListener.withSelectListener(new DragSelectTouchListener.OnDragSelectListener() {

			@Override
			public void onSelectChange(int start, int end, boolean isSelected) {

				if (mAlbumAdapter != null) {
					mAlbumAdapter.selectChange(start, end, isSelected);
				}
			}
		});
		mRecyclerView.addOnItemTouchListener(mTouchListener);

		addLoadMoreListener();
	}

	/**
	 * 下拉加载更多图片信息
	 */
	protected void addLoadMoreListener() {

		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (dy > 0) {
					// 类似上拉加载
					FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
					int last = mGridLayoutManager.findLastVisibleItemPosition();
					if (folderInfo.getCount() > mItems.size() && last >= mItems.size() - 6 && !mLoading) {
						mLoading = true;
						List<PhotoInfo> item = mPhotoStore.getPhotoInfos(folderInfo.getName(), mItems.size(), mPhotoStore.getCacheSize());

						if (mSelectedPaths != null && !mSelectedPaths.isEmpty()) {
							for (PhotoInfo info : item) {
								if (mSelectedPaths.contains(info.getImagePath())) {
									info.setSelected(true);
									mSelectedPaths.remove(info.getImagePath());
								}
							}
						}

						mItems.addAll(item);
						mAlbumAdapter.addItems(item);
						mLoading = false;
					}
				}
			}
		});
	}

	/**
	 * 设置当前选中的文件夹下标
	 */
	public void setFolderIndex(int folderIndex) {
		mFolderIndex = folderIndex;
	}

	/**
	 * 设置图片数据
	 * @param photoList 图片信息列表
	 * @param singleSelect 是否单选
	 */
	public void setPhotoList(List<PhotoInfo> photoList, boolean singleSelect) {
		mItems = new ArrayList<>();
		if (photoList != null) {
			mItems.addAll(photoList);
		}
		mAlbumAdapter = new SystemAlbumAdapter(mContext, photoList, singleSelect);
		mRecyclerView.setAdapter(mAlbumAdapter);
	}

	public void onPause() {
		mSelectedPaths = mAlbumAdapter.getSelectPhotoPaths();
	}

	/**
	 * 单击或长按监听
	 */
	public void setOnItemClickListener(SystemAlbumAdapter.OnItemClickListener listener) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.setOnItemClickListener(listener);
		}
	}

	/**
	 * 选择监听
	 */
	public void setOnSelectListener(SystemAlbumAdapter.OnSelectListener listener) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.setOnSelectListener(listener);
		}
	}

	/**
	 * 进入选择模式
	 */
	public void startSelect() {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.startSelect();
		}
	}

	/**
	 * 退出选择模式
	 */
	public void cancelSelect() {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.cancelSelect();

			if (mSelectedPaths != null) {
				mSelectedPaths.clear();
				mSelectedPaths = null;
			}
		}
	}

	/**
	 * 更新图片信息（更换相册时会调用）
	 * @param photoInfos 图片列表
	 */
	public void updatePhotos(List<PhotoInfo> photoInfos) {
		mItems.clear();
		if (photoInfos != null) {
			mItems.addAll(photoInfos);
		}

		if (mSelectedPaths != null && !mSelectedPaths.isEmpty()) {
			for (PhotoInfo info : mItems) {
				if (mSelectedPaths.contains(info.getImagePath())) {
					info.setSelected(true);
					mSelectedPaths.remove(info.getImagePath());
					mAlbumAdapter.addSelectedPhotoInfo(info);
				}
			}
		}
		mAlbumAdapter.updatePhoto(photoInfos);
	}

	/**
	 * 删除图片
	 * @param indexList 图片下标
	 */
	public void delete(List<Integer> indexList) {
		for (int index : indexList) {
			if (index != -1) {
				mItems.remove(index);
			}
		}
		if (mAlbumAdapter != null) {
			mAlbumAdapter.delete(indexList);
		}
	}

	/**
	 * 获取位置
	 */
	public AlbumPage.Position getPosition() {
		AlbumPage.Position position = new AlbumPage.Position();
		position.position = mGridLayoutManager.findFirstVisibleItemPosition();
		View view = mGridLayoutManager.findViewByPosition(position.position);
		if (view != null) {
			position.offset = view.getTop();
		}

		return position;
	}

	/**
	 * 恢复到指定位置
	 * @param position 位置
	 */
	public void restorePosition(AlbumPage.Position position) {
		if (position == null) {
			return;
		}

		FolderInfo folderInfo = mPhotoStore.getFolderInfo(mFolderIndex);
		if (position.position + 15 >= mItems.size()) {
			while (position.position + 15 >= mItems.size() && position.position < folderInfo.getCount()) {
				List<PhotoInfo> item = mPhotoStore.getPhotoInfos(folderInfo.getName(), mItems.size(), mPhotoStore.getCacheSize());

				if (mItems.size() + item.size() > folderInfo.getCount()) {
					for (int i = 0; i < folderInfo.getCount() - mItems.size(); i++) {
						mItems.add(item.get(i));
					}
					break;
				} else {
					mItems.addAll(item);
				}
			}
			mAlbumAdapter.updatePhoto(mItems);
		}

		mGridLayoutManager.scrollToPositionWithOffset(position.position, position.offset);
	}

	/**
	 * 获取选中的图片列表
	 * @param indexList 选中的图片下标
	 * @return 图片列表
	 */
	public List<ImageStore.ImageInfo> getSelectPhotos(List<Integer> indexList) {
		if (mAlbumAdapter != null) {
			return mAlbumAdapter.getSelectPhotos(indexList);
		}

		return null;
	}

	/**
	 * 返回顶端
	 */
	public void backToTop() {
		mRecyclerView.scrollToPosition(0);
	}

	/**
	 * 判断数据是否为空
	 */
	public boolean isEmpty() {
		return mItems == null || mItems.isEmpty();
	}

	/**
	 * 设置是否选择模式
	 */
	public void setSelectMode(boolean isSelectMode) {
		this.isSelectMode = isSelectMode;
	}

	@Override
	public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
		return !isSelectMode && !isEmpty() && (getScrollY() < 0 || !mRecyclerView.canScrollVertically(-1));
	}

	@Override
	public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

		if (dy == 0) {
			return;
		}

		int scrollY = getScrollY();

		if (scrollY < 0 || (!mRecyclerView.canScrollVertically(-1) && dy < 0)) {

			int deltaY = dy;

			if (dy > 0) {
				if (scrollY + deltaY > 0) {
					deltaY = 0 - scrollY;
				}
			} else {
				if (scrollY + deltaY < -mCacheViewHeight) {
					deltaY = -mCacheViewHeight - getScrollY();
				}
			}

			consumed[1] = deltaY;

			scrollBy(0, deltaY);
		}
	}

	@Override
	public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
		return getScrollY() < 0 || super.onNestedPreFling(target, velocityX, velocityY);
	}

	@Override
	public void onStopNestedScroll(View child) {
		super.onStopNestedScroll(child);
		int scrollY = getScrollY();

		if (!mHideCacheView && scrollY < 0) {
			int deltaY;
			if (Math.abs(scrollY) > mCacheViewHeight / 2) {
				deltaY = -mCacheViewHeight-scrollY;
			} else {
				deltaY = -scrollY;
			}

			int duration = (int)(Math.abs(deltaY) * 1f / mCacheViewHeight * DURATION);
			mScroller.startScroll(0, scrollY, 0, deltaY, duration);
			ViewCompat.postInvalidateOnAnimation(this);
		}

		mHideCacheView = false;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(0, mScroller.getCurrY());
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/**
	 * 隐藏头部缓存View
	 */
	public void hideCacheView() {
		if (getScrollY() < 0) {
			mScroller.abortAnimation();
			scrollTo(0, 0);
		}
	}

	public void hideCacheViewWithAnim() {
		int scrollY = getScrollY();
		if (scrollY < 0) {
			mHideCacheView = true;
			mScroller.abortAnimation();
			int duration = (int)(Math.abs(scrollY) * 1f / mCacheViewHeight * DURATION);
			mScroller.startScroll(0, scrollY, 0, -scrollY, duration);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/**
	 * 关闭清理资源
	 */
	public void onClose() {
		if (mScroller != null && !mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		mCacheView.onClose();
	}

	/**
	 * 开始滑动选中
	 * @param startPosition 开始选中的下标
	 */
	public void startDragSelection(int startPosition) {
		mTouchListener.startDragSelection(startPosition);
	}

	public void itemChange(int startPosition) {
		if (startPosition >= 0 && startPosition < mItems.size()) {
			mAlbumAdapter.selectChange(startPosition, startPosition, !mItems.get(startPosition).isSelected());
			mTouchListener.startDragSelection(startPosition);
		}
	}
}
