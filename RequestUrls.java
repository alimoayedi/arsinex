package com.arsinex.com;

import com.arsinex.com.enums.RequestType;

import org.jetbrains.annotations.NotNull;

public class RequestUrls {

    private static final String BASE_URL = "https://arsinex.com/";

    /**
     *
     * @param requestType gets request type as input
     * @return it returns the base url of your request
     */
    public String getUrl(@NotNull RequestType requestType) {
        String sub_url;
        switch (requestType) {
            case VERSION_CONTROL:
                sub_url = "api/v4/mobile/version";
                break;
            case USER:
                sub_url = "api/user";
                break;
            case USER_BALANCE:
                sub_url = "api/v4/mobile/user_balance";
                break;
            case WALLET_BALANCE:
                sub_url = "api/v4/mobile/finance/balance";
                break;
            case LOGIN:
                sub_url = "auth/api/login";
                break;
            case REGISTER_EMAIL:
            case RESEND_REGISTER_EMAIL:
                sub_url = "api/v4/user/register";
                break;
            case REGISTER_PHONE:
            case RESEND_REGISTER_PHONE:
                sub_url = "api/v4/user/registerPhone";
                break;
            case CODE_CONFIRMATION_EMAIL:
            case CODE_CONFIRMATION_PHONE:
                sub_url = "api/v4/user/approve";
                break;
            case ADD_PHONE:
            case RESEND_ADD_PHONE:
                sub_url = "api/v4/user/sendSMSAddProfile";
                break;
            case ADD_EMAIL:
            case RESEND_ADD_EMAIL:
                sub_url = "api/v4/user/sendMailAddProfile";
                break;
            case ADD_PHONE_CONFIRM:
                sub_url = "api/v4/user/addTelephoneNumber";
                break;
            case ADD_EMAIL_CONFIRM:
                sub_url = "api/v4/user/addMailAddress";
                break;
            case KYC_IMAGE_UPLOAD:
                sub_url = "api/v4/mobile/kyc/image-send";
                break;
            case KYC_SUBMIT:
                sub_url = "api/v4/mobile/kyc/kyc-send";
                break;
            case FORGET_PASSWORD_EMAIL:
                sub_url = "api/v4/user/forgetpasswordmail";
                break;
            case FORGET_PASSWORD_SMS:
                sub_url = "api/v4/user/forgetpasswordphone";
                break;
            case MARKET_LIST:
                sub_url = "api/v4/mobile/market_list";
                break;
            case PUT_LIMIT_ORDER:
                sub_url = "api/v4/mobile/order/put_limit";
                break;
            case PUT_MARKET_ORDER:
                sub_url = "api/v4/mobile/order/put_market";
                break;
            case ORDER_FINISHED:
                sub_url = "api/v4/mobile/order/finished";
                break;
            case ORDER_PENDING:
                sub_url = "api/v4/mobile/order/pending";
                break;
            case ORDER_CANCEL:
                sub_url = "api/v4/mobile/order/cancel";
                break;
            case GET_PREDICTIONS:
                sub_url = "api/v4/prediction/predictions";
                break;
            case SET_BET:
                sub_url = "api/v4/prediction/vote";
                break;
            case GET_ASSETS_LIST:
                sub_url = "api/v4/mobile/finance";
                break;
            case GET_BANKS_LIST:
                sub_url = "api/v4/mobile/finance/banks";
                break;
            case GET_NETWORKS_LIST:
                sub_url = "api/v4/mobile/finance/get-withdraw-crypto";
                break;
            case WITHDRAW_MONEY_REQUEST:
                sub_url = "api/v4/mobile/finance/para-cekme";
                break;
            case GET_COMMISSION:
                sub_url = "api/v4/mobile/finance/getCommissionFee";
                break;
            case SEND_SMS_FOR_WITHDRAW:
                sub_url = "api/v4/mobile/finance/crypto/sendSMS";
                break;
            case SEND_EMAIL_FOR_WITHDRAW:
                sub_url = "api/v4/mobile/finance/crypto/sendMail";
                break;
            case WITHDRAW_CRYPTO_REQUEST:
                sub_url = "api/v4/mobile/finance/para-cekme-crypto";
                break;
            case GET_DEPOSIT_BANK_ACCOUNTS:
                sub_url = "api/v4/mobile/finance/get-withdraw";
                break;
            case GET_WALLET_ADDRESS:
                sub_url = "api/v4/mobile/finance/get-withdraw-crypto-account";
                break;
            default:
                throw new IllegalArgumentException("Invalid Key, Prediction Card Type");
        }
        return BASE_URL + sub_url;
    }
}
