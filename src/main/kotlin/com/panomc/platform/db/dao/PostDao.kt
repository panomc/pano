package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Post
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PostDao : Dao<Post> {
    fun removePostCategoriesByCategoryID(
        categoryID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (post: Map<String, Any>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun moveTrashByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun moveDraftByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun publishByID(
        id: Int,
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun insertAndPublish(
        post: Post,
        sqlConnection: SqlConnection,
        handler: (postID: Long?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun updateAndPublish(
        userID: Int,
        post: Post,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun count(sqlConnection: SqlConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun countByCategory(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByPageType(
        pageType: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(id: Int, sqlConnection: SqlConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun getByCategory(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (posts: List<Post>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SqlConnection,
        handler: (posts: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )
}