package com.example.dell.gps_alarm;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

/**
 * Created by dell on 2017/3/13.
 */

public class Alarm {
    public static MediaPlayer player;
    public static boolean isPlaying = false;

    public static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    public static double calculateDistance(double latitude, double targetLatitude, double longitude, double targetLongitude) {
        double radLat1=rad(latitude), radLat2=rad(targetLatitude);
        double a = radLat1 - radLat2;
        double b = rad(longitude) - rad(targetLongitude);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +
                Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s *6378137.0 ;// EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    public static void ring(Context context) {
        isPlaying=true;
        player = new MediaPlayer();
        player.reset();
        player = MediaPlayer.create(context, R.raw.test);
        player.start();
        return;
    }

    public static void closeRing() {
        if(isPlaying)
        player.stop();
        player.release();
        return;
    }
}

