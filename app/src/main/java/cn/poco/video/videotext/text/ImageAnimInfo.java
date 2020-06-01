package cn.poco.video.videotext.text;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by admin on 2017/6/14.
 */

public class ImageAnimInfo
{
	public AnimFrameInfo[] m_frames;
	public String imageName;
	public int imageW;
	public int imageH;
	public Bitmap m_pic;	//大图
	private int m_lastIndex = -1;

	/**
	 * @param index
	 * @return
	 */
	public synchronized Bitmap GetPicByIndex(int index)
	{
		Bitmap out = null;
		if(m_frames != null && index < m_frames.length)
		{
			if(m_lastIndex == index && m_frames[index].m_bmp != null)
			{
				out = m_frames[index].m_bmp;
			}
			else
			{
				for(int i = 0; i < m_frames.length; i ++)
				{
					m_frames[i].m_bmp = null;
				}
				if(m_frames[index].m_bmp == null)
				{
					m_frames[index].m_bmp = makeFrameBmp(m_frames[index]);
				}
				out = m_frames[index].m_bmp;
			}
			m_lastIndex = index;
		}
		return out;
	}

	protected Bitmap makeFrameBmp(AnimFrameInfo info)
	{
		Bitmap out = null;
		if(m_pic != null && !m_pic.isRecycled())
		{
			out = Bitmap.createBitmap(info.w, info.h, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(out);
			canvas.translate(-info.x, -info.y);
			canvas.drawBitmap(m_pic, 0, 0, null);
		}
		return out;
	}
}
