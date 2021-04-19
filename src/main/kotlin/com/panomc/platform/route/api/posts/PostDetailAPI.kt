package com.panomc.platform.route.api.posts

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.Post
import com.panomc.platform.db.model.PostCategory
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PostDetailAPI : Api() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/posts/detail")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection(
            (this::createConnectionHandler)(
                handler,
                id
            )
        )
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        id: Int
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().postDao.isExistsByID(
            id,
            sqlConnection,
            (this::isExistsByIDHandler)(handler, sqlConnection, id)
        )
    }

    private fun isExistsByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        id: Int
    ) = handler@{ exists: Boolean?, _: AsyncResult<*> ->
        if (exists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_229))
            }

            return@handler
        }

        if (!exists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.POST_NOT_FOUND))
            }

            return@handler
        }

        databaseManager.getDatabase().postDao.increaseViewByOne(
            id,
            sqlConnection,
            (this::increaseViewByOneHandler)(handler, sqlConnection, id)
        )
    }

    private fun increaseViewByOneHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        id: Int
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        if (result == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_237))
            }

            return@handler
        }

        databaseManager.getDatabase().postDao.getByID(
            id,
            sqlConnection,
            (this::getByIDHandler)(handler, sqlConnection)
        )
    }

    private fun getByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
    ) = handler@{ post: Post?, _: AsyncResult<*> ->
        if (post == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_228))
            }

            return@handler
        }

        if (post.categoryId == 0) {
            databaseManager.getDatabase().userDao.getUsernameFromUserID(
                post.writerUserID,
                sqlConnection,
                (this::getUsernameFromUserIDHandler)(handler, sqlConnection, post, null)
            )

            return@handler
        }

        databaseManager.getDatabase().postCategoryDao.getByID(
            post.categoryId,
            sqlConnection,
            (this::getPostCategoryByIDHandler)(handler, sqlConnection, post)
        )
    }

    private fun getPostCategoryByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        post: Post
    ) = handler@{ category: PostCategory?, _: AsyncResult<*> ->
        if (category == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_227))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getUsernameFromUserID(
            post.writerUserID,
            sqlConnection,
            (this::getUsernameFromUserIDHandler)(handler, sqlConnection, post, category)
        )
    }

    private fun getUsernameFromUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        post: Post,
        category: PostCategory?
    ) = handler@{ username: String?, _: AsyncResult<*> ->
        if (username == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_230))
            }

            return@handler
        }

        sendResultHandler(handler, sqlConnection, post, category, username)
    }

    private fun sendResultHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        post: Post,
        category: PostCategory?,
        username: String
    ) {
        databaseManager.closeConnection(sqlConnection) {
            handler.invoke(
                Successful(
                    mapOf(
                        "post" to mapOf(
                            "id" to post.id,
                            "title" to post.title,
                            "category" to
                                    if (category == null)
                                        "-"
                                    else
                                        mapOf<String, Any?>(
                                            "title" to category.title,
                                            "url" to category.url
                                        ),
                            "writer" to mapOf(
                                "username" to username
                            ),
                            "text" to post.post,
                            "date" to post.date,
                            "status" to post.status,
                            "image" to post.image,
                            "views" to post.views
                        )
                    )
                )
            )
        }
    }
}