package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PermissionDao
import com.panomc.platform.db.model.Permission
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

class PermissionDaoImpl(override val tableName: String = "permission") : DaoImpl(), PermissionDao {
    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `name` varchar(16) NOT NULL UNIQUE,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Permission Table';
                        """
                )
                .execute {
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
        sqlConnection: SqlConnection,
        handler: (isTherePermission: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(name) FROM `${getTablePrefix() + tableName}` where name = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) != 0, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun add(
        permission: Permission,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "INSERT INTO `${getTablePrefix() + tableName}` (name) VALUES (?)"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getPermissionID(
        permission: Permission,
        sqlConnection: SqlConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id FROM `${getTablePrefix() + tableName}` where `name` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getPermissionByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (permission: Permission?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `name` FROM `${getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(Permission(id, rows.toList()[0].getString(0)), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    private fun createAdminPermission(
        sqlConnection: SqlConnection,
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