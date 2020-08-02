package com.panomc.platform.route.api.post.panel.ticket.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class TicketCategoryAddAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/ticket/category/add")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

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
            databaseManager.createConnection { connection, _ ->
                if (connection == null)
                    handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
                else
                    addCategoryToDB(connection, title, description, handler) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Successful())
                        }
                    }
            }
    }

    private fun addCategoryToDB(
        connection: Connection,
        title: String,
        description: String,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket_category (`title`, `description`) VALUES (?, ?)"

        databaseManager.getSQLConnection(connection)
            .updateWithParams(query, JsonArray().add(title).add(description)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke()
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_ADD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_91))
                    }
            }
    }
}