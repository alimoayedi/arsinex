package com.arsinex.com;

public class GetURL {

    private static final String APIUrl_API = "https://arsinex.com/api/";
    private static final String APIUrl_WALLET = "https://arsinex.com/api/WalletApi/";
    private static final String APIUrl_ORDERS = "https://arsinex.com/api/OrdersApi/";

    // Other codes
    public static final int CHECK_INVITATION_CODE = 101;
    public static final int VERSION_CONTROL = 102;
    public static final int KYC_SUBMIT = 103;
    // Login
    public static final int LOGIN_WITH_MAIL = 107;
    public static final int LOGIN_WITH_PHONE = 108;
    // Register with email
    public static final int REGISTER_WITH_EMAIL = 201;
    public static final int REGISTER_EMAIL_EMAIL_VERIFICATION = 202;
    public static final int REGISTER_EMAIL_ADD_PHONE = 203;
    public static final int REGISTER_EMAIL_PHONE_VERIFICATION = 204;
    public static final int REGISTER_EMAIL_EMAIL_RESEND = 205;
    public static final int REGISTER_EMAIL_SMS_RESEND = 206;
    // Register with phone
    public static final int REGISTER_WITH_PHONE = 301;
    public static final int REGISTER_PHONE_PHONE_VERIFICATION = 302;
    public static final int REGISTER_PHONE_ADD_EMAIL = 303;
    public static final int REGISTER_PHONE_EMAIL_VERIFICATION = 304;
    public static final int REGISTER_PHONE_EMAIL_RESEND = 305;
    public static final int REGISTER_PHONE_SMS_RESEND = 306;
    //
    public static final int MARKET_LIST = 401;

    // Wallet
    public static final int WALLET_BALANCE = 402;
    public static final int NET_WORTH_MONEY = 403;
    public static final int TOTAL_NET_WORTH = 404;
    public static final int WALLET_CREDIT_BALANCE = 405;
    public static final int WALLET_CRYPTO_BALANCE = 406;
    public static final int ASSETS_LIST = 407;
    public static final int WITHDRAW_BANK_LIST = 408;
    public static final int TOKEN_NETWORKS = 409;
    public static final int WITHDRAW_CRYPTO = 410;
    // Orders
    public static final int PLACE_BUY_ORDER = 501;
    public static final int PLACE_SELL_ORDER = 502;
    public static final int USER_ORDER_HISTORY = 503;
    public static final int EXCHANGE_INFO = 504;
    public static final int OPEN_ORDERS = 505;
    public static final int CANCEL_ORDER = 506;
    public static final int ORDER_BALANCE = 507;


    public String get(int requestType) {
        switch (requestType){
            // Register with mail
            case REGISTER_WITH_EMAIL:
                return APIUrl_API + "register_with_mail";
            case REGISTER_EMAIL_EMAIL_VERIFICATION:
                return APIUrl_API + "check_user_email_confirmation";
            case REGISTER_EMAIL_ADD_PHONE:
                return APIUrl_API + "add_phone_number";
            case REGISTER_EMAIL_PHONE_VERIFICATION:
                return APIUrl_API + "check_user_phone_confirmation";
            case REGISTER_EMAIL_EMAIL_RESEND:
                return APIUrl_API + "send_email_confirmation_code";
            case REGISTER_EMAIL_SMS_RESEND:
                return APIUrl_API + "sms_resend";
            // Register with phone
            case REGISTER_WITH_PHONE:
                return APIUrl_API + "register_with_phone";
            case REGISTER_PHONE_PHONE_VERIFICATION:
                return APIUrl_API + "user_phone_confirmation";
            case REGISTER_PHONE_ADD_EMAIL:
                return APIUrl_API + "add_email";
            case REGISTER_PHONE_EMAIL_VERIFICATION:
                return APIUrl_API + "user_email_confirmation";
            case REGISTER_PHONE_EMAIL_RESEND:
                return APIUrl_API + "resend_email";
            case REGISTER_PHONE_SMS_RESEND:
                return APIUrl_API + "register_resend_sms";
            // Login
            case LOGIN_WITH_MAIL:
                return APIUrl_API + "login_with_mail";
            case LOGIN_WITH_PHONE:
                return APIUrl_API + "login_with_phone";
            // Others
            case CHECK_INVITATION_CODE:
                return APIUrl_API + "check_invite_code";
            case VERSION_CONTROL:
                return APIUrl_API + "versions";
            case KYC_SUBMIT:
                return APIUrl_API + "kyc";
            // Wallet
            case NET_WORTH_MONEY:
                return APIUrl_WALLET + "net_worth_money";
            case WALLET_BALANCE:
                return APIUrl_WALLET + "wallet_money_balance";
            case TOTAL_NET_WORTH:
                return APIUrl_WALLET + "totall_netWorth";
            case WALLET_CREDIT_BALANCE:
                return APIUrl_WALLET + "wallet_money_balance";
            case WALLET_CRYPTO_BALANCE:
                return APIUrl_WALLET + "wallet_all_crypto_balance";
            case ASSETS_LIST:
                return APIUrl_WALLET + "assets_list";
            case WITHDRAW_BANK_LIST:
                return APIUrl_WALLET + "bank_list_withdraw";
            case TOKEN_NETWORKS:
                return APIUrl_WALLET + "token_networks";
            case WITHDRAW_CRYPTO:
                return APIUrl_WALLET + "withdraw_crypto";
            // Orders
            case PLACE_BUY_ORDER:
                return APIUrl_ORDERS + "buy_order_limit";
            case PLACE_SELL_ORDER:
                return APIUrl_ORDERS + "sell_order_limit";
            case USER_ORDER_HISTORY:
                return APIUrl_ORDERS + "order_history";
            case EXCHANGE_INFO:
                return APIUrl_ORDERS + "market_exchange_info";
            case OPEN_ORDERS:
                return APIUrl_ORDERS + "open_orders";
            case CANCEL_ORDER:
                return APIUrl_ORDERS + "order_cancel";
            case ORDER_BALANCE:
                return  APIUrl_ORDERS + "check_balance_order";
            case MARKET_LIST:
                return APIUrl_API + "market_list";
            default:
                return "ERROR IN API URL";
        }
    }
}
