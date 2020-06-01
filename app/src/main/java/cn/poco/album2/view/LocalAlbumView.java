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
import cn.poco.album2.adapter.LocalAlbumAdapter;
import cn.poco.album2.listener.DragSelectTouchListener;
import cn.poco.album2.site.AlbumSite;
import cn.poco.album2.utils.PhotoGridDivide;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class LocalAlbumView extends FrameLayout {

	private Context mContext;

	private RecyclerView mRecyclerView;
	private LocalAlbumAdapter mAlbumAdapter;
	private GridLayoutManager mGridLayoutManager;

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

	public LocalAlbumView(@NonNull Context context, AlbumSite albumSite) {
		super(context);

		mContext = context;

		mAlbumSite = albumSite;

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
	}

	/**
	 * 设置图片数据
	 * @param photoList 图片信息列表
	 * @param singleSelect 是否单选
	 */
	public void setPhotoList(List<ImageStore.ImageInfo> photoList, boolean singleSelect) {
		mAlbumAdapter = new LocalAlbumAdapter(mContext, photoList, singleSelect);
		mRecyclerView.setAdapter(mAlbumAdapter);
	}

	/**
	 * 单击或长按监听
	 */
	public void setOnItemClickListener(LocalAlbumAdapter.OnItemClickListener listener) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.setOnItemClickListener(listener);
		}
	}

	/**
	 * 选择监听
	 */
	public void setOnSelectListener(LocalAlbumAdapter.OnSelectListener listener) {
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
		}
	}

	/**
	 * 获取选中的图片列表
	 *
	 * @param indexList 选中的图片下标
	 * @return 图片列表
	 */
	public List<ImageStore.ImageInfo> getSelectPhotos(List<Integer> indexList) {
		if (mAlbumAdapter != null) {
			return mAlbumAdapter.getSelectPhotos(indexList);
		}

		return new ArrayList<>();
	}

	/**
	 * 进入粘贴效果操作
	 */
	public void enterPasteEffect() {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.enterPasteEffect();
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
		if (position != null) {
			mGridLayoutManager.scrollToPositionWithOffset(position.position, position.offset);
		}
	}

	/**
	 * 删除图片
	 * @param indexList 图片下标
	 */
	public void delete(List<Integer> indexList) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.delete(indexList);
		}
	}

	/**
	 * 更改图片状态
	 * @param indexList 图片下标
	 */
	public void change(List<Integer> indexList) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.change(indexList);
		}
	}

	/**
	 * 插入图片
	 * @param imageInfos 图片信息
	 */
	public void insert(List<ImageStore.ImageInfo> imageInfos) {
		if (mAlbumAdapter != null) {
			mAlbumAdapter.insert(imageInfos);
		}
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
		return mAlbumAdapter == null || mAlbumAdapter.isEmpty();
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
		if (startPosition >= 0 && startPosition < mAlbumAdapter.getSize()) {
			mAlbumAdapter.selectChange(startPosition, startPosition, !mAlbumAdapter.getItem(startPosition).selected);
			mTouchListener.startDragSelection(startPosition);
		}
	}
}
