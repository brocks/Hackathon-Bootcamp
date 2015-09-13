package com.hacks.ram.ramhacks;

import android.app.Application;

import com.facebook.FacebookSdk;

/**
 * Created by Wes on 9/12/2015.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());

    }
}
