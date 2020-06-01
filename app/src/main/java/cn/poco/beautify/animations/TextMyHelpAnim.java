package cn.poco.beautify.animations;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.site.TextMyHelpAnimSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.setting.LanguagePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;

/**
 * 文字-我的帮助页
 */
public class TextMyHelpAnim extends IPage
{
	protected TextMyHelpAnimSite m_site;

	protected FrameLayout m_topBar;
//	protected ImageView m_backBtn;
	protected LinearLayout m_titleFr;
	protected ImageView m_exitBtn;

	protected ScrollView m_scroll;
	protected TextAnim1 m_anim1;
	protected TextAnim2 m_anim2;
	protected int m_textSize = 16;
	public TextMyHelpAnim(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (TextMyHelpAnimSite)site;
		init();
		MyBeautyStat.onClickByRes(R.string.我的_打开我的动画);
	}
	private void init()
	{
		if(!TagMgr.CheckTag(getContext(), LanguagePage.ENGLISH_TAGVALUE))
		{
			m_textSize = 10;
		}
		LayoutParams fl;
		this.setBackgroundColor(0xff333333);
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		Bitmap bk1 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0xcc383838, 0x1e383838);
		if(bmp != null)
		{
			bmp.recycle();
		}

		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.light_scale_bg);
		Bitmap bk2 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0xcc383838, 0x1e383838);
		if(bmp != null)
		{
			bmp.recycle();
		}

		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
		Bitmap bk3 = BeautifyResMgr.MakeBkBmp(bmp, ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(100), 0x99000000, 0xaa000000);
		if(bmp != null)
		{
			bmp.recycle();
		}
		m_topBar = new FrameLayout(getContext());
		m_topBar.setBackgroundDrawable(new BitmapDrawable(bk3));
		fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(80));
		fl.gravity = Gravity.TOP;
		m_topBar.setLayoutParams(fl);
		this.addView(m_topBar);
		{
			/*m_backBtn = new ImageView(getContext());
			m_backBtn.setImageResource(R.drawable.framework_back_btn);
			fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			m_backBtn.setLayoutParams(fl);
			m_topBar.addView(m_backBtn);
			m_backBtn.setOnClickListener(m_btnLst);*/

			m_titleFr = new LinearLayout(getContext());
			m_titleFr.setOnClickListener(m_btnLst);
			m_titleFr.setGravity(Gravity.CENTER);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.CENTER;
			m_titleFr.setLayoutParams(fl);
			m_topBar.addView(m_titleFr);
			{
				LinearLayout.LayoutParams ll;
				TextView text = new TextView(getContext());
				text.setTextColor(0xffffffff);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				text.setText(getResources().getString(R.string.Mine));
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				text.setLayoutParams(ll);
				m_titleFr.addView(text);

				ImageView img = new ImageView(getContext());
				img.setImageResource(R.drawable.beautify_effect_help_up);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(6);
				img.setLayoutParams(ll);
				m_titleFr.addView(img);
			}
		}

		m_scroll = new ScrollView(getContext());
		fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.TOP;
		m_scroll.setLayoutParams(fl);
		this.addView(m_scroll, 0);
		{
			LinearLayout lin = new LinearLayout(getContext());
			lin.setOrientation(LinearLayout.VERTICAL);
			fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.TOP;
			lin.setLayoutParams(fl);
			m_scroll.addView(lin);
			{
				LinearLayout.LayoutParams ll;

				FrameLayout fr = new FrameLayout(getContext());
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(580));
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(80+60);
				fr.setLayoutParams(ll);
				lin.addView(fr);
				{
					m_anim1 = new TextAnim1(getContext(), null);
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.TOP;
					m_anim1.setLayoutParams(fl);
					fr.addView(m_anim1);

					TextView text = new TextView(getContext());
					text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
					text.setGravity(Gravity.CENTER_VERTICAL);
					text.setBackgroundDrawable(new BitmapDrawable(bk1));
					text.setText(getResources().getString(R.string.tipsAddwaterMark));
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
					text.setTextColor(Color.WHITE);
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
					fl.gravity = Gravity.BOTTOM;
					text.setLayoutParams(fl);
					fr.addView(text);
				}

				fr = new FrameLayout(getContext());
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(580), ShareData.PxToDpi_xhdpi(580));
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(50);
//				ll.bottomMargin = ShareData.PxToDpi_xhdpi(60);
				fr.setLayoutParams(ll);
				lin.addView(fr);
				{
                    m_anim2 = new TextAnim2(getContext());
                    m_anim2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
							m_anim2.startFirstAnimation();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                m_anim2.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else{
                                m_anim2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                          }
                        }
                    });
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(480));
					fl.gravity = Gravity.TOP;
					m_anim2.setLayoutParams(fl);
					fr.addView(m_anim2);

					TextView text = new TextView(getContext());
					text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
					text.setGravity(Gravity.CENTER_VERTICAL);
					text.setBackgroundDrawable(new BitmapDrawable(bk2));
					text.setText(getResources().getString(R.string.deleteAndRename));
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
					text.setTextColor(Color.WHITE);
					fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
					fl.gravity = Gravity.BOTTOM;
					text.setLayoutParams(fl);
					fr.addView(text);
				}

				m_exitBtn = new ImageView(getContext());
				m_exitBtn.setImageResource(R.drawable.help_anim_exit_btn);
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_HORIZONTAL;
				ll.topMargin = ShareData.PxToDpi_xhdpi(50);
				ll.bottomMargin = ShareData.PxToDpi_xhdpi(50);
				m_exitBtn.setLayoutParams(ll);
				lin.addView(m_exitBtn);
				m_exitBtn.setOnClickListener(m_btnLst);

			}
		}
	}

	protected OnClickListener m_btnLst = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == m_titleFr || v == m_exitBtn)
			{
				onBack();
			}
		}
	};

	/**
	 *
	 * @param params
	 * Bitmap: img
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params != null)
		{
			Object o = params.get("img");
			if(o != null)
			{
				Bitmap img = (Bitmap)o;
				m_scroll.setBackgroundDrawable(new BitmapDrawable(img));
			}
		}
	}

	@Override
	public void onBack()
	{
		m_site.OnBack(getContext());
	}

	@Override
	public void onClose()
	{
		m_scroll.setBackgroundDrawable(null);
		if(m_anim1 != null)
		{
			m_anim1.release();
			m_anim1 = null;
		}
		if(m_anim2 != null)
		{
			m_anim2.release();
			m_anim2 = null;
		}
		this.removeAllViews();
		super.onClose();
		MyBeautyStat.onClickByRes(R.string.我的动画_收起我的动画);
	}
}
