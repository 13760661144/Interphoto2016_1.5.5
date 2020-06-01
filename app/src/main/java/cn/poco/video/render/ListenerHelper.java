package cn.poco.video.render;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public class ListenerHelper {

	private List<IVideoPlayer.OnPlayListener> mOnVideoPlayListeners = new ArrayList<>();

	private List<IVideoPlayer.OnProgressListener> mOnProgressListeners = new ArrayList<>();

	private List<IVideoPlayer.OnItemProgressListener> mOnItemProgressListeners = new ArrayList<>();

	public void addOnPlayListener(IVideoPlayer.OnPlayListener listener) {
		if (!mOnVideoPlayListeners.contains(listener)) {
			mOnVideoPlayListeners.add(listener);
		}
	}

	public void removeOnPlayListener(IVideoPlayer.OnPlayListener listener) {
		mOnVideoPlayListeners.remove(listener);
	}

	public void onVideoStart() {
		for (IVideoPlayer.OnPlayListener listener : mOnVideoPlayListeners) {
			listener.onStart();
		}
	}

	public void onVideoResume() {
		for (IVideoPlayer.OnPlayListener listener : mOnVideoPlayListeners) {
			listener.onResume();
		}
	}

	public void onVideoPause() {
		for (IVideoPlayer.OnPlayListener listener : mOnVideoPlayListeners) {
			listener.onPause();
		}
	}

	public void addOnProgressListener(IVideoPlayer.OnProgressListener listener) {
		if (!mOnProgressListeners.contains(listener)) {
			mOnProgressListeners.add(listener);
		}
	}

	public void removeOnProgressListener(IVideoPlayer.OnProgressListener listener) {
		mOnProgressListeners.remove(listener);
	}

	public void onProgressChanged(float progress, boolean isSeekTo) {
		for (IVideoPlayer.OnProgressListener listener : mOnProgressListeners) {
			listener.onChanged(progress, isSeekTo);
		}
	}

	public void addOnItemProgressListener(IVideoPlayer.OnItemProgressListener listener) {
		if (!mOnItemProgressListeners.contains(listener)) {
			mOnItemProgressListeners.add(listener);
		}
	}

	public void removeOnItemProgressListener(IVideoPlayer.OnItemProgressListener listener) {
		mOnItemProgressListeners.remove(listener);
	}

	public void onItemProgressChanged(int index, float progress, boolean isSeekTo) {
		if (progress > 1) {
			progress = 1;
		}

		for (IVideoPlayer.OnItemProgressListener listener : mOnItemProgressListeners) {
			listener.onItemChanged(index, progress, isSeekTo);
		}
	}

	public void clear() {
		mOnVideoPlayListeners.clear();
		mOnProgressListeners.clear();
		mOnItemProgressListeners.clear();
	}
}
