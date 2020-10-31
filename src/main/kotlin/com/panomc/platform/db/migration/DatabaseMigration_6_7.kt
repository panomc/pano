package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_6_7 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 6
    override val SCHEME_VERSION = 7
    override val SCHEME_VERSION_INFO = "Change date field in table panel_notifications to LONG type."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            changeField()
        )

    private fun changeField(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${databaseManager.getTablePrefix()}panel_notification` MODIFY `date` MEDIUMTEXT;
                """
            ) {
                handler.invoke(it)
            }
        }
}