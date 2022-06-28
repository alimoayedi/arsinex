package com.arsinex.com.Utilities;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class JSONArrayToArrayList {

    public static ArrayList<Object> convert(JSONArray jsonArray)
    {
        ArrayList<Object> list = new ArrayList<Object>();

        try {
            for (int index=0; index<jsonArray.length(); index++){
                list.add(jsonArray.get(index));
            }
        } catch (JSONException e) {
            Log.d("Error in JSON array to array list conversion",e.toString());
        }
        return list;
    }
}
