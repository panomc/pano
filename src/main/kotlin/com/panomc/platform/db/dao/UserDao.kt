package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.User
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface UserDao : Dao<User> {
    fun add(user: User, handler: (result: Result?) -> Unit)

    fun add(sqlConnection: SQLConnection, user: User, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun isEmailExists(
        email: String,
        sqlConnection: SQLConnection,
        handler: (isEmailExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getUserIDFromUsername(
        username: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getPermissionIDFromUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getSecretKeyByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (secretKey: String?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isLoginCorrect(
        email: String,
        password: String,
        sqlConnection: SQLConnection,
        handler: (isLoginCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun getUsernameFromUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (username: String?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (user: User?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getAllByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (userList: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )
}