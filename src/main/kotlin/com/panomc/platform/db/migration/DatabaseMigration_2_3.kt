package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_2_3 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 2
    override val SCHEME_VERSION = 3
    override val SCHEME_VERSION_INFO = "Removed connect_board feature."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            deleteConnectBoardFeature()
        )

    private fun deleteConnectBoardFeature(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.updateWithParams(
                """
                   DELETE FROM ${databaseManager.getTablePrefix()}system_property WHERE `option` = ?
            """.trimIndent(),
                JsonArray()
                    .add("show_connect_server_info")
            ) {
                handler.invoke(it)
            }
        }
}