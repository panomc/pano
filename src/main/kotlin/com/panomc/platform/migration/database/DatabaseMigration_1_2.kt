package com.panomc.platform.migration.database

import com.panomc.platform.util.DatabaseManager.Companion.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_1_2 : DatabaseMigration {
    override val FROM_SCHEME_VERSION = 1
    override val SCHEME_VERSION = 2
    override val SCHEME_VERSION_INFO = ""

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createSystemPropertyTable()
        )

    private fun createSystemPropertyTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}system_property` (
              `id` int NOT NULL AUTO_INCREMENT,
              `option` text NOT NULL,
              `value` text NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='System Property table.';
        """
        ) {
            if (it.succeeded())
                sqlConnection.updateWithParams(
                    """
                    INSERT INTO ${tablePrefix}system_property (`option`, `value`) VALUES (?, ?)
            """.trimIndent(),
                    JsonArray()
                        .add("show_getting_started")
                        .add("true")
                ) {
                    if (it.succeeded())
                        sqlConnection.updateWithParams(
                            """
                    INSERT INTO ${tablePrefix}system_property (`option`, `value`) VALUES (?, ?)
            """.trimIndent(),
                            JsonArray()
                                .add("show_connect_server_info")
                                .add("true")
                        ) {
                            handler.invoke(it)
                        }
                    else
                        handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }
}