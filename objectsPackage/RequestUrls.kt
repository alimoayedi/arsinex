package com.arsinex.com.objectsPackage

object RequestUrls {
    private const val BASE_URL = "https://arsinex.com/"
    const val WEB_SOCKET_URL = "wss://wss.arsinex.com:8443/"

    // app related
    const val VERSION_CONTROL = BASE_URL + "api/v4/mobile/version"

    // user related
    const val USER = BASE_URL + "api/user"
    const val USER_BALANCE = BASE_URL + "api/v4/mobile/user_balance"
    const val WALLET_BALANCE = BASE_URL + "api/v4/mobile/finance/balance"

    // login
    const val LOGIN = BASE_URL + "auth/api/login"

    // registration
    const val REGISTER_EMAIL = BASE_URL + "api/v4/user/register"
    const val RESEND_REGISTER_EMAIL = BASE_URL + "api/v4/user/register"
    const val REGISTER_PHONE = BASE_URL + "api/v4/user/registerPhone"
    const val RESEND_REGISTER_PHONE = BASE_URL + "api/v4/user/registerPhone"
    const val CODE_CONFIRMATION_EMAIL = BASE_URL + "api/v4/user/approve"
    const val CODE_CONFIRMATION_PHONE = BASE_URL + "api/v4/user/approve"

    // email address and phone number verification
    const val ADD_PHONE = BASE_URL + "api/v4/user/sendSMSAddProfile"
    const val RESEND_ADD_PHONE = BASE_URL + "api/v4/user/sendSMSAddProfile"
    const val ADD_EMAIL = BASE_URL + "api/v4/user/sendMailAddProfile"
    const val RESEND_ADD_EMAIL = BASE_URL + "api/v4/user/sendMailAddProfile"
    const val ADD_PHONE_CONFIRM = BASE_URL + "api/v4/user/addTelephoneNumber"
    const val ADD_EMAIL_CONFIRM = BASE_URL + "api/v4/user/addMailAddress"

    // KYC verification
    const val KYC_IMAGE_UPLOAD = BASE_URL + "api/v4/mobile/kyc/image-send"
    const val KYC_SUBMIT = BASE_URL + "api/v4/mobile/kyc/kyc-send"

    // forget password
    const val FORGET_PASSWORD_EMAIL = BASE_URL + "api/v4/user/forgetpasswordmail"
    const val FORGET_PASSWORD_SMS = BASE_URL + "api/v4/user/forgetpasswordphone"

    // markets list
    const val MARKET_LIST = BASE_URL + "api/v4/mobile/market_list"

    // set order
    const val PUT_LIMIT_ORDER = BASE_URL + "api/v4/mobile/order/put_limit"
    const val PUT_MARKET_ORDER = BASE_URL + "api/v4/mobile/order/put_market"
    const val ORDER_FINISHED = BASE_URL + "api/v4/mobile/order/finished"
    const val ORDER_PENDING = BASE_URL + "api/v4/mobile/order/pending"
    const val ORDER_CANCEL = BASE_URL + "api/v4/mobile/order/cancel"

    // prediction related
    const val GET_PREDICTIONS = BASE_URL + "api/v4/prediction/predictions"
    const val SET_BET = BASE_URL + "api/v4/prediction/vote"

    // withdraw and deposit related
    const val GET_ASSETS_LIST = BASE_URL + "api/v4/mobile/finance"
    const val GET_BANKS_LIST = BASE_URL + "api/v4/mobile/finance/banks"
    const val GET_NETWORKS_LIST = BASE_URL + "api/v4/mobile/finance/get-withdraw-crypto"
    const val WITHDRAW_MONEY_REQUEST = BASE_URL + "api/v4/mobile/finance/para-cekme"
    const val GET_COMMISSION = BASE_URL + "api/v4/mobile/finance/getCommissionFee"
    const val SEND_SMS_FOR_WITHDRAW = BASE_URL + "api/v4/mobile/finance/crypto/sendSMS"
    const val SEND_EMAIL_FOR_WITHDRAW = BASE_URL + "api/v4/mobile/finance/crypto/sendMail"
    const val WITHDRAW_CRYPTO_REQUEST = BASE_URL + "api/v4/mobile/finance/para-cekme-crypto"
    const val GET_DEPOSIT_BANK_ACCOUNTS = BASE_URL + "api/v4/mobile/finance/get-withdraw"
    const val GET_WALLET_ADDRESS = BASE_URL + "api/v4/mobile/finance/get-withdraw-crypto-account"

    // transactions history
    const val GET_WITHDRAW_HISTORY = BASE_URL
    const val GET_CRYPTO_DEPOSIT_HISTORY = BASE_URL
}
