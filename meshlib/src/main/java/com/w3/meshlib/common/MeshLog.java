package com.w3.meshlib.common;

import android.util.Log;

public class MeshLog {

    private static String TAG = "MeshLog";

    public static void e(String msg){
        e(TAG, msg);
    }

    public static void e(String tag, String msg){
        Log.e(tag, msg);
    }

    public static void d(String msg){
        d(TAG, msg);
    }

    public static void d(String tag, String msg){
        Log.d(tag, msg);
    }

    public static void v(String msg){
        v(TAG, msg);
    }

    public static void v(String tag, String msg){
        Log.v(tag, msg);
    }
}
