package com.panomc.platform.route.api.panel.dashboard

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class CloseConnectServerCardAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/dashboard/closeConnectServerCard")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            val token = context.getCookie("pano_token").value

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_26))
                    }
                else
                    databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                        userID,
                        sqlConnection
                    ) { isUserInstalledSystem, _ ->
                        if (isUserInstalledSystem == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_27))
                            }
                        else
                            databaseManager.getDatabase().systemPropertyDao.update(
                                SystemProperty(
                                    -1,
                                    "false",
                                    "show_connect_server_info"
                                ), sqlConnection
                            ) { updateResult, _ ->
                                databaseManager.closeConnection(sqlConnection) {
                                    if (updateResult == null)
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_25))
                                    else
                                        handler.invoke(Successful())
                                }
                            }
                    }
            }
        }
    }
}