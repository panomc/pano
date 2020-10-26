package com.panomc.platform.route.api.post.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class EditPostPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/editPost")

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
            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                databaseManager.getSQLConnection(connection)
            ) { result, _ ->
                when {
                    result == null -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_100))
                    }
                    result -> databaseManager.getDatabase().postDao.getByID(
                        id,
                        databaseManager.getSQLConnection(connection)
                    ) { post, _ ->
                        if (post == null)
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Error(ErrorCode.EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_99))
                            }
                        else
                            databaseManager.closeConnection(connection) {
                                handler.invoke(
                                    Successful(
                                        mapOf(
                                            "post" to post
                                        )
                                    )
                                )
                            }
                    }
                    else -> databaseManager.closeConnection(connection) {
                        handler.invoke(Error(ErrorCode.POST_NOT_FOUND))
                    }
                }

            }
        }
    }
}