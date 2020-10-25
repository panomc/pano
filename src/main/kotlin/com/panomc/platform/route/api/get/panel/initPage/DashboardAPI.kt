package com.panomc.platform.route.api.get.panel.initPage

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

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else {
                val token = context.getCookie("pano_token").value

                getUserIDFromToken(connection, token, handler) { userID ->
                    isUserInstalledSystem(connection, userID, handler) { isUserInstalledSystem ->
                        getCountOfUsers(connection, handler) { countOfUsers ->
                            getCountOfPosts(connection, handler) { countOfPosts ->
                                getCountOfTickets(connection, handler) { countOfTickets ->
                                    getCountOfOpenTickets(connection, handler) { countOfOpenTickets ->
                                        getTickets(connection, handler) { tickets ->
                                            val result = mutableMapOf<String, Any?>(
                                                "registered_player_count" to countOfUsers,
                                                "post_count" to countOfPosts,
                                                "tickets_count" to countOfTickets,
                                                "open_tickets_count" to countOfOpenTickets,
                                                "tickets" to tickets
                                            )

                                            if (!isUserInstalledSystem) {
                                                result["getting_started_blocks"] = mapOf(
                                                    "welcome_board" to false
                                                )

                                                databaseManager.closeConnection(connection) {
                                                    handler.invoke(Successful(result))
                                                }
                                            } else
                                                getWelcomeBoardStatus(connection, handler) { showWelcomeBoard ->
                                                    result["getting_started_blocks"] = mapOf(
                                                        "welcome_board" to showWelcomeBoard
                                                    )

                                                    databaseManager.closeConnection(connection) {
                                                        handler.invoke(Successful(result))
                                                    }
                                                }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getCountOfPosts(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (postCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_19))
                }
        }
    }

    private fun getCountOfTickets(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (ticketsCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_112))
                }
        }
    }

    private fun getCountOfOpenTickets(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (openTicketsCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket WHERE status = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(1)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_118))
                }
        }
    }

    private fun getUserIDFromToken(
        connection: Connection,
        token: String,
        resultHandler: (result: Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT `user_id` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}token where `token` = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_16))
                }
        }
    }

    private fun isUserInstalledSystem(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (isUserInstalledSystem: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(`value`) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}system_property where `option` = ? and value = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add("who_installed_user_id").add(userID.toString())) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0) != 0)
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_17))
                    }
            }
    }

    private fun getCountOfUsers(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (userCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}user"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_18))
                }
        }
    }

    private fun getWelcomeBoardStatus(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (showWelcomeBoard: Boolean) -> Unit
    ) {
        val query =
            "SELECT value FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}system_property where `option` = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add("show_getting_started")) { queryResult ->
                if (queryResult.succeeded())
                    if (queryResult.result().results[0].getString(0) == null)
                        handler.invoke(false)
                    else handler.invoke(queryResult.result().results[0].getString(0)!!.toBoolean())
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.DASHBOARD_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_20))
                    }
            }
    }

    private fun getTickets(
        connection: Connection,
        resultHandler: (result: Result) -> Unit,
        handler: (tickets: List<Map<String, Any>>) -> Unit
    ) {
        var query =
            "SELECT id, title, category_id, user_id, date, status FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket ORDER BY `date` DESC, `id` LIMIT 5"

        val parameters = JsonArray()

        databaseManager.getSQLConnection(connection).queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val tickets = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    query =
                        "SELECT id, title FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}ticket_category"

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
                                                if (categoryInDB.getInteger(0) == ticketInDB.getInteger(2).toInt())
                                                    category = mapOf(
                                                        "id" to categoryInDB.getInteger(0),
                                                        "title" to String(
                                                            Base64.getDecoder()
                                                                .decode(categoryInDB.getString(1).toByteArray())
                                                        )
                                                    )
                                            }

                                            if (category == "null")
                                                category = mapOf(
                                                    "title" to "-"
                                                )

                                            tickets.add(
                                                mapOf(
                                                    "id" to ticketInDB.getInteger(0),
                                                    "title" to String(
                                                        Base64.getDecoder()
                                                            .decode(ticketInDB.getString(1).toByteArray())
                                                    ),
                                                    "category" to category,
                                                    "writer" to mapOf(
                                                        "username" to username
                                                    ),
                                                    "date" to ticketInDB.getString(4),
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
            "SELECT username FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

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