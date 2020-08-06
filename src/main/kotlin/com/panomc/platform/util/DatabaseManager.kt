package com.panomc.platform.util

import com.panomc.platform.migration.database.*
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.core.json.jsonObjectOf

class DatabaseManager(
    private val mVertx: Vertx,
    private val mLogger: Logger,
    private val mConfigManager: ConfigManager,
    setupManager: SetupManager
) {
    private lateinit var mAsyncSQLClient: AsyncSQLClient

    private val mMigrations = listOf(
        DatabaseMigration_1_2(),
        DatabaseMigration_2_3(),
        DatabaseMigration_3_4(),
        DatabaseMigration_4_5(),
        DatabaseMigration_5_6(),
        DatabaseMigration_6_7(),
        DatabaseMigration_7_8(),
        DatabaseMigration_8_9(),
        DatabaseMigration_9_10(),
        DatabaseMigration_10_11()
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
                            val handlers = mMigrations.map { it.migrate(sqlConnection, tablePrefix) }

                            var currentIndex = 0

                            fun invoke() {
                                val localHandler: (AsyncResult<*>) -> Unit = {
                                    fun check() {
                                        when {
                                            it.failed() -> closeConnection(connection) {
                                                mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                                            }
                                            currentIndex == handlers.lastIndex -> closeConnection(connection)
                                            else -> {
                                                currentIndex++

                                                invoke()
                                            }
                                        }
                                    }

                                    if (it.succeeded())
                                        mMigrations[currentIndex].updateSchemeVersion(sqlConnection, tablePrefix)
                                            .invoke { updateSchemeVersion ->
                                                if (updateSchemeVersion.failed())
                                                    closeConnection(connection) {
                                                        mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                                                    }
                                                else
                                                    check()
                                            }
                                    else
                                        check()
                                }

                                if (mMigrations[currentIndex].isMigratable(databaseVersion)) {
                                    if (currentIndex <= handlers.lastIndex)
                                        handlers[currentIndex].invoke(localHandler)
                                } else if (currentIndex == handlers.lastIndex)
                                    closeConnection(connection)
                                else {
                                    currentIndex++

                                    invoke()
                                }
                            }

                            invoke()
                        }
                    }
                }
            }
    }

    companion object {
        const val DATABASE_SCHEME_VERSION = 11
        const val DATABASE_SCHEME_VERSION_INFO = ""

        interface DatabaseMigration {
            val handlers: List<(
                sqlConnection: SQLConnection,
                tablePrefix: String,
                handler: (asyncResult: AsyncResult<*>) -> Unit
            ) -> SQLConnection>

            val FROM_SCHEME_VERSION: Int
            val SCHEME_VERSION: Int
            val SCHEME_VERSION_INFO: String

            fun isMigratable(version: Int) = version == FROM_SCHEME_VERSION

            fun migrate(
                sqlConnection: SQLConnection,
                tablePrefix: String
            ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit = { handler ->
                var currentIndex = 0

                fun invoke() {
                    val localHandler: (AsyncResult<*>) -> Unit = {
                        when {
                            it.failed() -> handler.invoke(it)
                            currentIndex == handlers.lastIndex -> handler.invoke(it)
                            else -> {
                                currentIndex++

                                invoke()
                            }
                        }
                    }

                    if (currentIndex <= handlers.lastIndex)
                        handlers[currentIndex].invoke(sqlConnection, tablePrefix, localHandler)
                }

                invoke()
            }

            fun updateSchemeVersion(
                sqlConnection: SQLConnection,
                tablePrefix: String
            ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
                sqlConnection.updateWithParams(
                    """
                        INSERT INTO ${tablePrefix}scheme_version (`key`, `extra`) VALUES (?, ?)
            """.trimIndent(),
                    JsonArray()
                        .add(SCHEME_VERSION.toString())
                        .add(DATABASE_SCHEME_VERSION_INFO)
                ) {
                    handler.invoke(it)
                }
            }
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

            val mySQLClientConfig = jsonObjectOf(
                Pair("host", host),
                Pair("port", port),
                Pair("database", databaseConfig["name"]),
                Pair("username", databaseConfig["username"]),
                Pair("password", if (databaseConfig["password"] == "") null else databaseConfig["password"])
            )

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
                val sqlConnection = connection.getSQLConnection()

                val databaseInitProcessHandlers = listOf(
                    DatabaseInitUtil.createUserTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createPermissionTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createTokenTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createPanelConfigTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createServerTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createSchemeVersionTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createAdminPermission(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createSystemPropertyTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createPanelNotificationsTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createPostTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createPostCategoryTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createTicketTable(sqlConnection, tablePrefix),
                    DatabaseInitUtil.createTicketCategoryTable(sqlConnection, tablePrefix)
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