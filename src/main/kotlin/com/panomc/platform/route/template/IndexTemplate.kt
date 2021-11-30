package com.panomc.platform.route.template

import com.panomc.platform.Main
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.Template
import com.panomc.platform.util.AuthProvider
import com.panomc.platform.util.SetupManager
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import javax.inject.Inject

class IndexTemplate : Template() {
    private val mHotLinks = mapOf<String, String>()

    override val routes = arrayListOf("/*")

    override val order = 999

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var templateEngine: HandlebarsTemplateEngine

    @Inject
    lateinit var setupManager: SetupManager

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var authProvider: AuthProvider

    override fun getHandler() = Handler<RoutingContext> { context ->
        val response = context.response()
        val normalisedPath = context.normalizedPath()

        if (!mHotLinks[normalisedPath.lowercase()].isNullOrEmpty())
            response.putHeader(
                "location",
                mHotLinks[normalisedPath.lowercase()]
            ).setStatusCode(302).end()
        else if (normalisedPath.startsWith("/panel") && setupManager.isSetupDone())
            authProvider.isLoggedIn(context) { isLoggedIn ->
                if (!isLoggedIn) {
                    handleTemplateEngine(context, false)

                    return@isLoggedIn
                }

                authProvider.hasAccessPanel(context) { hasAccess, _ ->
                    handleTemplateEngine(context, hasAccess)
                }
            }
        else
            handleTemplateEngine(context)
    }

    private fun handleTemplateEngine(context: RoutingContext, hasAccess: Boolean = false) {
        val response = context.response()

        templateEngine
            .render(
                JsonObject()
                    .put("is_panel", hasAccess)
                    .put("is_setup", !setupManager.isSetupDone()),
                "src/main/resources/index-template.hbs"
            ) { res ->
                if (res.succeeded())
                    response.end(res.result())
                else
                    response.end("Hello to Pano web platform!")
            }

    }
}