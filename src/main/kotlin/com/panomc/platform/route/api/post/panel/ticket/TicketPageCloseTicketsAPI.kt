package com.panomc.platform.route.api.post.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
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

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val selectedTickets = data.getJsonArray("tickets")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                closeTickets(connection, selectedTickets, handler) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Successful())
                    }
                }
        }
    }

    private fun closeTickets(
        connection: Connection,
        selectedTickets: JsonArray,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val parameters = JsonArray()
        parameters.add(3)

        var selectedTicketsSQLText = ""

        selectedTickets.forEach {
            if (selectedTicketsSQLText.isEmpty())
                selectedTicketsSQLText = "?"
            else
                selectedTicketsSQLText += ", ?"

            parameters.add(it)
        }

        if (selectedTicketsSQLText.isEmpty())
            handler.invoke()
        else {
            val query =
                "UPDATE ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket SET status = ? WHERE id IN ($selectedTicketsSQLText)"

            databaseManager.getSQLConnection(connection).updateWithParams(query, parameters) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.TICKET_CLOSE_TICKETS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_113))
                    }
            }
        }
    }
}