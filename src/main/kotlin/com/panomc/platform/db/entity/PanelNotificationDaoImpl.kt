package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PanelNotificationDao
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import com.panomc.platform.util.NotificationStatus
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

class PanelNotificationDaoImpl(override val tableName: String = "panel_notification") : DaoImpl(),
    PanelNotificationDao {

    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `user_id` int NOT NULL,
                              `type_ID` varchar(255) NOT NULL,
                              `date` MEDIUMTEXT NOT NULL,
                              `status` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Panel Notification table.';
                        """
                )
                .execute {
                    handler.invoke(it)
                }
        }

    override fun add(
        panelNotification: PanelNotification,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (user_id, type_ID, date, status) " +
                    "VALUES (?, ?, ?, ?)"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    panelNotification.userID,
                    panelNotification.typeID,
                    panelNotification.date,
                    panelNotification.status
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getCountOfNotReadByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT count(`id`) FROM `${getTablePrefix() + tableName}` WHERE (`user_id` = ? OR `user_id` = ?) AND `status` = ? ORDER BY `date` DESC, `id` DESC"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userID,
                    -1,
                    NotificationStatus.NOT_READ,
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getCountByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT count(`id`) FROM `${getTablePrefix() + tableName}` WHERE (`user_id` = ? OR `user_id` = ?) ORDER BY `date` DESC, `id` DESC"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userID,
                    -1
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getLast10ByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (notifications: List<PanelNotification>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userID,
                    -1
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val notifications = mutableListOf<PanelNotification>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            notifications.add(
                                PanelNotification(
                                    row.getInteger(0),
                                    row.getInteger(1),
                                    row.getString(2),
                                    row.getString(3),
                                    NotificationStatus.valueOf(row.getString(4))
                                )
                            )
                        }

                    handler.invoke(notifications, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun get10ByUserIDAndStartFromID(
        userID: Int,
        notificationID: Int,
        sqlConnection: SqlConnection,
        handler: (notifications: List<PanelNotification>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE (`user_id` = ? OR `user_id` = ?) AND id < ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userID,
                    -1,
                    notificationID
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val notifications = mutableListOf<PanelNotification>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            notifications.add(
                                PanelNotification(
                                    row.getInteger(0),
                                    row.getInteger(1),
                                    row.getString(2),
                                    row.getString(3),
                                    NotificationStatus.valueOf(row.getString(4))
                                )
                            )
                        }

                    handler.invoke(notifications, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun markReadLast10(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userID,
                    -1
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun markReadLast10StartFromID(
        userID: Int,
        notificationID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE (`user_id` = ? OR `user_id` = ?) AND id < ?  ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userID,
                    -1,
                    notificationID
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getLast5ByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (notifications: List<PanelNotification>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userID,
                    -1
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    val notifications = mutableListOf<PanelNotification>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            notifications.add(
                                PanelNotification(
                                    row.getInteger(0),
                                    row.getInteger(1),
                                    row.getString(2),
                                    row.getString(3),
                                    NotificationStatus.valueOf(row.getString(4))
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
//        databaseManager.getSqlConnection(connection)
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
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userID,
                    -1
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }
}