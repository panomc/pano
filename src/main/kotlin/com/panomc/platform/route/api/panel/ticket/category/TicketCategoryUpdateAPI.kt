package com.panomc.platform.route.api.panel.ticket.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class TicketCategoryUpdateAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category/update")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")
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

                databaseManager.getDatabase().ticketCategoryDao.update(
                    TicketCategory(id, title, description),
                    sqlConnection
                ) { result, _ ->
                    if (result == null)
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.TICKET_CATEGORY_UPDATE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_92))
                        }
                    else
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Successful())
                        }
                }
            }
    }
}