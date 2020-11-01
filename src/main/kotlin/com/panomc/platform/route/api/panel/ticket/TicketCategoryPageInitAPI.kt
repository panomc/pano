package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import kotlin.math.ceil

class TicketCategoryPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/tickets/categoryPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val page = data.getInteger("page")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().ticketCategoryDao.count(sqlConnection) { count, _ ->
                if (count == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_80))
                    }
                else {
                    var totalPage = ceil(count.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1)
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }
                    else
                        databaseManager.getDatabase().ticketCategoryDao.getByPage(
                            page,
                            sqlConnection
                        ) { categories, _ ->
                            if (categories == null)
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_79))
                                }
                            else
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(
                                        Successful(
                                            mutableMapOf<String, Any?>(
                                                "categories" to categories,
                                                "category_count" to count,
                                                "total_page" to totalPage,
                                                "host" to "http://"
                                            )
                                        )
                                    )
                                }
                        }
                }
            }
        }
    }
}