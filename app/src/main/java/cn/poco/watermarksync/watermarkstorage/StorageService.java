package cn.poco.watermarksync.watermarkstorage;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.poco.storagesystemlibs.IStorage;
import cn.poco.storagesystemlibs.ServiceStruct;
import cn.poco.system.SysConfig;
import cn.poco.system.WaterInterface;
import cn.poco.tianutils.CommonUtils;
import cn.poco.watermarksync.model.Watermark;

public class StorageService extends WatermarkStorageService {

	private static final int APP_ID = 0x09000000;//主要用于区分不同APP的ID
	private static int AC_ID = 0;


	@Override
	public void onCreate()
	{
		super.onCreate();

		SysConfig.Read(this);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private static int GetAcId() {
		if (AC_ID == 0) {
			AC_ID = ((android.os.Process.myPid() << 16) & 0x00FFFFFF) | APP_ID;
		}
		AC_ID++;
		return AC_ID;
	}

	@Override
	public IStorage GetIStorage()
	{
		return WaterInterface.GetInstance(this);
	}


	public static List<Integer> PushUploadTask(Context context, ArrayList<ServiceStruct> strList) {
		List<Integer> result = new ArrayList<>();
		int out = 0;
		for (ServiceStruct str : strList) {
			if (str != null) {
				out = GetAcId();
				str.mAcId = out;
				((Watermark)(str.mEx)).setAcid(str.mAcId);
				result.add(out);
			}
		}
		Intent it = new Intent(context, StorageService.class);
		it.putExtra("type", StorageService.UPLOAD);
		it.putParcelableArrayListExtra("str", strList);
		context.startService(it);
		return result;
	}


	public static int PushDownloadTask(Context context, ServiceStruct str) {
		int out = 0;

		CommonUtils.MakeFolder(STORAGE_PATH);
		CommonUtils.MakeFolder(STORAGE_TEMP_PATH);

		if (str != null) {
			out = GetAcId();
			str.mAcId = out;
			((Watermark)(str.mEx)).setAcid(str.mAcId);
			Intent it = new Intent(context, StorageService.class);
			it.putExtra("type", StorageService.DOWNLOAD);
			it.putExtra("str", str);
			context.startService(it);
		}

		return out;
	}

	@Override
	protected void OnSingleComplete(int type, ServiceStruct str) {
		super.OnSingleComplete(type, str);
	}

	@Override
	protected void OnSingleFail(int type, ServiceStruct str) {
		super.OnSingleFail(type, str);
	}

	@Override
	protected void OnError(int type, ServiceStruct str) {
		super.OnError(type, str);
	}
}
