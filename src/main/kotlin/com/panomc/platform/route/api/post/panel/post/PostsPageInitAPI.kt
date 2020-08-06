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
import kotlin.math.ceil

class PostsPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/postPage")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getCountOfPostsByPageType(connection, pageType, handler) { countOfPostsByPageType ->
                    var totalPage = ceil(countOfPostsByPageType.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1)
                        databaseManager.closeConnection(connection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }
                    else
                        getPosts(connection, page, pageType, handler) { posts ->
                            val result = mutableMapOf<String, Any?>(
                                "posts" to posts,
                                "posts_count" to countOfPostsByPageType,
                                "total_page" to totalPage
                            )

                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful(result))
                            }
                        }
                }
        }
    }

    private fun getCountOfPostsByPageType(
        connection: Connection,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (postsCountByPageType: Int) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post WHERE status = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(pageType)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_84))
                }
        }
    }

    private fun getPosts(
        connection: Connection,
        page: Int,
        pageType: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (posts: List<Map<String, Any>>) -> Unit
    ) {
        var query =
            "SELECT id, title, category_id, writer_user_id, date, views FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post WHERE status = ? ORDER BY ${if (pageType == 1) "date DESC" else "move_date DESC"} LIMIT 10 OFFSET ${(page - 1) * 10}"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(pageType)) { queryResult ->
            if (queryResult.succeeded()) {
                val posts = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    query =
                        "SELECT id, title, url, color FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}post_category"

                    databaseManager.getSQLConnection(connection).query(query) { categoryQueryResult ->
                        if (categoryQueryResult.succeeded()) {
                            val handlers: List<(handler: () -> Unit) -> Any> =
                                queryResult.result().results.map { postInDB ->
                                    val localHandler: (handler: () -> Unit) -> Any = { handler ->
                                        getUserNameFromID(
                                            connection,
                                            postInDB.getInteger(3),
                                            resultHandler
                                        ) { username ->
                                            var category: Any = "null"

                                            categoryQueryResult.result().results.forEach { categoryInDB ->
                                                if (categoryInDB.getInteger(0) == postInDB.getInteger(2).toInt())
                                                    category = mapOf(
                                                        "id" to categoryInDB.getInteger(0),
                                                        "title" to String(
                                                            Base64.getDecoder()
                                                                .decode(categoryInDB.getString(1).toByteArray())
                                                        ),
                                                        "url" to categoryInDB.getString(2),
                                                        "color" to categoryInDB.getString(3)
                                                    )
                                            }

                                            if (category == "null")
                                                category = mapOf(
                                                    "title" to "-"
                                                )

                                            posts.add(
                                                mapOf(
                                                    "id" to postInDB.getInteger(0),
                                                    "title" to String(
                                                        Base64.getDecoder().decode(postInDB.getString(1).toByteArray())
                                                    ),
                                                    "category" to category,
                                                    "writer" to mapOf(
                                                        "username" to username
                                                    ),
                                                    "date" to postInDB.getString(4),
                                                    "views" to postInDB.getString(5)
                                                )
                                            )

                                            handler.invoke()
                                        }
                                    }

                                    localHandler
                                }

                            var currentIndex = -1

                            fun invoke() {
                                val localHandler: () -> Unit = {
                                    if (currentIndex == handlers.lastIndex)
                                        handler.invoke(posts)
                                    else
                                        invoke()
                                }

                                currentIndex++

                                if (currentIndex <= handlers.lastIndex)
                                    handlers[currentIndex].invoke(localHandler)
                            }

                            invoke()
                        } else
                            databaseManager.closeConnection(connection) {
                                resultHandler.invoke(Error(ErrorCode.POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_83))
                            }
                    }
                } else
                    handler.invoke(posts)
            } else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_82))
                }
        }
    }

    private fun getUserNameFromID(
        connection: Connection,
        id: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (username: String) -> Unit
    ) {
        val query =
            "SELECT username FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.POST_PAGE_INIT_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_81))
                }
        }
    }
}