package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.Result
import com.panomc.platform.model.Token
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TokenDao : Dao<Token> {
    fun add(token: Token, sqlConnection: SQLConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun getUserIDFromToken(
        token: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isTokenExists(
        token: String,
        sqlConnection: SQLConnection,
        handler: (isTokenExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(
        token: Token,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}