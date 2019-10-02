package com.panomc.platform.util.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.util.Auth
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.apache.commons.codec.digest.DigestUtils
import java.util.*


class LoginSystem : Auth() {
    private fun isValidLoginData(
        formData: JsonObject,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: () -> Unit
    ) {
        var robotError = false

        var errorCode: ErrorCode? = when {
            formData.getString("email").isNullOrEmpty() -> ErrorCode.LOGIN_EMAIL_EMPTY
            formData.getString("password").isNullOrEmpty() -> ErrorCode.LOGIN_PASSWORD_EMPTY

            formData.getString("email")!!.length < 5 || (!formData.getString("email")!!.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) -> ErrorCode.LOGIN_INVALID_EMAIL
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
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "SELECT COUNT(email) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where email = ? and password = ?"

        getConnection().queryWithParams(
            query,
            JsonArray().add(formData.getString("email")).add(DigestUtils.md5Hex(formData.getString("password")))
        ) { queryResult ->
            if (queryResult.result().results[0].getInteger(0) == 0)
                closeConnection {
                    resultHandler.invoke(Error(ErrorCode.LOGIN_WRONG_EMAIL_OR_PASSWORD))
                }
            else
                handler.invoke()
        }
    }

    private fun getUserIDFromUsername(
        username: String,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        createConnection(resultHandler) {
            val query =
                "SELECT id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where username = ?"

            getConnection().queryWithParams(query, JsonArray().add(username)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0))
                else
                    closeConnection {
                        resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_4))
                    }
            }
        }
    }

    private fun getUserSecretKeyFromUserID(
        userID: Int,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: (secretKey: String) -> Unit
    ) {
        createConnection(resultHandler) {
            val query =
                "SELECT secret_key FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

            getConnection().queryWithParams(query, JsonArray().add(userID)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getString(0))
                else
                    closeConnection {
                        resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_6))
                    }
            }
        }
    }

    private fun createTokenForLoginSession(
        userID: Int,
        resultHandler: (authResult: AuthResult) -> Unit,
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

                val query =
                    "INSERT INTO ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token (token, created_time, user_id, subject) " +
                            "VALUES (?, ?, ?, ?)"

                getConnection().updateWithParams(
                    query,
                    JsonArray().add(token).add(Calendar.getInstance().time.toString()).add(userID).add("LOGIN_SESSION")
                ) { queryResult ->
                    if (queryResult.succeeded())
                        handler.invoke(token)
                    else
                        closeConnection {
                            resultHandler.invoke(Error(ErrorCode.LOGIN_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_5))
                        }
                }
            }
        }
    }

    fun createSession(
        username: String,
        routingContext: RoutingContext,
        resultHandler: (authResult: AuthResult) -> Unit
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
        resultHandler: (authResult: AuthResult) -> Unit
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