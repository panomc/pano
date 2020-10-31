package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PostDao
import com.panomc.platform.db.model.Post
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class PostDaoImpl(override val tableName: String = "post") : DaoImpl(), PostDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `title` MEDIUMTEXT NOT NULL,
              `category_id` int(11) NOT NULL,
              `writer_user_id` int(11) NOT NULL,
              `post` longblob NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              `move_date` MEDIUMTEXT NOT NULL,
              `status` int(1) NOT NULL,
              `image` longblob NOT NULL,
              `views` MEDIUMTEXT NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Posts table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    override fun removePostCategoriesByCategoryID(
        categoryID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "UPDATE `${getTablePrefix() + tableName}` SET category_id = ? WHERE category_id = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(-1).add(categoryID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
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

    override fun getByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (post: Map<String, Any>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `title`, `category_id`, `writer_user_id`, `post`, `date`, `status`, `image`, `views` FROM `${getTablePrefix() + tableName}` WHERE  `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded()) {

                val post = mapOf(
                    "id" to queryResult.result().results[0].getInteger(0),
                    "title" to String(Base64.getDecoder().decode(queryResult.result().results[0].getString(1))),
                    "category" to queryResult.result().results[0].getInteger(2),
                    "writer_user_id" to queryResult.result().results[0].getInteger(3),
                    "text" to String(Base64.getDecoder().decode(queryResult.result().results[0].getString(4))),
                    "date" to queryResult.result().results[0].getString(5),
                    "status" to queryResult.result().results[0].getInteger(6),
                    "image" to queryResult.result().results[0].getString(7),
                    "views" to queryResult.result().results[0].getString(8)
                )

                handler.invoke(post, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun moveTrashByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET move_date = ?, status = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(System.currentTimeMillis())
                .add(0)
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun moveDraftByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET move_date = ?, status = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(System.currentTimeMillis())
                .add(2)
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun publishByID(
        id: Int,
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET writer_user_id = ?, `date` = ?, move_date = ?, status = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(userID)
                .add(System.currentTimeMillis())
                .add(System.currentTimeMillis())
                .add(1)
                .add(id)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun insertAndPublish(
        post: Post,
        sqlConnection: SQLConnection,
        handler: (postID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`title`, `category_id`, `writer_user_id`, `post`, `date`, `move_date`, `status`, `image`, `views`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(post.title.toByteArray()))
                .add(post.categoryId)
                .add(post.writerUserID)
                .add(post.post)
                .add(System.currentTimeMillis())
                .add(System.currentTimeMillis())
                .add(1)
                .add(post.imageCode)
                .add(0)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().keys.getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun updateAndPublish(
        userID: Int,
        post: Post,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET title = ?, category_id = ?, writer_user_id = ?, post = ?, `date` = ?, move_date = ?, status = ?, image = ? WHERE `id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(Base64.getEncoder().encodeToString(post.title.toByteArray()))
                .add(post.categoryId)
                .add(post.writerUserID)
                .add(post.post)
                .add(System.currentTimeMillis())
                .add(System.currentTimeMillis())
                .add(1)
                .add(post.imageCode)
                .add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
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

    override fun countByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where category_id = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun countByPageType(
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` WHERE status = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(pageType)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun delete(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${getTablePrefix() + tableName}` WHERE id = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getByCategory(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (posts: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title FROM `${getTablePrefix() + tableName}` WHERE category_id = ? ORDER BY `date` DESC LIMIT 5"

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

    override fun getByPageAndPageType(
        page: Int,
        pageType: Int,
        sqlConnection: SQLConnection,
        handler: (posts: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        var query =
            "SELECT id, title, category_id, writer_user_id, `date`, views, status FROM `${getTablePrefix() + tableName}` WHERE status = ? ORDER BY ${if (pageType == 1) "`date` DESC" else "move_date DESC"} LIMIT 10 OFFSET ${(page - 1) * 10}"

        sqlConnection.queryWithParams(query, JsonArray().add(pageType)) { queryResult ->
            if (queryResult.succeeded()) {
                val posts = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0) {
                    databaseManager.getDatabase().postCategoryDao.getCategories(sqlConnection) { categories, asyncResult ->
                        if (categories == null) {
                            handler.invoke(null, asyncResult)

                            return@getCategories
                        }

                        val handlers: List<(handler: () -> Unit) -> Any> =
                            queryResult.result().results.map { postInDB ->
                                val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                    databaseManager.getDatabase().userDao.getUsernameFromUserID(
                                        postInDB.getInteger(3),
                                        sqlConnection
                                    ) { username, asyncResult ->
                                        if (username == null) {
                                            handler.invoke(null, asyncResult)

                                            return@getUsernameFromUserID
                                        }

                                        var category: Any = "null"

                                        categories.forEach { categoryInDB ->
                                            if (categoryInDB["id"] == postInDB.getInteger(2).toInt())
                                                category = mapOf(
                                                    "id" to categoryInDB["id"] as Int,
                                                    "title" to categoryInDB["title"] as String,
                                                    "url" to categoryInDB["url"] as String,
                                                    "color" to categoryInDB["color"] as String
                                                )
                                        }

                                        if (category == "null")
                                            category = mapOf(
                                                "title" to "-"
                                            )

                                        posts.add(
                                            mapOf(
                                                "id" to postInDB.getInteger(0),
                                                "title" to String(
                                                    Base64.getDecoder().decode(postInDB.getString(1).toByteArray())
                                                ),
                                                "category" to category,
                                                "writer" to mapOf(
                                                    "username" to username
                                                ),
                                                "date" to postInDB.getString(4),
                                                "views" to postInDB.getString(5),
                                                "status" to postInDB.getInteger(6)
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
                                    handler.invoke(posts, asyncResult)
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
                    handler.invoke(posts, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }
}