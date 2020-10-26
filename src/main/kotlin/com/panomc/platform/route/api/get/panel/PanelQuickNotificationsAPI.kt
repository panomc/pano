package com.panomc.platform.route.api.get.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PanelQuickNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotifications")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                databaseManager.getSQLConnection(connection)
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_74))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
                        userID,
                        databaseManager.getSQLConnection(connection)
                    ) { notifications, _ ->
                        if (notifications == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_73))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                                userID,
                                databaseManager.getSQLConnection(connection)
                            ) { count, _ ->
                                if (count == null)
                                    databaseManager.closeConnection(connection) {
                                        handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_67))
                                    }
                                else
                                    databaseManager.closeConnection(connection) {
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