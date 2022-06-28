package com.arsinex.com;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class AlertActivity extends AppCompatActivity {

    private int messageCount = 0;
    private static Uri alarmSound;
    // Vibration pattern long array
    private final long[] pattern = { 100, 300, 300, 300 };
    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        // DEFAULT ALARM SOUND
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // INITIALIZE NOTIFICATION MANAGER
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Button fab = (Button) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNotification();
            }
        });
    }

    protected void showNotification() {
        Log.i("Start", "notification");

        // Invoking the default notification service
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(AlertActivity.this);

        mBuilder.setContentTitle("ProgrammingWizards TV");
        mBuilder.setContentText("We've just released a new android video at our channel");
        mBuilder.setTicker("New Message Alert!");
        mBuilder.setSmallIcon(R.drawable.ic_moon);

        //Increment message count when a new message arrives
        mBuilder.setNumber(++messageCount);
        mBuilder.setSound(alarmSound);
        mBuilder.setVibrate(pattern);

        // Explicit intent to open notifactivity
        Intent i = new Intent(AlertActivity.this, NotificationActivity.class);
        i.putExtra("notificationId", 111);
        i.putExtra("message", "http://www.google.com");

        // Task builder to maintain task for pending intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NotificationActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(i);

        //PASS REQUEST CODE AND FLAG
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(111, mBuilder.build());
    }
}