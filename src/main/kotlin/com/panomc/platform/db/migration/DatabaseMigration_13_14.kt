package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlConnection

@Suppress("ClassName")
@Migration
class DatabaseMigration_13_14(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 13
    override val SCHEME_VERSION = 14
    override val SCHEME_VERSION_INFO = "Create ticket_message table."

    override val handlers: List<suspend (sqlConnection: SqlConnection) -> Unit> =
        listOf(
            createTicketMessageTable()
        )

    private fun createTicketMessageTable(): suspend (sqlConnection: SqlConnection) -> Unit =
        { sqlConnection: SqlConnection ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}ticket_message` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `user_id` int NOT NULL,              
                              `ticket_id` int NOT NULL,
                              `message` text NOT NULL,
                              `date` MEDIUMTEXT NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Ticket message table.';
                        """
                )
                .execute()
                .await()
        }
}