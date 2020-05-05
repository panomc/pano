package com.panomc.platform.route.api.get.panel

import com.beust.klaxon.JsonObject
import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.*
import io.vertx.core.Handler
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PanelQuickNotificationsAndReadAPI : Api() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotificationsAndRead")

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

                getNotificationsData(context) { result ->
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

    private fun getNotificationsData(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getUserIDFromToken(connection, token, handler) { userID ->
                    getNotifications(connection, userID, handler) { notifications ->
                        getNotificationsCount(connection, userID, handler) { count ->
                            markNotificationsRead(connection, userID, handler) {
                                val result = mutableMapOf<String, Any?>(
                                    "notifications" to notifications,
                                    "notifications_count" to count
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
                    resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_72))
                }
        }
    }

    private fun getNotifications(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (notifications: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}panel_notification WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(userID).add(-1)) { queryResult ->
                if (queryResult.succeeded()) {
                    val notifications = mutableListOf<Map<String, Any>>()
                    if (queryResult.result().results.size > 0)
                        queryResult.result().results.forEach { categoryInDB ->
                            notifications.add(
                                mapOf(
                                    "id" to categoryInDB.getInteger(0),
                                    "type_ID" to categoryInDB.getString(2),
                                    "date" to categoryInDB.getLong(3),
                                    "status" to categoryInDB.getString(4),
                                    "isPersonal" to (categoryInDB.getInteger(1) == userID)
                                )
                            )
                        }

                    handler.invoke(notifications)
                } else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_71))
                    }
            }
    }


    private fun markNotificationsRead(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}panel_notification SET status = ? WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(NotificationStatus.READ).add(userID).add(-1)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_70))
                    }
            }
    }

    private fun getNotificationsCount(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (count: Int) -> Unit
    ) {
        val query =
            "SELECT count(`id`) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}panel_notification WHERE (`user_id` = ? OR `user_id` = ?) AND `status` = ? ORDER BY `date` DESC, `id` DESC"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(userID).add(-1).add(NotificationStatus.NOT_READ)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0))
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_69))
                    }
            }
    }
}