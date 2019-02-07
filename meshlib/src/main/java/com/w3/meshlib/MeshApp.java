package com.w3.meshlib;

import android.app.Application;
import android.content.Context;

import com.w3.meshlib.model.MyObjectBox;
import com.w3.meshlib.model.Routing;
import com.w3.meshlib.model.User;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;

public class MeshApp extends Application {
    private static Context context;
    private static BoxStore boxStore;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        boxStore = MyObjectBox.builder().androidContext(this).build();
        if (BuildConfig.DEBUG) {
            boolean started = new AndroidObjectBrowser(boxStore).start(this);

        }

        boxStore.boxFor(User.class).removeAll();
        boxStore.boxFor(Routing.class).removeAll();
    }

    public static Context getContext(){
        return context;
    }

    public static BoxStore getBoxStore(){
        return boxStore;
    }
}
