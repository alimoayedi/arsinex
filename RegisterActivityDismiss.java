package com.arsinex.com;

import android.app.Dialog;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class RegisterActivityDismiss extends GestureDetector.SimpleOnGestureListener{

    private Dialog dialog = null;

    public void setActivity (Dialog dialog){
        this.dialog = dialog;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (e2.getY() - e1.getY() > 20){
            dialog.dismiss();
        }
        return false;
    }
}
