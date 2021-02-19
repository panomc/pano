package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import kotlin.math.ceil

class PostsPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/postPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                pageType,
                page
            )
        )
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

        databaseManager.getDatabase().postDao.countByPageType(
            pageType,
            sqlConnection,
            (this::countByPageTypeHandler)(handler, sqlConnection, pageType, page)
        )
    }

    private fun countByPageTypeHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        pageType: Int,
        page: Int
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        if (count == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_84))
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

        databaseManager.getDatabase().postDao.getByPageAndPageType(
            page,
            pageType,
            sqlConnection,
            (this::getByPageAndPageTypeHandler)(handler, sqlConnection, count, totalPage)
        )
    }

    private fun getByPageAndPageTypeHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        count: Int,
        totalPage: Int
    ) = handler@{ posts: List<Map<String, Any>>?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (posts == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_82))

                return@closeConnection
            }

            handler.invoke(
                Successful(
                    mutableMapOf<String, Any?>(
                        "posts" to posts,
                        "posts_count" to count,
                        "total_page" to totalPage
                    )
                )
            )
        }
    }
}