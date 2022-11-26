package com.panomc.platform.db.model

import com.panomc.platform.util.NotificationStatus
import io.vertx.core.json.JsonObject
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

data class Notification(
    val id: Long = -1,
    val userId: Long,
    val typeId: String,
    val action: String,
    val properties: JsonObject = JsonObject(),
    val date: Long = System.currentTimeMillis(),
    val status: NotificationStatus = NotificationStatus.NOT_READ
) {
    companion object {
        fun from(row: Row) = Notification(
            row.getLong(0),
            row.getLong(1),
            row.getString(2),
            row.getString(3),
            if (row.getString(4).isNullOrEmpty()) JsonObject() else JsonObject(row.getString(4)),
            row.getLong(5),
            NotificationStatus.valueOf(row.getString(6)),
        )

        fun from(rowSet: RowSet<Row>) = rowSet.map { from(it) }
    }
}