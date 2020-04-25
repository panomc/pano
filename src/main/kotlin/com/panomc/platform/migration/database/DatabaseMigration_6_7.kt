package com.panomc.platform.migration.database

import com.panomc.platform.util.DatabaseManager.Companion.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_6_7 : DatabaseMigration {
    override val FROM_SCHEME_VERSION = 6
    override val SCHEME_VERSION = 7
    override val SCHEME_VERSION_INFO = "Change date field in table panel_notifications to LONG type."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            changeField()
        )

    private fun changeField(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    ALTER TABLE `${tablePrefix}panel_notification` MODIFY `date` BIGINT(250);
                """
        ) {
            handler.invoke(it)
        }
    }
}