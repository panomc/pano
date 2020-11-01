package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class PostOnlyPublishAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/onlyPublish")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        val token = context.getCookie("pano_token").value

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
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_104))
                    }

                    return@isExistsByID
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }

                    return@isExistsByID
                }

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                    token,
                    sqlConnection
                ) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_105))
                        }

                        return@getUserIDFromToken
                    }

                    databaseManager.getDatabase().postDao.publishByID(
                        id,
                        userID,
                        sqlConnection
                    ) { result, _ ->
                        if (result == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_103))
                            }

                            return@publishByID
                        }

                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Successful())
                        }
                    }
                }
            }
        }
    }
}