package cn.poco.video.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.poco.interphoto2.BuildConfig;

/**
 * Created by: fwc
 * Date: 2018/1/9
 */
public class ThreadPool {

	private static final int THREAD_COUNT = 3;

	private volatile static ThreadPool sInstance;

	private ExecutorService mService;

	private ThreadPool() {
		mService = Executors.newFixedThreadPool(THREAD_COUNT);
	}

	public static ThreadPool getInstance() {
		if (sInstance == null) {
			synchronized (ThreadPool.class) {
				if (sInstance == null) {
					sInstance = new ThreadPool();
				}
			}
		}

		return sInstance;
	}

	public void execute(Runnable runnable) {
		if (runnable != null) {
			mService.execute(runnable);
		} else if (BuildConfig.DEBUG) {
			throw new RuntimeException("the runnable is null");
		}
	}

	public void release() {
		if (mService != null) {
			mService.shutdown();
			mService = null;
		}

		sInstance = null;
	}
}
