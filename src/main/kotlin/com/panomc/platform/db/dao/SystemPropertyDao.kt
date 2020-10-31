package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.model.Result
import com.panomc.platform.model.SystemProperty
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface SystemPropertyDao : Dao<SystemProperty> {
    fun add(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun update(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isPropertyExists(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isUserInstalledSystemByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (isUserInstalledSystem: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getValue(
        systemProperty: SystemProperty,
        sqlConnection: SQLConnection,
        handler: (systemProperty: SystemProperty?, asyncResult: AsyncResult<*>) -> Unit
    )
}