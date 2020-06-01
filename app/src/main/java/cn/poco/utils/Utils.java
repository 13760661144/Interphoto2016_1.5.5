package cn.poco.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.beautify.EffectInfo;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.MyFramework2App;
import cn.poco.imagecore.ImageUtils;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statistics.TongJi2;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.tianutils.NetCore2;
import cn.poco.tianutils.ShareData;

public class Utils
{
	/**
	 * 用于统一控制显示时的图片尺寸,输出的图片已经旋转、翻转、裁剪好
	 *
	 * @param ac
	 * @param img       路径、资源id、byte[]
	 * @param rotate
	 * @param scale_w_h 图片的最合适宽高比例，-1为按图比例
	 * @param flip      翻转 MakeBmpV2.FLIP_NONE, MakeBmpV2.FLIP_H, MakeBmpV2.FLIP_V
	 * @return
	 */
	public static Bitmap DecodeShowImage(Activity ac, Object img, int rotate, float scale_w_h, int flip)
	{
		Bitmap out = null;

		ShareData.InitData(ac);

		final int DEF_SIZE = 1280;
		int w = ShareData.m_screenRealWidth;
		if(w > DEF_SIZE)
		{
			w = DEF_SIZE;
		}
		int h = ShareData.m_screenRealHeight;
		if(h > DEF_SIZE)
		{
			h = DEF_SIZE;
		}
		Bitmap temp = DecodeImage(ac, img, rotate, scale_w_h, w, h);
		if(temp != null)
		{
			out = temp;
			if(temp.getWidth() > w || temp.getHeight() > h || rotate != 0 || scale_w_h <= 0 || flip != MakeBmpV2.FLIP_NONE)
			{
				out = MakeBmpV2.CreateBitmapV2(temp, rotate, flip, scale_w_h, w, h, Config.ARGB_8888);
				temp.recycle();
				temp = null;
			}
		}

		return out;
	}

	/**
	 * 生成最合适的bitmap(有内存限制),输出的图片没有经过旋转裁剪,需要再调用MakeBmp进行修改
	 *
	 * @param context
	 * @param img       路径、资源id、byte[]
	 * @param rotate    原始bitmap需要旋转的角度0,90,180,270...
	 * @param scale_w_h 图片的最合适宽高比例，-1为按图比例
	 * @param minW      最小宽度，-1为不限制
	 * @param minH      最小高度，-1为不限制
	 * @return
	 */
	public static Bitmap DecodeImage(Context context, Object img, int rotate, float scale_w_h, int minW, int minH)
	{
		rotate = rotate / 90 * 90;

		Bitmap outBmp = null;
		if(img != null)
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if(img instanceof String)
			{
				DecodeFile((String)img, opts);
			}
			else if(img instanceof Integer)
			{
				BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
			}
			else if(img instanceof byte[])
			{
				BitmapFactory.decodeByteArray((byte[])img, 0, ((byte[])img).length, opts);
			}

			int inW = opts.outWidth;
			int inH = opts.outHeight;

			if(rotate % 180 != 0)
			{
				inW += inH;
				inH = inW - inH;
				inW -= inH;
			}

			if(minW < 1 && minH < 1)
			{
				opts.inSampleSize = 1;
			}
			else
			{
				if(minW < 1)
				{
					minW = inW << 1;
				}
				if(minH < 1)
				{
					minH = inH << 1;
				}
				if(scale_w_h <= 0)
				{
					scale_w_h = (float)inW / (float)inH;
				}
				float w = minH * scale_w_h;
				float h = minH;
				if(w > minW)
				{
					w = minW;
					h = minW / scale_w_h;
				}
				opts.inSampleSize = (int)(inW / w < inH / h ? inW / w : inH / h);
				if(opts.inSampleSize < 1)
				{
					opts.inSampleSize = 1;
				}
			}

			long maxMem = (long)(Runtime.getRuntime().maxMemory() * MakeBmpV2.MEM_SCALE);
			int bpp = 4;
			long imgMem = inW / opts.inSampleSize * inH / opts.inSampleSize * bpp;
			if(imgMem > maxMem)
			{
				opts.inSampleSize = (int)Math.ceil(Math.sqrt((long)inW * inH * bpp / (double)maxMem));
			}

			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Config.ARGB_8888;

			boolean success = true;
			do
			{
				try
				{
					success = true;

					if(img instanceof String)
					{
						outBmp = DecodeFile((String)img, opts);
					}
					else if(img instanceof Integer)
					{
						outBmp = BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
					}
					else if(img instanceof byte[])
					{
						outBmp = DecodeByteArr((byte[])img, opts);
					}
				}
				catch(OutOfMemoryError e)
				{
					success = false;
					opts.inSampleSize++; //失败再缩小，直到能解析为止
					if(opts.inSampleSize > 10)
					{
						outBmp = null;
						break;
					}
				}
			}
			while(!success);
		}

		return outBmp;
	}

	/**
	 * 缩放值比{@link #DecodeImage(Context, Object, int, float, int, int)}要小，解出来的图片大一些
	 * @param context
	 * @param img
	 * @param rotate
	 * @param scale_w_h
	 * @param minW
	 * @param minH
	 * @return
	 */
	public static Bitmap DecodeImage2(Context context, Object img, int rotate, float scale_w_h, int minW, int minH)
	{
		rotate = rotate / 90 * 90;

		Bitmap outBmp = null;
		if(img != null)
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if(img instanceof String)
			{
				DecodeFile((String)img, opts);
			}
			else if(img instanceof Integer)
			{
				BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
			}
			else if(img instanceof byte[])
			{
				BitmapFactory.decodeByteArray((byte[])img, 0, ((byte[])img).length, opts);
			}

			int inW = opts.outWidth;
			int inH = opts.outHeight;

			if(rotate % 180 != 0)
			{
				inW += inH;
				inH = inW - inH;
				inW -= inH;
			}

			if(minW < 1 && minH < 1)
			{
				opts.inSampleSize = 1;
			}
			else
			{
				if(minW < 1)
				{
					minW = inW << 1;
				}
				if(minH < 1)
				{
					minH = inH << 1;
				}
				if(scale_w_h <= 0)
				{
					scale_w_h = (float)inW / (float)inH;
				}
				float w = minH * scale_w_h;
				float h = minH;
				if(w > minW)
				{
					w = minW;
					h = minW / scale_w_h;
				}
				opts.inSampleSize = (int)(inW / w < inH / h ? inW / w : inH / h);
				if(opts.inSampleSize < 1)
				{
					opts.inSampleSize = 1;
				}
			}

			float memScale = 1/7f * 2;
			if(android.os.Build.VERSION.SDK_INT < 21)
			{
				memScale *= 0.5f;
			}
			float minScale = 1f / 12f;
			if(memScale < minScale)
			{
				memScale = minScale;
			}

			long maxMem = (long)(Runtime.getRuntime().maxMemory() * memScale);
			int bpp = 4;
			long imgMem = inW / opts.inSampleSize * inH / opts.inSampleSize * bpp;
			if(imgMem > maxMem)
			{
				opts.inSampleSize = (int)Math.ceil(Math.sqrt((long)inW * inH * bpp / (double)maxMem));
			}

			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Config.ARGB_8888;

			boolean success = true;
			do
			{
				try
				{
					success = true;

					if(img instanceof String)
					{
						outBmp = DecodeFile((String)img, opts);
					}
					else if(img instanceof Integer)
					{
						outBmp = BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
					}
					else if(img instanceof byte[])
					{
						outBmp = DecodeByteArr((byte[])img, opts);
					}
				}
				catch(OutOfMemoryError e)
				{
					success = false;
					opts.inSampleSize++; //失败再缩小，直到能解析为止
					if(opts.inSampleSize > 10)
					{
						outBmp = null;
						break;
					}
				}
			}
			while(!success);
		}

		return outBmp;
	}

	/**
	 * 解析常用图片
	 *
	 * @param data
	 * @param opts
	 * @return
	 */
	public static Bitmap DecodeByteArr(byte[] data, BitmapFactory.Options opts)
	{
		Bitmap out = null;

		if(opts != null && opts.inJustDecodeBounds)
		{
			try
			{
				if(data != null)
				{
					if(data[0] == 'F' && data[1] == 'A' && data[2] == 'S' && data[3] == 'T' && data[4] == 'B' && data[5] == 'M' && data[6] == 'P' && data[7] == 0)
					{
						opts.outHeight = data[8] | data[9] << 8 | data[10] << 16 | data[11] << 24;
						opts.outWidth = data[12] | data[13] << 8 | data[14] << 16 | data[15] << 24;
					}
					else
					{
						BitmapFactory.decodeByteArray(data, 0, data.length, opts);
					}
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
			if(opts.inSampleSize < 1)
			{
				opts.inSampleSize = 1;
			}
		}
		else
		{
			if(data != null)
			{
				if(data[0] == 'F' && data[1] == 'A' && data[2] == 'S' && data[3] == 'T' && data[4] == 'B' && data[5] == 'M' && data[6] == 'P' && data[7] == 0)
				{
				}
				else
				{
					BitmapFactory.Options tempOpts = new BitmapFactory.Options();
					tempOpts.inJustDecodeBounds = true;
					BitmapFactory.decodeByteArray(data, 0, data.length, tempOpts);
					if(tempOpts.outMimeType != null && tempOpts.outMimeType.equals("image/jpeg"))
					{
						int inSampleSize = 1;
						if(opts != null)
						{
							inSampleSize = opts.inSampleSize;
						}
						out = ImageUtils.DecodeJpg(data, inSampleSize);
					}
					else
					{
						out = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
					}
				}
			}
		}

		return out;
	}

	/**
	 * 解析常用图片
	 *
	 * @param path
	 * @param opts
	 * @param readExif
	 * @return
	 */
	public static Bitmap DecodeFile(String path, BitmapFactory.Options opts, boolean readExif)
	{
		Bitmap out = null;

		if(opts != null && opts.inJustDecodeBounds)
		{
			if(ImageUtils.CheckIfFastBmp(path) != 0)
			{
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(path);
					if(fis.read() == 'F' && fis.read() == 'A' && fis.read() == 'S' && fis.read() == 'T' && fis.read() == 'B' && fis.read() == 'M' && fis.read() == 'P' && fis.read() == 0)
					{
						opts.outHeight = fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
						opts.outWidth = fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
					}
					fis.close();
					fis = null;
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						if(fis != null)
						{
							fis.close();
							fis = null;
						}
					}
					catch(Throwable e2)
					{
						e2.printStackTrace();
					}
				}
			}
			else
			{
				BitmapFactory.decodeFile(path, opts);
			}

			if(opts.inSampleSize < 1)
			{
				opts.inSampleSize = 1;
			}
		}
		else
		{
			if(ImageUtils.CheckIfFastBmp(path) != 0)
			{
				int inSampleSize = 1;
				if(opts != null)
				{
					inSampleSize = opts.inSampleSize;
				}
				out = ImageUtils.ReadFastBmp(path, inSampleSize);
			}
			else if(ImageUtils.CheckIfJpg(path) != 0)
			{
				int inSampleSize = 1;
				if(opts != null)
				{
					inSampleSize = opts.inSampleSize;
				}
				out = ImageUtils.ReadJpg(path, inSampleSize);

				if(readExif)
				{
					int[] info = CommonUtils.GetImgInfo(path);
					if(info[0] % 360 != 0 || info[1] != MakeBmpV2.FLIP_NONE)
					{
						Bitmap temp = out;
						out = MakeBmpV2.CreateBitmapV2(temp, info[0], info[1], -1, -1, -1, Config.ARGB_8888);
						temp.recycle();
						temp = null;
					}
				}
			}
			//else if(ImageUtils.CheckIfPng(path) != 0)
			//{
			//	int inSampleSize = 1;
			//	if(opts != null)
			//	{
			//		inSampleSize = opts.inSampleSize;
			//	}
			//	out = ImageUtils.ReadPng(path, inSampleSize);
			//}
			else
			{
				out = BitmapFactory.decodeFile(path, opts);
			}
		}

		return out;
	}

	/**
	 * 解析常用图片
	 *
	 * @param path
	 * @param opts
	 * @return
	 */
	public static Bitmap DecodeFile(String path, BitmapFactory.Options opts)
	{
		return DecodeFile(path, opts, false);
	}

	/**
	 * m_img需要自行转换
	 *
	 * @param path
	 * @return
	 */
	public static RotationImg2 Path2ImgObj(String path)
	{
		RotationImg2 out = new RotationImg2();
		out.m_orgPath = path;
		out.m_img = path;
		int[] vs = CommonUtils.GetImgInfo(path);
		out.m_degree = vs[0];
		out.m_flip = vs[1];

		return out;
	}

	/**
	 * 插入数据库;
	 *
	 * @param picPath
	 * @return
	 */
	protected static Uri InsertImgToSys(Context context, String picPath)
	{
		File file = new File(picPath);
		if(!file.exists())
		{
			return null;
		}
		ContentResolver resolver = context.getContentResolver();
		int degree = Path2ImgObj(picPath).m_degree;
		long dateTaken = System.currentTimeMillis();
		long size = file.length();
		String fileName = file.getName();
		ContentValues values = new ContentValues(7);
		values.put(Images.Media.DATE_TAKEN, dateTaken);//时间;
		values.put(Images.Media.DATE_MODIFIED, dateTaken / 1000);//时间;
		values.put(Images.Media.DATE_ADDED, dateTaken / 1000);//时间;
		values.put(ImageColumns.DATA, picPath);//路径;
		values.put(Images.Media.DISPLAY_NAME, fileName);//文件名;
		values.put(Images.Media.ORIENTATION, degree);//角度;
		values.put(Images.Media.SIZE, size);//图片的大小;
		Uri uri = null;
		try
		{
			if(resolver != null)
			{
				uri = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MediaScannerConnection.scanFile(context, new String[]{file.toString()}, new String[]{"image/png", "image/jpeg"}, null);
		}
		return uri;
	}

	public static void FileScan(Context context, String file)
	{
		if(file == null) return;
		Uri data = InsertImgToSys(context, file); //修改时间:2013年8月27日,先插入数据库;
		if(data == null)
		{
			data = Uri.parse("file://" + file);
		}
		File external = Environment.getExternalStorageDirectory();
		if(external == null) return;
		String externalDir = external.getPath();
		if(externalDir == null) return;
		if(file.startsWith(externalDir) == false) return;
		if(context != null)
		{
			ImageStore.clearCache();
			//context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
		}
	}

	protected static String MakePhotoName(float scale_w_h, EffectInfo info, int mode)
	{
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
		String strDate = df.format(date);
		String strRand = Integer.toString((int)(Math.random() * 100));
		if(strRand.length() < 4)
		{
			strRand = "0000".substring(strRand.length()) + strRand;
		}
		int layout = 0;
		float r4_3 = 4.0f / 3.0f;
		float r3_4 = 3.0f / 4.0f;
		float r16_9 = 16.0f / 9.0f;
		float r9_16 = 9.0f / 16.0f;
		float r = scale_w_h;
		if(r > 0.95 && r < 1.05) layout = 3;
		else if(r > r4_3 - 0.05f && r < r4_3 + 0.05f) layout = 1;
		else if(r > r16_9 - 0.05f && r < r16_9 + 0.05f) layout = 4;
		else if(r > r3_4 - 0.05f && r < r3_4 + 0.05f) layout = 2;
		else if(r > r9_16 - 0.05f && r < r9_16 + 0.05f) layout = 5;
		//if(PocoCamera.main.getLayoutMode() == 6)
		//{
		//	layout = 6;
		//}

		String color = "000";
		String decorate = "000";
		String frame = "000";
		if(info != null)
		{
			if(info.effect != -1)
			{
				color = Integer.toString(info.effect & 0x0fff, 16);
				if(color.length() < 3)
				{
					color = "000".substring(color.length()) + color;
				}
			}
			if(info.decorate != -1)
			{
				decorate = Integer.toString(info.decorate & 0x0fff, 16);
				if(decorate.length() < 3)
				{
					decorate = "000".substring(decorate.length()) + decorate;
				}
			}
			if(info.frame != -1)
			{
				frame = Integer.toString(info.frame & 0x0fff, 16);
				if(frame.length() < 3)
				{
					frame = "000".substring(frame.length()) + frame;
				}
			}
		}

		String strMode = Integer.toHexString(mode);
		String type = strMode + Integer.toString(layout);
		type += "-" + color;
		type += decorate;
		type += frame;
		String str = "MA" + strDate + strRand + "-" + type + ".jpg";
		return str;
	}

	/**
	 * 构造一个图片保存的路径
	 *
	 * @param scale_w_h w/h比例
	 * @return
	 */
	public static String MakeSavePhotoPath(Context context, float scale_w_h)
	{
		String out = SettingInfoMgr.GetSettingInfo(context).GetPhotoSavePath();

		out += File.separator + MakePhotoName(scale_w_h, null, 0);

		return out;
	}

	/**
	 * 获取图片的w/h比例
	 */
	public static float GetImgScaleWH(String path)
	{
		float out = 0;
		if(path != null)
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);
			if(opts.outHeight != 0)
			{
				out = (float)opts.outWidth / (float)opts.outHeight;
			}
		}
		return out;
	}

	/**
	 * 临时图片,保存为FastBmp格式
	 *
	 * @param bmp
	 * @param path
	 * @return
	 */
	public static boolean SaveTempImg(Bitmap bmp, String path)
	{
		boolean out = false;

		if(bmp != null)
		{
			Bitmap temp = bmp;
			if(!bmp.isMutable() || bmp.getConfig() != Config.ARGB_8888)
			{
				temp = bmp.copy(Config.ARGB_8888, true);
			}
			if(ImageUtils.WriteFastBmp(temp, 100, path) == 0)
			{
				out = true;
			}
			if(temp != bmp)
			{
				temp.recycle();
				temp = null;
			}
		}

		return out;
	}

	/**
	 * 保存图片并更新相册
	 *
	 * @param context
	 * @param bmp
	 * @param path    为null时自动创建一个路径
	 * @param quality 质量(1-100)
	 * @return
	 */
	public static String SaveImg(Context context, Bitmap bmp, @Nullable String path, int quality)
	{
		return SaveImg(context, bmp, path, quality, true);
	}

	/**
	 * 保存图片
	 *
	 * @param context
	 * @param bmp
	 * @param path        为null时自动创建一个路径
	 * @param quality     质量(1-100)
	 * @param updateAlbum
	 * @return
	 */
	public static String SaveImg(Context context, Bitmap bmp, @Nullable String path, int quality, boolean updateAlbum)
	{
		String out = null;

		if(context != null && bmp != null && bmp.getWidth() > 0 && bmp.getHeight() > 0)
		{
			String tempPath = path;
			if(tempPath == null)
			{
				tempPath = MakeSavePhotoPath(context, (float)bmp.getWidth() / (float)bmp.getHeight());
			}

			Bitmap temp = bmp;
			if(!bmp.isMutable() || bmp.getConfig() != Config.ARGB_8888)
			{
				temp = bmp.copy(Config.ARGB_8888, true);
			}

			if(ImageUtils.WriteJpg(temp, quality, tempPath) == 0)
			{
				out = tempPath;
				if(updateAlbum)
				{
					FileScan(context, out);
				}
			}
			if(temp != bmp)
			{
				temp.recycle();
				temp = null;
			}
		}

		return out;
	}

	/**
	 *
	 * @param context
	 * @param res
	 * @return
	 */
	public static Bitmap MakeLogo(Context context, Object res)
	{
		Bitmap out = null;

		if(res != null)
		{
			if(res instanceof Bitmap)
			{
				out = (Bitmap)res;
				return out;
			}
			if(res instanceof String)
			{
				if(!FileUtil.isFileExists((String)res))
				{
					try
					{
						InputStream is = context.getAssets().open((String)res);
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						byte[] bytes = new byte[1024];
						while(is.read(bytes) != -1)
						{
							bout.write(bytes, 0, bytes.length);
						}
						res = bout.toByteArray();
						bout.close();
						is.close();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			out = Utils.DecodeImage(MyFramework2App.getInstance().getApplicationContext(), res, 0, -1, -1, -1);
		}
		return out;
	}

	public static Bitmap MakeSinaLogo(Context context, Object res)
	{
		Bitmap outBmp = null;
		if(res != null)
		{
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if(res instanceof String)
			{
				if(!FileUtil.isFileExists((String)res))
				{
					BitmapFactory.decodeStream(FileUtil.getAssetsStream(context, (String)res), null, opts);
				}
				else
				{
					Utils.DecodeFile((String)res, opts);
				}
			}
			else if(res instanceof Integer)
			{
				BitmapFactory.decodeResource(MyFramework2App.getInstance().getApplicationContext().getResources(), (Integer)res, opts);
			}

			int inW = opts.outWidth;
			int inH = opts.outHeight;
			opts.inSampleSize = 1;

			int bpp = 4;
			long maxMem = 32 * 1024;
			long imgMem = inW / opts.inSampleSize * inH / opts.inSampleSize * bpp;
			if(imgMem > maxMem)
			{
				opts.inSampleSize = (int)Math.ceil(Math.sqrt((long)inW * inH * bpp / (double)maxMem));
			}

			opts.inJustDecodeBounds = false;
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

			boolean success = true;
			do
			{
				success = true;
				if(res instanceof String)
				{
					if(!FileUtil.isFileExists((String)res))
					{
						outBmp = BitmapFactory.decodeStream(FileUtil.getAssetsStream(context, (String)res), null, opts);
					}
					else
					{
						outBmp = Utils.DecodeFile((String)res, opts);
					}
				}
				else if(res instanceof Integer)
				{
					outBmp = BitmapFactory.decodeResource(MyFramework2App.getInstance().getApplicationContext().getResources(), (Integer)res, opts);
				}
				/*if(outBmp.getWidth() * outBmp.getHeight() * 4 > maxMem)
				{
					success = false;
					opts.inSampleSize++; //失败再缩小，直到能解析为止
				}*/
			}
			while(!success);
		}

//		System.out.println("outBmp getWidth: " + outBmp.getWidth() + "height: " + outBmp.getHeight());
		return outBmp;
	}

	//照片加日期
	public static void attachDate(Bitmap bmp)
	{
		String time = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date());
		Paint p = new Paint();
		p.setAntiAlias(true);
		//p.setColor(0xFFFFFF00);
		p.setTextSize(ShareData.PxToDpi_hdpi(16));
		p.setFakeBoldText(true);
		int textWidth = (int)(p.measureText(time) + 0.5f);
		int textHeight = (int)(p.descent() - p.ascent());
		int paintSize = 16;
		//确保文字清晰
		while(textWidth < bmp.getWidth() / 4)
		{
			paintSize++;
			p.setTextSize(ShareData.PxToDpi_hdpi(paintSize));
			p.setAntiAlias(true);
			textWidth = (int)(p.measureText(time) + 0.5f);
			textHeight = (int)(p.descent() - p.ascent());
		}
		//确保文字在图片的右下角
		while(textWidth > bmp.getWidth() / 3)
		{
			paintSize--;
			p.setTextSize(ShareData.PxToDpi_hdpi(paintSize));
			p.setAntiAlias(true);
			textWidth = (int)(p.measureText(time) + 0.5f);
			textHeight = (int)(p.descent() - p.ascent());
		}

		int x = bmp.getWidth() - textWidth;
		int y = bmp.getHeight() - textHeight;
		x = x >= 0 ? x : 0;
		y = y >= 0 ? y : 0;

		int w = bmp.getWidth() - x;
		int h = bmp.getHeight() - y;
		if(w > 90) w = 90;
		if(h > 14) h = 14;
		int len = w * h;
		int[] pixels = new int[len];
		bmp.getPixels(pixels, 0, w, x, y, w, h);
		int r = 0, g = 0, b = 0;
		for(int i = 0; i < len; i++)
		{
			int pixel = pixels[i];
			r += (pixel & 0x00ff0000) >> 16;
			g += (pixel & 0x0000ff00) >> 8;
			b += (pixel & 0x000000ff);
		}
		//int color = (~((r/len)<<16|(g/len)<<8|(b/len)))|0xff000000;
		int color = (float)(r / len + g / len + b / len) / (float)765 > 0.5 ? 0xff000000 : 0xffffffff;
		p.setColor(color);

		Canvas c = new Canvas(bmp);
		c.drawText(time, x, y, p);
	}

	/**
	 * 主要用于触发统计
	 *
	 * @param url
	 */
	public static void UrlTrigger(final Context context, final String url)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String myUrl = PocoDecodeUrl(context, url);
				NetCore2 net = new MyNetCore(context);
				net.HttpGet(myUrl);
				net.ClearAll();
			}
		}).start();
	}

	public static void SendTj(Context context, String[] arr)
	{
		if(arr != null)
		{
			for(String str : arr)
			{
				if(str.startsWith("http"))
				{
					Utils.UrlTrigger(context, str);
				}
				else
				{
					try
					{
						int value = Integer.parseInt(str);
						if(value != 0)
						{
							TongJi2.AddCountById(str);
						}
					}
					catch(Throwable e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static String PocoDecodeUrl(Context context, String url)
	{
		String out = url;

		if(url != null)
		{
			url = url.replace("[OS]", "0");
			String mac = CommonUtils.GetLocalMacAddress(context);
			if(mac != null)
			{
				mac = CommonUtils.Encrypt("MD5", mac);
				url = url.replace("[MAC]", mac);
				url = url.replace("__MAC__", mac);
			}
			String ip = CommonUtils.GetLocalIpAddress();
			if(ip != null)
			{
				url = url.replace("[IP]", ip);
				url = url.replace("__IP__", ip);
			}
			String imei = CommonUtils.GetIMEI(context);
			if(imei != null)
			{
				imei = CommonUtils.Encrypt("MD5", imei);
				url = url.replace("[IMEI]", imei);
				url = url.replace("__IMEI__", imei);
			}

			out = url;
		}

		return out;
	}

	/**
	 * 检查网络状态
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(info != null)
		{
			return info.isAvailable();
		}
		return false;
	}

	/**
	 * 检测当前是否是WiFi连接
	 */
	public static boolean isWifiConnected(Context context)
	{
		ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if(info != null && info.getType() == ConnectivityManager.TYPE_WIFI)
		{
			return true;
		}
		return false;
	}

	public static boolean isSelected(Matrix matrix, float[] points, float x, float y)
	{
		if(matrix != null && points != null && points.length == 8)
		{
			Matrix m = new Matrix();
			matrix.invert(m);
			float[] src = {x, y};
			float[] dsts = new float[2];
			m.mapPoints(dsts, src);
			float minX = points[0] < points[2] ? points[0] : points[2];
			float maxX = points[0] < points[2] ? points[2] : points[0];
			float minY = points[1] < points[5] ? points[1] : points[5];
			float maxY = points[1] < points[5] ? points[5] : points[1];
			if(dsts[0] >= minX && dsts[0] <= maxX && dsts[1] >= minY && dsts[1] <= maxY)
			{
				return true;
			}
		}
		return false;
	}


	/**
	 * 要求dst和src的长度相等，并且长度为8
	 * @param dst
	 * @param src
	 */
	public static void RoundPoint(float[] dst, float[] src)
	{
		if(dst != null && src != null && dst.length == src.length && dst.length == 8)
		{
			float dstMinX = Math.min(Math.min(dst[0], dst[2]), Math.min(dst[4], dst[6]));
			float dstMaxX = Math.max(Math.max(dst[0], dst[2]), Math.max(dst[4], dst[6]));
			float dstMinY = Math.min(Math.min(dst[1], dst[3]), Math.min(dst[5], dst[7]));
			float dstMaxY = Math.max(Math.max(dst[1], dst[3]), Math.max(dst[5], dst[7]));

			float srcMinX = Math.min(Math.min(src[0], src[2]), Math.min(src[4], src[6]));
			float srcMaxX = Math.max(Math.max(src[0], src[2]), Math.max(src[4], src[6]));
			float srcMinY = Math.min(Math.min(src[1], src[3]), Math.min(src[5], src[7]));
			float srcMaxY = Math.max(Math.max(src[1], src[3]), Math.max(src[5], src[7]));

			dst[0] = dst[6] = Math.min(dstMinX, srcMinX);
			dst[1] = dst[3] = Math.min(dstMinY, srcMinY);
			dst[2] = dst[4] = Math.max(dstMaxX, srcMaxX);
			dst[5] = dst[7] = Math.max(dstMaxY, srcMaxY);
		}
	}

	/**
	 * 两条直线的交点
	 * @param line1	长度为4	直线1
	 * @param line2	同上	直线2
	 * @return
	 */
	public static float[] CrossPoints(float[] line1, float[] line2)
	{
		float x, y;
		float x1, y1, x2, y2;
		float x3, y3, x4, y4;
		if(line1 != null && line2 != null && line1.length == 4 && line2.length == 4)
		{
			x1 = line1[0];
			y1 = line1[1];
			x2 = line1[2];
			y2 = line1[3];
			x3 = line2[0];
			y3 = line2[1];
			x4 = line2[2];
			y4 = line2[3];

			boolean flag1=false;
			boolean flag2=false;
			float k1 = Float.MAX_VALUE, k2 = Float.MAX_VALUE;

			if((x1-x2)==0)
				flag1=true;
			if((x3-x4)==0)
				flag2=true;

			if(!flag1)
				k1=(y1-y2)/(x1-x2);
			if(!flag2)
				k2=(y3-y4)/(x3-x4);

			if(k1==k2)
				return null;

			if(flag1){
				if(flag2)
					return null;
				x=x1;
				if(k2==0){
					y=y3;
				}else{
					y=k2*(x-x4)+y4;
				}
			}else if(flag2){
				x=x3;
				if(k1==0){
					y=y1;
				}else{
					y=k1*(x-x2)+y2;
				}
			}else{
				if(k1==0){
					y=y1;
					x=(y-y4)/k2+x4;
				}else if(k2==0){
					y=y3;
					x=(y-y2)/k1+x2;
				}else{
					x=(k1*x2-k2*x4+y4-y2)/(k1-k2);
					y=k1*(x-x2)+y2;
				}
			}
			float[] out = new float[2];
			out[0] = x;
			out[1] = y;
			return out;

		}
		return null;
	}

	public static byte[] InputStreamToByteArray(InputStream stream)
	{
		byte[] out = null;
		ByteArrayOutputStream baos = null;
		if(stream != null)
		{
			try
			{
				byte[] buffer = new byte[1024];
				baos = new ByteArrayOutputStream();
				int bl;
				while((bl = stream.read(buffer)) > 0)
				{
					baos.write(buffer, 0, bl);
				}
				out = baos.toByteArray();
				stream.close();
				baos.close();
			}catch(Exception e){}
			finally
			{
				try
				{
					if(baos != null){
						baos.close();
					}
					stream.close();
				}catch(Exception e){}
			}
		}
		return out;
	}

	public static void AlphaAnim(View view, boolean show, int duration)
	{
		if (view == null)
			return;
		view.clearAnimation();

		int start;
		int end;
		if (show)
		{
			view.setVisibility(View.VISIBLE);

			start = 0;
			end = 1;
		}
		else
		{
			view.setVisibility(View.GONE);

			start = 1;
			end = 0;
		}

		AnimationSet as;
		AlphaAnimation aa;
		as = new AnimationSet(true);
		aa = new AlphaAnimation(start, end);
		aa.setDuration(duration);
		as.addAnimation(aa);
		view.startAnimation(as);
	}


	/**
	 * 修改editText控件的光标颜色
	 * @param editText 要修改光标颜色的editText
	 * @param color 光标的新颜色
	 */
	public static void modifyEditTextCursor(EditText editText, int color) {
		try {
			Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
			fCursorDrawableRes.setAccessible(true);
			int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
			Field fEditor = TextView.class.getDeclaredField("mEditor");
			fEditor.setAccessible(true);
			Object editor = fEditor.get(editText);
			Class<?> clazz = editor.getClass();
			Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
			fCursorDrawable.setAccessible(true);
			Drawable[] drawables = new Drawable[2];
			drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
			drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
			drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			fCursorDrawable.set(editor, drawables);
		} catch (Throwable ignored) {

		}
	}


}
