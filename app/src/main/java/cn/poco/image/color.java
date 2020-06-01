package cn.poco.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class color 
{
	
	/**
	 *  constant for contrast calculations:
	 */
	  private static float[] DELTA_INDEX = new float[]{
		0f,    0.01f, 0.02f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.1f,  0.11f,
		0.12f, 0.14f, 0.15f, 0.16f, 0.17f, 0.18f, 0.20f, 0.21f, 0.22f, 0.24f,
		0.25f, 0.27f, 0.28f, 0.30f, 0.32f, 0.34f, 0.36f, 0.38f, 0.40f, 0.42f,
		0.44f, 0.46f, 0.48f, 0.5f,  0.53f, 0.56f, 0.59f, 0.62f, 0.65f, 0.68f, 
		0.71f, 0.74f, 0.77f, 0.80f, 0.83f, 0.86f, 0.89f, 0.92f, 0.95f, 0.98f,
		1.0f,  1.06f, 1.12f, 1.18f, 1.24f, 1.30f, 1.36f, 1.42f, 1.48f, 1.54f,
		1.60f, 1.66f, 1.72f, 1.78f, 1.84f, 1.90f, 1.96f, 2.0f,  2.12f, 2.25f, 
		2.37f, 2.50f, 2.62f, 2.75f, 2.87f, 3.0f,  3.2f,  3.4f,  3.6f,  3.8f,
		4.0f,  4.3f,  4.7f,  4.9f,  5.0f,  5.5f,  6.0f,  6.5f,  6.8f,  7.0f,
		7.3f,  7.5f,  7.8f,  8.0f,  8.4f,  8.7f,  9.0f,  9.4f,  9.6f,  9.8f, 
		10.0f
	};
	  
  /**
	 *  调节亮度
	 *  @src 源Bitmap 
	 *  @value 亮度值，范围在[-255, 255]。
	 *  @返回一个处理后的新Bitmap
	 */  
	public static Bitmap changeBrightness(Bitmap src,float value)
	{
		value = Math.max( -255, Math.min( value, 255 ) );
		
		float[] brightness = new float[]{
				1, 0, 0, 0, value,
				0, 1, 0, 0, value,
				0, 0, 1, 0, value,
				0, 0, 0, 1, 0,
				0, 0, 0, 0, 1
		};
		
		Bitmap returnBitmap  = Bitmap.createBitmap(src);		
		Canvas canvas = new Canvas(returnBitmap);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(brightness);
		ColorMatrixColorFilter  brightnessFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(brightnessFilter);	
		canvas.drawBitmap(src, 0, 0, paint);
		
		return returnBitmap;		
		
	}
	
	/**
	 *  调节对比度	
	 *  @src 源Bitmap 
	 *  @value 亮度值，范围在[-100, 100]。
	 *  @返回一个处理后的新Bitmap
	 */  
	public static Bitmap changeContrast(Bitmap src,float value)
	{
		int value_index = (int)Math.max( -100, Math.min( value, 100 ) );
		
		float x = 0;
		if( value_index < 0 )
		{
			x = 127 + value_index * 127 / 100;
		}
		else
		{
			x = value_index % 1;
			if( x == 0 )
			{
				x = DELTA_INDEX[value_index];
			}
			else
			{
				//x = DELTA_INDEX[(p_val<<0)]; // this is how the IDE does it.
				x = DELTA_INDEX[(value_index<<0)] * (1 - x) + DELTA_INDEX[(value_index<<0) + 1] * x; // use linear interpolation for more granularity.
			}
			x = x * 127 + 127;
		}
		
		
		float[] contrast = new float[]{
				x/127, 0,     0,     0, 0.5f*(127-x),
				0,     x/127, 0,     0, 0.5f*(127-x),
				0,     0,     x/127, 0, 0.5f*(127-x),
				0,     0,     0,     1, 0,
				0,     0,     0,     0, 1
		};
		
		Bitmap returnBitmap  = Bitmap.createBitmap(src);		
		Canvas canvas = new Canvas(returnBitmap);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(contrast);
		ColorMatrixColorFilter  contrastFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(contrastFilter);	
		canvas.drawBitmap(src, 0, 0, paint);
		
		return returnBitmap;		
		
	}
	
	/**
	 *  调节饱和度
	 *  @src 源Bitmap 
	 *  @value 亮度值，范围在[-100, 100]。
	 *  @返回一个处理后的新Bitmap
	 */  
	public static Bitmap changeSaturation(Bitmap src,float value)
	{
		value = Math.max( -100, Math.min( value, 100 ) );
		
		float x = 1 + ((value > 0) ? 3 * value / 100 : value / 100);
		float lumR = 0.3086f;
		float lumG = 0.6094f;
		float lumB = 0.0820f;
		
		float[] saturation = new float[]{
				lumR*(1-x)+x, lumG*(1-x),   lumB*(1-x),   0, 0,
				lumR*(1-x),   lumG*(1-x)+x, lumB*(1-x),   0, 0,
				lumR*(1-x),   lumG*(1-x),   lumB*(1-x)+x, 0, 0,
				0,            0,            0,            1, 0,
				0,            0,            0,            0, 1
		};
		
		Bitmap returnBitmap  = Bitmap.createBitmap(src);		
		Canvas canvas = new Canvas(returnBitmap);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(saturation);
		ColorMatrixColorFilter  saturationFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(saturationFilter);	
		canvas.drawBitmap(src, 0, 0, paint);
		
		return returnBitmap;		
		
	}
	
	public static Bitmap negativeFilter(Bitmap src)
	{		
		
		float[] negative = new float[]{
				-1, 0,  0,  0, 255,
				0,  -1, 0,  0, 255,
				0,  0,  -1, 0, 255,
				0,  0,  0,  1, 1
		};
		
		Bitmap returnBitmap  = Bitmap.createBitmap(src);		
		Canvas canvas = new Canvas(returnBitmap);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(negative);
		ColorMatrixColorFilter  negativeFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(negativeFilter);	
		canvas.drawBitmap(src, 0, 0, paint);
		
		return returnBitmap;		
		
	}
	
	public static Bitmap noiseFilter(Bitmap source, int degree )
	{
		int noise = 0;
		int color = 0;
		int r = 0;
		int g = 0;
		int b = 0;
		
		Bitmap returnBitmap  = Bitmap.createBitmap(source);	
		
		for(int i = 0; i < source.getHeight(); i++ )
		{
			for( int j = 0; j < source.getWidth(); j++ )
			{
				noise = (int)( Math.random() * degree * 2 ) - degree; // 范围在 [-degree, degree]
				
				
				color = returnBitmap.getPixel( j, i );
				r = (color & 0xff0000) >> 16;
				g = (color & 0x00ff00) >> 8;
				b = color & 0x0000ff;
				
				r = r + noise < 0 ? 0 : (r + noise > 255 ? 255 : r + noise);
				g = g + noise < 0 ? 0 : (g + noise > 255 ? 255 : g + noise);
				b = b + noise < 0 ? 0 : (b + noise > 255 ? 255 : b + noise);
				returnBitmap.setPixel( j, i, r * 65536 + g * 256 + b );
			}
		}
		
		return returnBitmap;
		
	}
	
	
	public static Bitmap gray(Bitmap source )
	{
		
		/**
		 * 灰度
		 * 一个给 ColorMatrixFilter 对象作参数用的描述灰度的常数数组
		 */
		float[] GRAY_SCALE_MATRIX = new float[]{
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0,      0,      0,      1, 0
										};		
		
		
		Bitmap returnBitmap  = source.copy(Bitmap.Config.ARGB_8888, true);		
		Canvas canvas = new Canvas(returnBitmap);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(GRAY_SCALE_MATRIX);
		ColorMatrixColorFilter  grayFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(grayFilter);	
		canvas.drawBitmap(source, 0, 0, paint);		
		
	
		return returnBitmap;
		
	}
	
	public static Boolean gray(Bitmap dest,Bitmap source )
	{
		
		/**
		 *  灰度
		 *  一个给 ColorMatrixFilter 对象作参数用的描述灰度的常数数组
		 */
		float[] GRAY_SCALE_MATRIX = new float[]{
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0.3086f, 0.6094f, 0.0820f, 0, 0,
										0,      0,      0,      1, 0
										};				
		
			
		Canvas canvas = new Canvas(dest);		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);		
		ColorMatrix cm = new ColorMatrix();
		
		cm.set(GRAY_SCALE_MATRIX);
		ColorMatrixColorFilter  grayFilter  = new ColorMatrixColorFilter ( cm );
		paint.setColorFilter(grayFilter);	
		canvas.drawBitmap(source, 0, 0, paint);		
		
		return true;
		
	}
	
	
	
	public static Bitmap changeSaturationAndContrast(Bitmap dest, float s, float c)
	{
		if(null!=dest && dest.getConfig()==Config.ARGB_8888)
		{
			s = Math.max( -100, Math.min( s, 100 ));
			c = Math.max( -100, Math.min( c, 100 ));
			
			PocoNativeFilter.changeSaturationAndContrast(dest, s, c);
		}
		return dest;
	}

}
