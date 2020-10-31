package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().userDao.countByPageType(
                pageType,
                databaseManager.getSQLConnection(connection)
            ) { count, _ ->
                if (count == null) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.PLAYERS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_124))
                    }

                    return@countByPageType
                }


                var totalPage = ceil(count.toDouble() / 10).toInt()

                if (totalPage < 1)
                    totalPage = 1

                if (page > totalPage || page < 1) {
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                    }

                    return@countByPageType
                }

                databaseManager.getDatabase().userDao.getAllByPageAndPageType(
                    page,
                    pageType,
                    databaseManager.getSQLConnection(connection)
                ) { userList, _ ->
                    if (userList == null) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PLAYERS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_125))
                        }

                        return@getAllByPageAndPageType
                    }


                    val result = mutableMapOf<String, Any?>(
                        "players" to userList,
                        "players_count" to count,
                        "total_page" to totalPage
                    )

                    databaseManager.closeConnection(connection) {
                        handler.invoke(Successful(result))
                    }
                }
            }
        }
    }
}