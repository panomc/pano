package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_5_6 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 5
    override val SCHEME_VERSION = 6
    override val SCHEME_VERSION_INFO = "Drop post, post_category, ticket and ticket_category."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            dropPanelPostTable(),
            dropPanelPostCategoryTable(),
            dropPanelTicketTable(),
            dropPanelTicketCategoryTable()
        )

    private fun dropPanelPostTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}post`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropPanelPostCategoryTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}post_category`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropPanelTicketTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}ticket`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropPanelTicketCategoryTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}ticket_category`;
                """
            ) {
                handler.invoke(it)
            }
        }
}