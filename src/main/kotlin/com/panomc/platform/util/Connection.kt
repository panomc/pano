package com.panomc.platform.util

import io.vertx.core.AsyncResult
import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.sql.SQLConnection

class Connection(private val mSqlConnection: SQLConnection) {

    companion object {
        fun createConnection(
            logger: Logger,
            asyncSQLClient: AsyncSQLClient,
            handler: (connection: Connection?, asyncResult: AsyncResult<SQLConnection>) -> Unit
        ) {
            asyncSQLClient.getConnection { getConnection ->
                handler.invoke(
                    if (getConnection.succeeded())
                        Connection(getConnection.result())
                    else {
                        logger.error("Failed to connect database! Error is: ${getConnection.result()}")

                        null
                    },
                    getConnection
                )
            }
        }
    }

    fun getSQLConnection() = mSqlConnection

    fun closeConnection(handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        mSqlConnection.close {
            handler?.invoke(it)
        }
    }
}