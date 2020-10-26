package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.ServerDao
import com.panomc.platform.model.Server
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class ServerDaoImpl(override val tableName: String = "server") : DaoImpl(), ServerDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Connected server table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    override fun add(
        server: Server,
        sqlConnection: SQLConnection,
        handler: (token: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val key = Keys.keyPairFor(SignatureAlgorithm.RS256)

        val token = Jwts.builder()
            .setSubject("SERVER_CONNECT")
            .signWith(
                key.private
            )
            .compact()

        val query =
            "INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (name, player_count, max_player_count, server_type, server_version, favicon, secret_key, public_key, token, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(server.name)
                .add(server.playerCount)
                .add(server.maxPlayerCount)
                .add(server.type)
                .add(server.version)
                .add(server.favicon)
                .add(Base64.getEncoder().encodeToString(key.private.encoded))
                .add(Base64.getEncoder().encodeToString(key.public.encoded))
                .add(token)
                .add(server.status)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(token, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}