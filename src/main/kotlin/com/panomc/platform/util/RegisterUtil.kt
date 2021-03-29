package com.panomc.platform.util

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PermissionGroup
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.db.model.User
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

object RegisterUtil {
    fun register(
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        user: User,
        isAdmin: Boolean = false,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.isEmailExists(
            user.email,
            sqlConnection
        ) { isEmailExists, asyncResultOfIsEmailExists ->
            if (isEmailExists == null) {
                handler.invoke(
                    Error(ErrorCode.UNKNOWN_ERROR_1),
                    asyncResultOfIsEmailExists
                )

                return@isEmailExists
            }

            if (isEmailExists) {
                handler.invoke(Error(ErrorCode.REGISTER_EMAIL_NOT_AVAILABLE), asyncResultOfIsEmailExists)

                return@isEmailExists
            }

            if (!isAdmin) {
                addUser(user, databaseManager, sqlConnection, handler)

                return@isEmailExists
            }

            databaseManager.getDatabase().permissionGroupDao.getPermissionGroupID(
                PermissionGroup(-1, "admin"),
                sqlConnection
            ) { permissionGroupID, asyncResultOfGetPermissionGroupID ->
                if (permissionGroupID == null) {
                    handler.invoke(
                        Error(ErrorCode.UNKNOWN_ERROR_3),
                        asyncResultOfGetPermissionGroupID
                    )

                    return@getPermissionGroupID
                }

                val newUser = User(
                    user.id,
                    user.username,
                    user.email,
                    user.password,
                    user.registeredIp,
                    permissionGroupID,
                    System.currentTimeMillis().toString()
                )

                addUser(newUser, databaseManager, sqlConnection) { result, asyncResultOfAddUser ->
                    if (result == null) {
                        handler.invoke(
                            Error(ErrorCode.UNKNOWN_ERROR_157),
                            asyncResultOfAddUser
                        )

                        return@addUser
                    }

                    databaseManager.getDatabase().userDao.getUserIDFromUsername(
                        user.username,
                        sqlConnection
                    ) { userID, asyncResultOfGetUserIDFromUsername ->
                        if (userID == null) {
                            handler.invoke(
                                Error(ErrorCode.UNKNOWN_ERROR_12),
                                asyncResultOfGetUserIDFromUsername
                            )

                            return@getUserIDFromUsername
                        }

                        val property = SystemProperty(-1, "who_installed_user_id", userID.toString())

                        databaseManager.getDatabase().systemPropertyDao.isPropertyExists(
                            property,
                            sqlConnection
                        ) { exists, asyncResultOfIsPropertyExists ->
                            if (exists == null) {
                                handler.invoke(
                                    Error(ErrorCode.UNKNOWN_ERROR_13),
                                    asyncResultOfIsPropertyExists
                                )

                                return@isPropertyExists
                            }

                            if (exists) {
                                databaseManager.getDatabase().systemPropertyDao.update(
                                    property,
                                    sqlConnection
                                ) { resultOfUpdate, asyncResultOfUpdate ->
                                    if (resultOfUpdate == null) {
                                        handler.invoke(
                                            Error(ErrorCode.UNKNOWN_ERROR_15),
                                            asyncResultOfUpdate
                                        )

                                        return@update
                                    }

                                    handler.invoke(Successful(), asyncResultOfUpdate)
                                }

                                return@isPropertyExists
                            }

                            databaseManager.getDatabase().systemPropertyDao.add(
                                property,
                                sqlConnection
                            ) { resultOfAdd, asyncResultOfAddToSystemPropertyDao ->
                                if (resultOfAdd == null) {
                                    handler.invoke(
                                        Error(ErrorCode.UNKNOWN_ERROR_14),
                                        asyncResultOfAddToSystemPropertyDao
                                    )

                                    return@add
                                }

                                handler.invoke(Successful(), asyncResultOfAddToSystemPropertyDao)
                            }
                        }
                    }
                }
            }
        }
    }

    fun validateForm(
        username: String,
        email: String,
        emailRepeat: String = email,
        password: String,
        passwordRepeat: String = password,
        checkRobot: Boolean = true,
        recaptchaToken: String = "",
        reCaptcha: ReCaptcha? = null,
        handler: (result: Result) -> Unit
    ) {
        if (username.isEmpty()) {
            handler.invoke(Error(ErrorCode.REGISTER_USERNAME_EMPTY))

            return
        }

        if (email.isEmpty()) {
            handler.invoke(Error(ErrorCode.REGISTER_EMAIL_EMPTY))

            return
        }

        if (password.isEmpty()) {
            handler.invoke(Error(ErrorCode.REGISTER_PASSWORD_EMPTY))

            return
        }

        if (username.length < 3) {
            handler.invoke(Error(ErrorCode.REGISTER_USERNAME_TOO_SHORT))

            return
        }

        if (username.length > 32) {
            handler.invoke(Error(ErrorCode.REGISTER_USERNAME_TOO_LONG))

            return
        }

        if (password.length < 6) {
            handler.invoke(Error(ErrorCode.REGISTER_PASSWORD_TOO_SHORT))

            return
        }

        if (password.length > 128) {
            handler.invoke(Error(ErrorCode.REGISTER_PASSWORD_TOO_LONG))

            return
        }

        if (!username.matches(Regex("^[a-zA-Z0-9_]+\$"))) {
            handler.invoke(Error(ErrorCode.REGISTER_INVALID_USERNAME))

            return
        }

        if (!email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            handler.invoke(Error(ErrorCode.REGISTER_INVALID_USERNAME))

            return
        }

        if (email != emailRepeat) {
            handler.invoke(Error(ErrorCode.REGISTER_EMAIL_AND_EMAIL_REPEAT_NOT_SAME))

            return
        }

        if (password != passwordRepeat) {
            handler.invoke(Error(ErrorCode.REGISTER_PASSWORD_AND_PASSWORD_REPEAT_NOT_SAME))

            return
        }

        if (checkRobot && !reCaptcha!!.isValid(recaptchaToken)) {
            handler.invoke(Error(ErrorCode.REGISTER_CANT_VERIFY_ROBOT))

            return
        }

        handler.invoke(Successful())
    }

    private fun addUser(
        user: User,
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.add(user, sqlConnection) { isSuccessful, asyncResultOfAdd ->
            if (isSuccessful == null) {
                handler.invoke(null, asyncResultOfAdd)

                return@add
            }

            handler.invoke(Successful(), asyncResultOfAdd)
        }
    }
}