package cn.poco.MaterialMgr2;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.resource.BaseRes;
import cn.poco.tianutils.ShareData;

/**
 * Created by admin on 2016/9/12.
 */
public class ManageItemView extends RelativeLayout
{
	private int m_thumbW;
	private int m_thumbH;
	private int m_viewH;
	public ImageView m_thumb;
	public TextView m_title;
	public ImageView m_scanIcon;
	public ImageView m_dragIcon;
	private BaseRes m_data;
	private TextView m_manageText;
	public ManageItemView(Context context)
	{
		super(context);
		InitUI();
	}

	public ManageItemView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		InitUI();
	}

	public ManageItemView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		InitUI();
	}

	public void InitUI()
	{
		m_viewH = ShareData.PxToDpi_xhdpi(130);
		m_thumbH = m_thumbW = ShareData.PxToDpi_xhdpi(80);
		this.setMinimumHeight(m_viewH);
		this.setBackgroundColor(0xff0e0e0e);
		this.setPadding(ShareData.PxToDpi_xhdpi(40), 0, ShareData.PxToDpi_xhdpi(40), 0);

		RelativeLayout.LayoutParams rl;
		m_thumb = new ImageView(getContext());
		m_thumb.setId(R.id.manage_mgr_thumb);
		rl = new LayoutParams(m_thumbW, m_thumbH);
		rl.addRule(RelativeLayout.CENTER_VERTICAL | RelativeLayout.ALIGN_PARENT_LEFT);
//		rl.leftMargin = ShareData.PxToDpi_xhdpi(40);
		m_thumb.setLayoutParams(rl);
		this.addView(m_thumb);

		m_title = new TextView(getContext());
		m_title.setId(R.id.manage_mgr_title);
		m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		m_title.setTextColor(Color.WHITE);
		rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(RIGHT_OF, R.id.manage_mgr_thumb);
		rl.leftMargin = ShareData.PxToDpi_xhdpi(30);
		m_title.setLayoutParams(rl);
		this.addView(m_title);

		m_dragIcon = new ImageView(getContext());
		m_dragIcon.setImageResource(R.drawable.mgr_drag_btn);
		m_dragIcon.setId(R.id.manage_mgr_drag);
		rl.rightMargin = ShareData.PxToDpi_xhdpi(20);
		rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(ALIGN_PARENT_RIGHT);
//		rl.rightMargin = ShareData.PxToDpi_xhdpi(10);
		m_dragIcon.setLayoutParams(rl);
		this.addView(m_dragIcon);

		View line = new View(getContext());
		line.setBackgroundColor(0xff262626);
		line.setId(R.id.manage_mgr_divide);
		rl = new LayoutParams(1, ShareData.PxToDpi_xhdpi(40));
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(LEFT_OF, R.id.manage_mgr_drag);
		rl.rightMargin = ShareData.PxToDpi_xhdpi(10);
		rl.leftMargin = ShareData.PxToDpi_xhdpi(10);
		line.setLayoutParams(rl);
		this.addView(line);

		m_scanIcon = new ImageView(getContext());
		m_scanIcon.setImageResource(R.drawable.mgr_scan_delete);
		m_scanIcon.setVisibility(GONE);
		m_scanIcon.setId(R.id.manage_mgr_scan);
		rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(LEFT_OF, R.id.manage_mgr_divide);
		m_scanIcon.setLayoutParams(rl);
		this.addView(m_scanIcon);

/*
		m_manageText  = new TextView(getContext());
		m_manageText.setText("不可删除");
		m_manageText.setVisibility(GONE);
		m_manageText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
		m_scanIcon.setId(R.id.manage_mgr_manageText);
		rl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addRule(LEFT_OF, R.id.manage_mgr_divide);
		rl.rightMargin = ShareData.PxToDpi_xhdpi(10);
		m_manageText.setLayoutParams(rl);
		this.addView(m_manageText);
*/


	}

	public void SetData(BaseRes data)
	{
		m_data = data;
	}

	public BaseRes GetData()
	{
		return m_data;
	}

	public void Scan(boolean scan)
	{
		if(!scan)
		{
			m_scanIcon.setVisibility(GONE);

			//m_scanIcon.setImageResource(R.drawable.mgr_scan_delete);
		}else {
			m_scanIcon.setVisibility(VISIBLE);
		}

	}
}
