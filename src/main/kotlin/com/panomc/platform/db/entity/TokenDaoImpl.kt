package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TokenDao
import com.panomc.platform.db.model.Token
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

class TokenDaoImpl(override val tableName: String = "token") : DaoImpl(), TokenDao {
    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `token` text NOT NULL,
                              `created_time` BIGINT(20) NOT NULL,
                              `user_id` int(11) NOT NULL,
                              `subject` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token Table';
                        """
                )
                .execute {
                    handler.invoke(it)
                }
        }

    override fun add(
        token: Token,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (token, created_time, user_id, subject) VALUES (?, ?, ?, ?)"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token.token,
                    System.currentTimeMillis(),
                    token.userID,
                    token.subject
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getUserIDFromToken(
        token: String,
        sqlConnection: SqlConnection,
        handler: (result: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT user_id FROM `${getTablePrefix() + tableName}` where `token` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun isTokenExists(
        token: String,
        sqlConnection: SqlConnection,
        handler: (isTokenExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `token` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) == 1, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun delete(
        token: Token,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${getTablePrefix() + tableName}` WHERE token = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    token.token
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }
}