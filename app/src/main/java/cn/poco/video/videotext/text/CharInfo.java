package cn.poco.video.videotext.text;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Map;

/**
 * 水印文字部分结构体
 */

public class CharInfo extends WatermarkCommonInfo
{
	public static int ANIM_TIME = 3000;	//入场动画时间
	public static int ANIM_INDEX = ANIM_TIME / 40;
	public String m_font;	//字体
	public String m_fontSize;	//字体大小
	public int m_fontColor;
	public int m_verticalspacing = 0;	//行间距
	public int m_wordspace = 2;	//列间距
	public String m_typeSet;	//横排、竖排
	public int m_maxLine = -1;	//最大行数
	public int m_maxNum = -1;	//最大显示文字个数
	public String m_align;
	public Map<String, String> m_wenan;	//默认文案
	public String m_showText;	//显示文案

	public float fontOffsetX;
	public float fontOffsetY;
	public int effect_id;	//类型（是否加下划线）

	public int animation_hold_time; //动画停留在最后一帧的停留时间

	public Paint m_paint;	//画笔缓存
	public ArrayList<ElementsAnimInfo> m_animInfo;

	public float m_animScaleX = 1.0f;
	public float m_animScaleY = 1.0f;
	public float m_animRotate = 0;
	public float m_showTextAnimDx = 0;
	public float m_showTextAnimDy = 0;
	public int m_animTextAlpha = 255;//[0-255]
	public int m_animTextW;

	public boolean isTextAnimEnd = true;
	private int m_curTime;
	private int m_startTime;
	private int m_stayTime;		//动画停留时间， 用户控制

	private boolean m_showLastFrame = false;
	private int m_videoTime = -1;

	public void ShowLastFrame(boolean show)
	{
		m_showLastFrame = show;
	}

	public void SetVideoTime(int time)
	{
		m_videoTime = time;
	}

	public boolean isEnd()
	{
		return isTextAnimEnd;
	}

	/**
	 *
	 * @param time
	 * @param animW	遮罩动画用
	 * @param width
	 * @param height
	 */
	public void SetCurTime(int time, float animW, float width, float height)
	{
		if(animation_id == VideoText.ANIM_NONE)
		{
			return;
		}
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
		if(runTime < d)
		{
			initAnimData();
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
			if(animation_id == VideoText.ANIM1 || animation_id == VideoText.ANIM2)
			{
				float alphaRate = animTime / (float)animation_time;
//				float rate = VideoTextView.m_interPolator.getInterpolation(alphaRate);
				m_animTextW = (int)(animW * alphaRate);
			}
			if(m_animInfo != null && m_animInfo.size() > 0)
			{
				DoAnimByElement(animTime, width, height);
			}
			else
			{
				if(m_showLastFrame)
				{
					initAnimData();
				}
				else
				{
					m_animTextAlpha = 0;
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

	private void DoAnimByElement(int animTime, float width, float height)
	{
		int index = animTime / 40;	//当前位置，按照1S 25帧计算；
		int size = m_animInfo.size();
		ElementsAnimInfo mirror = null;
		if(ANIM_INDEX < size)
		{
			mirror = m_animInfo.get(ANIM_INDEX);
		}
		if(mirror != null)
		{
			if(index <= ANIM_INDEX)
			{
				ElementsAnimInfo info = m_animInfo.get(index);
				m_showTextAnimDx = (info.m_x - mirror.m_x) * width;
				m_showTextAnimDy = (info.m_y - mirror.m_y) * height;
				m_animScaleX = info.m_scaleX;
				m_animScaleY = info.m_scaleY;
				m_animRotate = info.m_rotate;
				m_animTextAlpha = info.m_alpha;
			}
			else if(index > ANIM_INDEX)
			{
				if(m_showLastFrame)
				{
					m_animTextAlpha = 255;
				}
				else
				{
					if(index < size)
					{
						ElementsAnimInfo info = m_animInfo.get(index);
						m_showTextAnimDx = (info.m_x - mirror.m_x) * width;
						m_showTextAnimDy = (info.m_y - mirror.m_y) * height;
						m_animScaleX = info.m_scaleX;
						m_animScaleY = info.m_scaleY;
						m_animRotate = info.m_rotate;
						m_animTextAlpha = info.m_alpha;
					}
					else
					{
						m_animTextAlpha = 0;
					}
				}
			}
		}
		else
		{
			m_animTextAlpha = 255;
		}
	}

	public int GetAnimTime()
	{
		int time = animation_be;
		if(m_animInfo != null)
		{
			int size = m_animInfo.size();
			time += (int)(size / 25f * 1000);
		}
		time += 40 + m_stayTime;
		return time;
	}

	public int GetAnimTimeNoStayTime()
	{
		int time = animation_be;
		if(m_animInfo != null)
		{
			int size = m_animInfo.size();
			time += (int)(size / 25f * 1000);
		}
		time += 40;
		return time;
	}

	public void initAnimData()
	{
		m_animTextAlpha = 0;
		m_animTextW = 0;
		m_animScaleX = 1.0f;
		m_animScaleY = 1.0f;
		m_animRotate = 0;
		m_showTextAnimDx = 0;
		m_showTextAnimDy = 0;
	}
}
