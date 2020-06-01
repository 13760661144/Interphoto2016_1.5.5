package cn.poco.video.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.poco.draglistview.MyListItem;

/**
 * Created by lgd on 2017/6/8.
 */

public class VideoDragItem extends MyListItem
{
	private boolean isMusicMode = false;  //是否音乐
	private ImageView m_selectLogo;

	public VideoDragItem(Context context)
	{
		super(context);
	}

	@Override
	public void Init()
	{
		super.Init();
		LayoutParams params;
		m_selectLogo = new ImageView(getContext());
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
//		addView(m_selectLogo, params);
		m_container.addView(m_selectLogo, params);
	}

	public void SetSelectLogo(Bitmap bmp)
	{
		if(bmp == null)
		{
			m_selectLogo.setVisibility(GONE);
		}
		else
		{
			m_selectLogo.setVisibility(VISIBLE);
		}
		m_selectLogo.setImageBitmap(bmp);
	}

	@Override
	public void SetOver(Bitmap overBg) {
		if(isMusicMode){
			m_showTitle = false;
		}
		super.SetOver(overBg);
	}

	@Override
	public void SetOut(Bitmap outBg) {
		if(isMusicMode){
			m_showTitle = true;
		}
		super.SetOut(outBg);
	}
	@Override
	protected void ReLayoutTitleFr(boolean isOver, boolean hasAnim) {
		super.ReLayoutTitleFr(isOver, hasAnim);
	}

	public void setMusicMode(boolean musicMode) {
		isMusicMode = musicMode;
	}
}
