package cn.poco.video.page;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.video.process.ClipMusicTask;
import cn.poco.video.process.OnProcessListener;
import cn.poco.video.site.VideoBeautifySite;

/**
 * Created by: fwc
 * Date: 2018/1/10
 */
public class TestPage extends IPage {

	private Context mContext;

	private VideoBeautifySite mSite;

	public TestPage(Context context, BaseSite site) {
		super(context, site);

		mContext = context;
		mSite = (VideoBeautifySite) site;
	}

	@Override
	public void SetData(HashMap<String, Object> params) {
		String musicPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "audio.mp3";
		String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "output.aac";

		ClipMusicTask task = new ClipMusicTask(musicPath, 3000, 5000, outputPath);
		task.setOnProcessListener(new OnProcessListener() {
			@Override
			public void onStart() {
				Log.d("comit", "onStart");
			}

			@Override
			public void onFinish() {
				Log.d("comit", "onFinish");
			}

			@Override
			public void onError(String message) {
				Log.d("comit", "onError: " + message);
			}
		});
		new Thread(task).start();
	}

	@Override
	public void onBack() {
		mSite.onBack(mContext, null);
	}
}
