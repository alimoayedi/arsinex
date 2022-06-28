package com.arsinex.com;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final AtomicBoolean applicationBackgrounded = new AtomicBoolean(true);
    private static final long INTERVAL_BACKGROUND_STATE_CHANGE = 750L;
    private static WeakReference<Activity> currentActivityReference;

    private static boolean userLoggedIn = true;
    // TODO define variable as null keep the time user goes to background, on return check difference and ask for finger print if logged in
    // TODO To check if user in logged in keep a variable in the shared prefersnces of the app

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
    }

    private void determineForegroundStatus() {
        if (applicationBackgrounded.get()) {
            MyApplication.onEnterForeground();
            applicationBackgrounded.set(false);
        }
    }

    private void determineBackgroundStatus() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!applicationBackgrounded.get() && currentActivityReference == null) {
                    applicationBackgrounded.set(true);
                    onEnterBackground();
                }
            }
        }, INTERVAL_BACKGROUND_STATE_CHANGE);
    }

    public static void onEnterForeground() {
        //This is where you'll handle logic you want to execute when your application enters the foreground
//        Log.d("*****************new method****************", "no need for finger");
    }

    public static void onEnterBackground() {
        //This is where you'll handle logic you want to execute when your application enters the background
//        Log.d("*****************new method****************", "Background");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        MyApplication.currentActivityReference = new WeakReference<>(activity);
        determineForegroundStatus();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        MyApplication.currentActivityReference = null;
        determineBackgroundStatus();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // if you want to do something when every activity is created, do it here
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // if you want to do something when every activity is started, do it here
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // if you want to do something when every activity is stopped, do it here
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // if you want to do something when an activity saves its instance state, do it here
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // if you want to do something when every activity is destroyed, do it here
    }

}