package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Post
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PostPublishAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/publish")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getValue("id").toString().toInt()
        val title = data.getString("title")
        val categoryID = data.getValue("category").toString().toInt()
        val text = data.getString("text")
        val imageCode = data.getString("imageCode") ?: ""

        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, _ ->
                if (userID == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(
                            Error(
                                ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_116
                            )
                        )
                    }
                else {
                    val post = Post(id, title, categoryID, userID, text, imageCode)

                    if (id == -1)
                        databaseManager.getDatabase().postDao.insertAndPublish(
                            post,
                            sqlConnection
                        ) { postID, _ ->
                            if (postID == null)
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_114))
                                }
                            else
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(
                                        Successful(
                                            mapOf(
                                                "id" to postID
                                            )
                                        )
                                    )
                                }
                        }
                    else
                        databaseManager.getDatabase().postDao.updateAndPublish(
                            userID,
                            post,
                            sqlConnection
                        ) { result, _ ->
                            if (result == null)
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_115))
                                }
                            else
                                handler.invoke(Successful())

                        }
                }
            }
        }
    }
}