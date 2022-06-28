package com.arsinex.com;

import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.arsinex.com.marketPackage.AllMarketActivity;

public class DetectSwipeGestureListener extends GestureDetector.SimpleOnGestureListener{

    private Activity activity = null;

    public void setActivity (Activity activity){
        this.activity = activity;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (e1.getY() - e2.getY() > 20){
            // from here the registration activity is called
            Intent registrationActivity = new Intent(activity, AllMarketActivity.class);
            activity.startActivity(registrationActivity);
            activity.finish();
        }
        return false;
    }
}
