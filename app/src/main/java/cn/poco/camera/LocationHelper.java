package cn.poco.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by zwq on 2016/07/12 11:24.<br/><br/>
 */
public class LocationHelper {

    private static final String TAG = LocationHelper.class.getName();

    private static LocationHelper sLocationHelper;
    private boolean gpsEnable;
    private LocationManager locationManager;
    private MyLocationListener mLocationListener;
    private Location mLocation;

    public static LocationHelper getInstance() {
        if (sLocationHelper == null) {
            synchronized (LocationHelper.class) {
                if (sLocationHelper == null) {
                    sLocationHelper = new LocationHelper();
                }
            }
        }
        return sLocationHelper;
    }

    private LocationHelper() {

    }

    public boolean openGPS(Context context) {
        gpsEnable = false;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(this, GPS模块正常 ,Toast.LENGTH_SHORT).show();
            gpsEnable = true;
        }
//        Toast.makeText(this, 请开启GPS！ ,Toast.LENGTH_SHORT).show();
//        Intent intent = newIntent(Settings.ACTION_SECURITY_SETTINGS);
//        startActivityForResult(intent,0); //此为设置完成后返回到获取界面
        return gpsEnable;
    }

    public void setGpsEnable(boolean enable) {
        gpsEnable = enable;
    }

    public void startLocation(Context context) {
        // 获取位置管理服务
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }

        // 查找到服务信息
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(false);
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息

//        locationManager.addGpsStatusListener(new GpsStatus.Listener() {
//            @Override
//            public void onGpsStatusChanged(int event) {
//                switch (event) {
//                    // 第一次定位
//                    case GpsStatus.GPS_EVENT_FIRST_FIX:
//                        Log.i(TAG, "第一次定位");
//                        break;
//                    // 卫星状态改变
//                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//                        Log.i(TAG, "卫星状态改变");
//                        // 获取当前状态
//                        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
//                        // 获取卫星颗数的默认最大值
//                        int maxSatellites = gpsStatus.getMaxSatellites();
//                        // 创建一个迭代器保存所有卫星
//                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
//                        int count = 0;
//                        while (iters.hasNext() && count <= maxSatellites) {
//                            GpsSatellite gpsSatellite = iters.next();
//                            count++;
//                        }
//                        Log.i(TAG, "搜索到：" + count + "颗卫星");
//                        break;
//                    // 定位启动
//                    case GpsStatus.GPS_EVENT_STARTED:
//                        Log.i(TAG, "定位启动");
//                        break;
//                    // 定位结束
//                    case GpsStatus.GPS_EVENT_STOPPED:
//                        Log.i(TAG, "定位结束");
//                        break;
//                }
//            }
//        });
        // >= 6.0
        if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && */ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//                Log.i(TAG, "requestLocationUpdates: GPS_PROVIDER");
                // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
                mLocationListener = new MyLocationListener();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100 * 1000, 10, mLocationListener);
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 通过GPS获取位置

            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                Log.i(TAG, "requestLocationUpdates: NETWORK_PROVIDER");
                mLocationListener = new MyLocationListener();
                // 设置监听*器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100 * 1000, 10, mLocationListener);
                mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
//            Log.i(TAG, "requestLocationUpdates: "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
//            Log.i(TAG, "requestLocationUpdates: "+e.getMessage());
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
//                Log.i(TAG, "onLocationChanged:"+location.getLongitude()+", "+location.getLatitude());
                mLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            try {
                if (locationManager != null) {
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
//                        Log.i(TAG, "onProviderEnabled:"+location.getLongitude()+", "+location.getLatitude());
                        mLocation = location;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    public Location getLocation() {
        return mLocation;
    }

    public void destroy() {
        if (locationManager != null && mLocationListener != null) {
            try {
                locationManager.removeUpdates(mLocationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mLocationListener = null;
            }
        }

        sLocationHelper = null;
    }
}
