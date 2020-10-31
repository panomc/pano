package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.SystemPropertyDao
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class SystemPropertyDaoImpl(override val tableName: String = "system_property") : DaoImpl(), SystemPropertyDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `option` text NOT NULL,
              `value` text NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='System Property table.';
        """
        ) {
            if (it.succeeded())
                addShowGettingStartedOption(sqlConnection) {
                    handler.invoke(it)
                }
            else
                handler.invoke(it)
        }
    }

    override fun add(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`option`, `value`) VALUES (?, ?)
            """.trimIndent(),
            JsonArray()
                .add(systemProperty.option)
                .add(systemProperty.value)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    override fun update(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val params = JsonArray()

        params.add(systemProperty.value)
        params.add(systemProperty.option)

        if (systemProperty.id != -1)
            params.add(systemProperty.id)

        sqlConnection.updateWithParams(
            """
                UPDATE `${databaseManager.getTablePrefix() + tableName}` SET value = ? ${if (systemProperty.id != -1) ", option = ?" else ""} WHERE `${if (systemProperty.id == -1) "option" else "id"}` = ?
            """.trimIndent(),
            params
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }


    override fun isPropertyExists(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT COUNT(`value`) FROM `${databaseManager.getTablePrefix() + tableName}` where `option` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(systemProperty.option)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) != 0, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isUserInstalledSystemByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (isUserInstalledSystem: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(`value`) FROM `${databaseManager.getTablePrefix() + tableName}` where `option` = ? and `value` = ?"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add("who_installed_user_id").add(userID.toString())
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) != 0, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getValue(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (systemProperty: SystemProperty?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT `value` FROM `${databaseManager.getTablePrefix() + tableName}` where `option` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(systemProperty.option)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(
                    SystemProperty(
                        systemProperty.id,
                        systemProperty.option,
                        queryResult.result().results[0].getString(0)
                    ), queryResult
                )
            else
                handler.invoke(null, queryResult)
        }
    }

    private fun addShowGettingStartedOption(
        sqlConnection: SQLConnection,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) {
        add(SystemProperty(-1, "show_getting_started", "true"), sqlConnection) { _, asyncResult ->
            handler.invoke(asyncResult)
        }
    }
}