package com.panomc.platform.route.api.panel

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
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_72))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.getLast5ByUserID(
                        userID,
                        sqlConnection
                    ) { notifications, _ ->
                        if (notifications == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_71))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                                userID,
                                sqlConnection
                            ) { count, _ ->
                                if (count == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_70))
                                    }
                                else
                                    databaseManager.getDatabase().panelNotificationDao.markReadLat5ByUserID(
                                        userID,
                                        sqlConnection
                                    ) { result, _ ->
                                        if (result == null)
                                            databaseManager.closeConnection(sqlConnection) {
                                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_69))
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
}