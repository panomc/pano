package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.Error
import com.panomc.platform.model.LoggedInApi
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext

class LogoutAPI : LoggedInApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/auth/logout")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        LoginUtil.logout(databaseManager, context, (this::logoutHandler)(handler))
    }

    private fun logoutHandler(handler: (result: Result) -> Unit) = handler@{ isLoggedOut: Result?, _: AsyncResult<*>? ->
        if (isLoggedOut == null) {
            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_28))

            return@handler
        }

        handler.invoke(isLoggedOut)
    }
}