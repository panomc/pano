package com.panomc.platform.migration.database

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_5_6 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 5
    override val SCHEME_VERSION = 6
    override val SCHEME_VERSION_INFO = "Drop post, post_category, ticket and ticket_category."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            dropPanelPostTable(),
            dropPanelPostCategoryTable(),
            dropPanelTicketTable(),
            dropPanelTicketCategoryTable()
        )

    private fun dropPanelPostTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    DROP TABLE IF EXISTS `${tablePrefix}post`;
                """
        ) {
            handler.invoke(it)
        }
    }

    private fun dropPanelPostCategoryTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    DROP TABLE IF EXISTS `${tablePrefix}post_category`;
                """
        ) {
            handler.invoke(it)
        }
    }

    private fun dropPanelTicketTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    DROP TABLE IF EXISTS `${tablePrefix}ticket`;
                """
        ) {
            handler.invoke(it)
        }
    }

    private fun dropPanelTicketCategoryTable(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
                    DROP TABLE IF EXISTS `${tablePrefix}ticket_category`;
                """
        ) {
            handler.invoke(it)
        }
    }
}