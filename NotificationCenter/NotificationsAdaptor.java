package com.arsinex.com.NotificationCenter;

import android.app.Activity;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.arsinex.com.Objects.FAQObject;
import com.arsinex.com.R;
import com.arsinex.com.Support.FAQAdaptor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class NotificationsAdaptor extends RecyclerView.Adapter<NotificationsAdaptor.MyViewHolder> {

    private Activity activity;
    private ArrayList<NotificationObject> notificationsList = new ArrayList<NotificationObject>();
    private OnItemClickListener onItemClickListener;

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgPriority;
        private TextView lblTitle, lblExplaination;

        private OnItemClickListener onItemClickListener;

        MyViewHolder(@NonNull View view, OnItemClickListener onItemClickListener) {
            super(view);
            imgPriority = (ImageView) view.findViewById(R.id.imgPriority);
            lblTitle = (TextView) view.findViewById(R.id.lblTitle);
            lblExplaination = (TextView) view.findViewById(R.id.lblExplaination);

            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onNotificationClicked(view);
        }
    }

    public interface OnItemClickListener {
        void onNotificationClicked(View view);
    }

    public NotificationsAdaptor(Activity activity, ArrayList<NotificationObject> notificationsList, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.notificationsList = notificationsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notification_item, parent, false);
        return new MyViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        NotificationObject notificationObject = notificationsList.get(position);
        if(notificationObject.hasPriority()) {
            holder.imgPriority.setImageDrawable(ContextCompat.getDrawable(activity.getBaseContext(), R.drawable.ic_error));
        } else {
            holder.imgPriority.setImageDrawable(ContextCompat.getDrawable(activity.getBaseContext(), R.drawable.ic_warning));
        }
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

}
