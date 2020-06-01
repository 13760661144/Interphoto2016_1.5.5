package cn.poco.beautify;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * 只实现了界面逻辑<BR/>
 * 1.RecomDisplayUI obj = new RecomDisplayUI(...);<BR/>
 * 2.obj.CreateUI();<BR/>
 * 3.obj.SetBk(..); / obj.SetImg(...); / obj.SetContent(...);<BR/>
 * 4.obj.SetImgState(...); / obj.SetBtnState(...);<BR/>
 * 5.obj.Show(...);<BR/>
 * 6.obj.OnCancel();<BR/>
 */
public class RecomDisplayUI
{
	public static final int BTN_STATE_UNLOCK = 0x1;
	public static final int BTN_STATE_LOADING = 0x2;
	public static final int BTN_STATE_DOWNLOAD = 0x4;

	public static final int IMG_STATE_COMPLETE = 0x1;
	public static final int IMG_STATE_LOADING = 0x2;

	protected boolean m_uiEnabled;
	protected Activity m_ac;
	protected RecomDisplayUI.Callback m_cb;

	protected int m_frW;
	protected int m_frH;
	protected int m_fr2W;
	protected int m_fr2H;
	protected int m_imgW;
	protected int m_imgH;

	protected FrameLayout m_parent;
	protected FrameLayout m_fr0;
	protected ImageView m_bk;
	protected Bitmap m_bkBmp;
	protected FrameLayout m_fr1;
	protected FrameLayout m_fr2;
	protected boolean m_animFinish = true;

	protected FrameLayout m_imgFr;
	protected ProgressBar m_imgLoading;
	protected ImageView m_img;
	protected Bitmap m_imgBmp;
	protected int m_imgState;
	protected TextView m_title;
	protected TextView m_content;
	protected ProgressBar m_loading;
	protected ImageView m_btn; //解锁/下载/loading/
	protected int m_btnState;
	protected ImageView m_cancelBtn;

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

		public void OnBtn(int state);
	}

	public RecomDisplayUI(Activity ac, RecomDisplayUI.Callback cb)
	{
		m_ac = ac;
		m_cb = cb;
	}

	public void CreateUI()
	{
		if(m_fr0 == null)
		{
			ShareData.InitData(m_ac);
			m_frW = ShareData.PxToDpi_xhdpi(500);
			m_frH = ShareData.PxToDpi_xhdpi(950);
			m_fr2W = ShareData.PxToDpi_xhdpi(500);
			m_fr2H = ShareData.PxToDpi_xhdpi(650);
			m_imgW = ShareData.PxToDpi_xhdpi(500);
			m_imgH = ShareData.PxToDpi_xhdpi(500);

			m_fr0 = new FrameLayout(m_ac);
			{
				FrameLayout.LayoutParams fl;

				m_bk = new ImageView(m_ac);
				if(m_bkBmp != null)
				{
					m_bk.setBackgroundDrawable(new BitmapDrawable(m_bkBmp));
				}
				else
				{
					m_bk.setBackgroundColor(0xEEFFFFFF);
				}
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

				m_fr1 = new FrameLayout(m_ac);
				fl = new FrameLayout.LayoutParams(m_frW, m_frH);
				fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
				fl.bottomMargin = ShareData.PxToDpi_xhdpi(50);
				m_fr1.setLayoutParams(fl);
				m_fr0.addView(m_fr1);
				{
					m_fr2 = new FrameLayout(m_ac);
					m_fr2.setBackgroundColor(0x8032bd9f);
					fl = new FrameLayout.LayoutParams(m_fr2W, m_fr2H);
					fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
					m_fr2.setLayoutParams(fl);
					m_fr1.addView(m_fr2);
					{
						m_imgFr = new FrameLayout(m_ac);
						fl = new FrameLayout.LayoutParams(m_imgW, m_imgH);
						fl.gravity = Gravity.LEFT | Gravity.TOP;
						m_imgFr.setLayoutParams(fl);
						m_fr2.addView(m_imgFr);
						{
							m_imgLoading = new ProgressBar(m_ac);
							m_imgLoading.setIndeterminateDrawable(m_ac.getResources().getDrawable(R.drawable.unlock_progress));
							m_imgLoading.setVisibility(View.GONE);
							fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(46), ShareData.PxToDpi_xhdpi(46));
							fl.gravity = Gravity.CENTER;
							m_imgLoading.setLayoutParams(fl);
							m_imgFr.addView(m_imgLoading);

							m_img = new ImageView(m_ac);
							m_img.setScaleType(ScaleType.CENTER_CROP);
							fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
							fl.gravity = Gravity.CENTER;
							m_img.setLayoutParams(fl);
							m_imgFr.addView(m_img);
						}

						m_title = new TextView(m_ac);
						m_title.getPaint().setFakeBoldText(true);
						m_title.setSingleLine();
						m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
						m_title.setTextColor(0xffffffff);
						m_title.setGravity(Gravity.LEFT | Gravity.TOP);
						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(320), ShareData.PxToDpi_xhdpi(40));
						fl.gravity = Gravity.LEFT | Gravity.TOP;
						fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
						fl.topMargin = m_imgH + ShareData.PxToDpi_xhdpi(20);
						m_title.setLayoutParams(fl);
						m_fr2.addView(m_title);

						m_content = new TextView(m_ac);
						m_content.setMaxLines(2);
						m_content.setEllipsize(TextUtils.TruncateAt.END);
						m_content.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
						m_content.setTextColor(0xffffffff);
						m_content.setGravity(Gravity.LEFT | Gravity.TOP);
						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(320), ShareData.PxToDpi_xhdpi(60));
						fl.gravity = Gravity.LEFT | Gravity.TOP;
						fl.leftMargin = ShareData.PxToDpi_xhdpi(20);
						fl.topMargin = m_imgH + ShareData.PxToDpi_xhdpi(68);
						m_content.setLayoutParams(fl);
						m_fr2.addView(m_content);

						FrameLayout fr3 = new FrameLayout(m_ac);
						fr3.setBackgroundColor(0xff32bea0);
						fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(150), ShareData.PxToDpi_xhdpi(150));
						fl.gravity = Gravity.RIGHT | Gravity.TOP;
						fl.topMargin = m_imgH;
						fr3.setLayoutParams(fl);
						m_fr2.addView(fr3);
						{
							m_loading = new ProgressBar(m_ac);
							m_loading.setIndeterminateDrawable(m_ac.getResources().getDrawable(R.drawable.unlock_progress));
							m_loading.setVisibility(View.GONE);
							fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(46), ShareData.PxToDpi_xhdpi(46));
							fl.gravity = Gravity.CENTER;
							m_loading.setLayoutParams(fl);
							fr3.addView(m_loading);

							m_btn = new ImageView(m_ac);
							m_btn.setScaleType(ScaleType.CENTER);
							fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
							fl.gravity = Gravity.CENTER;
							m_btn.setLayoutParams(fl);
							fr3.addView(m_btn);
							m_btn.setOnClickListener(m_btnLst);
						}
					}
					m_fr2.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
						}
					});

					m_cancelBtn = new ImageView(m_ac);
					m_cancelBtn.setScaleType(ScaleType.CENTER);
					//m_cancelBtn.setImageResource(R.drawable.homepage_vip_arrow_btn);
					fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
					fl.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
					fl.bottomMargin = ShareData.PxToDpi_xhdpi(50);
					m_cancelBtn.setLayoutParams(fl);
					m_fr1.addView(m_cancelBtn);
					m_cancelBtn.setOnClickListener(m_btnLst);
				}
			}

			SetImgState(m_imgState);
			SetBtnState(m_btnState);
		}
	}

	public void SetBk(Bitmap bkBmp)
	{
		if(m_bk != null)
		{
			m_bk.setBackgroundDrawable(null);
		}
		if(m_bkBmp != null)
		{
			m_bkBmp.recycle();
			m_bkBmp = null;
		}
		m_bkBmp = bkBmp;
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
		if(m_img != null)
		{
			m_img.setImageBitmap(null);
		}

		if(res != null)
		{
			Bitmap temp = Utils.DecodeImage(m_ac, res, 0, -1, m_imgW, m_imgH);
			m_imgBmp = MakeBmp.CreateBitmap(temp, m_imgW, m_imgH, -1, 0, Config.ARGB_8888);
			temp.recycle();
			temp = null;
			if(m_img != null)
			{
				m_img.setImageBitmap(m_imgBmp);
			}
		}
	}

	public void SetContent(String name, String content)
	{
		if(m_title != null)
		{
			m_title.setText(name);
		}
		if(m_content != null)
		{
			m_content.setText(content);
		}
	}

	public void SetImgState(int state)
	{
		switch(state)
		{
			case IMG_STATE_COMPLETE:
				if(m_imgLoading != null)
				{
					m_imgLoading.setVisibility(View.GONE);
				}
				if(m_img != null)
				{
					m_img.setVisibility(View.VISIBLE);
				}

				m_imgState = state;
				break;

			case IMG_STATE_LOADING:
				if(m_imgLoading != null)
				{
					m_imgLoading.setVisibility(View.VISIBLE);
				}
				if(m_img != null)
				{
					m_img.setVisibility(View.GONE);
				}

				m_imgState = state;
				break;

			default:
				break;
		}
	}

	public void SetBtnState(int state)
	{
		switch(state)
		{
			case BTN_STATE_UNLOCK:
			{
				if(m_loading != null)
				{
					m_loading.setVisibility(View.GONE);
				}
				if(m_btn != null)
				{
					//m_btn.setImageResource(R.drawable.unlock_icon);
					m_btn.setVisibility(View.VISIBLE);
				}

				m_btnState = state;
				break;
			}

			case BTN_STATE_LOADING:
			{
				if(m_loading != null)
				{
					m_loading.setVisibility(View.VISIBLE);
				}
				if(m_btn != null)
				{
					m_btn.setVisibility(View.GONE);
				}

				m_btnState = state;
				break;
			}

			case BTN_STATE_DOWNLOAD:
			{
				if(m_loading != null)
				{
					m_loading.setVisibility(View.GONE);
				}
				if(m_btn != null)
				{
					//m_btn.setImageResource(R.drawable.unlock_download_btn);
					m_btn.setVisibility(View.VISIBLE);
				}

				m_btnState = state;
				break;
			}

			default:
				break;
		}
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
		if(m_fr1 != null)
		{
			m_fr1.clearAnimation();
			m_bk.clearAnimation();

			TranslateAnimation ta = null;
			AlphaAnimation aa = null;
			if(isOpen)
			{
				m_fr1.setVisibility(View.VISIBLE);

				if(hasAnimation)
				{
					ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_SELF, 0);
					aa = new AlphaAnimation(0, 1);
				}
			}
			else
			{
				m_fr1.setVisibility(View.GONE);

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
				m_fr1.startAnimation(as);

				aa.setDuration(350);
				m_bk.startAnimation(aa);
			}
		}
	}

	public void OnCancel()
	{
		if(m_uiEnabled)
		{
			m_uiEnabled = false;

			SetFr1State(false, true, new AnimationListener()
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
						if(m_fr1 != null)
						{
							m_fr1.clearAnimation();
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

	protected View.OnClickListener m_btnLst = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == m_btn)
			{
				if(m_cb != null)
				{
					m_cb.OnBtn(m_btnState);
				}
			}
			else if(v == m_cancelBtn)
			{
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
