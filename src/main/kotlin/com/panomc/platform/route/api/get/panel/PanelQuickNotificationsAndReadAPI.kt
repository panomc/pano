package com.panomc.platform.route.api.get.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PanelQuickNotificationsAndReadAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotificationsAndRead")

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
                        handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_72))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
                        userID,
                        databaseManager.getSQLConnection(connection)
                    ) { notifications, _ ->
                        if (notifications == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_71))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                                userID,
                                databaseManager.getSQLConnection(connection)
                            ) { count, _ ->
                                if (count == null)
                                    databaseManager.closeConnection(connection) {
                                        handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_70))
                                    }
                                else
                                    databaseManager.getDatabase().panelNotificationDao.markReadLat5ByUserID(
                                        userID,
                                        databaseManager.getSQLConnection(connection)
                                    ) { result, _ ->
                                        if (result == null)
                                            databaseManager.closeConnection(connection) {
                                                handler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_69))
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
}