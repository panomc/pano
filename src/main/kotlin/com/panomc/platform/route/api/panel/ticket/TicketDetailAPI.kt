package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.db.model.TicketMessage
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext

class TicketDetailAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/ticket/detail")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")

        databaseManager.createConnection((this::createConnectionHandler)(handler, id))
    }

    private fun createConnectionHandler(handler: (result: Result) -> Unit, id: Int) =
        handler@{ sqlConnection: SQLConnection?, _: AsyncResult<SQLConnection> ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@handler
            }

            databaseManager.getDatabase().ticketDao.isExistsByID(
                id,
                sqlConnection,
                (this::isExistsByHandler)(handler, id, sqlConnection)
            )
        }

    private fun isExistsByHandler(handler: (result: Result) -> Unit, id: Int, sqlConnection: SQLConnection) =
        handler@{ exists: Boolean?, _: AsyncResult<*> ->
            if (exists == null) {
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_131))
                }

                return@handler
            }

            if (!exists)
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(Error(ErrorCode.NOT_EXISTS))
                }
            else
                databaseManager.getDatabase().ticketDao.getByID(
                    id,
                    sqlConnection,
                    (this::getByIDHandler)(handler, id, sqlConnection)
                )
        }

    private fun getByIDHandler(handler: (result: Result) -> Unit, id: Int, sqlConnection: SQLConnection) =
        handler@{ ticket: Ticket?, _: AsyncResult<*> ->
            if (ticket == null) {
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_132))
                }

                return@handler
            }

            databaseManager.getDatabase().userDao.getUsernameFromUserID(
                ticket.userID,
                sqlConnection,
                (this::getUsernameFromUserIDHandler)(handler, id, sqlConnection, ticket)
            )
        }

    private fun getUsernameFromUserIDHandler(
        handler: (result: Result) -> Unit,
        id: Int,
        sqlConnection: SQLConnection,
        ticket: Ticket
    ) = handler@{ username: String?, _: AsyncResult<*> ->
        if (username == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_133))
            }

            return@handler
        }

        databaseManager.getDatabase().ticketMessageDao.getByTicketIDAndPage(
            id,
            1,
            sqlConnection,
            (this::getByTicketIDAndPageHandler)(handler, sqlConnection, ticket, username)
        )
    }

    private fun getByTicketIDAndPageHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SQLConnection,
        ticket: Ticket,
        username: String
    ) = handler@{ messages: List<TicketMessage>?, _: AsyncResult<*> ->
        if (messages == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_135))
            }

            return@handler
        }

        val userIDList = mutableListOf<Int>()

        messages.forEach { message ->
            if (userIDList.indexOf(message.userID) == -1)
                userIDList.add(message.userID)
        }

        databaseManager.getDatabase().userDao.getUsernameByListOfID(
            userIDList,
            sqlConnection,
            (this::getUsernameByListOfIDHandler)(handler, sqlConnection, ticket, username, messages)
        )
    }

    private fun getUsernameByListOfIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SQLConnection,
        ticket: Ticket,
        username: String,
        messages: List<TicketMessage>
    ) = handler@{ usernameList: Map<Int, String>?, _: AsyncResult<*> ->
        if (usernameList == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_136))
            }

            return@handler
        }

        if (ticket.categoryID == -1)
            databaseManager.closeConnection(sqlConnection) {
                invokeHandler(handler, ticket, usernameList, null, username, messages)
            }
        else
            databaseManager.getDatabase().ticketCategoryDao.getByID(
                ticket.categoryID,
                sqlConnection
            ) { ticketCategory, _ ->
                databaseManager.closeConnection(sqlConnection) {
                    if (ticketCategory == null) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_134))

                        return@closeConnection
                    }

                    invokeHandler(handler, ticket, usernameList, ticketCategory, username, messages)
                }
            }
    }

    private fun invokeHandler(
        handler: (result: Result) -> Unit,
        ticket: Ticket,
        usernameList: Map<Int, String>,
        ticketCategory: TicketCategory?,
        username: String,
        ticketMessages: List<TicketMessage>
    ) {
        val messages = mutableMapOf<Int, Map<String, Any?>>()

        ticketMessages.forEach { ticketMessage ->
            messages[ticketMessage.id] = mapOf(
                "id" to ticketMessage.id,
                "userID" to ticketMessage.userID,
                "ticketID" to ticketMessage.ticketID,
                "username" to usernameList[ticketMessage.userID],
                "message" to ticketMessage.message,
                "date" to ticketMessage.date,
                "panel" to ticketMessage.panel
            )
        }

        handler.invoke(
            Successful(
                mapOf(
                    "ticket" to mapOf(
                        "username" to username,
                        "title" to ticket.title,
                        "category" to
                                if (ticketCategory == null)
                                    "-"
                                else
                                    mapOf(
                                        "title" to ticketCategory.title
                                    ),
                        "messages" to messages,
                        "status" to ticket.status,
                        "date" to ticket.date
                    )
                )
            )
        )
    }
}