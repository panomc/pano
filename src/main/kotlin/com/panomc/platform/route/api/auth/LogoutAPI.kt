package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.Error
import com.panomc.platform.model.LoggedInApi
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class LogoutAPI : LoggedInApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/auth/logout")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        LoginUtil.logout(databaseManager, context) { isLoggedOut, _ ->
            if (isLoggedOut == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_28))

                return@logout
            }

            handler.invoke(isLoggedOut)
        }
    }
}