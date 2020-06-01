package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.PhotoPicker.ImageStore;
import cn.poco.album2.AlbumPage;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.camera.ImageFile2;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.FileCacheMgr;
import cn.poco.image.filter;
import cn.poco.resource.FilterRes;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.utils.Utils;
import cn.poco.video.render.gles.GlUtil;

public class BeautifyHandler extends Handler
{
	public static final int MSG_INIT = 1;
	public static final int MSG_SAVE = 2;
	public static final int MSG_COLOR_FILTER = 3;	//滤色
	public static final int MSG_CACHE = 4;
	public static final int MSG_COLOR_CACHE = 5;
	public static final int MSG_SAVE_CAMERA_IMG = 6;
	public static final int MSG_READ_ALUMB = 7;
	public static final int MSG_MAKEBK = 8;
	public static final int MSG_COLOR_ADJUST = 9;	//调整
	public static final int DECODE_LIGHTEFFECT_IMG = 10;
	public static final int MSG_PASTE = 11;
	public static final int MSG_LIGHT_ADJUST = 12;
	protected Context m_context;
	protected Handler m_UIHandler;

	public BeautifyHandler(Looper looper, Context context, Handler ui)
	{
		super(looper);

		m_context = context;
		m_UIHandler = ui;
	}

	@Override
	public void handleMessage(Message msg)
	{
		Message uiMsg;
		switch(msg.what)
		{
			case MSG_INIT:
			{
				InitMsg params = (InitMsg)msg.obj;
				msg.obj = null;

				//先把图片保存到相册
				RotationImg2[] info = null;
				Bitmap tempBmp = null;
				if(params.m_inImgs instanceof ImageFile2)
				{
					ImageFile2 file = (ImageFile2)params.m_inImgs;
					info = file.SaveImg2(m_context);
					params.m_inImgs = info;
				}

				if(params.m_thumb != null)
				{
					String path = FileCacheMgr.GetLinePath();
					boolean flag = Utils.SaveTempImg(params.m_thumb, path);
					if(flag)
					{
						params.m_tempPath = path;
					}
				}

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = params;
				uiMsg.what = MSG_INIT;
				m_UIHandler.sendMessage(uiMsg);

				break;
			}
			case MSG_CACHE:
			{
				InitMsg params = (InitMsg)msg.obj;
				msg.obj = null;
				if(params != null)
				{
					if(params.m_inImgs != null && params.m_inImgs instanceof ImageStore.ImageInfo)
					{
						ImageStore.ImageInfo imgInfo = (ImageStore.ImageInfo)params.m_inImgs;
						if(params.m_thumb != null)
						{
							if(imgInfo.localAlbum)
							{
								params.m_outImgs = AlbumUtils.saveEffect(m_context, imgInfo.id, params.m_thumb, imgInfo.image, imgInfo.effect);
							}
							else
							{
								params.m_outImgs = AlbumUtils.insertImage(m_context, params.m_thumb, imgInfo.image, imgInfo.effect);
							}
						}
					}
				}

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = params;
				uiMsg.what = MSG_CACHE;
				m_UIHandler.sendMessage(uiMsg);

				break;
			}

			case MSG_COLOR_CACHE:
			{
				ColorMsg params = (ColorMsg)msg.obj;
				msg.obj = null;

				Bitmap tempBmp = MakeBmp2(m_context, params.m_orgThumbPath, -1, -1);

				String effectDatas = MakeFilterJson(params);
				Bitmap bmp = AddFilter(m_context, tempBmp, params.m_filterData, params.m_filterAlpha);
				bmp = AddEffects(params.m_adjustData, bmp);
				if(params.m_curInfo != null)
				{
					bmp = DoCurve(bmp, params.m_curInfo);
				}
				if(bmp != null)
				{
					if(params != null)
					{
						if(params.m_inImgs != null && params.m_inImgs instanceof ImageStore.ImageInfo)
						{
							ImageStore.ImageInfo imgInfo = (ImageStore.ImageInfo)params.m_inImgs;
							if(imgInfo.localAlbum)
							{
								params.m_outImgs = AlbumUtils.saveEffect(m_context, imgInfo.id, bmp, imgInfo.image, effectDatas);
							}
							else
							{
								params.m_outImgs = AlbumUtils.insertImage(m_context, bmp, imgInfo.image, effectDatas);
							}
						}
						params.m_thumb = bmp;
					}
				}

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = params;
				uiMsg.what = MSG_COLOR_CACHE;
				m_UIHandler.sendMessage(uiMsg);
				break;
			}

			case MSG_COLOR_FILTER:
			{
				ColorMsg params = (ColorMsg)msg.obj;
				msg.obj = null;

				Bitmap tempBmp = params.m_thumb;
				params.m_thumb = AddFilter(m_context, tempBmp, params.m_filterData, params.m_filterAlpha);

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = params;
				uiMsg.what = MSG_COLOR_FILTER;
				m_UIHandler.sendMessage(uiMsg);
				break;
			}
			case MSG_COLOR_ADJUST:
			{
				ColorMsg params = (ColorMsg)msg.obj;
				msg.obj = null;

				Bitmap tempBmp = params.m_thumb;
				params.m_thumb = AddEffects(params.m_adjustData, tempBmp);

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = params;
				uiMsg.what = MSG_COLOR_ADJUST;
				m_UIHandler.sendMessage(uiMsg);
				break;
			}
			case MSG_MAKEBK:
			{
				Bitmap bmp;
				if(!(msg.obj instanceof Bitmap))
				{
					bmp = MakeBmp(m_context, msg.obj, msg.arg1, msg.arg2);
				}
				else
				{
					bmp = (Bitmap)msg.obj;
				}
				if(bmp != null)
				{
					Bitmap bk = BeautifyResMgr.MakeBkBmp(bmp, msg.arg1, msg.arg2, 0xcc000000, 0x26000000);
					if(bmp != bk)
					{
						bmp.recycle();
					}
					uiMsg = m_UIHandler.obtainMessage();
					uiMsg.obj = bk;
					uiMsg.what = MSG_MAKEBK;
					m_UIHandler.sendMessage(uiMsg);
				}
				break;
			}
			case DECODE_LIGHTEFFECT_IMG:
			{
				Bitmap out = null;
				Object res = msg.obj;
				int id = msg.arg1;
				if(res != null && res instanceof String)
				{
					File file = new File((String)res);
					if(!file.exists())
					{
						try
						{
							InputStream is = m_context.getAssets().open((String)res);
							ByteArrayOutputStream bout = new ByteArrayOutputStream();
							byte[] bytes = new byte[1024];
							while(is.read(bytes) != -1)
							{
								bout.write(bytes, 0, bytes.length);
							}
							res = bout.toByteArray();
							bout.close();
							is.close();
						}catch(Exception e)
						{
							e.printStackTrace();
						}
					}

					out = Utils.DecodeImage(m_context, res, 0, -1, -1, -1);
					int size = GlUtil.getMaxTextureSize();
					if(out != null && (out.getHeight() > size || out.getWidth() > size))
					{
						out = MakeBmpV2.CreateBitmapV2(out, 0, 0, -1, size, size, Config.ARGB_8888);
					}
				}

				uiMsg = m_UIHandler.obtainMessage();
				uiMsg.obj = out;
				uiMsg.arg1 = id;
				uiMsg.what = DECODE_LIGHTEFFECT_IMG;
				m_UIHandler.sendMessage(uiMsg);

				break;
			}

			case MSG_SAVE:
			{
				HashMap<String, Object> params = (HashMap<String, Object>)msg.obj;
				if(params != null)
				{
					Object o = params.get("img");
					if(o != null)
					{
						ImageStore.ImageInfo imgs = (ImageStore.ImageInfo)o;
						boolean adddate = (Boolean)params.get("add_date");
						if(adddate)
						{
							ExifInterface orgExif = null;
							if(imgs.image != null)
							{
								try
								{
									orgExif = new ExifInterface(imgs.image);
								}
								catch(IOException e)
								{
									e.printStackTrace();
								}
							}
							Bitmap bmp = MakeBmp2(m_context, imgs.image, -1, -1);
							if(bmp != null)
							{
								Utils.attachDate(bmp);
								File file = new File(imgs.image);
								if(file.exists())
								{
									file.delete();
								}
								int quality = 100;
								if(bmp.getWidth() > 2048 && bmp.getHeight() > 2048)
								{
									quality = 96;
								}
								String savePath = Utils.SaveImg(m_context, bmp, imgs.image, quality, false);
								imgs.image = savePath;
							}
							ExifInterface saveExif = null;
							if(imgs.image != null)
							{
								try
								{
									saveExif = new ExifInterface(imgs.image);
								}
								catch(IOException e)
								{
									e.printStackTrace();
								}
							}
							SaveExifInfoToImg(orgExif, saveExif);
						}
						AlbumUtils.save(m_context, imgs.id);

						uiMsg = m_UIHandler.obtainMessage();
						uiMsg.obj = params;
						uiMsg.what = MSG_SAVE;
						m_UIHandler.sendMessage(uiMsg);
					}
				}
				break;
			}
			case MSG_PASTE:
			{
				HashMap<String, Object> params = (HashMap<String, Object>)msg.obj;
				if(params != null)
				{
					AlbumPage.CopyEffect copyEffect = null;
					ImageStore.ImageInfo imageInfo = null;
					Object o = params.get("copy_effect");
					if(o != null)
					{
						copyEffect = (AlbumPage.CopyEffect) o;
					}
					o = params.get("img");
					if(o != null)
					{
						imageInfo = (ImageStore.ImageInfo)o;
					}
					params.clear();
					Object outImgs = null;
					if(imageInfo != null && copyEffect != null)
					{
						Bitmap tempBmp = MakeBmp2(m_context, imageInfo.image, -1, -1);
						Bitmap bmp = DoEffects(m_context, copyEffect.effect, tempBmp);
						if(imageInfo.localAlbum)
						{
							outImgs = AlbumUtils.saveEffect(m_context, imageInfo.id, bmp, imageInfo.image, copyEffect.effect);
						}
						else
						{
							outImgs = AlbumUtils.insertImage(m_context, bmp, imageInfo.image, copyEffect.effect);
						}
						bmp.recycle();
					}
					if(outImgs != null)
					{
						params.put("outInfo", outImgs);
					}

					uiMsg = m_UIHandler.obtainMessage();
					uiMsg.obj = params;
					uiMsg.what = MSG_PASTE;
					m_UIHandler.sendMessage(uiMsg);
				}
				break;
			}

			case MSG_LIGHT_ADJUST:
			{
				Bitmap bmp = null;
				if(msg.obj != null)
				{
					LightAdjustMsg temp = (LightAdjustMsg) msg.obj;
					bmp = temp.m_thumb;
					HashMap<Integer,BeautifyResMgr.LightAdjustData> m_arr = temp.m_adjustArr;
					int value1 = getLightAdjustValue(0,m_arr);
					int value2 = getLightAdjustValue(1,m_arr);
					int value3 = getLightAdjustValue(2,m_arr);
					Log.i("HHH","value1 = " + value1 + " value2 = " + value2 + " value3 = " + value3 + " " + bmp.isRecycled());
					filter.lightEffectAdjsut(bmp,value1,value2,value3);
				}

				Message message = Message.obtain();
				message.what = MSG_LIGHT_ADJUST;
				message.obj = bmp;
				m_UIHandler.sendMessage(message);

				break;
			}
		}
	}

	private int getLightAdjustValue(int index, HashMap<Integer,BeautifyResMgr.LightAdjustData> arr)
	{

		int out = 0;
		BeautifyResMgr.LightAdjustData data = arr.get(index);
		if(data != null)
		{
			out = (int) data.m_value;
		}
		return out;
	}

	public static void SaveExifInfoToImg(String orgPath, String savePath)
	{
		ExifInterface orgExif = null;
		if(orgPath != null)
		{
			try
			{
				orgExif = new ExifInterface(orgPath);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		if(savePath != null && orgExif != null)
		{
			try {
				ExifInterface saveExif = new ExifInterface(savePath);
				SaveExifInfoToImg(orgExif, saveExif);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void SaveExifInfoToImg(ExifInterface orgExif, ExifInterface saveExif)
	{

		if (orgExif == null || saveExif == null) {
			return;
		}

		Class<ExifInterface> cls = ExifInterface.class;
		Field[] fields = cls.getFields();

		try {
			for (int i = 0; i < fields.length; i++) {
				String fieldName = fields[i].getName();
				if (!TextUtils.isEmpty(fieldName) && fieldName.startsWith("TAG")) {

					if (fieldName.equals("TAG_ORIENTATION")) {
						saveExif.setAttribute(ExifInterface.TAG_ORIENTATION, "0");
					} else {
						String fieldValue = fields[i].get(cls).toString();
						String attribute = orgExif.getAttribute(fieldValue);
						if (!TextUtils.isEmpty(attribute)) {
							saveExif.setAttribute(fieldValue, attribute);
						}
					}
				}
			}

			saveExif.saveAttributes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String MakeFilterJson(ColorMsg msg)
	{
		String out = null;
		if(msg != null)
		{
			try
			{
				JSONObject jsonObject = new JSONObject();
				if(msg.m_filterData != null)
				{
					jsonObject.put("filter_id", msg.m_filterData.m_id);
					jsonObject.put("filter_data", msg.m_filterData.m_filterData == null ? "": msg.m_filterData.m_filterData);
					jsonObject.put("filter_alpha", msg.m_filterAlpha);
				}
				if(msg.m_curInfo != null)
				{
					int infoSize = msg.m_curInfo.size();
					ArrayList<Float> allPoints = new ArrayList<>();
					int count[] = new int[infoSize];
					for(int s = 0; s < infoSize; s ++)
					{
						CurveView2.CurveInfo info = msg.m_curInfo.get(s);
						int size = info.m_ctrlPoints.size();
						for(int i = 0; i < size; i ++)
						{
							allPoints.add(info.m_ctrlPoints.get(i).x / info.m_viewSize);
							allPoints.add((info.m_viewSize - info.m_ctrlPoints.get(i).y) / info.m_viewSize);
						}
						count[s] = size;
					}
					int size = allPoints.size();
					JSONArray jsonArr = new JSONArray();
					for(int i = 0; i < size; i ++)
					{
						jsonArr.put(allPoints.get(i));
					}
					jsonObject.put("curve_channelPoints", jsonArr);
					jsonArr = new JSONArray();
					for(int i = 0; i < count.length; i ++)
					{
						jsonArr.put(count[i]);
					}
					jsonObject.put("curve_count", jsonArr);
				}
				if(msg.m_adjustData != null && msg.m_adjustData.size() > 0)
				{
					int size = msg.m_adjustData.size();
					JSONArray jsonArr = new JSONArray();
					boolean hasAdjust = false;
					JSONObject jsonObject1;
					for(int i = 0; i < size; i ++)
					{
						if(msg.m_adjustData.get(i).m_value != 0)
						{
							hasAdjust = true;
							jsonObject1 = new JSONObject();
							jsonObject1.put("id", msg.m_adjustData.get(i).m_type.GetValue());
							jsonObject1.put("value", msg.m_adjustData.get(i).m_value);
							jsonArr.put(jsonObject1);
						}
					}
					if(hasAdjust)
					{
						jsonObject.put("adjust", jsonArr);
					}
				}
				out = jsonObject.toString();
				if(out.equals("{}"))
				{
					out = null;
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}

		}
		return out;
	}

	public static Bitmap DoEffects(Context context, String effectDatas, Bitmap bmp)
	{
		Bitmap out = bmp;
		if(effectDatas != null && effectDatas.length() > 0)
		{
			try
			{
				JSONObject jsonObject = new JSONObject(effectDatas);
				if(jsonObject.has("filter_id"))
				{
					int filterId = jsonObject.getInt("filter_id");
					String filterData = null;
					if(jsonObject.has("filter_data"))
					{
						filterData = jsonObject.getString("filter_data");
					}
					int filterAlpha = jsonObject.getInt("filter_alpha");
					out = AddFilter(context, out, filterId, filterData, filterAlpha);
				}
				JSONArray jsonArr;
				JSONObject jsonObj;
				if(jsonObject.has("adjust"))
				{
					jsonArr = jsonObject.getJSONArray("adjust");
					ArrayList<BeautifyResMgr.AdjustData> adjustData = new ArrayList<>();
					BeautifyResMgr.AdjustData itemData;
					if(jsonArr != null && jsonArr.length() != 0)
					{
						int length = jsonArr.length();
						for(int i = 0; i < length; i ++)
						{
							jsonObj = jsonArr.getJSONObject(i);
							int id = jsonObj.getInt("id");
							float value = Float.parseFloat(jsonObj.getString("value"));
							itemData = new BeautifyResMgr.AdjustData(BeautyAdjustType.GetType(id), value);
							adjustData.add(itemData);
						}
					}
					out = AddEffects(adjustData, out);
				}

				int[] count = null;
				float[] points = null;
				if(jsonObject.has("curve_count"))
				{
					jsonArr = jsonObject.getJSONArray("curve_count");
					if(jsonArr != null && jsonArr.length() > 0)
					{
						count = new int[jsonArr.length()];
						for(int i = 0; i < count.length; i ++)
						{
							count[i] = jsonArr.getInt(i);
						}
					}
				}
				if(jsonObject.has("curve_channelPoints"))
				{
					jsonArr = jsonObject.getJSONArray("curve_channelPoints");
					if(jsonArr != null && jsonArr.length() > 0)
					{
						points = new float[jsonArr.length()];
						for(int i = 0; i < points.length; i ++)
						{
							points[i] = Float.parseFloat(jsonArr.getString(i));
						}
					}
				}
				if(count != null && points != null)
				{
					out = filter.AdjustCurveAll(out, out, points, count);
				}

			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return out;
	}

	public static Bitmap AddEffects(ArrayList<BeautifyResMgr.AdjustData> params, Bitmap tempBmp)
	{
		if(params == null)
			return tempBmp;
		BeautifyResMgr.AdjustData data = null;
		int size = params.size();
		for(int i = 0; i < size; i ++)
		{
			data = params.get(i);
			if(data != null && data.m_type != null)
			{
				switch(data.m_type)
				{
					case BRIGHTNESS:
					{
						//亮度
						tempBmp = filter.changeBrightness(tempBmp, data.m_value);
						break;
					}
					case CONTRAST:
					{
						//对比度
						tempBmp = filter.changeContrast_p(tempBmp, (int)data.m_value);
						break;
					}
					case SATURABILITY:
					{
						//饱和度
						tempBmp = filter.changeSaturation(tempBmp, (int)ComputeRate(data.m_value, 100));
						break;
					}
					case TEMPERATURE:
					{
						//色温
						tempBmp = filter.whiteBalance_p(tempBmp, ComputeRate(data.m_value, 1));
						break;
					}
					case SHARPEN:
					{
						//锐化
						tempBmp = filter.sharpen(tempBmp, (int)data.m_value);
						break;
					}
					case BETTER:
					{
						//加强效果
						tempBmp = Retinex(tempBmp, (int)data.m_value);
						break;
					}
					case HIGHTLIGHT:
					{
						//高光
						tempBmp = filter.AdjustHighLight(tempBmp, ComputeRate(data.m_value, 10));
						break;
					}
					case SHADE:
					{
						//暗部
						tempBmp = filter.AdjustShadow(tempBmp, ComputeRate(data.m_value, 10));
						break;
					}
					case HUE:
					{
						//色调
						tempBmp = filter.AdjustTone(tempBmp, ComputeRate(data.m_value, 10));
						break;
					}
					case DARKCORNER:
					{
						//暗角
						tempBmp = filter.AdjustDarkenCorner(tempBmp, data.m_value);
						break;
					}
					case PARTICAL:
					{
						//颗粒
						tempBmp = filter.AdjustGranule(tempBmp, data.m_value);
						break;
					}
					case FADE:
					{
						//褪色
						tempBmp = filter.AdjustFade(tempBmp, data.m_value);
						break;
					}
					case BEAUTY:
					{
						//肤色
						tempBmp = filter.skinColorAdjust(tempBmp, (int)ComputeRate(data.m_value, 50));
						break;
					}
				}
			}
		}

		return tempBmp;
	}

	protected Bitmap DoCurve(Bitmap bmp, ArrayList<CurveView2.CurveInfo> curInfos)
	{
		if(curInfos != null)
		{
			int infoSize = curInfos.size();
			ArrayList<Float> allPoints = new ArrayList<>();
			int count[] = new int[infoSize];
			for(int s = 0; s < infoSize; s ++)
			{
				CurveView2.CurveInfo info = curInfos.get(s);
				int size = info.m_ctrlPoints.size();
				for(int i = 0; i < size; i ++)
				{
					allPoints.add(info.m_ctrlPoints.get(i).x / info.m_viewSize);
					allPoints.add((info.m_viewSize - info.m_ctrlPoints.get(i).y) / info.m_viewSize);
				}
				count[s] = size;
			}
			int size = allPoints.size();
			float[] controlPoints = new float[size];
			for(int i = 0; i < size; i ++)
			{
				controlPoints[i] = allPoints.get(i);
			}
			filter.AdjustCurveAll(bmp, bmp, controlPoints, count);
		}
		return bmp;
	}

	protected static float ComputeRate(float src, int max)
	{
		if(src == 0)
			return src;
		int maxP = 120;
		int progress = (int)(((src + max) / (max * 2f)) * maxP);
		int dotNum = 13;
		int spacePro = 0;
		if(dotNum > 1)
		{
			spacePro = maxP / (dotNum - 1);
		}
		spacePro = spacePro / 2;
		for(int i = 1; i <= dotNum; i ++)
		{
			int minD = spacePro + (i - 2) * 2 * spacePro;
			int maxD = spacePro + (i - 1) * 2 * spacePro;
			if(progress > minD && progress <= maxD)
			{
				progress = (i - 1) * 2 * spacePro;
				if(progress >= maxP)
				{
					progress = maxP;
				}
				if(progress <= 0)
				{
					progress = 0;
				}
				break;
			}
		}
		int index = progress / 10 - 6;
		int flag = index > 0 ? 1: -1;
		float rate = 0;
		index = Math.abs(index);
		if(index == 1)
		{
			rate = 0.1f;
		}
		else if(index == 2)
		{
			rate = 0.25f;
		}
		else if(index == 3)
		{
			rate = 0.4f;
		}
		else if(index == 4)
		{
			rate = 0.6f;
		}
		else if(index == 5)
		{
			rate = 0.8f;
		}
		else if(index == 6)
		{
			rate = 1.0f;
		}
		return max * rate * flag;
	}

	/**
	 *  增强调节	
	 *  @src 源Bitmap
	 *  @value 亮度值，范围在[0, 100]。
	 *  @ 返回一个处理后的新Bitmap
	 */
	public static Bitmap Retinex(Bitmap src, int percent)
	{
		if(percent == 0)
			return src;
		Bitmap dst = src.copy(Config.ARGB_8888, true);
		src = filter.oneKeyRetinex(src);
		ImageProcessor.DrawMask(dst, src, percent);
		if(src != null)
		{
			src.recycle();
			src = null;
		}
		return dst;
	}

	public static Bitmap AddFilter(Context context, Bitmap bmp, FilterRes color, int alpha)
	{
		Bitmap out = bmp;
		if(color != null && out != null && !out.isRecycled())
		{
			out = AddFilter(context, bmp, color.m_id, color.m_filterData, alpha);
		}
		return out;
	}

	public static Bitmap AddFilter(Context context, Bitmap bmp, int color_id, String color_data, int alpha)
	{
		Bitmap out = bmp;
		if(out != null && !out.isRecycled())
		{
			if(alpha >= 100)
			{
				out = ImageProcessor.ConversionImgColor2(context, out, color_data, color_id);
			}
			else
			{
				bmp = ImageProcessor.ConversionImgColor2(context, out.copy(Config.ARGB_8888, true), color_data, color_id);

				if(ImageProcessor.needGray(color_data) || color_id == 30 || color_id == 54 || color_id == 38)
				{
					out = toGrayscale(out);
				}
				if (bmp != null && !bmp.isRecycled()) {
					out = ImageProcessor.DrawMask(out, bmp, alpha);
					bmp.recycle();
				}
				bmp = null;
			}
		}
		return out;
	}

	public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * 用于显示
	 * @param context
	 * @param res
	 * @param frW
	 * @param frH
	 * @return
	 */
	public static Bitmap MakeBmp(Context context, Object res, int frW, int frH)
	{
		Bitmap out = null;

		if(res instanceof RotationImg2[])
		{
			int rotation = ((RotationImg2[])res)[0].m_degree;
			int flip = ((RotationImg2[])res)[0].m_flip;
			Bitmap temp = Utils.DecodeImage(context, ((RotationImg2[])res)[0].m_img, rotation, -1, frW, frH);
			if(temp == null)
				return out;
			if(rotation == 0 && flip == MakeBmpV2.FLIP_NONE && temp.getWidth() <= frW && temp.getHeight() <= frH)
			{
				if(temp.isMutable())
				{
					out = temp;
				}
				else
				{
					out = temp.copy(Config.ARGB_8888, true);
					temp.recycle();
					temp = null;
				}
			}
			else
			{
				out = MakeBmpV2.CreateBitmapV2(temp, rotation, flip, -1, frW, frH, Config.ARGB_8888);
				temp.recycle();
				temp = null;
			}
		}
		else if(res instanceof ImageFile2)
		{
			Bitmap temp = ((ImageFile2)res).MakeBmp(context, frW, frH);
			if(temp.isMutable())
			{
				out = temp;
			}
			else
			{
				out = temp.copy(Config.ARGB_8888, true);
				temp.recycle();
				temp = null;
			}
		}
		else if(res instanceof String)
		{
			RotationImg2[] img = new RotationImg2[1];
			img[0] = Utils.Path2ImgObj((String)res);
			out = MakeBmp(context, img, frW, frH);
		}

		return out;
	}

	/**
	 * 用于保存
	 * @param context
	 * @param res
	 * @param frW
	 * @param frH
	 * @return
	 */
	public static Bitmap MakeBmp2(Context context, Object res, int frW, int frH)
	{
		Bitmap out = null;

		if(res instanceof RotationImg2[])
		{
			int rotation = ((RotationImg2[])res)[0].m_degree;
			int flip = ((RotationImg2[])res)[0].m_flip;
			Bitmap temp = Utils.DecodeImage2(context, ((RotationImg2[])res)[0].m_img, rotation, -1, frW, frH);
			if(temp == null)
				return out;
			if(rotation == 0 && flip == MakeBmpV2.FLIP_NONE && temp.getWidth() <= frW && temp.getHeight() <= frH)
			{
				if(temp.isMutable())
				{
					out = temp;
				}
				else
				{
					out = temp.copy(Config.ARGB_8888, true);
					temp.recycle();
					temp = null;
				}
			}
			else
			{
				out = MakeBmpV2.CreateBitmapV2(temp, rotation, flip, -1, frW, frH, Config.ARGB_8888);
				temp.recycle();
				temp = null;
			}
		}
		else if(res instanceof ImageFile2)
		{
			Bitmap temp = ((ImageFile2)res).MakeBmp(context, frW, frH);
			if(temp.isMutable())
			{
				out = temp;
			}
			else
			{
				out = temp.copy(Config.ARGB_8888, true);
				temp.recycle();
				temp = null;
			}
		}
		else if(res instanceof String)
		{
			RotationImg2[] img = new RotationImg2[1];
			img[0] = Utils.Path2ImgObj((String)res);
			out = MakeBmp2(context, img, frW, frH);
		}

		return out;
	}

	public static class ColorMsg extends CmdMsg
	{
		//in
		public String m_orgThumbPath;
		public FilterRes m_filterData;
		public int m_filterAlpha = 100;

		public ArrayList<CurveView2.CurveInfo> m_curInfo;
		public ArrayList<BeautifyResMgr.AdjustData> m_adjustData = new ArrayList<>();
	}

	public static class LightAdjustMsg extends CmdMsg
	{
		public HashMap<Integer,BeautifyResMgr.LightAdjustData> m_adjustArr = new HashMap<>();
	}

	public static class InitMsg extends CmdMsg {

	}

	public static class CmdMsg
	{
		public Object m_inImgs;//可修改为object

		public Object m_outImgs;

		// in
		public Bitmap m_thumb;

		// out
		public String m_tempPath;
	}
}
