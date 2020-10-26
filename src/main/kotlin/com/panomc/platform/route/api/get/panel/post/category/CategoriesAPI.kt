package com.panomc.platform.route.api.get.panel.post.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CategoriesAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/post/category/categories")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postCategoryDao.getCount(databaseManager.getSQLConnection(connection)) { countOfCategories, _ ->
                if (countOfCategories == null)
                    databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_88))
                    }
                else
                    databaseManager.getDatabase().postCategoryDao.getCategories(
                        databaseManager.getSQLConnection(
                            connection
                        )
                    ) { categories, _ ->
                        if (categories == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.POST_CATEGORY_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_87))
                            }
                        else {
                            val result = mutableMapOf<String, Any?>(
                                "categories" to categories,
                                "category_count" to countOfCategories
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