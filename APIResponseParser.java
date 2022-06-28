package com.arsinex.com;

import com.arsinex.com.enums.RequestType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class APIResponseParser {

    /**
     *
     * @param requestType determines the request type send to the API
     * @param response response comes directly from API
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public HashMap<Object, Object> parseResponse(@NotNull RequestType requestType, String response) throws JSONException {
        if(response == null) { return null; }
        HashMap<Object, Object> hashResponse = new HashMap<Object, Object>();
        JSONObject jsonResponse;
        JSONArray arrayResponse;
        switch (requestType) {
            case LOGIN:
                jsonResponse = new JSONObject(response);
                hashResponse.put("access_token", jsonResponse.getString("access_token"));
                hashResponse.put("token_type", jsonResponse.getString("token_type"));
                break;
            case USER:
                jsonResponse = new JSONObject(response);
                hashResponse.put("name", jsonResponse.getString("name"));
                hashResponse.put("surname", jsonResponse.getString("surname"));
                hashResponse.put("email", jsonResponse.getString("email"));
                hashResponse.put("phone", jsonResponse.getString("phone"));
                hashResponse.put("invite_code", jsonResponse.getString("invite_code"));
                hashResponse.put("mail_confirm", Integer.valueOf(jsonResponse.getString("mail_confirm")));
                hashResponse.put("phone_confirm", Integer.valueOf(jsonResponse.getString("phone_confirm")));
                hashResponse.put("kyc_confirm", Integer.valueOf(jsonResponse.getString("kyc_confirm")));
                break;
            case REGISTER_EMAIL:
            case REGISTER_PHONE:
            case CODE_CONFIRMATION_EMAIL:
            case CODE_CONFIRMATION_PHONE:
            case RESEND_REGISTER_EMAIL:
            case RESEND_REGISTER_PHONE:
            case RESEND_ADD_EMAIL:
            case RESEND_ADD_PHONE:
                jsonResponse = new JSONObject(response);
                if(jsonResponse.has("status")) {
                    hashResponse.put("hasError", true);
                    hashResponse.put("error_msg", jsonResponse.getString("message"));
                } else {
                    hashResponse.put("hasError", false);
                    hashResponse.put("access_token", jsonResponse.getString("access_token"));
                    hashResponse.put("token_type", jsonResponse.getString("token_type"));
                }
                break;
            case ADD_EMAIL:
            case ADD_PHONE:
            case ADD_PHONE_CONFIRM:
            case ADD_EMAIL_CONFIRM:
            case KYC_SUBMIT:
                jsonResponse = new JSONObject(hashResponse);
                if(jsonResponse.has("success")) {
                    hashResponse.put("hasError", false);
                    hashResponse.put("error_msg", jsonResponse.getString("success"));
                } else if(jsonResponse.has("status")) {
                    hashResponse.put("hasError", true);
                    hashResponse.put("error_msg", jsonResponse.getString("message"));
                }
                break;
            case MARKET_LIST:
                arrayResponse = new JSONArray(hashResponse);
                for(int INDEX=0; INDEX<arrayResponse.length(); INDEX++) {
                    hashResponse.put("name", arrayResponse.getJSONObject(INDEX).getString("name"));
                    hashResponse.put("stock", arrayResponse.getJSONObject(INDEX).getString("stock"));
                    hashResponse.put("money", arrayResponse.getJSONObject(INDEX).getString("money"));
                    hashResponse.put("money_prec", arrayResponse.getJSONObject(INDEX).getString("money_prec"));
                }
                break;
            case USER_BALANCE:
                jsonResponse = new JSONObject(hashResponse);
                for (Iterator<String> iterator = jsonResponse.keys(); iterator.hasNext(); ) {
                    String key = iterator.next();
                    hashResponse.put(key, jsonResponse.getJSONObject(key).getString("available"));
                }
                break;
            default:
                break;
        }
        return hashResponse;
    }
}
