package com.arsinex.com.Utilities;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;
import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static JSONObject decoded(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            return new JSONObject(getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            Log.d("****** Error in JWTUtiles ******", e.toString());
            return null;
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
