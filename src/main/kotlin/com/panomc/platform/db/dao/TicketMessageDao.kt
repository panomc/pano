package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.TicketMessage
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TicketMessageDao : Dao<TicketMessage> {
    fun getByTicketIDAndPage(
        ticketID: Int,
        page: Int,
        sqlConnection: SqlConnection,
        handler: (messages: List<TicketMessage>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getByTicketIDPageAndStartFromID(
        lastMessageID: Int,
        ticketID: Int,
        page: Int,
        sqlConnection: SqlConnection,
        handler: (messages: List<TicketMessage>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCountByTicketID(
        ticketID: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )
}