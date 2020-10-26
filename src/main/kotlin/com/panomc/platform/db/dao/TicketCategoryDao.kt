package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.Result
import com.panomc.platform.model.TicketCategory
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TicketCategoryDao : Dao<TicketCategory> {
    fun getAll(
        sqlConnection: SQLConnection,
        handler: (categories: List<TicketCategory>?, asyncResult: AsyncResult<*>) -> Unit
    )

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

    fun add(
        ticketCategory: TicketCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun update(
        ticketCategory: TicketCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun getByPage(
        page: Int,
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )
}