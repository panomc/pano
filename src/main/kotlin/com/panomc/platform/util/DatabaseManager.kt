package com.panomc.platform.util

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLConnection

class DatabaseManager(
    private val mVertx: Vertx,
    private val mLogger: Logger,
    private val mConfigManager: ConfigManager
) {

    private lateinit var mAsyncSQLClient: AsyncSQLClient

    private lateinit var mSqlConnection: SQLConnection

    fun createConnection() = Future.future<AsyncResult<SQLConnection>> { getConnectionFunction ->
        if (!::mAsyncSQLClient.isInitialized) {
            val databaseConfig = (mConfigManager.config["database"] as Map<String, Any>)

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

        mAsyncSQLClient.getConnection { getConnection ->
            getConnectionFunction.complete(
                if (getConnection.succeeded()) {
                    mSqlConnection = getConnection.result()

                    mSqlConnection
                    getConnection
                } else {
                    mLogger.error("Failed to connect database! Error is: ${getConnection.result()}")

                    getConnection
                }
            )
        }
    }

    fun closeConnection(handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        mSqlConnection.close {
            handler?.invoke(it)
        }
    }

    fun getSQLConnection() = mSqlConnection

    fun initDatabaseTables(handler: (asyncResult: AsyncResult<*>) -> Unit) {
        createConnection().setHandler {
            if (it.result().succeeded()) {
                val tablePrefix = (mConfigManager.config["database"] as Map<*, *>)["prefix"].toString()

                val databaseInitProcessHandlers = listOf(
                    DatabaseInitUtil.createUserTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createPermissionTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createTokenTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createPanelConfigTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createServerTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createPostTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createPostCategoryTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createTicketTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createTicketCategoryTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createSchemeVersionTable(mSqlConnection, tablePrefix),
                    DatabaseInitUtil.createAdminPermission(mSqlConnection, tablePrefix)
                )

                var currentIndex = 0

                fun invoke() {
                    val localHandler: (AsyncResult<*>) -> Unit = {
                        when {
                            it.failed() -> closeConnection { _ ->
                                handler.invoke(it)
                            }
                            currentIndex == databaseInitProcessHandlers.lastIndex -> closeConnection { _ ->
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
                closeConnection { _ ->
                    handler.invoke(it.result())
                }
        }
    }
}