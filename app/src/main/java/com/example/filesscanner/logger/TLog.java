package com.example.filesscanner.logger;

import android.util.Log;


public class TLog {
    private final static boolean isLogEnabled = true;

    public static void e(String TAG, String msg){
        if(isLogEnabled)
            Log.e(TAG, msg);
    }

    public static void d(String TAG, String msg){
        if(isLogEnabled)
            Log.d(TAG, msg);
    }

    public static void i(String TAG, String msg){
        if(isLogEnabled)
            Log.i(TAG, msg);
    }
}
