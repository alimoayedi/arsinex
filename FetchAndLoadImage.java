package com.arsinex.com;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.arsinex.com.Utilities.Utils;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import okhttp3.internal.Util;

public class FetchAndLoadImage {

    private Context context;
    private Utils utils = new Utils();

    public FetchAndLoadImage(Context context){
        this.context = context;
    }

    /**
     *
     * @param image_url image URL from any outer source like SERVER or API
     * @param imageView image place holder in view
     * @param sourceName image source name from assets for backup if API or connection failed.
     *
     * Method first checks if the backup asset exists or not, If exists will use it. Otherwise tries
     * to load it from the given url.
     */
    public void setImage(String image_url, @NotNull ImageView imageView, String sourceName) {
        if(TextUtils.isEmpty(sourceName)){ sourceName = ""; }

        // in turkish language there is a conflict between "ı" and "i" in the lower case, they are replaced
        sourceName = utils.standardizeTurkishStrings(sourceName);

        int resourceID = context.getResources().getIdentifier(sourceName, "drawable", context.getPackageName());
        if ( resourceID != 0 ) {  // the resource exists...
            imageView.setBackground(null);
            imageView.setImageResource(resourceID);
        } else {
            Glide.with(context)
                    .load(image_url)
                    .placeholder(R.drawable.ic_image_loading)
                    .centerCrop()
                    .error(R.drawable.ic_image_not_loaded)
                    .into(imageView);
        }
    }

}
