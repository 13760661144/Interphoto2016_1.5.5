package cn.poco.image;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader.TileMode;

public class mask 
{
	
	public static Bitmap createMask(int width,int height,int bgColor)
	{
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp); 
        canvas.drawColor(bgColor);
        
        return destBmp;	
        
	}
	
	public static Bitmap createMask(int width,int height,int r,int g,int b)
	{
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp); 
        canvas.drawRGB(r, g, b);
        
        return destBmp;		
        
	}
	
	public static Bitmap createDarkCornerMask(int width,int height,int color1,int color2)
	{		
		int[] colors = {color1,color2};
		float[] positions = {0.0f,1.0f}; 
		
		return createDarkCornerMask(width,height,colors,positions);
		
	}
	
	public static Bitmap createDarkCornerMask(int width,int height,	int color1,int color2,int color3,float position1,float position2,float position3)
	{
		int[] colors = {color1,color2,color3};
		float[] positions = {position1,position2,position3}; 
		
		return createDarkCornerMask(width,height,colors,positions);
		
	}
	public static Bitmap createDarkCornerMask(int width,int height,	int color1,int color2,int color3,int color4, float position1,float position2,float position3,float position4)
	{
		int[] colors = {color1,color2,color3,color4};
		float[] positions = {position1,position2,position3,position4}; 
		
		return createDarkCornerMask(width,height,colors,positions);
		
	}
	
	public static Bitmap createDarkCornerMask(int width,int height,	int color1,int color2,int color3,int color4,int color5,	float position1,float position2,float position3,float position4,float position5)
	{
		int[] colors = {color1,color2,color3,color4,color5};
		float[] positions = {position1,position2,position3,position4,position5}; 
		
		return createDarkCornerMask(width,height,colors,positions);
	}
	
	
	public static Bitmap createDarkCornerMask(int width,int height,int[] colors,float positions[])
	{
		
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp);        
        
        int cx = width / 2;
        int cy = height / 2;
        int radius = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));       
        
        RadialGradient radialGradientShader=new RadialGradient(cx,cy, radius, colors,  positions, TileMode.CLAMP); 
        Paint shaderPaint = new Paint();    
        shaderPaint.setShader(radialGradientShader);
        shaderPaint.setAntiAlias(true);
        
        canvas.drawCircle(cx,cy,radius,shaderPaint); 
        
        return destBmp;
        
	}
	
	public static Bitmap createMagicPurpleMask(int width,int height)
	{
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp); 
        canvas.drawARGB(0, 0, 0, 0);
        
        int[] colors;
        float positions[];
        Paint paint= new Paint();
   
        colors = new int[]{0xffffffff,0x00000000,0x00000000,0xffffffff};
        positions = new float[]{0.0f,	0.4f,	  	0.6f,1.0f};
        
        LinearGradient lg = new LinearGradient(0,0,width,height, colors,positions,TileMode.CLAMP);
        paint.setShader(lg);
        canvas.drawRect(0,0,width,height, paint);       
 
        return destBmp;
		 
	}

	
}
