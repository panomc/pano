package com.panomc.platform.util

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.impl.StaticHandlerImpl
import javax.inject.Inject

class AssetsStaticHandler(private val mRoot: String) : StaticHandlerImpl(mRoot) {

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    init {
        getComponent().inject(this)
    }

    override fun handle(context: RoutingContext) {
        if (context.normalisedPath().startsWith("/panel/"))
            LoginUtil.isAdmin(databaseManager, context) { isAdmin, _ ->
                handle(context, isAdmin)
            }
        else
            handle(context, false)
    }

    private fun handle(context: RoutingContext, isAdmin: Boolean) {
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") var assetsFolderRoot = if (setupManager.isSetupDone())
            "src/main/resources/themes/" + configManager.getConfig().string("current-theme") + "/assets/"
        else
            "src/main/resources/setup/assets/"

        val normalisedPath = context.normalisedPath()

        if (normalisedPath.startsWith("/panel/") && isAdmin)
            assetsFolderRoot = "src/main/resources/panel/assets/"
        else if (normalisedPath.startsWith("/panel/")) {
            context.reroute("/error-404")

            return
        }

        setWebRoot(assetsFolderRoot + mRoot)

        super.handle(context)
    }
}