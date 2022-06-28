package com.arsinex.com.Discover;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.arsinex.com.R;
import com.arsinex.com.Utilities.MainActivityUtilities;
import com.arsinex.com.Utilities.NetworkChangeListener;
import com.arsinex.com.Utilities.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DiscoverActivity extends AppCompatActivity implements ArticlesListAdaptor.OnItemClickListener {

    private final static String CAMPUS_URL = "https://campus.arsinex.com/wp-json/wp/v2/posts";
    private final static String CAMPUS_TAG = "campus_tag";

    private DrawerLayout drawerLayout;
    private LinearLayout lyProfile, lyMarket, lyAboutUs, lyWithdraw, lyDeposit, lyDiscover, lyLogout, lyContactUs, lySupport;

    private RecyclerView recyclerNewsList;
    private ArticlesListAdaptor articlesListAdaptor;
    private ArrayList<DiscoverArticleObject> articlesList = new ArrayList<DiscoverArticleObject>();

    private OkHttpClient client = new OkHttpClient();

    private Dialog pleaseWaitDialog;

    private Utils utils = new Utils();
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        InitializeDrawerMenu();

        recyclerNewsList = (RecyclerView) findViewById(R.id.recyclerNewsList);

        // News list recycle view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerNewsList.setLayoutManager(gridLayoutManager);
        recyclerNewsList.setItemAnimator(new DefaultItemAnimator());

        articlesListAdaptor = new ArticlesListAdaptor(this, articlesList, this);
        recyclerNewsList.setAdapter(articlesListAdaptor);

        pleaseWaitDialog = new Dialog(this);
        utils.setupDialog(pleaseWaitDialog, this);
        pleaseWaitDialog.show();

        makeApiRequest();

    }

    private void InitializeDrawerMenu() {
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //Assign Variables
        drawerLayout = findViewById(R.id.drawerLayout);
        lyProfile = (LinearLayout) drawerLayout.findViewById(R.id.lyProfile);
        lyMarket = (LinearLayout) drawerLayout.findViewById(R.id.lyMarket);
        lyAboutUs = (LinearLayout) drawerLayout.findViewById(R.id.lyAboutUs);
        lyWithdraw = (LinearLayout) drawerLayout.findViewById(R.id.lyWithdraw);
        lyDeposit = (LinearLayout) drawerLayout.findViewById(R.id.lyDeposit);
        lyDiscover = (LinearLayout) drawerLayout.findViewById(R.id.lyDiscover);
        lyContactUs = (LinearLayout) drawerLayout.findViewById(R.id.lyContactUs);
        lySupport = (LinearLayout) drawerLayout.findViewById(R.id.lySupport);
        lyLogout = (LinearLayout) drawerLayout.findViewById(R.id.lyLogout);

        lyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickMarket(DiscoverActivity.this);
            }
        });

        lyMarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickMarket(DiscoverActivity.this);
            }
        });

        lyWithdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickWithdraw(DiscoverActivity.this);
            }
        });

        lyDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickDeposit(DiscoverActivity.this);
            }
        });

        lyDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClickDiscover();
            }
        });

        lyAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickAboutUs(DiscoverActivity.this);
            }
        });

        lyContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickContactUs(DiscoverActivity.this);
            }
        });

        lySupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityUtilities.ClickSupport(DiscoverActivity.this);
            }
        });

        lyLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityUtilities.ClickLogout(DiscoverActivity.this);
            }
        });
    }

    public void ClickDiscover() {
        // Recreate activity
        MainActivityUtilities.closeDrawer(drawerLayout);
    }

    public void ClickMenu(View view) {
        //Open drawer
        MainActivityUtilities.openDrawer(this, drawerLayout);
    }

    @Override
    public void onArticleClicked(DiscoverArticleObject articleObject) {
        Intent articleWebViewIntent = new Intent(DiscoverActivity.this, ArticleWebView.class);
        articleWebViewIntent.putExtra("url", articleObject.getLink());
        startActivity(articleWebViewIntent);
    }

    public void makeApiRequest() {
        Request request = new Request.Builder().url(CAMPUS_URL).tag(CAMPUS_TAG).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        call.cancel();
//                        failureRequest(requestType, request);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, final Response response) throws IOException {
                if (response.code() == HttpsURLConnection.HTTP_OK) {
                    String stringResponse = response.body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleResponse(stringResponse);
                            pleaseWaitDialog.dismiss();
                            articlesListAdaptor.notifyDataSetChanged();
                        }
                    });
                } else {
//                    showErrorMessage(response.code());
                }
                response.close();
            }
        });
    }

    private void handleResponse(String response) {
        try {
            int articlesNumbers = new JSONArray(response).length();
            for (int INDEX=0; INDEX < articlesNumbers; INDEX++) {
                JSONObject article = new JSONArray(response).getJSONObject(INDEX);
                articlesList.add(new DiscoverArticleObject(
                        article.getString("date"),
                        article.getString("link"),
                        article.getJSONObject("title").getString("rendered"),
                        article.getJSONObject("content").getString("rendered"),
                        article.getJSONObject("excerpt").getString("rendered"),
                        article.getString("author"),
                        article.getJSONObject("_links").getJSONArray("author").getJSONObject(0).getString("href")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
}