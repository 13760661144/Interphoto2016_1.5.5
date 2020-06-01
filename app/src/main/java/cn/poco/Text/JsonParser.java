package cn.poco.Text;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.FileUtil;
import cn.poco.video.videotext.text.ElementsAnimInfo;
import cn.poco.video.videotext.text.AnimFrameInfo;
import cn.poco.video.videotext.text.CharInfo;
import cn.poco.video.videotext.text.ImageAnimInfo;
import cn.poco.video.videotext.text.ImageInfo;
import cn.poco.video.videotext.text.WaterMarkInfo;

/**
 * 解析json
 * @author pocouser
 *
 */
public class JsonParser
{
	public static final String TAG = "TextInfoFileReader";
    
	public static MyTextInfo parseTextJson(Object json)
    {
    	String jsonString = null;
    	MyTextInfo textInfo = null;
    	if(json == null)
    		return textInfo;
    	if(json instanceof InputStream)
    	{
        	jsonString = readJsonString((InputStream)json);
    	}
    	else
    	{
    		jsonString = (String)json;
    	}
    	if(jsonString != null && !jsonString.equals(""))
    	{
    		textInfo = new MyTextInfo();
    		try
    		{
    			JSONObject jsonObject = new JSONObject(jsonString);
    			JSONArray jsonArray = jsonObject.getJSONArray("ts");
    			int len = jsonArray.length();
    			for(int i = 0; i < len; i ++)
    			{
    				JSONObject item = jsonArray.getJSONObject(i);
    				String classValue = item.getString("class");
    				if(classValue != null)
    				{
    					if(classValue.equals("图片"))
    					{
    						if(textInfo.m_imgInfo == null)
    						{
    							textInfo.m_imgInfo = new ArrayList<ImgInfo>();
    						}
    						textInfo.m_imgInfo.add(parseImg(item));
    					}
    					else if(classValue.equals("文字"))
    					{
    						if(textInfo.m_fontsInfo == null)
    						{
    							textInfo.m_fontsInfo = new ArrayList<FontInfo>();
    						}
    						textInfo.m_fontsInfo.add(parseText(item));
    					}
    				}
    			}
				if(jsonObject.has("t_pos"))
				{
					textInfo.align = jsonObject.getString("t_pos");
				}
				if(jsonObject.has("offset_x"))
				{
					textInfo.offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
				}
				if(jsonObject.has("offset_y"))
				{
					textInfo.offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
				}
				if(jsonObject.has("matrix"))
				{
					String matrix = jsonObject.getString("matrix");
					String[] value = matrix.split(",");
					if(value != null && value.length == 9)
					{
						textInfo.m_matrixValue = new float[9];
						for(int i = 0; i < value.length; i ++)
						{
							textInfo.m_matrixValue[i] = Float.parseFloat(value[i]);
						}
					}
				}
				if(jsonObject.has("alpha"))
				{
					textInfo.m_alpha = jsonObject.getInt("alpha");
				}
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	return textInfo;
    }
    
    private static ImgInfo parseImg(JSONObject jsonObject)
    {
    	ImgInfo out = new ImgInfo();
    	if(jsonObject != null)
    	{
        	try
			{
				if(jsonObject.has("cid"))
				{
					out.m_cid = jsonObject.getInt("cid");
				}
				if(jsonObject.has("shadow_c"))
				{
					out.m_shadowColor = Painter.GetColor(jsonObject.getString("shadow_c"));
					out.m_shadowAlpha = Painter.GetAlpha(out.m_shadowColor);
				}
				if(jsonObject.has("shadow_x"))
				{
					out.m_shadowX = Float.parseFloat(jsonObject.getString("shadow_x"));
				}
				if(jsonObject.has("shadow_y"))
				{
					out.m_shadowY = Float.parseFloat(jsonObject.getString("shadow_y"));
				}
				if(jsonObject.has("shadow_r"))
				{
					out.m_shadowRadius = Float.parseFloat(jsonObject.getString("shadow_r"));
				}
				if(jsonObject.has("nc_color"))
				{
					out.m_ncColor = Integer.parseInt(jsonObject.getString("nc_color"));
				}

				if(jsonObject.has("matrix"))
				{
					String matrix = jsonObject.getString("matrix");
					String[] value = matrix.split(",");
					if(value != null && value.length == 9)
					{
						out.m_matrixValue = new float[9];
						for(int i = 0; i < value.length; i ++)
						{
							out.m_matrixValue[i] = Float.parseFloat(value[i]);
						}
					}
				}

        		out.m_imgFile = jsonObject.getString("file");
        		out.m_con = jsonObject.getString("con");
        		out.m_offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
        		out.m_offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
        		out.m_pos = jsonObject.optString("pos");
				if(jsonObject.has("color"))
				{
					out.paint_color = Painter.GetColor(jsonObject.getString("color"));
					out.baseAlpha = Painter.GetAlpha(out.paint_color);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
    	}
    	return out;
    }
    
    private static FontInfo parseText(JSONObject jsonObject)
    {
    	FontInfo out = new FontInfo();
    	if(jsonObject != null)
    	{
        	try
			{
				if(jsonObject.has("cid"))
				{
					out.m_cid = jsonObject.getInt("cid");
				}
				if(jsonObject.has("shadow_c"))
				{
					out.m_shadowColor = Painter.GetColor(jsonObject.getString("shadow_c"));
					out.m_shadowAlpha = Painter.GetAlpha(out.m_shadowColor);
				}
				if(jsonObject.has("shadow_x"))
				{
					out.m_shadowX = Float.parseFloat(jsonObject.getString("shadow_x"));
				}
				if(jsonObject.has("shadow_y"))
				{
					out.m_shadowY = Float.parseFloat(jsonObject.getString("shadow_y"));
				}
				if(jsonObject.has("shadow_r"))
				{
					out.m_shadowRadius = Float.parseFloat(jsonObject.getString("shadow_r"));
				}
				if(jsonObject.has("nc_color"))
				{
					out.m_ncColor = Integer.parseInt(jsonObject.getString("nc_color"));
				}
				if(jsonObject.has("matrix"))
				{
					String matrix = jsonObject.getString("matrix");
					String[] value = matrix.split(",");
					if(value != null && value.length == 9)
					{
						out.m_matrixValue = new float[9];
						for(int i = 0; i < value.length; i ++)
						{
							out.m_matrixValue[i] = Float.parseFloat(value[i]);
						}
					}
				}
        		out.m_font = jsonObject.getString("font");
        		out.m_fontSize = jsonObject.getString("size");
				if(jsonObject.has("color"))
				{
					out.m_fontColor = Painter.GetColor(jsonObject.getString("color"));
					out.baseAlpha = Painter.GetAlpha(out.m_fontColor);
				}
        		out.m_typeSet = jsonObject.getString("typeset");
        		out.m_con = jsonObject.getString("con");
        		out.m_pos = jsonObject.getString("pos");
        		out.m_align = jsonObject.getString("align");
            	out.m_offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
            	out.m_offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
            	try
            	{
            		String wordspace = jsonObject.getString("wordspace");
            		if(wordspace != null && !"".equals(wordspace))
            		{
            			out.m_wordspace = Integer.parseInt(wordspace);
            		}
            		String verticalspacing = jsonObject.getString("verticalspacing");
            		if(verticalspacing != null && !"".equals(verticalspacing))
            		{
            			out.m_verticalspacing = Integer.parseInt(verticalspacing);
            		}
                	out.m_maxLine = jsonObject.getInt("maxLine");
                	out.m_maxNum = jsonObject.getInt("maxNum");
                	out.m_wenan = new HashMap<String,String>();

        			JSONArray stringArray = jsonObject.getJSONArray("wenan");
        			int len = stringArray.length();
        			for(int i = 0; i < len; i += 2)
        			{
//        				String key = stringArray.getString(i);
        				String value = stringArray.getString(i);
        				if(!value.equals("undefined"))
                        {
                        	out.m_wenan.put(i + "",value);
                        }
        			}
            	}catch(Exception e)
            	{
            	}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
    	}
    	return out;
    }

	public static WaterMarkInfo parseWaterMarkJson(Context context, String jsonName, String res_path)
	{
		if(!TextUtils.isEmpty(res_path) && !TextUtils.isEmpty(jsonName))
		{
			String path = res_path + File.separator + jsonName;
			String jsonStr = readStringFromPath(context, path);
			return parseWaterMarkJson1(context, jsonStr, res_path);
		}
		return null;
	}

	public static WaterMarkInfo parseWaterMarkJson1(Context context, Object json, String res_path)
	{
		String jsonString = null;
		WaterMarkInfo textInfo = null;
		if(json == null)
			return textInfo;
		if(json instanceof InputStream)
		{
			jsonString = readJsonString((InputStream)json);
		}
		else
		{
			jsonString = (String)json;
		}

		if(jsonString != null && !jsonString.equals(""))
		{
			textInfo = new WaterMarkInfo();
			textInfo.res_path = res_path;
			try
			{
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONArray jsonArray = jsonObject.getJSONArray("ts");
				int len = jsonArray.length();
				for(int i = 0; i < len; i ++)
				{
					JSONObject item = jsonArray.getJSONObject(i);
					String classValue = item.getString("class");
					if(classValue != null)
					{
						if(classValue.equals("图片"))
						{
							if(textInfo.m_imgInfo == null)
							{
								textInfo.m_imgInfo = new ArrayList<ImageInfo>();
							}
							textInfo.m_imgInfo.add(parseVideoWatermarkImg(context, item, res_path));
						}
						else if(classValue.equals("文字"))
						{
							if(textInfo.m_fontsInfo == null)
							{
								textInfo.m_fontsInfo = new ArrayList<CharInfo>();
							}
							textInfo.m_fontsInfo.add(parseVideoWatermarkText(item, context, res_path));
						}
					}
				}
				if(jsonObject.has("t_pos"))
				{
					textInfo.align = jsonObject.getString("t_pos");
				}
				if(jsonObject.has("offset_x"))
				{
					textInfo.offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
				}
				if(jsonObject.has("offset_y"))
				{
					textInfo.offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
				}
				if(jsonObject.has("animation_id"))
				{
					textInfo.animation_id = Integer.parseInt(jsonObject.getString("animation_id"));
				}
				if(jsonObject.has("effect_id"))
				{
					textInfo.effect_id = Integer.parseInt(jsonObject.getString("effect_id"));
				}
				if(jsonObject.has("animation_be"))
				{
					textInfo.animation_be = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_be")));
				}
				if(jsonObject.has("animation_time"))
				{
					textInfo.animation_time = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_time")));
				}
				if(jsonObject.has("animation_hold_time"))
				{
					textInfo.animation_hold_time = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_hold_time")));
				}
				if(jsonObject.has("animation_json"))
				{
					String path = jsonObject.getString("animation_json");
					if(!TextUtils.isEmpty(path))
						textInfo.m_animInfo = parseCharAnimInfo(context, res_path + File.separator + path);
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return textInfo;
	}

	private static ArrayList<ElementsAnimInfo> parseCharAnimInfo(Context context, String path)
	{
		ArrayList<ElementsAnimInfo> out = null;
		if(!TextUtils.isEmpty(path))
		{
			out = new ArrayList<>();
			String jsonStr = readStringFromPath(context, path);
			if(!TextUtils.isEmpty(jsonStr))
			{
				try
				{
					JSONArray jsonArr = new JSONArray(jsonStr);
					if(jsonArr != null)
					{
						int len = jsonArr.length();
						ElementsAnimInfo temp;
						JSONObject json1;
						JSONObject json2;
						JSONArray jsonArr1;
						String tempStr;
						for(int i = 0; i < len; i++)
						{
							jsonArr1 = jsonArr.getJSONArray(i);
							int len1 = jsonArr1.length();
							for(int k = 0; k < len1; k ++)
							{
								temp = new ElementsAnimInfo();
								json1 = jsonArr1.getJSONObject(k);
								tempStr = json1.getString("layerId");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.m_layerID = Integer.parseInt(tempStr);
								}
								tempStr = json1.getString("intAlpha");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.m_alpha = Integer.parseInt(tempStr);
								}
								tempStr = json1.getString("rolate");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.m_rotate = Float.parseFloat(tempStr);
								}
								tempStr = json1.getString("scaleX");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.m_scaleX = Float.parseFloat(tempStr);
								}
								tempStr = json1.getString("scaleY");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.m_scaleY = Float.parseFloat(tempStr);
								}
								JSONArray jsonArr2 = json1.getJSONArray("screenPots");
								if(jsonArr2 != null)
								{
									json2 = jsonArr2.getJSONObject(0);
									tempStr = json2.getString("x");
									if(!TextUtils.isEmpty(tempStr))
									{
										temp.m_x = Float.parseFloat(tempStr);
									}
									tempStr = json2.getString("y");
									if(!TextUtils.isEmpty(tempStr))
									{
										temp.m_y = Float.parseFloat(tempStr);
									}
								}
								out.add(temp);
							}
						}
					}

				}catch(JSONException e)
				{
					e.printStackTrace();
				}
			}

		}
		return out;
	}

	/**
	 * 视频文字图片
	 * @param jsonObject
	 * @return
	 */
	private static ImageInfo parseVideoWatermarkImg(Context context, JSONObject jsonObject, String res_path)
	{
		ImageInfo out = new ImageInfo();
		if(jsonObject != null)
		{
			try
			{
				if(jsonObject.has("cid"))
				{
					out.m_cid = jsonObject.getInt("cid");
				}
				if(jsonObject.has("shadow_c"))
				{
					out.m_shadowColor = Painter.GetColor(jsonObject.getString("shadow_c"));
					out.m_shadowAlpha = Painter.GetAlpha(out.m_shadowColor);
				}
				if(jsonObject.has("shadow_x"))
				{
					out.m_shadowX = Float.parseFloat(jsonObject.getString("shadow_x"));
				}
				if(jsonObject.has("shadow_y"))
				{
					out.m_shadowY = Float.parseFloat(jsonObject.getString("shadow_y"));
				}
				if(jsonObject.has("shadow_r"))
				{
					out.m_shadowRadius = Float.parseFloat(jsonObject.getString("shadow_r"));
				}
				if(jsonObject.has("nc_color"))
				{
					out.m_ncColor = Integer.parseInt(jsonObject.getString("nc_color"));
				}
				out.m_con = jsonObject.getString("con");
				out.m_offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
				out.m_offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
				out.m_pos = jsonObject.optString("pos");
				if(jsonObject.has("color"))
				{
					out.paint_color = Painter.GetColor(jsonObject.getString("color"));
					out.baseAlpha = Painter.GetAlpha(out.paint_color);
				}
				if(jsonObject.has("animation_id"))
				{
					out.animation_id = Integer.parseInt(jsonObject.getString("animation_id"));
				}
				if(jsonObject.has("type_id"))
				{
					out.type_id = Integer.parseInt(jsonObject.getString("type_id"));
				}
				if(jsonObject.has("animation_be"))
				{
					out.animation_be = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_be")));
				}
				if(jsonObject.has("animation_time"))
				{
					out.animation_time = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_time")));
				}
				out.m_imgFile = jsonObject.getString("file");
				if(out.type_id == ImageInfo.ANIM)
				{
					String[] str = out.m_imgFile.split(",");
					out.m_animInfo = parseImageAnimInfo(context, str, res_path);
				}
				if(jsonObject.has("special_x"))
				{
					out.m_specialX = Integer.parseInt(jsonObject.getString("special_x"));
				}
				if(jsonObject.has("special_y"))
				{
					out.m_specialY = Integer.parseInt(jsonObject.getString("special_y"));
				}
				if(jsonObject.has("animation_json"))
				{
					String path = jsonObject.getString("animation_json");
					if(!TextUtils.isEmpty(path))
						out.m_eleAnimInfo = parseCharAnimInfo(context, res_path + File.separator + path);
				}
			}
			catch(Exception e)
			{
 				e.printStackTrace();
			}
		}
		return out;
	}

	private static CharInfo parseVideoWatermarkText(JSONObject jsonObject, Context context, String res_path)
	{
		CharInfo out = new CharInfo();
		if(jsonObject != null)
		{
			try
			{
				if(jsonObject.has("cid"))
				{
					out.m_cid = jsonObject.getInt("cid");
				}
				if(jsonObject.has("shadow_c"))
				{
					out.m_shadowColor = Painter.GetColor(jsonObject.getString("shadow_c"));
					out.m_shadowAlpha = Painter.GetAlpha(out.m_shadowColor);
				}
				if(jsonObject.has("shadow_x"))
				{
					out.m_shadowX = Float.parseFloat(jsonObject.getString("shadow_x"));
				}
				if(jsonObject.has("shadow_y"))
				{
					out.m_shadowY = Float.parseFloat(jsonObject.getString("shadow_y"));
				}
				if(jsonObject.has("shadow_r"))
				{
					out.m_shadowRadius = Float.parseFloat(jsonObject.getString("shadow_r"));
				}
				if(jsonObject.has("nc_color"))
				{
					out.m_ncColor = Integer.parseInt(jsonObject.getString("nc_color"));
				}
				out.m_font = jsonObject.getString("font");
				out.m_fontSize = jsonObject.getString("size");
				if(jsonObject.has("color"))
				{
					out.m_fontColor = Painter.GetColor(jsonObject.getString("color"));
					out.baseAlpha = Painter.GetAlpha(out.m_fontColor);
				}
				out.m_typeSet = jsonObject.getString("typeset");
				out.m_con = jsonObject.getString("con");
				out.m_pos = jsonObject.getString("pos");
				out.m_align = jsonObject.getString("align");
				out.m_offsetX = Float.parseFloat(jsonObject.getString("offset_x"));
				out.m_offsetY = Float.parseFloat(jsonObject.getString("offset_y"));
				try
				{
					String wordspace = jsonObject.getString("wordspace");
					if(wordspace != null && !"".equals(wordspace))
					{
						out.m_wordspace = Integer.parseInt(wordspace);
					}
					String verticalspacing = jsonObject.getString("verticalspacing");
					if(verticalspacing != null && !"".equals(verticalspacing))
					{
						out.m_verticalspacing = Integer.parseInt(verticalspacing);
					}
					out.m_maxLine = jsonObject.getInt("maxLine");
					out.m_maxNum = jsonObject.getInt("maxNum");
					out.m_wenan = new HashMap<String,String>();

					JSONArray stringArray = jsonObject.getJSONArray("wenan");
					int len = stringArray.length();
					for(int i = 0; i < len; i += 2)
					{
//        				String key = stringArray.getString(i);
						String value = stringArray.getString(i);
						if(!value.equals("undefined"))
						{
							out.m_wenan.put(i + "",value);
						}
					}
					if(out.m_wenan.size() > 0)
					{
						out.m_showText = out.m_wenan.get(0 + "");
					}
					if(jsonObject.has("animation_id"))
					{
						out.animation_id = Integer.parseInt(jsonObject.getString("animation_id"));
					}
					if(jsonObject.has("animation_be"))
					{
						out.animation_be = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_be")));
					}
					if(jsonObject.has("animation_time"))
					{
						out.animation_time = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_time")));
					}
					if(jsonObject.has("animation_hold_time"))
					{
						out.animation_hold_time = (int)(1000 * Float.parseFloat(jsonObject.getString("animation_hold_time")));
					}
					if(jsonObject.has("animation_json"))
					{
						String path = jsonObject.getString("animation_json");
						if(!TextUtils.isEmpty(path))
							out.m_animInfo = parseCharAnimInfo(context, res_path + File.separator + path);
					}
					if(jsonObject.has("font_x_off"))
					{
						out.fontOffsetX = Float.parseFloat(jsonObject.getString("font_x_off"));
					}
					if(jsonObject.has("font_y_off"))
					{
						out.fontOffsetY = Float.parseFloat(jsonObject.getString("font_y_off"));
					}
					if(jsonObject.has("effect_id"))
					{
						out.effect_id = Integer.parseInt(jsonObject.getString("effect_id"));
					}

				}catch(Exception e)
				{
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return out;
	}

	private static ImageAnimInfo[] parseImageAnimInfo(Context context, String[] path, String res_path)
	{
		ImageAnimInfo[] out = null;
		if(path != null && path.length > 0)
		{
			out = new ImageAnimInfo[path.length];
			for(int k = 0; k < path.length; k ++)
			{
				String jsonStr = readStringFromPath(context, res_path + File.separator + path[k] + ".json");
				if(!TextUtils.isEmpty(jsonStr))
				{
					try
					{
						out[k] = new ImageAnimInfo();
						JSONObject json = new JSONObject(jsonStr);
						JSONArray jsonArr = json.getJSONArray("frames");
						if(jsonArr != null)
						{
							int len = jsonArr.length();
							out[k].m_frames = new AnimFrameInfo[len];
							AnimFrameInfo temp;
							JSONObject json1;
							JSONObject json2;
							String tempStr;
							for(int i = 0; i < len; i++)
							{
								temp = new AnimFrameInfo();
								json1 = jsonArr.getJSONObject(i);

								temp.fileName = json1.getString("filename");
								tempStr = json1.getString("d");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.duration = (int)(Float.parseFloat(tempStr) * 1000);
								}
								json2 = json1.getJSONObject("frame");
								tempStr = json2.getString("x");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.x = Integer.parseInt(tempStr);
								}
								tempStr = json2.getString("y");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.y = Integer.parseInt(tempStr);
								}
								tempStr = json2.getString("w");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.w = Integer.parseInt(tempStr);
								}
								tempStr = json2.getString("h");
								if(!TextUtils.isEmpty(tempStr))
								{
									temp.h = Integer.parseInt(tempStr);
								}
								out[k].m_frames[i] = temp;
							}
						}
						JSONObject json1 = json.getJSONObject("meta");
						JSONObject json2 = json1.getJSONObject("size");
						out[k].imageName = json1.getString("image");
						String tempStr = json2.getString("w");
						if(!TextUtils.isEmpty(tempStr))
						{
							out[k].imageW = Integer.parseInt(tempStr);
						}
						tempStr = json2.getString("h");
						if(!TextUtils.isEmpty(tempStr))
						{
							out[k].imageH = Integer.parseInt(tempStr);
						}
					}catch(JSONException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		return out;
	}

	public static String readStringFromPath(Context context, String path)
	{
		String out = null;
		File file = new File(path);
		if(!file.exists())
		{
			byte[] datas = FileUtil.getAssetsData(context, path);
			if(datas != null && datas.length > 0)
			{
				out = new String(datas);
			}
		}
		else
		{
			byte[] datas = CommonUtils.ReadFile(path);
			if(datas != null && datas.length > 0)
			{
				out = new String(datas);
			}
		}
		return out;
	}
    
    public static String readJsonString(InputStream inputStream)
    {
    	StringBuffer buffer = new StringBuffer();
    	try
    	{
    		InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
    		BufferedReader buffReader = new BufferedReader(reader);
            String line = null;
            while ((line=buffReader.readLine())!=null) {
                buffer.append(line);
            }
    	}catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    	return buffer.toString();
    }
}
