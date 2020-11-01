package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketDao
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class TicketDaoImpl(override val tableName: String = "ticket") : DaoImpl(), TicketDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` MEDIUMTEXT NOT NULL,
              `category_id` int(11) NOT NULL,
              `user_id` int(11) NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              `status` int(1) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Tickets table.';
        """
            ) {
            handler.invoke(it)
        }
    }

    override fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}`"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun countOfOpenTickets(
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` WHERE status = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(1)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getLast5Tickets(
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, category_id, user_id, date, status FROM `${getTablePrefix() + tableName}` ORDER BY `date` DESC, `id` LIMIT 5"

        val parameters = JsonArray()

        sqlConnection.queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val tickets = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    databaseManager.getDatabase().ticketCategoryDao.getAll(sqlConnection) { categories, _ ->
                        if (categories == null) {
                            handler.invoke(null, queryResult)
                            return@getAll
                        }

                        val handlers: List<(handler: () -> Unit) -> Any> =
                            queryResult.result().results.map { ticketInDB ->
                                val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                    databaseManager.getDatabase().userDao.getUsernameFromUserID(
                                        ticketInDB.getInteger(
                                            3
                                        ), sqlConnection
                                    ) { username, asyncResult ->
                                        if (username == null) {
                                            handler.invoke(null, asyncResult)

                                            return@getUsernameFromUserID
                                        }

                                        var category: TicketCategory? = null

                                        categories.forEach { categoryInDB ->
                                            if (categoryInDB.id == ticketInDB.getInteger(2).toInt())
                                                category = categoryInDB
                                        }

                                        if (category == null)
                                            category = TicketCategory(-1, "-")

                                        tickets.add(
                                            mapOf(
                                                "id" to ticketInDB.getInteger(0),
                                                "title" to String(
                                                    Base64.getDecoder().decode(ticketInDB.getString(1).toByteArray())
                                                ),
                                                "category" to category!!,
                                                "writer" to mapOf(
                                                    "username" to username
                                                ),
                                                "date" to ticketInDB.getString(4),
                                                "status" to ticketInDB.getInteger(5)
                                            )
                                        )

                                        localHandler.invoke()
                                    }
                                }

                                localHandler
                            }

                        var currentIndex = -1

                        fun invoke() {
                            val localHandler: () -> Unit = {
                                if (currentIndex == handlers.lastIndex)
                                    handler.invoke(tickets, queryResult)
                                else
                                    invoke()
                            }

                            currentIndex++

                            if (currentIndex <= handlers.lastIndex)
                                handlers[currentIndex].invoke(localHandler)
                        }

                        invoke()
                    }
                } else
                    handler.invoke(tickets, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getAllByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "SELECT id, title, category_id, user_id, date, status FROM `${getTablePrefix() + tableName}` ${if (pageType != 2) "WHERE status = ? " else ""}ORDER BY ${if (pageType == 2) "`status` ASC, " else ""}`date` DESC, `id` LIMIT 10 ${if (page == 1) "" else "OFFSET ${(page - 1) * 10}"}"

        val parameters = JsonArray()

        if (pageType != 2)
            parameters.add(pageType)

        sqlConnection.queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded()) {
                val tickets = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    databaseManager.getDatabase().ticketCategoryDao.getAll(sqlConnection) { categories, _ ->
                        if (categories == null) {
                            handler.invoke(null, queryResult)
                            return@getAll
                        }

                        val handlers: List<(handler: () -> Unit) -> Any> =
                            queryResult.result().results.map { ticketInDB ->
                                val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                    databaseManager.getDatabase().userDao.getUsernameFromUserID(
                                        ticketInDB.getInteger(
                                            3
                                        ), sqlConnection
                                    ) { username, asyncResult ->
                                        if (username == null) {
                                            handler.invoke(null, asyncResult)

                                            return@getUsernameFromUserID
                                        }

                                        var category: TicketCategory? = null

                                        categories.forEach { categoryInDB ->
                                            if (categoryInDB.id == ticketInDB.getInteger(2).toInt())
                                                category = categoryInDB
                                        }

                                        if (category == null)
                                            category = TicketCategory(-1, "-")

                                        tickets.add(
                                            mapOf(
                                                "id" to ticketInDB.getInteger(0),
                                                "title" to String(
                                                    Base64.getDecoder()
                                                        .decode(ticketInDB.getString(1).toByteArray())
                                                ),
                                                "category" to category!!,
                                                "writer" to mapOf(
                                                    "username" to username
                                                ),
                                                "date" to ticketInDB.getString(4),
                                                "status" to ticketInDB.getInteger(5)
                                            )
                                        )

                                        localHandler.invoke()
                                    }
                                }

                                localHandler
                            }

                        var currentIndex = -1

                        fun invoke() {
                            val localHandler: () -> Unit = {
                                if (currentIndex == handlers.lastIndex)
                                    handler.invoke(tickets, queryResult)
                                else
                                    invoke()
                            }

                            currentIndex++

                            if (currentIndex <= handlers.lastIndex)
                                handlers[currentIndex].invoke(localHandler)
                        }

                        invoke()
                    }
                } else
                    handler.invoke(tickets, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCountByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` ${if (pageType != 2) "WHERE status = ?" else ""}"

        val parameters = JsonArray()

        if (pageType != 2)
            parameters.add(pageType)

        sqlConnection.queryWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title FROM `${getTablePrefix() + tableName}` WHERE category_id = ? ORDER BY `id` DESC LIMIT 5"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded()) {
                val posts = mutableListOf<Map<String, Any>>()

                queryResult.result().results.forEach { postInDB ->
                    posts.add(
                        mapOf(
                            "id" to postInDB.getInteger(0),
                            "title" to String(
                                Base64.getDecoder().decode(postInDB.getString(1).toByteArray())
                            )
                        )
                    )
                }

                handler.invoke(posts, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun closeTickets(
        selectedTickets: JsonArray,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val parameters = JsonArray()

        parameters.add(3)

        var selectedTicketsSQLText = ""

        selectedTickets.forEach {
            if (selectedTicketsSQLText.isEmpty())
                selectedTicketsSQLText = "?"
            else
                selectedTicketsSQLText += ", ?"

            parameters.add(it)
        }

        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE id IN ($selectedTicketsSQLText)"

        sqlConnection.updateWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun countByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` WHERE category_id = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded()) {
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun delete(
        ticketList: JsonArray,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val parameters = JsonArray()

        var selectedTicketsSQLText = ""

        ticketList.forEach {
            if (selectedTicketsSQLText.isEmpty())
                selectedTicketsSQLText = "?"
            else
                selectedTicketsSQLText += ", ?"

            parameters.add(it)
        }

        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE id IN ($selectedTicketsSQLText)"

        sqlConnection.updateWithParams(query, parameters) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun countByUserID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where user_id = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}