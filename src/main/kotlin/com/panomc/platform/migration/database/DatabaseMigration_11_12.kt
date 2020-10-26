package com.panomc.platform.migration.database

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_11_12 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 11
    override val SCHEME_VERSION = 12
    override val SCHEME_VERSION_INFO = "Add register_date field to user table."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            addRegisterDateFieldToUserTable()
        )

    private fun addRegisterDateFieldToUserTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    ALTER TABLE `${tablePrefix}user` 
                    ADD `register_date` MEDIUMTEXT;
                """
        ) {
            handler.invoke(it)
        }
    }
}