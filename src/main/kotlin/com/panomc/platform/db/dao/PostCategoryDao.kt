package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.PostCategory
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PostCategoryDao : Dao<PostCategory> {
    fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun deleteByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCount(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun getCategories(
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCategories(
        page: Int,
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByURL(
        url: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByURLNotByID(
        url: String,
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun add(
        postCategory: PostCategory,
        sqlConnection: SQLConnection,
        handler: (id: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun update(
        postCategory: PostCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}