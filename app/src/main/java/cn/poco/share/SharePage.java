package cn.poco.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.net.ConnectivityManager;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.blogcore.BlogConfig;
import cn.poco.blogcore.QzoneBlog2;
import cn.poco.blogcore.QzoneBlog2.BindQzoneCallback;
import cn.poco.blogcore.SinaBlog;
import cn.poco.blogcore.SinaBlog.BindSinaCallback;
import cn.poco.blogcore.Tools;
import cn.poco.blogcore.WeiXinBlog;
import cn.poco.blogcore.WeiboInfo;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.share.SendWXAPI.WXCallListener;
import cn.poco.share.site.SharePageSite;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.NetState;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.Utils;

public class SharePage extends IPage 
{
	protected SharePageSite m_site;
	
	private ShareFrame mShareFrame = null;

	public boolean ActivityRun = false;
	public ProgressDialog mProgressDialog;
	private String mContent;
	private String mPicPath;
	private Context mContext;
	
	private ArrayList<Integer> sdkSendList;		//SDK发送队列
	
	public static final int BLOG_NUMBER = 10;	//微博数量（有新微博添加时修改此值）
	public static final int POCO_ACT = 0;		//广告活动
	public static final int POCO = 1;
	public static final int SINA = 2;
	public static final int QQ = 3;
	public static final int RENREN = 4;
	public static final int FACEBOOK = 5;
	public static final int TWITTER = 6;
	public static final int TUMBLR = 7;
	public static final int DOUBAN = 8;
	public static final int WEIXIN = 10000;		//微信
	public static final int WXFRIENDS = 10001;	//微信好友圈
	public static final int YIXIN = 10002;		//易信
	public static final int YXFRIENDS = 10003;	//易信朋友圈
	public static final int QZONE = 10004;		//QQ空间
	
	//微信
	private static final int WX_THUMB_SIZE = 150;						//微信限制缩略图最大边长
	
	//采用分享包功能
//	private PocoBlog mPoco;
	private SinaBlog mSina;
//	private TengXunBlog mQQ;
	private QzoneBlog2 mQzone;
//	private RenrenBlog mRenRen;
//	private DouBanBlog mDouban;
//	private FacebookBlog mFacebook;
//	private TwitterBlog mTwitter;
//	private TumblrBlog mTumblr;
	private WeiXinBlog mWeiXin;
//	private YiXinBlog mYiXin;
	
	
	
	//发送微博对话框点击回调接口
	public interface DialogListener
	{
		public void onClick(int view);
	}
	
	public interface BindCompleteListener
	{
		public void success();
		
		public void fail();
	}
	
	public interface SendPocoListener
	{
		public void sendComplete(String works_id);
	}
	
	public SharePage(Context context, BaseSite site)
	{
		super(context, site);
		TongJi2.AddCountByRes(getContext(), R.integer.分享);
		m_site = (SharePageSite)site;
		InitData();
		InitUI();
	}

	/**
	 * img:RotationImg2/String</br>
	 * 当img:RotationImg2时,用户什么都没操作不需要保存图片</br>
	 * 当img:String时,路径保存的是FastBmp格式的图片,需要调用
	 * {@link Utils#MakeSavePhotoPath(Context, float)}获取保存路径,然后重新保存为JPG格式
	 */
	@Override
	public void SetData(HashMap<String, Object> params)
	{
		if(params == null || mShareFrame == null) return;
		Object img = params.get("img");
		if(img == null) return;
		if(img instanceof String)
		{
			mShareFrame.setImage((String)img);
			Bitmap bg = (Bitmap)params.get("bg");
			mShareFrame.SetBackground(bg);

//			Bitmap bmp = Utils.DecodeFile((String)img, null);
//			if(bmp != null && !bmp.isRecycled())
//			{
//				String path = Utils.SaveImg(mContext, bmp, Utils.MakeSavePhotoPath(mContext, (float)bmp.getWidth() / bmp.getHeight()), 100, true);
//				if(path == null || path.length() <= 0) return;
//				mShareFrame.setImage(path, bmp);
//				bmp.recycle();
//			}
		}
		else if(img instanceof RotationImg2)
		{
			mShareFrame.setImage((String)((RotationImg2)img).m_img);
			Bitmap bg = (Bitmap)params.get("bg");
			mShareFrame.SetBackground(bg);

//			Bitmap bmp = MakeBmpV2.CreateBitmapV2(Utils.DecodeImage(mContext, ((RotationImg2)img).m_img, ((RotationImg2)img).m_degree, -1, -1, -1), ((RotationImg2)img).m_degree, ((RotationImg2)img).m_flip, -1, -1, -1, Config.ARGB_8888);
//			if(bmp != null && !bmp.isRecycled())
//			{
//				String path = Utils.SaveImg(mContext, bmp, FileCacheMgr.GetLinePath(), 100, false);
//				if(path == null || path.length() <= 0) return;
//				mShareFrame.setImage(path, bmp);
//				bmp.recycle();
//			}
		}
	}

	public void onHomeBtn()
	{
		if(m_site != null) m_site.OnHome(getContext());
	}
	
	public void onBackBtn()
	{
		if(m_site != null) m_site.OnBack(getContext());
	}
	
	public void setContentAndPic(String content, String pic)
	{
		mContent = null;
		mContent = content;
		mPicPath = null;
		mPicPath = pic;
	}
	
	@Override
	public void onClose()
	{
		ActivityRun = false;
		if(mProgressDialog != null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		if(sdkSendList != null)
		{	
			sdkSendList.clear();
			sdkSendList = null;
		}
		if(mSina != null)
		{
			mSina.clear();
			mSina = null;
		}
		if(mQzone != null)
		{
			mQzone.clear();
			mQzone = null;
		}
		mWeiXin = null;
		if(mShareFrame != null)
		{
			mShareFrame.clear();
			mShareFrame = null;
		}
		this.clearFocus();
		System.gc();	
		super.onClose();
	}

	@Override
	public void onBack()
	{
		if(m_site != null) m_site.OnBack(getContext());
	}
	
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if(mSina != null) mSina.onActivityResult(requestCode, resultCode, data, 10086);
		if(mQzone != null) mQzone.onActivityResult(requestCode, resultCode, data);
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onResume() 
	{
		sendSdkClient();
		super.onResume();
	}

	protected void InitData()
	{
		ActivityRun = true;
		ShareData.InitData((Activity)this.getContext());
		
		sdkSendList = new ArrayList<Integer>();
		mContext = this.getContext();
		
		//传入微博工具参数
		initBlogConfig();
	}

	protected void InitUI()
	{
		FrameLayout.LayoutParams fl;

//		Bitmap bg = BitmapFactory.decodeResource(getResources(), R.drawable.framework_bg);
//		BitmapDrawable bd = new BitmapDrawable(getResources(), bg);
//		bd.setTileModeXY(TileMode.REPEAT , TileMode.REPEAT );
//		bd.setDither(true);
//		mRFlayout.setBackgroundDrawable(bd);
		
		
		mShareFrame = new ShareFrame(mContext, this);
		fl = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		fl.gravity = Gravity.LEFT | Gravity.TOP;
		addView(mShareFrame, fl);
		
		/**---------------第一次进入页面提示---------------*/
//        if(Configure.queryHelpFlag("shareframe_help"))
//        {
//        	LayoutParams ffparams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//    		m_UIHelp = new HelpPager(mContext);
//			m_UIHelp.p_hasDot = false;
//			m_UIHelp.InitData(new HelpCallback()
//			{	
//				@Override
//				public void OnHelpFinish() 
//				{
//					closeHelp();
//				}
//			});
//			
//			ImageView img = new ImageView(mContext);
//			img.setScaleType(ScaleType.FIT_XY);
//			img.setImageResource(R.drawable.share_layout_share_help);
//			m_UIHelp.AddPage(img);
//			
//			img = new ImageView(mContext);
//			img.setScaleType(ScaleType.FIT_XY);
//			img.setImageResource(R.drawable.share_layout_share_help2);
//			m_UIHelp.AddPage(img);
//			m_UIHelp.setLayoutParams(ffparams);
//			mRFlayout.addView(m_UIHelp);
//        }
	}
	
	/**
	 * 绑定新浪
	 * @param listener
	 */
	public void bindSina(final BindCompleteListener listener)
	{
		if(mSina == null)
		{
			mSina = new SinaBlog(getContext());
		}
		
		mSina.bindSinaWithSSO(new BindSinaCallback() 
		{
			@Override
			public void success(final String accessToken, String expiresIn, String uid, String userName, String nickName) 
			{
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaAccessToken(accessToken);
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaUid(uid);
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaExpiresIn(expiresIn);
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaSaveTime(String.valueOf(System.currentTimeMillis() / 1000));
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaUserName(userName);
				SettingInfoMgr.GetSettingInfo(mContext).SetSinaUserNick(nickName);
				
				if(listener != null) listener.success();
				
//				new Thread(new Runnable()
//				{	
//					@Override
//					public void run() 
//					{
//						mSina.flowerCameraSinaWeibo(Constant.sinaUserId, accessToken);
//					}
//				}).start();
			}
			
			@Override
			public void fail() 
			{	
				switch(mSina.LAST_ERROR)
				{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					AlertDialog dlg = new AlertDialog.Builder(mContext).create();
					dlg.setTitle(getResources().getString(R.string.tip));
					dlg.setMessage(getResources().getString(R.string.installSinaWeiboTips));
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
					break;
					
				default:
					Toast.makeText(mContext, mContext.getResources().getString(R.string.LinkSinaWeiboFailed), Toast.LENGTH_LONG).show();
					break;
				}
				if(listener != null) listener.fail();
			}
		});
	}
	
	/**
	 * 绑定QQ空间
	 * @param listener
	 */
	public void bindQzone(final BindCompleteListener listener)
	{
		if(mQzone == null)
		{
			mQzone = new QzoneBlog2(getContext());
		}
		
		mQzone.bindQzoneWithSDK(new BindQzoneCallback()
		{	
			@Override
			public void success(String accessToken, String expiresIn, String openId,
					String nickName)
			{
				SettingInfoMgr.GetSettingInfo(mContext).SetQzoneAccessToken(accessToken);
				SettingInfoMgr.GetSettingInfo(mContext).SetQzoneOpenid(openId);
				SettingInfoMgr.GetSettingInfo(mContext).SetQzoneExpiresIn(expiresIn);
				SettingInfoMgr.GetSettingInfo(mContext).SetQzoneSaveTime(String.valueOf(System.currentTimeMillis() / 1000));
				SettingInfoMgr.GetSettingInfo(mContext).SetQzoneUserName(nickName);
				if(listener != null) listener.success();
			}
			
			@Override
			public void fail() 
			{
				switch(mQzone.LAST_ERROR)
				{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					AlertDialog dlg = new AlertDialog.Builder(mContext).create();
					dlg.setTitle(getResources().getString(R.string.tip));
					dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
					break;
					
				default:
					Toast.makeText(mContext, getResources().getString(R.string.boundQQZoneFailed), Toast.LENGTH_LONG).show();
					break;
				}
				if(listener != null) listener.fail();
			}
		});
	}
	
	/**
	 * 开始发送SDK微博
	 */
	public void startSendSdkClient(int send_blog)
	{
		if(sdkSendList == null) return;
		sdkSendList.clear();
		sdkSendList.add(send_blog);
		sendSdkClient(); 
	}
	
	private synchronized boolean sendSdkClient()
	{
		if(sdkSendList != null && sdkSendList.size() > 0)
		{
			String send_path = null;
			Bitmap thumb = null;
			switch(sdkSendList.get(0))
			{
			case WEIXIN:
				sdkSendList.remove(0);
				TongJi2.AddCountByRes(getContext(), R.integer.分享_微信好友);
				if(mWeiXin == null) mWeiXin = new WeiXinBlog(getContext());
				thumb = MakeBmp.CreateBitmap(Utils.DecodeImage(getContext(), mPicPath, 0, -1, WX_THUMB_SIZE, WX_THUMB_SIZE), WX_THUMB_SIZE, WX_THUMB_SIZE, -1, 0, Config.ARGB_8888);	
				if(!mWeiXin.sendToWeiXin(mPicPath, thumb, true)); 
				{
					WeiXinBlog.showErrorMessageToast(getContext(), mWeiXin.LAST_ERROR, true);
					sendSdkClient();
				}
				return true;
				
			case WXFRIENDS:
				sdkSendList.remove(0);
				TongJi2.AddCountByRes(getContext(), R.integer.分享_微信朋友圈);
				if(mWeiXin == null) mWeiXin = new WeiXinBlog(getContext());
				thumb = MakeBmp.CreateBitmap(Utils.DecodeImage(getContext(), mPicPath, 0, -1, WX_THUMB_SIZE, WX_THUMB_SIZE), WX_THUMB_SIZE, WX_THUMB_SIZE, -1, 0, Config.ARGB_8888);
				if(!mWeiXin.sendToWeiXin(mPicPath, thumb, false)); 
				{
					WeiXinBlog.showErrorMessageToast(getContext(), mWeiXin.LAST_ERROR, false);
					sendSdkClient();
				}
				return true;
				
			case QZONE:
				sdkSendList.remove(0);
				if(!Tools.checkApkExist(getContext(), QzoneBlog2.QQ_PACKAGE_NAME))
				{
					AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
					dlg.setTitle(getResources().getString(R.string.tip));
					dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
					sendSdkClient();
					return true;
				}
				
				TongJi2.AddCountByRes(getContext(), R.integer.分享_QQ空间);
				if(mQzone == null) mQzone = new QzoneBlog2(getContext());
				mQzone.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken());
				mQzone.setOpenId(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneOpenid());
				send_path = makeCachePic(getContext(), mPicPath);
				mQzone.sendToPublicQzone(1, send_path);
				return true;
				
			case SINA:
				sdkSendList.remove(0);
				if(!Tools.checkApkExist(getContext(), SinaBlog.SINA_PACKAGE_NAME))
				{
					AlertDialog dlg = new AlertDialog.Builder(getContext()).create();
					dlg.setTitle(getResources().getString(R.string.tip));
					dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
					sendSdkClient();
					return true;
				}
				
				TongJi2.AddCountByRes(getContext(), R.integer.分享_新浪微博);
				if(mSina == null) mSina = new SinaBlog(getContext());
				mSina.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken());
				send_path = makeCachePic(getContext(), mPicPath);
				mSina.sendBitmapToWeibo(send_path);
//				Intent intent = new Intent(getContext(), SinaRequestActivity.class);
//				intent.putExtra("pic", send_path);
//				((Activity) getContext()).startActivityForResult(intent, SinaBlog.SINA_REQUEST_CODE);
//				mSina.setSendSinaResponse(new SendSinaResponse()
//				{	
//					@Override
//					public void response(boolean send_success, int response_code)
//					{
//						if(send_success)
//						{
//							switch(response_code)
//							{
//							case WBConstants.ErrorCode.ERR_OK:
//								Toast.makeText(mContext, "发送新浪微博完成", Toast.LENGTH_LONG).show();
//								break;
//								
//							case WBConstants.ErrorCode.ERR_CANCEL:
//								Toast.makeText(mContext, "取消发送新浪微博", Toast.LENGTH_LONG).show();
//								break;
//								
//							case WBConstants.ErrorCode.ERR_FAIL:
//							case SinaBlog.NO_RESPONSE:
//								Toast.makeText(mContext, "发送新浪微博失败", Toast.LENGTH_LONG).show();
//								break;
//							}
//						}
//						else
//						{
//							Toast.makeText(mContext, "发送新浪微博失败", Toast.LENGTH_LONG).show();
//						}
//					}
//				});
				return true;
			}
		}
		return false;
	}

	/**
	 * 微信解锁
	 * @param context
	 * @param title 分享链接标题
	 * @param description 分享链接内容，发朋友圈不用传
	 * @param url 分享链接
	 * @param thumb 缩略图，长、宽都不能超过150
	 * @param WXSceneSession true为发送好友，false为发送朋友圈
	 * @param cb 发送结果监听器，结果为BaseResp.ErrCode.ERR_OK（成功），BaseResp.ErrCode.ERR_USER_CANCEL（取消发送），BaseResp.ErrCode.ERR_AUTH_DENIED（发送失败）
	 */
	public static void unlockResourceByWeiXin(Context context, String title, String description, String url, Bitmap thumb, boolean WXSceneSession, final WXCallListener cb)
	{
		initBlogConfig();
		if(SendWXAPI.sendUrlToWeiXin(context, url, title, description, thumb, WXSceneSession))
		{
			WXCallListener listener = new WXCallListener()
			{
				@Override
				public void onCallFinish(int result)
				{
					SendWXAPI.removeListener(this);
					if(cb != null) cb.onCallFinish(result);
				}
			};
			SendWXAPI.addListener(listener);
		}
	}
	
	//制作发微博用的缓存图片
	public static String makeCachePic(Context context, String file)
	{
		if(context == null) return file;
		if(file != null && file.endsWith(".gif")) return file;
		
		int network_type = NetState.GetConnectNet(context);
		int longest = 1024;
  		int quality = 100;
  		switch(network_type)
  		{
  		case ConnectivityManager.TYPE_MOBILE:
  			longest = 640;
  			quality = 90;
  			break;
  			
  		case ConnectivityManager.TYPE_WIFI:
  			quality = 96;
  			break;
  			
  		case NetState.NET_NONE:
  			return file;
  		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, opts);
		opts.inSampleSize = 1;
		boolean bigPic = false;
		int bigest = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight; 
		if(bigest > longest)
		{			
			opts.inSampleSize = bigest / longest;
			bigPic = true;
		}

		opts.inJustDecodeBounds = false;
		opts.inPreferredConfig = Config.ARGB_8888;
		Bitmap bmp = Utils.DecodeFile(file, opts, true);
		if(bmp == null)
		{
			return null;
		}
		if(bigPic)
		{
			Matrix matrix = new Matrix();
			bigest = bmp.getWidth() > bmp.getHeight() ? bmp.getWidth() : bmp.getHeight();
			float scale = (float)longest / bigest;
			int outW = (int) (bmp.getWidth() * scale);
			int outH = (int) (bmp.getHeight() * scale);
			Bitmap outBmp = Bitmap.createBitmap(outW, outH, Config.ARGB_8888);
			Canvas canvas = new Canvas(outBmp);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
			matrix.postScale(scale, scale);
			canvas.drawBitmap(bmp, matrix, null);
			bmp.recycle();
			bmp = outBmp;
			System.gc();
		}
		String path = null;
		try 
		{			
			path = Utils.SaveImg(context, bmp, FileCacheMgr.GetLinePath(), quality, false);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return path;
	}
	

	
	//**********************传入各种微博信息**********************
	public static void initBlogConfig()
	{
//		BlogConfig.POCO_TYPE_NUMBER = Constant.POCO_CTYPE_NUMBER;
//		
//		BlogConfig.DOUBAN_CALLBACK_URL = "http://phone.poco.cn/";
//		BlogConfig.DOUBAN_CONSUMER_KEY = Constant.doubanAppKey;
//		BlogConfig.DOUBAN_CONSUMER_SECRET = Constant.doubanAppSecret;
//		
//		BlogConfig.FACEBOOK_CALLBACK_URL = "fbconnect://success";
//		BlogConfig.FACEBOOK_CONSUMER_KEY = Constant.facebookAppKey;
//		BlogConfig.FACEBOOK_CONSUMER_SECRET = Constant.facebookAppSecret;
		
		BlogConfig.QZONE_CALLBACK_URL = URLEncoder.encode("poco.cn");
		BlogConfig.QZONE_CONSUMER_KEY = Constant.qzoneAppKey;
		
//		BlogConfig.RENREN_CALLBACK_URL = "http://graph.renren.com/oauth/login_success.html";
//		BlogConfig.RENREN_CONSUMER_KEY = Constant.renrenAppKey;
//		BlogConfig.RENREN_CONSUMER_SECRET = Constant.renrenAppSecret;
		
		BlogConfig.SINA_CALLBACK_URL = "http://www.poco.cn";
		BlogConfig.SINA_CONSUMER_KEY = Constant.sinaConsumerKey;
		BlogConfig.SINA_CONSUMER_SECRET = Constant.sinaConsumerSecret;
		
//		BlogConfig.TENGXUN_CALLBACK_URL = "http://phone.poco.cn/app/camera/";
//		BlogConfig.TENGXUN_CONSUMER_KEY = Constant.qqConsumerKey;
//		BlogConfig.TENGXUN_CONSUMER_SECRET = Constant.qqConsumerSecret;
//		
//		BlogConfig.TUMBLR_CALLBACK_URL = "http://www.tumblr.com/oauth/patui//succes";
//		BlogConfig.TUMBLR_CONSUMER_KEY = Constant.tumblrAppKey;
//		BlogConfig.TUMBLR_CONSUMER_SECRET = Constant.tumblrAppSecret;
//		
//		BlogConfig.TWITTER_CALLBACK_URL = "http://poco.cn";
//		BlogConfig.TWITTER_CONSUMER_KEY = Constant.twitterAppKey;
//		BlogConfig.TWITTER_CONSUMER_SECRET = Constant.twitterAppSecret;
		
		BlogConfig.WEIXIN_CONSUMER_KEY = Constant.weixinAppId;
		BlogConfig.WEIXIN_CONSUMER_SECRET = Constant.weixinAppSecret;
		
//		BlogConfig.YIXIN_CONSUMER_KEY = Constant.yixinAppId;
	}
}
