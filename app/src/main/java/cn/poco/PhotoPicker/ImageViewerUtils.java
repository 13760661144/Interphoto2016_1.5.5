package cn.poco.PhotoPicker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;

import java.io.File;
import java.io.FileInputStream;

import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.MakeBmp;
import cn.poco.utils.JniUtils;
import cn.poco.utils.Utils;

public class ImageViewerUtils
{
	public static float sDensity;
	public static float sDensityDpi;
	public static int sScreenW;
	public static int sScreenH;

	public static void init(Activity activiy)
	{
		Display dis = activiy.getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		dis.getMetrics(dm);
		int h = dis.getHeight();
		int w = dis.getWidth();
		sScreenW = w < h ? w : h;
		sScreenH = w < h ? h : w;
		sDensity = dm.density;
		sDensityDpi = dm.densityDpi;
	}

	public static Bitmap scaleBitmap(Bitmap bmp, int size)
	{
		Bitmap.Config config = bmp.getConfig();
		if(config == null)
		{
			config = Bitmap.Config.ARGB_8888;
		}
		return scaleBitmap(bmp, size, config, false);
	}

	public static Bitmap scaleBitmap(Bitmap bmp, int size, boolean keepQuality)
	{
		Bitmap.Config config = bmp.getConfig();
		if(config == null)
		{
			config = Bitmap.Config.ARGB_8888;
		}
		return scaleBitmap(bmp, size, config, keepQuality);
	}

	public static Bitmap scaleBitmap(Bitmap bmp, int size, Bitmap.Config config, boolean keepQuality)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		if(keepQuality == true && w < size && h < size)
		{
			return bmp;
		}
		int dw = 0;
		int dh = 0;
		float r1 = (float)w / (float)h;
		if(r1 < 1)
		{
			dh = size;
			dw = (int)(size * r1);
		}
		else
		{
			dw = size;
			dh = (int)(size / r1);
		}
		Bitmap bitmap = scaleBitmap(bmp, dw, dh, config);
		return bitmap;
	}

	public static Bitmap scaleBitmap(Bitmap bitmap, int w, int h, Bitmap.Config config)
	{
		if(w < 1)
			w = 1;
		if(h < 1)
			h = 1;
		Bitmap bmp = Bitmap.createBitmap(w, h, config);
		Canvas canvas = new Canvas(bmp);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(bitmap, null, new Rect(0, 0, w, h), null);
		return bmp;
	}

	public static String getSdcardPath()
	{
		File sdcard = Environment.getExternalStorageDirectory();
		if(sdcard == null)
		{
			return "";
		}
		return sdcard.getPath();
	}

	public static int getRealPixel2(int pxSrc)
	{
		return (int)(pxSrc * sScreenW / 480);
	}

	public static int getRealPixel3(int size)
	{
		return (int)(size / 2f * sDensity + 0.5f);
	}

	public static int getScreenW()
	{
		return sScreenW;
	}

	public static int getScreenH()
	{
		return sScreenH;
	}

	public static Bitmap decodeFile(String img, int size)
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		decodeFile(img, opts, false);
		opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		if(opts.outHeight > 0 && opts.outWidth > 0)
		{
			int bigOne = opts.outWidth > opts.outHeight ? opts.outWidth : opts.outHeight;
			opts.inSampleSize = bigOne / size;
			bitmap = decodeFile(img, opts, true);
		}
		return bitmap;
	}

	public static Bitmap decodeFile(String img, BitmapFactory.Options opts, boolean processExif)
	{
		boolean handled = false;
		Bitmap bmp = null;
		if(!img.endsWith(".jpg") && !img.endsWith(".JPG") && !img.endsWith(".JPEG") && !img.endsWith(".jpeg"))
		{
			if(opts != null && opts.inJustDecodeBounds == true)
			{
				try
				{
					byte[] buffer = new byte[10240];
					int w = 0, h = 0;
					FileInputStream fis = new FileInputStream(img);
					if('B' == fis.read() && 'M' == fis.read())
					{
						fis.read(buffer, 0, 16);
						w = fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
						h = fis.read() | fis.read() << 8 | fis.read() << 16 | fis.read() << 24;
						opts.outHeight = h;
						opts.outWidth = w;
						handled = true;
					}
					fis.close();
				}
				catch(Exception e)
				{
				}
			}
			else
			{
				if(opts.inSampleSize < 1)
					opts.inSampleSize = 1;
				bmp = JniUtils.readAlphaBitmap(img.getBytes(), opts.inSampleSize);
				if(bmp != null)
				{
					handled = true;
				}
			}
		}
		if(handled == false)
		{
			bmp = null;
			if(!opts.inJustDecodeBounds)
			{
				bmp = ReadJpgAndCount(img, opts.inSampleSize);
				if(bmp == null)
				{
					BitmapFactory.Options tempOpts = new BitmapFactory.Options();
					tempOpts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(img, tempOpts);
					if(tempOpts.outMimeType != null && !tempOpts.outMimeType.equals(""))
					{
						//System.out.println(opts.outMimeType);
						if(tempOpts.outMimeType.equals("image/png"))
						{
							bmp = cn.poco.imagecore.ImageUtils.ReadPng(img, opts.inSampleSize);
						}
					}
				}
			}
			if(bmp == null)
			{
				bmp = BitmapFactory.decodeFile(img, opts);
			}
			if(processExif && bmp != null)
			{
				if(img.endsWith(".jpg") || img.endsWith(".JPG") || img.endsWith(".jpeg") || img.endsWith(".JPEG") || img.endsWith(".dat"))
				{
					int rotation = CommonUtils.GetImgInfo(img)[0];
					if(rotation != 0)
					{
						//Matrix m = new Matrix();
						//m.setRotate(rotation);
						//bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
						//主要为了释放原图，如果不释放后面可能导致oom
						Bitmap temp = MakeBmp.CreateBitmap(bmp, -1, -1, -1, rotation, Bitmap.Config.ARGB_8888);
						bmp.recycle();
						bmp = temp;
					}
				}
			}
		}
		return bmp;
	}

	public static Bitmap decodeFile(Context c, String img, BitmapFactory.Options opts, boolean processExif, int max_size)
	{
		int rotation = CommonUtils.GetImgInfo(img)[0];

		Bitmap bmp = Utils.DecodeImage(c, img, rotation, -1, -1, -1);
		Bitmap out = MakeBmp.CreateBitmap(bmp, -1, -1, -1, rotation, Bitmap.Config.ARGB_8888);
		bmp.recycle();
		bmp = null;

		return out;
	}

	public static Bitmap ReadJpgAndCount(String path, int inSampleSize)
	{
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		if(opts.outMimeType != null && !opts.outMimeType.equals(""))
		{
			//System.out.println(opts.outMimeType);
			if(opts.outMimeType.equals("image/jpeg"))
			{
				if(inSampleSize < 1)
				{
					inSampleSize = 1;
				}
				//System.out.println(inSampleSize);
				Bitmap bmp = cn.poco.imagecore.ImageUtils.ReadJpg(path, inSampleSize);
				return bmp;
			}
		}

		return null;
	}
}
