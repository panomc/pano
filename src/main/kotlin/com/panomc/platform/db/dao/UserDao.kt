package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.User
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface UserDao : Dao<User> {

    fun add(user: User, sqlConnection: SqlConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun isEmailExists(
        email: String,
        sqlConnection: SqlConnection,
        handler: (isEmailExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getUserIDFromUsername(
        username: String,
        sqlConnection: SqlConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getPermissionIDFromUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getSecretKeyByID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (secretKey: String?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isLoginCorrect(
        usernameOrEmail: String,
        password: String,
        sqlConnection: SqlConnection,
        handler: (isLoginCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun count(sqlConnection: SqlConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun getUsernameFromUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (username: String?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (user: User?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByPageType(
        pageType: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getAllByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SqlConnection,
        handler: (userList: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getUserIDFromUsernameOrEmail(
        usernameOrEmail: String,
        sqlConnection: SqlConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getUsernameByListOfID(
        userIDList: List<Int>,
        sqlConnection: SqlConnection,
        handler: (usernameList: Map<Int, String>?, asyncResult: AsyncResult<*>) -> Unit
    )
}