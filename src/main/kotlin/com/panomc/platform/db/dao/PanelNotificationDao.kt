package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PanelNotificationDao : Dao<PanelNotification> {
    fun add(
        panelNotification: PanelNotification,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCountByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getAllByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (notifications: List<PanelNotification>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun markReadAll(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getLast5ByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (notifications: List<PanelNotification>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun markReadLat5ByUserID(
        userID: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )
}