package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import kotlin.math.ceil

class PostCategoryPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/posts/categoryPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val page = data.getInteger("page")

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                page
            )
        )
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        page: Int
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().postCategoryDao.getCount(
            sqlConnection,
            (this::getCountHandler)(handler, sqlConnection, page)
        )
    }

    private fun getCountHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        page: Int
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_86))
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

        databaseManager.getDatabase().postCategoryDao.getCategories(
            page,
            sqlConnection
        ) { categories, _ ->
            databaseManager.closeConnection(sqlConnection) {
                if (categories == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_85))

                    return@closeConnection
                }

                val result = mutableMapOf<String, Any?>(
                    "categories" to categories,
                    "category_count" to count,
                    "total_page" to totalPage,
                    "host" to "http://"
                )

                handler.invoke(Successful(result))
            }
        }
    }
}