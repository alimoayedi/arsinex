package com.arsinex.com;

import org.jetbrains.annotations.NotNull;

import okhttp3.MediaType;

public class RequestsClass {

    private static final String BASE_URL = "https://arsinex.com/";

    // Others
//    public static final int VERSION_CONTROL = 901;

    // User
//    public static final int USER = 102;
//    public static final int KYC_STATUS = 103;
//    public static final int REGISTRATION_STATUS = 104;
//    public static final int USER_BALANCE = 105;
//    public static final int FORGET_PASSWORD_EMAIL = 106;
//    public static final int FORGET_PASSWORD_SMS = 107;

    // Login
//    public static final int LOGIN = 201;

    // Register
//    public static final int REGISTER_EMAIL = 301;
//    public static final int REGISTER_PHONE = 302;
//    public static final int CODE_CONFIRMATION_EMAIL = 303;
//    public static final int CODE_CONFIRMATION_PHONE = 304;
//    public static final int ADD_PHONE = 305;
//    public static final int ADD_PHONE_CONFIRM = 306;
//    public static final int ADD_EMAIL = 307;
//    public static final int ADD_EMAIL_CONFIRM = 308;
//    public static final int RESEND_REGISTER_EMAIL = 309;
//    public static final int RESEND_REGISTER_PHONE = 310;
//    public static final int EMAIL_RESEND = 311;
//    public static final int SMS_RESEND = 312;
//    public static final int KYC_IMAGE_UPLOAD = 313;
//    public static final int KYC_SUBMIT = 314;

    // Orders
//    public static final int ORDER_BOOK = 401;
//    public static final int ORDER_FINISHED = 402;
//    public static final int ORDER_PENDING = 403;
//    public static final int ORDER_CANCEL = 404;
//    public static final int PUT_LIMIT_ORDER = 405;
//    public static final int PUT_MARKET_ORDER = 406;

    // Wallet
//    public static final int WALLET_BALANCE = 501;
//    public static final int GET_ASSETS_LIST = 502;
//    public static final int GET_BANKS_LIST = 503;
//    public static final int GET_NETWORKS_LIST = 504;
//    public static final int WITHDRAW_MONEY_REQUEST = 505;
//    public static final int GET_COMMISSION = 506;
//    public static final int SEND_SMS_FOR_WITHDRAW = 507;
//    public static final int SEND_EMAIL_FOR_WITHDRAW = 508;
//    public static final int WITHDRAW_CRYPTO_REQUEST = 509;
//    public static final int GET_DEPOSIT_BANK_ACCOUNTS = 510;
//    public static final int GET_WALLET_ADDRESS = 511;

    // Market
//    public static final int MARKET = 601;

    // Prediction
//    public static final int GET_PREDICTIONS = 701;
    public static final int REFRESH_PREDICTION_TIMER = 702;
//    public static final int SET_BET = 703;


    /**
     *
     * @param requestType gets request type as input
     * @return it returns the base url of your request
     */
    public String getURL(@NotNull int requestType) {
        String sub_url;
        switch (requestType) {
//            case LOGIN:
//                sub_url = "auth/api/login";
//                break;
//            case USER:
//            case KYC_STATUS:
//            case REGISTRATION_STATUS:
//                sub_url = "api/user";
//                break;
//            case KYC_IMAGE_UPLOAD:
//                sub_url = "api/v4/mobile/kyc/image-send";
//                break;
//            case KYC_SUBMIT:
//                sub_url = "api/v4/mobile/kyc/kyc-send";
//                break;
//            case REGISTER_EMAIL:
//            case RESEND_REGISTER_EMAIL:
//                sub_url = "api/v4/user/register";
//                break;
//            case REGISTER_PHONE:
//            case RESEND_REGISTER_PHONE:
//                sub_url = "api/v4/user/registerPhone";
//                break;
//            case CODE_CONFIRMATION_EMAIL:
//            case CODE_CONFIRMATION_PHONE:
//                sub_url = "api/v4/user/approve";
//                break;
//            case ADD_PHONE:
//            case SMS_RESEND:
//                sub_url = "api/v4/user/sendSMSAddProfile";
//                break;
//            case ADD_EMAIL:
//            case EMAIL_RESEND:
//                sub_url = "api/v4/user/sendMailAddProfile";
//                break;
//            case ADD_PHONE_CONFIRM:
//                sub_url = "api/v4/user/addTelephoneNumber";
//                break;
//            case ADD_EMAIL_CONFIRM:
//                sub_url = "api/v4/user/addMailAddress";
//                break;
//            case FORGET_PASSWORD_EMAIL:
//                sub_url = "api/v4/user/forgetpasswordmail";
//                break;
//            case FORGET_PASSWORD_SMS:
//                sub_url = "api/v4/user/forgetpasswordphone";
//                break;
//            case MARKET:
//                sub_url = "api/v4/mobile/market_list";
//                break;
//            case USER_BALANCE:
//                sub_url = "api/v4/mobile/user_balance";
//                break;
//            case ORDER_BOOK:
//                sub_url = "api/v4/mobile/order/book";
//                break;
//            case ORDER_FINISHED:
//                sub_url = "api/v4/mobile/order/finished";
//                break;
//            case ORDER_PENDING:
//                sub_url = "api/v4/mobile/order/pending";
//                break;
//            case ORDER_CANCEL:
//                sub_url = "api/v4/mobile/order/cancel";
//                break;
//            case PUT_LIMIT_ORDER:
//                sub_url = "api/v4/mobile/order/put_limit";
//                break;
//            case PUT_MARKET_ORDER:
//                sub_url = "api/v4/mobile/order/put_market";
//                break;
//            case WALLET_BALANCE:
//                sub_url = "api/v4/mobile/finance/balance";
//                break;
//            case GET_ASSETS_LIST:
//                sub_url = "api/v4/mobile/finance";
//                break;
//            case GET_BANKS_LIST:
//                sub_url = "api/v4/mobile/finance/banks";
//                break;
//            case GET_NETWORKS_LIST:
//                sub_url = "api/v4/mobile/finance/get-withdraw-crypto";
//                break;
//            case WITHDRAW_MONEY_REQUEST:
//                sub_url = "api/v4/mobile/finance/para-cekme";
//                break;
//            case GET_COMMISSION:
//                sub_url = "api/v4/mobile/finance/getCommissionFee";
//                break;
//            case SEND_SMS_FOR_WITHDRAW:
//                sub_url = "api/v4/mobile/finance/crypto/sendSMS";
//                break;
//            case SEND_EMAIL_FOR_WITHDRAW:
//                sub_url = "api/v4/mobile/finance/crypto/sendMail";
//                break;
//            case WITHDRAW_CRYPTO_REQUEST:
//                sub_url = "api/v4/mobile/finance/para-cekme-crypto";
//                break;
//            case GET_DEPOSIT_BANK_ACCOUNTS:
//                sub_url = "api/v4/mobile/finance/get-withdraw";
//                break;
//            case GET_WALLET_ADDRESS:
//                sub_url = "api/v4/mobile/finance/get-withdraw-crypto-account";
//                break;
//            case VERSION_CONTROL:
//                sub_url = "api/v4/mobile/version";
//                break;
//            case GET_PREDICTIONS:
//                sub_url = "api/v4/prediction/predictions";
//                break;
            case REFRESH_PREDICTION_TIMER:
                sub_url = "api/v4/prediction/lock";
                break;
//            case SET_BET:
//                sub_url = "api/v4/prediction/vote";
//                break;
            default:
                throw new IllegalArgumentException("Invalid Key, Prediction Card Type");
        }
        return BASE_URL + sub_url;
    }
}
