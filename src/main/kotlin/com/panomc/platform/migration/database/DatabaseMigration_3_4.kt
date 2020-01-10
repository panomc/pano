package com.panomc.platform.migration.database

import com.panomc.platform.util.DatabaseManager.Companion.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_3_4 : DatabaseMigration {
    override val FROM_SCHEME_VERSION = 3
    override val SCHEME_VERSION = 4
    override val SCHEME_VERSION_INFO = "Add panel notifications table."

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
            CREATE TABLE IF NOT EXISTS `${tablePrefix}panel_notification` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,
              `type_ID` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Panel Notification table.';
        """
        ) {
            handler.invoke(it)
        }
    }
}