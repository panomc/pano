package com.panomc.platform.route.api.panel.post.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class CategoriesAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/post/category/categories")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postCategoryDao.getCount(sqlConnection) { countOfCategories, _ ->
                if (countOfCategories == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_88))
                    }
                else
                    databaseManager.getDatabase().postCategoryDao.getCategories(
                        sqlConnection
                    ) { categories, _ ->
                        if (categories == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_87))
                            }
                        else {
                            val result = mutableMapOf<String, Any?>(
                                "categories" to categories,
                                "category_count" to countOfCategories
                            )

                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Successful(result))
                            }
                        }

                    }
            }
        }
    }
}