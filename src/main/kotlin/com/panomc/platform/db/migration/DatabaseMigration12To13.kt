package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlConnection

@Migration
class DatabaseMigration12To13(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 12
    override val SCHEME_VERSION = 13
    override val SCHEME_VERSION_INFO = "Add email_verified field to user table."

    override val handlers: List<suspend (sqlConnection: SqlConnection) -> Unit> =
        listOf(
            addEmailVerifiedFieldToUserTable()
        )

    private fun addEmailVerifiedFieldToUserTable(): suspend (sqlConnection: SqlConnection) -> Unit =
        { sqlConnection: SqlConnection ->
            sqlConnection
                .query("ALTER TABLE `${getTablePrefix()}user` ADD `email_verified` int(1) NOT NULL DEFAULT 0;")
                .execute()
                .await()
        }
}