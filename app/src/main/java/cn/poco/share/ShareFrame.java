package cn.poco.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.poco.framework.FileCacheMgr;
import cn.poco.image.filter;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.setting.SettingPage;
import cn.poco.share.SharePage.BindCompleteListener;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.NetState;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

/**
 * ShareFrame 分享主界面布局和按钮事件
 * 
 * @author pocouser
 * 
 */
public class ShareFrame extends FrameLayout
{
	private SharePage mParent; 		//父类实例，用于调用父类的方法
	private String mSavedPicPath; 	//保存图片路径
	private Context mContext;
	

	//**************UI布局***************
//	private ImageView mHomeBtn; 	//返回主页
//	private ImageView mBackBtn; 	//返回美化

//	private ImageView mImageHolder; //缩略图
	
	private ImageView mIconSina; 	//Sina微博
	private ImageView mIconQzone; 	//QQ空间
	private ImageView mIconWeiXin; 	//微信
	private ImageView mIconWXFriends; //微信好友圈

	public Bitmap mThumb; 		//预览用缩略图
	public Bitmap mBackground;	//背景磨砂玻璃图	
	
	public ShareFrame(Context context, SharePage parent)
	{
		super(context);
		mParent = parent;
		initialize(context);
	}
	
	/*protected void initialize(final Context context)
	{
		mContext = context;

		this.setBackgroundColor(0xff84f1f3);

		
		LinearLayout.LayoutParams ll;
		FrameLayout.LayoutParams fl;

		int ScreenW = ShareData.m_screenWidth;
		int ScreenH = ShareData.m_screenHeight;
		if(ScreenW <= 0) ScreenW = 1;
		if(ScreenH <= 0) ScreenH = 1;

		LinearLayout mainFrame = new LinearLayout(context);
		mainFrame.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.addView(mainFrame, fl);
		{
			FrameLayout top_bar = new FrameLayout(context);
			top_bar.setBackgroundColor(0x4c000000);
			ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(100));
			ll.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			mainFrame.addView(top_bar, ll);
			{	
				mHomeBtn = new ImageView(context);
				mHomeBtn.setScaleType(ScaleType.CENTER_INSIDE);
//				mHomeBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_home_btn2_out, R.drawable.framework_home_btn2_over));
				mHomeBtn.setImageResource(R.drawable.framework_home_btn);
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
				fl.rightMargin = ShareData.PxToDpi_xhdpi(18);
				top_bar.addView(mHomeBtn, fl);
				mHomeBtn.setOnClickListener(mClickListener);
				
				mBackBtn = new ImageView(context);
				mBackBtn.setScaleType(ScaleType.CENTER_INSIDE);
//				mBackBtn.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.framework_back_btn_out, R.drawable.framework_back_btn_over));
				mBackBtn.setImageResource(R.drawable.framework_back_btn);
				fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
				fl.leftMargin = ShareData.PxToDpi_xhdpi(10);
				top_bar.addView(mBackBtn, fl);
				mBackBtn.setOnClickListener(mClickListener);
			}		
				
			FrameLayout image_frame = new FrameLayout(context);
			ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(470), ShareData.PxToDpi_xhdpi(760));
			ll.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			ll.topMargin = ShareData.PxToDpi_xhdpi(40);
			mainFrame.addView(image_frame, ll);
			{				
				mImageHolder = new ImageView(context);
//				mImageHolder.setBackgroundResource(R.drawable.share_thumb_shadow);
				fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				fl.gravity = Gravity.CENTER;
				image_frame.addView(mImageHolder, fl);
				mImageHolder.setOnClickListener(mClickListener);
			}			
		}
			
		FrameLayout bottom_frame = new FrameLayout(context);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, ShareData.PxToDpi_xhdpi(188));
		fl.gravity = Gravity.BOTTOM | Gravity.LEFT;
		addView(bottom_frame, fl);
		{
			LinearLayout share_text = new LinearLayout(context);
			share_text.setOrientation(LinearLayout.HORIZONTAL);
			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			bottom_frame.addView(share_text, fl);
			{
				TextView text = new TextView(context);
				text.setText("从这里分享,画质更好哦");
				text.setTextColor(0xff999999);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				share_text.addView(text, ll);
				
				ImageView arrow = new ImageView(context);
				arrow.setImageResource(R.drawable.sharepage_prompt_arrow);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(15);
				share_text.addView(arrow, ll);
				
				ImageView line = new ImageView(context);
				line.setBackgroundColor(0xff999999);
				ll = new LinearLayout.LayoutParams(ShareData.PxToDpi_xhdpi(372), 1);
				ll.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(12);
				share_text.addView(line, ll);
			}
		
			HorizontalScrollView h_scroll = new HorizontalScrollView(context);
			h_scroll.setHorizontalScrollBarEnabled(false);
			fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			fl.topMargin = ShareData.PxToDpi_xhdpi(72);
			bottom_frame.addView(h_scroll, fl);
			
			LinearLayout share_button = new LinearLayout(context);
			share_button.setOrientation(LinearLayout.HORIZONTAL);
			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.LEFT;
			h_scroll.addView(share_button, fl);
			{
				int icons = 4;
				int margin = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(92) * icons) / (icons + 1);
				if(margin < ShareData.PxToDpi_xhdpi(20)) margin = ShareData.PxToDpi_xhdpi(20);
				
				//绑定微信朋友圈
				mIconWXFriends = new ImageView(context);
//				mIconWXFriends.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_wechat_friend_normal, R.drawable.share_weibo_wechat_friend_press));
				mIconWXFriends.setImageResource(R.drawable.sharepage_wxfriends);
				mIconWXFriends.setScaleType(ScaleType.CENTER_INSIDE);
				mIconWXFriends.setOnClickListener(mClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconWXFriends, ll);
				
				//绑定微信
				mIconWeiXin = new ImageView(context);
//				mIconWeiXin.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_wechat_normal, R.drawable.share_weibo_wechat_press));
				mIconWeiXin.setImageResource(R.drawable.sharepage_weixin);
				mIconWeiXin.setScaleType(ScaleType.CENTER_INSIDE);
				mIconWeiXin.setOnClickListener(mClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconWeiXin, ll);
				
				//绑定Sina
				mIconSina=new ImageView(context);
//				mIconSina.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_sina_normal, R.drawable.share_weibo_sina_press));
				mIconSina.setImageResource(R.drawable.sharepage_sina);
				mIconSina.setScaleType(ScaleType.CENTER_INSIDE);
				mIconSina.setOnClickListener(mClickListener);
				mIconSina.setOnLongClickListener(mLongClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconSina, ll);
				
				//绑定QQ空间
				mIconQzone = new ImageView(context);
//				mIconQzone.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_qzone_normal, R.drawable.share_weibo_qzone_press));
				mIconQzone.setImageResource(R.drawable.sharepage_qzone);
				mIconQzone.setScaleType(ScaleType.CENTER_INSIDE);
				mIconQzone.setOnClickListener(mClickListener);
				mIconQzone.setOnLongClickListener(mLongClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconQzone, ll);	
			}
		}
	}*/

	protected void initialize(final Context context)
	{
		mContext = context;

		this.setBackgroundColor(0xff84f1f3);
		this.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mParent != null) mParent.onBackBtn();
			}
		});

		LinearLayout.LayoutParams ll;
		FrameLayout.LayoutParams fl;

		int ScreenW = ShareData.m_screenWidth;
		int ScreenH = ShareData.m_screenHeight;
		if(ScreenW <= 0) ScreenW = 1;
		if(ScreenH <= 0) ScreenH = 1;

		LinearLayout mainFrame = new LinearLayout(context);
		mainFrame.setOrientation(LinearLayout.VERTICAL);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		this.addView(mainFrame, fl);
		{
			TextView text = new TextView(context);
			text.setText("分享至");
			text.setTextColor(0xff999999);
			text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			mainFrame.addView(text, ll);

			HorizontalScrollView h_scroll = new HorizontalScrollView(context);
			h_scroll.setHorizontalScrollBarEnabled(false);
			ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			ll.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			ll.topMargin = ShareData.PxToDpi_xhdpi(72);
			mainFrame.addView(h_scroll, ll);

			LinearLayout share_button = new LinearLayout(context);
			share_button.setOrientation(LinearLayout.HORIZONTAL);
			fl = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.TOP | Gravity.LEFT;
			h_scroll.addView(share_button, fl);
			{
				int icons = 4;
				int margin = (ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(92) * icons) / (icons + 1);
				if(margin < ShareData.PxToDpi_xhdpi(20)) margin = ShareData.PxToDpi_xhdpi(20);

				//绑定微信朋友圈
				mIconWXFriends = new ImageView(context);
//				mIconWXFriends.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_wechat_friend_normal, R.drawable.share_weibo_wechat_friend_press));
				mIconWXFriends.setImageResource(R.drawable.sharepage_wxfriends);
				mIconWXFriends.setScaleType(ScaleType.CENTER_INSIDE);
				mIconWXFriends.setOnClickListener(mClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconWXFriends, ll);

				//绑定微信
				mIconWeiXin = new ImageView(context);
//				mIconWeiXin.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_wechat_normal, R.drawable.share_weibo_wechat_press));
				mIconWeiXin.setImageResource(R.drawable.sharepage_weixin);
				mIconWeiXin.setScaleType(ScaleType.CENTER_INSIDE);
				mIconWeiXin.setOnClickListener(mClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconWeiXin, ll);

				//绑定Sina
				mIconSina=new ImageView(context);
//				mIconSina.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_sina_normal, R.drawable.share_weibo_sina_press));
				mIconSina.setImageResource(R.drawable.sharepage_sina);
				mIconSina.setScaleType(ScaleType.CENTER_INSIDE);
				mIconSina.setOnClickListener(mClickListener);
				mIconSina.setOnLongClickListener(mLongClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconSina, ll);

				//绑定QQ空间
				mIconQzone = new ImageView(context);
//				mIconQzone.setImageDrawable(CommonUtils.CreateXHDpiBtnSelector((Activity)getContext(), R.drawable.share_weibo_qzone_normal, R.drawable.share_weibo_qzone_press));
				mIconQzone.setImageResource(R.drawable.sharepage_qzone);
				mIconQzone.setScaleType(ScaleType.CENTER_INSIDE);
				mIconQzone.setOnClickListener(mClickListener);
				mIconQzone.setOnLongClickListener(mLongClickListener);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.TOP | Gravity.LEFT;
				ll.leftMargin = margin;
				share_button.addView(mIconQzone, ll);
			}
		}
	}

	public void setImage(String pic, Bitmap bmp)
	{
		mSavedPicPath = pic;
		if(bmp != null && !bmp.isRecycled())
		{
			mThumb = createThumb(bmp);
			mBackground = createBackground(bmp);
			bmp.recycle();
			System.gc();
		}
		else if(pic != null && pic.length() > 0)
		{
			mThumb = createThumb(pic);
			mBackground = createBackground(pic);
		}
//		if(mThumb != null && !mThumb.isRecycled()) mImageHolder.setImageBitmap(mThumb);
		if(mBackground != null && !mBackground.isRecycled())
		{
			BitmapDrawable bd = new BitmapDrawable(getResources(), mBackground);
			this.setBackgroundDrawable(bd);
		}
	}

	public void setImage(String pic)
	{
		mSavedPicPath = pic;
	}

	public void SetBackground(Bitmap bg)
	{
		if(bg != null && !bg.isRecycled())
		{
			mBackground = bg;
			BitmapDrawable bd = new BitmapDrawable(getResources(), mBackground);
			this.setBackgroundDrawable(bd);
		}
	}

	/**
	 * 所有按钮的长按事件;
	 */
	protected OnLongClickListener mLongClickListener = new OnLongClickListener()
	{
		@Override
		public boolean onLongClick(View v)
		{
			if(v == mIconSina)
			{
				if(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken() != null)
				{
					AlertDialog alert = new AlertDialog.Builder(mContext).create();
					alert.setTitle(getResources().getString(R.string.tip));
					alert.setMessage("是否要取消绑定新浪微博吗?");
					alert.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							SettingPage.clearSinaConfigure(mContext);
						}
					});
					alert.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.no), (DialogInterface.OnClickListener)null);
					alert.show();
				}
			}
			else if(v == mIconQzone)
			{
				if(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken() != null)
				{
					AlertDialog alert = new AlertDialog.Builder(mContext).create();
					alert.setTitle(getResources().getString(R.string.tip));
					alert.setMessage(getResources().getString(R.string.confirmTounlinkQQ));
					alert.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.yes), new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							SettingPage.clearQzoneConfigure(mContext);
						}
					});
					alert.setButton(AlertDialog.BUTTON_NEGATIVE, mContext.getResources().getString(R.string.no), (DialogInterface.OnClickListener)null);
					alert.show();
				}
			}
			return false;
		}
	};

	/**
	 * 所有按钮的单击事件;
	 */
	protected OnClickListener mClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{		
			if(v == mIconSina)
			{
				if(!NetState.IsConnectNet(mContext))
				{
					Toast.makeText(mContext, getResources().getString(R.string.networkError), Toast.LENGTH_LONG).show();
					return;
				}
				if(!SettingPage.checkSinaBindingStatus(mContext))
				{
					mParent.bindSina(new BindCompleteListener()
					{		
						@Override
						public void success() 
						{
							mParent.setContentAndPic(null, mSavedPicPath);
							mParent.startSendSdkClient(SharePage.SINA);
						}
						
						@Override
						public void fail() {}
					});
				}
				else
				{
					mParent.setContentAndPic(null, mSavedPicPath);
					mParent.startSendSdkClient(SharePage.SINA);
				}
			}	
			else if(v == mIconQzone)
			{
				if(!NetState.IsConnectNet(mContext))
				{
					Toast.makeText(mContext, getResources().getString(R.string.networkError), Toast.LENGTH_LONG).show();
					return;
				}
				
				if(!SettingPage.checkQzoneBindingStatus(mContext))
				{
					mParent.bindQzone(new BindCompleteListener()
					{		
						@Override
						public void success() 
						{
							mParent.setContentAndPic(getResources().getString(R.string.FromInterphoto), mSavedPicPath);
							mParent.startSendSdkClient(SharePage.QZONE);
						}
						
						@Override
						public void fail() {}
					});
				}
				else
				{
					mParent.setContentAndPic(getResources().getString(R.string.FromInterphoto), mSavedPicPath);
					mParent.startSendSdkClient(SharePage.QZONE);
				}
			}	
			else if(v == mIconWeiXin)
			{		
				if(!NetState.IsConnectNet(mContext))
				{
					Toast.makeText(mContext, getResources().getString(R.string.networkError), Toast.LENGTH_LONG).show();
					return;
				}
				
				mParent.setContentAndPic(null, mSavedPicPath);
				mParent.startSendSdkClient(SharePage.WEIXIN);		
			}
			else if(v == mIconWXFriends)
			{
				if(!NetState.IsConnectNet(mContext))
				{
					Toast.makeText(mContext, getResources().getString(R.string.networkError), Toast.LENGTH_LONG).show();
					return;
				}
				
				mParent.setContentAndPic(null, mSavedPicPath);
				mParent.startSendSdkClient(SharePage.WXFRIENDS);		
			}
//			else if(v == mHomeBtn)
//			{
//				if(mParent != null) mParent.onHomeBtn();
//			}
//			else if(v == mBackBtn)
//			{
//				if(mParent != null) mParent.onBackBtn();
//			}
		}
	};
	
	/**
	 * 截屏(只能在Activity中调用)
	 * @param context
	 * @param screen_w 屏幕宽
	 * @param screen_h 屏幕高
	 * @return 截屏图片
	 */
	public static Bitmap screenCapture(Context context, int screen_w, int screen_h)
	{  
		if(context == null || screen_w <= 0 || screen_h <= 0) return null;
		//获取屏幕  
		View decorview = ((Activity) context).getWindow().getDecorView();
		decorview.setDrawingCacheEnabled(true);   
		Bitmap screen = decorview.getDrawingCache();
		if(screen != null && !screen.isRecycled())
		{
			return MakeBmp.CreateFixBitmap(screen, screen_w, screen_h, MakeBmp.POS_CENTER, 0, Config.ARGB_8888);
		}
		return null;
	}
	
	public static String makeGlassBackground(Context context, Bitmap bmp)
	{
		if(bmp == null || bmp.isRecycled()) return null;
		Bitmap background = filter.fakeGlass(bmp, 0xa5c3f3f4);
		String path = null;
		try 
		{			
			path = Utils.SaveImg(context, background, FileCacheMgr.GetLinePath(), 100, false);	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return path;
	}
	
	/**
	 * onDestroy时候清理;
	 */
	public void clear()
	{
//		mImageHolder.setOnClickListener(null);
		mIconSina.setOnClickListener(null);
		mIconQzone.setOnClickListener(null);
		mIconSina.setOnLongClickListener(null);
		mIconQzone.setOnLongClickListener(null);
		if(mThumb != null && !mThumb.isRecycled())
		{
			mThumb.recycle();
			mThumb = null;
		}
		removeAllViews();	
		hideKeyboard();
	}

	public void hideKeyboard()
	{
		this.post(new Runnable()
		{
			@Override
			public void run()
			{
				InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindowToken(), 0);
			}
		});
	}

	public Bitmap createBackground(String path)
	{
		Bitmap bmp = Utils.DecodeImage(getContext(), path, 0, -1, ShareData.m_screenHeight, ShareData.m_screenHeight);
		if(bmp == null || bmp.isRecycled()) return null;
		int rotate = CommonUtils.GetImgInfo(path)[0];
		Bitmap background = MakeBmp.CreateFixBitmap(bmp, ShareData.m_screenRealWidth, ShareData.m_screenHeight, MakeBmp.POS_CENTER, rotate, Config.ARGB_8888);
		bmp.recycle();
		bmp = null;
		System.gc();
		if(background != null && !background.isRecycled())
		{
			background = filter.fakeGlass(background.copy(Config.ARGB_8888, true), 0x99000000);
		}
		return background;
	}
	
	public Bitmap createBackground(Bitmap org)
	{
		Bitmap background = MakeBmp.CreateFixBitmap(org, ShareData.m_screenRealWidth, ShareData.m_screenHeight, MakeBmp.POS_CENTER, 0, Config.ARGB_8888);
		if(background != null && !background.isRecycled())
		{
			background = filter.fakeGlass(background.copy(Config.ARGB_8888, true), 0x99000000);
		}
		return background;
	}
	
	/**
	 * 制作缩略图
	 * @param path 图片地址
	 */
	public Bitmap createThumb(String path)
	{
		int limit_w = ShareData.PxToDpi_xhdpi(470);
		int limit_h = ShareData.PxToDpi_xhdpi(760);
		int thumb_w = 0;
		int thumb_h = 0;
		
		Bitmap bmp = Utils.DecodeImage(getContext(), path, 0, -1, limit_w, limit_h);
		if(bmp == null || bmp.isRecycled()) return null;
		int rotate = CommonUtils.GetImgInfo(path)[0];
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		if(rotate % 180 != 0)
		{
			int a = w;
			w = h;
			h = a;
		}
		if(w >= h)
		{
			float scale = (float)limit_w / w;
			thumb_w = limit_w;
			thumb_h = (int) (h * scale);
		}
		else
		{
			float scale = (float)limit_h / h;
			thumb_w = (int) (w * scale);
			thumb_h = limit_h;
		}
		
		Bitmap thumb = MakeBmp.CreateFixBitmap(bmp, thumb_w, thumb_h, MakeBmp.POS_CENTER, rotate, Config.ARGB_8888);
		bmp.recycle();
		bmp = null;
		return thumb;
	}

	/**
	 * 制作缩略图
	 * @param org 原始图片
	 */
	private Bitmap createThumb(Bitmap org)
	{
		if(org == null || org.isRecycled()) return null;
		
		int limit_w = ShareData.PxToDpi_xhdpi(470);
		int limit_h = ShareData.PxToDpi_xhdpi(760);
		int thumb_w = 0;
		int thumb_h = 0;
		if(org.getWidth() >= org.getHeight())
		{
			float scale = (float)limit_w / org.getWidth();
			thumb_w = limit_w;
			thumb_h = (int) (org.getHeight() * scale);
		}
		else
		{
			float scale = (float)limit_h / org.getHeight();
			thumb_w = (int) (org.getWidth() * scale);
			thumb_h = limit_h;
		}
		return MakeBmp.CreateFixBitmap(org, thumb_w, thumb_h, MakeBmp.POS_CENTER, 0, Config.ARGB_8888);
	}

}