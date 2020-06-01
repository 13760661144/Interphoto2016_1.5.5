package cn.poco.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.File;

import cn.poco.interphoto2.R;

/**
 * Created by admin on 2016/10/12.
 */

public class GlideImageLoader
{
	public static void LoadCircleImg(ImageView view, Context context, String url, int broderSize, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		url = MakeLoadUrl(url);
		DrawableTypeRequest<String> request = Glide.with(context).load(url);
		request.priority(Priority.HIGH);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.transform(new GlideCircleTransform(context, broderSize)).into(view);
	}

	public static void LoadCircleImg(ImageView view, Context context, Integer url, int broderSize, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		DrawableTypeRequest<Integer> request = Glide.with(context).load(url);
		request.priority(Priority.HIGH);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.transform(new GlideCircleTransform(context, broderSize)).into(view);
	}

	public static void LoadRoundImg(ImageView view, Context context, String url, int roundSize, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		url = MakeLoadUrl(url);
		DrawableTypeRequest<String> request = Glide.with(context).load(url);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.transform(new GlideRoundTransform(context, roundSize)).into(view);
	}

	public static void LoadRoundImg(ImageView view, Context context, Integer url, int roundSize, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		DrawableTypeRequest<Integer> request = Glide.with(context).load(url);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.transform(new GlideRoundTransform(context, roundSize)).into(view);
	}

	public static void LoadImg(final ImageView view, Context context, String url, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		url = MakeLoadUrl(url);
		DrawableTypeRequest<String> request = Glide.with(context).load(url);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

//		request.diskCacheStrategy(DiskCacheStrategy.NONE);
//		request.error(R.drawable.about_interphoto);
		request.into(view);
	}

	public static void LoadImg(final ImageView view, Context context, Uri url, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		DrawableTypeRequest<Uri> request = Glide.with(context).load(url);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.RESULT);
		request.error(R.drawable.about_interphoto);
		request.into(view);
	}

	public static void LoadImg(final ImageView view, Context context, Integer url, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		DrawableTypeRequest<Integer> request = Glide.with(context).load(url);
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

//		request.diskCacheStrategy(DiskCacheStrategy.NONE);
//		request.error(R.drawable.about_interphoto);
		request.into(view);
	}

	public static void LoadImg(final ImageView view, Context context, String url, int width, int height, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		url = MakeLoadUrl(url);
		DrawableTypeRequest<String> request = Glide.with(context).load(url);
		if(width > 0 && height > 0)
		{
			request.override(width, height);
		}
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

//		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.error(R.drawable.about_interphoto);
		request.into(view);
	}

	public static void LoadImg(final ImageView view, Context context, Integer url, int width, int height, boolean hasAnim)
	{
		Glide.get(context).setMemoryCategory(MemoryCategory.LOW);
		DrawableTypeRequest<Integer> request = Glide.with(context).load(url);
		if(width > 0 && height > 0)
		{
			request.override(width, height);
		}
		request.centerCrop();
		if(hasAnim)
		{
			request.crossFade();
		}
		else
		{
			request.dontAnimate();
		}

		request.diskCacheStrategy(DiskCacheStrategy.NONE);
		request.error(R.drawable.about_interphoto);
		request.into(view);
	}

	public static String MakeLoadUrl(String url)
	{
		if(url != null && url.length() > 0)
		{
			if(url.startsWith("http://") || url.startsWith("https://"))
				return url;
			File file = new File(url);
			if(!file.exists())
			{
				url = "file:///android_asset/" + url;
			}
		}
		return url;
	}

	public static void Clear(Context context)
	{
		Glide.get(context).clearMemory();
//		Glide.get(context).clearDiskCache();
	}

	public static void Clear(View view)
	{
		Glide.clear(view);
	}

	public static class GlideCircleTransform extends BitmapTransformation
	{
		private int broderSize;
		public GlideCircleTransform(Context context, int broderSize) {
			super(context);
			this.broderSize = broderSize;
		}

		@Override
		protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
			return makeCircleBmp(pool, toTransform, outWidth, outHeight, broderSize, 0xffffffff);
		}

		@Override public String getId() {
			return getClass().getName();
		}
	}

	public static class GlideRoundTransform extends BitmapTransformation
	{
		private int roundSize;
		public GlideRoundTransform(Context context, int broderSize) {
			super(context);
			this.roundSize = broderSize;
		}

		@Override
		protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
			return ImageUtil.roundCrop(pool, toTransform, roundSize);
		}

		@Override public String getId() {
			return getClass().getName();
		}
	}



	public static Bitmap makeCircleBmp(BitmapPool pool, Bitmap source, int outWidth, int outHeight, int broderSize, int broderColor)
	{
		if (source == null) return null;
		int size = Math.min(outWidth, outHeight);
		Bitmap squared = Bitmap.createScaledBitmap(source, outWidth, outHeight, true);
		Bitmap result = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
		if (result == null) {
			result = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
		paint.setAntiAlias(true);
		float r = size / 2f;
		canvas.drawCircle(r, r, r - broderSize, paint);

		if(broderSize > 0)
		{
			paint.reset();
			paint.setColor(broderColor);
			paint.setStrokeWidth(broderSize);
			paint.setStyle(Paint.Style.STROKE);
			paint.setAntiAlias(true);
			paint.setFlags(Paint.ANTI_ALIAS_FLAG);
			canvas.drawCircle(size / 2f, size / 2f, size / 2f - broderSize, paint);
		}

		return result;
	}
}
