package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Token
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TokenDao : Dao<Token> {
    fun add(token: Token, sqlConnection: SqlConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun getUserIDFromToken(
        token: String,
        sqlConnection: SqlConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isTokenExists(
        token: String,
        sqlConnection: SqlConnection,
        handler: (isTokenExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(
        token: Token,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}