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

class EditPostPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/editPost")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                isPostExistsByID(connection, id, handler) { exists ->
                    if (!exists)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.POST_NOT_FOUND))
                        }
                    else
                        getPost(connection, id, handler) { post ->
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
                }
        }
    }

    private fun isPostExistsByID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (exists: Boolean) -> Unit
    ) {
        val query =
            "SELECT COUNT(`id`) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post WHERE `id` = ?"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(id)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0) == 1)
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_100))
                    }
            }
    }

    private fun getPost(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (post: Map<String, Any>) -> Unit
    ) {
        val query =
            "SELECT `id`, `title`, `category_id`, `writer_user_id`, `post`, `date`, `status`, `image`, `views` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}post WHERE  `id` = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded()) {

                val post = mapOf(
                    "id" to queryResult.result().results[0].getInteger(0),
                    "title" to String(Base64.getDecoder().decode(queryResult.result().results[0].getString(1))),
                    "category" to queryResult.result().results[0].getInteger(2),
                    "writer_user_id" to queryResult.result().results[0].getInteger(3),
                    "text" to String(Base64.getDecoder().decode(queryResult.result().results[0].getString(4))),
                    "date" to queryResult.result().results[0].getString(5),
                    "status" to queryResult.result().results[0].getInteger(6),
                    "image" to queryResult.result().results[0].getString(7),
                    "views" to queryResult.result().results[0].getString(8)
                )

                handler.invoke(post)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.EDIT_POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_99))
                }
        }
    }
}