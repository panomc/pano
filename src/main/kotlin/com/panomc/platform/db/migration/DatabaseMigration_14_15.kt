package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_14_15 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 14
    override val SCHEME_VERSION = 15
    override val SCHEME_VERSION_INFO = "Add panel field to ticket_message table."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            addPanelFieldToTicketMessageTable()
        )

    private fun addPanelFieldToTicketMessageTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}ticket_message` 
                    ADD `panel` int(1) NOT NULL DEFAULT 0;
                """
            ) {
                handler.invoke(it)
            }
        }
}