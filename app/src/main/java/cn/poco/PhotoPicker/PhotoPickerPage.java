package cn.poco.PhotoPicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextPaint;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.PhotoPicker.ImageStore.FolderInfo;
import cn.poco.PhotoPicker.ImageStore.ImageInfo;
import cn.poco.PhotoPicker.site.PhotoPickerPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ShareData;

public class PhotoPickerPage extends IPage
{
	PhotoPickerPageSite m_site;

	public PhotoPickerPage(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (PhotoPickerPageSite)site;

		//mFolders = folders;
		initialize(getContext());
	}

	public static final int MODE_SINGLE = 0;
	public static final int MODE_MULTI = 1;
	public static final int MODE_PUZZLES = 2;
	public static final int MODE_ALBUM = 3;
	public static final int MODE_REPEAT = 4;

	private ImageView mBackBtn;
	private ImageView mCancelBtn;
	private RelativeLayout mTopBar;
	private RelativeLayout mCaptionBar;
	private ImageView mDeleteBtn;
	//private ImageView mAlbumPuzzlesBtn;
	//private ImageView mAlbumCameraBtn;
	//private ImageView mAlbumMoreBtn;
	private RelativeLayout mBottomContainer;
	private Button mBtnMore;
	private Button mBtnAll;
	private LinearLayout mTopBarBtnGroup;
	private LinearLayout mTopBarBtnAll;
	private LinearLayout mTopBarAllLine;
	private LinearLayout mTopBarBtnMore;
	private LinearLayout mTopBarMoreLine;
	private int mCurrentPage;
	private IPage mPage;
	private RelativeLayout mContainer;
	private RelativeLayout mMultiOperationBar;
	//private RelativeLayout mAlbumBar;
	private ImageList mImageList;
	private final int PAGE_FOLDERLIST = 1;
	private final int PAGE_MYPHOTO = 2;
	private final int PAGE_IMAGELIST = 3;
	private TextView mTitle;
	private TextView mEditModeTitle;
	private int mMode = MODE_SINGLE;
	private int mChooseMaxNumber = 1;
	private int mChooseMinNumber = 1;
	private boolean mIsEditMode;
	private float mRatioRestrict = 5.0f / 16.0f;
	//private OnChooseImageListener mOnChooseImageListener;
	private ArrayList<ImageInfo> mChoosedImages = new ArrayList<ImageInfo>();
	private ArrayList<Integer> mPageStack = new ArrayList<Integer>();
	private String[] mFolders = null;
	private String m_selImg;
	private int m_defMoudle = -1;
	//private AlbumEventListener mAlbumEventListener;

	//public interface OnChooseImageListener
	//{
	//	void onChoose(String[] imgs);
	//};

	//public interface AlbumEventListener
	//{
	//	void onPuzzles(String[] imgs);
	//
	//	void onBeautify(String img);
	//
	//	void onCamera();
	//
	//	void onShare(String[] imgs);
	//};

	private RelativeLayout m_fr;

	private void initialize(Context context)
	{
		ShareData.InitData((Activity)context);

		FrameLayout.LayoutParams fl;
		RelativeLayout.LayoutParams rl;
		//LinearLayout.LayoutParams ll;

		m_fr = new RelativeLayout(getContext());
		m_fr.setBackgroundColor(0xff0d0e0f);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		this.addView(m_fr, fl);
		{
			mTopBar = new RelativeLayout(context);
			mTopBar.setId(R.id.photo_picker_top_bar2);
			mTopBar.setBackgroundColor(Color.BLUE);
			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
			m_fr.addView(mTopBar, rl);
			{
				mCaptionBar = new RelativeLayout(context);
//				BitmapDrawable bmpDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.framework_top_bar_bk);
//				bmpDraw.setTileModeX(TileMode.REPEAT);
//				mCaptionBar.setBackgroundDrawable(bmpDraw);
				mCaptionBar.setBackgroundColor(0xFF0e0e0e);
				rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				mTopBar.addView(mCaptionBar, rl);
				{
					mBackBtn = new ImageView(getContext());
					mBackBtn.setImageResource(R.drawable.framework_back_btn);
					rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.CENTER_VERTICAL);
					mCaptionBar.addView(mBackBtn, rl);
					mBackBtn.setOnClickListener(mOnClickListener);

					mTopBarBtnGroup = new LinearLayout(context);
					mTopBarBtnGroup.setOrientation(LinearLayout.HORIZONTAL);
					mTopBarBtnGroup.setBackgroundColor(0xFF0e0e0e);
					rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.CENTER_IN_PARENT);
					mCaptionBar.addView(mTopBarBtnGroup, rl);
					{
						mTopBarBtnAll = new LinearLayout(getContext());
						mTopBarBtnAll.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(180),RelativeLayout.LayoutParams.WRAP_CONTENT);
						mTopBarBtnGroup.addView(mTopBarBtnAll,ll);
						{
							mBtnAll = new Button(context);
//							mBtnAll.setBackgroundResource(R.drawable.imagelayout_allbtn_over);
							mBtnAll.setBackgroundColor(0xFF0e0e0e);
							mBtnAll.setText(getResources().getString(R.string.photos));
							mBtnAll.setPadding(0, 0, 0, 0);
							mBtnAll.setTextColor(0xFFFFFFFF);
							mBtnAll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
							ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,ShareData.PxToDpi_xhdpi(76));
							ll.gravity = Gravity.CENTER;
							mBtnAll.setLayoutParams(ll);
							mTopBarBtnAll.addView(mBtnAll);
							mBtnAll.setOnClickListener(mOnClickListener);
							
							mTopBarAllLine = new LinearLayout(getContext());
							ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(100),ShareData.PxToDpi_xhdpi(4));
						    ll.gravity = Gravity.CENTER;
							mTopBarAllLine.setBackgroundColor(0xFFFFC433);
							mTopBarBtnAll.addView(mTopBarAllLine,ll);
						}

						mTopBarBtnMore = new LinearLayout(getContext());
						mTopBarBtnMore.setOrientation(LinearLayout.VERTICAL);
						ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(180),RelativeLayout.LayoutParams.WRAP_CONTENT);
						mTopBarBtnGroup.addView(mTopBarBtnMore,ll);
						{							
							mBtnMore = new Button(context);
//							mBtnMore.setBackgroundResource(R.drawable.imagelayout_morebtn);
							mBtnMore.setBackgroundColor(0xFF0e0e0e);
							mBtnMore.setText(getResources().getString(R.string.OtherAlbums));
							mBtnMore.setPadding(0, 0, 0, 0);
							mBtnMore.setTextColor(0xFFFFFFFF);
							mBtnMore.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
							ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,ShareData.PxToDpi_xhdpi(76));
							ll.gravity = Gravity.CENTER;
							mBtnMore.setLayoutParams(ll);
							mTopBarBtnMore.addView(mBtnMore);
							mBtnMore.setOnClickListener(mOnClickListener);
							
							mTopBarMoreLine = new LinearLayout(getContext());
							ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(100),ShareData.PxToDpi_xhdpi(4));
							ll.gravity = Gravity.CENTER;
							mTopBarMoreLine.setBackgroundColor(0xFFFFC433);
							mTopBarMoreLine.setVisibility(GONE);
							mTopBarBtnMore.addView(mTopBarMoreLine,ll);
						}
					}

					mTitle = new TextView(context);
					mTitle.setGravity(Gravity.CENTER);
					mTitle.setTextColor(0xffffffff);
					mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
					mTitle.setSingleLine();
					mTitle.setEllipsize(TruncateAt.END);
					mTitle.setText(getResources().getString(R.string.SelectAlbums));
					rl = new RelativeLayout.LayoutParams(ImagePage.getRealPixel2(250), RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.CENTER_IN_PARENT);
					mCaptionBar.addView(mTitle, rl);
					mTitle.setVisibility(GONE);
				}

				mMultiOperationBar = new RelativeLayout(context);
				mMultiOperationBar.setBackgroundResource(R.drawable.framework_top_bar_bk);
				rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				mTopBar.addView(mMultiOperationBar, rl);
				mMultiOperationBar.setVisibility(GONE);
				{
					mDeleteBtn = new ImageView(getContext());
					//mDeleteBtn.setId(1);
//					mDeleteBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_edit_deletebtn_on, R.drawable.album_edit_deletebtn_hover));
					rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					rl.addRule(RelativeLayout.CENTER_VERTICAL);
					mMultiOperationBar.addView(mDeleteBtn, rl);
					mDeleteBtn.setOnClickListener(mOnClickListener);

					mEditModeTitle = new TextView(context);
					mEditModeTitle.setTextColor(0xffffffff);
					mEditModeTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
					mEditModeTitle.setText(getResources().getString(R.string.MultipleSelection));
					rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.CENTER_IN_PARENT);
					mMultiOperationBar.addView(mEditModeTitle, rl);

					mCancelBtn = new ImageView(getContext());
					//mCancelBtn.setId(2);
					mCancelBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_back_btn, R.drawable.framework_back_btn));
					rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl.addRule(RelativeLayout.CENTER_VERTICAL);
					mMultiOperationBar.addView(mCancelBtn, rl);
					mCancelBtn.setOnClickListener(mOnClickListener);
				}
			}

			mBottomContainer = new RelativeLayout(context);
			mBottomContainer.setId(R.id.photo_picker_bottom_container);
			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			m_fr.addView(mBottomContainer, rl);
			{
				mImageList = new ImageList(context);
				rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				mBottomContainer.addView(mImageList, rl);
				if(mMode == MODE_SINGLE || mMode == MODE_ALBUM)
				{
					mImageList.setVisibility(GONE);
				}

				//mAlbumBar = new RelativeLayout(context);
				//mAlbumBar.setId(8);
				//BitmapDrawable drawable = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.framework_bottom_bar_bk));
				//drawable.setTileModeX(TileMode.REPEAT);
				//mAlbumBar.setBackgroundDrawable(drawable);
				//rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//mBottomContainer.addView(mAlbumBar, rl);
				//if(mMode != MODE_ALBUM)
				//{
				//	mAlbumBar.setVisibility(GONE);
				//}
				//{
				//	LinearLayout btnGroup = new LinearLayout(context);
				//	btnGroup.setOrientation(LinearLayout.HORIZONTAL);
				//	rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//	mAlbumBar.addView(btnGroup, rl);
				//	{
				//		RelativeLayout btnDelete = new RelativeLayout(context);
				//		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				//		ll.weight = 1;
				//		btnGroup.addView(btnDelete, ll);
				//		{
				//			mAlbumCameraBtn = new ImageView(getContext());
				//			mAlbumCameraBtn.setId(1);
				//			mAlbumCameraBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(),R.drawable.album_camerabtn_out, R.drawable.album_camerabtn_hover));
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnDelete.addView(mAlbumCameraBtn, rl);
				//			mAlbumCameraBtn.setOnClickListener(mOnClickListener);
				//			
				//			TextView btnText = new TextView(context);
				//			btnText.setGravity(Gravity.CENTER);
				//			btnText.setText("拍照");
				//			btnText.setTextSize(12);
				//			btnText.setTextColor(0xffaaaaaa);
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnDelete.addView(btnText, rl);
				//		}
				//		
				//		RelativeLayout btnShare = new RelativeLayout(context);
				//		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				//		ll.weight = 1;
				//		btnGroup.addView(btnShare, ll);
				//		{
				//			mAlbumPuzzlesBtn = new ImageView(getContext());
				//			mAlbumPuzzlesBtn.setId(1);
				//			mAlbumPuzzlesBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(),R.drawable.album_puzzlesbtn_out, R.drawable.album_puzzlesbtn_hover));
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnShare.addView(mAlbumPuzzlesBtn, rl);
				//			mAlbumPuzzlesBtn.setOnClickListener(mOnClickListener);
				//			
				//			TextView btnText = new TextView(context);
				//			btnText.setGravity(Gravity.CENTER);
				//			btnText.setText("拼图");
				//			btnText.setTextSize(12);
				//			btnText.setTextColor(0xffaaaaaa);
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnShare.addView(btnText, rl);
				//		}
				//		
				//		RelativeLayout btnBeautify = new RelativeLayout(context);
				//		ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				//		ll.weight = 1;
				//		btnGroup.addView(btnBeautify, ll);
				//		{
				//			mAlbumMoreBtn = new ImageView(getContext());
				//			mAlbumMoreBtn.setId(1);
				//			mAlbumMoreBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(),R.drawable.album_editbtn_out, R.drawable.album_editbtn_hover));
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnBeautify.addView(mAlbumMoreBtn, rl);
				//			mAlbumMoreBtn.setOnClickListener(mOnClickListener);
				//			
				//			TextView btnText = new TextView(context);
				//			btnText.setGravity(Gravity.CENTER);
				//			btnText.setText("批量操作");
				//			btnText.setTextSize(12);
				//			btnText.setTextColor(0xffaaaaaa);
				//			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				//			rl.addRule(RelativeLayout.ALIGN_BOTTOM, 1);
				//			rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
				//			btnBeautify.addView(btnText, rl);
				//		}
				//	}
				//}
			}

			mContainer = new RelativeLayout(context);
			mContainer.setBackgroundColor(0xff0d0e0f);
			rl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
			rl.addRule(RelativeLayout.BELOW, R.id.photo_picker_top_bar2);
			rl.addRule(RelativeLayout.ABOVE, R.id.photo_picker_bottom_container);
			m_fr.addView(mContainer, 0, rl);
		}

		openMyPhoto();
	}

	//public void setOnChooseListener(OnChooseImageListener l)
	//{
	//	mOnChooseImageListener = l;
	//}
	//
	//public void setAlbumEventListener(AlbumEventListener l)
	//{
	//	mAlbumEventListener = l;
	//}

	public void setChooseMaxNumber(int max)
	{
		mChooseMaxNumber = max;
		if(mImageList != null)
		{
			mImageList.setMaxNumber(max);
		}
	}

	public void setChooseMinNumber(int min)
	{
		mChooseMinNumber = min;
	}

	public void setFolders(String[] folders)
	{
		mFolders = folders;
	}

	public void setMode(int mode)
	{
		mMode = mode;
		if(mMode == MODE_SINGLE)
		{
			mBottomContainer.setVisibility(GONE);
			//mAlbumBar.setVisibility(GONE);
			if(mImageList != null)
			{
				mImageList.setVisibility(GONE);
			}
		}
		else if(mMode == MODE_MULTI || mMode == MODE_PUZZLES || mMode == MODE_REPEAT)
		{
			mBottomContainer.setVisibility(VISIBLE);
			//mAlbumBar.setVisibility(GONE);
			if(mImageList != null)
			{
				mImageList.setVisibility(VISIBLE);
			}
		}
		else if(mMode == MODE_ALBUM)
		{
			mBottomContainer.setVisibility(VISIBLE);
			//mAlbumBar.setVisibility(VISIBLE);
			mImageList.setVisibility(GONE);
		}
		switch(mMode)
		{
			case MODE_PUZZLES:
				mImageList.setOkBtnText(getResources().getString(R.string.startImageStitching));
				break;
			case MODE_MULTI:
				mImageList.setOkBtnText(getResources().getString(R.string.InsertAPhoto));
				break;
			case MODE_REPEAT:
				mImageList.setOkBtnText(getResources().getString(R.string.Add));
				break;
		}
	}

	public ImageInfo[] getSelImgs()
	{
		ImageInfo[] imgs;
		if(mChoosedImages.size() > 0)
		{
			imgs = mChoosedImages.toArray(new ImageInfo[mChoosedImages.size()]);
			return imgs;
		}
		return null;
	}

	public void setSelImgs(ArrayList<String> paths)
	{
		/*String imgs = "";
		if(paths != null)
		{
			for(int i = 0; i < paths.size(); i++)
			{
				imgs += paths.get(i);
			}
		}*/
		ArrayList<ImageInfo> mSelImgs = new ArrayList<ImageInfo>();
		ArrayList<ImageInfo> a = ImageStore.getImages(getContext());
		if(a != null)
		{
			ImageInfo img;
			ImageInfo[] images = a.toArray(new ImageInfo[a.size()]);
			/*for(int i = 0; i < images.length; i++)
			{
				img = images[i];
				if(imgs.contains(img.image))
				{
					mSelImgs.add(img);
				}
			}*/
			for(int i = 0; i < paths.size(); i++)
			{
				for(int j = 0; j < images.length; j++)
				{
					if(paths.get(i).equalsIgnoreCase(images[j].image))
					{
						img = images[j];
						mSelImgs.add(img);
						break;
					}

				}
			}

		}

		final ImageInfo[] selImgs = mSelImgs.toArray(new ImageInfo[mSelImgs.size()]);
		ImageInfo img;
		if(selImgs != null)
		{
			for(int i = 0; i < selImgs.length; i++)
			{
				img = selImgs[i];
				if(mMode != MODE_REPEAT)
				{
					img.selected = true;
				}
				else
				{
					img.selected = false;
				}
				mChoosedImages.add(img);
				mImageList.addImage(img);
			}
		}

	}

	public void setSelImgs(ImageInfo[] selImgs)
	{
		ImageInfo img;
		if(selImgs != null)
		{
			for(int i = 0; i < selImgs.length; i++)
			{
				img = selImgs[i];
				if(mMode != MODE_REPEAT)
				{
					img.selected = true;
				}
				else
				{
					img.selected = false;
				}
				mChoosedImages.add(img);
				mImageList.addImage(img);
			}
		}
	}

	public void refresh()
	{
		if(mPage instanceof ImagePage)
		{
			ImagePage list = (ImagePage)mPage;
			list.refresh();
		}
	}

	public void openImageList(String folder)
	{
		IPage page = setActivePage(PAGE_IMAGELIST);
		if(page != null)
		{
			mBackBtn.setVisibility(VISIBLE);
			mTopBarBtnGroup.setVisibility(GONE);
			if(folder != null)
			{
				mTitle.setText(folder);
			}
			mTitle.setVisibility(VISIBLE);
			//if(mMode == MODE_ALBUM)
			//{
			//	mAlbumBar.setVisibility(VISIBLE);
			//}
			ImagePage imgPage = (ImagePage)page;
			imgPage.loadFiles(new String[]{folder});
			imgPage.setOnImageSelectListener(mImageChooseListener);
			imgPage.setOnPreChooseImageListener(mOnPreChooseImageListener);
			imgPage.setOnItemLongClickListener(mItemLongClickListener);
		}
	}

	public void openMyPhoto()
	{
		mTopBarBtnGroup.setVisibility(VISIBLE);
//		mBtnMore.setBackgroundResource(R.drawable.imagelayout_morebtn);
//		mBtnMore.setTextColor(0xffbebebe);
//		mBtnAll.setBackgroundResource(R.drawable.imagelayout_allbtn_over);
//		mBtnAll.setTextColor(0xffffffff);
		mTopBarAllLine.setVisibility(VISIBLE);
		mTopBarMoreLine.setVisibility(GONE);
		mTitle.setVisibility(GONE);
		//if(mMode == MODE_ALBUM)
		//{
		//	mAlbumBar.setVisibility(VISIBLE);
		//}
		IPage page = setActivePage(PAGE_MYPHOTO);
		if(page != null)
		{
			ImagePage imgPage = (ImagePage)page;
			imgPage.loadFiles(mFolders);
			imgPage.setOnImageSelectListener(mImageChooseListener);
			imgPage.setOnPreChooseImageListener(mOnPreChooseImageListener);
			imgPage.setOnItemLongClickListener(mItemLongClickListener);
		}
	}

	public void openFolderList()
	{
		mTopBarBtnGroup.setVisibility(VISIBLE);
//		mBtnMore.setBackgroundResource(R.drawable.imagelayout_morebtn_over);
//		mBtnMore.setTextColor(0xffffffff);
//		mBtnAll.setBackgroundResource(R.drawable.imagelayout_allbtn);
//		mBtnAll.setTextColor(0xffbebebe);
		mTopBarMoreLine.setVisibility(VISIBLE);
		mTopBarAllLine.setVisibility(GONE);
		mTitle.setVisibility(GONE);
		//if(mMode == MODE_ALBUM)
		//{
		//	mAlbumBar.setVisibility(GONE);
		//}
		FolderPage page = (FolderPage)setActivePage(PAGE_FOLDERLIST);
		page.loadFolders();
		page.setOnItemClickListener(mFolderItemClickListener);
	}

	private IPage setActivePage(int page)
	{
		mCurrentPage = page;
		mContainer.removeAllViews();
		if(mPage != null)
		{
			mPage.onClose();
			mPage = null;
		}
		pushToPageStack(page);
		View view = null;
		switch(page)
		{
			case PAGE_FOLDERLIST:
				view = new FolderPage(getContext(), null);
				break;
			case PAGE_MYPHOTO:
				view = new ImagePage(getContext(), null);
				break;
			case PAGE_IMAGELIST:
				view = new ImagePage(getContext(), null);
				break;
		}
		if(view != null)
		{
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mContainer.addView(view, params);
			mPage = (IPage)view;
		}
		return mPage;
	}

	private void delImage(ImageInfo img)
	{
		if(img != null)
		{
			img.selected = false;
		}
		if(mCurrentPage == PAGE_MYPHOTO || mCurrentPage == PAGE_IMAGELIST)
		{
			ImagePage page = (ImagePage)mPage;
			page.setSelected(img, img.selected);
		}
		if(mChoosedImages.contains(img))
		{
			mChoosedImages.remove(img);
		}
		if(mImageList != null)
		{
			mImageList.delImage(img);
		}
	}

	private void onChoosedImages(String[] imgs)
	{
		//if(mOnChooseImageListener != null)
		//{
		//	mOnChooseImageListener.onChoose(imgs);
		//}
		if(imgs == null || imgs.length == 0)
		{
			Toast.makeText(getContext(), getResources().getString(R.string.PhotoNotExist), Toast.LENGTH_SHORT).show();
			return;
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("imgs", imgs);
		if(m_defMoudle != -1)
		{
			map.put("def_page", m_defMoudle);
		}
		m_site.OnSelPhoto(getContext(), map);
	}

	private FolderPage.OnItemClickListener mFolderItemClickListener = new FolderPage.OnItemClickListener()
	{

		@Override
		public void onItemClick(FolderInfo info)
		{
			if(info != null)
			{
				openImageList(info.folder);
			}
		}
	};

	//页面堆栈
	private void pushToPageStack(int page)
	{
		for(int i = 0; i < mPageStack.size(); i++)
		{
			if(mPageStack.get(i) == page)
			{
				mPageStack.remove(i);
				break;
			}
		}
		mPageStack.add(page);
	}

	private int popPageStack()
	{
		if(mPageStack.size() > 1)
		{
			int page = mPageStack.get(mPageStack.size() - 2);
			mPageStack.remove(mPageStack.size() - 1);
			return page;
		}
		return -1;
	}

	//图片选择之前的监听器,返回false表示不处理,true表示已处理
	private ImagePage.OnPreChooseImageListener mOnPreChooseImageListener = new ImagePage.OnPreChooseImageListener()
	{

		@Override
		public boolean onPreChoose(ImageInfo[] imgs)
		{
			if(imgs == null)
			{
				return false;
			}
			if(imgs.length > 0 && mIsEditMode == false && mMode == MODE_ALBUM)
			{
				//Context context = getContext();
				//if(context instanceof AlbumActivity)
				//{
				//	TongJi.add_using_count("POCO相册（从桌面进入）/进入大图查看页");
				//}
				//openImageBrowser(imgs[0]);
				return true;
			}
			if(mIsEditMode)
			{
				return false;
			}
			if(mMode == MODE_ALBUM)
			{
				return true;
			}
			if(mMode == MODE_REPEAT)
			{
				if(imgs.length > 0)
				{
					final ImageInfo img = imgs[0];
					if(mChoosedImages.size() < mChooseMaxNumber)
					{
						if(mChoosedImages.contains(img) == false)
						{
							mChoosedImages.add(img);
							if(mImageList != null)
							{
								mImageList.addImage(img);
							}
						}
						else
						{
							AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
							dlg.setTitle(getResources().getString(R.string.sureAdd));
							dlg.setMessage(getResources().getString(R.string.alreaySelectedPhotoTips));
							dlg.setCanceledOnTouchOutside(false);
							dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.Add), new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									mChoosedImages.add(img);
									if(mImageList != null)
									{
										mImageList.addImage(img);
									}
								}
							});
							dlg.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.Cancel), (DialogInterface.OnClickListener)null);
							dlg.show();
						}
					}
					else
					{
						Toast.makeText(getContext(), getResources().getString(R.string.photosSeletedTips1) + mChooseMaxNumber + getResources().getString(R.string.photosSeletedTips2), Toast.LENGTH_SHORT).show();
					}
				}
				return true;
			}
			if(mMode == MODE_PUZZLES || mMode == MODE_SINGLE || mMode == MODE_MULTI)
			{
				for(int i = 0; i < imgs.length; i++)
				{
					ImageInfo info = imgs[i];
					if(info.selected == false && info.image != null)
					{
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(info.image, opts);
						if(opts.outWidth > 0 && opts.outHeight > 0)
						{
							float r = (float)opts.outWidth / (float)opts.outHeight;
							if(r > 1)
								r = 1 / r;
							if(r < mRatioRestrict)
							{
								Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.notSupportedTips), Toast.LENGTH_SHORT);
								toast.show();
								toast.setGravity(Gravity.CENTER, 0, 0);
								return true;
							}
						}
						else if(new File(info.image).exists() == false)
						{

							Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.InvalidImgeTips), Toast.LENGTH_SHORT);
							toast.show();
							toast.setGravity(Gravity.CENTER, 0, 0);
							return true;
						}
					}
				}
			}
			if(mMode == MODE_SINGLE)
			{
				onChoosedImages(new String[]{imgs[0].image});
				return true;
			}
			int number = mChoosedImages.size();
			for(int i = 0; i < imgs.length; i++)
			{
				ImageInfo info = imgs[i];
				if(mChoosedImages.contains(info) == true)
				{
					if(info.selected == true)
					{
						number--;
					}
				}
			}
			if((mMode == MODE_MULTI || mMode == MODE_PUZZLES) && number >= mChooseMaxNumber)
			{
				Toast.makeText(getContext(), getResources().getString(R.string.photosSeletedTips1) + mChooseMaxNumber + getResources().getString(R.string.photosSeletedTips2), Toast.LENGTH_SHORT).show();
				return true;
			}
			return false;
		}
	};

	//
	private ImagePage.OnImageSelectListener mImageChooseListener = new ImagePage.OnImageSelectListener()
	{

		@Override
		public void onSelected(ImageInfo[] imgs)
		{
			if(mMode == MODE_ALBUM || mIsEditMode == true)
			{
				if(mIsEditMode)
				{
					int count = ImageStore.getSelCount();
					String strText = getResources().getString(R.string.MultipleSelection1) + count + getResources().getString(R.string.MultipleSelection2);
					mEditModeTitle.setText(strText);

					if(count == 0)
					{
//						mDeleteBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_edit_deletebtn_out, R.drawable.album_edit_deletebtn_out));
					}
					else
					{
//						mDeleteBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_edit_deletebtn_on, R.drawable.album_edit_deletebtn_hover));
					}
				}
				return;
			}
			if(imgs != null && imgs.length > 0)
			{
				if(mMode != MODE_SINGLE)
				{
					for(int i = 0; i < imgs.length; i++)
					{
						ImageInfo info = imgs[i];
						if(mChoosedImages.contains(info) == false)
						{
							if(info.selected == true)
							{
								mChoosedImages.add(info);
								if(mImageList != null)
								{
									mImageList.addImage(info);
								}
							}
						}
						else
						{
							if(info.selected == false)
							{
								mChoosedImages.remove(info);
								if(mImageList != null)
								{
									mImageList.delImage(info);
								}
							}
						}
					}
				}
			}
		}
	};

	private boolean mPressedPuzzlesBtn = false;
	private OnClickListener mOnClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(v == mBackBtn || v == mCancelBtn)
			{
				onBack();
			}
			else if(v == mBtnMore)
			{
				openFolderList();
			}
			else if(v == mBtnAll)
			{
				openMyPhoto();
			}
			//else if(v == mAlbumMoreBtn)
			//{
			//	Context context = getContext();
			//	//if(context instanceof AlbumActivity)
			//	//{
			//	//	TongJi.add_using_count("POCO相册（从桌面进入）/下面的批量操作按钮");
			//	//}
			//	setEditMode(true);
			//	mAlbumBar.setVisibility(GONE);
			//	String strText = "多选，已选择0张";
			//	mEditModeTitle.setText(strText);
			//	mDeleteBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_edit_deletebtn_out, R.drawable.album_edit_deletebtn_out));
			//}
			//else if(v == mAlbumPuzzlesBtn)
			//{
			//	Context context = getContext();
			//	//if(context instanceof AlbumActivity)
			//	//{
			//	//	TongJi.add_using_count("POCO相册（从桌面进入）/下面的拼图按钮");
			//	//}
			//	mPressedPuzzlesBtn = true;
			//	setMode(MODE_PUZZLES);
			//	setChooseMaxNumber(8);
			//	setChooseMinNumber(2);
			//}
			else if(v == mDeleteBtn)
			{
				delImgs();
			}
			//else if(v == mAlbumCameraBtn)
			//{
			//	if(mAlbumEventListener != null)
			//	{
			//		mAlbumEventListener.onCamera();
			//	}
			//	else
			//	{
			//		PocoCamera.main.openCamera(-1);
			//	}
			//}
		}

	};

	private void delImgs()
	{
		if(mPage instanceof ImagePage)
		{
			ImagePage page = (ImagePage)mPage;
			page.delSelImgs(new Runnable()
			{
				@Override
				public void run()
				{
					int size = mChoosedImages.size();
					for(int i = 0; i < size; i++)
					{
						if(!checkFileExist(mChoosedImages.get(i).image))
						{
							ImageInfo img = mChoosedImages.get(i);
							mChoosedImages.remove(i);
							mImageList.delImage(img);
							i--;
							size--;
						}
					}
					setEditMode(false);
				}
			});
		}
	}

	public boolean checkFileExist(String filePath)
	{
		try
		{
			File file = new File(filePath);
			if(file.exists())
			{
				return true;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return false;
	}

	private class ImageList extends RelativeLayout
	{

		public ImageList(Context context, AttributeSet attrs, int defStyle)
		{
			super(context, attrs, defStyle);
			initialize(context);
		}

		public ImageList(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			initialize(context);
		}

		public ImageList(Context context)
		{
			super(context);
			initialize(context);
		}

		private int mMax = 0;
		private TextView mTxNumber;
		private HorizontalScrollView mScrollContainer;
		private Button mBtnOk;
		private TextView mTxMax;
		private LinearLayout mThumbIconLayout;
		private ArrayList<ThumbItem> mThumbItems = new ArrayList<ThumbItem>();

		private void initialize(Context context)
		{
			LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, ShareData.PxToDpi_hdpi(58));
			RelativeLayout topBar = new RelativeLayout(context);
			addView(topBar, params);
			//			topBar.setBackgroundColor(0xff2E2E2E);
			BitmapDrawable drawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.framework_top_bar_bk));
			drawable.setTileModeX(TileMode.REPEAT);
			topBar.setBackgroundDrawable(drawable);
			topBar.setId(R.id.photo_picker_top_bar);

			params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, R.id.photo_picker_top_bar);
			mScrollContainer = new HorizontalScrollView(context);
			mScrollContainer.setHorizontalScrollBarEnabled(false);
			addView(mScrollContainer, params);
			//mScrollContainer.setId(1);
			mScrollContainer.setBackgroundColor(0xff000000);

			params = new LayoutParams(LayoutParams.FILL_PARENT, ImagePage.getRealPixel2(156));
			mThumbIconLayout = new LinearLayout(context);
			mThumbIconLayout.setOrientation(LinearLayout.HORIZONTAL);
			mScrollContainer.addView(mThumbIconLayout, params);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.leftMargin = ShareData.PxToDpi_hdpi(18);
			TextView tx1 = new TextView(context);
			tx1.setText(getResources().getString(R.string.curSelect) + " ");
			tx1.setTextColor(0xff797980);
			tx1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
			topBar.addView(tx1, params);
			tx1.setId(R.id.photo_picker_tex);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.photo_picker_tex);
			mTxNumber = new TextView(context);
			mTxNumber.setText("0");
			//mTxNumber.setTextColor(0xff49B006);
			mTxNumber.setTextColor(0xffffffff);
			mTxNumber.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
			topBar.addView(mTxNumber, params);
			mTxNumber.setId(R.id.photo_picker_number2);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.photo_picker_number2);
			mTxMax = new TextView(context);
			mTxMax.setText(getResources().getString(R.string.tipsText1) + mChooseMaxNumber + getResources().getString(R.string.tipsText2));
			mTxMax.setTextColor(0xff797980);
			mTxMax.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.0f);
			topBar.addView(mTxMax, params);

			params = new LayoutParams(LayoutParams.WRAP_CONTENT, ImagePage.getRealPixel2(60));
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.rightMargin = ImagePage.getRealPixel2(16);
			mBtnOk = new Button(context);
//			mBtnOk.setBackgroundDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.imagelayout_btn_puzzle_out1, R.drawable.imagelayout_btn_puzzle_over1));
			mBtnOk.setText(getResources().getString(R.string.startImageStitching));
			mBtnOk.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			mBtnOk.setTextColor(0xffffffff);
			TextPaint tp = mBtnOk.getPaint();
			tp.setFakeBoldText(true);
			mBtnOk.setGravity(Gravity.CENTER);
			mBtnOk.setPadding(0, 0, 0, 0);
			topBar.addView(mBtnOk, params);
			//mBtnOk.setId(4);
			mBtnOk.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					String[] imgs = new String[mChoosedImages.size()];
					for(int i = 0; i < imgs.length; i++)
					{
						imgs[i] = mChoosedImages.get(i).image;
					}
					if(imgs.length < mChooseMinNumber)
					{
						Toast.makeText(getContext(), getResources().getString(R.string.leastPhoto1) + mChooseMinNumber + getResources().getString(R.string.leastPhoto2), Toast.LENGTH_SHORT).show();
						return;
					}
					onChoosedImages(imgs);
				}
			});
		}

		public void setMaxNumber(int max)
		{
			mMax = max;
			mTxMax.setText(getResources().getString(R.string.tipsText1) + mMax + getResources().getString(R.string.tipsText2));
		}

		public void setOkBtnText(String text)
		{
			mBtnOk.setText(text);
		}

		//		public void setOkBtnImage(int normal, int press)
		//		{
		//			mBtnOk.setButtonImage(normal, press);
		//		}

		/*public void setOkBtnImage(int image)
		{
			mBtnOk.setBackgroundResource(image);
		}*/

		public void addImage(ImageInfo img)
		{
			if(img != null)
			{
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.leftMargin = ShareData.PxToDpi_hdpi(8);
				params.rightMargin = ShareData.PxToDpi_hdpi(8);
				params.gravity = Gravity.CENTER_VERTICAL;
				ThumbItem item = new ThumbItem(getContext());
				mThumbIconLayout.addView(item, params);
				mThumbItems.add(item);
				item.setImage(img);
				Handler handler = new Handler();
				handler.post(new Runnable()
				{

					@Override
					public void run()
					{
						if(mThumbIconLayout.getWidth() >= mScrollContainer.getWidth())
						{
							mScrollContainer.smoothScrollTo(mThumbIconLayout.getWidth() - mScrollContainer.getWidth(), 0);
						}
					}

				});

				mTxNumber.setText("" + mThumbItems.size());
			}
		}

		public void delImage(ImageInfo img)
		{
			ThumbItem item;
			int count = mThumbItems.size();
			for(int i = 0; i < count; i++)
			{
				item = mThumbItems.get(i);
				if(item.getImage() == img)
				{
					mThumbIconLayout.removeView(item);
					mThumbItems.remove(item);
					break;
				}
			}
			mTxNumber.setText("" + mThumbItems.size());
		}

		public void clear()
		{
			mThumbIconLayout.removeAllViews();
			mThumbItems.clear();
			mTxNumber.setText("" + mThumbItems.size());
		}

		class ThumbItem extends FrameLayout
		{
			public ImageView mImgPic;
			public ImageView mBtnColose;
			public ImageInfo mImg;

			public ThumbItem(Context context)
			{
				super(context);
				FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(ShareData.PxToDpi_hdpi(142), ShareData.PxToDpi_hdpi(142));
				frameParams.gravity = Gravity.CENTER;
				RelativeLayout cLayout = new RelativeLayout(context);
				this.addView(cLayout, frameParams);
				{
					RelativeLayout.LayoutParams relLayoutParams = new RelativeLayout.LayoutParams(ShareData.PxToDpi_hdpi(124), ShareData.PxToDpi_hdpi(124));
					FrameLayout frameLayout = new FrameLayout(getContext());
					frameLayout.setBackgroundColor(0xffffffff);
					relLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					relLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					frameLayout.setPadding(ShareData.PxToDpi_hdpi(4), ShareData.PxToDpi_hdpi(4), ShareData.PxToDpi_hdpi(4), ShareData.PxToDpi_hdpi(4));
					cLayout.addView(frameLayout, relLayoutParams);
					{
						frameParams = new FrameLayout.LayoutParams(ShareData.PxToDpi_hdpi(118), ShareData.PxToDpi_hdpi(118));
						mImgPic = new ImageView(context);
						mImgPic.setScaleType(ScaleType.CENTER_CROP);
						frameParams.gravity = Gravity.CENTER;
						frameLayout.addView(mImgPic, frameParams);
					}

					relLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					relLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					relLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					mBtnColose = new ImageView(context);
					mBtnColose.setScaleType(ScaleType.CENTER_INSIDE);
//					mBtnColose.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.imagelayout_btn_puzzle_choose_close, R.drawable.imagelayout_btn_puzzle_choose_close_over));
					cLayout.addView(mBtnColose, relLayoutParams);
					mBtnColose.setOnClickListener(new OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							Handler handler = new Handler();
							handler.post(new Runnable()
							{
								@Override
								public void run()
								{
									PhotoPickerPage.this.delImage(mImg);
								}
							});
						}
					});
				}
			}

			public void setImage(ImageInfo img)
			{
				mImg = img;
				if(mImg != null)
				{
					Bitmap bmp = ImageStore.getThumbnail(getContext(), mImg);
					mImgPic.setImageBitmap(bmp);
				}
				else
				{
					mImgPic.setImageBitmap(null);
				}
			}

			public ImageInfo getImage()
			{
				return mImg;
			}
		}
	}

	public ImagePage.OnItemLongClickListener mItemLongClickListener = new ImagePage.OnItemLongClickListener()
	{

		@Override
		public void onLongClick(View v)
		{
			if(mIsEditMode == false)
			{
				Vibrator vib = (Vibrator)getContext().getSystemService(Service.VIBRATOR_SERVICE);
				if(vib != null)
				{
					vib.vibrate(30);
				}
				setEditMode(true);
				//mAlbumBar.setVisibility(GONE);
				String strText = getResources().getString(R.string.MultipleSelection1) + "1" + getResources().getString(R.string.MultipleSelection2);
				mEditModeTitle.setText(strText);
//				mDeleteBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.album_edit_deletebtn_on, R.drawable.album_edit_deletebtn_hover));
			}
		}
	};

	private void setEditMode(boolean editMode)
	{
		if(editMode == true)
		{
			mMultiOperationBar.setVisibility(VISIBLE);
			mCaptionBar.setVisibility(GONE);
			mImageList.setVisibility(GONE);
			mIsEditMode = true;
			if(mMode == MODE_PUZZLES || mMode == MODE_MULTI)
			{
				ArrayList<ImageInfo> a = ImageStore.getImages(getContext());
				ImageInfo[] imgs = null;
				ImageInfo img;
				if(a != null)
				{
					imgs = a.toArray(new ImageInfo[a.size()]);
					for(int i = 0; i < imgs.length; i++)
					{
						img = imgs[i];
						img.selected = false;
					}
				}
			}
		}
		else
		{
			ArrayList<ImageInfo> a = ImageStore.getImages(getContext());
			ImageInfo[] imgs = null;
			ImageInfo img;
			if(a != null)
			{
				imgs = a.toArray(new ImageInfo[a.size()]);
				for(int i = 0; i < imgs.length; i++)
				{
					img = imgs[i];
					img.selected = false;
				}
			}
			if(mMode == MODE_PUZZLES || mMode == MODE_MULTI)
			{
				mImageList.setVisibility(VISIBLE);
				int size = mChoosedImages.size();
				for(int i = 0; i < size; i++)
				{
					img = mChoosedImages.get(i);
					if(img.deleted == false)
					{
						img.selected = true;
					}
					else
					{
						mChoosedImages.remove(i);
						mImageList.delImage(img);
						i--;
						size--;
					}
				}
			}
			else
			{
				ImagePage page = (ImagePage)mPage;
				page.clearSelected();
			}
			//if(mMode == MODE_ALBUM)
			//{
			//	mAlbumBar.setVisibility(VISIBLE);
			//}
			mMultiOperationBar.setVisibility(GONE);
			mCaptionBar.setVisibility(VISIBLE);
			mIsEditMode = false;
		}
	}

	@Override
	public void onBack()
	{
		if(mPage != null)
		{
			if(mTopBarBtnGroup.getVisibility() == GONE)
			{
				int pageId = popPageStack();
				if(pageId == PAGE_FOLDERLIST)
				{
					mTopBarBtnGroup.setVisibility(VISIBLE);
//					mBackBtn.setVisibility(GONE);
					mTitle.setVisibility(GONE);
					FolderPage page = (FolderPage)setActivePage(PAGE_FOLDERLIST);
					page.setOnItemClickListener(mFolderItemClickListener);
					page.onRestore();
					return;
				}
				else if(pageId == PAGE_IMAGELIST)
				{
					ImagePage page = (ImagePage)setActivePage(PAGE_IMAGELIST);
					//page.onRestore();
					page.setOnImageSelectListener(mImageChooseListener);
					page.setOnPreChooseImageListener(mOnPreChooseImageListener);
					return;
				}
			}
		}
		m_site.OnBack(getContext());
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(mPage != null)
		{
			return mPage.onActivityResult(requestCode, resultCode, data);
		}
		return false;
	}

	@Override
	public void onClose()
	{
		super.onClose();
		if(mPage != null)
		{
			mPage.onClose();
		}
		FolderPage.clearGlobalCache();
		ImageStore.clearCache();
		ImageStore.clear(false);
	}

	public void onRestore()
	{
		if(mPage != null)
		{
			//mPage.onRestore();
		}
	}

	@Override
	public void onStart()
	{
		if(mPage != null)
		{
			mPage.onStart();
		}
	}

	@Override
	public void onResume()
	{
		if(mPage != null)
		{
			mPage.onResume();
		}

	}

	@Override
	public void onPause()
	{
		if(mPage != null)
		{
			mPage.onPause();
		}
	}

	@Override
	public void onStop()
	{
		if(mPage != null)
		{
			mPage.onStop();
		}
	}

	@Override
	public void onDestroy()
	{
		ImageStore.clear(true);
		FolderPage.clearGlobalCache();
		if(mPage != null)
		{
			mPage.onDestroy();
		}
	}

	/**
	 * sel_img: String
	 * def_page: Integer
	 * @param params
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			Object o = params.get("sel_img");
			if(o != null)
			{
				m_selImg = (String)o;
				onChoosedImages(new String[]{m_selImg});
			}
			m_defMoudle = -1;
			o = params.get("is_back");
			if(o == null || (Boolean)o == false)
			{
				o = params.get("def_page");
				if(o != null)
				{
					m_defMoudle = (Integer)o;
				}
			}
		}
	}
}