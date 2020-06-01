#include <jni.h>
#include <stdlib.h>

#define bool int
#define false 0
#define true 1


//*****************************************************************************************
//LZW压缩算法
const int bit_masks[] = {0x0000,0x0001,0x0003,0x0007,0x000F,0x001F,0x003F,0x007F,0x00FF,
						0x01FF,0x03FF,0x07FF,0x0FFF,0x1FFF,0x3FFF,0x7FFF,0xFFFF};

#define MAXCODE(n_bits) ((1 << (n_bits)) - 1)

#define flush_char() \
{ \
	if (a_count > 0) { \
		outdata[count++] = a_count; \
		for(k = 0; k < a_count; k++) \
		{ \
			outdata[count++] = accum[k]; \
		} \
		a_count = 0; \
	} \
}

#define char_out(c) \
{ \
	accum[a_count++] = c; \
	if (a_count >= 254) \
		flush_char() \
}

#define output(code) \
{ \
	cur_accum &= bit_masks[cur_bits]; \
	if(cur_bits > 0) \
		cur_accum |= (code << cur_bits); \
	else \
		cur_accum = code; \
	cur_bits += n_bits; \
	while (cur_bits >= 8) { \
		char_out(cur_accum&0xff) \
		cur_accum >>= 8; \
		cur_bits -= 8; \
	} \
	if (free_ent > maxcode || clear_flg) { \
		if (clear_flg) { \
			n_bits = init_bits; \
			maxcode = MAXCODE(n_bits); \
			clear_flg = false; \
		} else { \
			++n_bits; \
			if (n_bits == maxbits) \
				maxcode = maxmaxcode; \
			else \
				maxcode = MAXCODE(n_bits); \
		} \
	} \
	if (code == EOFCODE) { \
		while (cur_bits > 0) { \
			char_out(cur_accum&0xff) \
			cur_accum >>= 8; \
			cur_bits -= 8; \
		} \
		flush_char() \
	} \
}

unsigned char* lzwcompress(unsigned char* data, int* plen)
{
	char accum[256];
	int a_count = 0;
	int cur_bits = 0;
	int cur_accum = 0;
	int count = 0;
	int i, j, k;
	int hsize = 5003;
	int fcode;
	int init_bits = 9;
	int n_bits = init_bits;
	int len = *plen;
	int* htab = (int*)malloc(hsize*sizeof(int));
	int* ctab = (int*)malloc(hsize*sizeof(int));
	int buffer_size = len;
	unsigned char* outdata = (unsigned char*)malloc(buffer_size);
	int CLEAR = 1<<(n_bits-1);
	int EOFCODE = CLEAR+1;
	int ent = 0;
	int free_ent = EOFCODE+1;
	int hshift = 0;
	int maxbits = 12;
	int maxcode = MAXCODE(n_bits);
	int maxmaxcode = 1 << maxbits;
	int c = 0;
	int disp = 0;
	bool clear_flg = false;

	for (fcode = hsize; fcode < 65536; fcode *= 2)
		++hshift;
	hshift = 8 - hshift;

	for(i = 0; i < hsize; i++)
	{
		htab[i] = -1;
		ctab[i] = 0;
	}

	output(CLEAR)

	ent = data[0];
	bool ok = false;
	for(j = 1; j < len; j++)
	{
		c = data[j];
		fcode = (c << maxbits) + ent;
		i = (c << hshift) ^ ent;

		if(htab[i] == fcode) 
		{
			ent = ctab[i];
			continue;
		} 
		else if(htab[i] >= 0)
		{
			disp = hsize - i;
			if (i == 0)
				disp = 1;
			ok = false;
			do 
			{
				if((i -= disp) < 0)
				{
					i += hsize;
				}
				if(htab[i] == fcode) 
				{
					ent = ctab[i];
					ok = true;
					break;
				}
			}while(htab[i] >= 0);
			if(ok == true)
			{
				continue;
			}
		}
		output(ent)
		ent = c;
		if (free_ent < maxmaxcode) 
		{
			ctab[i] = free_ent++;
			htab[i] = fcode;
		} 
		else
		{
			for(i = 0; i < hsize; i++)
			{
				htab[i] = -1;
			}
			free_ent = CLEAR + 2;
			clear_flg = true;
			output(CLEAR)
		}
	}
	output(ent)
	output(EOFCODE)
	free(htab);
	free(ctab);
	*plen = count;
	return outdata;
}
//LZW压缩算法
//*****************************************************************************************


const unsigned char color16[] = {0, 0, 0,
0, 0, 128,
0, 128, 0,
0, 128, 128,
128, 0, 0,
128, 0, 128,
128, 128, 0,
192, 192, 192,
128, 128, 128,
0, 0, 255,
0, 255, 0,
0, 255, 255,
255, 0, 0,
255, 0, 255,
255, 255, 0,
255, 255, 255};

#define block_size 1024
#define write(psrc, len) \
	if(writed_size+len > buffer_size) \
	{ \
		buffer_size = writed_size+(len/block_size+1)*block_size; \
		gifdata = (unsigned char*)realloc(gifdata, buffer_size); \
	} \
	writed_size += write_bytes(gifdata+writed_size, psrc, len)

#define write_byte(onebyte) \
	if(writed_size+1 > buffer_size) \
	{ \
		buffer_size = writed_size+block_size; \
		gifdata = (unsigned char*)realloc(gifdata, buffer_size); \
	} \
	gifdata[writed_size++] = onebyte

#define write_short(twobyte) \
	if(writed_size+2 > buffer_size) \
	{ \
		buffer_size = writed_size+block_size; \
		gifdata = (unsigned char*)realloc(gifdata, buffer_size); \
	} \
	*((short*)(gifdata+writed_size)) = twobyte; \
	writed_size+=2

int write_bytes(unsigned char* pdst, const char* psrc, int len)
{
	if(len == 1)
	{
		*pdst = *psrc;
	}
	else if(len > 100)
	{
		memcpy(pdst, psrc, len);
	}
	else 
	{
		int i = 0;
		for(i = 0; i < len; i++)
		{
			pdst[i] = psrc[i];
		}
	}
	return len;
}

//*****************************************************************************************
//src:			图像像素数据
//bpp:		图像像素数据的格式 RGB->3, ARGB->4
//width:		图片宽度
//height:		图片高度
//gif_width:	生成的gif宽度
//gif_height:	生成的gif高度
//transcolor:	透明色
//trans:		是否需要透明
//delay:		延迟时间
//repeat:		播放循环次数
//is_first_frame:	是否是第一帧
//finish:		是否是最后一帧
//plen:			用于返回生成后的帧数据流的大小
unsigned char* makeframe(unsigned char* src, int bpp, short x, short y, int width, int height, int gif_width, int gif_height, int transcolor, bool trans, int delay, int repeat, bool is_first_frame, bool finish, int* plen)
{
	int i, j, k, l, index;
	int tr, tg, tb;
	if(bpp > 4) bpp = 4;
	if(bpp < 3) bpp = 3;

	//*****************************************************************************************
	//创建调色板和图象数据
	int color_tab_size = 256*3;
	int color = 16;
	int divisor = 256/color;
	int color2 = color*color;
	int color_num = color*color*color;
	int palette_count = 0;
	int pixel_num = width*height;

	int* map_color_index = (int*)malloc(color_num*sizeof(int));
	memset(map_color_index, 0xff, color_num*sizeof(int));
	int* color_tab = (int*)malloc(color_num*sizeof(int));
	memset(color_tab, 0, color_num*sizeof(int));
	int* color_tab_r_add = (int*)malloc(color_num*sizeof(int));
	memset(color_tab_r_add, 0, color_num*sizeof(int));
	int* color_tab_g_add = (int*)malloc(color_num*sizeof(int));
	memset(color_tab_g_add, 0, color_num*sizeof(int));
	int* color_tab_b_add = (int*)malloc(color_num*sizeof(int));
	memset(color_tab_b_add, 0, color_num*sizeof(int));
	unsigned char* color_palette = (unsigned char*)malloc(color_tab_size);
	memset(color_palette, 0, color_tab_size);
	unsigned char r;
	unsigned char g;
	unsigned char b;
	unsigned char _r;
	unsigned char _g;
	unsigned char _b;
	i = 0;
	unsigned char *p = (unsigned char*)src;
	while(i < pixel_num)
	{
		b = *p;
		g = *(p+1);
		r = *(p+2);
		_r = r/divisor;
		_g = g/divisor;
		_b = b/divisor;
		j = _r*color2+_g*color+_b;
		color_tab_r_add[j] += r;
		color_tab_g_add[j] += g;
		color_tab_b_add[j] += b;
		color_tab[j]++;
		p += bpp;
		i++;
	}
#define  COLOR2 6
#define  COLOR_DIVISOR 3
	int color_tab256[COLOR2][COLOR2][COLOR2];
	int color_tab256r_add[COLOR2][COLOR2][COLOR2];
	int color_tab256g_add[COLOR2][COLOR2][COLOR2];
	int color_tab256b_add[COLOR2][COLOR2][COLOR2];
	memset(color_tab256, 0, COLOR2*COLOR2*COLOR2*sizeof(int));
	memset(color_tab256r_add, 0, COLOR2*COLOR2*COLOR2*sizeof(int));
	memset(color_tab256g_add, 0, COLOR2*COLOR2*COLOR2*sizeof(int));
	memset(color_tab256b_add, 0, COLOR2*COLOR2*COLOR2*sizeof(int));
	i = 0;
	while(i < color_num)
	{
		if(color_tab[i] > 0)
		{
			int count = color_tab[i];
			_r = color_tab_r_add[i]/count;
			_g = color_tab_g_add[i]/count;
			_b = color_tab_b_add[i]/count;
			r = _r/color/COLOR_DIVISOR;
			g = _g/color/COLOR_DIVISOR;
			b = _b/color/COLOR_DIVISOR;
			color_tab256r_add[r][g][b] += _r;
			color_tab256g_add[r][g][b] += _g;
			color_tab256b_add[r][g][b] += _b;
			color_tab256[r][g][b]++;
		}
		i++;
	}

	bool whl = true;
	int min = pixel_num/10;
	for(i = 0; i < COLOR2; i++)
	{
		for(j = 0; j < COLOR2; j++)
		{
			for(k = 0; k < COLOR2; k++)
			{
				if(color_tab256[i][j][k] > 0)
				{
					int count = color_tab256[i][j][k];
					r = color_tab256r_add[i][j][k]/count;
					g = color_tab256g_add[i][j][k]/count;
					b = color_tab256b_add[i][j][k]/count;
					index = r/divisor*color2+g/divisor*color+b/divisor;
					map_color_index[index] = palette_count;
					color_palette[palette_count*3] = r;
					color_palette[palette_count*3+1] = g;
					color_palette[palette_count*3+2] = b;
					palette_count++;
				}
			}
		}
	}
	int color_count = 0;
	whl = true;
	min = pixel_num/256;
	int pick_num = 0;
	while(whl)
	{
		for(i = 0; i < color; i++)
		{
			for(j = 0; j < color; j++)
			{
				for(k = 0; k < color; k++)
				{
					index = i*color2+j*color+k;
					if(color_tab[index] > min && map_color_index[index] == -1)
					{
						map_color_index[index] = palette_count;
						int count = color_tab[index];
						r = color_tab_r_add[index]/count;
						g = color_tab_g_add[index]/count;
						b = color_tab_b_add[index]/count;
						color_palette[palette_count*3] = r;
						color_palette[palette_count*3+1] = g;
						color_palette[palette_count*3+2] = b;
						palette_count++;
						if(palette_count >= 256)
						{
							whl = false;
							break;
						}
					}
				}
				if(whl == false)
				{
					break;
				}
			}
			if(whl == false)
			{
				break;
			}
		}
		if(min == 0)
		{
			break;
		}
		min /= 2;
	}
	free(color_tab_r_add);
	color_tab_r_add = NULL;
	free(color_tab_g_add);
	color_tab_g_add = NULL;
	free(color_tab_b_add);
	color_tab_b_add = NULL;

	int offset;
	int error = 0;
	int _x = 0;
	int _y = 0; 
	unsigned char* pixel;

#define plus_truncate_uchar(a, c) \
	if (((int)(a)) + (c) < 0) \
	(a) = 0; \
	else if (((int)(a)) + (c) > 255) \
	(a) = 255; \
	else \
	(a) += (c);

#define compute_disperse() \
	error = (int)r - (int)color_palette[index*3]; \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+_y*width);\
	pixel += 2; \
	plus_truncate_uchar(*pixel, (error*4) >> 4); \
	} \
	if (_y + 1 < height) { \
	if (_x - 1 > 0) { \
	pixel = (unsigned char*)(pixels+(_x-1)+(_y+1)*width);\
	pixel += 2; \
	plus_truncate_uchar(*pixel, (error*2) >> 4); \
	} \
	pixel = (unsigned char*)(pixels+(_x+0)+(_y+1)*width);\
	pixel += 2; \
	plus_truncate_uchar(*pixel, (error*3) >> 4); \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+(_y+1)*width);\
	pixel += 2; \
	plus_truncate_uchar(*pixel, (error*1) >> 4); \
	} \
	} \
	error = (int)g - (int)color_palette[index*3+1]; \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+_y*width);\
	pixel += 1; \
	plus_truncate_uchar(*pixel, (error*4) >> 4); \
	} \
	if (_y + 1 < height) { \
	if (_x - 1 > 0) { \
	pixel = (unsigned char*)(pixels+(_x-1)+(_y+1)*width);\
	pixel += 1; \
	plus_truncate_uchar(*pixel, (error*2) >> 4); \
	} \
	pixel = (unsigned char*)(pixels+(_x+0)+(_y+1)*width);\
	pixel += 1; \
	plus_truncate_uchar(*pixel, (error*3) >> 4); \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+(_y+1)*width);\
	pixel += 1; \
	plus_truncate_uchar(*pixel, (error*1) >> 4); \
	} \
	} \
	error = (int)b - (int)color_palette[index*3+2]; \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+_y*width);\
	plus_truncate_uchar(*pixel, (error*4) >> 4); \
	} \
	if (_y + 1 < height) { \
	if (_x - 1 > 0) { \
	pixel = (unsigned char*)(pixels+(_x-1)+(_y+1)*width);\
	plus_truncate_uchar(*pixel, (error*2) >> 4); \
	} \
	pixel = (unsigned char*)(pixels+(_x+0)+(_y+1)*width);\
	plus_truncate_uchar(*pixel, (error*3) >> 4); \
	if (_x + 1 < width) { \
	pixel = (unsigned char*)(pixels+(_x+1)+(_y+1)*width);\
	plus_truncate_uchar(*pixel, (error*1) >> 4); \
	} \
	}

	free(color_tab);
	color_tab = NULL;
	free(map_color_index);
	map_color_index = NULL;

	map_color_index = (int*)malloc(sizeof(int)*0x8000);
	memset(map_color_index, 0xff, sizeof(int)*0x8000);

	unsigned char* indexed_color = (unsigned char*)malloc(pixel_num);
	p = (unsigned char*)src;
	int* pixels = (int*)src;
	for(_y = 0; _y < height; _y++)
	{
		for(_x = 0; _x < width; _x++)
		{
			k = _y*width+_x;
			b = *p;
			g = *(p+1);
			r = *(p+2);
			i = r/8*32*32+g/8*32+b/8;
			index = map_color_index[i];
			if(index == -1)
			{
				min = 0x00ffffff;
				for(l = 0; l < palette_count; l++)
				{
					_r = color_palette[l*3];
					_g = color_palette[l*3+1];
					_b = color_palette[l*3+2];
					offset =  (b-_b)*(b-_b);
					offset += (g-_g)*(g-_g);
					offset += (r-_r)*(r-_r);
					if(offset < min)
					{
						min = offset;
						index = l;
					}
				}
				map_color_index[i] = index;
				//index = 0;
			}
			indexed_color[k] = index;
			compute_disperse()
			p += bpp;
		}
	}
	free(map_color_index);
	map_color_index = NULL;

	int imgdata_size = pixel_num;
	unsigned char* imgdata = lzwcompress(indexed_color, &imgdata_size);
	free(indexed_color);
	indexed_color = NULL;
	//*****************************************************************************************

	int writed_size = 0;
	int buffer_size = 1024;
	unsigned char* gifdata = (unsigned char*)malloc(buffer_size);

	if(is_first_frame == true)
	{
		//写GIF文件头
		write("GIF89a", 6);
		//写逻辑屏幕标示符
		write_short(gif_width);  //GIF图片的宽度
		write_short(gif_height); //GIF图片的高度
		write_byte(0x80|0x70|0x00|7); //1:是否有全局颜色列表, 2-4:颜色深度, 5:是否全局颜色列表分类排列, 6-8:全局颜色列表大小(x+1)
		write_byte(0); //背景色
		write_byte(0); //像素宽高比
		//写全局调色板
		write((char*)color_palette, color_tab_size);
		//
		if(repeat >= 0)
		{
			write_byte(0x21); //扩展块标示符
			write_byte(0xff); //块类型标志
			write_byte(11); //块大小
			write("NETSCAPE2.0", strlen("NETSCAPE2.0")); // app id + auth code
			write_byte(3); //子块大小
			write_byte(1); // loop sub-block id
			write_short(repeat); //循环次数，0代表无限循环
			write_byte(0); //块终止
		}
	}

	//图形控制扩展块
	write_byte(0x21); //扩展块标示符
	write_byte(0xf9); //块类型标志
	write_byte(4); //块大小
	int transp, disp, trans_index = 0;
	if (!trans == true) {
		transp = 0;
		disp = 0; 
	} else {
		transp = 1;
		disp = 2;
		r = transcolor>>16&0x00ff;
		g = transcolor>>8&0x00ff;
		b = transcolor&0x00ff;
		trans_index = 0;
		min = 0x00ffffff;
		for(l = 0; l < palette_count; l++)
		{
			_r = color_palette[l*3];
			_g = color_palette[l*3+1];
			_b = color_palette[l*3+2];
			offset =  _b>b?_b-b:b-_b;
			offset += _g>g?_g-g:g-_g;
			offset += _r>r?_r-r:r-_r;
			if(offset < min)
			{
				min = offset;
				trans_index = l;
			}
		}
	}
	disp <<= 2;
	write_byte(0|disp|0|transp); //1-3:保留, 4-6:处置方法, 7:用户输入标志, 8:是否使用透明色
	write_short(delay/10); //延迟时间1/100秒
	write_byte(trans_index); //透明色索引
	write_byte(0); //块终止

	//图像描述符
	write_byte(0x2c); //图像描述符开始标志
	write_short(x); //图像x位置
	write_short(y);	//图像y位置
	write_short(width); //图像宽度
	write_short(height); //图像高度
	if (is_first_frame == true) 
	{
		write_byte(0); //不使用局部色表
	} 
	else 
	{
		write_byte(0x80|0|0|0|7); // 1:是否使用局部色表, 2:是否图像数据使用交织排列, 3:是否色表使用分类排列, 4-5:保留, 6-8:色表大小(x+1)
	}

	//局部色表
	if(is_first_frame == false)
	{
		write((char*)color_palette, color_tab_size);
	}

	free(color_palette);
	color_palette = NULL;

	//图像数据
	write_byte(8); //LZW初始码表大小的位数
	write((char*)imgdata, imgdata_size);
	write_byte(0); //块终止

	//gif结束符
	if(finish == true)
	{
		write_byte(0x3b);
	}

	free(imgdata);
	imgdata = NULL;
	*plen = writed_size;
	return gifdata;
}

//不支持大于255的字符
//char* jchar2str(jchar* str, char* buf)
//{
//	if(str == NULL) return -1;
//	int i = 0;
//	while(str[i] != 0)
//	{
//		buf[i] = str[i];
//		i++;
//	}
//	buf[i] = 0;
//	return buf;
//}
//
//int getParamInt(char* str, const char* param)
//{
//	if(str == NULL || param == NULL)
//		return -1;
//	int len = strlen(param);
//	char search[256];
//	strcpy(search, param);
//	strcat(search, ":");
//	char* p = strstr(str, search);
//	if(p != NULL)
//	{
//		p += len+1;
//		char *pstr = strstr(p, ";");
//		if(pstr == NULL && strstr(p, ":") == NULL)
//		{
//			//if(p-str >= strlen(str)-2) 
//				//return -1;
//			return atoi(p);
//		}
//		len = pstr-p;
//		if(pstr != NULL && len > 0)
//		{
//			memcpy(search, p, len);
//			return atoi(search);
//		}
//	}
//	return -1;
//}

jbyteArray Java_my_Gif_GifEncoder_makeGifFrame(JNIEnv* env, jobject thiz, 
	jintArray pixelArray, jshort x, jshort y, jint width, jint height, 
	jint gifWidth, jint gifHeight, jint transColor, 
	jboolean trans, jint delay, jint repeat, 
	jboolean isFirstFrame, jboolean isEndFrame)
{
	int *data;
	data = (*env)->GetIntArrayElements(env, pixelArray, 0);

	int len = 0;
	unsigned char* bytes = makeframe((unsigned char*)data, 4, x, y, width, height, gifWidth, gifHeight, transColor, trans, delay, repeat, isFirstFrame, isEndFrame, &len);
	(*env)->ReleaseIntArrayElements(env, pixelArray, data, 0);
	
	jbyteArray outArray = (*env)->NewByteArray(env, len);
	(*env)->SetByteArrayRegion(env, outArray,0,len,(jbyte*)bytes);
	free(bytes);
	bytes = NULL;
	return outArray;
}
