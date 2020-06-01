package cn.poco.capture2;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cn.poco.capture2.model.Snippet;
import cn.poco.video.NativeUtils;
import cn.poco.video.save.audio.AudioInfo;
import cn.poco.video.utils.AudioHandler;
import cn.poco.video.utils.FileUtils;
import cn.poco.video.utils.VideoUtils;

/**
 * Created by: fwc
 * Date: 2017/10/26
 */
public class SaveVideoTask extends AsyncTask<Snippet, Void, String> {

	private Uri mVideoUri;
	private OnSaveListener mListener;

	public SaveVideoTask(Uri uri, OnSaveListener listener) {
		mVideoUri = uri;
		mListener = listener;
	}

	@Override
	protected void onPreExecute() {
		if (mListener != null) {
			mListener.onStart();
		}
	}

	@Override
	protected String doInBackground(Snippet... snippets) {

		if (snippets == null || snippets.length == 0) {
			return null;
		}

		if (snippets.length == 1) {
			return outputVideo(snippets[0].path);
		} else {
			return mergeVideo(snippets);
		}
	}

	@Override
	protected void onPostExecute(String s) {
		if (mListener != null) {
			mListener.onFinish(s);
		}
	}

	private String mergeVideo(Snippet[] snippets) {
		List<Segment> segments = new ArrayList<>();
		Segment segment;
		int result;
		for (Snippet snippet : snippets) {
			segment = new Segment();
			segment.h264Path = FileUtils.getTempPath(FileUtils.H264_FORMAT);
			segment.audioPath = FileUtils.getTempPath(FileUtils.AAC_FORMAT);
			segment.duration = VideoUtils.getDurationFromVideo2(snippet.path);
			result = NativeUtils.getH264FromFile(snippet.path, segment.h264Path);
			if (result >= 0) {
				result = NativeUtils.getAACFromVideo(snippet.path, segment.audioPath);
				if (result < 0) {
					segment.audioPath = AudioHandler.generateAAC(segment.duration);
				}

				segments.add(segment);
			}
		}

		if (segments.isEmpty()) {
			return null;
		}

		String audioPath = AudioHandler.jointAudio(changeToAudioInfo(segments));
		String tempH264File = FileUtils.getTempPath(FileUtils.H264_FORMAT);
		for (Segment segment1 : segments) {
			NativeUtils.mixH264(segment1.h264Path, tempH264File);
		}

		String videoPath = FileUtils.getTempPath(FileUtils.MP4_FORMAT);
		result = NativeUtils.muxerMp4WithRotation(tempH264File, audioPath, videoPath, 0, "0");
		if (result < 0) {
			videoPath = null;
		}
		return outputVideo(videoPath);
	}

	private List<AudioInfo> changeToAudioInfo(List<Segment> segments) {

		List<AudioInfo> audioInfos = new ArrayList<>();
		AudioInfo audioInfo;
		for (Segment segment : segments) {
			audioInfo = new AudioInfo();
			audioInfo.path = segment.audioPath;
			audioInfo.duration = segment.duration;

			audioInfos.add(audioInfo);
		}

		return audioInfos;
	}

	private String outputVideo(String videoPath)  {

		String outputPath = videoPath;
		if (mVideoUri != null) {
			try {
				File saveFile = new File(new URI(mVideoUri.toString()));
				outputPath = saveFile.getAbsolutePath();
				boolean result = new File(videoPath).renameTo(saveFile);

				if (!result) {
					cn.poco.album2.utils.FileUtils.copyFile(videoPath, outputPath);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		return outputPath;
	}

	private static class Segment {
		private String h264Path;
		private String audioPath;
		private long duration;
	}

	public interface OnSaveListener {
		void onStart();
		void onFinish(String path);
	}
}
