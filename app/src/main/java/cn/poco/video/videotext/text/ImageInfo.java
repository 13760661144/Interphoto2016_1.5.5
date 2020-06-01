package cn.poco.video.videotext.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.Text.Painter;
import cn.poco.framework.MyFramework2App;
import cn.poco.svg.SVG;
import cn.poco.svg.SVGParser;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.FileUtil;

/**
 * 水印图片部分结构体
 */

public class ImageInfo extends WatermarkCommonInfo
{
	public static final int STATIC_IMG = 0;		//静态图
	public static final int ANIM = 1;		//动画
	public static final int STRETCH_HV = 2;	//横向、纵向可拉伸
	public static final int SCREEN_H = 3;	//横向铺满
	public static final int SCREEN_V = 4;	//纵向铺满

	public Object m_pic;
//	public byte[] m_svgStr;
	public String m_imgFile;
	public int paint_color;
	public int type_id;		//文字图片的分类（0 静态图 1动画 2横向可拉伸 3纵向可拉伸 4）

	public int m_specialX;	//部分固定类型的图片与文字间的距离
	public int m_specialY;

	public ImageAnimInfo[] m_animInfo;
	private boolean isEnd = true;
	private String parentPath;
	private boolean m_showLastFrame = false;
	private boolean m_isLastFrame = false;	//是否是出场动画最后一帧
	private int m_videoTime = -1;
	private int m_curTime;
	private int m_startTime;
	public int m_animAlpha = 255;
	private int m_stayTime;		//动画停留时间， 用户控制

	public ArrayList<ElementsAnimInfo> m_eleAnimInfo;
	public float m_animScaleX = 1.0f;
	public float m_animScaleY = 1.0f;
	public float m_animRotate = 0;
	public float m_showAnimDx = 0;
	public float m_showAnimDy = 0;
	public int m_animTextW;

	public void ShowLastFrame(boolean show)
	{
		m_showLastFrame = show;
	}

	public void SetVideoTime(int time)
	{
		m_videoTime = time;
	}

	public void SetCurTime(int time, String parentPath, float width, float height)
	{
//		System.out.println("m_imgFile: " + m_imgFile + "m_eleAnimInfo: " + m_eleAnimInfo.size());
		if((m_eleAnimInfo != null && m_eleAnimInfo.size() > 0) || (m_animInfo != null && m_animInfo.length > 0))
		{
			this.parentPath = parentPath;
			m_curTime = time;
			if(m_videoTime > 0)
			{
				m_startTime = m_videoTime - GetAnimTime();
			}
			if(m_startTime <= 0)
			{
				m_startTime = 0;
			}

			int runTime = m_curTime - m_startTime;		//当前动画时间
			int d = animation_be;	//延迟开始时间
			if(runTime <= d)
			{
				m_pic = null;
				m_animAlpha = 0;
			}
			else
			{
				int animTime = runTime - d;

				if(animTime > CharInfo.ANIM_TIME)
				{
					animTime = animTime - m_stayTime;
					if(animTime <= CharInfo.ANIM_TIME)
					{
						animTime = CharInfo.ANIM_TIME;
					}
				}

				if(m_animInfo != null && m_animInfo.length > 0)
				{
					DoAnimByFrame(animTime);
				}

				if(m_eleAnimInfo != null && m_eleAnimInfo.size() > 0)
				{
					DoAnimByElement(animTime, width, height);
				}
			}
		}
	}

	public void setStartTime(int time)
	{
		m_startTime = time;
	}

	public void setStayTime(int time)
	{
		m_stayTime = time;
	}

	//获取最后一帧
	private Bitmap GetLastFrame()
	{
		Bitmap out = null;
		int frame_time = 0;
		int last_frame_time;
		if(!m_isLastFrame)
		{
			for(int i = 0; i < m_animInfo.length; i ++)
			{
				if(m_animInfo[i] != null)
				{
					for(int k = 0; k < m_animInfo[i].m_frames.length; k ++)
					{
						last_frame_time = frame_time;
						frame_time += m_animInfo[i].m_frames[k].duration;
						if(CharInfo.ANIM_TIME >= last_frame_time && CharInfo.ANIM_TIME < frame_time)
						{
							if(m_animInfo[i].m_pic == null)
							{
								m_animInfo[i].m_pic = (Bitmap)GetBmp(MyFramework2App.getInstance().getApplicationContext(), m_animInfo[i].imageName, false);
							}

							out = m_animInfo[i].GetPicByIndex(k);
							m_isLastFrame = true;
							return out;
						}
					}
				}
			}
		}
		else
		{
			out = (Bitmap)m_pic;
		}
		return out;
	}

	public int GetAnimTime()
	{
		int time = GetFrameAnimTime();
		int time1 = GetEleAnimTime();
		if(time < time1)
		{
			time = time1;
		}
		time += m_stayTime;
		return time;
	}

	public int GetAnimTimeNoStaytime()
	{
		int time = GetFrameAnimTime();
		int time1 = GetEleAnimTime();
		if(time < time1)
		{
			time = time1;
		}
		return time;
	}

	private int GetFrameAnimTime()
	{
		int time = animation_be;
		if(m_animInfo != null)
		{
			for(int i = 0; i < m_animInfo.length; i ++)
			{
				if(m_animInfo[i].m_frames != null)
				{
					for(int j = 0; j < m_animInfo[i].m_frames.length; j ++)
					{
						time += m_animInfo[i].m_frames[j].duration;
					}
				}
			}
		}
		time += 40;
		return time;
	}

	//帧动画
	private void DoAnimByFrame(int animTime)
	{
		m_animAlpha = 255;
		int index = -1;
		int frame_time = 0;
		int last_frame_time;
		if(m_animInfo != null)
		{
			for(int i = 0; i < m_animInfo.length; i ++)
			{
				if(m_animInfo[i] != null)
				{
					for(int k = 0; k < m_animInfo[i].m_frames.length; k ++)
					{
						last_frame_time = frame_time;
						frame_time += m_animInfo[i].m_frames[k].duration;
						if(animTime >= last_frame_time && animTime < frame_time)
						{
							if(CharInfo.ANIM_TIME >= last_frame_time && CharInfo.ANIM_TIME < frame_time)
							{
								m_isLastFrame = true;
							}
							else {
								m_isLastFrame = false;
							}
							if(m_showLastFrame)
							{
								if(animTime <= CharInfo.ANIM_TIME)
								{
									if(m_animInfo[i].m_pic == null)
									{
										m_animInfo[i].m_pic = (Bitmap)GetBmp(MyFramework2App.getInstance().getApplicationContext(), m_animInfo[i].imageName, false);
									}

									m_pic = m_animInfo[i].GetPicByIndex(k);
									index = i;
									break;
								}
							}
							else
							{

								if(m_animInfo[i].m_pic == null)
								{
									m_animInfo[i].m_pic = (Bitmap)GetBmp(MyFramework2App.getInstance().getApplicationContext(), m_animInfo[i].imageName, false);
								}

								m_pic = m_animInfo[i].GetPicByIndex(k);
								index = i;
								break;
							}
						}
					}
					if(index != i)
					{
						if(i == m_animInfo.length - 1)
						{
							if(m_showLastFrame)
							{
								m_pic = GetLastFrame();
							}
							else
							{
								m_pic = null;
							}
						}
						m_animInfo[i].m_pic = null;
					}
					else
					{
						break;
					}
				}
			}
		}
	}

	//元素动画
	private void DoAnimByElement(int animTime, float width, float height)
	{
		int index = animTime / 40;	//当前位置，按照1S 25帧计算；
		int size = m_eleAnimInfo.size();
		ElementsAnimInfo mirror = null;
		int mirrorTime = CharInfo.ANIM_INDEX;
		if(m_animInfo != null && m_animInfo.length > 0)
		{
			int frameTime =	 GetFrameAnimTime();
			mirrorTime = frameTime / 40;
		}

		if(mirrorTime < size)
		{
			mirror = m_eleAnimInfo.get(mirrorTime);
		}
		if(mirror != null)
		{
			if(index <= mirrorTime)
			{
				ElementsAnimInfo info = m_eleAnimInfo.get(index);
				m_showAnimDx = (info.m_x - mirror.m_x) * width;
				m_showAnimDy = (info.m_y - mirror.m_y) * height;
				m_animScaleX = info.m_scaleX;
				m_animScaleY = info.m_scaleY;
				m_animRotate = info.m_rotate;
				m_animAlpha = info.m_alpha;

//				System.out.println("m_showAnimDx: " + m_showAnimDx + "m_showAnimDy: " +
//										   m_showAnimDy + "m_animScaleX: " + m_animScaleX+
//										   "m_animScaleY: " + m_animScaleY+ "m_animAlpha: " + m_animAlpha );
			}
			else if(index > mirrorTime)
			{
				if(m_showLastFrame)
				{
					m_animAlpha = 255;
				}
				else
				{
					if(index < size)
					{
						ElementsAnimInfo info = m_eleAnimInfo.get(index);
						m_showAnimDx = (info.m_x - mirror.m_x) * width;
						m_showAnimDy = (info.m_y - mirror.m_y) * height;
						m_animScaleX = info.m_scaleX;
						m_animScaleY = info.m_scaleY;
						m_animRotate = info.m_rotate;
						m_animAlpha = info.m_alpha;
					}
					else
					{
						m_animAlpha = 0;
					}
				}
			}
		}
		else
		{
			m_animAlpha = 255;
		}


	}

	private int GetEleAnimTime()
	{
		int time = animation_be;
		if(m_eleAnimInfo != null)
		{
			int size = m_eleAnimInfo.size();
			time += (int)(size / 25f * 1000);
		}
		time += 40;
		return time;
	}

	public boolean isEnd()
	{
		return isEnd;
	}

	protected Object GetBmpByInfo(Context context, String parentPath)
	{
		this.parentPath = parentPath;

		paint_color = Painter.SetColorAlpha(m_animAlpha, 255, paint_color);
		int shadowAlpha = Painter.GetAlpha(m_shadowColor);
		m_shadowColor = Painter.SetColorAlpha(m_animAlpha, shadowAlpha, m_shadowColor);
		boolean need_recode = true;
		/*if(!TextUtils.isEmpty(m_imgFile))
		{
			if(m_imgFile.endsWith(".svg"))
			{
				need_recode = false;
			}
		}*/
		return GetBmp(context, m_imgFile, need_recode);
	}

	protected Object GetBmp(Context context, String fileName, boolean need_record)
	{
		Object out = null;
		if(!TextUtils.isEmpty(parentPath))
		{
			String path = parentPath + File.separator + fileName;
			byte[] fileStr = null;
			if(FileUtil.isFileExists(path))
			{
				fileStr = CommonUtils.ReadFile(path);
			}
			else
			{
				fileStr = FileUtil.getAssetsData(context, path);
			}
			if(fileStr != null && fileStr.length > 0)
			{
				if(fileName.endsWith(".svg"))
				{
					int replaceColor = paint_color;
					HashMap<String, Object> shadow = new HashMap<>();
					shadow.put("shadow_c", m_shadowColor);
					shadow.put("shadow_x", m_shadowX);
					shadow.put("shadow_y", m_shadowY);
					shadow.put("shadow_r", m_shadowRadius);
					SVG svg = SVGParser.getSVGFromString(new String(fileStr), null, replaceColor, shadow);
					out = svg.getPicture();
				}
				else
				{
					out = BitmapFactory.decodeByteArray(fileStr, 0, fileStr.length);
				}
				if(need_record)
				{
					if(m_pic != null && m_pic instanceof Bitmap)
					{
						((Bitmap)m_pic).recycle();
						m_pic = null;
					}
					m_pic = out;
				}
			}
		}
		return out;
	}

	public interface AnimCallback
	{
		public void onAnim();

		public void onEnd();
	}
}
