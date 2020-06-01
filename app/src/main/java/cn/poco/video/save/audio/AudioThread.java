package cn.poco.video.save.audio;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.poco.video.NativeUtils;
import cn.poco.video.render.PlayVideoInfo;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.save.SaveParams;
import cn.poco.video.utils.AudioHandler;
import cn.poco.video.utils.FileUtils;

/**
 * Created by: fwc
 * Date: 2017/10/16
 */
public class AudioThread extends Thread {

	private SaveParams mSaveParams;

	private String mMusicPath;

	private String mAudioPath = null;

	private volatile boolean mExited = false;

	public AudioThread(SaveParams saveParams) {
		mSaveParams = saveParams;
	}

	@Override
	public void run() {

		Thread.currentThread().setName("AudioThread");

		try {
			mMusicPath = mSaveParams.musicPath;
			if (mSaveParams.videoVolume > 0) {
				String videoAudioPath = getVideoAudioPath();
				if (videoAudioPath == null) {
					handleVideoSilence();
				} else if (FileUtils.isFileExist(mMusicPath) && mSaveParams.musicVolume != 0) {
					clipMusic();
					if (mMusicPath != null) {
						handleAudioMix(videoAudioPath);
					} else {
						mAudioPath = videoAudioPath;
					}
				} else {
					mAudioPath = videoAudioPath;
				}
			} else {
				handleVideoSilence();
			}
		} catch (Throwable e) {
			mAudioPath = null;
			e.printStackTrace();
		}

		mExited = true;
	}

	private void clipMusic() {
		long videoDuration = getTotalVideoDuration();
		videoDuration = (videoDuration / 1000 + 1) * 1000;
		mMusicPath = AudioHandler.clipMp3(mMusicPath, mSaveParams.musicStart, mSaveParams.musicStart + videoDuration);
	}

	/**
	 * 视频的背景音频和音乐混合处理
	 */
	private void handleAudioMix(String videoAudioPath) {
		String audioPath = AudioHandler.mixAudio(videoAudioPath, mSaveParams.videoVolume, mMusicPath, mSaveParams.musicVolume, true, 0, 1);
		mAudioPath = AudioHandler.audioFade(audioPath, 3);
	}

	/**
	 * 处理视频静音的情况
	 */
	private void handleVideoSilence() {
		if (FileUtils.isFileExist(mMusicPath) && mSaveParams.musicVolume != 0) {
			clipMusic();
			if (mMusicPath != null) {
				String audioPath = mMusicPath;
				if (mSaveParams.musicVolume != 1) {
					audioPath = AudioHandler.volumeAdjust(audioPath, mSaveParams.musicVolume);
				}
				mAudioPath = AudioHandler.audioFade(audioPath, 3);
			}
		}
	}

	/**
	 * 获取所有视频的总时长
	 */
	private long getTotalVideoDuration() {
		// 没有减去视频混合的时间
//		long duration = 0;
//		for (PlayVideoInfo info : mSaveParams.videoInfos) {
//			duration += info.data.duration;
//		}
//
//		return duration;
		return mSaveParams.duration;
	}

	/**
	 * 获取视频的音频文件路径
	 */
	private String getVideoAudioPath() {

		List<AudioInfo> audioInfoList = new ArrayList<>();

		AudioInfo audioInfo;
		int result, transitionId;
		PlayVideoInfo videoInfo;

		String videoAudioPath = null;

		if (mSaveParams.videoInfos.size() > 1) {
			for (int i = 0; i < mSaveParams.videoInfos.size(); i++) {
				videoInfo = mSaveParams.videoInfos.get(i);
				audioInfo = new AudioInfo();
				if (videoInfo.isMute) {
					audioInfo.path = AudioHandler.generateAAC(videoInfo.data.duration);
				} else {
					audioInfo.path = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
					result = NativeUtils.getAACFromVideo(videoInfo.path, audioInfo.path);
					if (result < 0 || !isFileCorrect(audioInfo.path)) {
						audioInfo.path = AudioHandler.generateAAC(videoInfo.data.duration);
					}
				}

				audioInfo.duration = videoInfo.data.duration;
				audioInfoList.add(audioInfo);

				if (i > 0) {
					transitionId = mSaveParams.transitions[i - 1];
					if (TransitionItem.isBlendTransition(transitionId)) {
						handleVideoMix(audioInfoList.get(i - 1), audioInfoList.get(i));
					}
				}
			}

			videoAudioPath = AudioHandler.jointAudio(audioInfoList);
		} else if (!mSaveParams.videoInfos.get(0).isMute) {
			videoAudioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			result = NativeUtils.getAACFromVideo(mSaveParams.videoInfos.get(0).path, videoAudioPath);
			if (result < 0 || !isFileCorrect(videoAudioPath)) {
				FileUtils.delete(videoAudioPath);
				videoAudioPath = null;
			}
		}

		return videoAudioPath;
	}

	@SuppressWarnings("all")
	private static boolean isFileCorrect(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}

		File file = new File(path);
		if (!file.exists() || file.length() == 0) {
			return false;
		}

		return true;
	}

	private void handleVideoMix(AudioInfo audioInfo1, AudioInfo audioInfo2) {
		if (audioInfo1.path != null && audioInfo2.path != null) {
			String clipPath = AudioHandler.clipAudio(audioInfo2.path, 0, 1000);
			double start = (audioInfo1.duration - 1000) * 1d / audioInfo1.duration;
			audioInfo1.path = AudioHandler.mixAudio(audioInfo1.path, 1, clipPath, 1, false, start, 1);
			audioInfo2.path = AudioHandler.clipAudio(audioInfo2.path, 1000, audioInfo2.duration);
			audioInfo2.duration -= 1000;
		}
	}

	@SuppressWarnings("all")
	public String getAudioPath() {
		// 等待退出
//		while (!mExited) {}

		try {
			this.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mAudioPath;
	}
}
