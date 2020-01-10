package com.panomc.platform.util

import com.panomc.platform.util.DatabaseManager.Companion.DATABASE_SCHEME_VERSION
import com.panomc.platform.util.DatabaseManager.Companion.DATABASE_SCHEME_VERSION_INFO
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

object DatabaseInitUtil {

    fun createUserTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}user` (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(16) NOT NULL UNIQUE,
              `email` varchar(255) NOT NULL UNIQUE,
              `password` varchar(255) NOT NULL,
              `permission_id` int(11) NOT NULL,
              `registered_ip` varchar(255) NOT NULL,
              `secret_key` text NOT NULL,
              `public_key` text NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='User Table';
        """
        ) {
            handler.invoke(it)
        }
    }

    fun createPermissionTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}permission` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(16) NOT NULL UNIQUE,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Permission Table';
        """
        ) {
            handler.invoke(it)
        }
    }

    fun createTokenTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}token` (
              `id` int NOT NULL AUTO_INCREMENT,
              `token` text NOT NULL,
              `created_time` varchar(255) NOT NULL,
              `user_id` int(11) NOT NULL,
              `subject` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Token Table';
        """
        ) {
            handler.invoke(it)
        }
    }

    fun createPanelConfigTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}panel_config` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int(11) NOT NULL,
              `option` varchar(255) NOT NULL,
              `value` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Panel Config per player table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    fun createServerTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}server` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(255) NOT NULL,
              `player_count` int(11) NOT NULL,
              `max_player_count` int(11) NOT NULL,
              `server_type` varchar(255) NOT NULL,
              `server_version` varchar(255) NOT NULL,
              `favicon` text NOT NULL,
              `secret_key` text NOT NULL,
              `public_key` text NOT NULL,
              `token` text NOT NULL,
              `permission_granted` int(1) default 0,
              `status` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Connected server table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    fun createPostTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}post` (
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

    fun createPostCategoryTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}post_category` (
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
                        INSERT INTO ${tablePrefix}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
            """.trimIndent(),
                    JsonArray().add("Genel").add("Genel").add("genel").add("48CFAD")
                ) {
                    if (it.succeeded())
                        sqlConnection.updateWithParams(
                            """
                        INSERT INTO ${tablePrefix}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
            """.trimIndent(),
                            JsonArray().add("Duyuru").add("Duyuru").add("duyuru").add("5D9CEC")
                        ) {
                            if (it.succeeded())
                                sqlConnection.updateWithParams(
                                    """
                        INSERT INTO ${tablePrefix}post_category (title, description, url, color) VALUES (?, ?, ?, ?)
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

    fun createTicketTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}ticket` (
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

    fun createTicketCategoryTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}ticket_category` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` varchar(255) NOT NULL,
              `description` text NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Ticket category table.';
        """
        ) {
            if (it.succeeded())
                sqlConnection.queryWithParams(
                    """
                        INSERT INTO ${tablePrefix}ticket_category (title) VALUES (?)
            """.trimIndent(),
                    JsonArray().add("Genel")
                ) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }

    fun createSchemeVersionTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}scheme_version` (
              `when` timestamp not null default CURRENT_TIMESTAMP,
              `key` varchar(256) not null,
              `extra` varchar(256),
              PRIMARY KEY (`key`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Database scheme version table.';
        """
        ) {
            if (it.succeeded())
                sqlConnection.queryWithParams(
                    """
                        SELECT COUNT(`key`) FROM ${tablePrefix}scheme_version where `key` = ?
            """.trimIndent(),
                    JsonArray().add(DATABASE_SCHEME_VERSION.toString())
                ) {
                    if (it.failed() || it.result().results[0].getInteger(0) != 0)
                        handler.invoke(it)
                    else
                        sqlConnection.updateWithParams(
                            """
                        INSERT INTO ${tablePrefix}scheme_version (`key`, `extra`) VALUES (?, ?)
            """.trimIndent(),
                            JsonArray()
                                .add(DATABASE_SCHEME_VERSION.toString())
                                .add(DATABASE_SCHEME_VERSION_INFO)
                        ) {
                            handler.invoke(it)
                        }
                }
            else
                handler.invoke(it)
        }
    }

    fun createAdminPermission(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.queryWithParams(
            """
            SELECT COUNT(name) FROM ${tablePrefix}permission where name = ?
        """,
            JsonArray().add("admin")
        ) {
            if (it.succeeded() && it.result().results[0].getInteger(0) == 0)
                sqlConnection.updateWithParams(
                    """
                        INSERT INTO ${tablePrefix}permission (name) VALUES (?)
            """.trimIndent(),
                    JsonArray().add("admin")
                ) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }

    fun createSystemPropertyTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}system_property` (
              `id` int NOT NULL AUTO_INCREMENT,
              `option` text NOT NULL,
              `value` text NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='System Property table.';
        """
        ) {
            if (it.succeeded())
                sqlConnection.updateWithParams(
                    """
                    INSERT INTO ${tablePrefix}system_property (`option`, `value`) VALUES (?, ?)
            """.trimIndent(),
                    JsonArray()
                        .add("show_getting_started")
                        .add("true")
                ) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }

    fun createPanelNotificationsTable(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${tablePrefix}panel_notification` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,
              `type_ID` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Panel Notification table.';
        """
        ) {
            handler.invoke(it)
        }
    }
}