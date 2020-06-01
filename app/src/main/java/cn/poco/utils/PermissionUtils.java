package cn.poco.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by admin on 2017/12/1.
 */

public class PermissionUtils
{
	public static boolean checkStoragePermission(Context context){
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
		{
			return true;
		}

		return false;
	}

	public static boolean checkAudioPermission(Context context){
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
		{
			return true;
		}

		return false;
	}

	public static boolean checkCameraPermission(Context context){
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
		{
			return true;
		}

		return false;
	}

	public static boolean checkLocationPermission(Context context){
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED)
		{
			return true;
		}

		return false;
	}

	public static boolean checkPhonePermission(Context context){
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)	== PackageManager.PERMISSION_GRANTED)
		{
			return true;
		}

		return false;
	}

	// 请求权限兼容低版本
	public static void requestPermissions(Context context, String... permissions) {
		ActivityCompat.requestPermissions((Activity)context, permissions, 0);
	}

	public static void openPermissionSetting(Context context)
	{
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		Uri uri = Uri.fromParts("package", context.getApplicationContext().getPackageName(), null);
		intent.setData(uri);
		context.startActivity(intent);
	}

	public static void requestInterPermissions(Context context) {
		String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.READ_PHONE_STATE};
		for(String str : permissions)
		{
			if(!chechOpPermission(str) &&
					ContextCompat.checkSelfPermission(context, str) != PackageManager.PERMISSION_GRANTED)
			{
				requestPermissions(context, str);
			}
		}
	}

	public static ArrayList<String> mOpPermissions;

	public static synchronized void setmOpPermissions(String... strings)
	{
		if(mOpPermissions == null)
		{
			mOpPermissions = new ArrayList<>();
		}
		if(strings != null)
		{
			for(String str : strings)
			{
				if(!chechOpPermission(str))
					mOpPermissions.add(str);
			}
		}
	}

	public static boolean chechOpPermission(String permission)
	{
		if(mOpPermissions != null && !TextUtils.isEmpty(permission))
		{
			for(String str : mOpPermissions)
			{
				if(permission.equals(str))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static void requestStoragePermissions(Context context) {
		requestPermissions(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

}
