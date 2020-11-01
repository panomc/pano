package com.panomc.platform.route.api.panel.ticket.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class TicketCategoryAddAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category/add")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val title = data.getString("title")
        val description = data.getString("description")

        val errors = mutableMapOf<String, Boolean>()

        if (title.isEmpty() || title.length > 32)
            errors["title"] = true

        if (description.isEmpty())
            errors["description"] = true

        if (errors.isNotEmpty())
            handler.invoke(Errors(errors))
        else
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                    return@createConnection
                }

                databaseManager.getDatabase().ticketCategoryDao.add(
                    TicketCategory(-1, title, description),
                    sqlConnection
                ) { result, _ ->
                    if (result == null)
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_91))
                        }
                    else
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Successful())
                        }
                }
            }
    }
}