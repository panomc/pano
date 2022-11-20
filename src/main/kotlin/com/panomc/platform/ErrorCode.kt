package com.panomc.platform

// last ID 128
enum class ErrorCode {
    INVALID_DATA,

    REGISTER_USERNAME_EMPTY,
    REGISTER_EMAIL_EMPTY,
    PASSWORD_EMPTY,

    REGISTER_USERNAME_TOO_SHORT,
    REGISTER_USERNAME_TOO_LONG,

    PASSWORD_TOO_SHORT,
    PASSWORD_TOO_LONG,

    NEW_PASSWORD_EMPTY,
    NEW_PASSWORD_TOO_SHORT,
    NEW_PASSWORD_TOO_LONG,
    NEW_PASSWORD_REPEAT_DOESNT_MATCH,

    REGISTER_INVALID_USERNAME,
    REGISTER_INVALID_EMAIL,

    REGISTER_CANT_VERIFY_ROBOT,

    REGISTER_PASSWORD_AND_PASSWORD_REPEAT_NOT_SAME,

    REGISTER_USERNAME_NOT_AVAILABLE,
    REGISTER_EMAIL_NOT_AVAILABLE,

    REGISTER_NOT_ACCEPTED_AGREEMENT,

    LOGIN_IS_INVALID,
    LOGIN_EMAIL_NOT_VERIFIED,

    CANT_CONNECT_DATABASE,

    CONNECT_NEW_SERVER_API_PLATFORM_CODE_WRONG,

    PAGE_NOT_FOUND,

    NOT_EXISTS,
    CATEGORY_NOT_EXISTS,

    POST_NOT_FOUND,

    NOT_LOGGED_IN,
    INSTALLATION_REQUIRED,
    NO_PERMISSION,
    PLATFORM_ALREADY_INSTALLED,

    CANT_UPDATE_ADMIN_PERMISSION,

    TITLE_CANT_BE_EMPTY,
    DESCRIPTION_CANT_BE_EMPTY,

    UNKNOWN,

    INVALID_IP_ADDRESS,
    INVALID_LINK,

    EMAIL_ALREADY_VERIFIED,

    LAST_ADMIN,
    LOGIN_USER_IS_BANNED,

    NOT_BANNED,
    CANT_BAN_YOURSELF,
    ALREADY_BANNED,

    FAVICON_WRONG_CONTENT_TYPE,
    FAVICON_EXCEEDS_SIZE,

    WEBSITE_LOGO_WRONG_CONTENT_TYPE,
    WEBSITE_LOGO_EXCEEDS_SIZE
}