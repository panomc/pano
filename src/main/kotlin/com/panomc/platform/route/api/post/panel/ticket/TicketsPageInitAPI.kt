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
import kotlin.math.ceil

class TicketsPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/ticketPage")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getCountOfTicketsByPageType(connection, pageType, handler) { countOfTicketsByPageType ->
                    var totalPage = ceil(countOfTicketsByPageType.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }
                    else
                        getTickets(connection, page, pageType, handler) { tickets ->
                            val result = mutableMapOf<String, Any?>(
                                "tickets" to tickets,
                                "tickets_count" to countOfTicketsByPageType,
                                "total_page" to totalPage
                            )

                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful(result))
                            }
                        }
                }
        }
    }

    private fun getCountOfTicketsByPageType(
        connection: Connection,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (ticketsCountByPageType: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket ${if (pageType != 2) "WHERE status = ?" else ""}"

        val parameters = JsonArray()

        if (pageType != 2)
            parameters.add(pageType)

        databaseManager.getSQLConnection(connection).queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_78))
                }
        }
    }

    private fun getTickets(
        connection: Connection,
        page: Int,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (tickets: List<Map<String, Any>>) -> Unit
    ) {
        var query =
            "SELECT id, title, category_id, user_id, date, status FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket ${if (pageType != 2) "WHERE status = ? " else ""}ORDER BY ${if (pageType == 2) "`status` ASC, " else ""}`date` DESC, `id` LIMIT 10 ${if (page == 1) "" else "OFFSET ${(page - 1) * 10}"}"

        val parameters = JsonArray()

        if (pageType != 2)
            parameters.add(pageType)

        databaseManager.getSQLConnection(connection).queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val tickets = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    query =
                        "SELECT id, title FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket_category"

                    databaseManager.getSQLConnection(connection).query(query) { categoryQueryResult ->
                        if (categoryQueryResult.succeeded()) {
                            val handlers: List<(handler: () -> Unit) -> Any> =
                                queryResult.result().results.map { ticketInDB ->
                                    val localHandler: (handler: () -> Unit) -> Any = { handler ->
                                        getUserNameFromID(
                                            connection,
                                            ticketInDB.getInteger(3),
                                            resultHandler
                                        ) { username ->
                                            var category: Any = "null"

                                            categoryQueryResult.result().results.forEach { categoryInDB ->
                                                if (categoryInDB.getInteger(0) == ticketInDB.getString(2).toInt())
                                                    category = mapOf(
                                                        "id" to categoryInDB.getInteger(0),
                                                        "title" to categoryInDB.getString(1)
                                                    )
                                            }

                                            if (category == "null")
                                                category = mapOf(
                                                    "title" to "-"
                                                )

                                            tickets.add(
                                                mapOf(
                                                    "id" to ticketInDB.getInteger(0),
                                                    "title" to ticketInDB.getString(1),
                                                    "category" to category,
                                                    "writer" to mapOf(
                                                        "username" to username
                                                    ),
                                                    "date" to ticketInDB.getInteger(4),
                                                    "status" to ticketInDB.getInteger(5)
                                                )
                                            )

                                            handler.invoke()
                                        }
                                    }

                                    localHandler
                                }

                            var currentIndex = -1

                            fun invoke() {
                                val localHandler: () -> Unit = {
                                    if (currentIndex == handlers.lastIndex)
                                        handler.invoke(tickets)
                                    else
                                        invoke()
                                }

                                currentIndex++

                                if (currentIndex <= handlers.lastIndex)
                                    handlers[currentIndex].invoke(localHandler)
                            }

                            invoke()
                        } else
                            databaseManager.closeConnection(connection) {
                                resultHandler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_77))
                            }
                    }
                } else
                    handler.invoke(tickets)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_76))
                }
        }
    }

    private fun getUserNameFromID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (username: String) -> Unit
    ) {
        val query =
            "SELECT username FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_75))
                }
        }
    }
}