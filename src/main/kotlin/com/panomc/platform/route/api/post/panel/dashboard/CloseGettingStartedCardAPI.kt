package com.panomc.platform.route.api.post.panel.dashboard

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CloseGettingStartedCardAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/dashboard/closeGettingStartedCard")

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
                        if (!isUserInstalledSystem) {
                            databaseManager.closeConnection(connection) {
                                context.reroute("/")
                            }
                        } else
                            connection.getSQLConnection().updateWithParams(
                                """
                                    UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}system_property SET value = ? WHERE `option` = ?
                                """.trimIndent(),
                                JsonArray().add("false").add("show_getting_started")
                            ) { queryResult ->
                                databaseManager.closeConnection(connection) {
                                    if (queryResult.succeeded())
                                        handler.invoke(Successful())
                                    else
                                        handler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_22))
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
                    resultHandler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_23))
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
                        resultHandler.invoke(Error(ErrorCode.CLOSE_GETTING_STARTED_CARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_24))
                    }
            }
    }
}