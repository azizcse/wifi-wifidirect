package com.w3.meshlib;

import android.app.Application;
import android.content.Context;

import com.w3.meshlib.model.MyObjectBox;

import io.objectbox.BoxStore;

public class MeshApp extends Application {
    private static Context context;
    private static BoxStore boxStore;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        boxStore = MyObjectBox.builder().androidContext(this).build();
    }

    public static Context getContext(){
        return context;
    }

    public static BoxStore getBoxStore(){
        return boxStore;
    }
}
