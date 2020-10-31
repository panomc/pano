package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_9_10 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 9
    override val SCHEME_VERSION = 10
    override val SCHEME_VERSION_INFO = "Update post, ticket and few other tables to be better."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            updatePostTableDateColumn(),
            updateTicketTableDateColumn(),
            updatePostTableTitleColumn(),
            updatePostTableMoveDateColumn(),
            updatePostTableCategoryIDColumn(),
            updateTicketTableTicketCategoryIDColumn(),
            updateTicketTableTitleColumn(),
            updateTicketCategoryTableTitleColumn(),
            updatePostCategoryTableTitleColumn(),
            updateTokenTableCreatedTimeColumn()
        )

    private fun updatePostTableDateColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}post` MODIFY `date` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updatePostTableMoveDateColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}post` MODIFY `move_date` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updatePostTableTitleColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}post` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updatePostTableCategoryIDColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}post` MODIFY `category_id` int(11);
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updatePostCategoryTableTitleColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}post_category` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updateTicketTableDateColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}ticket` MODIFY `date` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updateTicketTableTicketCategoryIDColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}ticket` CHANGE `ticket_category_id` `category_id` int(11);
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updateTicketTableTitleColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}ticket` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updateTicketCategoryTableTitleColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}ticket_category` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }

    private fun updateTokenTableCreatedTimeColumn(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
               ALTER TABLE `${databaseManager.getTablePrefix()}token` MODIFY `created_time` MEDIUMTEXT;
            """.trimIndent()
            ) {
                handler.invoke(it)
            }
        }
}