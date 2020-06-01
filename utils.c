#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,"JniUtils",__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"JniUtils",__VA_ARGS__)

jboolean authenticated = JNI_FALSE;
JNIEXPORT jboolean JNICALL Java_cn_poco_utils_JniUtils_authenticate(JNIEnv* env, jclass obj, jobject context)
{
	if(context == NULL)
	{
		return JNI_FALSE;
	}

	jclass cls = (*env)->FindClass(env, "android/content/Context");
	if(cls == NULL)
	{
		authenticated = JNI_TRUE;
		return authenticated;
	}

	if((*env)->IsInstanceOf(env, context, cls) == JNI_FALSE)
	{
		return JNI_FALSE;
	}

	cls = (*env)->FindClass(env, "android/content/pm/ApplicationInfo");
	if(cls == NULL)
	{
		authenticated = JNI_TRUE;
		return authenticated;
	}
	
	cls = (*env)->GetObjectClass(env, context);
	if(cls != NULL)
	{
		jmethodID method = (*env)->GetMethodID(env, cls, "getApplicationInfo", "()Landroid/content/pm/ApplicationInfo;");
		if(method != NULL)
		{
			jobject info = (*env)->CallObjectMethod(env, context, method);
			if(info != NULL)
			{
				cls = (*env)->GetObjectClass(env, info);
				jfieldID field = (*env)->GetFieldID(env, cls, "packageName", "Ljava/lang/String;");
				if(field != NULL)
				{
					jstring packageName = (jstring)(*env)->GetObjectField(env, info, field);
					if(packageName != NULL)
					{
						int len = (*env)->GetStringLength(env, packageName);
						if(len > 0)
						{
							int mbslen = (len+1)*2;
							char* strbuf = (char*)malloc(mbslen);
							memset(strbuf, 0, mbslen);
							const jchar* name = (*env)->GetStringChars(env, packageName, NULL);
							int i = 0;
							for(i = 0; i < len; i++)
							{
								strbuf[i] = name[i];
							}
							(*env)->ReleaseStringChars(env, packageName, name);
							if(strcmp(strbuf, "cn.poco.BabyCamera") == 0
								|| strcmp(strbuf, "my.PCamera") == 0
								|| strcmp(strbuf, "my.beautyCamera") == 0)
							{
								authenticated = JNI_TRUE;
							}
							free(strbuf);
						}
					}
				}
			}
		}	
	}
	return authenticated;   
}

void adjust_pixels(unsigned char* data, int w, int h)
{
	int s = h-1;
	unsigned char a,r,g,b;
	int f,l,t,j;
	int x, y;
	int k = h/2;
	for(y = 0; y < k; y++)
	{
		t = y*w;
		j = (s-y)*w;
		for(x = 0; x < w; x++)
		{
			f = (t+x)*4;
			l = (j+x)*4;
			b = data[f];
			g = data[f+1];
			r = data[f+2];
			a = data[f+3];
			
			data[f] = data[l+2];
			data[f+1] = data[l+1];
			data[f+2] = data[l];
			data[f+3] = data[l+3];
			data[l] = r;
			data[l+1] = g;
			data[l+2] = b;
			data[l+3] = a;
		}
	}
	if(h%2 == 1)
	{
		t = k*w;
		for(x = 0; x < w; x++)
		{
			f = (t+x)*4;
			b = data[f];
			data[f] = data[f+2];
			data[f+2] = b;
		}
	}
}

void reversePixels(int *data, int w, int h)
{
	int s = h-1;
	int f,l,t,j;
	int x, y;
	int k = h/2;
	int temp;
	for(y = 0; y < k; y++)
	{
		t = y*w;
		j = (s-y)*w;
		for(x = 0; x < w; x++)
		{
			f = t+x;
			l = j+x;
			temp = data[f];
			data[f] = data[l];
			data[l] = temp;
		}
	}
}

void Java_cn_poco_utils_JniUtils_conversePixels(JNIEnv* env, jobject thiz, 
	jbyteArray pixelArray, jint w, jint h)
{
	if(authenticated != JNI_TRUE)
	{
		return;
	}
	unsigned char *data;
	data = (*env)->GetByteArrayElements(env, pixelArray, 0);
	adjust_pixels(data, w, h);
	(*env)->ReleaseByteArrayElements(env, pixelArray, data, 0);
}

void Java_cn_poco_utils_JniUtils_reversePixels(JNIEnv* env, jobject thiz, 
	jintArray pixelArray, jint w, jint h)
{
	if(authenticated != JNI_TRUE)
	{
		return;
	}
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);
	reversePixels(data, w, h);
	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
}

jbyteArray Java_cn_poco_utils_JniUtils_byteArrayToIntArray(JNIEnv* env, jobject thiz, jbyteArray byteArray)
{
	if(authenticated != JNI_TRUE)
	{
		return NULL;
	}
	int len = (*env)->GetArrayLength(env, byteArray);
	unsigned char *data;
	data = (*env)->GetByteArrayElements(env, byteArray, 0);
	len = len/4;
	jintArray outArray = (*env)->NewIntArray(env, len);
	(*env)->SetIntArrayRegion(env, outArray, 0, len, (jint*)data);
	(*env)->ReleaseByteArrayElements(env, byteArray, data, 0);
	return outArray;
}

JNIEXPORT jboolean JNICALL Java_cn_poco_utils_JniUtils_saveAlphaBitmap(JNIEnv * env, jobject  obj, jobject bitmap, jbyteArray file)
{
	if(authenticated != JNI_TRUE)
	{
		return JNI_FALSE;
	}
	AndroidBitmapInfo  info;
    unsigned char*     pixels;
    int                ret;
    
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGB_88888 !");
        return JNI_FALSE;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void*)&pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    
    //读取文件名
    int len = (*env)->GetArrayLength(env, file);
	char* lpstr = (char*)(*env)->GetByteArrayElements(env, file, 0);
	char* lpcsfile = (char*)malloc(len+1);
	memcpy(lpcsfile, lpstr, len);
	lpcsfile[len] = 0;
    
    //参数设置
    int		temp_int = 0;
	short	temp_short = 0;
	int		offset = 54;
	int		w = info.width;
	int		h = info.height;
	int		data_size = w*h*4;
	int		fsize = data_size+offset;
	jboolean result = JNI_FALSE;
	
	//拷贝数据
	unsigned char*   bits = (unsigned char*)malloc(data_size);
	memcpy(bits, pixels, data_size);
	AndroidBitmap_unlockPixels(env, bitmap);
	
	FILE* fp = fopen(lpcsfile, "wb");
	if(fp != NULL)
	{
		adjust_pixels(bits, w, h);
		fwrite("BM", 1, 2, fp);
		fwrite(&fsize, 4, 1, fp);
		temp_int = 0;
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&offset, 4, 1, fp);
		temp_int = 0x00000028;
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&w, 4, 1, fp);
		fwrite(&h, 4, 1, fp);
		temp_short = 1;
		fwrite(&temp_short, 2, 1, fp);
		temp_short = 32;
		fwrite(&temp_short, 2, 1, fp);
		temp_int = 0;
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&data_size, 4, 1, fp);
		temp_int = 0;
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&temp_int, 4, 1, fp);
		fwrite(&temp_int, 4, 1, fp);
		fwrite(bits, 1, data_size, fp);
		fclose(fp);
		result = JNI_TRUE;
	}
	
	free(bits);
	free(lpcsfile);
    (*env)->ReleaseByteArrayElements(env, file, lpstr, 0);
    return result;
}

JNIEXPORT jintArray JNICALL Java_cn_poco_utils_JniUtils_getAlphaArea(JNIEnv * env, jobject  obj, jobject bitmap)
{
	if(authenticated != JNI_TRUE)
	{
		return NULL;
	}
	AndroidBitmapInfo  info;
    unsigned char*     pixels;
    int                ret;
    
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return NULL;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGB_88888 !");
        return NULL;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void*)&pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return NULL;
    }
    
    jintArray rcArray = NULL;
    int w = info.width;
    int h = info.height;
    int minX = w;
	int maxX = 0;
	int minY = h;
	int maxY = 0;
	int x = 0;
	int y = 0;
	unsigned char a = 0;
	for(y = 0; y < h; y++)
	{
		for(x = 0; x < w; x++)
		{
			a = pixels[(w*y+x)*4+3];
			if(a < 200)
			{
				if(x < minX)
				{
					minX = x;
				}
				if(x > maxX)
				{
					maxX = x;
				}
				if(y < minY)
				{
					minY = y;
				}
				if(y > maxY)
				{
					maxY = y;
				}
			}
		}
	}
	if(minX < maxX && minY < maxY)
	{
		rcArray = (*env)->NewIntArray(env, 4);
		jint rc[4] = {minX, minY, maxX, maxY};
		(*env)->SetIntArrayRegion(env, rcArray, 0, 4, (jint*)rc);
	}
    AndroidBitmap_unlockPixels(env, bitmap);
    return rcArray;
}

JNIEXPORT jboolean JNICALL Java_cn_poco_utils_JniUtils_getMaskedBitmap(JNIEnv * env, jobject  obj, jobject bitmap, jobject mask)
{
	if(authenticated != JNI_TRUE)
	{
		//return JNI_FALSE;
	}
	AndroidBitmapInfo  info;
    unsigned char*     pixels;
    AndroidBitmapInfo  maskInfo;
    unsigned char*     maskPixels;
    int                ret;
    
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGB_88888 !");
        return JNI_FALSE;
    }
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void*)&pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    if ((ret = AndroidBitmap_getInfo(env, mask, &maskInfo)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    if (maskInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGB_88888 !");
        return JNI_FALSE;
    }
    if ((ret = AndroidBitmap_lockPixels(env, mask, (void*)&maskPixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return JNI_FALSE;
    }
    
    int* pixels32 = (int*)pixels;
    int w = info.width;
    int h = info.height;
	int x = 0;
	int y = 0;
	int maskW = maskInfo.width;
	int maskH = maskInfo.height;
	unsigned char* p;
	unsigned char mska;
	unsigned char srca;
	unsigned char c;
	for(y = 0; y < maskH; y++)
	{
		for(x = 0; x < maskW; x++)
		{
			mska = maskPixels[(maskW*y+x)*4+3];
			if(x < w && y < h && mska < 255)
			{
				p = &pixels[(w*y+x)*4+3];
				srca = *p;
				if(srca > mska)
				{
					if(mska == 0)
					{
						pixels32[w*y+x] = 0;
					}
					else
					{
						*p = mska;
						
						p = &pixels[(w*y+x)*4+2];
						c = *p;
						c = c*mska/srca;
						*p = c;
						
						p = &pixels[(w*y+x)*4+1];
						c = *p;
						c = c*mska/srca;
						*p = c;
						
						p = &pixels[(w*y+x)*4];
						c = *p;
						c = c*mska/srca;
						*p = c;
					}
				}
				//pixels[(w*y+x)*4+2] = 128;//maskPixels[(maskW*y+x)*4+2];
				//pixels[(w*y+x)*4+1] = 128;//maskPixels[(maskW*y+x)*4+1];
				//pixels[(w*y+x)*4+0] = 128;//maskPixels[(maskW*y+x)*4+0];
			}
		}
	}
    AndroidBitmap_unlockPixels(env, bitmap);
    AndroidBitmap_unlockPixels(env, mask);
    return JNI_TRUE;
}

JNIEXPORT jfloatArray JNICALL Java_cn_poco_utils_TianUtils_TransformPoint(JNIEnv* env, jclass obj, jfloatArray inArr, jfloat x, jfloat y)
{
	if(authenticated != JNI_TRUE)
	{
		return NULL;
	}
	jfloat* values = (*env)->GetFloatArrayElements(env, inArr, 0);

	jfloatArray outArr = (*env)->NewFloatArray(env, 2);
	jfloat* output = (*env)->GetFloatArrayElements(env, outArr, 0);

	output[0] = values[0] * x + values[1] * y + values[2];
	output[1] = values[4] * y + values[3] * x + values[5];

	(*env)->ReleaseFloatArrayElements(env, inArr, values, 0);
	(*env)->ReleaseFloatArrayElements(env, outArr, output, 0);
	return outArr;
}

JNIEXPORT jfloatArray JNICALL Java_cn_poco_utils_TianUtils_DeltaTransformPoint(JNIEnv* env, jclass obj, jfloatArray inArr, jfloat x, jfloat y)
{
	if(authenticated != JNI_TRUE)
	{
		return NULL;
	}
	jfloat* values = (*env)->GetFloatArrayElements(env, inArr, 0);

	jfloatArray outArr = (*env)->NewFloatArray(env, 2);
	jfloat* output = (*env)->GetFloatArrayElements(env, outArr, 0);

	output[0] = values[0] * x + values[1] * y;
	output[1] = values[4] * y + values[3] * x;

	(*env)->ReleaseFloatArrayElements(env, inArr, values, 0);
	(*env)->ReleaseFloatArrayElements(env, outArr, output, 0);
	return outArr;
}

float GetRectangleArea(float x1, float y1, float x2, float y2, float x3, float y3)
{
	return sqrtf((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)) * sqrt((x2 - x3) * (x2 - x3) + (y2 - y3) * (y2 - y3));
}

float GetTriangleArea(float a, float b, float c)
{
	float p = (a + b + c) / 2.0f;
	return sqrtf(p * (p - a) * (p - b) * (p - c));
}

float GetTriangleAreaEx(float x1, float y1, float x2, float y2, float x3, float y3)
{
	return GetTriangleArea(sqrtf((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)), sqrtf((x2 - x3) * (x2 - x3) + (y2 - y3) * (y2 - y3)), sqrtf((x3 - x1) * (x3 - x1) + (y3 - y1) * (y3 - y1)));
}

JNIEXPORT jboolean JNICALL Java_cn_poco_utils_TianUtils_IsSelectTarget(JNIEnv* env, jclass obj, jfloatArray inArr, jfloat w, jfloat h, jfloat x, jfloat y)
{
	if(authenticated != JNI_TRUE)
	{
		return JNI_FALSE;
	}
	jfloat* values = (*env)->GetFloatArrayElements(env, inArr, 0);

	float x1 = values[2];
	float y1 = values[5];
	float x2 = values[0] * w + values[2];
	float y2 = values[3] * w + values[5];
	float x3 = values[0] * w + values[1] * h + values[2];
	float y3 = values[4] * h + values[3] * w + values[5];
	float x4 = values[1] * h + values[2];
	float y4 = values[4] * h + values[5];

	float result = GetRectangleArea(x1, y1, x2, y2, x3, y3) - GetTriangleAreaEx(x, y, x1, y1, x2, y2) - GetTriangleAreaEx(x, y, x2, y2, x3, y3) - GetTriangleAreaEx(x, y, x3, y3, x4, y4) - GetTriangleAreaEx(x, y, x4, y4, x1, y1);

	(*env)->ReleaseFloatArrayElements(env, inArr, values, 0);
	if(fabsf(result) < 15)
	{
		return JNI_TRUE;
	}
	else
	{
		return JNI_FALSE;
	}
}

unsigned char R_V_table[256][256];
unsigned char B_U_table[256][256];
unsigned char G_U_V_table[256][256];

void yuv2rgb(JNIEnv *env, jclass jc, jint w, jint h, jint sz, jbyteArray yuv, jintArray rgb)
{
	jbyte *im_yuv;
	im_yuv = (*env)->GetByteArrayElements(env, yuv, 0);
	jint *im_rgb;
	im_rgb = (*env)->GetIntArrayElements(env, rgb, 0);

		int i = 0,y1,y2;
		int uvp,u = 0,v = 0;
		int r, g,b;	
		int r2, g2,b2;	
		int j,yp;
		int off_set;
		int h_1 = h - 1;
		int gray1, gray2;
		for(j=0,yp=0;j<h;j++)
			{		
			    off_set = (h_1 - j);
				uvp = sz + (j>>1)*w;
				for(i=0;i<w;i+=2,yp+=2)
				{
					y1=(0xff & ((short)im_yuv[yp]));
					y2=(0xff & ((short)im_yuv[yp+1]));
					if(y1<0)y1=0;
					if(y2<0)y2=0;
					
					v = (0xff & im_yuv[uvp++]);
					u = (0xff & im_yuv[uvp++]);				
					
					r = R_V_table[y1][v];
					b =  B_U_table[y1][u];	

					g = (y1 - G_U_V_table[u][v]);	
					g<0?g=0:g>255?g = 255:0;	
			
					
					r2 = R_V_table[y2][v];
					b2 = (short) B_U_table[y2][u];	

					g2 = (y2 - G_U_V_table[u][v]);		
					g2<0?g2=0:g2>255?g2 = 255:0;	 	 
							
					gray1 = r<<16 | g<<8 | b | 0xff000000;		
					gray2 = r2<<16 | g2<<8 | b2 | 0xff000000;				
																
					im_rgb[off_set] =gray1;						
					off_set+=h;				
					im_rgb[off_set] =gray2;					
					off_set+=h;										
				}
			}	
			
	(*env)->ReleaseByteArrayElements(env, yuv, im_yuv, 0);
	(*env)->ReleaseIntArrayElements(env, rgb, im_rgb, 0);
}

void init_table() 
{
	unsigned short i,j;
	for(j=0;j<256;j++)
	{
			for(i=0;i<256;i++)
			{				
				int dx = (int)(1000*j +1402 *(i-128))/1000;					
				if(dx<0)dx=0;else if(dx>255)	dx = 255;
				R_V_table[j][i] = dx;					
			}			
	}
	
	for(j=0;j<256;j++)
	{
			for(i=0;i<256;i++)
			{								
				int dx =  (int)(1000*j + 1772 *(i-128))/1000;						
				if(dx<0)dx=0;else if(dx>255)dx = 255;					
				B_U_table[j][i] = dx;					
			}
	}
	for(j=0;j<256;j++)
	{
			for(i=0;i<256;i++)
			{				
				int dx =  (int)( 344 *(j-128) + 714 *(i-128))/1000;					
				if(dx<0)dx=0;else if(dx>255)dx = 255;
				G_U_V_table[j][i] = dx;			
			}
	}	
}

int g_table_initialized = 0;
JNIEXPORT void JNICALL Java_cn_poco_utils_JniUtils_yuv2rgb(JNIEnv *env, jclass jc, jint w,jint h,jint sz, jbyteArray yuv, jintArray rgb) 
{
	if(authenticated != JNI_TRUE)
	{
		return;
	}
	if(g_table_initialized == 0)
	{
		g_table_initialized = 1;
		init_table();
	}
	yuv2rgb(env, jc, w,h,sz, yuv, rgb);			
}