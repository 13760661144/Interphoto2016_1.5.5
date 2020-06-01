package cn.poco.video.page;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.HashMap;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.beautify.animations.TextAnim3;
import cn.poco.beautify.animations.TextAnim4;
import cn.poco.beautify.site.TextHelpAnimSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.setting.LanguagePage;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;
import cn.poco.video.NativeUtils;
import cn.poco.video.site.VideoMosaicHelpSite;

/**
 * 视频拼接帮助页
 */
public class VideoEditHelpAnim extends IPage
{
	private String animPath1;
	private String animPath2;
	private String animPath3;
	protected VideoMosaicHelpSite m_site;

	protected FrameLayout m_topBar;
//	protected ImageView m_backBtn;
	protected LinearLayout m_titleFr;
	private TextView m_title;
	protected ImageView m_exitBtn;

	protected ScrollView m_scroll;
	protected VideoView m_anim1;
	protected VideoView m_anim2;
	protected VideoView m_anim3;
	protected ImageView m_anim1Icon;
	protected ImageView m_anim2Icon;
	protected ImageView m_anim3Icon;
	protected int m_textSize = 16;
	private boolean mCanBack;
	public VideoEditHelpAnim(Context context, BaseSite site)
	{
		super(context, site);
		m_site = (VideoMosaicHelpSite)site;
		init();
		String packageName = getContext().getPackageName();
		animPath1 = "android.resource://" + packageName + "/" + R.raw.video_edit_help_tip1;
		animPath2 = "android.resource://" + packageName + "/" + R.raw.video_edit_help_tip2;
		animPath3 = "android.resource://" + packageName + "/" + R.raw.video_edit_help_tip3;
		MyBeautyStat.onClickByRes(R.string.照片文字_打开文字动画1);
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
			m_titleFr = new LinearLayout(getContext());
			m_titleFr.setOnClickListener(m_btnLst);
			m_titleFr.setGravity(Gravity.CENTER);
			fl = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			fl.gravity = Gravity.CENTER;
			m_titleFr.setLayoutParams(fl);
			m_topBar.addView(m_titleFr);
			{
				LinearLayout.LayoutParams ll;

				m_title = new TextView(getContext());
				m_title.setTextColor(0xffffffff);
				m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				m_title.setText(getResources().getString(R.string.footage));
				ll = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				m_title.setLayoutParams(ll);
				m_titleFr.addView(m_title);

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
					m_anim1 = new VideoView(getContext());
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.TOP;
					m_anim1.setLayoutParams(fl);
					fr.addView(m_anim1);

					m_anim1Icon = new ImageView(getContext());
					m_anim1Icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.TOP;
					m_anim1Icon.setLayoutParams(fl);
					fr.addView(m_anim1Icon);

					TextView text = new TextView(getContext());
					text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
					text.setGravity(Gravity.CENTER_VERTICAL);
					text.setBackgroundDrawable(new BitmapDrawable(bk1));
					text.setText(getResources().getString(R.string.video_edit_help1));
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
					text.setTextColor(Color.WHITE);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
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
                    m_anim2 = new VideoView(getContext(), null);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(480));
					fl.gravity = Gravity.TOP;
					m_anim2.setLayoutParams(fl);
					fr.addView(m_anim2);

					m_anim2Icon = new ImageView(getContext());
					m_anim2Icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.TOP;
					m_anim2Icon.setLayoutParams(fl);
					fr.addView(m_anim2Icon);

					TextView text = new TextView(getContext());
					text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
					text.setGravity(Gravity.CENTER_VERTICAL);
					text.setBackgroundDrawable(new BitmapDrawable(bk2));
					text.setText(R.string.video_edit_help2);
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
					text.setTextColor(Color.WHITE);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
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
					m_anim3 = new VideoView(getContext(), null);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(480));
					fl.gravity = Gravity.TOP;
					m_anim3.setLayoutParams(fl);
					fr.addView(m_anim3);

					m_anim3Icon = new ImageView(getContext());
					m_anim3Icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
					fl.gravity = Gravity.TOP;
					m_anim3Icon.setLayoutParams(fl);
					fr.addView(m_anim3Icon);

					TextView text = new TextView(getContext());
					text.setPadding(ShareData.PxToDpi_xhdpi(30), 0, 0, 0);
					text.setGravity(Gravity.CENTER_VERTICAL);
					text.setBackgroundDrawable(new BitmapDrawable(bk2));
					text.setText(R.string.video_edit_help3);
					text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, m_textSize);
					text.setTextColor(Color.WHITE);
					fl = new LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
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
	 * Integer: Integer;
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
		m_scroll.setSmoothScrollingEnabled(false);
		mCanBack = false;
		m_anim1Icon.setImageResource(R.drawable.video_edit_help_thumb1);
		m_anim2Icon.setImageResource(R.drawable.video_edit_help_thumb2);
		m_anim3Icon.setImageResource(R.drawable.video_edit_help_thumb3);
		DoAnim();
	}

	private int m_scrollTop = 0;
	private int m_scrollBottom = 0;
	private void DoAnim(){
		TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
		ta.setDuration(350);
		Animation.AnimationListener animLst = new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				mCanBack = true;
				m_scroll.setSmoothScrollingEnabled(true);
				m_scroll.scrollTo(0, 0);
				int[] location = new int[2];
				m_scroll.getLocationOnScreen(location);
				m_scrollTop = location[1];
				m_scrollBottom = m_scrollTop + m_scroll.getHeight();

				m_anim1.setVideoURI(Uri.parse(animPath1));
				m_anim1.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					@Override
					public void onCompletion(MediaPlayer mp)
					{
						boolean flag = startVideo(m_anim2, m_anim2Icon);
						if(!flag)
						{
							flag = startVideo(m_anim3, m_anim3Icon);
						}
						if(!flag){
							startVideo(m_anim1, m_anim1Icon);
						}
					}
				});
				m_anim1.start();
				m_anim1Icon.setVisibility(GONE);

				m_anim2.setVideoURI(Uri.parse(animPath2));
				m_anim2.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					@Override
					public void onCompletion(MediaPlayer mp)
					{
						boolean flag = startVideo(m_anim3, m_anim3Icon);

						if(!flag)
						{
							flag = startVideo(m_anim1, m_anim1Icon);
						}
						if(!flag){
							startVideo(m_anim2, m_anim2Icon);
						}
					}
				});
				m_anim3.setVideoURI(Uri.parse(animPath3));
				m_anim3.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					@Override
					public void onCompletion(MediaPlayer mp)
					{
						boolean flag = startVideo(m_anim1, m_anim1Icon);

						if(!flag)
						{
							flag = startVideo(m_anim2, m_anim2Icon);
						}
						if(!flag){
							startVideo(m_anim3, m_anim3Icon);
						}
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		};
		ta.setAnimationListener(animLst);
		this.startAnimation(ta);
	}

	private boolean startVideo(VideoView view, ImageView icon){
		boolean flag = false;
		if(view != null){
			int[] location = new int[2];
			view.getLocationOnScreen(location);
			float y = location[1];
			float yBottom = y + view.getHeight();
			if(y >= m_scrollTop && y <= m_scrollBottom && yBottom >=m_scrollTop && yBottom <= m_scrollBottom)
			{
				icon.setVisibility(GONE);
				view.start();
				flag = true;
			}
		}
		return flag;
	}

	@Override
	public void onBack()
	{
		if(mCanBack){
			m_site.onBack(getContext());
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		if(m_anim1 != null)
		{
			m_anim1.pause();
		}
		if(m_anim2 != null)
		{
			m_anim2.pause();
		}
		if(m_anim3 != null)
		{
			m_anim3.pause();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(m_anim1 != null)
		{
			m_anim1.resume();
		}
		if(m_anim2 != null)
		{
			m_anim2.resume();
		}
		if(m_anim3 != null)
		{
			m_anim3.resume();
		}
	}

	@Override
	public void onClose()
	{
		m_scroll.setBackgroundDrawable(null);
		if(m_anim1 != null)
		{
			m_anim1.setVisibility(GONE);
			m_anim1.stopPlayback();
			m_anim1 = null;
		}
		if(m_anim2 != null)
		{
			m_anim2.setVisibility(GONE);
			m_anim2.stopPlayback();
			m_anim2 = null;
		}
		if(m_anim3 != null)
		{
			m_anim3.setVisibility(GONE);
			m_anim3.stopPlayback();
			m_anim3 = null;
		}
		this.removeAllViews();
		super.onClose();
		MyBeautyStat.onClickByRes(R.string.文字动画1_收起文字动画1);
	}
}
