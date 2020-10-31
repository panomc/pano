package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_8_9 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 8
    override val SCHEME_VERSION = 9
    override val SCHEME_VERSION_INFO = "Delete platformCode system property."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            deletePlatformCode()
        )

    private fun deletePlatformCode(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.updateWithParams(
            """
                   DELETE FROM ${tablePrefix}system_property WHERE `option` = ?
            """.trimIndent(),
            JsonArray()
                .add("platformCode")
        ) {
            handler.invoke(it)
        }
    }
}