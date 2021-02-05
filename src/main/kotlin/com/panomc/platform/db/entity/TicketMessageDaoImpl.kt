package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketMessageDao
import com.panomc.platform.db.model.TicketMessage
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class TicketMessageDaoImpl(override val tableName: String = "ticket_message") : DaoImpl(), TicketMessageDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,
              `ticket_id` int NOT NULL,
              `message` text NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Ticket message table.';
        """
            ) {
                handler.invoke(it)
            }
        }

    override fun getByTicketIDAndPage(
        ticketID: Int,
        page: Int,
        sqlConnection: SQLConnection,
        handler: (messages: List<TicketMessage>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, user_id, ticket_id, message, `date` FROM `${getTablePrefix() + tableName}` ORDER BY id DESC LIMIT 10 OFFSET ${(page - 1) * 10}"
        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded()) {
                val messages = mutableListOf<TicketMessage>()

                if (queryResult.result().results.size > 0) {
                    messages.add(
                        TicketMessage(
                            id = queryResult.result().results[0].getInteger(0),
                            userID = queryResult.result().results[0].getInteger(1),
                            ticketID = queryResult.result().results[0].getInteger(2),
                            message = String(
                                Base64.getDecoder().decode(queryResult.result().results[0].getString(3).toByteArray())
                            ),
                            date = queryResult.result().results[0].getString(4)
                        )
                    )
                }
                handler.invoke(messages, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }
}