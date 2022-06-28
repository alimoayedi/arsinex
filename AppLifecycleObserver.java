package com.arsinex.com;

import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifecycleObserver implements LifecycleObserver {

    public static final String TAG = AppLifecycleObserver.class.getName();

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() { Log.d("*************************", "Resume from cycle class"); }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() { Log.d("*************************", "Start"); }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d("*************************", "Stop");
    }

}