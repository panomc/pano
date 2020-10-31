package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class TicketPageCloseTicketsAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/close/selectedList")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val selectedTickets = data.getJsonArray("tickets")

        if (selectedTickets.isEmpty) {
            handler.invoke(
                Successful()
            )

            return
        }

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().ticketDao.closeTickets(
                selectedTickets,
                databaseManager.getSQLConnection(connection)
            ) { result, _ ->
                if (result == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.TICKET_CLOSE_TICKETS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_113))
                    }
                else
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Successful())
                    }
            }
        }
    }
}