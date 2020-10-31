package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_7_8 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 7
    override val SCHEME_VERSION = 8
    override val SCHEME_VERSION_INFO = "Restore post, postCategory, ticket and ticketCategory tables."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createPostTable(),
            createPostCategoryTable(),
            createTicketTable(),
            createTicketCategoryTable()
        )

    private fun createPostTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}post` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` varchar(255) NOT NULL,
              `category_id` varchar(11) NOT NULL,
              `writer_user_id` int(11) NOT NULL,
              `post` longblob NOT NULL,
              `date` int(50) NOT NULL,
              `move_date` int(50) NOT NULL,
              `status` int(1) NOT NULL,
              `image` longblob NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Posts table.';
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun createPostCategoryTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}post_category` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` varchar(255) NOT NULL,
              `description` text NOT NULL,
              `url` varchar(255) NOT NULL,
              `color` varchar(6) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Post category table.';
        """
            ) {
            if (it.succeeded())
                sqlConnection.updateWithParams(
                    """
                        INSERT INTO ${getTablePrefix()}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
            """.trimIndent(),
                    JsonArray().add("Genel").add("Genel").add("genel").add("48CFAD")
                ) {
                    if (it.succeeded())
                        sqlConnection.updateWithParams(
                            """
                        INSERT INTO ${getTablePrefix()}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
            """.trimIndent(),
                            JsonArray().add("Duyuru").add("Duyuru").add("duyuru").add("5D9CEC")
                        ) {
                            if (it.succeeded())
                                sqlConnection.updateWithParams(
                                    """
                        INSERT INTO ${getTablePrefix()}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
            """.trimIndent(),
                                    JsonArray().add("Haber").add("Haber").add("haber").add("FFCE54")
                                ) {
                                    handler.invoke(it)
                                }
                            else
                                handler.invoke(it)
                        }
                    else
                        handler.invoke(it)
                }
            else
                handler.invoke(it)
            }
        }

    private fun createTicketTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}ticket` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` varchar(255) NOT NULL,
              `ticket_category_id` varchar(11) NOT NULL,
              `user_id` int(11) NOT NULL,
              `date` int(50) NOT NULL,
              `status` int(1) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Tickets table.';
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun createTicketCategoryTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}ticket_category` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` varchar(255) NOT NULL,
              `description` text,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ticket category table.';
        """
            ) {
            if (it.succeeded())
                sqlConnection.queryWithParams(
                    """
                        INSERT INTO ${getTablePrefix()}ticket_category (title) VALUES (?)
            """.trimIndent(),
                    JsonArray().add("Genel")
                ) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }
}