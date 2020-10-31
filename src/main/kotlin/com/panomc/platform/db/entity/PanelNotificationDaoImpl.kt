package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PanelNotificationDao
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import com.panomc.platform.util.NotificationStatus
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class PanelNotificationDaoImpl(override val tableName: String = "panel_notification") : DaoImpl(),
    PanelNotificationDao {

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,
              `type_ID` varchar(255) NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              `status` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Panel Notification table.';
        """
        ) {
            handler.invoke(it)
        }
    }

    override fun add(
        panelNotification: PanelNotification,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (user_id, type_ID, date, status) " +
                    "VALUES (?, ?, ?, ?)"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(panelNotification.userID)
                .add(panelNotification.typeID)
                .add(panelNotification.date)
                .add(panelNotification.status)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCountByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT count(`id`) FROM `${getTablePrefix() + tableName}` WHERE (`user_id` = ? OR `user_id` = ?) AND `status` = ? ORDER BY `date` DESC, `id` DESC"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add(userID).add(-1).add(NotificationStatus.NOT_READ)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getAllByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notifications: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id`"

        sqlConnection.queryWithParams(query, JsonArray().add(userID).add(-1)) { queryResult ->
            if (queryResult.succeeded()) {
                val notifications = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0)
                    queryResult.result().results.forEach { categoryInDB ->
                        notifications.add(
                            mapOf(
                                "id" to categoryInDB.getInteger(0),
                                "type_ID" to categoryInDB.getString(2),
                                "date" to categoryInDB.getString(3).toLong(),
                                "status" to categoryInDB.getString(4),
                                "isPersonal" to (categoryInDB.getInteger(1) == userID)
                            )
                        )
                    }

                handler.invoke(notifications, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun markReadAll(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add(NotificationStatus.READ).add(userID).add(-1)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getLast5ByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notifications: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        sqlConnection.queryWithParams(query, JsonArray().add(userID).add(-1)) { queryResult ->
            if (queryResult.succeeded()) {
                val notifications = mutableListOf<Map<String, Any>>()

                if (queryResult.result().results.size > 0)
                    queryResult.result().results.forEach { categoryInDB ->
                        notifications.add(
                            mapOf(
                                "id" to categoryInDB.getInteger(0),
                                "type_ID" to categoryInDB.getString(2),
                                "date" to categoryInDB.getString(3).toLong(),
                                "status" to categoryInDB.getString(4),
                                "isPersonal" to (categoryInDB.getInteger(1) == userID)
                            )
                        )
                    }

                handler.invoke(notifications, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

//    private fun getNotifications(
//        connection: Connection,
//        userID: Int,
//        resultHandler: (result: Result) -> Unit,
//        handler: (notifications: List<Map<String, Any>>) -> Unit
//    ) {
//        val query =
//            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}panel_notification WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"
//
//        databaseManager.getSQLConnection(connection)
//            .queryWithParams(query, JsonArray().add(userID).add(-1)) { queryResult ->
//                if (queryResult.succeeded()) {
//                    val notifications = mutableListOf<Map<String, Any>>()
//
//                    if (queryResult.result().results.size > 0)
//                        queryResult.result().results.forEach { categoryInDB ->
//                            notifications.add(
//                                mapOf(
//                                    "id" to categoryInDB.getInteger(0),
//                                    "type_ID" to categoryInDB.getString(2),
//                                    "date" to categoryInDB.getString(3).toLong(),
//                                    "status" to categoryInDB.getString(4),
//                                    "isPersonal" to (categoryInDB.getInteger(1) == userID)
//                                )
//                            )
//                        }
//
//                    handler.invoke(notifications)
//                } else
//                    databaseManager.closeConnection(connection) {
//                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_AND_READ_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_71))
//                    }
//            }
//    }


    override fun markReadLat5ByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        sqlConnection.queryWithParams(
            query,
            JsonArray().add(NotificationStatus.READ).add(userID).add(-1)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}