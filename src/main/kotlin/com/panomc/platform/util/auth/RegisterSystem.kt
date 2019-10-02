package com.panomc.platform.util.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.util.Auth
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import org.apache.commons.codec.digest.DigestUtils
import java.util.*

class RegisterSystem : Auth() {
    private fun isValidRegisterData(
        formData: JsonObject,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: () -> Unit
    ) {
        var robotError = false

        var errorCode: ErrorCode? = when {
            formData.getString("username").isNullOrEmpty() -> ErrorCode.REGISTER_USERNAME_EMPTY
            formData.getString("email").isNullOrEmpty() -> ErrorCode.REGISTER_EMAIL_EMPTY
            formData.getString("password").isNullOrEmpty() -> ErrorCode.REGISTER_PASSWORD_EMPTY

//            checkRecaptcha && recaptchaValue.isNullOrEmpty() -> {
//                robotError = true
//                null
//            }

            formData.getString("username")!!.length < 3 -> ErrorCode.REGISTER_USERNAME_TOO_SHORT
            formData.getString("username")!!.length > 32 -> ErrorCode.REGISTER_USERNAME_TOO_LONG

            formData.getString("password")!!.length < 6 -> ErrorCode.REGISTER_PASSWORD_TOO_SHORT
            formData.getString("password")!!.length > 128 -> ErrorCode.REGISTER_PASSWORD_TOO_LONG

            !formData.getString("username")!!.matches(Regex("^[a-zA-Z]+\$")) -> ErrorCode.REGISTER_INVALID_USERNAME
            !formData.getString("email")!!.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")) || formData.getString(
                "email"
            )!!.length > 128 -> ErrorCode.REGISTER_INVALID_EMAIL

//            !isAdmin && (emailConfirm.isNullOrEmpty() || emailConfirm != email) -> ErrorCode.REGISTER_EMAIL_AND_REPEAT_NOT_SAME

//            !isAdmin && (acceptCheckBox == null || !acceptCheckBox!!) -> ErrorCode.REGISTER_PLEASE_ACCEPT_CHECKBOX

//            checkRecaptcha && !recaptcha.isValid(recaptchaValue) -> {
//                robotError = true
//                null
//            }

            else -> null
        }

        if (robotError)
            errorCode = ErrorCode.REGISTER_CANT_VERIFY_ROBOT

        if (errorCode == null)
            handler.invoke()
        else
            resultHandler.invoke(Error(errorCode))
    }

    private fun isRegisteredWithEmail(
        formData: JsonObject,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "SELECT COUNT(email) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where email = ?"

        getConnection().queryWithParams(query, JsonArray().add(formData.getString("email"))) { queryResult ->
            val errorCode: ErrorCode? = if (!queryResult.succeeded())
                ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_1
            else if (queryResult.result().results[0].getInteger(0) == 1)
                ErrorCode.REGISTER_EMAIL_NOT_AVAILABLE
            else
                null

            if (errorCode == null)
                handler.invoke()
            else
                closeConnection {
                    resultHandler.invoke(Error(errorCode))
                }
        }
    }

    private fun getAdminPermissionIdFromDB(
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: (permissionID: Int) -> Unit
    ) {
        val query =
            "SELECT id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}permission where name = ?"

        getConnection().queryWithParams(query, JsonArray().add("admin")) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_3))
        }
    }

    private fun registerNew(
        formData: JsonObject,
        resultHandler: (authResult: AuthResult) -> Unit,
        handler: () -> Unit
    ) {
        registerNew(formData, resultHandler, 0, handler)
    }

    private fun registerNew(
        formData: JsonObject,
        resultHandler: (authResult: AuthResult) -> Unit,
        permissionID: Int,
        handler: () -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user (username, email, password, registered_ip, permission_id, secret_key, public_key) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)"

        val key = Keys.keyPairFor(SignatureAlgorithm.RS256)

        getConnection().updateWithParams(
            query,
            JsonArray()
                .add(formData.getString("username"))
                .add(formData.getString("email"))
                .add(DigestUtils.md5Hex(formData.getString("password")))
                .add(ipAddress)
                .add(permissionID)
                .add(Base64.getEncoder().encodeToString(key.private.encoded))
                .add(Base64.getEncoder().encodeToString(key.public.encoded))
        ) { queryResult ->
            if (!queryResult.succeeded()) {
                val errorCode = ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_2

                closeConnection {
                    resultHandler.invoke(Error(errorCode))
                }
            } else
                handler.invoke()
        }
    }

    fun register(
        formData: JsonObject,
        ipAddress: String,
        isAdmin: Boolean = false,
        checkRecaptcha: Boolean = !isAdmin,
        resultHandler: (authResult: AuthResult) -> Unit
    ) {
        this.ipAddress = ipAddress
        this.checkRecaptcha = checkRecaptcha

        isValidRegisterData(formData, resultHandler) {
            createConnection(resultHandler) {
                isRegisteredWithEmail(formData, resultHandler) {
                    if (!isAdmin)
                        registerNew(formData, resultHandler) {
                            closeConnection {
                                resultHandler.invoke(Successful())
                            }
                        }
                    else
                        getAdminPermissionIdFromDB(resultHandler) {
                            registerNew(formData, resultHandler, it) {
                                closeConnection {
                                    resultHandler.invoke(Successful())
                                }
                            }
                        }
                }
            }
        }
    }
}