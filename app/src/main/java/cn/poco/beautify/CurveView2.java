package cn.poco.beautify;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.poco.image.PocoImageInfo;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * 曲线控件
 */
public class CurveView2 extends LinearLayout
{
	private CurveView m_view;
	protected FrameLayout m_topFr;
	protected LinearLayout m_resetFr;
	protected TextView m_coord;
	private int m_curViewSize;
	protected ArrayList<CurveInfo> m_curInfo = new ArrayList<>();

	public CurveView2(Context context)
	{
		super(context);
		ShareData.InitData(getContext());
		InitUI();
	}

	protected void InitUI()
	{
		this.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams ll;
		FrameLayout.LayoutParams fl;
		int topBarH = ShareData.PxToDpi_xhdpi(40);
		m_topFr = new FrameLayout(getContext());
		m_topFr.setPadding(ShareData.PxToDpi_xhdpi(12), 0, ShareData.PxToDpi_xhdpi(12), 0);
		ll = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, topBarH);
		m_topFr.setLayoutParams(ll);
		this.addView(m_topFr);
		{
			m_resetFr = new LinearLayout(getContext());
			m_resetFr.setOnClickListener(m_btnListener);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			m_resetFr.setLayoutParams(fl);
			m_topFr.addView(m_resetFr);

			{
				ImageView reset = new ImageView(getContext());
				reset.setImageResource(R.drawable.beauty_curve_reset);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				reset.setLayoutParams(ll);
				m_resetFr.addView(reset);

				TextView text = new TextView(getContext());
				text.setText(getContext().getResources().getString(R.string.huanyuan));
				text.setTextColor(Color.WHITE);
				text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
				ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ll.gravity = Gravity.CENTER_VERTICAL;
				ll.leftMargin = ShareData.PxToDpi_xhdpi(10);
				text.setLayoutParams(ll);
				m_resetFr.addView(text);
			}

			m_coord = new TextView(getContext());
//			m_coord.setVisibility(View.GONE);
			m_coord.setText("(0,0)");
			m_coord.setTextColor(Color.WHITE);
			m_coord.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			m_coord.setShadowLayer(3F, 2F, 2F, 0x30000000);
			fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			fl.gravity = Gravity.CENTER;
			m_coord.setLayoutParams(fl);
			m_topFr.addView(m_coord);
		}

		m_curViewSize = ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(100);
		m_view = new CurveView(getContext(), m_curViewSize);
		ll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.CENTER_HORIZONTAL;
		ll.topMargin = ShareData.PxToDpi_xhdpi(20);
		m_view.setLayoutParams(ll);
		this.addView(m_view);
	}

	public int GetCurveViewSize()
	{
		return m_curViewSize;
	}

	public void SetMode(int mode)
	{
		m_view.SetMode(mode);
	}

	public int GetMode() {
		return m_view.m_mode;
	}

	public void SetOnChangeListener(CurveView.Callback cb)
	{
		m_view.setCallback(cb);
	}

	public ArrayList<CurveInfo> GetCurveInfos()
	{
		m_curInfo.clear();

		CurveInfo info = new CurveInfo();
		info.m_colorChannel = PocoImageInfo.ChannelType.AllChannels;
		info.m_ctrlPoints = m_view.rgb.m_ctrlPoints;
		info.m_viewSize = m_curViewSize;
		m_curInfo.add(info);

		info = new CurveInfo();
		info.m_colorChannel = PocoImageInfo.ChannelType.RedChannel;
		info.m_ctrlPoints = m_view.r.m_ctrlPoints;
		info.m_viewSize = m_curViewSize;
		m_curInfo.add(info);

		info = new CurveInfo();
		info.m_colorChannel = PocoImageInfo.ChannelType.GreenChannel;
		info.m_ctrlPoints = m_view.g.m_ctrlPoints;
		info.m_viewSize = m_curViewSize;
		m_curInfo.add(info);

		info = new CurveInfo();
		info.m_colorChannel = PocoImageInfo.ChannelType.BlueChannel;
		info.m_ctrlPoints = m_view.b.m_ctrlPoints;
		info.m_viewSize = m_curViewSize;
		m_curInfo.add(info);

		return m_curInfo;
	}

	public void ShowCoord(boolean show)
	{
		if(!show)
		{
			m_coord.setVisibility(View.GONE);
		}
		else
		{
			m_coord.setVisibility(View.VISIBLE);
		}
	}

	public void SetCoord(int x, int y)
	{
		m_coord.setText("(" + x + "," + y + ")");
	}

	public CurveView.ControlInfo getRedControlInfo() {
	    return m_view.r;
	}

	public CurveView.ControlInfo getGreenControlInfo() {
	    return m_view.g;
	}

	public CurveView.ControlInfo getBlueControlInfo() {
	    return m_view.b;
	}

	public CurveView.ControlInfo getRgbControlInfo() {
		return m_view.rgb;
	}

	public void setCurveInfo(CurveView.ControlInfo red, CurveView.ControlInfo green, CurveView.ControlInfo blue, CurveView.ControlInfo rgb) {
		m_view.r = red;
		m_view.g = green;
		m_view.b = blue;
		m_view.rgb = rgb;
	}




	protected View.OnClickListener m_btnListener = new View.OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			if(v == m_resetFr)
			{
				m_view.Reset();
			}
		}
	};

	public static class  CurveInfo
	{
		public int m_colorChannel;
		public ArrayList<PointF> m_ctrlPoints;
		public int m_viewSize;
	}

}
