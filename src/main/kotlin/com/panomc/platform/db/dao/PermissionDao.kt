package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.Permission
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PermissionDao : Dao<Permission> {
    fun isTherePermission(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (isTherePermission: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun add(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getPermissionID(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (permissionID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getPermissionByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (permission: Permission?, asyncResult: AsyncResult<*>) -> Unit
    )
}