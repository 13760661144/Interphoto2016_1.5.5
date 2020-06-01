package cn.poco.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import cn.poco.beautify.WaitDialog;
import cn.poco.beautify.WaitDialog1;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;

/**
 * Created by admin on 2017/2/23.
 */

public class VideoViewFr extends FrameLayout
{
	private TextView m_errorTip;
	private ImageView m_playIcon;
	private ImageView m_closeBtn;
	private VideoView m_video;
	private ProgressBar m_progressBar;
	private ProgressBar m_waitDlg;
	private boolean m_uiEnabled = true;
	private String m_videoPath;
	private VideoViewCB m_pageCB;
	private int m_pauseTime = 0;
	public VideoViewFr(Context context)
	{
		this(context, null);
	}

	public VideoViewFr(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public VideoViewFr(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);
		Init();
	}

	private void Init()
	{
		FrameLayout.LayoutParams fl;
		this.setBackgroundColor(Color.BLACK);
		this.setOnClickListener(m_btnLst);

		m_video = new VideoView(getContext());
		m_video.setPadding(0, 0, 0, 4);
		fl = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		m_video.setLayoutParams(fl);
		addView(m_video);

		m_playIcon = new ImageView(getContext());
		m_playIcon.setOnClickListener(m_btnLst);
		m_playIcon.setVisibility(GONE);
		m_playIcon.setImageResource(R.drawable.homepage_video_logo);
		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.CENTER;
		m_playIcon.setLayoutParams(fl);
		addView(m_playIcon);

		int padding = ShareData.PxToDpi_xhdpi(30);
		m_closeBtn = new ImageView(getContext());
		m_closeBtn.setAlpha(0.3f);
		m_closeBtn.setPadding(padding, padding, padding, padding);
		m_closeBtn.setOnClickListener(m_btnLst);
		m_closeBtn.setImageResource(R.drawable.video_close_btn);
		fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		fl.gravity = Gravity.RIGHT | Gravity.TOP;
		m_closeBtn.setLayoutParams(fl);
		addView(m_closeBtn);

		m_progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
		m_progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.web_load_progress));
		m_progressBar.setMax(1000);
		m_progressBar.setMinimumHeight(4);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 4);
		fl.gravity = Gravity.LEFT | Gravity.BOTTOM;
		m_progressBar.setLayoutParams(fl);
		addView(m_progressBar);

		m_errorTip = new TextView(getContext());
		m_errorTip.setVisibility(GONE);
		m_errorTip.setAlpha(0.8f);
		m_errorTip.setTextColor(Color.WHITE);
		m_errorTip.setText(R.string.video_error_tip);
		fl = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 4);
		fl.gravity = Gravity.CENTER;
		m_errorTip.setLayoutParams(fl);
		this.addView(m_errorTip);

		fl = new FrameLayout.LayoutParams(ShareData.PxToDpi_xhdpi(70), ShareData.PxToDpi_xhdpi(70));
		fl.gravity = Gravity.CENTER;
		padding = ShareData.PxToDpi_xhdpi(10);
		m_waitDlg = new ProgressBar(getContext());
		m_waitDlg.setPadding(padding, padding, padding, padding);
		m_waitDlg.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.photofactory_progress));
		m_waitDlg.setLayoutParams(fl);
		this.addView(m_waitDlg);
	}

	private View.OnClickListener m_btnLst = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			if(v == m_playIcon)
			{
				if(m_video != null)
				{
					m_playIcon.setVisibility(GONE);
					m_video.start();
					post(mShowProgress);
				}
			}
			else if(v == m_closeBtn)
			{
				close();
			}

		}
	};

	public void onResume()
	{
		if(m_video != null)
		{
			m_playIcon.setVisibility(GONE);
			m_video.start();
			m_video.seekTo(m_pauseTime);
		}
	}

	public void onPause()
	{
		if(m_video != null)
		{
			m_pauseTime = m_video.getCurrentPosition();
			m_video.pause();
		}
	}

	public void show(final String videoPath)
	{
		SetWaitUI(true);
		m_videoPath = videoPath;
		initVideo();
	}

	protected void SetWaitUI(boolean flag)
	{
		if (flag)
		{
			if (m_waitDlg != null)
			{
				m_waitDlg.setVisibility(VISIBLE);
			}
		}
		else
		{
			if (m_waitDlg != null)
			{
				m_waitDlg.setVisibility(GONE);
			}
		}
	}

	public void close()
	{
		if(m_uiEnabled)
		{
			m_video.setVisibility(GONE);
			m_video.pause();
			m_video.setVisibility(GONE);

			m_video.stopPlayback();
			removeCallbacks(mShowProgress);

			SetWaitUI(false);

			if(m_pageCB != null)
			{
				m_pageCB.onClose();
			}
		}
	}

	private final Runnable mShowProgress = new Runnable() {
		@Override
		public void run() {
			int pos = setProgress();
			if (m_video != null && m_video.isPlaying()) {
				postDelayed(mShowProgress, 1000 - (pos % 1000));
			}
		}
	};

	private int setProgress() {
		if (m_video == null) {
			return 0;
		}
		int position = m_video.getCurrentPosition();
		int duration = m_video.getDuration();
		if (m_progressBar != null) {
			if (duration > 0) {
				// use long to avoid overflow
				long pos = 1000L * position / duration;
				m_progressBar.setProgress( (int) pos);
			}
			int percent = m_video.getBufferPercentage();
			m_progressBar.setSecondaryProgress(percent * 10);
		}
		return position;
	}

	private void initVideo()
	{
		m_video.setVideoPath(m_videoPath);

		m_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				m_playIcon.setVisibility(GONE);
				m_video.start();
				setProgress();

				post(mShowProgress);
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			m_video.setOnInfoListener(new MediaPlayer.OnInfoListener()
			{
				@Override
				public boolean onInfo(MediaPlayer mp, int what, int extra)
				{
					if(what == 3)
					{
						SetWaitUI(false);
					}
					return false;
				}
			});
		}
		else
		{
			SetWaitUI(false);
		}

		m_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				SetWaitUI(false);
				m_playIcon.setVisibility(VISIBLE);
				m_progressBar.setProgress(m_progressBar.getMax());
				removeCallbacks(mShowProgress);
			}
		});

		m_video.setOnErrorListener(new MediaPlayer.OnErrorListener()
		{
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra)
			{
				m_playIcon.setVisibility(GONE);
				SetWaitUI(false);
				m_errorTip.setVisibility(VISIBLE);
				return false;
			}
		});
	}

	public void SetPageCB(VideoViewCB cb)
	{
		m_pageCB = cb;
	}

	public void releaseMem()
	{
		if(m_video != null)
		{
			this.removeView(m_video);
			m_video.stopPlayback();
			m_video = null;
		}
	}

	public interface VideoViewCB
	{
		public void onClose();
	}
}
