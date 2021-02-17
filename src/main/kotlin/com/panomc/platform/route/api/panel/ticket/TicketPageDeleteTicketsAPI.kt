package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class TicketPageDeleteTicketsAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/delete/selectedTickets")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val selectedTickets = data.getJsonArray("tickets")

        if (selectedTickets.isEmpty) {
            handler.invoke(Successful())

            return
        }

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                return@createConnection
            }

            databaseManager.getDatabase().ticketDao.delete(
                selectedTickets,
                sqlConnection
            ) { result, _ ->
                if (result == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_117))
                    }

                    return@delete
                }

                databaseManager.getDatabase().ticketMessageDao.deleteByTicketIDList(
                    selectedTickets,
                    sqlConnection
                ) { resultOfDeleteByTicketIDList, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (resultOfDeleteByTicketIDList == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_147))

                            return@closeConnection
                        }

                        handler.invoke(Successful())
                    }
                }
            }
        }
    }
}