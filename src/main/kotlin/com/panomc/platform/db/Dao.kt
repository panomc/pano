package com.panomc.platform.db

import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

interface Dao<T> {
    fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection
}