package com.panomc.platform.route.staticFolder.src

import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.Route
import com.panomc.platform.util.LoginUtil
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.core.impl.StringEscapeUtils
import io.vertx.ext.web.RoutingContext
import java.io.File
import javax.inject.Inject

class SrcFolderRoute : Route() {
    override val routes = arrayListOf("/src/*", "/panel/src/*")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        val normalisedPath = context.normalisedPath()

        if (normalisedPath.startsWith("/panel/"))
            LoginUtil.isAdmin(databaseManager, context) { isAdmin, _ ->
                handle(context, isAdmin)
            }
        else
            handle(context)
    }

    private fun handle(context: RoutingContext, isAdmin: Boolean = false) {
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") var srcFolderRoot = if (setupManager.isSetupDone())
            "src/main/resources/themes/" + configManager.getConfig().string("current-theme") + "/"
        else
            "src/main/resources/setup/"

        val response = context.response()
        val normalisedPath = context.normalisedPath()

        if (setupManager.isSetupDone())
            if (normalisedPath.startsWith("/panel/") && isAdmin)
                srcFolderRoot = "src/main/resources/"
            else if (normalisedPath.startsWith("/panel/")) {
                context.reroute("/error-404")

                return
            }

        val pathSplit = normalisedPath.split("/")

        val componentName = pathSplit.lastOrNull() ?: ""

        val componentFile = File("$srcFolderRoot$normalisedPath/$componentName.js")
        val componentUIFile = File("$srcFolderRoot$normalisedPath/$componentName.html")

        if (componentFile.exists() && componentUIFile.exists())
            response.end(
                componentFile.readText().replace(
                    "PANO.UI",
                    "\"${StringEscapeUtils.escapeJavaScript(componentUIFile.readText())}\""
                )
            )
        else
            if (setupManager.isSetupDone())
                if (normalisedPath.startsWith("/panel/") && isAdmin)
                    context.reroute("/panel/error-404")
                else
                    context.reroute("/error-404")
            else
                context.reroute("/error-404")

    }
}