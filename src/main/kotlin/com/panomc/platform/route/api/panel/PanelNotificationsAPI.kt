package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PanelNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/notifications")

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
                        handler.invoke(Error(ErrorCode.PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_66))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.getCountByUserID(
                        userID,
                        sqlConnection
                    ) { count, _ ->
                        if (count == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_65))
                            }
                        else
                            databaseManager.getDatabase().panelNotificationDao.getAllByUserID(
                                userID,
                                sqlConnection
                            ) { notifications, _ ->
                                if (notifications == null)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_110))
                                    }
                                else
                                    databaseManager.getDatabase().panelNotificationDao.markReadAll(
                                        userID,
                                        sqlConnection
                                    ) { result, _ ->
                                        if (result == null)
                                            databaseManager.closeConnection(sqlConnection) {
                                                handler.invoke(Error(ErrorCode.PANEL_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_111))
                                            }
                                        else {
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
}