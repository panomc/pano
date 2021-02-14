package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketMessageDao
import com.panomc.platform.db.model.TicketMessage
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import java.util.*

class TicketMessageDaoImpl(override val tableName: String = "ticket_message") : DaoImpl(), TicketMessageDao {
    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
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
                )
                .execute {
                    handler.invoke(it)
                }
        }

    override fun getByTicketIDAndPage(
        ticketID: Int,
        page: Int,
        sqlConnection: SqlConnection,
        handler: (messages: List<TicketMessage>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, user_id, ticket_id, message, `date`, `panel` FROM `${getTablePrefix() + tableName}` WHERE ticket_id = ? ORDER BY id DESC LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(ticketID)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val messages = mutableListOf<TicketMessage>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            messages.add(
                                TicketMessage(
                                    id = row.getInteger(0),
                                    userID = row.getInteger(1),
                                    ticketID = row.getInteger(2),
                                    message = String(
                                        Base64.getDecoder().decode(row.getString(3).toByteArray())
                                    ),
                                    date = row.getString(4),
                                    panel = row.getInteger(5)
                                )
                            )
                        }

                    handler.invoke(messages, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getByTicketIDPageAndStartFromID(
        lastMessageID: Int,
        ticketID: Int,
        page: Int,
        sqlConnection: SqlConnection,
        handler: (messages: List<TicketMessage>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, user_id, ticket_id, message, `date`, `panel` FROM `${getTablePrefix() + tableName}` WHERE ticket_id = ? and id < ? ORDER BY id DESC LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(ticketID, lastMessageID)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val messages = mutableListOf<TicketMessage>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            messages.add(
                                TicketMessage(
                                    id = row.getInteger(0),
                                    userID = row.getInteger(1),
                                    ticketID = row.getInteger(2),
                                    message = String(
                                        Base64.getDecoder().decode(row.getString(3).toByteArray())
                                    ),
                                    date = row.getString(4),
                                    panel = row.getInteger(5)
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
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where ticket_id = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(ticketID)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }
}