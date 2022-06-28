package com.arsinex.com.marketPackage;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.arsinex.com.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.Nullable;

import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import me.toptas.fancyshowcase.listener.DismissListener;

public class MarketShowcase {

    private Activity activity;
    private View view;

    private FancyShowCaseQueue fancyShowCaseQueue = new FancyShowCaseQueue();
    private FancyShowCaseView fancyShowCaseView;

    public MarketShowcase(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
    }

    public void startShowcase() {
        fancyShowCaseQueue
                .add(welcomeShowcase())
                .add(bottomMenuShowcase())
                .add(menuButtonShowcase());
        fancyShowCaseQueue.show();
    }

    private FancyShowCaseView welcomeShowcase() {
        fancyShowCaseView = new FancyShowCaseView.Builder(activity)
                .customView(R.layout.showcase_layout_2, view -> {
                    view.findViewById(R.id.btnSkip).setOnClickListener(v -> {
                        fancyShowCaseQueue.cancel(true);
                    });
                })
                .delay(1000)
                .build();
        return fancyShowCaseView;
    }

    private FancyShowCaseView bottomMenuShowcase() {
        BottomNavigationView lblBalance = (BottomNavigationView) view.findViewById(R.id.bottomNavBar);
        fancyShowCaseView = new FancyShowCaseView.Builder(activity)
                .focusOn(lblBalance)
                .customView(R.layout.showcase_layout_1, view -> {
                    TextView lblTitle = view.findViewById(R.id.lblTitle);
                    lblTitle.setText(activity.getResources().getString(R.string.showcase_bottom_menu));
                    view.findViewById(R.id.btnSkip).setOnClickListener(v -> {
                        fancyShowCaseQueue.cancel(true);
                    });
                })
                .focusShape(FocusShape.ROUNDED_RECTANGLE)
                .focusBorderSize(5)
                .focusBorderColor(activity.getColor(R.color.mainGold))
                .disableFocusAnimation()
                .clickableOn(lblBalance)
                .build();
        return fancyShowCaseView;
    }

    private FancyShowCaseView menuButtonShowcase() {
        ImageView btnMenu = (ImageView) view.findViewById(R.id.btnMenu);
        fancyShowCaseView = new FancyShowCaseView.Builder(activity)
                .focusOn(btnMenu)
                .customView(R.layout.showcase_layout_1, view -> {
                    TextView lblTitle = view.findViewById(R.id.lblTitle);
                    lblTitle.setText(activity.getResources().getString(R.string.showcase_menu));
                    view.findViewById(R.id.btnSkip).setOnClickListener(v -> {
                        fancyShowCaseQueue.cancel(true);
                    });
                })
                .focusShape(FocusShape.CIRCLE)
                .focusBorderSize(5)
                .focusBorderColor(ContextCompat.getColor(activity.getBaseContext(), R.color.mainGold))
                .focusCircleRadiusFactor(1.5)
                .disableFocusAnimation()
                .closeOnTouch(true)
                .dismissListener(new DismissListener() {
                    @Override
                    public void onDismiss(@Nullable String s) {
                        btnMenu.performClick();
                    }

                    @Override
                    public void onSkipped(@Nullable String s) {

                    }
                })
                .build();
        return fancyShowCaseView;
    }
}
