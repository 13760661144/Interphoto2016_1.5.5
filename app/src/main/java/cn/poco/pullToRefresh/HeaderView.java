package cn.poco.pullToRefresh;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

public class HeaderView extends FrameLayout
{
	public int def_rotation_res = 0;
	private ProgressBar m_progress;
	private TextView m_stateText;
	private ImageView m_rotationView;
	private TextView m_lastUpdate;
	private String m_updateText = "---";

	private State m_preState = State.NONE;
	private State m_curState = State.NONE;

	private Animation m_rotateUpAnim;
	private Animation m_rotateDownAnim;

	public HeaderView(Context context)
	{
		this(context, null);
	}

	public HeaderView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public HeaderView(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs)
	{
		FrameLayout.LayoutParams params;
		ShareData.InitData((Activity)context);
		
		m_rotateDownAnim = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		m_rotateDownAnim.setDuration(250);
		m_rotateDownAnim.setFillAfter(true);
		
		m_rotateUpAnim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		m_rotateUpAnim.setDuration(250);
		m_rotateUpAnim.setFillAfter(true);
		
		FrameLayout frame = new FrameLayout(context);
		params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.BOTTOM | Gravity.LEFT;
		params.leftMargin = ShareData.PxToDpi_xhdpi(40);
		params.bottomMargin = ShareData.PxToDpi_xhdpi(20);
		params.topMargin = ShareData.PxToDpi_xhdpi(122);
		frame.setLayoutParams(params);
		this.addView(frame);
		{
			m_progress = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
			params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			frame.addView(m_progress, params);
			
			m_rotationView = new ImageView(context);
			m_rotationView.setImageResource(def_rotation_res);
			params = new FrameLayout.LayoutParams(ShareData.PxToDpi_hdpi(63), FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			frame.addView(m_rotationView, params);
		}
		
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
		params.bottomMargin = ShareData.PxToDpi_xhdpi(20);
		container.setLayoutParams(params);
		this.addView(container);
		{
			LinearLayout.LayoutParams lParams;
			
			m_stateText = new TextView(getContext());
			lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lParams.gravity = Gravity.CENTER_HORIZONTAL;
			container.addView(m_stateText, lParams);
			
			m_lastUpdate = new TextView(getContext());
			m_lastUpdate.setText(m_updateText);
			lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			lParams.gravity = Gravity.CENTER_HORIZONTAL;
			lParams.topMargin = ShareData.PxToDpi_xhdpi(10);
			container.addView(m_lastUpdate, lParams);
		}

		//init animation
		//		m_rotateUpAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

	}

	public void setText(CharSequence text)
	{
		m_stateText.setText(text);
	}

	public void setState(State state)
	{
		// TODO Auto-generated method stub
		if(m_curState != state)
		{
			m_preState = m_curState;
			m_curState = state;
			onStateChanged(m_curState);
		}
	}

	public State getState()
	{
		// TODO Auto-generated method stub
		return m_curState;
	}

	public int getContentSize()
	{
		// TODO Auto-generated method stub
		return getHeight();
	}

	public void onPull(int scale)
	{
		// TODO Auto-generated method stub

	}

	private void onStateChanged(State state)
	{
		switch(state)
		{
			case PULL_TO_REFRESH:
			{
				onPullToRefresh();
				break;
			}
			case RELEASE_TO_REFRESH:
			{
				onReleaseToRefresh();
				break;
			}
			case REFRESHING:
			{
				onRefreshing();
				break;
			}
			case RESET:
			{
				onReset();
				break;
			}
			default:
				break;
		}
	}

	protected void onPullToRefresh()
	{
		m_progress.setVisibility(View.INVISIBLE);
		m_rotationView.setVisibility(View.VISIBLE);
		setText(getResources().getString(R.string.PullDownToRefresh));
		if(m_preState == State.RELEASE_TO_REFRESH)
		{
			m_rotationView.clearAnimation();
			m_rotationView.startAnimation(m_rotateDownAnim);
		}
	}

	protected void onReleaseToRefresh()
	{
		m_progress.setVisibility(View.INVISIBLE);
		m_rotationView.setVisibility(View.VISIBLE);
		setText(getResources().getString(R.string.releaseToRefresh));
		m_rotationView.clearAnimation();
		m_rotationView.startAnimation(m_rotateUpAnim);
	}

	protected void onRefreshing()
	{
		m_rotationView.clearAnimation();
		m_rotationView.setVisibility(View.INVISIBLE);
		m_progress.setVisibility(View.VISIBLE);
		setText(getResources().getString(R.string.refreshing));
	}

	protected void onReset()
	{
		m_progress.setVisibility(View.INVISIBLE);
		m_rotationView.setVisibility(View.INVISIBLE);
		setText(getResources().getString(R.string.refreshFinished));
		m_rotationView.clearAnimation();
		Date date = new Date();
		SimpleDateFormat format = (SimpleDateFormat)SimpleDateFormat.getInstance();
		format.applyPattern("yyyy-MM-dd HH:mm:ss");
		m_updateText = getResources().getString(R.string.lastUpdated) + format.format(date);
		m_lastUpdate.setText(m_updateText);
	}

}
