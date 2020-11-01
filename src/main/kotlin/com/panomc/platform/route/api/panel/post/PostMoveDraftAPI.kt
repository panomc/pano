package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class PostMoveDraftAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/moveDraft")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                sqlConnection
            ) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_109))
                    }

                    return@isExistsByID
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }

                    return@isExistsByID
                }

                databaseManager.getDatabase().postDao.moveDraftByID(
                    id,
                    sqlConnection
                ) { result, _ ->
                    if (result == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_108))
                        }

                        return@moveDraftByID
                    }

                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Successful())
                    }
                }
            }
        }
    }
}