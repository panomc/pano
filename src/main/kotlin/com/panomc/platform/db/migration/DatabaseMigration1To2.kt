package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

@Migration
class DatabaseMigration1To2(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 1
    override val SCHEME_VERSION = 2
    override val SCHEME_VERSION_INFO = ""

    override val handlers: List<suspend (sqlConnection: SqlConnection) -> Unit> =
        listOf(createSystemPropertyTable())

    private fun createSystemPropertyTable(): suspend (sqlConnection: SqlConnection) -> Unit =
        { sqlConnection: SqlConnection ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}system_property` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `option` text NOT NULL,
                              `value` text NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System Property table.';
                        """
                )
                .execute()
                .await()

            sqlConnection
                .preparedQuery("INSERT INTO ${getTablePrefix()}system_property (`option`, `value`) VALUES (?, ?)")
                .execute(Tuple.of("show_getting_started", "true"))
                .await()

            sqlConnection
                .preparedQuery("INSERT INTO ${getTablePrefix()}system_property (`option`, `value`) VALUES (?, ?)")
                .execute(Tuple.of("show_connect_server_info", "true"))
                .await()
        }
}