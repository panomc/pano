package com.panomc.platform.route.api.post.panel.post.category

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostCategoryDeleteAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/category/delete")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection { connection, _ ->
            if (connection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().postCategoryDao.isExistsByID(
                id,
                databaseManager.getSQLConnection(connection)
            ) { exists, _ ->
                when {
                    exists == null -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_98))
                    }
                    exists -> databaseManager.getDatabase().postDao.removePostCategoriesByCategoryID(
                        id,
                        databaseManager.getSQLConnection(connection)
                    ) { removeResult, _ ->
                        if (removeResult == null) databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_121))
                        }
                        else databaseManager.getDatabase().postCategoryDao.deleteByID(
                            id,
                            databaseManager.getSQLConnection(connection)
                        ) { deleteResult, _ ->
                            if (deleteResult == null)
                                databaseManager.closeConnection(connection) {
                                    handler.invoke(Error(ErrorCode.POST_CATEGORY_DELETE_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_97))
                                }
                            else
                                handler.invoke(Successful())
                        }
                    }
                    else -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }
                }
            }
        }
    }
}