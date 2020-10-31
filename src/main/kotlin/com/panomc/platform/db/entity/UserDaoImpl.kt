package com.panomc.platform.db.entity

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.UserDao
import com.panomc.platform.db.model.User
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class UserDaoImpl(override val tableName: String = "user") : DaoImpl(), UserDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `username` varchar(16) NOT NULL UNIQUE,
              `email` varchar(255) NOT NULL UNIQUE,
              `password` varchar(255) NOT NULL,
              `permission_id` int(11) NOT NULL,
              `registered_ip` varchar(255) NOT NULL,
              `secret_key` text NOT NULL,
              `public_key` text NOT NULL,
              `register_date` MEDIUMTEXT NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='User Table';
        """
        ) {
            handler.invoke(it)
        }
    }

    override fun add(
        sqlConnection: SQLConnection,
        user: User,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (username, email, password, registered_ip, permission_id, secret_key, public_key, register_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"

        val key = Keys.keyPairFor(SignatureAlgorithm.RS256)

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(user.username)
                .add(user.email)
                .add(user.password)
                .add(user.ipAddress)
                .add(user.permissionID)
                .add(Base64.getEncoder().encodeToString(key.private.encoded))
                .add(Base64.getEncoder().encodeToString(key.public.encoded))
                .add(System.currentTimeMillis())
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else {
                val errorCode = ErrorCode.REGISTER_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_2

                handler.invoke(Error(errorCode), queryResult)
            }
        }
    }

    override fun isEmailExists(
        email: String,
        sqlConnection: SQLConnection,
        handler: (isEmailExists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(email) FROM `${databaseManager.getTablePrefix() + tableName}` where email = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(email)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getUserIDFromUsername(
        username: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id FROM `${databaseManager.getTablePrefix() + tableName}` where username = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(username)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getPermissionIDFromUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT permission_id FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getSecretKeyByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (secretKey: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT secret_key FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isLoginCorrect(
        email: String,
        password: String,
        sqlConnection: SQLConnection,
        handler: (isLoginCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(email) FROM `${databaseManager.getTablePrefix() + tableName}` where email = ? and password = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(email).add(password)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query = "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}`"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getUsernameFromUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (username: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT username FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (user: User?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `username`, `email`, `password`, `registered_ip`, `permission_id` FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(
                    User(
                        userID,
                        queryResult.result().results[0].getString(0),
                        queryResult.result().results[0].getString(1),
                        queryResult.result().results[0].getString(2),
                        queryResult.result().results[0].getString(3),
                        queryResult.result().results[0].getInteger(4)
                    ),
                    queryResult
                )
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun countByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}` ${if (pageType == 2) "WHERE permission_id != ?" else ""}"

        val parameters = JsonArray()

        if (pageType == 2)
            parameters.add(-1)

        sqlConnection.queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getAllByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (userList: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, username, register_date FROM `${databaseManager.getTablePrefix() + tableName}` ${if (pageType == 2) "WHERE permission_id != ? " else ""}ORDER BY `id` LIMIT 10 ${if (page == 1) "" else "OFFSET ${(page - 1) * 10}"}"

        val parameters = JsonArray()

        if (pageType == 2)
            parameters.add(-1)

        sqlConnection.queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val players = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    val handlers: List<(handler: () -> Unit) -> Any> =
                        queryResult.result().results.map { playerInDB ->
                            val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                databaseManager.getDatabase().ticketDao.countByUserID(
                                    playerInDB.getInteger(0),
                                    sqlConnection
                                ) { count, asyncResult ->
                                    if (count == null) {
                                        handler.invoke(null, asyncResult)

                                        return@countByUserID
                                    }

                                    players.add(
                                        mapOf(
                                            "id" to playerInDB.getInteger(0),
                                            "username" to playerInDB.getString(1),
                                            "ticket_count" to count,
                                            "register_date" to playerInDB.getString(2)
                                        )
                                    )

                                    localHandler.invoke()
                                }
                            }

                            localHandler
                        }

                    var currentIndex = -1

                    fun invoke() {
                        val localHandler: () -> Unit = {
                            if (currentIndex == handlers.lastIndex)
                                handler.invoke(players, queryResult)
                            else
                                invoke()
                        }

                        currentIndex++

                        if (currentIndex <= handlers.lastIndex)
                            handlers[currentIndex].invoke(localHandler)
                    }

                    invoke()
                } else
                    handler.invoke(players, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getUserIDFromUsernameOrEmail(
        usernameOrEmail: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id FROM `${databaseManager.getTablePrefix() + tableName}` where username = ? or email = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(usernameOrEmail).add(usernameOrEmail)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun add(user: User, handler: (result: Result?) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection != null) {
                add(databaseManager.getSQLConnection(connection), user) { result, _ ->
                    databaseManager.closeConnection(connection) {
                        handler.invoke(result)
                    }
                }
            } else
                handler.invoke(null)
        }
    }
}