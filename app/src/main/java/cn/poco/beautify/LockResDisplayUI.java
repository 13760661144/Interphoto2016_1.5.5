package cn.poco.beautify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.resource.LockRes;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.ImageUtil;
import cn.poco.utils.Utils;

public class LockResDisplayUI {
	public static final int LAYOUT_UNLOCK = 0x1;
	public static final int LAYOUT_SUCCESS = 0x2;

	public static final int IMG_STATE_COMPLETE = 0x1;
	public static final int IMG_STATE_LOADING = 0x2;

	protected boolean m_uiEnabled;
	protected Activity m_ac;
	protected LockResDisplayUI.Callback m_cb;

	protected int m_imgW;
	protected int m_imgH;

	protected FrameLayout m_parent;
	protected FrameLayout m_fr0;
	protected ImageView m_bk;
	protected Bitmap m_bkBmp;
	protected FrameLayout m_fr1;
	protected LinearLayout m_fr2;
	protected boolean m_animFinish = true;

	protected FrameLayout m_imgFr;
	protected ProgressBar m_imgLoading;
	protected Bitmap m_imgBmp;
	protected int m_imgState;
	protected ProgressBar m_loading;
//	protected ImageView m_btn; //解锁/下载/loading/
	protected int m_btnState;
	protected ImageView m_cancelBtn;
	
	protected TextView shareBtn;
	protected TextView m_lockSuccessBtn;
	protected TextView m_textContent;
	protected TextView m_textTitle;
	protected ImageView m_showImg;
	protected FrameLayout m_lockSuccessLayout;
	protected FrameLayout m_centerLayout;
	protected int unlockType;

	public interface Callback
	{
		/**
		 * 完成关闭动画后回调
		 */
		public void OnClose();

		/**
		 * 点击关闭或空白地方
		 */
		public void OnCloseBtn();

		public void OnBtn();
	}

	public LockResDisplayUI(Activity ac, LockResDisplayUI.Callback cb)
	{
		m_ac = ac;
		m_cb = cb;
	}

	@SuppressLint("NewApi")
	public void CreateUI()
	{
		if(m_fr0 == null)
		{
			ShareData.InitData(m_ac);
			m_imgW = ShareData.PxToDpi_xhdpi(520);
			m_imgH = ShareData.PxToDpi_xhdpi(520);

			m_fr0 = new FrameLayout(m_ac);
			FrameLayout.LayoutParams fl;
			fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, ShareData.m_screenHeight);
			fl.gravity = Gravity.LEFT | Gravity.TOP;
			m_fr0.setLayoutParams(fl);
			{
				m_bk = new ImageView(m_ac);
//				if(m_bkBmp != null)
//				{
//					m_bk.setBackgroundDrawable(new BitmapDrawable(m_bkBmp));
//				}
//				else
//				{
//					m_bk.setBackgroundColor(0xEEFFFFFF);
//				}
				fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, ShareData.m_screenHeight);
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				m_bk.setLayoutParams(fl);
				m_fr0.addView(m_bk);
				m_bk.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(m_cb != null)
						{
							m_cb.OnCloseBtn();
						}
					}
				});
				m_centerLayout = new FrameLayout(m_ac);
				fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, ShareData.m_screenHeight);
				fl.gravity = Gravity.LEFT | Gravity.TOP;
				m_centerLayout.setLayoutParams(fl);
				m_fr0.addView(m_centerLayout);

				m_fr1 = new FrameLayout(m_ac);
				fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(550),
						FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
				fl.leftMargin = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(520))/2;
				m_fr1.setLayoutParams(fl);
				m_centerLayout.addView(m_fr1);
				{
					m_fr2 = new LinearLayout(m_ac);
					m_fr2.setBackgroundColor(Color.WHITE);
					m_fr2.setOrientation(LinearLayout.VERTICAL);
					fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(520),
							FrameLayout.LayoutParams.WRAP_CONTENT);
					fl.topMargin = ShareData.PxToDpi_xhdpi(30);
					fl.gravity = Gravity.LEFT;
					m_fr2.setLayoutParams(fl);
					m_fr1.addView(m_fr2);
					{
						
						// TODO Auto-generated method stub
						FrameLayout showImgLayout = new FrameLayout(m_ac);
						LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(520),ShareData.PxToDpi_xhdpi(520));
						showImgLayout.setLayoutParams(ll);
						m_fr2.addView(showImgLayout);
						
						m_showImg = new ImageView(m_ac);
						ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(520),ShareData.PxToDpi_xhdpi(520));
						m_showImg.setLayoutParams(ll);
						m_showImg.setVisibility(View.GONE);
						showImgLayout.addView(m_showImg);


						m_imgLoading = new ProgressBar(m_ac);
//						m_imgLoading.setIndeterminateDrawable(m_ac.getResources().getDrawable(R.drawable.unlock_progress));
						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(46), ShareData.PxToDpi_xhdpi(46));
						fl.gravity = Gravity.CENTER;
						m_imgLoading.setLayoutParams(fl);
						showImgLayout.addView(m_imgLoading);
						
						m_textTitle = new TextView(m_ac);
						ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
						ll.topMargin = ShareData.PxToDpi_xhdpi(20);
						ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
//						title.setText(m_title);
						m_textTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
						m_textTitle.setTextColor(0xff000000);
						m_textTitle.setLayoutParams(ll);
						m_fr2.addView(m_textTitle);
						
						m_textContent = new TextView(m_ac);
						ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
						ll.topMargin = ShareData.PxToDpi_xhdpi(20);
						ll.leftMargin = ShareData.PxToDpi_xhdpi(20);
						ll.rightMargin = ShareData.PxToDpi_xhdpi(20);
						m_textContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP,12f);
						m_textContent.setTextColor(0xff666666);
						m_textContent.setLineSpacing(ShareData.PxToDpi_xhdpi(10),1.0f);
						m_textContent.setLayoutParams(ll);
//						setText(m_content1,m_content2);
						m_fr2.addView(m_textContent);
						
					    shareBtn = new TextView(m_ac);
					    shareBtn.setGravity(Gravity.CENTER);
					    ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(480),ShareData.PxToDpi_xhdpi(60));
					    ll.topMargin = ShareData.PxToDpi_xhdpi(30);
					    ll.gravity = Gravity.CENTER_HORIZONTAL;
						shareBtn.setText(m_ac.getResources().getString(R.string.shareToWechat));
						shareBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f);
						shareBtn.setTextColor(Color.WHITE);
						shareBtn.setBackgroundResource(R.drawable.beautify_declock);
						ll.setMargins(0, ShareData.PxToDpi_xhdpi(20), 0, ShareData.PxToDpi_xhdpi(30));
						shareBtn.setLayoutParams(ll);
						shareBtn.setOnClickListener(m_btnLst);
						m_fr2.addView(shareBtn);
						
						m_cancelBtn = new ImageView(m_ac);
						fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
								FrameLayout.LayoutParams.WRAP_CONTENT);
						fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
						m_cancelBtn.setBackgroundResource(R.drawable.beauty_lock_cancel_logo);
						m_cancelBtn.setLayoutParams(fl);
						m_cancelBtn.setOnClickListener(m_btnLst);
						m_centerLayout.addView(m_cancelBtn);

//						ImageView bight = new ImageView(m_ac);
//						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(520),ShareData.PxToDpi_xhdpi(520));
//						fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
//						bight.setLayoutParams(fl);
//						bight.setBackgroundColor(Color.GRAY);
//						m_centerLayout.addView(bight);

					}
					m_fr1.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
						}
					});

					m_fr1.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
						@Override
						public boolean onPreDraw() {
							m_fr1.getViewTreeObserver().removeOnPreDrawListener(this);
							final int h = m_fr1.getMeasuredHeight();
							ViewGroup.MarginLayoutParams params;
							params = (ViewGroup.MarginLayoutParams)m_cancelBtn.getLayoutParams();
							params.bottomMargin = (int)((ShareData.m_screenHeight - h - ShareData.PxToDpi_xhdpi(23)*2)/4f);
							m_cancelBtn.setLayoutParams(params);
							return false;
						}
					});
					
					{
						m_lockSuccessLayout = new FrameLayout(m_ac);
						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(480),ShareData.PxToDpi_xhdpi(300));
						fl.gravity = Gravity.CENTER;
						m_lockSuccessLayout.setVisibility(View.GONE);
						m_lockSuccessLayout.setLayoutParams(fl);
						m_centerLayout.addView(m_lockSuccessLayout);
						{
							LinearLayout linear = new LinearLayout(m_ac);
							linear.setOrientation(LinearLayout.VERTICAL);
							fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(480),ShareData.PxToDpi_xhdpi(240));
							fl.gravity = Gravity.BOTTOM;
							linear.setLayoutParams(fl);
							m_lockSuccessLayout.addView(linear);
							TextView img1 = new TextView(m_ac);
							img1.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18f);
							img1.setTextColor(Color.BLACK);
							img1.setPadding(0, ShareData.PxToDpi_xhdpi(98), 0, 0);
							img1.setGravity(Gravity.CENTER_HORIZONTAL);
							LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(480),ShareData.PxToDpi_xhdpi(180));
							img1.setLayoutParams(ll);
							Bitmap bmp = ImageUtil.createBitmapByColor(Color.WHITE, ShareData.PxToDpi_xhdpi(480), ShareData.PxToDpi_xhdpi(180));
//							bmp = ImageUtil.MakeDiffCornerRoundBmp(bmp, ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10), 0, 0);
						    img1.setBackgroundDrawable(new BitmapDrawable(bmp));
						    img1.setText(m_ac.getResources().getString(R.string.UnlockSuccessful));
						    linear.addView(img1);
						    
						    m_lockSuccessBtn = new TextView(m_ac);
						    ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(480),ShareData.PxToDpi_xhdpi(60));
						    m_lockSuccessBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
							m_lockSuccessBtn.setTextColor(0xffffffff);
							m_lockSuccessBtn.setGravity(Gravity.CENTER_HORIZONTAL);
							m_lockSuccessBtn.setText(m_ac.getResources().getString(R.string.ok));
							m_lockSuccessBtn.setLayoutParams(ll);
							linear.addView(m_lockSuccessBtn);
						    Bitmap bmpNormal = ImageUtil.createBitmapByColor(0xffffc433, 480, 60);
//						    bmpNormal = ImageUtil.MakeDiffCornerRoundBmp(bmpNormal, 0, 0, ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10));
						    Bitmap bmpPress = ImageUtil.createBitmapByColor(0xffffd670, 480, 60);
//						    bmpPress = ImageUtil.MakeDiffCornerRoundBmp(bmpPress, 0, 0, ShareData.PxToDpi_xhdpi(10), ShareData.PxToDpi_xhdpi(10));
						    BitmapDrawable normal = new BitmapDrawable(bmpNormal);
						    BitmapDrawable press = new BitmapDrawable(bmpPress);
						    StateListDrawable bg = new StateListDrawable();
						    bg.addState(new int[]{android.R.attr.state_pressed,android.R.attr.enabled}, press);
						    bg.addState(new int[]{android.R.attr.enabled}, normal);
						    bg.addState(new int[]{}, normal);
						    m_lockSuccessBtn.setBackgroundDrawable(bg);
						    m_lockSuccessBtn.setOnClickListener(m_btnLst);
						    
						    ImageView top = new ImageView(m_ac);
						    fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
						    fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
						    top.setLayoutParams(fl);
						    top.setImageResource(R.drawable.beautify_unlocksuccess_logo1);
						    m_lockSuccessLayout.addView(top);
						}
					}

				}
			}

			SetImgState(IMG_STATE_LOADING);
			SetLayoutState(LAYOUT_UNLOCK);
		}
	}

	public void SetBk(Object bkBmp)
	{
		if(m_bk != null)
		{
			m_bk.setBackgroundDrawable(null);
		}
//		if(m_bkBmp != null)
//		{
//			m_bkBmp.recycle();
//			m_bkBmp = null;
//		}
		if(bkBmp != null && bkBmp instanceof String)
		{
			m_bkBmp = Utils.DecodeFile((String)bkBmp, null);
		}
		else if(bkBmp instanceof Bitmap)
		{
			m_bkBmp = (Bitmap)bkBmp;
		}
		if(m_bk != null)
		{
			m_bk.setBackgroundDrawable(new BitmapDrawable(m_bkBmp));
		}
	}

	public void SetImg(Object res)
	{
		if(m_imgBmp != null)
		{
			m_imgBmp.recycle();
			m_imgBmp = null;
		}
		if(m_showImg != null)
		{
			m_showImg.setImageBitmap(null);
		}

		if(res != null)
		{
			Bitmap temp = Utils.DecodeImage(m_ac, res, 0, -1, m_imgW, m_imgH);
			m_imgBmp = MakeBmp.CreateBitmap(temp, m_imgW, m_imgH, -1, 0, Config.ARGB_8888);
			temp.recycle();
			temp = null;
			if(m_showImg != null)
			{
				m_showImg.setImageBitmap(m_imgBmp);
			}
			
		}
	}
	
	
	public void SetImgState(int state){
		switch (state) {
		case IMG_STATE_LOADING:
			if(m_showImg != null)
			{
				m_showImg.setVisibility(View.GONE);
			}
			if(m_imgLoading != null){
				m_imgLoading.setVisibility(View.VISIBLE);
			}
			break;
        case IMG_STATE_COMPLETE:
        	if(m_showImg != null)
			{
				m_showImg.setVisibility(View.VISIBLE);
			}
			if(m_imgLoading != null){
				m_imgLoading.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
	}
	
	public void SetLayoutState(int state){
		switch (state) {
		case LAYOUT_UNLOCK:
			if(m_fr1 != null)
			{
				m_fr1.setVisibility(View.VISIBLE);
			}
			
			if(m_lockSuccessLayout != null)
			{
				m_lockSuccessLayout.setVisibility(View.GONE);
			}
			break;
		case LAYOUT_SUCCESS:
			if(m_fr1 != null)
			{
				m_fr1.setVisibility(View.GONE);
			}
			
			if(m_lockSuccessLayout != null)
			{
				m_lockSuccessLayout.setVisibility(View.VISIBLE);
			}
			break;

		default:
			break;
		}
	}

	public void SetContent(String title, String m_showContent)
	{
//		String content = "分享到朋友圈，就能解锁使用[" + title + "]文字";
		String content = m_showContent;
		SpannableString string = new SpannableString(content);
		string.setSpan(new ForegroundColorSpan(0xff666666), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		string.setSpan(new AbsoluteSizeSpan(12,true), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		m_textContent.setText(string);
		m_textTitle.setText(title); 
	}

	
	public void setShareType(int type){
		this.unlockType = type;
	}

	public boolean IsShow()
	{
		boolean out = false;

		if(m_parent != null && m_fr0 != null)
		{
			int len = m_parent.getChildCount();
			for(int i = 0; i < len; i++)
			{
				if(m_parent.getChildAt(i) == m_fr0)
				{
					out = true;
					break;
				}
			}
		}

		return out;
	}

	public void Show(FrameLayout parent)
	{
		if(m_animFinish && m_fr0 != null)
		{
			m_parent = parent;
			m_uiEnabled = true;

			if(m_parent != null && m_fr0 != null)
			{
				m_parent.removeView(m_fr0);
				FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ShareData.m_screenWidth, ShareData.m_screenHeight);
				m_fr0.setLayoutParams(fl);
				m_parent.addView(m_fr0);

				m_animFinish = false;
				SetFr1State(true, true, new AnimationListener()
				{
					@Override
					public void onAnimationStart(Animation animation)
					{
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationRepeat(Animation animation)
					{
						// TODO Auto-generated method stub
					}

					@Override
					public void onAnimationEnd(Animation animation)
					{
						m_animFinish = true;
					}
				});
			}
		}
	}

	protected void SetFr1State(boolean isOpen, boolean hasAnimation, AnimationListener lst)
	{
		if(m_centerLayout != null)
		{
			m_centerLayout.clearAnimation();
			m_bk.clearAnimation();

			TranslateAnimation ta = null;
			AlphaAnimation aa = null;
			if(isOpen)
			{
				m_centerLayout.setVisibility(View.VISIBLE);

				if(hasAnimation)
				{
					ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, -1, Animation.RELATIVE_TO_SELF, 0);
					aa = new AlphaAnimation(0, 1);
				}
			}
			else
			{
				m_centerLayout.setVisibility(View.GONE);

				if(hasAnimation)
				{
					ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1);
					aa = new AlphaAnimation(1, 0);
				}
			}

			if(hasAnimation)
			{
				AnimationSet as;
				as = new AnimationSet(true);
				ta.setDuration(350);
				as.addAnimation(ta);
				as.setAnimationListener(lst);
				m_centerLayout.startAnimation(as);

				aa.setDuration(350);
				m_bk.startAnimation(aa);
			}
		}
	}

	public void OnCancel(boolean hasAnim)
	{
		if(m_uiEnabled)
		{
			m_uiEnabled = false;

			SetFr1State(false, hasAnim, new AnimationListener()
			{
				@Override
				public void onAnimationStart(Animation animation)
				{
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationRepeat(Animation animation)
				{
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationEnd(Animation animation)
				{
					if(m_parent != null && m_fr0 != null)
					{
						m_parent.removeView(m_fr0);
						if(m_centerLayout != null)
						{
							m_centerLayout.clearAnimation();
						}
						if(m_bk != null)
						{
							m_bk.clearAnimation();
						}
					}

					if(m_cb != null)
					{
						m_cb.OnClose();
					}
				}
			});
		}
	}
	
	protected void promptDlg(final int unlock_type) {
		// TODO Auto-generated method stub
		AlertDialog.Builder dlg = new AlertDialog.Builder(m_ac);
		dlg.setTitle(m_ac.getResources().getString(R.string.unlock));
		if(unlock_type == LockRes.SHARE_TYPE_WEIXIN){
			dlg.setMessage(m_ac.getResources().getString(R.string.shareToWechatuseTips));
		}
		else{
			dlg.setMessage(m_ac.getResources().getString(R.string.canuseTips));
		}
		dlg.setPositiveButton(m_ac.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(m_cb != null){
					m_cb.OnBtn();
				}
				dialog.dismiss();
			}
		});
		dlg.setNegativeButton(m_ac.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(m_cb != null){
					m_cb.OnCloseBtn();
				}
				dialog.dismiss();
			}
		});
		dlg.create();
		dlg.show();
	}

	protected View.OnClickListener m_btnLst = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == shareBtn)
			{
//				promptDlg(unlockType);
				if(m_cb != null){
					m_cb.OnBtn();
				}
			}
			else if(v == m_cancelBtn)
			{
				if(m_cb != null)
				{
					m_cb.OnCloseBtn();
				}
			}
			if(v == m_lockSuccessBtn){
				if(m_cb != null)
				{
					m_cb.OnCloseBtn();
				}
			}
		}
	};

	public void ClearAll()
	{
		SetBk(null);
		SetImg(null);
	}
}
