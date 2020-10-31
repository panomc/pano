package com.panomc.platform.util.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import com.panomc.platform.util.Auth
import io.vertx.core.json.JsonObject
import org.apache.commons.codec.digest.DigestUtils

class RegisterSystem : Auth() {
    private fun isValidRegisterData(
        formData: JsonObject,
        resultHandler: (authResult: Result) -> Unit,
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
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        databaseManager.getDatabase().userDao.isEmailExists(
            formData.getString("email"),
            getConnection()
        ) { isEmailExists, _ ->
            when {
                isEmailExists == null -> closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_1))
                }
                isEmailExists -> closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_EMAIL_NOT_AVAILABLE))
                }
                else -> handler.invoke()
            }
        }
    }

    private fun getAdminPermissionIdFromDB(
        resultHandler: (authResult: Result) -> Unit,
        handler: (permissionID: Int) -> Unit
    ) {
        databaseManager.getDatabase().permissionDao.getPermissionID(
            Permission(-1, "admin"),
            getConnection()
        ) { permissionID, _ ->
            if (permissionID == null)
                closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_3))
                }
            else
                handler.invoke(permissionID)
        }
    }

    private fun registerNew(
        formData: JsonObject,
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        registerNew(formData, resultHandler, 0, handler)
    }

    private fun registerNew(
        formData: JsonObject,
        resultHandler: (authResult: Result) -> Unit,
        permissionID: Int,
        handler: () -> Unit
    ) {
        databaseManager.getDatabase().userDao.add(
            getConnection(),
            User(
                -1,
                formData.getString("username"),
                formData.getString("email"),
                DigestUtils.md5Hex(formData.getString("password")),
                ipAddress!!,
                permissionID
            )
        ) { result, _ ->
            if (result == null || result !is Successful)
                closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_2))
                }
            else
                handler.invoke()
        }
    }

    private fun getUserIDFromUsername(
        formData: JsonObject,
        resultHandler: (Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        databaseManager.getDatabase().userDao.getUserIDFromUsername(
            formData.getString("username"),
            getConnection()
        ) { userID, _ ->
            if (userID == null)
                closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_12))
                }
            else
                handler.invoke(userID)
        }
    }

    private fun setWhoInstalledSystem(
        userID: Int,
        resultHandler: (authResult: Result) -> Unit,
        handler: () -> Unit
    ) {
        val property = SystemProperty(-1, "who_installed_user_id", userID.toString())

        databaseManager.getDatabase().systemPropertyDao.isPropertyExists(property, getConnection()) { exists, _ ->
            when {
                exists == null -> closeConnection {
                    resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_13))
                }
                exists -> databaseManager.getDatabase().systemPropertyDao.update(
                    property,
                    getConnection()
                ) { resultOfUpdate, _ ->
                    if (resultOfUpdate == null)
                        closeConnection {
                            resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_15))
                        }
                    else
                        handler.invoke()
                }
                else -> databaseManager.getDatabase().systemPropertyDao.add(
                    property,
                    getConnection()
                ) { resultOfAdd, _ ->
                    if (resultOfAdd == null)
                        closeConnection {
                            resultHandler.invoke(Error(ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_14))
                        }
                    else
                        handler.invoke()
                }
            }
        }
    }

    fun register(
        formData: JsonObject,
        ipAddress: String,
        isAdmin: Boolean = false,
        checkRecaptcha: Boolean = !isAdmin,
        resultHandler: (authResult: Result) -> Unit
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
                                getUserIDFromUsername(formData, resultHandler) { getUserIDFromUsername ->
                                    setWhoInstalledSystem(
                                        getUserIDFromUsername,
                                        resultHandler
                                    ) {
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
    }
}