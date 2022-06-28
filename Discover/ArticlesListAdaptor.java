package com.arsinex.com.Discover;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import com.arsinex.com.FetchAndLoadImage;
import com.arsinex.com.R;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArticlesListAdaptor extends RecyclerView.Adapter<ArticlesListAdaptor.MyViewHolder> {

    private Activity activity;
    private ArrayList<DiscoverArticleObject> articlesList;
    private final OkHttpClient client = new OkHttpClient();
    private Request request;

    private OnItemClickListener onItemClickListener;

    private FetchAndLoadImage fetchAndLoadImage;

    public ArticlesListAdaptor(Activity activity, ArrayList<DiscoverArticleObject> articlesList, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.articlesList = articlesList;
        this.onItemClickListener = onItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView lblArticleTitle, lblAuthor, lblDate;
        private ImageView imgArticle;

        private OnItemClickListener onItemClickListener;

        MyViewHolder(View view, OnItemClickListener onItemClickListener) {
            super(view);
            imgArticle = view.findViewById(R.id.imgArticle);
            lblArticleTitle = view.findViewById(R.id.lblArticleTitle);
            lblAuthor = view.findViewById(R.id.lblAuthor);
            lblDate = view.findViewById(R.id.lblDate);

            this.onItemClickListener = onItemClickListener;

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onArticleClicked(articlesList.get(getLayoutPosition()));
        }
    }

    public interface OnItemClickListener {
        void onArticleClicked(DiscoverArticleObject articleObject);
    }

    @NonNull
    @NotNull
    @Override
    public ArticlesListAdaptor.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_article_item, parent, false);
        return new MyViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        DiscoverArticleObject articleObject = articlesList.get(position);

        holder.lblArticleTitle.setText(articleObject.getTitle());
        holder.lblDate.setText(articleObject.getDate());

        fetchImage(articleObject.getImage_url(), holder.imgArticle);

        if (articleObject.getAuthor() == null) {
            fetchAuthorInfo(articleObject);
        } else {
            holder.lblAuthor.setText(articleObject.getAuthor());
        }

    }

    @Override
    public int getItemCount() { return articlesList.size(); }

    private void fetchImage(String image_url, ImageView imageView) {
        if (image_url == null) {
            imageView.setImageDrawable(ContextCompat.getDrawable(imageView.getContext(), R.drawable.logo_one_transparent));
        } else {
            fetchAndLoadImage = new FetchAndLoadImage(activity.getBaseContext());
            fetchAndLoadImage.setImage(image_url, imageView, "");
        }
    }

    private void fetchAuthorInfo(DiscoverArticleObject articleObject) {
        Request request = new Request.Builder().url(articleObject.getAuthor_url()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        call.cancel();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                if (response.code() == HttpsURLConnection.HTTP_OK) {
                    String stringResponse = response.body().string();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(stringResponse);
                                articleObject.setAuthor(jsonObject.getString("name"));
                                notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                response.close();
            }
        });

    }
}
