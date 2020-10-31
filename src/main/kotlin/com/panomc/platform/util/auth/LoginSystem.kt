package com.panomc.platform.util.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.Token
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import com.panomc.platform.util.Auth
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class LoginSystem : Auth() {
    private fun isValidLoginData(
        formData: JsonObject,
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        var robotError = false

        var errorCode: ErrorCode? = when {
            formData.getString("email").isNullOrEmpty() -> ErrorCode.LOGIN_EMAIL_EMPTY
            formData.getString("password").isNullOrEmpty() -> ErrorCode.LOGIN_PASSWORD_EMPTY

            formData.getString("email")!!.length < 5 || (!formData.getString("email")!!
                .matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) -> ErrorCode.LOGIN_INVALID_EMAIL
            formData.getString("password")!!.length < 6 || formData.getString("password")!!.length > 128 -> ErrorCode.LOGIN_INVALID_PASSWORD

//            !checkRecaptcha && recaptchaValue.isNullOrEmpty() -> {
//                robotError = true
//                ""
//            }
//
//            !checkRecaptcha && !recaptcha.isValid(recaptchaValue) -> {
//                robotError = true
//                ""
//            }

            else -> null
        }

        if (robotError)
            errorCode = ErrorCode.LOGIN_CANT_VERIFY_ROBOT

        if (errorCode == null)
            handler.invoke()
        else
            resultHandler.invoke(Error(errorCode))
    }

    private fun isLoginCorrect(
        formData: JsonObject,
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        databaseManager.getDatabase().userDao.isLoginCorrect(
            formData.getString("email"),
            DigestUtils.md5Hex(formData.getString("password")),
            sqlConnection
        ) { isLoginCorrect, _ ->
            when {
                isLoginCorrect == null -> closeConnection {
                    resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_127))
                }
                isLoginCorrect -> handler.invoke()
                else -> closeConnection {
                    resultHandler.invoke(Error(ErrorCode.LOGIN_WRONG_EMAIL_OR_PASSWORD))
                }
            }
        }
    }

    private fun getUserIDFromUsername(
        username: String,
        resultHandler: (authResult: Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        createConnection(resultHandler) {
            databaseManager.getDatabase().userDao.getUserIDFromUsername(
                username,
                sqlConnection
            ) { userID, _ ->
                if (userID == null)
                    closeConnection {
                        resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_4))
                    }
                else
                    handler.invoke(userID)
            }
        }
    }

    private fun getUserSecretKeyFromUserID(
        userID: Int,
        resultHandler: (authResult: Result) -> Unit,
        handler: (secretKey: String) -> Unit
    ) {
        databaseManager.getDatabase().userDao.getSecretKeyByID(userID, sqlConnection) { secretKey, _ ->
            if (secretKey == null)
                closeConnection {
                    resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_6))
                }
            else
                handler.invoke(secretKey)
        }
    }

    private fun createTokenForLoginSession(
        userID: Int,
        resultHandler: (authResult: Result) -> Unit,
        handler: (token: String) -> Unit
    ) {
        createConnection(resultHandler) {
            getUserSecretKeyFromUserID(userID, resultHandler) { secretKey ->
                val token = Jwts.builder()
                    .setSubject("LOGIN_SESSION")
                    .signWith(
                        Keys.hmacShaKeyFor(
                            Base64.getDecoder().decode(
                                secretKey
                            )
                        )
                    )
                    .compact()

                databaseManager.getDatabase().tokenDao.add(
                    Token(-1, token, userID, "LOGIN_SESSION"),
                    sqlConnection
                ) { result, _ ->
                    if (result == null)
                        closeConnection {
                            resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_5))
                        }
                    else
                        handler.invoke(token)
                }
            }
        }
    }

    fun createSession(
        username: String,
        routingContext: RoutingContext,
        resultHandler: (authResult: Result) -> Unit
    ) {
        getUserIDFromUsername(username, resultHandler) { userID ->
            createTokenForLoginSession(userID, resultHandler) { token ->
                val age = 60 * 60 * 24 * 365 * 2L
                val path = "/"

                val tokenCookie = Cookie.cookie("pano_token", token)

                tokenCookie.setMaxAge(age)

                tokenCookie.path = path

                routingContext.addCookie(tokenCookie)

                closeConnection {
                    resultHandler.invoke(Successful())
                }
            }
        }
    }

    fun login(
        formData: JsonObject,
        ipAddress: String,
        createSession: Boolean = true,
        resultHandler: (authResult: Result) -> Unit
    ) {
        this.ipAddress = ipAddress

        isValidLoginData(formData, resultHandler) {
            createConnection(resultHandler) {
                isLoginCorrect(formData, resultHandler) {
                    closeConnection {
                        if (!createSession)
                            resultHandler.invoke(Successful())
                        else
                            resultHandler.invoke(Successful())
                    }
                }
            }
        }
    }
}