package com.panomc.platform.route.api.get.panel.initPage

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val token = context.getCookie("pano_token").value

                getUserIDFromToken(connection, token, handler) { userID ->
                    isUserInstalledSystem(connection, userID, handler) { isUserInstalledSystem ->
                        getCountOfUsers(connection, handler) { countOfUsers ->
                            getCountOfPosts(connection, handler) { countOfPosts ->
                                getCountOfTickets(connection, handler) { countOfTickets ->
                                    val result = mutableMapOf<String, Any?>(
                                        "registered_player_count" to countOfUsers,
                                        "post_count" to countOfPosts,
                                        "tickets_count" to countOfTickets
                                    )

                                    if (!isUserInstalledSystem) {
                                        result["getting_started_blocks"] = mapOf(
                                            "welcome_board" to false
                                        )

                                        databaseManager.closeConnection(connection) {
                                            handler.invoke(Successful(result))
                                        }
                                    } else
                                        getWelcomeBoardStatus(connection, handler) { showWelcomeBoard ->
                                            result["getting_started_blocks"] = mapOf(
                                                "welcome_board" to showWelcomeBoard
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

    private fun getCountOfTickets(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (ticketsCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_112))
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
            "SELECT `user_id` FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token where `token` = ?"

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
            "SELECT COUNT(`value`) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property where `option` = ? and value = ?"

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

    private fun getWelcomeBoardStatus(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (showWelcomeBoard: Boolean) -> Unit
    ) {
        val query =
            "SELECT value FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property where `option` = ?"

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
}