package com.panomc.platform.route.api.panel.permission

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PermissionGroup
import com.panomc.platform.model.*
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.SetupManager
import io.vertx.ext.web.RoutingContext

@Endpoint
class PermissionUpdateGroupAPI(
    private val databaseManager: DatabaseManager,
    setupManager: SetupManager,
    authProvider: AuthProvider
) : PanelApi(setupManager, authProvider) {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/permission/update/group")

    override suspend fun handler(context: RoutingContext): Result {
        val data = context.bodyAsJson

        val id = data.getInteger("id")
        var name = data.getString("name")

        validateForm(name)

        name = getSystematicName(name)

        val sqlConnection = createConnection(databaseManager, context)

        val isTherePermissionGroupById = databaseManager.permissionGroupDao.isThereByID(id, sqlConnection)

        if (!isTherePermissionGroupById) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val isTherePermissionGroupByName =
            databaseManager.permissionGroupDao.isThere(PermissionGroup(id, name), sqlConnection)

        if (isTherePermissionGroupByName) {
            throw Errors(mapOf("name" to true))
        }

        val permissionGroup =
            databaseManager.permissionGroupDao.getPermissionGroupByID(id, sqlConnection) ?: throw Error(
                ErrorCode.UNKNOWN
            )

        if (permissionGroup.name == "admin") {
            throw Error(ErrorCode.CANT_UPDATE_ADMIN_PERMISSION)
        }

        databaseManager.permissionGroupDao.update(
            PermissionGroup(id, name),
            sqlConnection
        )

        return Successful()
    }

    private fun validateForm(
        name: String
    ) {
        val errors = mutableMapOf<String, Boolean>()

        if (name.isEmpty() || name.length > 32)
            errors["name"] = true

        if (errors.isNotEmpty()) {
            throw Errors(errors)
        }
    }

    private fun getSystematicName(name: String) = name.lowercase().replace("\\s+".toRegex(), "-")
}