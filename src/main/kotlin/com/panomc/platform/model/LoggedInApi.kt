package com.panomc.platform.model

import com.panomc.platform.Main
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

abstract class LoggedInApi : Api() {
    init {
        @Suppress("LeakingThis")
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        if (!setupManager.isSetupDone()) {
            context.reroute("/")

            return@Handler
        }

        LoginUtil.isLoggedIn(databaseManager, context) { isLoggedIn, _ ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            getHandler(context)
        }
    }
}