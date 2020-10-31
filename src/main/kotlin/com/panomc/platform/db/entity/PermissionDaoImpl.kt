package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PermissionDao
import com.panomc.platform.db.model.Permission
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class PermissionDaoImpl(override val tableName: String = "permission") : DaoImpl(), PermissionDao {
    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(16) NOT NULL UNIQUE,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Permission Table';
        """
        ) {
            if (it.succeeded())
                createAdminPermission(sqlConnection) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }

    override fun isTherePermission(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (isTherePermission: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.queryWithParams(
            """
            SELECT COUNT(name) FROM `${databaseManager.getTablePrefix() + tableName}` where name = ?
        """,
            JsonArray().add(permission.name)
        ) {
            if (it.succeeded())
                handler.invoke(it.result().results[0].getInteger(0) != 0, it)
            else
                handler.invoke(null, it)
        }
    }

    override fun add(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (name) VALUES (?)
            """.trimIndent(),
            JsonArray().add(permission.name)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    override fun getPermissionID(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id FROM `${databaseManager.getTablePrefix() + tableName}` where name = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(permission.name)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getPermissionByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (permission: Permission?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `name` FROM `${databaseManager.getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Permission(id, queryResult.result().results[0].getString(0)), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    private fun createAdminPermission(
        sqlConnection: SQLConnection,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) {
        isTherePermission(Permission(-1, "admin"), sqlConnection) { isTherePermission, asyncResult ->
            when {
                isTherePermission == null -> handler.invoke(asyncResult)
                isTherePermission -> handler.invoke(asyncResult)
                else -> add(Permission(-1, "admin"), sqlConnection) { _, asyncResultAdd ->
                    handler.invoke(asyncResultAdd)
                }
            }
        }
    }
}