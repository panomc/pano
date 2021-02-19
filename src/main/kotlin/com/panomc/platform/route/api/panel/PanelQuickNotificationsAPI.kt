package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelQuickNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotifications")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection((this::createConnectionHandler)(handler, token))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        token: String
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            token,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_74))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
            userID,
            sqlConnection,
            (this::getLast5ByUserIDHandler)(handler, sqlConnection, userID)
        )
    }

    private fun getLast5ByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int
    ) = handler@{ notifications: List<Map<String, Any>>?, _: AsyncResult<*> ->
        if (notifications == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_73))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
            userID,
            sqlConnection,
            (this::getCountByUserIDHandler)(handler, sqlConnection, notifications)
        )
    }

    private fun getCountByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        notifications: List<Map<String, Any>>
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (count == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_67))

                return@closeConnection
            }

            handler.invoke(
                Successful(
                    mutableMapOf<String, Any?>(
                        "notifications" to notifications,
                        "notifications_count" to count
                    )
                )
            )
        }
    }
}