package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_12_13 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 12
    override val SCHEME_VERSION = 13
    override val SCHEME_VERSION_INFO = "Add email_verified field to user table."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            addEmailVerifiedFieldToUserTable()
        )

    private fun addEmailVerifiedFieldToUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}user` 
                    ADD `email_verified` int(1) NOT NULL DEFAULT 0;
                """
            ) {
                handler.invoke(it)
            }
        }
}