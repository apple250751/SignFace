package com.kuma.facesignteacher;

import android.location.LocationListener;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kuma on 2018/1/1.
 */

public class GpsLocationListener implements LocationListener {
    private final String TAG = "GpsLocationListener";
    private static final long minTime = 2000;           //gps刷新的最小时间（mini sec）
    private static final float minDistance = 10;        //gps刷新的最短距离

    private double la;
    private double lt;

    private LocationManager locationManager;            //gps定位相关的管理器
    private String provider;                            //最优的gps定位器名称
    private Context context;                            //service

    public GpsLocationListener(Context context) {
        this.context = context;
    }

    public double getLa() {
        return la;
    }

    public double getLt() {
        return lt;
    }

    //定位刷新器，在service里面使用
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        List<String> locationList = locationManager.getProviders(true);
        //使用gps定位
        if (locationList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            //如果gps無法定位，則使用網絡定位
            if (locationList.contains(LocationManager.NETWORK_PROVIDER) && location == null) {
                provider = LocationManager.NETWORK_PROVIDER;
                location = locationManager.getLastKnownLocation(provider);
            }
        }//不存在gps定位，則使用網絡定位
        else if (locationList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
        }
        if (location != null) {
            la = location.getLatitude();
            lt = location.getLongitude();
        } else {
            la = 0;
            lt = 0;
        }
        Log.i(TAG, "la:" + String.valueOf(la) + " ,lt:" + String.valueOf(lt));
        locationManager.requestLocationUpdates(provider, minTime, minDistance, this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}
