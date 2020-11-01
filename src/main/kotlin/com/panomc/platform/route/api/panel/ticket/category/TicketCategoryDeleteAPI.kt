package com.panomc.platform.route.api.panel.ticket.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class TicketCategoryDeleteAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category/delete")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().ticketCategoryDao.isExistsByID(
                id,
                sqlConnection
            ) { exists, _ ->
                if (exists == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_90))
                    }
                else
                    if (!exists)
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.NOT_EXISTS))
                        }
                    else

                        databaseManager.getDatabase().ticketCategoryDao.deleteByID(
                            id,
                            sqlConnection
                        ) { result, _ ->
                            if (result == null)
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_89))
                                }
                            else
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Successful())
                                }
                        }

            }
        }
    }
}