package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketCategoryDao
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class TicketCategoryDaoImpl(override val tableName: String = "ticket_category") : DaoImpl(), TicketCategoryDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` MEDIUMTEXT NOT NULL,
              `description` text,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Ticket category table.';
        """
            ) {
            handler.invoke(it)
        }
    }

    override fun getAll(
        sqlConnection: SQLConnection,
        handler: (categories: List<TicketCategory>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT id, title FROM `${getTablePrefix() + tableName}`"
        val categories = mutableListOf<TicketCategory>()

        sqlConnection.query(query) { categoryQueryResult ->
            if (categoryQueryResult.failed()) {
                handler.invoke(null, categoryQueryResult)

                return@query
            }

            categoryQueryResult.result().results.forEach { categoryInDB ->
                categories.add(
                    TicketCategory(
                        categoryInDB.getInteger(0),
                        String(
                            Base64.getDecoder()
                                .decode(categoryInDB.getString(1).toByteArray())
                        )
                    )
                )
            }

            handler.invoke(categories, categoryQueryResult)
        }
    }

    override fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun deleteByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun add(
        ticketCategory: TicketCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {

        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`title`, `description`) VALUES (?, ?)"

        sqlConnection.updateWithParams(
            query,
            JsonArray().add(Base64.getEncoder().encodeToString(ticketCategory.title.toByteArray()))
                .add(Base64.getEncoder().encodeToString(ticketCategory.description.toByteArray()))
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun update(
        ticketCategory: TicketCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET title = ?, description = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(ticketCategory.title.toByteArray()))
                .add(Base64.getEncoder().encodeToString(ticketCategory.description.toByteArray()))
                .add(ticketCategory.id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun count(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}`"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getByPage(
        page: Int,
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, description FROM `${getTablePrefix() + tableName}` ORDER BY id DESC LIMIT 10 OFFSET ${(page - 1) * 10}"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded()) {
                val categories = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    val handlers: List<(handler: () -> Unit) -> Any> =
                        queryResult.result().results.map { categoryInDB ->
                            val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                databaseManager.getDatabase().ticketDao.countByCategory(
                                    categoryInDB.getInteger(0),
                                    sqlConnection
                                ) { count, asyncResult ->
                                    if (count == null) {
                                        handler.invoke(null, asyncResult)

                                        return@countByCategory
                                    }

                                    databaseManager.getDatabase().ticketDao.getByCategory(
                                        categoryInDB.getInteger(0),
                                        sqlConnection
                                    ) { tickets, asyncResultOfGetByCategory ->
                                        if (tickets == null) {
                                            handler.invoke(null, asyncResultOfGetByCategory)

                                            return@getByCategory
                                        }

                                        categories.add(
                                            mapOf(
                                                "id" to categoryInDB.getInteger(0),
                                                "title" to String(
                                                    Base64.getDecoder().decode(categoryInDB.getString(1))
                                                ),
                                                "description" to String(
                                                    Base64.getDecoder().decode(categoryInDB.getString(2))
                                                ),
                                                "ticket_count" to count,
                                                "tickets" to tickets
                                            )
                                        )

                                        localHandler.invoke()
                                    }
                                }
                            }

                            localHandler
                        }

                    var currentIndex = -1

                    fun invoke() {
                        val localHandler: () -> Unit = {
                            if (currentIndex == handlers.lastIndex)
                                handler.invoke(categories, queryResult)
                            else
                                invoke()
                        }

                        currentIndex++

                        if (currentIndex <= handlers.lastIndex)
                            handlers[currentIndex].invoke(localHandler)
                    }

                    invoke()
                } else
                    handler.invoke(categories, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (ticketCategory: TicketCategory?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `title`, `description` FROM `${getTablePrefix() + tableName}` WHERE  `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded()) {
                val ticket = TicketCategory(
                    id = queryResult.result().results[0].getInteger(0),
                    title = String(
                        Base64.getDecoder().decode(queryResult.result().results[0].getString(1).toByteArray())
                    ),
                    description = String(
                        Base64.getDecoder().decode(queryResult.result().results[0].getString(2).toByteArray())
                    ),
                )

                handler.invoke(ticket, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

}