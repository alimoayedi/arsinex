package com.arsinex.com.NotificationCenter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.arsinex.com.KYCVerification.KycVerificationStep1Activity;
import com.arsinex.com.OnDataChangeListener;
import com.arsinex.com.R;
import com.arsinex.com.Utilities.Utils;

import java.util.ArrayList;

public class NotificationCenterActivity extends AppCompatActivity implements NotificationsAdaptor.OnItemClickListener {

    private static final String KYC_NOT_CONFIRMED = "2";

    private ImageView btnBack;

    private ProgressBar progressLoading;
    private RecyclerView recycleNotification;
    private NotificationsAdaptor notificationsAdaptor;

    private ArrayList<NotificationObject> notificationsList = new ArrayList<NotificationObject>();

    private NotificationUtility notificationUtility;
    public static NotificationDataObserver notificationDataObserver = new NotificationDataObserver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        btnBack = (ImageView) findViewById(R.id.btnBack);

        progressLoading = (ProgressBar) findViewById(R.id.progressLoading);
        recycleNotification = (RecyclerView) findViewById(R.id.recycleNotification);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleNotification.setLayoutManager(layoutManager);
        recycleNotification.setItemAnimator(new DefaultItemAnimator());
        notificationsAdaptor = new NotificationsAdaptor(this, notificationsList, this);
        recycleNotification.setAdapter(notificationsAdaptor);

        notificationUtility = new NotificationUtility(this);
        notificationUtility.getKYCStatus();

        notificationDataObserver.setOnChangeListener(new OnDataChangeListener() {
            @Override
            public void onDataChanged(String response) {
                progressLoading.setVisibility(View.GONE);
                if (response.equals(KYC_NOT_CONFIRMED)) {
                    notificationsList.add(new NotificationObject(true));
                    notificationsAdaptor.notifyDataSetChanged();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onNotificationClicked(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent kycVerificationStep1Activity = new Intent(NotificationCenterActivity.this, KycVerificationStep1Activity.class);
                startActivity(kycVerificationStep1Activity);
            }
        });
    }
}