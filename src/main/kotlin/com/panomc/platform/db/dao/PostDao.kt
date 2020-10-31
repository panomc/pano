package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Post
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PostDao : Dao<Post> {
    fun removePostCategoriesByCategoryID(
        categoryID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (post: Map<String, Any>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun moveTrashByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun moveDraftByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun publishByID(
        id: Int,
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun insertAndPublish(
        post: Post,
        sqlConnection: SQLConnection,
        handler: (postID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun updateAndPublish(
        userID: Int,
        post: Post,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun countByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(id: Int, sqlConnection: SQLConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun getByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (posts: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (posts: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )
}