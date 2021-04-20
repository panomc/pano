package com.panomc.platform.route.api

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.NotificationStatus
import io.vertx.ext.web.RoutingContext

class TestSendNotificationAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/testNotification")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie(LoginUtil.COOKIE_NAME).value

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN))
                    }

                    return@getUserIDFromToken
                }

                databaseManager.getDatabase().panelNotificationDao.add(
                    PanelNotification(
                        -1,
                        userID,
                        "TEST NOTIFICATION",
                        System.currentTimeMillis(),
                        NotificationStatus.NOT_READ
                    ),
                    sqlConnection
                ) { result, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (result == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN))

                            return@closeConnection
                        }

                        handler.invoke(Successful())
                    }
                }
            }
        }
    }
}