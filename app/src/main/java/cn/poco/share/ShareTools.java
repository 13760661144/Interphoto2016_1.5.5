package cn.poco.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.sina.weibo.sdk.constant.WBConstants;
import com.taotie.cn.circlesdk.CircleApi;
import com.taotie.cn.circlesdk.CircleSDK;
import com.taotie.cn.circlesdk.ICIRCLEAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import java.io.File;

import BaseDataType.CircleMultiInfo;
import BaseDataType.ImageObject;
import BaseDataType.TextObject;
import BaseDataType.VideoObject;
import cn.poco.blogcore.FacebookBlog;
import cn.poco.blogcore.InstagramBlog;
import cn.poco.blogcore.QzoneBlog2;
import cn.poco.blogcore.SinaBlog;
import cn.poco.blogcore.Tools;
import cn.poco.blogcore.TwitterBlog;
import cn.poco.blogcore.WeiXinBlog;
import cn.poco.blogcore.WeiboInfo;
import cn.poco.framework.FileCacheMgr;
import cn.poco.interphoto2.R;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.tianutils.MakeBmp;
import cn.poco.utils.Utils;

public class ShareTools
{
	public static final int SUCCESS = 0;
	public static final int CANCEL = 1;
	public static final int FAIL = 2;

	private Context mContext;

	private SinaBlog mSina;
	private QzoneBlog2 mQzone;
	private WeiXinBlog mWeiXin;
	private FacebookBlog mFacebook;
	private TwitterBlog mTwitter;
	private InstagramBlog mInstagram;
	private ICIRCLEAPI mCircleApi;

	private static final int WX_THUMB_SIZE = 150;						//微信限制缩略图最大边长

	public static final String CIRCLE_PACKAGE_NAME = "com.taotie.circle";

	public interface SendCompletedListener
	{
		public void result(int result);
	}

	public ShareTools(Context context)
	{
		mContext = context;
		SharePage.initBlogConfig();
	}

	/**
	 * 绑定新浪
	 * @param listener 监听器
	 */
	public void bindSina(final SharePage.BindCompleteListener listener)
	{
		if(mSina == null) mSina = new SinaBlog(mContext);

		mSina.bindSinaWithSSO(new SinaBlog.BindSinaCallback()
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
			}

			@Override
			public void fail()
			{
				switch(mSina.LAST_ERROR)
				{
					case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
						AlertDialog dlg = new AlertDialog.Builder(mContext).create();
						dlg.setTitle(mContext.getResources().getString(R.string.tip));
						dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
						dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
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
	 * @param listener 监听器
	 */
	public void bindQzone(boolean show_dialog, final SharePage.BindCompleteListener listener)
	{
		if(mQzone == null) mQzone = new QzoneBlog2(mContext);
		mQzone.showDialog = show_dialog;
		mQzone.bindQzoneWithSDK(new QzoneBlog2.BindQzoneCallback()
		{
			@Override
			public void success(String accessToken, String expiresIn, String openId, String nickName)
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
						dlg.setTitle(mContext.getResources().getString(R.string.tip));
						dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
						dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
						dlg.show();
						break;

					default:
						Toast.makeText(mContext, mContext.getResources().getString(R.string.boundQQZoneFailed), Toast.LENGTH_LONG).show();
						break;
				}
				if(listener != null) listener.fail();
			}
		});
	}

	/**
	 * 发送图片到新浪微博
	 * @param pic 图片路径
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendToSina(Object pic, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, SinaBlog.SINA_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mSina == null) mSina = new SinaBlog(mContext);
		mSina.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken());
//		String send_path = SharePage.makeCachePic(mContext, path);
		mSina.setSendSinaResponse(new SinaBlog.SendSinaResponse()
		{
			@Override
			public void response(boolean send_success, int response_code)
			{
				if(send_success)
				{
					switch(response_code)
					{
						case WBConstants.ErrorCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case WBConstants.ErrorCode.ERR_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case WBConstants.ErrorCode.ERR_FAIL:
						case SinaBlog.NO_RESPONSE:
							if(listener != null) listener.result(FAIL);
							break;
					}
				}
				else
				{
					if(listener != null) listener.result(FAIL);
				}
			}
		});

		Intent intent = new Intent(mContext, SinaRequestActivity.class);
		intent.putExtra("type", SinaBlog.SEND_TYPE_PIC);
		intent.putExtra("pic", path);
		((Activity) mContext).startActivityForResult(intent, SinaBlog.SINA_REQUEST_CODE);
	}

	/**
	 * 发送图文到新浪微博
	 * @param pic 图片
	 * @param content 文字内容
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendToSina(Object pic, String content, final SendCompletedListener listener)
	{
		if(pic == null) return;

		if(!Tools.checkApkExist(mContext, SinaBlog.SINA_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mSina == null) mSina = new SinaBlog(mContext);
		mSina.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken());
//		String send_path = SharePage.makeCachePic(mContext, path);
		mSina.setSendSinaResponse(new SinaBlog.SendSinaResponse()
		{
			@Override
			public void response(boolean send_success, int response_code)
			{
				if(send_success)
				{
					switch(response_code)
					{
						case WBConstants.ErrorCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case WBConstants.ErrorCode.ERR_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case WBConstants.ErrorCode.ERR_FAIL:
						case SinaBlog.NO_RESPONSE:
							if(listener != null) listener.result(FAIL);
							break;
					}
				}
				else
				{
					if(listener != null) listener.result(FAIL);
				}
			}
		});

		Intent intent = new Intent(mContext, SinaRequestActivity.class);
		intent.putExtra("type", SinaBlog.SEND_TYPE_TEXT_AND_PIC);
		intent.putExtra("pic", path);
		intent.putExtra("content", content);
		((Activity) mContext).startActivityForResult(intent, SinaBlog.SINA_REQUEST_CODE);
	}

	public void sendVideoToSina(String content, String videoPath, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, SinaBlog.SINA_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		if(mSina == null) mSina = new SinaBlog(mContext);
		mSina.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken());
		mSina.setSendSinaResponse(new SinaBlog.SendSinaResponse()
		{
			@Override
			public void response(boolean send_success, int response_code)
			{
				if(send_success)
				{
					switch(response_code)
					{
						case WBConstants.ErrorCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case WBConstants.ErrorCode.ERR_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case WBConstants.ErrorCode.ERR_FAIL:
						case SinaBlog.NO_RESPONSE:
							if(listener != null) listener.result(FAIL);
							break;
					}
				}
				else
				{
					if(listener != null) listener.result(FAIL);
				}
			}
		});

		Intent intent = new Intent(mContext, SinaRequestActivity.class);
		intent.putExtra("video", videoPath);
		intent.putExtra("content", content);
		((Activity) mContext).startActivityForResult(intent, SinaBlog.SINA_REQUEST_CODE);
	}

	/**
	 * 发送文字到新浪微博
	 * @param content 文本内容
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendTextToSina(String content, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, SinaBlog.SINA_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installSinaWeiboTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		if(mSina == null) mSina = new SinaBlog(mContext);
		mSina.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetSinaAccessToken());
		mSina.setSendSinaResponse(new SinaBlog.SendSinaResponse()
		{
			@Override
			public void response(boolean send_success, int response_code)
			{
				if(send_success)
				{
					switch(response_code)
					{
						case WBConstants.ErrorCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case WBConstants.ErrorCode.ERR_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case WBConstants.ErrorCode.ERR_FAIL:
						case SinaBlog.NO_RESPONSE:
							if(listener != null) listener.result(FAIL);
							break;
					}
				}
				else
				{
					if(listener != null) listener.result(FAIL);
				}
			}
		});

		Intent intent = new Intent(mContext, SinaRequestActivity.class);
		intent.putExtra("type", SinaBlog.SEND_TYPE_TEXT);
		intent.putExtra("content", content);
		((Activity) mContext).startActivityForResult(intent, SinaBlog.SINA_REQUEST_CODE);
	}

	/**
	 * 发送图片到QQ
	 * @param pic 图片路径
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendToQQ(Object pic, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, QzoneBlog2.QQ_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mQzone == null) mQzone = new QzoneBlog2(mContext);
		mQzone.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken());
		mQzone.setOpenId(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneOpenid());
		mQzone.setSendQQorQzoneCompletelistener(new QzoneBlog2.SendQQorQzoneCompletelistener()
		{
			public void sendComplete(int result)
			{
				switch(result)
				{
					case QzoneBlog2.SEND_SUCCESS:
						if(listener != null) listener.result(SUCCESS);
						break;

					case QzoneBlog2.SEND_CANCEL:
						if(listener != null) listener.result(CANCEL);
						break;

					case QzoneBlog2.SEND_FAIL:
						if(listener != null) listener.result(FAIL);
						break;
				}
			}
		});
		mQzone.sendToQQ(path);
	}

	/**
	 * 发送url到QQ
	 * @param title 标题
	 * @param content 文字内容
	 * @param pic 图片路径
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendUrlToQQ(String title, String content, Object pic, String url, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, QzoneBlog2.QQ_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mQzone == null) mQzone = new QzoneBlog2(mContext);
		mQzone.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken());
		mQzone.setOpenId(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneOpenid());
		mQzone.setSendQQorQzoneCompletelistener(new QzoneBlog2.SendQQorQzoneCompletelistener()
		{
			public void sendComplete(int result)
			{
				switch(result)
				{
					case QzoneBlog2.SEND_SUCCESS:
						if(listener != null) listener.result(SUCCESS);
						break;

					case QzoneBlog2.SEND_CANCEL:
						if(listener != null) listener.result(CANCEL);
						break;

					case QzoneBlog2.SEND_FAIL:
						if(listener != null) listener.result(FAIL);
						break;
				}
			}
		});
		mQzone.sendUrlToQQ(path, title, content, url);
	}

	/**
	 * 发送图片到QQ空间
	 * @param pic 图片
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendToQzone(Object pic, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, QzoneBlog2.QQ_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mQzone == null) mQzone = new QzoneBlog2(mContext);
		mQzone.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken());
		mQzone.setOpenId(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneOpenid());
//		String send_path = SharePage.makeCachePic(mContext, path);
		mQzone.setSendQQorQzoneCompletelistener(new QzoneBlog2.SendQQorQzoneCompletelistener()
		{
			public void sendComplete(int result)
			{
				switch(result)
				{
					case QzoneBlog2.SEND_SUCCESS:
						if(listener != null) listener.result(SUCCESS);
						break;

					case QzoneBlog2.SEND_CANCEL:
						if(listener != null) listener.result(CANCEL);
						break;

					case QzoneBlog2.SEND_FAIL:
						if(listener != null) listener.result(FAIL);
						break;
				}
			}
		});
		mQzone.sendToPublicQzone(1, path);
	}

	/**
	 * 发送url到QQ空间
	 * @param content 文字内容
	 * @param pic 图片路径，用作缩略图
	 * @param title 标题
	 * @param tagerUrl 发送的url
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendUrlToQzone(String content, Object pic, String title, String tagerUrl, final SendCompletedListener listener)
	{
		if(!Tools.checkApkExist(mContext, QzoneBlog2.QQ_PACKAGE_NAME))
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.installQQTips));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}

		String path = transformToPath(pic);
		if(path == null) return;

		if(mQzone == null) mQzone = new QzoneBlog2(mContext);
		mQzone.SetAccessToken(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneAccessToken());
		mQzone.setOpenId(SettingInfoMgr.GetSettingInfo(mContext).GetQzoneOpenid());
		mQzone.setSendQQorQzoneCompletelistener(new QzoneBlog2.SendQQorQzoneCompletelistener()
		{
			public void sendComplete(int result)
			{
				switch(result)
				{
					case QzoneBlog2.SEND_SUCCESS:
						if(listener != null) listener.result(SUCCESS);
						break;

					case QzoneBlog2.SEND_CANCEL:
						if(listener != null) listener.result(CANCEL);
						break;

					case QzoneBlog2.SEND_FAIL:
						if(listener != null) listener.result(FAIL);
						break;
				}
			}
		});
		mQzone.sendToQzone2(content, path, title, tagerUrl);
	}

	/**
	 * 发送图片到微信
	 * @param pic 图片
	 * @param WXSceneSession true为发送到微信好友，false为发送到微信朋友圈
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendToWeiXin(Object pic, boolean WXSceneSession, final SendCompletedListener listener)
	{
		String path = transformToPath(pic);
		if(path == null) return;

		if(mWeiXin == null) mWeiXin = new WeiXinBlog(mContext);
		Bitmap thumb = MakeBmp.CreateBitmap(Utils.DecodeImage(mContext, path, 0, -1, WX_THUMB_SIZE, WX_THUMB_SIZE), WX_THUMB_SIZE, WX_THUMB_SIZE, -1, 0, Bitmap.Config.ARGB_8888);
		if(mWeiXin.sendToWeiXin(path, thumb, WXSceneSession))
		{
			SendWXAPI.WXCallListener wxlistener = new SendWXAPI.WXCallListener()
			{
				@Override
				public void onCallFinish(int result)
				{
					switch(result)
					{
						case BaseResp.ErrCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case BaseResp.ErrCode.ERR_USER_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case BaseResp.ErrCode.ERR_AUTH_DENIED:
							if(listener != null) listener.result(FAIL);
							break;
					}
					SendWXAPI.removeListener(this);
				}
			};
			SendWXAPI.addListener(wxlistener);
		}
		else
		{
			WeiXinBlog.showErrorMessageToast(mContext, mWeiXin.LAST_ERROR, WXSceneSession);
		}
	}

	/**
	 *
	 * @param file_path		文件路径
	 * @param title	标题
	 * @param thumb	缩略图
	 * @param listener	发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendFileToWeiXin(String file_path, String title, Bitmap thumb, final SendCompletedListener listener)
	{
		if(mWeiXin == null) mWeiXin = new WeiXinBlog(mContext);
		thumb = MakeBmp.CreateBitmap(thumb, WX_THUMB_SIZE, WX_THUMB_SIZE, -1, 0, Bitmap.Config.ARGB_8888);
		if(mWeiXin.sendFileToWeiXin(file_path, title, thumb))
		{
			SendWXAPI.WXCallListener wxlistener = new SendWXAPI.WXCallListener()
			{
				@Override
				public void onCallFinish(int result)
				{
					switch(result)
					{
						case BaseResp.ErrCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case BaseResp.ErrCode.ERR_USER_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case BaseResp.ErrCode.ERR_AUTH_DENIED:
							if(listener != null) listener.result(FAIL);
							break;
					}
					SendWXAPI.removeListener(this);
				}
			};
			SendWXAPI.addListener(wxlistener);
		}
		else
		{
			WeiXinBlog.showErrorMessageToast(mContext, mWeiXin.LAST_ERROR, true);
		}
	}

	/**
	 * 发送url到微信
	 * @param pic 用于制作缩略图
	 * @param url 发送的url链接
	 * @param title 标题
	 * @param content 文字内容
	 * @param WXSceneSession true为发送到微信好友，false为发送到微信朋友圈
	 * @param listener 发送结果监听器,分别为ShareTools.SUCCESS,ShareTools.CANCEL,ShareTools.FAIL三种结果
	 */
	public void sendUrlToWeiXin(Object pic, String url, String title, String content, boolean WXSceneSession, final SendCompletedListener listener)
	{
		Bitmap bmp = transformToBitmap(pic);
		if(bmp == null || bmp.isRecycled()) return;

		if(mWeiXin == null) mWeiXin = new WeiXinBlog(mContext);
		Bitmap thumb = MakeBmp.CreateBitmap(bmp, WX_THUMB_SIZE, WX_THUMB_SIZE, -1, 0, Bitmap.Config.ARGB_8888);
		if(mWeiXin.sendUrlToWeiXin(url, title, content, thumb, WXSceneSession))
		{
			SendWXAPI.WXCallListener wxlistener = new SendWXAPI.WXCallListener()
			{
				@Override
				public void onCallFinish(int result)
				{
					switch(result)
					{
						case BaseResp.ErrCode.ERR_OK:
							if(listener != null) listener.result(SUCCESS);
							break;

						case BaseResp.ErrCode.ERR_USER_CANCEL:
							if(listener != null) listener.result(CANCEL);
							break;

						case BaseResp.ErrCode.ERR_AUTH_DENIED:
							if(listener != null) listener.result(FAIL);
							break;
					}
					SendWXAPI.removeListener(this);
				}
			};
			SendWXAPI.addListener(wxlistener);
		}
		else
		{
			WeiXinBlog.showErrorMessageToast(mContext, mWeiXin.LAST_ERROR, WXSceneSession);
		}
	}

	/**
	 * 发送图片到Facebook
	 * @param pic 图片路径(图片不能大于12mb)
	 * @param callback 发送回调结果,FacebookBlog.RESULT_FAIL为发送失败，error_info为错误信息，FacebookBlog.RESULT_SUCCESS为发送成功或者取消发送,由于两者返回数据一样无法判别
	 */
	public void sendToFacebook(Object pic, FacebookBlog.FaceBookSendCompleteCallback callback)
	{
		if(pic == null)
		{
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(mContext.getResources().getString(R.string.mustSelectOnepicture));
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
			return;
		}
		if(mFacebook == null) mFacebook = new FacebookBlog(mContext);
		Bitmap bmp = null;
		if(pic instanceof Bitmap) bmp = (Bitmap)pic;
		else bmp = Utils.DecodeImage(mContext, pic, 0, -1, -1, -1);
		if(!mFacebook.sendPhotoToFacebookBySDK(bmp, callback))
		{
			String message = null;
			switch(mFacebook.LAST_ERROR)
			{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					message = mContext.getResources().getString(R.string.installFacebookTips);
					break;

				case WeiboInfo.BLOG_INFO_IMAGE_IS_NULL:
					message = mContext.getResources().getString(R.string.mustSelectOnepicture);
					break;

				default:
					message = mContext.getResources().getString(R.string.facebook_client_start_fail);
					break;
			}
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(message);
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
		}
	}

	/**
	 * 发送url到Facebook
	 * @param title 标题,不能为null
	 * @param content 文字内容，为null时会被Facebook自动填充不相关内容
	 * @param url 跳转链接，不能为null
	 * @param callback 发送回调结果,FacebookBlog.RESULT_FAIL为发送失败，error_info为错误信息，FacebookBlog.RESULT_SUCCESS为发送成功或者取消发送,由于两者返回数据一样无法判别
	 */
	public void sendUrlToFacebook(String title, String content, String url, FacebookBlog.FaceBookSendCompleteCallback callback)
	{
		if(mFacebook == null) mFacebook = new FacebookBlog(mContext);
		if(!mFacebook.sendUrlToFacebookBySDK(title, content, url, callback))
		{
			String message = null;
			switch(mFacebook.LAST_ERROR)
			{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					message = mContext.getResources().getString(R.string.installFacebookTips);
					break;

				case WeiboInfo.BLOG_INFO_CONTEXT_IS_NULL:
					message = mContext.getResources().getString(R.string.contentNotbeempty);
					break;

				default:
					message = mContext.getResources().getString(R.string.facebook_client_start_fail);
					break;
			}
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(message);
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
		}
	}

	/**
	 * 发送图片和文字到Twitter，两者至少有一种
	 * @param pic 图片
	 * @param content 文字内容
	 */
	public void sendToTwitter(Object pic, String content)
	{
		String path = transformToPath(pic);
		if(path == null) return;

		if(mTwitter == null) mTwitter = new TwitterBlog(mContext);
		if(!mTwitter.sendToTwitter(path, content))
		{
			String message = null;
			switch(mTwitter.LAST_ERROR)
			{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					message = mContext.getResources().getString(R.string.installTwitterTips);
					break;

				case WeiboInfo.BLOG_INFO_CONTEXT_IS_NULL:
					message = mContext.getResources().getString(R.string.contentNotbeempty);
					break;
			}
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(message);
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
		}
	}

	/**
	 * 发送图片到Instagram
	 * @param pic 图片
	 */
	public void sendToInstagram(Object pic)
	{
		String path = transformToPath(pic);
		if(path == null) return;

		if(mInstagram == null) mInstagram = new InstagramBlog(mContext);
		if(!mInstagram.sendToInstagram(path))
		{
			String message = null;
			switch(mInstagram.LAST_ERROR)
			{
				case WeiboInfo.BLOG_INFO_CLIENT_NO_INSTALL:
					message = mContext.getResources().getString(R.string.installInstagramTips);
					break;

				case WeiboInfo.BLOG_INFO_IMAGE_IS_NULL:
					message = mContext.getResources().getString(R.string.mustSelectOnepicture);
					break;
			}
			AlertDialog dlg = new AlertDialog.Builder(mContext).create();
			dlg.setTitle(mContext.getResources().getString(R.string.tip));
			dlg.setMessage(message);
			dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
			dlg.show();
		}
	}

	private String transformToPath(Object pic)
	{
		if(pic == null) return null;
		String path = null;
		if(pic instanceof String) path = (String)pic;
		else if(pic instanceof Bitmap) path = Utils.SaveImg(mContext, (Bitmap)pic, FileCacheMgr.GetLinePath(), 100, false);
		else path = Utils.SaveImg(mContext, Utils.DecodeImage(mContext, pic, 0, -1, -1, -1), FileCacheMgr.GetLinePath(), 100, false);
		return path;
	}

	private Bitmap transformToBitmap(Object pic)
	{
		if(pic == null) return null;
		Bitmap bmp = null;
		if(pic instanceof Bitmap) bmp = (Bitmap)pic;
		else bmp = Utils.DecodeImage(mContext, pic, 0, -1, -1, -1);
		return bmp;
	}

	//解析一下本地链接是否合法
	private Uri decodeImagePath(String imagePath)
	{
		Uri uri = null;
		if(imagePath.startsWith("file:///storage"))
		{
			uri = Uri.parse(imagePath);
		}
		if(imagePath.startsWith("/storage"))
		{
			imagePath = "file://" + imagePath;
			uri = Uri.parse(imagePath);
		}
		return uri;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(mSina != null) mSina.onActivityResult(requestCode, resultCode, data, 10086);
		if(mQzone != null) mQzone.onActivityResult(requestCode, resultCode, data);
		if(mFacebook != null) mFacebook.onActivityResult(requestCode, resultCode, data, 10086);
	}

	/**
	 * 发送图片和文字到Circle，两者至少包含其一
	 * @param pic 图片路径
	 * @param isImg true 分享图片 false 分享视频
	 * @param content 文字内容
	 * @param listener 发送监听器，code=0时发送成功，code=1时取消发送，code=10001时没有安装客户端，code=10000时跳转到客户端成功，其他值时发送失败
	 */
	public void sendToCircle(String pic, String content, boolean isImg, final SendCompletedListener listener)
	{
		if(pic == null || pic.length() <= 0 || !new File(pic).exists())
		{
			Toast.makeText(mContext, mContext.getResources().getString(R.string.share_error_image_is_null), Toast.LENGTH_LONG).show();
			return;
		}
		if(mCircleApi == null) mCircleApi = CircleSDK.createApi(mContext, 4);
		CircleMultiInfo multiInfo = new CircleMultiInfo();
		if(content != null)
		{
			TextObject textObject = new TextObject();
			textObject.text = content;
			multiInfo.add(textObject);
		}

		if(isImg)
		{
			ImageObject imageObject = new ImageObject();
			imageObject.imgUri = decodeImagePath(pic);
			multiInfo.add(imageObject);
		}
		else
		{
			VideoObject videoObject = new VideoObject();
			videoObject.videoUri = decodeImagePath(pic);
			multiInfo.add(videoObject);
		}

		CircleApi.setOnCallBackListener(new CircleApi.OnCallBackListener()
		{
			@Override
			public void OnMessage(int code , String msg)
			{
				if(code == 10001)
				{
					String message = mContext.getResources().getString(R.string.installCircleTips);
					AlertDialog dlg = new AlertDialog.Builder(mContext).create();
					dlg.setTitle(mContext.getResources().getString(R.string.tip));
					dlg.setMessage(message);
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
				}
				else if(code == 10007)
				{
					String message = mContext.getResources().getString(R.string.circle_unsupport_video);
					AlertDialog dlg = new AlertDialog.Builder(mContext).create();
					dlg.setTitle(mContext.getResources().getString(R.string.tip));
					dlg.setMessage(message);
					dlg.setButton(AlertDialog.BUTTON_POSITIVE, mContext.getResources().getString(R.string.ok), (DialogInterface.OnClickListener)null);
					dlg.show();
				}
				else
				{
					if(listener != null) listener.result(code);
				}
			}
		});
		mCircleApi.attachInfo(multiInfo);
		mCircleApi.share();
	}

	public void openCircleWithIntent() {
		PackageManager packageManager = mContext.getPackageManager();
		Intent intent = new Intent();
		try
		{
			intent = packageManager.getLaunchIntentForPackage(CIRCLE_PACKAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mContext.startActivity(intent);
	}

	public void openWeiXinWithIntent()
	{
		PackageManager packageManager = mContext.getPackageManager();
		Intent intent = new Intent();
		try
		{
			intent = packageManager.getLaunchIntentForPackage(WeiXinBlog.WX_PACKAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mContext.startActivity(intent);
	}

	public void openTwitterWithIntent()
	{
		PackageManager packageManager = mContext.getPackageManager();
		Intent intent = new Intent();
		try
		{
			intent = packageManager.getLaunchIntentForPackage(TwitterBlog.TWITTER_PACKAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mContext.startActivity(intent);
	}

	public void openFacebookWithIntent()
	{
		PackageManager packageManager = mContext.getPackageManager();
		Intent intent = new Intent();
		try
		{
			intent = packageManager.getLaunchIntentForPackage(FacebookBlog.FACEBOOK_PACKAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mContext.startActivity(intent);
	}

	public void openInstagramWithIntent()
	{
		PackageManager packageManager = mContext.getPackageManager();
		Intent intent = new Intent();
		try
		{
			intent = packageManager.getLaunchIntentForPackage(InstagramBlog.INSTAGRAM_PACKAGE_NAME);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		mContext.startActivity(intent);
	}

	public static void ToastSuccess(Context context)
	{
		Toast.makeText(context, context.getResources().getString(R.string.share_success), Toast.LENGTH_SHORT).show();
	}
}
