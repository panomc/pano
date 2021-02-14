package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("ClassName")
class DatabaseMigration_11_12 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 11
    override val SCHEME_VERSION = 12
    override val SCHEME_VERSION_INFO = "Add register_date field to user table."

    override val handlers: List<(sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit> =
        listOf(
            addRegisterDateFieldToUserTable()
        )

    private fun addRegisterDateFieldToUserTable(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query("ALTER TABLE `${getTablePrefix()}user` ADD `register_date` MEDIUMTEXT;")
                .execute {
                    handler.invoke(it)
                }
        }
}