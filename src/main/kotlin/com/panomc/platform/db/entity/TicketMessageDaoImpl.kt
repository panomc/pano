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
              `panel` int NOT NULL COMMENT='Is it a message wrote on panel by an authorized.',
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
            "SELECT id, user_id, ticket_id, message, `date`, `panel` FROM `${getTablePrefix() + tableName}` ORDER BY id DESC LIMIT 5 OFFSET ${(page - 1) * 5}"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded()) {
                val messages = mutableListOf<TicketMessage>()

                if (queryResult.result().results.size > 0)
                    queryResult.result().results.forEach { ticketMessage ->
                        messages.add(
                            TicketMessage(
                                id = ticketMessage.getInteger(0),
                                userID = ticketMessage.getInteger(1),
                                ticketID = ticketMessage.getInteger(2),
                                message = String(
                                    Base64.getDecoder().decode(ticketMessage.getString(3).toByteArray())
                                ),
                                date = ticketMessage.getString(4),
                                panel = ticketMessage.getInteger(5)
                            )
                        )
                    }

                handler.invoke(messages, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCountByTicketID(
        ticketID: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where ticket_id = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(ticketID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}