package cn.poco.MaterialMgr2;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * 主题列表item
 */
public class ThemeItemView extends FrameLayout
{
	public ImageView m_img;
	private LinearLayout m_stateFr;
	private ImageView m_lockImg;
	protected TextView m_stateText;
	private LinearLayout m_introFr;
	public ImageView m_headImg;
	private TextView m_title;
	private TextView m_intro;
	public int m_viewW;
	public int m_viewH;
	public int m_imgW;
	public int m_imgH;
	public int m_headW;
	public int m_headH;
	private ImageView m_labelView;

	public ThemeItemView(Context context)
	{
		super(context);
	}

	public void InitUI()
	{
		FrameLayout.LayoutParams fl;
		LinearLayout.LayoutParams ll;
		this.setPadding(0, 0, 0, 2);

		m_img = new ImageView(getContext());
		m_img.setScaleType(ImageView.ScaleType.FIT_END);
		fl = new LayoutParams(m_imgW, m_imgH);
		fl.gravity = Gravity.TOP;
//		fl.gravity = Gravity.CENTER;
		fl.topMargin = (int) ((int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f) - RecylerViewV1.m_initPosition*0.06f);
		m_img.setLayoutParams(fl);
		this.addView(m_img);

		ImageView maskLayer  = new ImageView(getContext());
		maskLayer.setBackgroundColor(0x33000000);
		fl = new LayoutParams(m_viewW, m_viewH);
		maskLayer.setLayoutParams(fl);
		this.addView(maskLayer);


		m_labelView  = new ImageView(getContext());
		m_labelView.setImageResource(R.drawable.theme_video_label);
		ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		ll.gravity = Gravity.LEFT;
		m_labelView.setVisibility(GONE);
		m_labelView.setLayoutParams(ll);
		this.addView(m_labelView);

		m_stateFr = new LinearLayout(getContext());
		m_stateFr.setPadding(ShareData.PxToDpi_xhdpi(18), 0, ShareData.PxToDpi_xhdpi(18), 0);
		m_stateFr.setBackgroundColor(0xb3ffc433);
		m_stateFr.setGravity(Gravity.CENTER);
		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ShareData.PxToDpi_xhdpi(48));
		fl.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		m_stateFr.setLayoutParams(fl);
		this.addView(m_stateFr);
		{
;

			m_lockImg = new ImageView(getContext());
			m_lockImg.setImageResource(R.drawable.mgr_lock_img);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.rightMargin = ShareData.PxToDpi_xhdpi(10);
			m_lockImg.setLayoutParams(ll);
			m_stateFr.addView(m_lockImg);


			m_stateText = new TextView(getContext());
			m_stateText.setTextColor(0xffffffff);
			m_stateText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
			m_stateText.setText(getResources().getString(R.string.theme_download));
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_stateText.setLayoutParams(ll);
			m_stateFr.addView(m_stateText);
		}

		m_introFr = new LinearLayout(getContext());
		m_introFr.setOrientation(LinearLayout.VERTICAL);
		m_introFr.setGravity(Gravity.CENTER);
		fl = new LayoutParams(ShareData.PxToDpi_xhdpi(100), ShareData.PxToDpi_xhdpi(48));
		fl.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		m_introFr.setLayoutParams(fl);
		this.addView(m_introFr);
		{
			m_headImg = new ImageView(getContext());
			ll = new LinearLayout.LayoutParams(m_headW, m_headH);
			ll.bottomMargin = ShareData.PxToDpi_xhdpi(20);
			m_headImg.setLayoutParams(ll);
			m_introFr.addView(m_headImg);

			m_title = new TextView(getContext());
			m_title.setTextColor(0xffffffff);
			m_title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			TextPaint tp = m_title.getPaint();
			tp.setFakeBoldText(true);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			m_title.setLayoutParams(ll);
			m_introFr.addView(m_title);

			m_intro = new TextView(getContext());
			m_intro.setTextColor(0xffeeeeee);
			m_intro.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ll.topMargin = ShareData.PxToDpi_xhdpi(20);
			m_intro.setLayoutParams(ll);
			m_introFr.addView(m_intro);
		}

//		View line = new View(getContext());
//		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
//		fl.gravity = Gravity.BOTTOM | Gravity.RIGHT;
//		line.setLayoutParams(fl);
//		this.addView(line);
	}

	public void SetHeadBmp(Bitmap bmp)
	{
		m_headImg.setImageBitmap(bmp);
	}

	public void SetImg(Bitmap bmp)
	{
		m_img.setImageBitmap(bmp);
	}

	public void SetData(ThemeItemInfo info,int position)
	{
		if(info != null)
		{
			if(info.m_themeRes != null)
			{
				if(info.m_lock)
				{
					m_lockImg.setVisibility(VISIBLE);
				}
				else
				{
					m_lockImg.setVisibility(GONE);
				}
				if(info.m_state == ThemeItemInfo.COMPLETE)
				{
					m_stateFr.setBackgroundColor(0xb3333333);
					m_stateText.setText(getResources().getString(R.string.theme_use));
				}
				else
				{
					m_stateFr.setBackgroundColor(0xb3ffc433);
					m_stateText.setText(getResources().getString(R.string.theme_download));
				}
			}
			switch(info.m_type)
			{
				case FILTER:
					m_labelView.setVisibility(GONE);
				{
					m_introFr.setVisibility(VISIBLE);
					LayoutIntroFr1();
					m_headImg.setVisibility(VISIBLE);
					m_intro.setVisibility(View.GONE);
					if(info.m_themeRes != null)
					{
						m_title.setText(info.m_themeRes.m_name);
					}
					break;
				}
				case LIGHT_EFFECT:
				{
					m_introFr.setVisibility(VISIBLE);
					m_labelView.setVisibility(GONE);
					LayoutIntroFr1();
					m_headImg.setVisibility(View.GONE);
					m_intro.setVisibility(VISIBLE);
					if(info.m_themeRes != null)
					{
						m_title.setText(info.m_themeRes.m_name);
						m_intro.setText(info.m_themeRes.m_subTitle);
					}
					break;
				}
				case TEXT:
				{
//					LayoutIntroFr2();
					m_labelView.setVisibility(GONE);
					m_introFr.setVisibility(INVISIBLE);
					m_headImg.setVisibility(View.GONE);
					m_intro.setVisibility(VISIBLE);
					if(info.m_themeRes != null)
					{
						m_title.setText(info.m_themeRes.m_name);
						m_intro.setText(info.m_themeRes.m_subTitle);
					}
					break;
				}
				case  AUDIO_TEXT:
				{
					m_labelView.setVisibility(VISIBLE);
				}


			}
//			layoutImg(position);
		}
	}

	public void layoutImg(int position)
	{
		if(RecylerViewV1.m_refreshState == RecylerViewV1.REFRESH_ING || RecylerViewV1.m_refreshHide)
		{
			return;
		}
		int temp = 0;

//		if(this.getTop() != 0)
//		{
//			temp = (int) this.getY();
//		}
//		else
//		{
//			temp = ShareData.PxToDpi_xhdpi(380)*position;
//		}

		int needReLayoutCount = (int) (RecylerViewV1.m_height/ (ShareData.PxToDpi_xhdpi(380) + 0.5f));
		if(position <= needReLayoutCount)
		{
			temp = ShareData.PxToDpi_xhdpi(380)*position;
			FrameLayout.LayoutParams fl = (LayoutParams) m_img.getLayoutParams();
			if(temp < RecylerViewV1.m_centerHeight)
			{
				if(temp < 0)
				{
					temp = -temp +  RecylerViewV1.m_centerHeight;
				}
				else
				{
					temp =  RecylerViewV1.m_centerHeight - temp;
				}

				fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f + temp*0.06f);
			}
			else
			{
				temp = temp -  RecylerViewV1.m_centerHeight;
				fl.topMargin = (int) (-(ShareData.m_screenWidth - ShareData.PxToDpi_xhdpi(380))/2f -  temp*0.06f);
			}
			m_img.setLayoutParams(fl);
		}
	}

	public void LayoutIntroFr1()
	{
		m_introFr.setGravity(Gravity.CENTER);
		LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		m_introFr.setLayoutParams(fl);
	}

	public void LayoutIntroFr2()
	{
		m_introFr.setGravity(Gravity.LEFT);
		LayoutParams fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.BOTTOM | Gravity.LEFT;
		fl.leftMargin = fl.bottomMargin = ShareData.PxToDpi_xhdpi(20);
		m_introFr.setLayoutParams(fl);
	}

	/**
	 * 固定view的高度
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int heightSpec = MeasureSpec.makeMeasureSpec(m_viewH, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, heightSpec + 2);
		View child = getChildAt(0);
		if(child != null)
		{
			heightSpec = MeasureSpec.makeMeasureSpec(m_imgH, MeasureSpec.EXACTLY);
			int widthSpec = MeasureSpec.makeMeasureSpec(m_imgW, MeasureSpec.EXACTLY);
			child.measure(widthSpec, heightSpec);
		}


	}
}
