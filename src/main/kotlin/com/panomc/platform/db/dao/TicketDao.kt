package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TicketDao : Dao<Ticket> {
    fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun countOfOpenTickets(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit)

    fun getLast5Tickets(
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getAllByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCountByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun closeTickets(
        selectedTickets: JsonArray,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(
        ticketList: JsonArray,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun countByUserID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (ticket: Ticket?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )
}