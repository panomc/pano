package com.panomc.platform.migration.database

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_9_10 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 9
    override val SCHEME_VERSION = 10
    override val SCHEME_VERSION_INFO = "Update post, ticket and few other tables to be better."

    override val handlers: List<(sqlConnection: SQLConnection, tablePrefix: String, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
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

    private fun updatePostTableDateColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}post` MODIFY `date` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updatePostTableMoveDateColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}post` MODIFY `move_date` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updatePostTableTitleColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}post` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updatePostTableCategoryIDColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}post` MODIFY `category_id` int(11);
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updatePostCategoryTableTitleColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}post_category` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updateTicketTableDateColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}ticket` MODIFY `date` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updateTicketTableTicketCategoryIDColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}ticket` CHANGE `ticket_category_id` `category_id` int(11);
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updateTicketTableTitleColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}ticket` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updateTicketCategoryTableTitleColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}ticket_category` MODIFY `title` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }

    private fun updateTokenTableCreatedTimeColumn(): (
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection = { sqlConnection, tablePrefix, handler ->
        sqlConnection.query(
            """
               ALTER TABLE `${tablePrefix}token` MODIFY `created_time` MEDIUMTEXT;
            """.trimIndent()
        ) {
            handler.invoke(it)
        }
    }
}