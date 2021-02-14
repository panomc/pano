package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

@Suppress("ClassName")
class DatabaseMigration_2_3 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 2
    override val SCHEME_VERSION = 3
    override val SCHEME_VERSION_INFO = "Removed connect_board feature."

    override val handlers: List<(sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit> =
        listOf(
            deleteConnectBoardFeature()
        )

    private fun deleteConnectBoardFeature(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .preparedQuery("DELETE FROM ${getTablePrefix()}system_property WHERE `option` = ?")
                .execute(Tuple.of("show_connect_server_info")) { queryResult ->
                    handler.invoke(queryResult)
                }
        }
}