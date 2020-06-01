package cn.poco.beautify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.poco.image.PocoPsAdjust;
import cn.poco.image.filter;
import cn.poco.imagecore.ImageUtils;
import cn.poco.resource.FilterInfo;
import cn.poco.tianutils.MakeBmp;
import cn.poco.tianutils.MakeBmpV2;
import cn.poco.utils.Utils;
import poco.photedatabaselib2016.info.InterPhoto;

public class ImageProcessor
{
	public static Bitmap ConversionImgColor(Context context, int uri, Bitmap bmp)
	{
		Bitmap out = bmp;
		if(context != null && bmp != null)
		{
			try
			{
				switch(uri)
				{
					case 0:
					{
						out = bmp;
						break;
					}
					case 39:
					{
						out = filter.testFilter2(bmp);
						break;
					}
					case 53:
					{
						out = filter.testFilter8(bmp);
						break;
					}
					case 52:
					{
						out = filter.testFilter9(bmp);
						break;
					}
					case 49:
					{
						out = filter.testFilter14(bmp, context);
						break;
					}
					case 51:
					{
						out = filter.testFilter18(bmp);
						break;
					}
					case 50:
					{
						out = filter.testFilter32(bmp);
						break;
					}
					case 41:
					{
						out = filter.testFilterAY1(bmp);
						break;
					}
					case 47:
					{
						out = filter.testFilterAY2(bmp);
						break;
					}
					case 40:
					{
						out = filter.testFilterAY6(bmp);
						break;
					}
					case 48:
					{
						out = filter.testFilterAY8(bmp);
						break;
					}
					case 301:
					{
						out = filter.testFilterPh1(bmp);
						break;
					}
					case 302:
					{
						out = filter.testFilterPh2(bmp);
						break;
					}
					case 38:
					{
						out = filter.testFilterPh3(bmp);
						break;
					}
					case 304:
					{
						out = filter.testFilterPh4(bmp);
						break;
					}
					case 305:
					{
						out = filter.testFilterPh5(bmp);
						break;
					}
					case 37:
					{
						out = filter.testFilterPh6(bmp);
						break;
					}
					case 101:
					{
						out = filter.testFilter1(bmp);
						break;
					}
					case 44:
					{
						out = filter.testFilter7(bmp);
						break;
					}
					case 43:
					{
						out = filter.testFilter15(bmp);
						break;
					}
					case 54:
					{
						out = filter.testFilter16(bmp);
						break;
					}
					case 55:
					{
						out = filter.testFilter17(bmp);
						break;
					}
					case 57:
					{
						out = filter.polaroid_g(bmp);
						break;
					}
					case 56:
					{
						out = filter.polaroid_y(bmp);
						break;
					}
					case 42:
					{
						out = filter.lightengreen2(bmp);
						break;
					}
					case 58:
					{
						out = filter.cate(bmp);
						break;
					}
					case 112:
					{
						out = filter.testFilter3(bmp);
						break;
					}
					case 65:
					{
						out = filter.testFilter4(bmp);
						break;
					}
					case 114:
					{
						out = filter.testFilter5(bmp);
						break;
					}
					case 115:
					{
						out = filter.testFilter6(bmp);
						break;
					}
					case 116:
					{
						out = filter.testFilter10(bmp);
						break;
					}
					case 60:
					{
						out = filter.testFilter11(bmp, context);
						break;
					}
					case 63:
					{
						out = filter.testFilter12(bmp, context);
						break;
					}
					case 66:
					{
						out = filter.testFilter13(bmp);
						break;
					}
					case 120:
					{
						out = filter.testFilter19(bmp);
						break;
					}
					case 121:
					{
						out = filter.testFilter20(bmp);
						break;
					}
					case 122:
					{
						out = filter.testFilter21(bmp);
						break;
					}
					case 68:
					{
						out = filter.testFilter22(bmp, context);
						break;
					}
					case 124:
					{
						out = filter.testFilter23(bmp);
						break;
					}
					case 125:
					{
						out = filter.testFilter24(bmp, context);
						break;
					}
					case 35:
					{
						out = filter.interFilterZhangY1(bmp);
						break;
					}
					case 34:
					{
						out = filter.interFilterZhangY2(bmp);
						break;
					}
					case 46:
					{
						out = filter.interFilterZ1(bmp);
						break;
					}
					case 45:
					{
						out = filter.interFilterZ2(bmp);
						break;
					}
					case 126:
					{
						filter.testFilter1(bmp);
						out = filter.testFilter15(bmp);
						break;
					}
					case 59:
					{
						filter.testFilter11(bmp, context);
						out = filter.testFilter19(bmp);
						break;
					}
					case 128:
					{
						filter.testFilter13(bmp);
						out = filter.testFilter7(bmp);
						break;
					}
					case 129:
					{
						filter.testFilter11(bmp, context);
						filter.testFilter20(bmp);
						out = filter.testFilter22(bmp, context);
						break;
					}
					case 130:
					{
						filter.testFilter3(bmp);
						filter.testFilter24(bmp, context);
						out = filter.testFilter22(bmp, context);
						break;
					}
					case 131:
					{
						filter.testFilter1(bmp);
						filter.testFilter5(bmp);
						out = filter.testFilter22(bmp, context);
						break;
					}
					case 64:
					{
						filter.testFilter14(bmp, context);
						out = filter.testFilter15(bmp);
						break;
					}
					case 711:
					{
						out = filter.interFilterYuS(bmp);
						break;
					}
					case 712:
					{
						out = filter.interFilterYuS1(bmp);
						break;
					}
					case 713:
					{
						out = filter.interFilterYuS2(bmp);
						break;
					}
					case 36:
					{
						out = filter.interFilterYuS3(bmp);
						break;
					}
					case 32:
					{
						out = filter.interFilterLHY1(bmp);
						break;
					}
					case 69:
					{
						out = filter.interFilterM30(bmp);
						break;
					}
					case 33:
					{
						out = filter.interFilterLHY2(bmp);
						break;
					}
					case 100:
					{
						out = filter.interFilterOLY_CC(bmp);
						break;
					}
					case 70:
					{
						out = filter.interFilterM31(bmp);
						break;
					}
					case 31:
					{
						out = filter.interFilterLXL2(bmp);
						break;
					}
					case 30:
					{
						out = filter.interFilterTH1(bmp);
						break;
					}
					case 29:
					{
						out = filter.interFilterTH2(bmp);
						break;
					}
					default:
					{
						out = bmp;
						break;
					}
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}

		return out;
	}

	public static Bitmap ConversionImgColor2(Context context, Bitmap bmp, String colorData, int uri)
	{
		Bitmap out = bmp;
		if(colorData == null || colorData.length() == 0 || colorData.equals("false") || colorData.equals("null"))
		{
			out = ConversionImgColor(context, uri, bmp);
			return out;
		}
		try
		{
			JSONArray jsonArr = new JSONArray(colorData);
			JSONObject jsonObj;
			String temp;
			Object data;
			if(jsonArr != null)
			{
				int len = jsonArr.length();
				for(int i = 0; i < len; i ++)
				{
					jsonObj = jsonArr.getJSONObject(i);
					temp = jsonObj.getString("type");
					if(temp != null && temp.length() > 0)
					{
						int type = Integer.parseInt(temp);
						data = jsonObj.get("data");
						out = DoFilter(bmp, data, type);
					}
				}
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return out;
	}

	public static Bitmap DoFilter(Bitmap bmp, Object data, int type) throws Exception
	{
		Bitmap out = bmp;
		if(data != null)
		{
			List<Object> filterInfos = new ArrayList<>();
			if(data instanceof JSONArray)
			{
				filterInfos = toList((JSONArray)data);
			}
			else if(data instanceof JSONObject)
			{
				Map<String, Object> map = jsonToMap((JSONObject)data);
				filterInfos.add(map);
			}
			int size = filterInfos.size();
			for(int i = 0; i < size; i ++)
			{
				HashMap<String, Object> filter = (HashMap<String, Object>)filterInfos.get(i);

				switch(type)
				{
					case FilterInfo.TYPE0:
					{
						out = PocoPsAdjust.AdjustContrast(bmp, GetIntegerFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE1:
					{
						out = PocoPsAdjust.AdjustBrightness(bmp, GetIntegerFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE2:
					{
						ArrayList<Number> array = (ArrayList<Number>)filter.get("controlPoints");
						if(array != null)
						{
							int size1 = array.size();
							float[] controlPoints = new float[size1];
							for(int c = 0; c < size1; c ++)
							{
								controlPoints[c] = array.get(c).floatValue();
							}
							out = PocoPsAdjust.AdjustCurve(bmp, GetIntegerFromHashMap(filter, "Colorchannel"), controlPoints, size1 / 2);
						}
						break;
					}
					case FilterInfo.TYPE3:
					{
						out = PocoPsAdjust.AdjustExposure(bmp, GetIntegerFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE4:
					{
						int Colorchannel = GetIntegerFromHashMap(filter, "Colorchannel");
						int input_low = GetIntegerFromHashMap(filter, "input_low");
						double gamma = GetDoubleFromHashMap(filter, "gamma");
						int input_high = GetIntegerFromHashMap(filter, "input_high");
						int output_low = GetIntegerFromHashMap(filter, "output_low");
						int output_high = GetIntegerFromHashMap(filter, "output_high");
						out = PocoPsAdjust.AdjustColorLevel(bmp, Colorchannel, input_low, gamma, input_high, output_low, output_high);
						break;
					}
					case FilterInfo.TYPE5:
					{
						int Colorchannel = GetIntegerFromHashMap(filter, "Colorchannel");
						int Hvalue = GetIntegerFromHashMap(filter, "Hvalue");
						int Svalue = GetIntegerFromHashMap(filter, "Svalue");
						int Bvalue = GetIntegerFromHashMap(filter, "Bvalue");
						out = PocoPsAdjust.AdjustHueAndSaturation(bmp, Colorchannel, Hvalue, Svalue, Bvalue);
						break;
					}
					case FilterInfo.TYPE6:
					{
						int red = GetIntegerFromHashMap(filter, "red");
						int yellow = GetIntegerFromHashMap(filter, "yellow");
						int green = GetIntegerFromHashMap(filter, "green");
						int cyan = GetIntegerFromHashMap(filter, "cyan");
						int blue = GetIntegerFromHashMap(filter, "blue");
						int magenta = GetIntegerFromHashMap(filter, "magenta");
						boolean isDone = GetBooleanFromHashMap(filter, "isDone");
						out = PocoPsAdjust.AdjustBlackWhite(bmp, red, yellow, green, cyan, blue, magenta, isDone);
						break;
					}
					case FilterInfo.TYPE7:
					{
						int cyan_red_l = GetIntegerFromHashMap(filter, "cyan_red_l");
						int cyan_red_m = GetIntegerFromHashMap(filter, "cyan_red_m");
						int cyan_red_h = GetIntegerFromHashMap(filter, "cyan_red_h");
						int magenta_green_l = GetIntegerFromHashMap(filter, "magenta_green_l");
						int magenta_green_m = GetIntegerFromHashMap(filter, "magenta_green_m");
						int magenta_green_h = GetIntegerFromHashMap(filter, "magenta_green_h");
						int yellow_blue_l = GetIntegerFromHashMap(filter, "yellow_blue_l");
						int yellow_blue_m = GetIntegerFromHashMap(filter, "yellow_blue_m");
						int yellow_blue_h = GetIntegerFromHashMap(filter, "yellow_blue_h");
						out = PocoPsAdjust.AdjustColorBalance(bmp, cyan_red_l, cyan_red_m, cyan_red_h, magenta_green_l,
														magenta_green_m, magenta_green_h, yellow_blue_l, yellow_blue_m, yellow_blue_h);
						break;
					}
					case FilterInfo.TYPE8:
					{
						int rRedPercent = GetIntegerFromHashMap(filter, "rRedPercent");
						int rGreenPercent = GetIntegerFromHashMap(filter, "rGreenPercent");
						int rBluePercent = GetIntegerFromHashMap(filter, "rBluePercent");
						int rConstant = GetIntegerFromHashMap(filter, "rConstant");
						int gRedPercent = GetIntegerFromHashMap(filter, "gRedPercent");
						int gGreenPercent = GetIntegerFromHashMap(filter, "gGreenPercent");
						int gBluePercent = GetIntegerFromHashMap(filter, "gBluePercent");
						int gConstant = GetIntegerFromHashMap(filter, "gConstant");
						int bRedPercent = GetIntegerFromHashMap(filter, "bRedPercent");
						int bGreenPercent = GetIntegerFromHashMap(filter, "bGreenPercent");
						int bBluePercent = GetIntegerFromHashMap(filter, "bBluePercent");
						int bConstant = GetIntegerFromHashMap(filter, "bConstant");
						out = PocoPsAdjust.AdjustMixChannel(bmp, rRedPercent, rGreenPercent, rBluePercent, rConstant, gRedPercent, gGreenPercent,
													  gBluePercent, gConstant, bRedPercent, bGreenPercent, bBluePercent, bConstant);
						break;
					}
					case FilterInfo.TYPE9:
					{
						boolean isDone = GetBooleanFromHashMap(filter, "isDone");
						out = PocoPsAdjust.AdjustNegative(bmp, isDone);
						break;
					}
					case FilterInfo.TYPE10:
					{
						int shadows = GetIntegerFromHashMap(filter, "shadows");
						int highlights = GetIntegerFromHashMap(filter, "highlights");
						int smoothing = GetIntegerFromHashMap(filter, "smothing");
						out = PocoPsAdjust.AdjustHighlightShadow(bmp, shadows, highlights, smoothing);
						break;
					}
					case FilterInfo.TYPE11:
					{
						int rc = GetIntegerFromHashMap(filter, "rc");
						int rm = GetIntegerFromHashMap(filter, "rm");
						int ry = GetIntegerFromHashMap(filter, "ry");
						int rk = GetIntegerFromHashMap(filter, "rk");
						int yc = GetIntegerFromHashMap(filter, "yc");
						int ym = GetIntegerFromHashMap(filter, "ym");
						int yy = GetIntegerFromHashMap(filter, "yy");
						int yk = GetIntegerFromHashMap(filter, "yk");
						int gc = GetIntegerFromHashMap(filter, "gc");
						int gm = GetIntegerFromHashMap(filter, "gm");
						int gy = GetIntegerFromHashMap(filter, "gy");
						int gk = GetIntegerFromHashMap(filter, "gk");
						int cc = GetIntegerFromHashMap(filter, "cc");
						int cm = GetIntegerFromHashMap(filter, "cm");
						int cy = GetIntegerFromHashMap(filter, "cy");
						int ck = GetIntegerFromHashMap(filter, "ck");
						int bc = GetIntegerFromHashMap(filter, "bc");
						int bm = GetIntegerFromHashMap(filter, "bm");
						int by = GetIntegerFromHashMap(filter, "by");
						int bk = GetIntegerFromHashMap(filter, "bk");
						int mc = GetIntegerFromHashMap(filter, "mc");
						int mm = GetIntegerFromHashMap(filter, "mm");
						int my = GetIntegerFromHashMap(filter, "my");
						int mk = GetIntegerFromHashMap(filter, "mk");
						int wc = GetIntegerFromHashMap(filter, "wc");
						int wm = GetIntegerFromHashMap(filter, "wm");
						int wy = GetIntegerFromHashMap(filter, "wy");
						int wk = GetIntegerFromHashMap(filter, "wk");
						int nc = GetIntegerFromHashMap(filter, "nc");
						int nm = GetIntegerFromHashMap(filter, "nm");
						int ny = GetIntegerFromHashMap(filter, "ny");
						int nk = GetIntegerFromHashMap(filter, "nk");
						int kc = GetIntegerFromHashMap(filter, "kc");
						int km = GetIntegerFromHashMap(filter, "km");
						int ky = GetIntegerFromHashMap(filter, "ky");
						int kk = GetIntegerFromHashMap(filter, "kk");

						out = PocoPsAdjust.AdjustOptionColor(bmp, rc, rm, ry, rk, yc, ym, yy, yk, gc, gm, gy, gk, cc, cm, cy, ck, bc, bm, by, bk,
													   mc, mm, my, mk, wc, wm, wy, wk, nc, nm, ny, nk, kc, km, ky, kk);
						break;
					}
					case FilterInfo.TYPE12:
					{
						out = PocoPsAdjust.AdjustSharpen(bmp, GetIntegerFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE13:
					{
						out = PocoPsAdjust.AdjustNoise(bmp, GetIntegerFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE14:
					{
						out = PocoPsAdjust.AdjustGaussianBlur(bmp, GetDoubleFromHashMap(filter, "value"));
						break;
					}
					case FilterInfo.TYPE15:
					{
						int radius = GetIntegerFromHashMap(filter, "radius");
						int threshold = GetIntegerFromHashMap(filter, "threshold");
						out = PocoPsAdjust.AdjustSurfaceBlur(bmp, radius, threshold);
						break;
					}
					case FilterInfo.TYPE16:
					{
						int r = GetIntegerFromHashMap(filter, "r");
						int g = GetIntegerFromHashMap(filter, "g");
						int b = GetIntegerFromHashMap(filter, "b");
						int comOp = GetIntegerFromHashMap(filter, "comOp");
						int fillParam = GetIntegerFromHashMap(filter, "fillParam");
						out = PocoPsAdjust.AdjustSolidFill(bmp, r, g, b, comOp, fillParam);
						break;
					}
					case FilterInfo.TYPE17:
					{
						int radius = GetIntegerFromHashMap(filter, "radius");
						int opacity = GetIntegerFromHashMap(filter, "opacity");
						out = PocoPsAdjust.AdjustDarkenCorner(bmp, radius, opacity);
						break;
					}
					case FilterInfo.TYPE19:
					{
						int value = GetIntegerFromHashMap(filter, "value");
						out = BeautifyHandler.Retinex(bmp, value);
						break;
					}
				}
			}
		}
		return out;
	}

	public static boolean needGray(String filterData)
	{
		if(!TextUtils.isEmpty(filterData) && !filterData.equals("false") && !filterData.equals("null"))
		{
			try
			{
				JSONArray jsonArr = new JSONArray(filterData);
				List<Object> map =  ImageProcessor.toList(jsonArr);
				if(map != null && map.size() > 0){
					int size = map.size();
					HashMap<String, Object> array;
					HashMap<String, Object> data;
					for(int i = 0; i < size; i ++)
					{
						array = (HashMap<String, Object>)map.get(i);
						if(array != null)
						{
							String type = "";
							boolean isDone = false;
							for(Iterator it = array.entrySet().iterator(); it.hasNext();)
							{
								HashMap.Entry e = (HashMap.Entry) it.next();
								String key = (String)e.getKey();
								Object value = e.getValue();
								if("type".equals(key))
								{
									type = (String)value;
								}
								if("data".equals(key) && value instanceof HashMap)
								{
									data = (HashMap<String, Object>)value;
									if(data.get("isDone") != null)
									{
										isDone = (Boolean)data.get("isDone");
									}
								}
							}
							if("6".equals(type) && isDone)
							{
								return true;
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}

	public static int GetIntegerFromHashMap(HashMap<String, Object> map, String key)
	{
		if(map != null && map.get(key) != null)
		{
			return map.get(key) instanceof Number ? ((Number)map.get(key)).intValue() : Integer.parseInt((String)map.get(key));
		}
		return 0;
	}

	public static double GetDoubleFromHashMap(HashMap<String, Object> map, String key)
	{
		if(map != null && map.get(key) != null)
		{
			return map.get(key) instanceof Number ? ((Number)map.get(key)).doubleValue() : Double.parseDouble((String)map.get(key));
		}
		return 0;
	}

	public static boolean GetBooleanFromHashMap(HashMap<String, Object> map, String key)
	{
		if(map != null && map.get(key) != null)
		{
			return map.get(key) instanceof Boolean ? (Boolean)map.get(key) : Boolean.getBoolean((String)map.get(key));
		}
		return false;
	}

	public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if(json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static Bitmap ZoomBmp(Bitmap bmp, int w)
	{
		float scale = (float)w / bmp.getWidth();
		Matrix m = new Matrix();
		int outW = (int)(bmp.getWidth() * scale);
		int outH = (int)(bmp.getHeight() * scale);
		if(outW >= 1 && outH >= 1)
		{
			m.setScale(scale, scale);
		}
		else if(outW >= 1 && outH < 1)
		{
			m.setScale(scale, 2f / bmp.getHeight());
		}
		else if(outW < 1 && outH >= 1)
		{
			m.setScale(2f / bmp.getWidth(), scale);
		}
		else
		{
			m.setScale(2f / bmp.getWidth(), 2f / bmp.getHeight());
		}

		return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
	}

	public static Bitmap DrawMask(Bitmap dst, Bitmap mask, int alpha)
	{
		Canvas canvas = new Canvas(dst);
		Paint pt = new Paint();
		pt.setAlpha((int)((float)alpha / 100f * 255));
		canvas.drawBitmap(mask, new Matrix(), pt);
		return dst;
	}

	public static Bitmap DecodeRes(Context context, Object img, int outW, int outH)
	{
		if(img != null)
		{
			Bitmap out = null;

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if(img instanceof String)
			{
				BitmapFactory.decodeFile((String)img, opts);
			}
			else if(img instanceof Integer)
			{
				BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
			}

			if(outW < 1)
			{
				outW = opts.outWidth << 1;
			}
			if(outH < 1)
			{
				outH = opts.outHeight << 1;
			}
			opts.inSampleSize = opts.outWidth / outW < opts.outHeight / outH ? opts.outWidth / outW : opts.outHeight / outH;
			if(opts.inSampleSize < 1)
			{
				opts.inSampleSize = 1;
			}

			long maxMem = (long)(Runtime.getRuntime().maxMemory() * (double)MakeBmpV2.MEM_SCALE);
			int bpp = 4;
			long imgMem = opts.outWidth / opts.inSampleSize * opts.outHeight / opts.inSampleSize * bpp;
			if(imgMem > maxMem)
			{
				opts.inSampleSize = (int)Math.ceil(Math.sqrt((long)opts.outWidth * opts.outHeight * bpp / (double)maxMem));
			}

			opts.inJustDecodeBounds = false;
			opts.inDither = true;
			opts.inPreferredConfig = Config.ARGB_8888;
			if(img instanceof String)
			{
				out = ImageUtils.ReadPng((String)img, opts.inSampleSize);
			}
			else if(img instanceof Integer)
			{
				out = BitmapFactory.decodeResource(context.getResources(), (Integer)img, opts);
			}

			return out;
		}

		return null;
	}

	public static Bitmap LoadBitmap(Context context, Object img, int outW, int outH)
	{
		Bitmap out = DecodeRes(context, img, outW, outH);
		if(out != null)
		{
			if(out.getConfig() != Bitmap.Config.ARGB_8888)
			{
				Bitmap temp = out.copy(Config.ARGB_8888, true);
				out.recycle();
				out = null;
				out = temp;
			}
			return out;
		}
		return null;
	}

	public static Bitmap DecodeColorRes(Context context, Object img, int outW, int outH)
	{
		Bitmap out = DecodeRes(context, img, outW, outH);
		if(out != null)
		{
			Bitmap temp = MakeBmp.CreateFixBitmap(out, outW, outH, MakeBmp.POS_CENTER, 0, Config.ARGB_8888);
			out.recycle();
			out = null;
			out = temp;

			return out;
		}
		return null;
	}
}
