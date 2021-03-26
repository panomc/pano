package com.panomc.platform.db.dao

import com.panomc.platform.db.Dao
import com.panomc.platform.db.model.PermissionGroupPerms
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface PermissionGroupPermsDao : Dao<PermissionGroupPerms> {
    fun getPermissionGroupPerms(
        sqlConnection: SqlConnection,
        handler: (permissionGroupPerms: List<PermissionGroupPerms>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun doesPermissionGroupHavePermission(
        permissionGroupID: Int,
        permissionID: Int,
        sqlConnection: SqlConnection,
        handler: (isTherePermission: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )
}