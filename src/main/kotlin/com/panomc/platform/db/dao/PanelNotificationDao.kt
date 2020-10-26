package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.PanelNotification
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PanelNotificationDao : Dao<PanelNotification> {
    fun add(
        panelNotification: PanelNotification,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCountByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getAllByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notifications: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun markReadAll(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getLast5ByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notifications: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun markReadLat5ByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}