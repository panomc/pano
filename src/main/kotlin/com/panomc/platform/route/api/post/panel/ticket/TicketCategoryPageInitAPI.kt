package com.panomc.platform.route.api.post.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil

class TicketCategoryPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/tickets/categoryPage")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val page = data.getInteger("page")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getTicketCategoriesCount(connection, handler) { countOfTicketCategories ->
                    var totalPage = ceil(countOfTicketCategories.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }
                    else
                        getCategoriesByPage(connection, page, handler) { categories ->
                            val result = mutableMapOf<String, Any?>(
                                "categories" to categories,
                                "category_count" to countOfTicketCategories,
                                "total_page" to totalPage,
                                "host" to "http://"
                            )

                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful(result))
                            }
                        }
                }
        }
    }

    private fun getTicketCategoriesCount(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (ticketCategoryCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket_category"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_80))
                }
        }
    }

    private fun getTicketsByCategory(
        connection: Connection,
        categoryID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (posts: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT id, title FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket WHERE category_id = ? ORDER BY `id` DESC LIMIT 5"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(categoryID)) { queryResult ->
                if (queryResult.succeeded()) {
                    val posts = mutableListOf<Map<String, Any>>()

                    queryResult.result().results.forEach { postInDB ->
                        posts.add(
                            mapOf(
                                "id" to postInDB.getInteger(0),
                                "title" to String(
                                    Base64.getDecoder().decode(postInDB.getString(1).toByteArray())
                                )
                            )
                        )
                    }

                    handler.invoke(posts)
                } else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_122))
                    }
            }
    }

    private fun getCountOfTicketsByCategory(
        connection: Connection,
        categoryID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (count: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket WHERE category_id = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(categoryID)) { queryResult ->
                if (queryResult.succeeded()) {
                    handler.invoke(queryResult.result().results[0].getInteger(0))
                } else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_123))
                    }
            }
    }

    private fun getCategoriesByPage(
        connection: Connection,
        page: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (categories: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT id, title, description FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket_category ORDER BY id DESC LIMIT 10 OFFSET ${(page - 1) * 10}"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded()) {
                val categories = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    val handlers: List<(handler: () -> Unit) -> Any> =
                        queryResult.result().results.map { categoryInDB ->
                            val localHandler: (handler: () -> Unit) -> Any = { handler ->
                                getCountOfTicketsByCategory(
                                    connection,
                                    categoryInDB.getInteger(0),
                                    resultHandler
                                ) { count ->
                                    getTicketsByCategory(
                                        connection,
                                        categoryInDB.getInteger(0),
                                        resultHandler
                                    ) { tickets ->
                                        categories.add(
                                            mapOf(
                                                "id" to categoryInDB.getInteger(0),
                                                "title" to String(
                                                    Base64.getDecoder().decode(categoryInDB.getString(1))
                                                ),
                                                "description" to String(
                                                    Base64.getDecoder().decode(categoryInDB.getString(2))
                                                ),
                                                "ticket_count" to count,
                                                "tickets" to tickets
                                            )
                                        )

                                        handler.invoke()
                                    }
                                }
                            }

                            localHandler
                        }

                    var currentIndex = -1

                    fun invoke() {
                        val localHandler: () -> Unit = {
                            if (currentIndex == handlers.lastIndex)
                                handler.invoke(categories)
                            else
                                invoke()
                        }

                        currentIndex++

                        if (currentIndex <= handlers.lastIndex)
                            handlers[currentIndex].invoke(localHandler)
                    }

                    invoke()
                } else
                    handler.invoke(categories)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.TICKET_CATEGORY_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_79))
                }
        }
    }
}