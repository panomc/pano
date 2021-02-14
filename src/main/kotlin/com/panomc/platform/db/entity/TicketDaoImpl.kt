package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketDao
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple
import java.util.*

class TicketDaoImpl(override val tableName: String = "ticket") : DaoImpl(), TicketDao {

    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
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
                )
                .execute {
                    handler.invoke(it)
                }
        }

    override fun count(sqlConnection: SqlConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}`"

        sqlConnection
            .preparedQuery(query)
            .execute { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun countOfOpenTickets(
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` WHERE status = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(1)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getLast5Tickets(
        sqlConnection: SqlConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, category_id, user_id, date, status FROM `${getTablePrefix() + tableName}` ORDER BY `date` DESC, `id` LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val tickets = mutableListOf<Map<String, Any>>()

                    if (rows.size() > 0) {
                        databaseManager.getDatabase().ticketCategoryDao.getAll(sqlConnection) { categories, _ ->
                            if (categories == null) {
                                handler.invoke(null, queryResult)
                                return@getAll
                            }

                            val handlers: List<(handler: () -> Unit) -> Any> =
                                rows.map { row ->
                                    val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                        databaseManager.getDatabase().userDao.getUsernameFromUserID(
                                            row.getInteger(
                                                3
                                            ), sqlConnection
                                        ) { username, asyncResult ->
                                            if (username == null) {
                                                handler.invoke(null, asyncResult)

                                                return@getUsernameFromUserID
                                            }

                                            var category: TicketCategory? = null

                                            categories.forEach { categoryInDB ->
                                                if (categoryInDB.id == row.getInteger(2).toInt())
                                                    category = categoryInDB
                                            }

                                            if (category == null)
                                                category = TicketCategory(-1, "-")

                                            tickets.add(
                                                mapOf(
                                                    "id" to row.getInteger(0),
                                                    "title" to String(
                                                        Base64.getDecoder().decode(row.getString(1).toByteArray())
                                                    ),
                                                    "category" to category!!,
                                                    "writer" to mapOf(
                                                        "username" to username
                                                    ),
                                                    "date" to row.getString(4),
                                                    "status" to row.getInteger(5)
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
        sqlConnection: SqlConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, category_id, user_id, date, status FROM `${getTablePrefix() + tableName}` ${if (pageType != 2) "WHERE status = ? " else ""}ORDER BY ${if (pageType == 2) "`status` ASC, " else ""}`date` DESC, `id` LIMIT 10 ${if (page == 1) "" else "OFFSET ${(page - 1) * 10}"}"

        val parameters = Tuple.tuple()

        if (pageType != 2)
            parameters.addInteger(pageType)

        sqlConnection
            .preparedQuery(query)
            .execute(parameters) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val tickets = mutableListOf<Map<String, Any>>()

                    if (rows.size() > 0) {
                        databaseManager.getDatabase().ticketCategoryDao.getAll(sqlConnection) { categories, _ ->
                            if (categories == null) {
                                handler.invoke(null, queryResult)
                                return@getAll
                            }

                            val handlers: List<(handler: () -> Unit) -> Any> =
                                rows.map { row ->
                                    val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                        databaseManager.getDatabase().userDao.getUsernameFromUserID(
                                            row.getInteger(
                                                3
                                            ), sqlConnection
                                        ) { username, asyncResult ->
                                            if (username == null) {
                                                handler.invoke(null, asyncResult)

                                                return@getUsernameFromUserID
                                            }

                                            var category: TicketCategory? = null

                                            categories.forEach { categoryInDB ->
                                                if (categoryInDB.id == row.getInteger(2).toInt())
                                                    category = categoryInDB
                                            }

                                            if (category == null)
                                                category = TicketCategory(-1, "-")

                                            tickets.add(
                                                mapOf(
                                                    "id" to row.getInteger(0),
                                                    "title" to String(
                                                        Base64.getDecoder()
                                                            .decode(row.getString(1).toByteArray())
                                                    ),
                                                    "category" to category!!,
                                                    "writer" to mapOf(
                                                        "username" to username
                                                    ),
                                                    "date" to row.getString(4),
                                                    "status" to row.getInteger(5)
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
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` ${if (pageType != 2) "WHERE status = ?" else ""}"

        val parameters = Tuple.tuple()

        if (pageType != 2)
            parameters.addInteger(pageType)

        sqlConnection
            .preparedQuery(query)
            .execute(parameters) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getByCategory(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (tickets: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title FROM `${getTablePrefix() + tableName}` WHERE category_id = ? ORDER BY `id` DESC LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val posts = mutableListOf<Map<String, Any>>()

                    rows.forEach { row ->
                        posts.add(
                            mapOf(
                                "id" to row.getInteger(0),
                                "title" to String(
                                    Base64.getDecoder().decode(row.getString(1).toByteArray())
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
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val parameters = Tuple.tuple()

        parameters.addInteger(3)

        var selectedTicketsSQLText = ""

        selectedTickets.forEach {
            if (selectedTicketsSQLText.isEmpty())
                selectedTicketsSQLText = "?"
            else
                selectedTicketsSQLText += ", ?"

            parameters.addValue(it)
        }

        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE id IN ($selectedTicketsSQLText)"

        sqlConnection
            .preparedQuery(query)
            .execute(parameters) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun countByCategory(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` WHERE category_id = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun delete(
        ticketList: JsonArray,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val parameters = Tuple.tuple()

        var selectedTicketsSQLText = ""

        ticketList.forEach {
            if (selectedTicketsSQLText.isEmpty())
                selectedTicketsSQLText = "?"
            else
                selectedTicketsSQLText += ", ?"

            parameters.addValue(it)
        }

        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE id IN ($selectedTicketsSQLText)"

        sqlConnection
            .preparedQuery(query)
            .execute(parameters) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun countByUserID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where user_id = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (ticket: Ticket?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `title`, `category_id`, `user_id`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE  `id` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val row = rows.toList()[0]

                    val ticket = Ticket(
                        id = row.getInteger(0),
                        title = String(
                            Base64.getDecoder().decode(row.getString(1).toByteArray())
                        ),
                        categoryID = row.getInteger(2),
                        userID = row.getInteger(3),
                        date = row.getString(4),
                        status = row.getInteger(5),
                    )

                    handler.invoke(ticket, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun isExistsByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) == 1, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }
}