package com.panomc.platform.route.api.post.panel.players

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

class PlayersPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/playersPage")

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
                getCountOfPlayersByPageType(connection, pageType, handler) { playersCountByPageType ->
                    var totalPage = ceil(playersCountByPageType.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }
                    else
                        getPlayers(connection, page, pageType, handler) { players ->
                            val result = mutableMapOf<String, Any?>(
                                "players" to players,
                                "players_count" to playersCountByPageType,
                                "total_page" to totalPage
                            )

                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful(result))
                            }
                        }
                }
        }
    }

    private fun getCountOfPlayersByPageType(
        connection: Connection,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (playersCountByPageType: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user ${if (pageType == 2) "WHERE permission_id != ?" else ""}"

        val parameters = JsonArray()

        if (pageType == 2)
            parameters.add(-1)

        databaseManager.getSQLConnection(connection).queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PLAYERS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_124))
                }
        }
    }

    private fun getPlayers(
        connection: Connection,
        page: Int,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (players: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT id, username, register_date FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user ${if (pageType == 2) "WHERE permission_id != ? " else ""}ORDER BY `id` LIMIT 10 ${if (page == 1) "" else "OFFSET ${(page - 1) * 10}"}"

        val parameters = JsonArray()

        if (pageType == 2)
            parameters.add(-1)

        databaseManager.getSQLConnection(connection).queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val players = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    val handlers: List<(handler: () -> Unit) -> Any> =
                        queryResult.result().results.map { playerInDB ->
                            val localHandler: (handler: () -> Unit) -> Any = { handler ->
                                getTicketCountByUserID(
                                    connection,
                                    playerInDB.getInteger(0),
                                    resultHandler
                                ) { ticketCount ->
                                    players.add(
                                        mapOf(
                                            "id" to playerInDB.getInteger(0),
                                            "username" to playerInDB.getString(1),
                                            "ticket_count" to ticketCount,
                                            "register_date" to playerInDB.getString(2)
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
                                handler.invoke(players)
                            else
                                invoke()
                        }

                        currentIndex++

                        if (currentIndex <= handlers.lastIndex)
                            handlers[currentIndex].invoke(localHandler)
                    }

                    invoke()
                } else
                    handler.invoke(players)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PLAYERS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_125))
                }
        }
    }

    private fun getTicketCountByUserID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (ticketCount: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}ticket where user_id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PLAYERS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_126))
                }
        }
    }
}