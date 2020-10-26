package com.panomc.platform.route.api.post.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().ticketDao.getCountByPageType(
                pageType,
                databaseManager.getSQLConnection(connection)
            ) { count, _ ->
                if (count == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_78))
                    }
                else {
                    var totalPage = ceil(count.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1) {
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }

                        return@getCountByPageType
                    }

                    databaseManager.getDatabase().ticketDao.getAllByPageAndPageType(
                        page,
                        pageType,
                        databaseManager.getSQLConnection(connection)
                    ) { tickets, _ ->
                        if (tickets == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.TICKETS_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_76))
                            }
                        else {

                            val result = mutableMapOf<String, Any?>(
                                "tickets" to tickets,
                                "tickets_count" to count,
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
    }
}