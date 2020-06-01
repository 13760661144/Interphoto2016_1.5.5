package cn.poco.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;


public class border 
{
	
	//黑白相框
	//color : 黑 0xff000000 , 白 0xffffffff	
	public static Bitmap addInsideBorder(Bitmap srcBmp,int color)
	{
		Bitmap destBmp = srcBmp.copy(Bitmap.Config.ARGB_8888, true);
		int size = srcBmp.getWidth() > srcBmp.getHeight() ? srcBmp.getWidth() : srcBmp.getHeight();
		addInsideBorder(destBmp,color,20 * size / 750);
		return destBmp;
	}
	
	//规则相框		
	public static boolean addRegularBorder(Bitmap srcBmp,
			Bitmap ltBmp,Bitmap rtBmp,Bitmap rbBmp,Bitmap lbBmp,
			Bitmap leftBmp,Bitmap rightBmp,Bitmap topBmp,Bitmap bottomBmp,
			int cornerSize,	int hSideWidth,int hSideHeight,int vSideWidth,int vSideHeight )
	{
		//缩放素材		
		
		Canvas canvas = new Canvas();			
		
		canvas.setBitmap(srcBmp);
		canvas.drawBitmap(ltBmp, 0, 0, new Paint());
		canvas.drawBitmap(rtBmp, srcBmp.getWidth() - rtBmp.getWidth(), 0, new Paint());
		canvas.drawBitmap(rbBmp, srcBmp.getWidth() - rbBmp.getWidth(), srcBmp.getHeight() - rbBmp.getHeight(), new Paint());
		canvas.drawBitmap(lbBmp, 0, srcBmp.getHeight() - lbBmp.getHeight(), new Paint());
		
		//左边
		Paint paint = new Paint();
		Rect dr = new Rect(),sr = new Rect();
		int top,n,h;
		top = ltBmp.getHeight();
		vSideWidth = leftBmp.getHeight();
		vSideHeight = leftBmp.getHeight();
		n = (srcBmp.getHeight() - ltBmp.getHeight() - lbBmp.getHeight()) / vSideHeight;
		for(int i = 0; i < n; ++i)
		{
			dr.left = 0;		
			dr.top = top + i * vSideHeight;
			dr.right = dr.left + vSideWidth;
			dr.bottom = dr.top + vSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + vSideWidth;
			sr.bottom = sr.top + vSideHeight;
			
			canvas.drawBitmap(leftBmp, sr, dr, paint);			
			
		}
		
		h = srcBmp.getHeight() - ltBmp.getHeight() - lbBmp.getHeight() - (n) * vSideHeight;
		if(  h > 0 )
		{
			dr.left = 0;		
			dr.top = top + (n) * vSideHeight;
			dr.right = dr.left + vSideWidth;
			dr.bottom = dr.top + h;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + vSideWidth;
			sr.bottom = sr.top + h;
			
			canvas.drawBitmap(leftBmp, sr, dr, paint);
		}
		
		//右边
		top = rtBmp.getHeight();
		vSideWidth = rightBmp.getWidth();
		vSideHeight = rightBmp.getHeight();
		n = (srcBmp.getHeight() - rtBmp.getHeight()- rbBmp.getHeight()) / vSideHeight;
		for(int i = 0; i < n; ++i)
		{
			dr.left = srcBmp.getWidth() - vSideWidth;		
			dr.top = top + i * vSideHeight;
			dr.right = dr.left + vSideWidth;
			dr.bottom = dr.top + vSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + vSideWidth;
			sr.bottom = sr.top + vSideHeight;
			
			canvas.drawBitmap(rightBmp, sr, dr, paint);			
			
		}
		
		h = srcBmp.getHeight() - rtBmp.getHeight()- rbBmp.getHeight() - n * vSideHeight;
		if(  h > 0 )
		{
			dr.left = srcBmp.getWidth() - vSideWidth;		
			dr.top = top + n * vSideHeight;
			dr.right = dr.left + vSideWidth;
			dr.bottom = dr.top + h;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + vSideWidth;
			sr.bottom = sr.top + h;
			
			canvas.drawBitmap(rightBmp, sr, dr, paint);
			
		}
		
		//上边
		int left,w;
		left = ltBmp.getWidth();
		hSideWidth = topBmp.getWidth();
		hSideHeight = topBmp.getHeight();
		n = (srcBmp.getWidth() - ltBmp.getWidth() - rtBmp.getWidth()) / hSideWidth;
		for(int i = 0; i < n; ++i)
		{
			dr.left = left + i * hSideWidth;		
			dr.top = 0;
			dr.right = dr.left + hSideWidth;
			dr.bottom = dr.top + hSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + hSideWidth;
			sr.bottom = sr.top + hSideHeight;
			
			canvas.drawBitmap(topBmp, sr, dr, paint);			
			
		}
		
		w = srcBmp.getWidth() - ltBmp.getWidth() - rtBmp.getWidth() - n * hSideWidth;
		if(  w > 0 )
		{
			dr.left = left + n * hSideWidth;		
			dr.top = 0;
			dr.right = dr.left + w;
			dr.bottom = dr.top + hSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + w;
			sr.bottom = sr.top + hSideHeight;
			
			canvas.drawBitmap(topBmp, sr, dr, paint);
		}
		
		//下边
		left = lbBmp.getWidth();
		hSideWidth = bottomBmp.getWidth();
		hSideHeight = bottomBmp.getHeight();
		n = (srcBmp.getWidth() - lbBmp.getWidth() - rbBmp.getWidth()) / hSideWidth;
		for(int i = 0; i < n; ++i)
		{
			dr.left = left + i * hSideWidth;		
			dr.top = srcBmp.getHeight() - hSideHeight;
			dr.right = dr.left + hSideWidth;
			dr.bottom = dr.top + hSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + hSideWidth;
			sr.bottom = sr.top + hSideHeight;
			
			canvas.drawBitmap(bottomBmp, sr, dr, paint);			
			
		}
		
		w = srcBmp.getWidth() - lbBmp.getWidth() - rbBmp.getWidth() - n * hSideWidth;
		if(  w > 0 )
		{
			dr.left = left + n * hSideWidth;		
			dr.top = srcBmp.getHeight() - hSideHeight;
			dr.right = dr.left + w;
			dr.bottom = dr.top + hSideHeight;
			
			sr.left = 0;
			sr.top = 0;
			sr.right = sr.left + w;
			sr.bottom = sr.top + hSideHeight;
			
			canvas.drawBitmap(bottomBmp, sr, dr, paint);
		}
		
		return true;		
	}
	
	public static Bitmap addWhiteBackgroundFixedBorder(Bitmap srcBmp,Bitmap frameBmp,int frameWidth,int frameHeight)
	{
		int[] colors = 		{0xfff5f5f5,0xffffffff};
		float[] positions = {0.0f,1.0f};
		return addFixedBorder(srcBmp,frameBmp,frameWidth,frameHeight,colors,positions);
	
	}
	
	public static Bitmap addBlackBackgroundFixedBorder(Bitmap srcBmp,Bitmap frameBmp,int frameWidth,int frameHeight)
	{
		int[] colors = 		{0xff1f1b1b,0xff000000};
		float[] positions = {0.0f,1.0f};
		return addFixedBorder(srcBmp,frameBmp,frameWidth,frameHeight,colors,positions);
	
	}
	
	public static Bitmap addFixedBorder(Bitmap srcBmp,Bitmap frameBmp,int frameWidth,int frameHeight,int[] backgroundColors,float[] backgroundPositions)
	{
		int width,height;
		
		if(srcBmp.getWidth() >= srcBmp.getHeight())
		{
			width = srcBmp.getWidth();
			height = width * frameHeight / frameWidth;
			
		}
		else
		{
			height = srcBmp.getHeight();
			width = height * frameWidth / frameHeight;
		}
		
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);  
			
        Canvas canvas = new Canvas(destBmp);
        
        //画底图
        canvas.drawARGB(255, 255, 255, 255);        
        
        Paint paint = new Paint();
        
        //画原图
        Matrix  matrix = new Matrix();        
        matrix.postTranslate((destBmp.getWidth() - srcBmp.getWidth()) / 2.0f,
        		(destBmp.getHeight() - srcBmp.getHeight()) / 2.0f);        
        canvas.drawBitmap(srcBmp, matrix,paint );
        
        //画相框
        Matrix matrix2 = new Matrix();        
       
        matrix2.postScale((float)destBmp.getWidth() / frameBmp.getWidth(), 
             	(float)destBmp.getHeight() / frameBmp.getHeight());
        
        canvas.drawBitmap(frameBmp, matrix2, paint);
        
        return destBmp;
		
	}

	public static Bitmap addFixedWidthBorder(Bitmap srcBmp, Bitmap topFrame,Bitmap middleFrame,Bitmap bottonFrame,Bitmap background,int frameFixedWidth)
	{
		Bitmap destBmp = null;
		if((frameFixedWidth <= 0) )
		{
			//边框不合规格，不加边框				
			destBmp = srcBmp.copy(srcBmp.getConfig(), true);
			return destBmp;
			
		}
		
		Bitmap topFrameBmp  =  null;
		Bitmap middleFrameBmp = null;
		Bitmap bottonFrameBmp = null;
		Bitmap backgroundBmp = null;
		
		int width,height;
		
		Canvas canvas = new Canvas();
		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		
		//如果source bitmap的宽小于frameFixedWidth，按比例缩小边框的top,middle,botton部件
		if(srcBmp.getWidth() < frameFixedWidth)
		{
			width = srcBmp.getWidth();
			height = srcBmp.getHeight();
			
			int topFrameBmpHeight = width * topFrame.getHeight() / topFrame.getWidth();
			if(topFrameBmpHeight <= 0)
				topFrameBmpHeight = 1;
			topFrameBmp = Bitmap.createBitmap(width,topFrameBmpHeight, srcBmp.getConfig());
			canvas.setBitmap(topFrameBmp);
			matrix.reset();
			matrix.postScale((float)topFrameBmp.getWidth()/topFrame.getWidth(), (float)topFrameBmp.getHeight()/topFrame.getHeight());
			canvas.drawBitmap(topFrame, matrix, paint);
			
			int middleFrameBmpHeight = width * middleFrame.getHeight() / middleFrame.getWidth();
			if(middleFrameBmpHeight <= 0)
				middleFrameBmpHeight = 1;
			middleFrameBmp = Bitmap.createBitmap(width,middleFrameBmpHeight, srcBmp.getConfig());
			canvas.setBitmap(middleFrameBmp);
			matrix.reset();
			matrix.postScale((float)middleFrameBmp.getWidth()/middleFrame.getWidth(), (float)middleFrameBmp.getHeight()/middleFrame.getHeight());
			canvas.drawBitmap(middleFrame, matrix, paint);
			
			int bottonFrameBmpHeight = width * bottonFrame.getHeight() / bottonFrame.getWidth();
			if(bottonFrameBmpHeight <= 0)
				bottonFrameBmpHeight = 1;
			bottonFrameBmp = Bitmap.createBitmap(width,bottonFrameBmpHeight, srcBmp.getConfig());
			canvas.setBitmap(bottonFrameBmp);
			matrix.reset();
			matrix.postScale((float)bottonFrameBmp.getWidth()/bottonFrame.getWidth(), (float)bottonFrameBmp.getHeight()/bottonFrame.getHeight());
			canvas.drawBitmap(bottonFrame, matrix, paint);
			
			int backgroundBmpHeight = width * background.getHeight() / background.getWidth();
			if(backgroundBmpHeight <= 0)
				backgroundBmpHeight = 1;
			backgroundBmp = Bitmap.createBitmap(width,backgroundBmpHeight, srcBmp.getConfig());
			canvas.setBitmap(backgroundBmp);
			matrix.reset();
			matrix.postScale((float)backgroundBmp.getWidth()/background.getWidth(), (float)backgroundBmp.getHeight()/background.getHeight());
			canvas.drawBitmap(background, matrix, paint);
		}
		else
		{
			width = frameFixedWidth;
			height = srcBmp.getHeight();
			
			backgroundBmp = background;
			topFrameBmp  =  topFrame;
			middleFrameBmp = middleFrame;
			bottonFrameBmp = bottonFrame;
			
		}

		//
		if(topFrameBmp.getHeight() + bottonFrameBmp.getHeight() > srcBmp.getHeight())
		{
			height = topFrameBmp.getHeight() + bottonFrameBmp.getHeight();
	
		}
		
		destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);	
		canvas.setBitmap(destBmp);
		
		//画底图
		for(int y = 0; y < destBmp.getHeight(); y += backgroundBmp.getHeight())
		{
			matrix.reset();
			//matrix.postScale((float)width / backgroundBmp.getWidth(), (float)height / backgroundBmp.getHeight());
			matrix.postTranslate(0.0f, (float)y);
			canvas.drawBitmap(backgroundBmp, matrix, paint);			
		}
		
		//画原图
		matrix.reset();
		matrix.postTranslate((destBmp.getWidth() - srcBmp.getWidth()) / 2.0f, (destBmp.getHeight() - srcBmp.getHeight()) / 2.0f);
		canvas.drawBitmap(srcBmp, matrix, paint);
		
		//上边
		matrix.reset();
		canvas.drawBitmap(topFrameBmp, matrix, paint);
		
		//中部
		for(int y = topFrameBmp.getHeight(); y < destBmp.getHeight(); y += middleFrameBmp.getHeight() )
		{
			matrix.reset();
			matrix.postTranslate(0.0f,y);
			canvas.drawBitmap(middleFrameBmp, matrix, paint);
	
		}
		
		//底边
		matrix.reset();
		matrix.postTranslate(0.0f,destBmp.getHeight() - bottonFrameBmp.getHeight());
		canvas.drawBitmap(bottonFrameBmp, matrix, paint);
		
		return destBmp;
		
	}
	
	public static Bitmap addRoundRectInsideBorder(Bitmap srcBmp,int color,int vBorder,int hBorder,int angle)
    {	  		
 		Bitmap destBmp = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp  
                .getHeight(), Bitmap.Config.ARGB_8888); 	
 		
        Canvas canvas = new Canvas(destBmp);         
 
        final Paint paint = new Paint();  
        final Rect rect = new Rect(hBorder, vBorder, 
        		srcBmp.getWidth() -  hBorder, srcBmp.getHeight() -  vBorder);  
        final RectF rectF = new RectF(rect);  
   
        paint.setAntiAlias(true);  
        canvas.drawARGB(0x00, 0xff, 0xff, 0xff);  
        
        paint.setColor(0xff000000);  
        canvas.drawRoundRect(rectF, angle, angle, paint);  
   
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));         
        canvas.drawBitmap(srcBmp, 0,0, paint);  		
		
		Bitmap retBmp = Bitmap.createBitmap(srcBmp.getWidth(), srcBmp.getHeight(), Bitmap.Config.ARGB_8888);
		canvas.setBitmap(retBmp);
		
		canvas.drawColor(color);
		canvas.drawBitmap(destBmp,0 , 0, new Paint());		
    	
    	return retBmp;
    	
    }
	
	public static Bitmap addRoundRectInsideBorder(Bitmap srcBmp,int color)
	{
		int size = srcBmp.getWidth() > srcBmp.getHeight() ? srcBmp.getWidth() : srcBmp.getHeight();
		return addRoundRectInsideBorder(srcBmp,color,20 * size / 750,20 * size / 750,0);
	}
	
	public static boolean addInsideBorder(Bitmap srcBmp,int color,int size)
    {	 		
        Canvas canvas = new Canvas(srcBmp);         
 
        Paint paint = new Paint();  
        paint.setColor(color);
        paint.setAntiAlias(true);
        
        //上边
        canvas.drawRect(0, 0, srcBmp.getWidth(), size, paint);
        //下边
        canvas.drawRect(0,srcBmp.getHeight() - size,srcBmp.getWidth(),srcBmp.getHeight(),paint);
        //左边
        canvas.drawRect(0, 0, size, srcBmp.getWidth(), paint);
        //右边
        canvas.drawRect(srcBmp.getWidth() - size, 0, srcBmp.getWidth(), srcBmp.getHeight(), paint);        	
    	
    	return true;
    	
    }

}
