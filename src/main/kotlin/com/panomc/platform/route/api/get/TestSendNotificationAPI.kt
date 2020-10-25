package com.panomc.platform.route.api.get

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import com.panomc.platform.util.NotificationStatus
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class TestSendNotificationAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/testNotification")

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getUserIDFromToken(connection, token, handler) { userID ->
                    insertNotification(connection, userID, handler) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Successful())
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
            "SELECT `user_id` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}token where `token` = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.UNKNOWN))
                }
        }
    }

    private fun insertNotification(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}panel_notification (user_id, type_ID, date, status) " +
                    "VALUES (?, ?, ?, ?)"

        databaseManager.getSQLConnection(connection)
            .updateWithParams(query, JsonArray().add(userID).add("TEST NOTIFICATION").add(System.currentTimeMillis()).add(NotificationStatus.NOT_READ)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.UNKNOWN))
                    }
            }
    }
}