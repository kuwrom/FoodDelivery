package com.habeshastudio.fooddelivery.remote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.habeshastudio.fooddelivery.R;
import com.habeshastudio.fooddelivery.common.Common;

public class GpsServices extends Service implements LocationListener, Listener {
    private LocationManager mLocationManager = null;
    private static final String TAG = "MyLocationService";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        mLocationManager.addGpsStatusListener(this);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.currentUserLocation = location;
       // data = MainActivity.getData();
//        if (data.isRunning()){
//            currentLat = location.getLatitude();
//            currentLon = location.getLongitude();
//
//            if (data.isFirstTime()){
//                lastLat = currentLat;
//                lastLon = currentLon;
//                data.setFirstTime(false);
//            }
//
//            lastlocation.setLatitude(lastLat);
//            lastlocation.setLongitude(lastLon);
//            double distance = lastlocation.distanceTo(location);
//
//            if (location.getAccuracy() < distance){
//                data.addDistance(distance);
//
//                lastLat = currentLat;
//                lastLon = currentLon;
//            }
//
//            if (location.hasSpeed()) {
//                data.setCurSpeed(location.getSpeed() * 3.6);
//                if(location.getSpeed() == 0){
//                    new isStillStopped().execute();
//                }
//            }
//            data.update();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                updateNotification(true);
//            }
//        }
    }

    @SuppressLint("StringFormatMatches")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }   
       
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }
   
    /* Remove the locationlistener updates when Services is stopped */
    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        stopForeground(true);
    }

    @Override
    public void onGpsStatusChanged(int event) {}

    @Override
    public void onProviderDisabled(String provider) {}
   
    @Override
    public void onProviderEnabled(String provider) {}
   
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

}
