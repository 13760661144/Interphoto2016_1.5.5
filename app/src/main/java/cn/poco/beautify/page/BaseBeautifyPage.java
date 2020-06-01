package cn.poco.beautify.page;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.PhotoPicker.site.PhotoPickerPageSite;
import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.WaitDialog1;
import cn.poco.camera.RotationImg2;
import cn.poco.draglistview.DragListItemInfo;
import cn.poco.draglistview.DragListView;
import cn.poco.draglistview.MyDragItemAdapter;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.resource.AbsDownloadMgr;
import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.resource.ResType;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.Utils;

/**
 * 美化功能页的封装
 */

public abstract class BaseBeautifyPage extends IPage
{
	protected boolean m_uiEnabled;
	protected int m_frW;
	protected int m_frH;
	protected int m_topBarHeight;
	protected int m_bottomBarHeight;
	protected int m_resBarHeight;
	protected int DEF_IMG_SIZE;

	protected WaitDialog1 m_waitDlg;
	protected ImageView m_mirrorImage;

	protected FrameLayout m_topBar;
	protected ImageView m_backBtn;
	protected ImageView m_okBtn;
	protected TextView m_title;
	protected LinearLayout m_titleFr;
	protected ImageView m_icon;
	protected FrameLayout m_bottomBar;
	protected FrameLayout m_resListFr;

	protected FrameLayout m_viewFr;

	protected ArrayList<DragListItemInfo> m_listDatas;
	protected DragListView m_resList;    //资源列
	protected MyDragItemAdapter m_listAdapter;
	protected MyDragItemAdapter.MyDragItem m_dragItem;	//拖动的item
	protected DefaultItemAnimator m_recycleAnim;
	protected HashMap<Integer, Boolean> m_themeCompleteFlags;

	protected LinearLayout m_hideFr;
	protected ImageView m_hideIcon;
	protected TextView m_hideText;
	protected boolean m_isChooseHideFr = false;

	protected ImageStore.ImageInfo m_orgInfo;
	protected HashMap<String, Object> m_params;
	protected float m_mainImgH = 1.0f;
	protected float m_curImgH = 1.0f;
	protected float m_mainViewH;
	protected int m_index;

	protected Bitmap m_org;
	protected Bitmap m_bkBmp;
	protected int m_selUri = -1;

	protected boolean m_helpFlag = true;
	protected MasterIntroPage m_masterPage;

	public BaseBeautifyPage(Context context, BaseSite site)
	{
		super(context, site);
	}

	protected void InitData()
	{
		m_uiEnabled = true;
		m_topBarHeight = ShareData.PxToDpi_xhdpi(80);
		m_bottomBarHeight = ShareData.PxToDpi_xhdpi(80);
		m_resBarHeight = ShareData.PxToDpi_xhdpi(160);
		m_frW = ShareData.m_screenWidth;
		m_frH = m_frW * 4 / 3;
		DEF_IMG_SIZE = SysConfig.GetPhotoSize(getContext()) * 2;

		m_themeCompleteFlags = new HashMap<>();

		DownloadMgr.getInstance().AddDownloadListener(m_downloadLst);
	}

	protected void InitUI()
	{
		this.setBackgroundColor(0xff0e0e0e);
		FrameLayout.LayoutParams fl;

		m_viewFr = new FrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.TOP;
		fl.topMargin = m_topBarHeight;
		m_viewFr.setLayoutParams(fl);
		this.addView(m_viewFr, 0);

		m_topBar = new FrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, m_topBarHeight);
		fl.gravity = Gravity.TOP;
		m_topBar.setLayoutParams(fl);
		this.addView(m_topBar);
		{
			m_backBtn = new ImageView(getContext());
			m_backBtn.setImageResource(R.drawable.framework_back_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_backBtn.setLayoutParams(fl);
			m_topBar.addView(m_backBtn);
			m_backBtn.setOnClickListener(GetBtnLst());

			m_titleFr = new LinearLayout(getContext());
			m_titleFr.setGravity(Gravity.CENTER);
			m_titleFr.setOnClickListener(GetBtnLst());
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.CENTER;
			m_titleFr.setLayoutParams(fl);
			m_topBar.addView(m_titleFr);
			{
				LinearLayout.LayoutParams ll;
				m_title = new TextView(getContext());
				m_title.setTextColor(0xffffffff);
				m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_title.setText(getResources().getString(R.string.Crop));
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				m_title.setLayoutParams(ll);
				m_titleFr.addView(m_title);

				m_icon = new ImageView(getContext());
				m_icon.setVisibility(View.GONE);
				m_icon.setImageResource(R.drawable.beautify_effect_help_down);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(6);
				m_icon.setLayoutParams(ll);
				m_titleFr.addView(m_icon);
			}

			m_okBtn = new ImageView(getContext());
			m_okBtn.setImageResource(R.drawable.framework_ok_btn);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
			m_okBtn.setLayoutParams(fl);
			m_topBar.addView(m_okBtn);
			m_okBtn.setOnClickListener(GetBtnLst());
		}

		m_bottomBar = new FrameLayout(getContext());
		fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.BOTTOM;
		m_bottomBar.setLayoutParams(fl);
		this.addView(m_bottomBar);

		m_waitDlg = new WaitDialog1(getContext(), R.style.waitDialog);
	}

	/**
	 * sel_uri Integer
	 * img ImageInfo
	 * @param params
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			m_params = (HashMap<String, Object>)params.clone();

			Object o = params.get("imgs");
			if(o != null)
			{
				m_orgInfo = (ImageStore.ImageInfo)o;
			}
			if(m_orgInfo != null)
			{
				o = params.get("curBmp");
				Bitmap temp = null;
				if(o != null)
				{
					temp = (Bitmap)o;
				}
				DecodeBmp(PhotoPickerPageSite.MakeRotationImg(new String[]{m_orgInfo.image}), temp);
				if(m_org == null || m_org.isRecycled() || m_org.getWidth() == 0 || m_org.getHeight() == 0)
				{
					Toast.makeText(getContext(), getResources().getString(R.string.selectPhotoAgain), Toast.LENGTH_SHORT).show();
					onBack();
				}
				if(m_org != null){

					InitShowView();

					SetViewState(m_bottomBar, true, true);

					o = params.get("imgh");
					if(o != null)
					{
						m_mainImgH = (Float)o;
					}
					float scale1 = (float)m_frW / (float)m_org.getWidth();
					float scale2 = (float)m_frH / (float)m_org.getHeight();
					m_curImgH = m_org.getHeight() * ((scale1 > scale2) ? scale2 : scale1);
					o = params.get("viewh");
					if(o != null)
					{
						m_mainViewH = (Integer)o;
					}
					if(m_mainImgH > 1.0f)
					{
						SetShowViewAnim();
					}
				}
			}
			o = params.get("sel_uri");
			if (o != null)
			{
				m_selUri = (Integer)o;
			}

			Utils.AlphaAnim(m_okBtn, true, 400);
		}
	}

	protected abstract ArrayList<DragListItemInfo> InitListDatas();

	protected void InitResList(boolean showTitle)
	{
		if (m_listDatas != null)
		{
			m_listDatas.clear();
			m_listDatas = null;
		}
		if(m_listAdapter != null)
		{
			m_listAdapter.ClearAll();
			m_listAdapter = null;
		}

		m_listDatas = InitListDatas();
		m_dragItem = new MyDragItemAdapter.MyDragItem(getContext(), new ImageView(getContext()));

		m_resList = new DragListView(getContext());
		m_resList.setCustomDragItem(m_dragItem);
		LinearLayoutManager lin = new LinearLayoutManager(getContext());
		lin.setOrientation(LinearLayoutManager.HORIZONTAL);
		m_resList.getRecyclerView().addOnLayoutChangeListener(new OnLayoutChangeListener()
		{
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom)
			{
				if(m_selUri != -1)
				{
					DragListItemInfo info;
					int index = m_listAdapter.GetIndexByUri(m_selUri);
					if(index != -1)
					{
						info = m_listDatas.get(index);
						OnItemClick(null, info, index);
					}
					m_selUri = -1;
				}
				m_resList.getRecyclerView().removeOnLayoutChangeListener(this);
			}
		});
		m_resList.setLayoutManager(lin);
		m_resList.setCanDragHorizontally(true);
		m_resList.setDragListCallback(m_dragControlCB);
		m_resList.setDragListListener(m_dragListener);

		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.BOTTOM;
		m_resList.setLayoutParams(fl);
		if(m_resListFr != null)
			m_resListFr.addView(m_resList);
		else{
			m_bottomBar.addView(m_resList);
		}

		fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM;
		fl.bottomMargin = m_bottomBarHeight + ShareData.PxToDpi_xhdpi(20);
		m_resList.getRecyclerView().setLayoutParams(fl);
		m_resList.getRecyclerView().setItemAnimator(null);
		m_recycleAnim = new DefaultItemAnimator();

		m_listAdapter = new MyDragItemAdapter(getContext(), true);
		m_listAdapter.showTitle(showTitle);
		m_listAdapter.setItemList(m_listDatas);
		m_listAdapter.SetOnClickCallback(m_listCallback);
		m_resList.setAdapter(m_listAdapter, true);

		m_hideFr = new LinearLayout(getContext());
		m_hideFr.setGravity(Gravity.CENTER);
		m_hideFr.setOrientation(LinearLayout.VERTICAL);
		m_hideFr.setVisibility(GONE);
		m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
		fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		m_hideFr.setLayoutParams(fl);
		m_resList.addView(m_hideFr, 0);
		{
			LinearLayout.LayoutParams ll;
			m_hideIcon = new ImageView(getContext());
			m_hideIcon.setImageResource(R.drawable.framework_delete_icon_out);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_hideIcon.setLayoutParams(ll);
			m_hideFr.addView(m_hideIcon);

			m_hideText = new TextView(getContext());
			m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			m_hideText.setTextColor(Color.WHITE);
			m_hideText.setText(R.string.Delete);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.topMargin = ShareData.PxToDpi_xhdpi(20);
			m_hideText.setLayoutParams(ll);
			m_hideFr.addView(m_hideText);
		}
	}

	protected abstract void SetShowViewAnim();

	protected void SetShowViewAnim(final View view)
	{
		float startScale = m_mainImgH / m_curImgH;
		float endScale = 1;
		AnimationSet as;
		ScaleAnimation sa;
		as = new AnimationSet(true);
		sa = new ScaleAnimation(startScale, endScale, startScale, endScale, m_frW / 2, m_frH / 2);
		sa.setDuration(350);
		as.addAnimation(sa);

		if(m_mainViewH != 0)
		{
			float start = (m_mainViewH - m_frH) / 2f;
			TranslateAnimation ta = new TranslateAnimation(0, 0, start, 0);
			ta.setDuration(350);
			as.addAnimation(ta);
		}

		view.startAnimation(as);
		as.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (view != null) {
					view.clearAnimation();
				}
			}
		});
	}

	protected abstract void InitShowView();

	protected abstract MyBtnLst GetBtnLst();

	protected void DecodeBmp(RotationImg2[] res, Bitmap bmp)
	{
		if(res != null && res.length > 0)
		{
			int rotation = res[0].m_degree;
			int flip = res[0].m_flip;
			Bitmap temp = bmp;
			if(bmp == null)
			{
				temp = Utils.DecodeImage(getContext(), res[0].m_img, rotation, -1, m_frW, m_frH);
			}
			else
			{
				rotation = 0;
				flip = 0;
			}
			if(temp != null)
			{
				int frH = m_frH;
				int frW = m_frW;
				int width = temp.getWidth();
				int height = temp.getHeight();
				if(rotation % 180 != 0)
				{
					width = width + height;
					height = width - height;
					width = width - height;
				}
				if(width < height)
				{
					frH = ShareData.m_screenHeight - m_topBarHeight - m_bottomBarHeight - m_resBarHeight - ShareData.PxToDpi_xhdpi(20);
					frW = frH * 3 / 4;

					m_frW = frW;
					m_frH = frH;
				}
				if(rotation == 0 && flip == MakeBmpV2.FLIP_NONE && temp.getWidth() <= frW && temp.getHeight() <= frH)
				{
					if(temp.isMutable())
					{
						m_org = temp;
					}
					else
					{
						m_org = temp.copy(Bitmap.Config.ARGB_8888, true);
					}
				}
				else
				{
					m_org = MakeBmpV2.CreateBitmapV2(temp, rotation, flip, -1, frW, frH, Bitmap.Config.ARGB_8888);
				}
			}
		}
	}

	protected void SetViewState(View v, boolean isOpen, boolean hasAnimation)
	{
		if (v == null)
			return;
		v.clearAnimation();

		int start;
		int end;
		if (isOpen)
		{
			v.setVisibility(View.VISIBLE);

			start = 1;
			end = 0;
		}
		else
		{
			v.setVisibility(View.GONE);

			start = 0;
			end = 1;
		}

		if (hasAnimation)
		{
			AnimationSet as;
			TranslateAnimation ta;
			as = new AnimationSet(true);
			ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, start, Animation.RELATIVE_TO_SELF, end);
			ta.setDuration(200);
			as.addAnimation(ta);
			v.startAnimation(as);
		}
	}

	protected void SetWaitUI(boolean flag, String str)
	{
		if (flag)
		{
			if (str == null)
			{
				str = "";
			}
			if (m_waitDlg != null)
			{
				m_waitDlg.show();
				m_waitDlg.setText(str);
			}
		}
		else
		{
			if (m_waitDlg != null)
			{
				m_waitDlg.hide();
			}
		}
	}

	protected synchronized void InitBkImg()
	{
		if(m_viewFr != null && (m_bkBmp == null || m_bkBmp.isRecycled()))
		{
			Bitmap bmp = ImageUtil.GetScreenBmp((Activity)getContext(), ShareData.m_screenWidth / 6, ShareData.m_screenHeight / 6);

			m_bkBmp = BeautifyResMgr.MakeBkBmp(bmp, ShareData.m_screenWidth, ShareData.m_screenHeight, 0xcc000000, 0x26000000);
		}
	}

	@Override
	public void onBack()
	{

	}

	protected void showPageMirror(Bitmap mirror)
	{
		m_mirrorImage = new ImageView(getContext());
		m_mirrorImage.setScaleType(ImageView.ScaleType.FIT_XY);
		FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.BOTTOM;
		m_mirrorImage.setLayoutParams(fl);
		this.addView(m_mirrorImage);
		m_mirrorImage.setImageBitmap(mirror);

	}

	@Override
	public void onClose()
	{
		super.onClose();
		if (m_waitDlg != null)
		{
			m_waitDlg.dismiss();
			m_waitDlg = null;
		}
		releaseMem();
	}

	protected void releaseMem()
	{
		m_helpFlag = false;
		this.clearAnimation();
		m_okBtn.clearAnimation();
		this.removeAllViews();
		m_org = null;
		m_bkBmp = null;
		if(m_listDatas != null)
		{
			m_listDatas.clear();
			m_listDatas = null;
			m_listAdapter.notifyDataSetChanged();
		}
		if(m_listAdapter != null)
		{
			m_listAdapter.ClearAll();
			m_listAdapter = null;
		}
		if(m_resList != null)
		{
			m_resList.Clear();
			m_resList.setAdapter(null, true);
			m_listCallback = null;
			m_resList.removeAllViews();
			m_resList = null;
		}
		m_dragControlCB = null;
		m_dragListener = null;

		DownloadMgr.getInstance().RemoveDownloadListener(m_downloadLst);
		m_downloadLst = null;
	}

	public AbsDownloadMgr.DownloadListener m_downloadLst = new AbsDownloadMgr.DownloadListener()
	{
		@Override
		public void OnDataChange(int resType, int downloadId, IDownload[] resArr)
		{
			Log.i("baseBeautify", "resType ：" + resType);
			Log.i("baseBeautify", "resArr ：" + resArr.length);
			if(resArr != null && resArr.length > 0)
			{
				int type = ((BaseRes)resArr[0]).m_resType;
				if(type == ResType.TEXT.GetValue() || type == ResType.LIGHT_EFFECT.GetValue()
						|| type == ResType.FILTER.GetValue())
				{
					boolean flag = true;
					int theme_id = ((BaseRes)resArr[resArr.length - 1]).m_id;
					for(int i = 0; i < resArr.length; i ++)
					{
						if(((BaseRes)resArr[i]).m_type != BaseRes.TYPE_LOCAL_PATH)
						{
							flag = false;
							break;
						}
					}
					boolean need_update = false;
					if(m_themeCompleteFlags.get(theme_id) == null || m_themeCompleteFlags.get(theme_id) == false)
					{
						need_update = true;
					}
					if(flag && need_update)
					{
						m_themeCompleteFlags.put(theme_id, true);
						UpdateListDatas();
					}
				}
			}
		}
	};

	protected void UpdateListDatas()
	{
		if (m_listDatas != null)
		{
			m_listDatas.clear();
			m_listDatas.addAll(InitListDatas());
			if(m_listAdapter != null)
			{
				m_listAdapter.ReleaseMem();
				m_listAdapter.setItemList(m_listDatas);
				m_listAdapter.SelCurUri();
				m_listAdapter.notifyDataSetChanged();
			}
		}
	}

	protected DragListView.DragListCallback m_dragControlCB = new DragListView.DragListCallback()
	{
		@Override
		public boolean canDragItemAtPosition(int dragPosition)
		{
			if(m_listDatas != null && dragPosition >= 0 && dragPosition < m_listDatas.size())
			{
				return m_listDatas.get(dragPosition).m_canDrag;
			}
			return false;
		}

		@Override
		public boolean canDropItemAtPosition(int dropPosition)
		{
			if(m_listDatas != null && dropPosition >= 0 && dropPosition < m_listDatas.size())
			{
				return m_listDatas.get(dropPosition).m_canDrop;
			}
			return false;
		}
	};

	protected abstract boolean canDelete(int position);

	/**隐藏item*/
	protected abstract void OnHideItem(int position);
	/**交换item的顺序*/
	protected abstract void OnChangeItem(int fromPosition, int toPosition);
	protected DragListView.DragListListener m_dragListener = new DragListView.DragListListener()
	{
		@Override
		public void onItemDragStarted(int position)
		{
			m_isChooseHideFr = false;
			m_resList.getRecyclerView().setItemAnimator(m_recycleAnim);
			m_resList.setBackgroundColor(0xb2000000);
//			m_hideFr.setVisibility(VISIBLE);
			if(canDelete(position))
				Utils.AlphaAnim(m_hideFr, true, 400);
		}

		@Override
		public void onItemDragging(int itemPosition, float x, float y)
		{
			boolean laseChoose = m_isChooseHideFr;
			RectF rectF = new RectF(m_hideFr.getLeft(), m_hideFr.getTop(), m_hideFr.getRight(), m_hideFr.getBottom());
			if(x >= rectF.left && x <= rectF.right && y >= rectF.top && y <= rectF.bottom)
			{
				m_isChooseHideFr = true;
				if(m_dragItem != null)
				{
					m_dragItem.DoAlphaAnim(true);
				}
			}
			else
			{
				m_isChooseHideFr = false;
				if(m_dragItem != null)
				{
					m_dragItem.DoAlphaAnim(false);
				}
			}
			if(laseChoose == !m_isChooseHideFr)
			{
				if(m_isChooseHideFr)
				{
					m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_over);
					m_hideIcon.setImageResource(R.drawable.framework_delete_icon_over);
					m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				}
				else
				{
					m_hideFr.setBackgroundResource(R.drawable.framework_hide_bg_out);
					m_hideIcon.setImageResource(R.drawable.framework_delete_icon_out);
					m_hideText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
				}
			}
		}

		@Override
		public void onItemDragEnded(int fromPosition, int toPosition)
		{
			m_resList.getRecyclerView().setItemAnimator(null);
			m_resList.setBackgroundColor(0x00000000);
//			m_hideFr.setVisibility(GONE);
			if(canDelete(fromPosition))
				Utils.AlphaAnim(m_hideFr, false, 400);
			if(m_isChooseHideFr && canDelete(fromPosition))
			{
				m_themeCompleteFlags.clear();
				OnHideItem(fromPosition);
			}
			else if(fromPosition != toPosition)
			{
				OnChangeItem(fromPosition, toPosition);
			}
		}
	};

	/**
	 * 缩略图列表每个item的点击事件
	 * @param view
	 * @param info
	 * @param index
	 */
	public abstract void OnItemClick(View view, DragListItemInfo info, int index);

	/**
	 * 缩略图列表每个item头像的点击事件
	 * @param view
	 * @param info
	 * @param index
	 */
	public abstract void OnHeadClick(View view, DragListItemInfo info, int index);
	protected MyDragItemAdapter.OnItemClickListener m_listCallback = new MyDragItemAdapter.OnItemClickListener()
	{
		@Override
		public void OnItemClick(View view, DragListItemInfo info, int index)
		{
			BaseBeautifyPage.this.OnItemClick(view, info, index);
		}

		@Override
		public void OnHeadClick(View view, DragListItemInfo info, int index)
		{
			BaseBeautifyPage.this.OnHeadClick(view, info, index);
		}
	};

	protected class MyBtnLst implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (m_uiEnabled)
			{
				onClick(v, true);
			}
		}

		public void onClick(View v, boolean fromUser)
		{

		}
	}
}
