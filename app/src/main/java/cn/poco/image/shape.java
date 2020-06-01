package cn.poco.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class shape 
{
	public static Bitmap resize(Bitmap src,int destWidth,int destHeight)
	{
		Bitmap dest = Bitmap.createBitmap(destWidth, destHeight,src.getConfig());
		Canvas canvas = new Canvas(dest);
		
		Matrix matrix = new Matrix();		
		matrix.postScale((float)dest.getWidth() / src.getWidth(), (float)(dest.getHeight()) / src.getHeight());
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(src, matrix, paint);		
		
		return dest;		
	}
	
	public static Bitmap scale(Bitmap src, float destScale)
	{
		return resize(src, (int)(src.getWidth() * destScale), (int)(src.getHeight() * destScale));
		
	}

}
