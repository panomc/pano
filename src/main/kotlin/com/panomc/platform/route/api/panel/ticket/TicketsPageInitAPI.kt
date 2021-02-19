package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import kotlin.math.ceil

class TicketsPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/ticketPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection((this::createConnectionHandler)(handler, pageType, page))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        pageType: Int,
        page: Int
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().ticketDao.getCountByPageType(
            pageType,
            sqlConnection,
            (this::getCountByPageTypeHandler)(handler, sqlConnection, pageType, page)
        )
    }

    private fun getCountByPageTypeHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        pageType: Int,
        page: Int
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_78))
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

        databaseManager.getDatabase().ticketDao.getAllByPageAndPageType(
            page,
            pageType,
            sqlConnection,
            (this::getAllByPageAndPageTypeHandler)(handler, sqlConnection, count, totalPage)
        )
    }

    private fun getAllByPageAndPageTypeHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        count: Int,
        totalPage: Int
    ) = handler@{ tickets: List<Map<String, Any>>?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (tickets == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_76))

                return@closeConnection
            }

            val result = mutableMapOf<String, Any?>(
                "tickets" to tickets,
                "tickets_count" to count,
                "total_page" to totalPage
            )

            handler.invoke(Successful(result))
        }
    }
}