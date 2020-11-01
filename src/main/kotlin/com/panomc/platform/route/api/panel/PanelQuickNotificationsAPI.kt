package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class PanelQuickNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotifications")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_74))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
                        userID,
                        sqlConnection
                    ) { notifications, _ ->
                        if (notifications == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_73))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                                userID,
                                sqlConnection
                            ) { count, _ ->
                                if (count == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_67))
                                    }
                                else
                                    databaseManager.closeConnection(sqlConnection) {
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
            }
        }
    }
}