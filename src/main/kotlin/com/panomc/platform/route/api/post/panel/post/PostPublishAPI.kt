package com.panomc.platform.route.api.post.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import java.util.*
import javax.inject.Inject

class PostPublishAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/post/publish")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getValue("id").toString().toInt()
        val title = data.getString("title")
        val categoryID = data.getValue("category").toString().toInt()
        val post = data.getString("text")
        val imageCode = data.getString("imageCode") ?: ""

        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getUserIDFromToken(connection, token, handler) { userID ->
                    if (id == -1)
                        insertAndPublishInDB(
                            connection,
                            title,
                            categoryID,
                            userID,
                            post,
                            imageCode,
                            handler
                        ) { insertID ->
                            databaseManager.closeConnection(connection) {
                                handler.invoke(
                                    Successful(
                                        mapOf(
                                            "id" to insertID
                                        )
                                    )
                                )
                            }
                        }
                    else
                        updateAndPublishInDB(
                            connection,
                            id,
                            title,
                            categoryID,
                            userID,
                            post,
                            imageCode,
                            handler
                        ) {
                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful())
                            }
                        }
                }
        }
    }

    private fun getUserIDFromToken(
        connection: Connection,
        token: String,
        resultHandler: (result: Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT user_id FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}token where token = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(
                        Error(
                            ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_116
                        )
                    )
                }
        }
    }

    private fun insertAndPublishInDB(
        connection: Connection,
        title: String,
        categoryId: Int,
        writerUserID: Int,
        post: String,
        imageCode: String,
        resultHandler: (result: Result) -> Unit,
        handler: (id: Int) -> Unit
    ) {
        val query =
            "INSERT INTO ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post (`title`, `category_id`, `writer_user_id`, `post`, `date`, `move_date`, `status`, `image`, `views`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(title.toByteArray()))
                .add(categoryId)
                .add(writerUserID)
                .add(post)
                .add(System.currentTimeMillis())
                .add(System.currentTimeMillis())
                .add(1)
                .add(imageCode)
                .add(0)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().keys.getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_114))
                }
        }
    }

    private fun updateAndPublishInDB(
        connection: Connection,
        id: Int,
        title: String,
        categoryId: Int,
        writerUserID: Int,
        post: String,
        imageCode: String,
        resultHandler: (result: Result) -> Unit,
        handler: () -> Unit
    ) {
        val query =
            "UPDATE ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post SET title = ?, category_id = ?, writer_user_id = ?, post = ?, date = ?, move_date = ?, status = ?, image = ? WHERE id = ?"

        databaseManager.getSQLConnection(connection).updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(title.toByteArray()))
                .add(categoryId)
                .add(writerUserID)
                .add(post)
                .add(System.currentTimeMillis())
                .add(System.currentTimeMillis())
                .add(1)
                .add(imageCode)
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke()
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PUBLISH_POST_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_115))
                }
        }
    }
}