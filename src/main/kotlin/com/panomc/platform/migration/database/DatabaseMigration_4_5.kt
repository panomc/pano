package com.panomc.platform.migration.database

import com.panomc.platform.util.DatabaseManager.Companion.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_4_5 : DatabaseMigration {
    override val FROM_SCHEME_VERSION = 4
    override val SCHEME_VERSION = 5
    override val SCHEME_VERSION_INFO = "Add new status and date field to panel notifications table."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createPanelNotificationsTable()
        )

    private fun createPanelNotificationsTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    ALTER TABLE `${tablePrefix}panel_notification` 
                    ADD date int(50), ADD status varchar(255);
                """
        ) {
            handler.invoke(it)
        }
    }
}