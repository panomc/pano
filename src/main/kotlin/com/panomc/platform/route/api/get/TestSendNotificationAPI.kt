package com.panomc.platform.route.api.get

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.util.NotificationStatus
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
                        handler.invoke(Error(ErrorCode.UNKNOWN))
                    }
                else
                    databaseManager.getDatabase().panelNotificationDao.add(
                        PanelNotification(
                            -1,
                            userID,
                            "TEST NOTIFICATION",
                            System.currentTimeMillis(),
                            NotificationStatus.NOT_READ
                        ),
                        databaseManager.getSQLConnection(connection)
                    ) { result, _ ->
                        if (result == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN))
                            }
                        else
                            handler.invoke(Successful())
                    }
            }
        }
    }
}