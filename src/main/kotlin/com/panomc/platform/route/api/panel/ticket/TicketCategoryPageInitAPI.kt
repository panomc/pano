package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import kotlin.math.ceil

class TicketCategoryPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/tickets/categoryPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val page = data.getInteger("page")

        databaseManager.createConnection((this::createConnectionHandler)(handler, page))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        page: Int,
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().ticketCategoryDao.count(
            sqlConnection,
            (this::countHandler)(handler, sqlConnection, page)
        )
    }

    private fun countHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        page: Int
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_80))
            }

            return@handler
        }

        var totalPage = ceil(count.toDouble() / 10).toInt()

        if (totalPage < 1)
            totalPage = 1

        if (page > totalPage || page < 1) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
            }

            return@handler
        }

        databaseManager.getDatabase().ticketCategoryDao.getByPage(
            page,
            sqlConnection,
            (this::getByPageHandler)(handler, sqlConnection, count, totalPage)
        )
    }

    private fun getByPageHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        count: Int,
        totalPage: Int
    ) = handler@{ categories: List<Map<String, Any>>?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (categories == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_79))

                return@closeConnection
            }

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