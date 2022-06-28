package com.arsinex.com.enumsPackage

enum class RequestTypes {

    // general app requests
    VERSION_CONTROL,

    // user info requests
    USER,
    USER_BALANCE,
    WALLET_BALANCE,

    // login , register, kyc, forget password
    LOGIN,
    REGISTER_EMAIL,
    REGISTER_PHONE,
    RESEND_REGISTER_EMAIL,
    RESEND_REGISTER_PHONE,
    CODE_CONFIRMATION_EMAIL,
    CODE_CONFIRMATION_PHONE,
    ADD_EMAIL,
    ADD_EMAIL_CONFIRM,
    RESEND_ADD_EMAIL,
    ADD_PHONE,
    ADD_PHONE_CONFIRM,
    RESEND_ADD_PHONE,
    KYC_IMAGE_UPLOAD,
    KYC_SUBMIT,
    FORGET_PASSWORD_EMAIL,
    FORGET_PASSWORD_SMS,

    // list of markets
    MARKET_LIST,

    // orders
    PUT_LIMIT_ORDER,
    PUT_MARKET_ORDER,
    ORDER_FINISHED,
    ORDER_PENDING,
    ORDER_CANCEL,

    // prediction
    GET_PREDICTIONS,
    SET_BET,
    REFRESH_PREDICTION_TIMER,

    // money transfer requests
    GET_ASSETS_LIST,
    GET_BANKS_LIST,
    GET_NETWORKS_LIST,
    WITHDRAW_MONEY_REQUEST,
    GET_COMMISSION,
    SEND_SMS_FOR_WITHDRAW,
    SEND_EMAIL_FOR_WITHDRAW,
    WITHDRAW_CRYPTO_REQUEST,
    GET_DEPOSIT_BANK_ACCOUNTS,
    GET_WALLET_ADDRESS,

    GET_CRYPTO_WITHDRAW_HISTORY,
    GET_CRYPTO_DEPOSIT_HISTORY
}