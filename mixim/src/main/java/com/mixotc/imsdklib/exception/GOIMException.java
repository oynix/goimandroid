package com.mixotc.imsdklib.exception;

public class GOIMException extends Exception {
    protected int errorCode = -1;
    private static final long serialVersionUID = 1L;

    public GOIMException() {
    }

    public GOIMException(String message) {
        super(message);
    }

    public GOIMException(String message, Throwable throwable) {
        super(message);
        super.initCause(throwable);
    }

    public GOIMException(int code, String message) {
        super(message);
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int code) {
        errorCode = code;
    }

//    public static String getErrorTextFromCode(int code, Context context) {
//        if (context == null) {
//            context = MyApplication.getAppContext();
//        }
//        String error = context.getString(R.string.exception_unknown);
//        switch (code) {
//            case ErrorType.ERROR_EXCEPTION_UNKNOWN_ERROR:
//                error = context.getString(R.string.exception_unknown);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING:
//                error = context.getString(R.string.exception_service_not_running);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SERVICE_ERROR:
//                error = context.getString(R.string.exception_service_remote_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_PHONE_NUMBER_OR_EMAIL:
//                error = context.getString(R.string.exception_empty_phone_or_email);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_USERNAME:
//                error = context.getString(R.string.exception_empty_username);
//                break;
//            case ErrorType.ERROR_EXCEPTION_LOGIN_ERROR:
//                error = context.getString(R.string.exception_login_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION:
//                error = context.getString(R.string.exception_server_connection_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_UNABLE_CONNECT_TO_SERVER:
//                error = context.getString(R.string.exception_unable_to_connect_to_server);
//                break;
//            case ErrorType.ERROR_EXCEPTION_UNKNOWN_SERVER_HOST:
//                error = context.getString(R.string.exception_unknown_server);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SEND_CODE_ERROR:
//                error = context.getString(R.string.exception_send_code_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NO_CONNECTION_ERROR:
//                error = context.getString(R.string.exception_no_connection_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NO_LOGIN_ERROR:
//                error = context.getString(R.string.exception_no_login_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SERVER_RESPONSE_ERROR:
//                error = context.getString(R.string.exception_server_response_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR:
//                error = context.getString(R.string.exception_unexpected_response_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_USERNAME_OR_PHONE:
//                error = context.getString(R.string.exception_empty_username_or_phone);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_KEYWORD:
//                error = context.getString(R.string.exception_empty_keyword);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NOT_FRIEND:
//                error = context.getString(R.string.exception_not_friend);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_MESSAGE:
//                error = context.getString(R.string.exception_empty_message);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FILE_NOT_EXIST:
//                error = context.getString(R.string.exception_file_not_exist);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FILE_SIZE_ZERO:
//                error = context.getString(R.string.exception_file_size_zero);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FILE_UPLOAD_ERROR:
//                error = context.getString(R.string.exception_file_upload_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GROUP_NOT_FOUND:
//                error = context.getString(R.string.exception_group_not_found);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GROUP_NOT_OWN:
//                error = context.getString(R.string.exception_group_not_own);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NOT_IN_GROUP:
//                error = context.getString(R.string.exception_group_not_in);
//                break;
//            case ErrorType.ERROR_EXCEPTION_INVALID_PHONE_NUMBER:
//                error = context.getString(R.string.exception_invalid_phone_number);
//                break;
//            case ErrorType.ERROR_EXCEPTION_INVALID_EMAIL:
//                error = context.getString(R.string.exception_invalid_email);
//                break;
//            case ErrorType.ERROR_EXCEPTION_EMPTY_CODE:
//                error = context.getString(R.string.exception_empty_code);
//                break;
//            case ErrorType.ERROR_EXCEPTION_CODE_EXPIRED:
//                error = context.getString(R.string.exception_code_expired);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FILE_SIZE_TOO_LARGE:
//                error = context.getString(R.string.exception_file_size_too_large);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FILE_NOT_SUPPORT:
//                error = context.getString(R.string.exception_file_not_support);
//                break;
//            case ErrorType.ERROR_EXCEPTION_UNKNOWN:
//                error = context.getString(R.string.exception_unknown);
//                break;
//            case ErrorType.ERROR_EXCEPTION_DBERROR:
//                error = context.getString(R.string.exception_server_db_error);
//                break;
//            case ErrorType.ERROR_EXCEPTION_CODESENT:
//                error = context.getString(R.string.exception_code_sent);
//                break;
//            case ErrorType.ERROR_EXCEPTION_OVERTIME:
//                error = context.getString(R.string.exception_code_overtime);
//                break;
//            case ErrorType.ERROR_EXCEPTION_CODEINVALID:
//                error = context.getString(R.string.exception_code_invalid);
//                break;
//            case ErrorType.ERROR_EXCEPTION_VERIFYFAIL:
//                error = context.getString(R.string.exception_code_verifyfail);
//                break;
//            case ErrorType.ERROR_EXCEPTION_PASSERROR:
//                error = context.getString(R.string.exception_code_passerror);
//                break;
//            case ErrorType.ERROR_EXCEPTION_CODEERROR:
//                error = context.getString(R.string.exception_code_codeerror);
//                break;
//            case ErrorType.ERROR_EXCEPTION_OTHERLOGIN:
//                error = context.getString(R.string.exception_code_otherlogin);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERTOSELF:
//                error = context.getString(R.string.exception_order_to_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOENOUGH:
//                error = context.getString(R.string.exception_order_not_enough_coins);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOPUBLISH:
//                error = context.getString(R.string.exception_order_no_publish);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOTFINISH:
//                error = context.getString(R.string.exception_order_not_finished);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERCANCELFIVE:
//                error = context.getString(R.string.exception_order_cancelled_too_many);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERCANCELFIVEO:
//                error = context.getString(R.string.exception_order_other_cancelled_too_many);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERCANCELNOTBUYER:
//                error = context.getString(R.string.exception_order_cancel_not_buyer);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOENOUGHSELF:
//                error = context.getString(R.string.exception_order_not_enough_coins_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOTFINIDHSELF:
//                error = context.getString(R.string.exception_order_not_finished_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ORDERNOTONSALE:
//                error = context.getString(R.string.exception_reorder_not_on_sale);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SALENOENOUGH:
//                error = context.getString(R.string.exception_sale_not_enough_coins);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SALETHREE:
//                error = context.getString(R.string.exception_sales_too_many);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SALE_COIN_TWO:
//                error = context.getString(R.string.exception_sales_coin_only_one);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SALE_NO_LARGE:
//                error = context.getString(R.string.exception_sales_no_large);
//                break;
//            case ErrorType.ERROR_EXCEPTION_WITHDRAWPASSERROR:
//                error = context.getString(R.string.exception_withdraw_passerror);
//                break;
//            case ErrorType.ERROR_EXCEPTION_WITHDRAWZERO:
//                error = context.getString(R.string.exception_withdraw_zero);
//                break;
//            case ErrorType.ERROR_EXCEPTION_WITHDRAWNOENOUGH:
//                error = context.getString(R.string.exception_withdraw_notenough);
//                break;
//            case ErrorType.ERROR_EXCEPTION_WITHDRAW_LESS_LIMIT:
//                error = context.getString(R.string.exception_withdraw_too_little);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATENOSUCHORDER:
//                error = context.getString(R.string.exception_rate_noorder);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATEBOTH:
//                error = context.getString(R.string.exception_rate_allrated);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATENOTFIN:
//                error = context.getString(R.string.exception_rate_notfinished);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATEBADSELLER:
//                error = context.getString(R.string.exception_rate_badseller);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATEDBUYER:
//                error = context.getString(R.string.exception_rate_alreadyrated);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATEBADBUYER:
//                error = context.getString(R.string.exception_rate_badbuyer);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATEDSELLER:
//                error = context.getString(R.string.exception_rate_alreadyrated);
//                break;
//            case ErrorType.ERROR_EXCEPTION_RATENOTUORDER:
//                error = context.getString(R.string.exception_rate_notyourorder);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECURETOSELF:
//                error = context.getString(R.string.exception_secure_to_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECURENOTTRADER:
//                error = context.getString(R.string.exception_secure_not_trader);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECURENOTENOUGH:
//                error = context.getString(R.string.exception_secure_notenough);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECURENOTFINISH:
//                error = context.getString(R.string.exception_secure_notfinished);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECUCANCELNOTRECEIVER:
//                error = context.getString(R.string.exception_secure_onlyreceivercancancel);
//                break;
//            case ErrorType.ERROR_EXCEPTION_SECUSTATEERROR:
//                error = context.getString(R.string.exception_secure_state_error);
//                break;
//
//            case ErrorType.ERROR_EXCEPTION_TRANTOSELF:
//                error = context.getString(R.string.exception_trans_to_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_TRANNOTENOUGH:
//                error = context.getString(R.string.exception_trans_notenough);
//                break;
//            case ErrorType.ERROR_EXCEPTION_TRANNOTYOURS:
//                error = context.getString(R.string.exception_trans_notyours);
//                break;
//            case ErrorType.ERROR_EXCEPTION_TRANVALID:
//                error = context.getString(R.string.exception_trans_invalid);
//                break;
//            case ErrorType.ERROR_EXCEPTION_TRANOVERTIME:
//                error = context.getString(R.string.exception_trans_timeout);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GIFTNOTENOUGH:
//                error = context.getString(R.string.exception_gift_notenough);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GIFTOVERTIME:
//                error = context.getString(R.string.exception_gift_timeout);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GIFTNOTMEMBER:
//                error = context.getString(R.string.exception_gift_notgroupmember);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GIFTNOLEFT:
//                error = context.getString(R.string.exception_gift_noleft);
//                break;
//            case ErrorType.ERROR_EXCEPTION_GIFTTAKED:
//                error = context.getString(R.string.exception_gift_already_taken);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NOTRUSTSELF:
//                error = context.getString(R.string.exception_not_trust_self);
//                break;
//            case ErrorType.ERROR_EXCEPTION_PASSSAME:
//                error = context.getString(R.string.exception_reset_pass_same);
//                break;
//            case ErrorType.ERROR_EXCEPTION_PASSEMPTY:
//                error = context.getString(R.string.exception_reset_pass_empty);
//                break;
//            case ErrorType.ERROR_EXCEPTION_NAMEUSED:
//                error = context.getString(R.string.exception_user_name_used);
//                break;
//            case ErrorType.ERROR_EXCEPTION_FRIENDTWICE:
//                error = context.getString(R.string.exception_request_friend_twice);
//                break;
//            case ErrorType.ERROR_EXCEPTION_APPEALNOTYOUR:
//                error = context.getString(R.string.exception_not_your_appeal);
//                break;
//            case ErrorType.ERROR_EXCEPTION_AREADY_HAD_WALLET:
//                error = context.getString(R.string.exception_already_had_add_wallet);
//                break;
//            case ErrorType.ERROR_EXCEPTION_ALREADY_FRIEND:
//                error = context.getString(R.string.exception_already_friend);
//                break;
//            case ErrorType.ERROR_EXCEPTION_PRICE_CHANGED:
//                error = context.getString(R.string.exception_price_changed);
//                break;
//            case ErrorType.ERROR_VERIFY_THREE:
//                error = context.getString(R.string.exception_verify_three);
//                break;
//            case ErrorType.ERROR_VERIFY_ING:
//                error = context.getString(R.string.exception_verify_ing);
//                break;
//            case ErrorType.ERROR_IDENTITY_STATE_UNKNOWN:
//                error = context.getString(R.string.exception_verify_unknown);
//                break;
//            case ErrorType.ERROR_IDENTITY_ALREADY_PASS:
//                error = context.getString(R.string.exception_verify_already_pass);
//                break;
//            case ErrorType.ERROR_IDENTITY_STATE_WRONG:
//                error = context.getString(R.string.exception_verify_state_wrong);
//                break;
//            case ErrorType.ERROR_IDENTITY_NOT_FOUND:
//                error = context.getString(R.string.exception_identity_not_found);
//                break;
//            case ErrorType.ERROR_IVERIFY_PAUSE:
//                error = context.getString(R.string.exception_identity_pause);
//                break;
//            case ErrorType.ERROR_NOT_ADMIN:
//                error = context.getString(R.string.exception_not_admin);
//                break;
//            case ErrorType.ERROR_APPEAL_WAS_ARBITRATED:
//                error = context.getString(R.string.exception_appeal_was_arbitrated);
//                break;
//            case ErrorType.ERROR_APPEAL_WAS_CANCEL:
//                error = context.getString(R.string.exception_appeal_was_cancel);
//                break;
//            case ErrorType.ERROR_APPEAL_NO_RESPONSIBLE:
//                error = context.getString(R.string.exception_appeal_no_responsible);
//                break;
//            case ErrorType.ERROR_APPEAL_STATUS_INVALID:
//                error = context.getString(R.string.exception_appeal_no_responsible);
//                break;
//            case ErrorType.ERROR_APPEAL_SHOULD_WAITTING:
//                error = context.getString(R.string.exception_appeal_should_wait);
//                break;
//            case ErrorType.ERROR_LOGIN_CODE_INNER_THIRTY:
//                error = context.getString(R.string.exception_login_code_inner_thirty);
//                break;
//            case ErrorType.ERROR_EXCEPTION_CREATE_COIN:
//                error = context.getString(R.string.wallet_create_error);
//                break;
//            default:
//        }
//        return error;
//    }
}
