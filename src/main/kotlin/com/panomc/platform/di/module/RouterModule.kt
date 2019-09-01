package com.panomc.platform.di.module

import com.panomc.platform.model.Route
import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Template
import com.panomc.platform.route.template.IndexTemplate
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import javax.inject.Singleton

@Module
class RouterModule(private val mVertx: Vertx) {
    @Singleton
    private val mRouter by lazy {
        val router = Router.router(mVertx)

        init(router)

        router
    }

    private val mStaticFolderRouteList by lazy {
        arrayOf<Route>(
        )
    }

    private val mTemplateRouteList by lazy {
        arrayOf<Template>(
            IndexTemplate()
        )
    }

    private val mAPIRouteList by lazy {
        arrayOf<Route>(
        )
    }

    private val mRouteList by lazy {
        listOf(
            *mStaticFolderRouteList,
            *mAPIRouteList,
            *mTemplateRouteList
        )
    }

    private fun init(router: Router) {
        router.route().handler(BodyHandler.create())
        router.route().handler(CookieHandler.create())
        router.route().handler(SessionHandler.create(LocalSessionStore.create(mVertx)))

        val allowedHeaders = mutableSetOf<String>()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")

        val allowedMethods = mutableSetOf<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)

        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)
        allowedMethods.add(HttpMethod.PUT)

        router.route().handler(
            CorsHandler.create(".*").allowCredentials(true).allowedHeaders(allowedHeaders).allowedMethods(allowedMethods)
        )

        mRouteList.forEach { route ->
            route.routes.forEach { url ->
                when (route.routeType) {
                    RouteType.ROUTE -> router.route(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.GET -> router.get(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.POST -> router.post(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.DELETE -> router.delete(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.PUT -> router.put(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideRouter() = mRouter

}