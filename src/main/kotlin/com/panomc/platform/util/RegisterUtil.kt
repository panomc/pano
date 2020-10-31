package com.panomc.platform.util

import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.User
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.ext.sql.SQLConnection

object RegisterUtil {
    fun register(
        databaseManager: DatabaseManager,
        user: User,
        sqlConnection: SQLConnection,
        handler: (isSuccessful: Result?) -> Unit
    ) {
        databaseManager.getDatabase().userDao.add(user, sqlConnection) { isSuccessful, _ ->
            if (isSuccessful == null) {
                handler.invoke(null)

                return@add
            }

            handler.invoke(Successful())
        }
    }
}