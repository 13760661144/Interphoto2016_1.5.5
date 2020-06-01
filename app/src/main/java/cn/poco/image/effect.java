package cn.poco.image;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

import java.util.ArrayList;

public class effect 
{	
	//===================================================
	//冬日飘雪
	
	public static boolean addSnow(Bitmap src)
	{		
		Canvas canvas = new Canvas(src);	
		
		int width = src.getWidth();
		int height = src.getHeight();
		int size = width > height ? width : height;
		
		int x,y;
		Bitmap sfBmp;
		Paint paint = new Paint();
		
		ArrayList<Bitmap> al = new ArrayList<Bitmap>();
		
		
		int rs320[] = {5 ,			7 ,			8 ,				10 ,			12 ,
		  	  	   	   14 ,			20 ,		25,				30,				40};
		int rs480[] = {5 ,			7 ,			8 ,				10 ,			13 ,
			       	   15 ,			25 ,		30,				35,				50};
		int rs640[] = {5 ,			7 ,			9 ,				12 ,			15 ,
			       	   20 ,			25 ,		30,				40,				60};
		int rs750[] = {5 ,			7 ,			9 ,				15 ,			17 ,
			       	   20 ,			30 ,		40,				45,				70};
		int rs[];
		int rtCount,count;
		if(size < 480)
		{
			rs = rs320;
			rtCount = 3;
			count = 70;		
			
		}
		else if(size >= 480 && size < 640)
		{
			rs = rs480;
			rtCount = 4;
			count = 90;
			
		}
		else if(size >= 640 && size < 750)
		{
			rs = rs640;
			rtCount = 4;
			count = 110;
			
		}
		else
		{
			rs = rs750;
			rtCount = 5;
			count = 130;
			
		}			
		
		int colors[] ={0xdcffffff,  			0xdcffffff, 			0xdcfffff, 					0xccffffff, 				0xaaffffff,
				       0xcaffffff,  			0x99ffffff, 			0x66ffffff, 				0x55ffffff, 				0x55ffffff};
		int blurs[] = {2,						3,						4,							4,							4,
				       6,			 			6,						3,							2,							2};
		
		for(int i = 0;i < rs.length; ++i)
		{
			al.add(createSnowflake(2 * rs[i],2 * rs[i],colors[i],blurs[i]));
			
			
		}
		
		
		int is1[] = {6,7,8,9};
		int i1;
		for(int i = 0; i < rtCount; ++i)
		{
			x = (int)(Math.random() * width/3 + 2 * width / 3 );
			y = (int)(Math.random() *  height / 4);
			
			i1 = (int)(Math.random() * is1.length);
			sfBmp = al.get(is1[i1]);
			canvas.drawBitmap(sfBmp, x, y, paint);
			
		}
		
	
		int is2[] = {0,0,0,0,0,1,1,1,1,1,2,2,2,2,2,3,4,5};
		int i2;
		for(int i = 0; i < count; ++i)
		{
			x = (int)(Math.random() * width );
			y = (int)(Math.random() *  height);
			
			i2 = (int)(Math.random() * is2.length);
			sfBmp = al.get(is2[i2]);
			canvas.drawBitmap(sfBmp, x, y, paint);
			
		}
		
		al.clear();
		
		return true;		
		
	}
	
	
	private static Bitmap createSnowflake(int width,int height,
			int color,int blur)
	{
		Bitmap dest = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(dest);		
		
		Paint paint = new Paint();
		paint.setColor(color);	
		
		int wr = width / 2;
		int hr = height / 2;		
		
		int r = wr < hr ? wr : hr;
		r = (int)(0.75 * r);
		canvas.drawCircle(width / 2, height / 2, r , paint);
				
		
		PocoNativeFilter.gaussianBlurImageChannel(dest, 
				PocoImageInfo.ChannelType.AllChannels,blur);
		
		return dest;
	}

	
	
	
	//===================================================
	//炫彩
	public static boolean addDazzle(Bitmap src)
	{		
		int width = src.getWidth();
		int height = src.getHeight();
		
		//create party dazzle-------------------
		
		int size = width >= height ? width : height;		
		
		
		float[] positions = {0.0f,			0.7f,			0.85f,		1.0f};			
		
		
		int[]	color1 	  = {0xbf808080,	0xd8808080,		0xd8808080,	0x00808080};
		int[]	color2 	  = {0x99f7be06,	0xbff7be06,		0xbff7be06,	0x00f7be06};
		int[]	color3 	  = {0x99e1ee0d,	0xbfe1ee0d,		0xbfe1ee0d,	0x00e1ee0d};		
		int[]	color4 	  = {0xbf999999,	0xd8999999,		0xd8999999,	0x00999999};
		int[]	color5 	  = {0xbffeeb82,	0xd8feeb82,		0xd8feeb82,	0x00feeb82};
		int[]	color6 	  = {0x99c6d70a,	0xbfc6d70a,		0xbfc6d70a,	0x00c6d70a};
		int[]	color7 	  = {0x99d1731b,	0xbfd1731b,		0xbfd1731b,	0x00d1731b};
		
		
		ArrayList<int[]> colorsArray = new ArrayList<int[]>();
		colorsArray.add(color1);
		colorsArray.add(color2);
		colorsArray.add(color3);
		colorsArray.add(color4);
		colorsArray.add(color5);
		colorsArray.add(color6);
		colorsArray.add(color7);

		int rs[] = {25 * size / 750,			32 * size / 750,			 32 * size / 750,			45 * size / 750,			45 * size / 750,
			   	    32 * size / 750,			37 * size / 750};
		
		//System.out.println(rs[6]);

		
		ArrayList<Bitmap> al = new ArrayList<Bitmap>();
		for(int i = 0;i < rs.length; ++i)
		{			
			al.add(createDazzle(2 * rs[i],2 * rs[i],colorsArray.get(i),positions));			
		}	
		
		
		//draw party dazzle---------------------
		
		int indexs[];		
		int x,y,w,h;		
		int count;
		
		//lt		
		indexs = new int[]{0};
		x = 0;
		y = 0;
		w = width / 4;
		h = height / 8;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{1,2};
		x = 0;
		y = height / 4;
		w = width / 6;
		h = height / 4;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{1,2};
		x = width / 5;
		y = height / 5;
		w = width / 4;
		h = height / 4;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{1,2};
		x = width / 8;
		y = height / 8;
		w = width / 4;
		h = height / 4;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);

		//rt		
		indexs = new int[]{1,2,3,4};
		x = 3 * width / 5;
		y = height / 4;
		w = width / 2;;
		h = height / 2;
		count = 3;		
		drawDazzle(src,al,count,indexs,x,y,w,h);

		//lb		
		indexs = new int[]{4};
		x = 0;
		y = 1 *  height / 3;
		w = width / 10;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{3,5};
		x = width / 6;
		y = 1 *  height / 2;
		w = width / 6;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{4};
		x = 2 * width / 9;
		y = 1 *  height / 3;
		w = width / 6;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		
		indexs = new int[]{3,4,5};
		x = 0;
		y = 2 *  height / 3;
		w = width / 10;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{3,4,5};
		x = width / 6;
		y = height;
		w = width / 6;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
		
		indexs = new int[]{3,4,5};
		x = 2 * width / 9;
		y = 2 *  height / 3;
		w = width / 6;;
		h = 2 * height / 9;
		count = 1;		
		drawDazzle(src,al,count,indexs,x,y,w,h);	
		
		
		//rb
		indexs = new int[]{5,6};
		x = width / 3;
		y = 2 *  height / 3;
		w = 2 * width / 3;
		h = height / 2;
		count = 3;		
		drawDazzle(src,al,count,indexs,x,y,w,h);
	
		
		al.clear();
		colorsArray.clear();
		
		return true;
		
	}
	
	public static Bitmap createDazzle(int width,int height,
			int[] colors,float[] positions)
	{
		Bitmap dest = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(dest);
		
		int cx = width / 2;
        int cy = height / 2;
        int radius = width / 2 < height / 2 ? width / 2 : height / 2;  
        
        int xoffset;
        int yoffset;
        if(Math.random() >= 0.3)
        {
        	xoffset = (int)(Math.random() * (radius * 0.15) +  (radius * 0.1)) ;
            yoffset = (int)(Math.random() * (radius * 0.25) +  (radius * 0.1));
        	
        }
        else
        {
        	xoffset = (int)(Math.random() * (radius * 0.25) +  (radius * 0.1));
            yoffset = (int)(Math.random() * (radius * 0.15) +  (radius * 0.1)) ;
        	
        }
        
       
        RadialGradient radialGradientShader=new RadialGradient(cx + xoffset,cy + yoffset, radius,  
                                                                 colors,  
                                                                 positions,  
                                                                 TileMode.CLAMP); 
        Paint shaderPaint = new Paint();    
        shaderPaint.setShader(radialGradientShader);
        shaderPaint.setAntiAlias(true);
        
        canvas.drawCircle(cx,cy,radius,shaderPaint); 
		
		return dest;
		
	}
	
	public static boolean drawDazzle(Bitmap src,ArrayList<Bitmap> al,int count,int indexs[],int x,int y,int w,int h)
	{	
		Bitmap dazzleBmp;
		int left,top,right,bottom;
		int index;	
		int width = src.getWidth();
		int height = src.getHeight();
		
		for(int i = 0; i < count; ++i)
		{
			index = (int)(Math.random() * indexs.length);
			dazzleBmp = (Bitmap)al.get(indexs[index]);
			
			left = (int)(Math.random() * w) + x ;
			if(left < 0)
				left = 0;
			top = (int)(Math.random() *  h) + y;
			if(top < 0)
				top = 0;
			right = left + dazzleBmp.getWidth();
			if(right >= width)
			{
				right = width;
				left = right - dazzleBmp.getWidth();
			}
			bottom = top + dazzleBmp.getHeight();			
			if(bottom >= height)
			{
				bottom = height;
				top = bottom - dazzleBmp.getHeight();
			}
			
			PocoNativeFilter.compositeImageRectChannel(src, dazzleBmp,
					new Rect(left,top,right,bottom), new Rect(0,0,dazzleBmp.getWidth(),dazzleBmp.getHeight()),
					PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.ScreenCompositeOp, 255);
			
		}
		return true;
	}
	

	//==================================================================
	//美好时光
	public static boolean addMagickHour(Bitmap src)
	{		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		Bitmap hourBmp;		
		
		//左上角漏光光照效果		
		hourBmp = createMagickLight(width,height);
					
		PocoNativeFilter.compositeImageRectChannel(src, hourBmp,				
				new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()), new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.LightenCompositeOp, 255);
		
			
		//大光环组
		float[] positions = {0.0f,1.0f};
		
		
		int br1 = 20 * width / 75;
		
		int[]	color1 	  = {0xff462c9d,	0xcc462c9d};
		
		hourBmp = createMagickHour(br1,br1,color1,positions);
		
		//int bleft1 = 5 * width / 10;
		//int btop1 =  3 * height / 10;
		int bleft1 = 7 * width / 10;
		int btop1 =  5 * height / 10;
		int bright1 = bleft1 + hourBmp.getWidth();
		int bbottom1 = btop1 + hourBmp.getHeight();	
		
		if(bright1 > width)
		{
			bright1 = width;
			bleft1 = bright1 - hourBmp.getWidth();
		}
		if(bbottom1 > height)
		{
			bbottom1 = height;
			btop1 = bbottom1 - hourBmp.getHeight();
		}
		
		PocoNativeFilter.compositeImageRectChannel(src, hourBmp,
				new Rect(bleft1,btop1,bright1,bbottom1), new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.LightenCompositeOp, 255);
			
		int sr1 = 3 * br1 / 5;	
		
		int[]	color2 	  = {0xff462c9d,	0xcc462c9d};
		
		hourBmp = createMagickHour(sr1,sr1,color2,positions);		
		
		int sleft1 = bleft1 - 1 * sr1 / 3;
		int stop1 = btop1;// + 1 * sr1 / 10;
		int sright1 = sleft1 + hourBmp.getWidth();
		int sbottom1 = stop1 + hourBmp.getHeight();			
		
		PocoNativeFilter.compositeImageRectChannel(src, hourBmp,
				new Rect(sleft1,stop1,sright1,sbottom1), new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.LightenCompositeOp, 100);
		
		//小光环组		
		int br2 = 8 * width / 75;
		
		int[]	color3 	  = {0xe5543a81,	0xcc543a81};
		
		hourBmp = createMagickHour(br2,br2,color3,positions);
		
		int bleft2 = 3 * width / 10;
		int btop2 =  1 * height / 20;
		int bright2 = bleft2 + hourBmp.getWidth();
		int bbottom2 = btop2 + hourBmp.getHeight();			
		
		PocoNativeFilter.compositeImageRectChannel(src, hourBmp,
				new Rect(bleft2,btop2,bright2,bbottom2), new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.LightenCompositeOp, 255);
			
		int sr2 = 3 * br2 / 5;
		
		int[]	color4 	  = {0xcc543a81,	0xbf543a81};
		
		hourBmp = createMagickHour(sr2,sr2,color4,positions);
		
		
		int sleft2 = bleft2 - sr2 / 8;
		int stop2 = btop2 ;
		int sright2 = sleft2 + hourBmp.getWidth();
		int sbottom2 = stop2 + hourBmp.getHeight();			
		
		PocoNativeFilter.compositeImageRectChannel(src, hourBmp,
				new Rect(sleft2,stop2,sright2,sbottom2), new Rect(0,0,hourBmp.getWidth(),hourBmp.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.LightenCompositeOp, 255);
		
		
		
		return true;
	}
	
	
	public static Bitmap createMagickLight(int width,int height)
	{
		
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp);   
        
        int cx,cy,radius;        
        Paint shaderPaint = new Paint();
        
        //小光环1
        int[] haloColors = 		new int[]{0xffffffe5,		0x00000000};
        float[] haloPositions = new float[]{0.0f,				1.0f};
        cx = width / 12;
        cy = height / 17;
        radius = width / 60;       
        RadialGradient radialGradientShader = null;
        if(radius > 0)
        {
        	 radialGradientShader = new RadialGradient(cx,cy, radius,  
             		haloColors,  
             		haloPositions,  
                     TileMode.CLAMP);
             shaderPaint.setShader(radialGradientShader);
             shaderPaint.setAntiAlias(true);
             shaderPaint.setAlpha(178);
             
            //System.out.printf("cx:%d,cy:%d\n", cx,cy);
             canvas.drawCircle(cx,cy,radius,shaderPaint);
        	
        }
       
        
       	//小光环2
        haloColors = 			new int[]{0xccffffe5,		0x00000000};
        haloPositions = 		new float[]{0.0f,				1.0f};
        cx = width / 7;
        cy = (int)((width / 7.0 - width / 12.0) * (0.5 * height / width) + height / 17.0);
        radius = width / 40;  
        
        if(radius > 0)
        {
        	 radialGradientShader = 	new RadialGradient(cx,cy, radius,  
             		haloColors,  
             		haloPositions,  
                     TileMode.CLAMP);
             shaderPaint.setShader(radialGradientShader);
             shaderPaint.setAntiAlias(true);
             shaderPaint.setAlpha(230);
             
            
             //System.out.printf("cx:%d,cy:%d\n", cx,cy);
             canvas.drawCircle(cx,cy,radius,shaderPaint);
        	
        }
       
        
        
      //小光环3
        int haloColors2[] = 		{0xb2ffffe5,		0x001e1ee5};
        float haloPositions2[] = 	{0.0f,				1.0f};
        cx = width / 5;
        cy = height / 4;
        radius = width / 25;    
        
        if(radius > 0)
        {
        	 radialGradientShader = new RadialGradient(cx,cy, radius,  
             		haloColors2,  
             		haloPositions2,  
                     TileMode.CLAMP);
             shaderPaint.setShader(radialGradientShader);
             shaderPaint.setAntiAlias(true);
             shaderPaint.setAlpha(178);
             
             canvas.drawCircle(cx,cy,radius,shaderPaint);
        	
        }
   
        
        
        //漏光
        int sunColors[] = 		{0xffffffe5,	0x808080ff,		0x00000080};
        float sunPositions[] = 	{0.0f,			0.5f,			1.0f};        
        
        cx = 0;
        cy = 0;
        radius = (int)Math.sqrt((width/2) * (width/2) + (height/2) * (height/2));
       
        if(radius > 0)
        {
        	 radialGradientShader=new RadialGradient(cx - width / 50,cy + height / 60, radius,  
             		sunColors,  
             		sunPositions,  
                     TileMode.CLAMP); 
                 
             shaderPaint.setShader(radialGradientShader);
             shaderPaint.setAntiAlias(true);
             shaderPaint.setAlpha(255);
            
             
             canvas.drawCircle(cx,cy,radius,shaderPaint); 
        	
        }

        
        return destBmp;      
		
	}
	
	

	public static Bitmap createMagickHour(int width,int height,
			int[] colors,float[] positions)
	{
		Bitmap dest = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(dest);
		
		int cx = width / 2;
        int cy = height / 2;
        int radius = width / 2 < height / 2 ? width / 2 : height / 2;        
       
        //RadialGradient radialGradientShader=new RadialGradient(cx,cy, radius,  
          //                                                       colors,  
            //                                                     positions,  
              //                                                   TileMode.CLAMP); 
        LinearGradient linearGrandientShader = new LinearGradient(0,0,width,height,colors,positions,TileMode.CLAMP);
        Paint shaderPaint = new Paint();    
        shaderPaint.setShader(linearGrandientShader);
        shaderPaint.setAntiAlias(true);
        
        canvas.drawCircle(cx,cy,radius,shaderPaint); 
		
		return dest;		
	}	
	
	public static Bitmap createMagickHour(int width,int height,
			int color1,int color2,int color3,float pos1,float pos2,float pos3)
	{
		
		Bitmap destBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
        Canvas canvas = new Canvas(destBmp);   
        
        int colors[] = {color1,color2,color3};
        float positions[] = {pos1,pos2,pos3};
        
        int cx = 0;
        int cy = 0;
        int radius = (int)Math.sqrt((width) * (width) + (height) * (height));
       
        
        RadialGradient radialGradientShader=new RadialGradient(cx,cy, radius,  
                                                                 colors,  
                                                                 positions,  
                                                                 TileMode.CLAMP); 
        Paint shaderPaint = new Paint();    
        shaderPaint.setShader(radialGradientShader);
        shaderPaint.setAntiAlias(true);
        
        canvas.drawCircle(cx,cy,radius,shaderPaint);        
        
        return destBmp;
		
		
	}	
	
	
	//===========================================================
	//重口味
	
	//common
	public static boolean drawColorFever(Bitmap destBmp,int x,int y,int r,
			int[] colors,float[] positions)
	{		
				
        Canvas canvas = new Canvas(destBmp);  
        
        RadialGradient radialGradientShader=new RadialGradient(x,y, r,  
                colors,  
                positions,  
                TileMode.CLAMP); 
        
		Paint shaderPaint = new Paint();    
		shaderPaint.setShader(radialGradientShader);
		shaderPaint.setAntiAlias(true);
		
		canvas.drawCircle(x,y,r,shaderPaint);		
        
       return true;      
		
	}
	
	public static boolean drawColorFever(Bitmap destBmp,int color)
	{		
				
       Canvas canvas = new Canvas(destBmp);  
        
       canvas.drawColor(color);	
        
       return true;      
		
	}
	//red
	public static boolean addColorFeverRed(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;		
		
		
		//全局渐变	
			
		drawColorFeverRed(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();
		
				
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.ScreenCompositeOp, 178);
		
		
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 204);
			
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 20, 200, 1.0);
		
		
		
		//右边部分渐变
		int w = width;
		int h = height;
		int r;
		if(width >= height)
		{
			r = width;
		}
		else
		{
			r = height;
		}
		
		int x = 2 * w / 3;
		int y = h / 2;		
		
		int[] colors2 = 	 {0xfff5d2f5,	0xffc8a5c8};
		float[] positions2 = {0.0f,			1.0f};
		drawColorFever(colorFever,x,y,r,colors2,positions2);
		left = 0;
		right = left +  colorFever.getWidth();
		top = 0;
		bottom = top + colorFever.getHeight();
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.DarkenCompositeOp, 255);
		
		
		
		return true;
		
	}
	
	public static boolean drawColorFeverRed(Bitmap destBmp,Bitmap shader)
	{
		//long timeStart = System.currentTimeMillis();
		
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);	
		
				
		int[] colors;
		float[] positions;	
		
		colors = new int[]		{0x00505050,	0xcc505050};
		positions = new float[] {0.0f,				1.0f};
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(  width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
		
		colors = 	new int[]	{0x33f2dbdb,				0xccf55516};
		positions = new float[]	{0.0f,						1.0f};
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		
		//long timeElapse = System.currentTimeMillis() - timeStart;
		//System.out.println("drawColorFeverRed time: " + timeElapse);
		
		return true;
	}

	
	
	
	
	//yellow
	public static boolean addColorFeverYellow(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;			
		
		
		//全局渐变	
			
		drawColorFeverYellow(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();
		
				
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.ScreenCompositeOp, 153);
		
		
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 178);
			
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 20, 185, 1.0);
		
		
		//右边部分渐变
		int w = width;
		int h = height;
		int r;
		if(width >= height)
		{
			r = width;
		}
		else
		{
			r = height;
		}
		
		int x = 2 * w / 3;
		int y = h / 2;		
		
		int[] colors2 = 	 {0xfff8e6fe,	0xffd2bed8};
		float[] positions2 = {0.0f,			1.0f};
		drawColorFever(colorFever,x,y,r,colors2,positions2);
		left = 0;
		right = left +  colorFever.getWidth();
		top = 0;
		bottom = top + colorFever.getHeight();
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.DarkenCompositeOp, 255);
		
		
		
		return true;
		
	}
	
	public static boolean drawColorFeverYellow(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		
		colors = new int[]		{0x00505050,	0xcc505050};		
		positions = new float[] {0.0f,			1.0f};
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
		
		colors = 	new int[]	{0x80f2f380,				0xe5f2f380};
		//colors = 	new int[]	{0x80fffd60,				0xfffffd60};
		positions = new float[]	{0.0f,						1.0f};
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}


	//gray 老旧黑白
	public static boolean addColorFeverGray(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;		
		
		//全局渐变	
		drawColorFeverGray(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();				
	
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 255);
			
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 0, 215, 1.0);
		
		
		//右边部分渐变		
		drawColorFever(colorFever,0xffe3f7fe);
		left = 0;
		right = left +  colorFever.getWidth();
		top = 0;
		bottom = top + colorFever.getHeight();
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.DarkenCompositeOp, 255);
		
		
		
		return true;
		
	}
	
	public static boolean drawColorFeverGray(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		
		colors = new int[]		{0xb2161637,	0xcc161637};		
		positions = new float[] {0.0f,			1.0f};
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
		
		colors = 	new int[]	{0xccdcf4fe,				0xccdcf4fe};
		positions = new float[]	{0.0f,						1.0f};
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
	
	//green
//	public static boolean addColorFeverGreen(Bitmap src,Bitmap shader)
	//{
		//int width = src.getWidth();
		//int height = src.getHeight();
		
	
		
		//Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);			
		//drawColorFeverGreen(colorFever,shader);		
		//PocoNativeFilter.composite(src, colorFever,5);		
		
		//PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 30, 215, 1.0);
		
	
		//return true;
		
	//}
	
	public static boolean drawColorFeverGreen(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		
		colors = 	new int[]	{0xffd9f8b4,				0xff204154};	
		positions = new float[]	{0.0f,						1.0f};
		
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);		
		
		
		colors = new int[]		{0xbfd9f8b4,	0xe5d9f8b4};		
		positions = new float[] {0.0f,			1.0f};
		
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
	
/*	
	//yellow2  胶卷过曝
	public static boolean addColorFeverYellow2(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		int left,right,top,bottom;
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();		
	
		drawColorFeverYellow2(colorFever,shader);		
		PocoNativeFilter.composite(src, colorFever, 4);
	
		colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		drawColorFeverYellow2b(colorFever,shader);
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
			new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
			PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 255);
				
	
		
		return true;
		
	}
*/	
	public static boolean drawColorFeverYellow2(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		
		colors = new int[]		{0xfff7fbfe,		0xfff7fbfe};		
		positions = new float[] {0.0f,				1.0f};
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
		
		colors = 	new int[]	{0xfff7fbfe,				0xfffaef86};
		positions = new float[]	{0.0f,						1.0f};
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
	
	
	public static boolean drawColorFeverYellow2b(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		
		colors = new int[]		{0x0000000,		0xff000000};		
		positions = new float[] {0.0f,				1.0f};
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
		
		colors = 	new int[]	{0x00faef86,				0xfffaef86};
		positions = new float[]	{0.0f,						1.0f};
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
	
/*	
	//red2 渲染溢光
	public static boolean addColorFeverRed2(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;	
		
		
		
		//全局渐变	
			
		drawColorFeverRed2(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();	
		
		PocoNativeFilter.composite(src, colorFever,3);
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 255);
	

			
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 30, 255, 1.0);
	
		
	
		return true;
		
	}
*/	
	public static boolean drawColorFeverRed2(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		colors = 	new int[]	{0x80f2acac,				0xcc161a1a};	
		positions = new float[]	{0.0f,						1.0f};
		
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);		
		
		colors = new int[]		{0x80f2acac,	0xccf2acac};		
		positions = new float[] {0.0f,			1.0f};		
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
/*	
	//red3 艳丽反转
	public static boolean addColorFeverRed3(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;	
		
		
		
		//全局渐变	
			
		drawColorFeverRed3(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();	
		
		PocoNativeFilter.composite(src, colorFever,3);
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 255);
	
			
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 30, 235, 1.0);
	
		
	
		return true;
		
	}
*/	
	public static boolean drawColorFeverRed3(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		colors = new int[]		{0x80f2acac,	0xccf2acac};		
		positions = new float[] {0.0f,			1.0f};
		
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);		
		
				
		colors = 	new int[]	{0x80f2acac,				0xcc161a1a};	
		positions = new float[]	{0.0f,						1.0f};
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
/*	
	//green2  青色映画	
	public static boolean addColorFeverGreen2(Bitmap src,Bitmap shader)
	{
		
		int width = src.getWidth();
		int height = src.getHeight();		
		
		
		Bitmap colorFever = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;		
		
		//全局渐变	
			
		drawColorFeverGreen2(colorFever,shader);
		
		
		left = 0;
		right = left + colorFever.getWidth();
		bottom = height;
		top =  bottom - colorFever.getHeight();	
		
		PocoNativeFilter.composite(src, colorFever,1);
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.OverlayCompositeOp, 255);
	
			
		drawColorFever(colorFever,0xfff4fcfe);
		
		PocoNativeFilter.compositeImageRectChannel(src, colorFever,				
				new Rect(left,top,right,bottom), new Rect(0,0,colorFever.getWidth(),colorFever.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.DarkenCompositeOp, 178);
			
		
		PocoNativeFilter.LevelImageChannel(src, PocoImageInfo.ChannelType.AllChannels, 30, 255, 1.0);
		
	
		return true;
		
	}
*/	
	public static boolean drawColorFeverGreen2(Bitmap destBmp,Bitmap shader)
	{
		Canvas canvas = new Canvas(destBmp);		
		
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		Paint paint = new Paint(); 
		paint.setAntiAlias(true);		
		
		int[] colors;
		float[] positions;	
		
		colors = new int[]		{0xbfd9f8b4,	0xe5d9f8b4};		
		positions = new float[] {0.0f,			1.0f};
		
		
		
		int r = (int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		RadialGradient radialGradient = new RadialGradient(   width / 2,height / 2, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		paint.setShader(radialGradient);
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
	
				
		BitmapShader bmpShader = new BitmapShader(shader,TileMode.CLAMP,TileMode.CLAMP);
		
			
		
		
		colors = 	new int[]	{0x80b7c0ad,				0xff071c15};	
		positions = new float[]	{0.0f,						1.0f};
		
		
		
		r =(int)Math.sqrt((width / 2) * (width / 2) + (height / 2) * (height / 2));
		
		radialGradient = new RadialGradient(width / 2,height / 2, (int)(1.0 * r),  
                colors,  
                positions,  
                TileMode.CLAMP);		
		
		
		ComposeShader composeShader  = new ComposeShader(bmpShader,radialGradient,PorterDuff.Mode.SRC_IN);

		paint.setShader(composeShader);
		
		canvas.drawRect(0, 0, destBmp.getWidth(), destBmp.getHeight(), paint);
		return true;
	}
	
	
	public static boolean addSunset(Bitmap destBmp)
	{
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();		
		
		
		
		Bitmap sunset = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 		
			
		int left,right,top,bottom;		
		
		left = 0;
		right = left + sunset.getWidth();
		bottom = height;
		top =  bottom - sunset.getHeight();	
		
		drawSunset(sunset);
		
		PocoNativeFilter.composite(destBmp, sunset,5);
		
		PocoNativeFilter.compositeImageRectChannel(destBmp, sunset,				
				new Rect(left,top,right,bottom), new Rect(0,0,sunset.getWidth(),sunset.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.MultiplyCompositeOp, 128);
		
		PocoNativeFilter.LevelImageChannel(destBmp, PocoImageInfo.ChannelType.AllChannels, 30, 195, 1.0);
		
		sunset = Bitmap.createBitmap(2 * width/ 3, 2 * height / 3, Bitmap.Config.ARGB_8888); 
		drawSunset2(sunset);
		
		left = 0;
		right = left + sunset.getWidth();
		top =  0;	
		bottom = top + sunset.getHeight();
		
		
		PocoNativeFilter.compositeImageRectChannel(destBmp, sunset,				
				new Rect(left,top,right,bottom), new Rect(0,0,sunset.getWidth(),sunset.getHeight()),
				PocoImageInfo.ChannelType.AllChannels, PocoCompositeOperator.ScreenCompositeOp, 255);
	
		
		
		return true;		
	}
	
	public static boolean drawSunset(Bitmap destBmp)
	{
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		int[] colors = 	new int[]		{0xfffcf7b3,	0xffe3ce8d,		0xff4f1a31};	
		float[] positions = new float[]	{0.0f,			0.3f,			1.0f};
		
		
		
		int r =(int)Math.sqrt((3 * width / 5) * (3 * width / 5) + (3 * height / 5) * (3 * height / 5));
		
		
		RadialGradient radialGradient = new RadialGradient(2 * width / 5,2 * height / 5, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		
		Paint paint = new Paint();
		paint.setShader(radialGradient);
		Canvas canvas = new Canvas(destBmp);
		canvas.drawRect(new Rect(0,0,width,height), paint);
		
		return true;		
	}
	
	
	public static boolean drawSunset2(Bitmap destBmp)
	{
		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		
		int[] colors = 	new int[]		{0xffead9bb,	0xccdab27e,		0x00dab27e};	
		float[] positions = new float[]	{0.0f,			0.3f,			1.0f};		
		
		
		int r = width < height ? width : height;		
		
		RadialGradient radialGradient = new RadialGradient(-r/15,r / 5, r,  
                colors,  
                positions,  
                TileMode.CLAMP);
		
		Paint paint = new Paint();
		paint.setShader(radialGradient);
		Canvas canvas = new Canvas(destBmp);
		canvas.drawRect(new Rect(0,0,width,height), paint);
		
		return true;		
	}


}
