package com.panomc.platform.util

import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLConnection

class DatabaseManager(
    private val mVertx: Vertx,
    private val mLogger: Logger,
    private val mConfigManager: ConfigManager,
    setupManager: SetupManager
) {
    private lateinit var mAsyncSQLClient: AsyncSQLClient

    private val mMigrations = listOf<DatabaseMigration>(
    )

    init {
        if (setupManager.isSetupDone())
            createConnection { connection, _ ->
                if (connection != null) {
                    val tablePrefix = (mConfigManager.config["database"] as Map<*, *>)["prefix"].toString()

                    val query = "SELECT MAX(`key`) FROM ${tablePrefix}scheme_version"

                    val sqlConnection = connection.getSQLConnection()

                    sqlConnection.query(
                        query
                    ) { queryResult ->
                        val databaseVersion = queryResult.result().results[0].getString(0).toIntOrNull() ?: 0

                        if (databaseVersion == 0)
                            mLogger.error("Database Error: Database scheme is not correct, please reinstall platform")
                        else {
                            val handlers = mMigrations.map { it.migrate(sqlConnection, databaseVersion, tablePrefix) }

                            var currentIndex = 0

                            fun invoke() {
                                val localHandler: (AsyncResult<*>) -> Unit = {
                                    when {
                                        it.failed() -> closeConnection(connection)
                                        currentIndex == handlers.lastIndex -> closeConnection(connection)
                                        else -> {
                                            currentIndex++

                                            invoke()
                                        }
                                    }
                                }

                                if (currentIndex <= handlers.lastIndex)
                                    handlers[currentIndex].invoke(localHandler)
                            }

                            invoke()
                        }
                    }
                }
            }
    }

    companion object {
        interface DatabaseMigration {
            fun migrate(
                sqlConnection: SQLConnection,
                version: Int,
                tablePrefix: String
            ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection
        }
    }

    fun createConnection(handler: (connection: Connection?, asyncResult: AsyncResult<SQLConnection>) -> Unit) {
        if (!::mAsyncSQLClient.isInitialized) {
            val databaseConfig = (mConfigManager.config["database"] as Map<*, *>)

            var port = 3306
            var host = databaseConfig["host"] as String

            if (host.contains(":")) {
                val splitHost = host.split(":")

                host = splitHost[0]

                port = splitHost[1].toInt()
            }

            val mySQLClientConfig = io.vertx.core.json.JsonObject()
                .put("host", host)
                .put("port", port)
                .put("database", databaseConfig["name"])
                .put("username", databaseConfig["username"])
                .put("password", databaseConfig["password"])

            mAsyncSQLClient = MySQLClient.createShared(mVertx, mySQLClientConfig, "MysqlLoginPool")
        }

        Connection.createConnection(mLogger, mAsyncSQLClient) { connection, asyncResult ->
            handler.invoke(connection, asyncResult)
        }
    }

    fun closeConnection(connection: Connection, handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        connection.closeConnection(handler)
    }

    fun getSQLConnection(connection: Connection) = connection.getSQLConnection()

    fun initDatabaseTables(handler: (asyncResult: AsyncResult<*>) -> Unit) {
        createConnection { connection, asyncResult ->
            if (connection !== null) {
                val tablePrefix = (mConfigManager.config["database"] as Map<*, *>)["prefix"].toString()

                val databaseInitProcessHandlers = listOf(
                    DatabaseInitUtil.createUserTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createPermissionTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createTokenTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createPanelConfigTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createServerTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createPostTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createPostCategoryTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createTicketTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createTicketCategoryTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createSchemeVersionTable(connection.getSQLConnection(), tablePrefix),
                    DatabaseInitUtil.createAdminPermission(connection.getSQLConnection(), tablePrefix)
                )

                var currentIndex = 0

                fun invoke() {
                    val localHandler: (AsyncResult<*>) -> Unit = {
                        when {
                            it.failed() -> closeConnection(connection) { _ ->
                                handler.invoke(it)
                            }
                            currentIndex == databaseInitProcessHandlers.lastIndex -> closeConnection(connection) { _ ->
                                handler.invoke(it)
                            }
                            else -> {
                                currentIndex++

                                invoke()
                            }
                        }
                    }

                    if (currentIndex <= databaseInitProcessHandlers.lastIndex)
                        databaseInitProcessHandlers[currentIndex].invoke(localHandler)
                }

                invoke()
            } else
                handler.invoke(asyncResult)
        }
    }
}