package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PostCategoryDao
import com.panomc.platform.model.PostCategory
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class PostCategoryDaoImpl(override val tableName: String = "post_category") : DaoImpl(), PostCategoryDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` MEDIUMTEXT NOT NULL,
              `description` text NOT NULL,
              `url` varchar(255) NOT NULL,
              `color` varchar(6) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Post category table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    override fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

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
        val query =
            "DELETE FROM `${databaseManager.getTablePrefix() + tableName}` WHERE id = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCount(sqlConnection: SQLConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}`"

        sqlConnection.query(query) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCategories(
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, description, url, color FROM `${databaseManager.getTablePrefix() + tableName}` ORDER BY id DESC"

        sqlConnection.query(query) { queryResult ->
            if (queryResult.succeeded()) {
                val categories = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0)
                    queryResult.result().results.forEach { categoryInDB ->
                        categories.add(
                            mapOf(
                                "id" to categoryInDB.getInteger(0),
                                "title" to String(
                                    Base64.getDecoder().decode(categoryInDB.getString(1))
                                ),
                                "description" to String(Base64.getDecoder().decode(categoryInDB.getString(2))),
                                "url" to categoryInDB.getString(3),
                                "color" to categoryInDB.getString(4)
                            )
                        )
                    }

                handler.invoke(categories, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCategories(
        page: Int,
        sqlConnection: SQLConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, description, url, color FROM `${databaseManager.getTablePrefix() + tableName}` ORDER BY id DESC LIMIT 10 OFFSET ${(page - 1) * 10}"

        sqlConnection.queryWithParams(query, JsonArray()) { queryResult ->
            if (queryResult.succeeded()) {
                val categories = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    val handlers: List<(handler: () -> Unit) -> Any> =
                        queryResult.result().results.map { categoryInDB ->
                            val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                databaseManager.getDatabase().postDao.countByCategory(
                                    categoryInDB.getInteger(0),
                                    sqlConnection
                                ) { count, asyncResultOfCount ->
                                    if (count == null) {
                                        handler.invoke(null, asyncResultOfCount)

                                        return@countByCategory
                                    }

                                    databaseManager.getDatabase().postDao.getByCategory(
                                        categoryInDB.getInteger(0),
                                        sqlConnection
                                    ) { posts, asyncResultOfPosts ->
                                        if (posts == null) {
                                            handler.invoke(null, asyncResultOfPosts)

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
                                                "url" to categoryInDB.getString(3),
                                                "color" to categoryInDB.getString(4),
                                                "post_count" to count,
                                                "posts" to posts
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

    override fun isExistsByURL(
        url: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}` where `url` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(url)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isExistsByURLNotByID(
        url: String,
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}` where `url` = ? and id != ?"

        sqlConnection.queryWithParams(query, JsonArray().add(url).add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun add(
        postCategory: PostCategory,
        sqlConnection: SQLConnection,
        handler: (id: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`title`, `description`, `url`, `color`) VALUES (?, ?, ?, ?)"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(postCategory.title.toByteArray()))
                .add(Base64.getEncoder().encodeToString(postCategory.description.toByteArray()))
                .add(postCategory.url)
                .add(postCategory.color.replace("#", ""))
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().keys.getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun update(
        postCategory: PostCategory,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${databaseManager.getTablePrefix() + tableName}` SET title = ?, description = ?, url = ?, color = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(
                    Base64.getEncoder().encodeToString(postCategory.title.toByteArray())
                )
                .add(Base64.getEncoder().encodeToString(postCategory.description.toByteArray()))
                .add(postCategory.url)
                .add(postCategory.color.replace("#", ""))
                .add(postCategory.id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}