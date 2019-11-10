package com.panomc.platform.route.api.get.panel.initPage

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class DashboardAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        val response = context.response()

        val auth = Auth()

        auth.isAdmin(context) { isAdmin ->
            if (isAdmin) {
                response
                    .putHeader("content-type", "application/json; charset=utf-8")

                getDashboardData(context) { result ->
                    if (result is Successful) {
                        val responseMap = mutableMapOf<String, Any?>(
                            "result" to "ok"
                        )

                        responseMap.putAll(result.map)

                        response.end(
                            JsonObject(
                                responseMap
                            ).toJsonString()
                        )
                    } else if (result is Error)
                        response.end(
                            JsonObject(
                                mapOf(
                                    "result" to "error",
                                    "error" to result.errorCode
                                )
                            ).toJsonString()
                        )
                }
            } else
                context.reroute("/")
        }
    }

    private fun getDashboardData(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val token = context.getCookie("pano_token").value

                getUserIDFromToken(connection, token, handler) { userID ->
                    isUserInstalledSystem(connection, userID, handler) { isUserInstalledSystem ->
                        getCountOfUsers(connection, handler) { countOfUsers ->
                            getCountOfPosts(connection, handler) { countOfPosts ->
                                val result = mutableMapOf<String, Any?>(
                                    "registered_player_count" to countOfUsers,
                                    "post_count" to countOfPosts
                                )

                                if (!isUserInstalledSystem) {
                                    result["getting_started_blocks"] = mapOf(
                                        "welcome_board" to false,
                                        "connect_board" to false
                                    )

                                    databaseManager.closeConnection(connection) {
                                        handler.invoke(Successful(result))
                                    }
                                } else
                                    getWelcomeBoardStatus(connection, handler) { showWelcomeBoard ->
                                        getConnectBoardStatus(connection, handler) { showConnectBoard ->
                                            result["getting_started_blocks"] = mapOf(
                                                "welcome_board" to showWelcomeBoard,
                                                "connect_board" to showConnectBoard
                                            )

                                            databaseManager.closeConnection(connection) {
                                                handler.invoke(Successful(result))
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getUserIDFromToken(
        connection: Connection,
        token: String,
        resultHandler: (result: Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT user_id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token where token = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_16))
                }
        }
    }

    private fun isUserInstalledSystem(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (isUserInstalledSystem: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(value) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property where option = ? and value = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add("who_installed_user_id").add(userID.toString())) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0) != 0)
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_17))
                    }
            }
    }

    private fun getCountOfUsers(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (userCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_18))
                }
        }
    }

    private fun getCountOfPosts(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (postCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_19))
                }
        }
    }

    private fun getWelcomeBoardStatus(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (showWelcomeBoard: Boolean) -> Unit
    ) {
        val query =
            "SELECT value FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property where option = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add("show_getting_started")) { queryResult ->
                if (queryResult.succeeded())
                    if (queryResult.result().results[0].getString(0) == null)
                        handler.invoke(false)
                    else handler.invoke(queryResult.result().results[0].getString(0)!!.toBoolean())
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_20))
                    }
            }
    }

    private fun getConnectBoardStatus(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (showConnectBoard: Boolean) -> Unit
    ) {
        val query =
            "SELECT value FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property where option = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add("show_connect_server_info")) { queryResult ->
                if (queryResult.succeeded())
                    if (queryResult.result().results[0].getString(0) == null)
                        handler.invoke(false)
                    else handler.invoke(queryResult.result().results[0].getString(0)!!.toBoolean())
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_21))
                    }
            }
    }
}