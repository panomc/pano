package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION
import com.panomc.platform.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION_INFO
import com.panomc.platform.db.dao.SchemeVersionDao
import com.panomc.platform.model.Result
import com.panomc.platform.model.SchemeVersion
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class SchemeVersionDaoImpl(override val tableName: String = "scheme_version") : DaoImpl(), SchemeVersionDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `when` timestamp not null default CURRENT_TIMESTAMP,
              `key` varchar(255) not null,
              `extra` varchar(255),
              PRIMARY KEY (`key`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Database scheme version table.';
        """
        ) {
            if (it.succeeded())
                getLastSchemeVersion(sqlConnection) { schemeVersion, asyncResult ->
                    if (schemeVersion == null)
                        add(
                            sqlConnection,
                            SchemeVersion(DATABASE_SCHEME_VERSION.toString(), DATABASE_SCHEME_VERSION_INFO)
                        ) { _, asyncResultAdd ->
                            handler.invoke(asyncResultAdd)
                        }
                    else {
                        val databaseVersion = schemeVersion.key.toIntOrNull() ?: 0

                        if (databaseVersion == 0)
                            add(
                                sqlConnection,
                                SchemeVersion(DATABASE_SCHEME_VERSION.toString(), DATABASE_SCHEME_VERSION_INFO)
                            ) { _, asyncResultAdd ->
                                handler.invoke(asyncResultAdd)
                            }
                        else
                            handler.invoke(asyncResult)
                    }

                }
            else
                handler.invoke(it)
        }
    }

    override fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) = sqlConnection.updateWithParams(
        """
            INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`key`, `extra`) VALUES (?, ?)
        """.trimIndent(),
        JsonArray()
            .add(schemeVersion.key)
            .add(schemeVersion.extra)
    ) {
        handler.invoke(if (it.succeeded()) Successful() else null, it)
    }!!

    override fun add(schemeVersion: SchemeVersion, handler: (result: Result?) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection != null) {
                add(databaseManager.getSQLConnection(connection), schemeVersion) { result, _ ->
                    databaseManager.closeConnection(connection) {
                        handler.invoke(result)
                    }
                }
            }
        }
    }

    override fun getLastSchemeVersion(
        sqlConnection: SQLConnection,
        handler: (schemeVersion: SchemeVersion?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT MAX(`key`) FROM `${databaseManager.getTablePrefix() + tableName}`"

        sqlConnection.query(
            query
        ) { queryResult ->
            if (queryResult.failed()) {
                handler.invoke(null, queryResult)

                return@query
            }

            handler.invoke(
                if (queryResult.result().results[0].getString(0) == null) null else SchemeVersion(
                    queryResult.result().results[0].getString(
                        0
                    ), null
                ), queryResult
            )
        }
    }
}