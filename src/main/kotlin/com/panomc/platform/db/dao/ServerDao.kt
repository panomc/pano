package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Server
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface ServerDao : Dao<Server> {
    fun add(
        server: Server,
        sqlConnection: SQLConnection,
        handler: (token: String?, asyncResult: AsyncResult<*>) -> Unit
    )
}