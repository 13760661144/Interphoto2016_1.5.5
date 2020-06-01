#include <jni.h>

typedef struct _BMPINFO
{
	unsigned char* pbytes;
	int width;
	int height;
} BMPINFO;

int max2(int a, int b)
{
	return a>b?a:b;
}

float sqrt4(float x){ 
	union{ 
		int intPart; 
		float floatPart; 
	} convertor; 
	union{ 
		int intPart; 
		float floatPart; 
	} convertor2; 
	convertor.floatPart = x; 
	convertor2.floatPart = x; 
	convertor.intPart = 0x1FBCF800 + (convertor.intPart >> 1); 
	convertor2.intPart = 0x5f3759df - (convertor2.intPart >> 1); 
	return 0.5f*(convertor.floatPart + (x * convertor2.floatPart)); 
}

unsigned char* createMaskBmp(int width, int height, int inner, int outer)
{
	int len = width*height*4;
	unsigned char *bytes = (unsigned char*)malloc(len);
	memset(bytes, 0, len);
	int y1 = outer+inner;
	int x = 0;
	int y = 0;
	int *pbytes = (int*)bytes;
	unsigned char alpha = 0;
	for (y = 0; y < height;y++)
	{
		for(x= 0; x < width;x++)
		{
			if(y < outer && y >= 0)
			{
				pbytes[y*width+x] = 255*(outer-y)/outer*0x01000000;
			}
			else if(y > y1 && y < y1+outer)
			{
				pbytes[y*width+x] = 255*(y-y1)/outer*0x01000000;
			}
		}
	}
	return bytes;
}

BMPINFO rotateBmp(int* data, int width, int height, float angle)
{
	int new_w;
	int new_h;
	int line_bytes;
	int new_line_bytes;
	int i;
	int j;
	int i0;
	int j0;
	float sina, cosa;
	float srcx1,srcy1,srcx2,srcy2,srcx3,srcy3,srcx4,srcy4;
	float dstx1,dsty1,dstx2,dsty2,dstx3,dsty3,dstx4,dsty4;
	float f1,f2;
	sina = (float) sin((double)angle);
	cosa = (float) cos((double)angle);

	BMPINFO bmpInfo;
	bmpInfo.height = 0;
	bmpInfo.width = 0;
	bmpInfo.pbytes = 0;

	// 计算原图的四个角的坐标（以图像中心为坐标系原点）
	srcx1 = (float) (- (width  - 1) / 2);
	srcy1 = (float) (  (height - 1) / 2);
	srcx2 = (float) (  (width  - 1) / 2);
	srcy2 = (float) (  (height - 1) / 2);
	srcx3 = (float) (- (width  - 1) / 2);
	srcy3 = (float) (- (height - 1) / 2);
	srcx4 = (float) (  (width  - 1) / 2);
	srcy4 = (float) (- (height - 1) / 2);

	dstx1 =  cosa * srcx1 + sina * srcy1;
	dsty1 = -sina * srcx1 + cosa * srcy1;
	dstx2 =  cosa * srcx2 + sina * srcy2;
	dsty2 = -sina * srcx2 + cosa * srcy2;
	dstx3 =  cosa * srcx3 + sina * srcy3;
	dsty3 = -sina * srcx3 + cosa * srcy3;
	dstx4 =  cosa * srcx4 + sina * srcy4;
	dsty4 = -sina * srcx4 + cosa * srcy4;

	new_w  = (int) (max2((int)fabs(dstx4 - dstx1), (int)(fabs(dstx3 - dstx2)) + 0.5));
	new_h = (int) (max2((int)fabs(dsty4 - dsty1), (int)(fabs(dsty3 - dsty2))  + 0.5));

	f1 = (float) (-0.5 * (new_w - 1) * cosa - 0.5 * (new_h - 1) * sina + 0.5 * (width  - 1));
	f2 = (float) ( 0.5 * (new_w - 1) * sina - 0.5 * (new_h - 1) * cosa + 0.5 * (height - 1));
	int* bytes_new = (int*)malloc(new_w * new_h*4);
	if (bytes_new == 0)
	{
		return bmpInfo;
	}
	int cosa1 = cosa*100000;
	int sina1 = sina*100000;
	int f1_1 = f1*100000;
	int f2_1 = f2*100000;
	for(i = 0; i < new_h; i++)
	{
		for(j = 0; j < new_w; j++)
		{
			i0 = (-j * sina1 + i * cosa1 + f2_1 + 50000)/100000;
			j0 =  ( j * cosa1 + i * sina1 + f1_1 + 50000)/100000;
			//i0 = (int) (-((float) j) * sina + ((float) i) * cosa + f2 + 0.5);
			//j0 = (int) ( ((float) j) * cosa + ((float) i) * sina + f1 + 0.5);
			if( (j0 >= 0) && (j0 < width) && (i0 >= 0) && (i0 < height))
			{
				bytes_new[new_w * (new_h - 1 - i) + j] = data[ width * (height - 1 - i0) + j0];
			}
			else
			{
				bytes_new[new_w * (new_h - 1 - i) + j] = 0xff000000;
			}
		}
	}
	bmpInfo.pbytes = (unsigned char*)bytes_new;
	bmpInfo.height = new_h;
	bmpInfo.width = new_w;
	return bmpInfo;
}

int combinBmp(int* src, int* mask, int width, int height)
{
	unsigned char* srcbytes = (unsigned char*)src;
	unsigned char* mskbytes = (unsigned char*)mask;
	int x, y;
	for(y = 0; y < height; y++)
	{
		for(x = 0; x < width; x++)
		{
			int px  = (y*width+x)*4;
			unsigned char alpha1 = mskbytes[px+3];
			srcbytes[px] = srcbytes[px]*(255-alpha1)/255+mskbytes[px]*alpha1/255;
			px++;
			srcbytes[px] = srcbytes[px]*(255-alpha1)/255+mskbytes[px]*alpha1/255;
			px++;
			srcbytes[px] = srcbytes[px]*(255-alpha1)/255+mskbytes[px]*alpha1/255;
			px++;
			srcbytes[px] = 0xff;
		}
	}
	return 1;
}

int circleMask(int* data, int width, int height, int radius1, int radius2, int cenx, int ceny)
{
	int x, y;
	int xx, yy;
	int diagonal;
	int alpha;

	for(x = 0; x < width; x++)
	{
		for(y = 0; y < height; y++)
		{
			xx = x-cenx;
			yy = y-ceny;
			diagonal = sqrt4(xx*xx+yy*yy);
			if(diagonal <= radius1)
			{
				data[x+y*width] &= 0x00ffffff;
			}
			else if(diagonal <= radius2)
			{
				alpha = 255-255*(radius2-diagonal)/(radius2-radius1);
				data[x+y*width] &= (alpha<<24) | 0x00ffffff;
			}
		}
	}
	return 1;
}

int linearMask(int* data, int width, int height, int inner, int outer, float angle, int cenx, int ceny)
{
	int diagonal = sqrt((float)(width*width+height*height));
	int mask_w = diagonal;
	int mask_h = outer*2+inner;

	int* mask_bytes = (int*)createMaskBmp(mask_w, mask_h, inner, outer);
	BMPINFO info = rotateBmp((int*)mask_bytes, mask_w, mask_h, angle);
	free(mask_bytes);
	mask_bytes = (int*)info.pbytes;
	mask_w = info.width;
	mask_h = info.height;

	int mask_top = (mask_h-height)/2+(height/2-ceny);
	int mask_left = (mask_w-width)/2+(width/2-cenx);
	int x, y,x2,y2;
	int srcpixel;
	int maskpixel;
	x2 = mask_left;
	y2 = mask_top;
	for(y = 0; y < height; y++)
	{
		for(x = 0; x < width; x++)
		{
			if(x2 >= 0 && x2 < mask_w && y2 >= 0 && y2 < mask_h)
			{
				srcpixel = y*width+x;
				maskpixel = y2*mask_w+x2;
				data[srcpixel] = data[srcpixel] & (mask_bytes[maskpixel] & 0xff000000 | 0x00ffffff);
			}
			x2++;
		}
		x2 = mask_left;
		y2++;
	}
	free(mask_bytes);
}

int gaussBlur(int *data, int width ,int height ,double sigma ,int radius)     
{  
	float *gaussMatrix, gaussSum = 0.0, _2sigma2 = 2 * sigma * sigma;     
	int x, y, xx, yy, xxx, yyy;     
	float *pdbl;
	long a, r, g, b, d;     
	unsigned char *bbb, *pout, *poutb;     
	poutb =(unsigned char*)malloc(width*height*4);     
	pout = poutb;  
	if (!pout) return 0;     
	int gaussLen = (radius * 2 + 1) * (radius * 2 + 1) ;
	gaussMatrix = (float *)malloc(gaussLen* sizeof(float));     
	pdbl = gaussMatrix;  
	if (!gaussMatrix) {     
		free(pout);     
		return 0;     
	}     

	double e;
	for (y = -radius; y <= radius; y++) {     
		for (x = -radius; x <= radius; x++) {     
			e = exp(-(double)(x * x + y * y) / _2sigma2);     
			*pdbl++ = e;     
			gaussSum += e;     
		}     
	}     

	pdbl = gaussMatrix;     
	for (y = -radius; y <= radius; y++) {     
		for (x = -radius; x <= radius; x++) {     
			*pdbl = *pdbl/gaussSum;
			pdbl++;
		}     
	}

	long* gaussMatrixLong = (long *)malloc(gaussLen * sizeof(long));    
	pdbl = gaussMatrix;     
	for (y = 0; y < gaussLen; y++)
	{
		gaussMatrixLong[y] = (*pdbl) * 1000000;
		pdbl++;
	}

	long* pdbl2;
	for (y = 0; y < height; y++) 
	{     
		for (x = 0; x < width; x++) 
		{     
			a = r = g = b = 0.0;     
			pdbl2 = gaussMatrixLong;     
			for (yy = -radius; yy <= radius; yy++) 
			{     
				yyy = y + yy;     
				if(yyy < 0) yyy = 0;
				if(yyy >= height) yyy = height-1;
				for (xx = -radius; xx <= radius; xx++) 
				{     
					xxx = x + xx;     
					if(xxx < 0) xxx = 0;
					if(xxx >= width) xxx = width-1;     
					bbb = (unsigned char *)&data[xxx + yyy * width];     
					d = *pdbl2;
					b += d * bbb[0];
					g += d * bbb[1];
					r += d * bbb[2];
					pdbl2++;
				}
			}        
			*pout++ = (unsigned char)(b/1000000);     
			*pout++ = (unsigned char)(g/1000000);     
			*pout++ = (unsigned char)(r/1000000);     
			*pout++ = 0xff;     
		}     
	}     
	memcpy(data, poutb, width * height * 4);     
	free(gaussMatrix);     
	free(gaussMatrixLong);     
	free(poutb);     
}  

jint Java_my_MicroScene_MicroSceneView_blur( JNIEnv* env, jobject thiz, jintArray pixelArray, jint width, jint height, jdouble sigma , jint radius)     
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);
	
	gaussBlur(data, width, height, sigma, radius);
	
	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	return 1;
}

jint Java_my_MicroScene_MicroSceneView_circleMask( JNIEnv* env, jobject thiz, jintArray pixelArray, jint width, jint height, jint radius1, jint radius2, jint cenx, jint ceny)
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);

	circleMask(data, width, height, radius1, radius2, cenx, ceny);

	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	return 2;
}

jint Java_my_MicroScene_MicroSceneView_linearMask( JNIEnv* env, jobject thiz, jintArray pixelArray, jint width, jint height, jint inner, jint outer, jfloat angle, jint cenx, jint ceny)
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);

	linearMask(data, width, height, inner, outer, angle, cenx, ceny);

	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	return 1;
}

jint Java_my_MicroScene_MicroSceneView_cricleBlur( JNIEnv* env, jobject thiz, jintArray pixelArray, jint width, jint height, jint radius1, jint radius2, jint cenx, jint ceny, int sigma, int radius )
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);
	
	int* mskbytes = (int*)malloc(width*height*4);
	memcpy(mskbytes, data, width * height * 4);   
	gaussBlur(mskbytes, width, height, sigma, radius) ;
	circleMask(mskbytes, width, height, radius1, radius2, cenx, ceny);
	combinBmp(data, mskbytes, width, height);
	free(mskbytes);
	
	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	return 1;
}

jint Java_my_MicroScene_MicroSceneView_linearBlur(JNIEnv* env, jobject thiz, jintArray pixelArray, jint width, jint height, jint inner, jint outer, jfloat angle, jint cenx, jint ceny, jint sigma, jint radius)
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);
	
	int* mskbytes = (int*)malloc(width*height*4);
	memcpy(mskbytes, data, width * height * 4);   
	gaussBlur(mskbytes, width, height, sigma, radius) ;
	linearMask(mskbytes, width, height, inner, outer, angle, cenx, ceny);
	combinBmp(data, mskbytes, width, height);
	free(mskbytes);
	
	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	return 1;
}
