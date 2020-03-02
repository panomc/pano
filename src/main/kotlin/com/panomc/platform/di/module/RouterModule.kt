package com.panomc.platform.di.module

import com.panomc.platform.model.RouteType
import com.panomc.platform.model.Template
import com.panomc.platform.route.api.get.panel.BasicDataAPI
import com.panomc.platform.route.api.get.panel.PanelNotificationsAPI
import com.panomc.platform.route.api.get.panel.PanelQuickNotificationsAPI
import com.panomc.platform.route.api.get.panel.initPage.DashboardAPI
import com.panomc.platform.route.api.get.panel.platformAuth.RefreshKeyAPI
import com.panomc.platform.route.api.get.panel.post.category.CategoriesAPI
import com.panomc.platform.route.api.post.auth.LogoutAPI
import com.panomc.platform.route.api.post.panel.dashboard.CloseConnectServerCardAPI
import com.panomc.platform.route.api.post.panel.dashboard.CloseGettingStartedCardAPI
import com.panomc.platform.route.api.post.panel.post.*
import com.panomc.platform.route.api.post.panel.post.category.PostCategoryAddAPI
import com.panomc.platform.route.api.post.panel.post.category.PostCategoryDeleteAPI
import com.panomc.platform.route.api.post.panel.post.category.PostCategoryUpdateAPI
import com.panomc.platform.route.api.post.panel.ticket.TicketCategoryPageInitAPI
import com.panomc.platform.route.api.post.panel.ticket.TicketsPageInitAPI
import com.panomc.platform.route.api.post.panel.ticket.category.TicketCategoryAddAPI
import com.panomc.platform.route.api.post.panel.ticket.category.TicketCategoryDeleteAPI
import com.panomc.platform.route.api.post.panel.ticket.category.TicketCategoryUpdateAPI
import com.panomc.platform.route.api.post.server.ConnectNewAPI
import com.panomc.platform.route.api.post.setup.DBConnectionTestAPI
import com.panomc.platform.route.api.post.setup.FinishAPI
import com.panomc.platform.route.api.post.setup.step.BackStepAPI
import com.panomc.platform.route.api.post.setup.step.CheckAPI
import com.panomc.platform.route.api.post.setup.step.NextStepAPI
import com.panomc.platform.route.staticFolder.assets.*
import com.panomc.platform.route.staticFolder.common.CommonCSSFolder
import com.panomc.platform.route.staticFolder.common.CommonFontsFolder
import com.panomc.platform.route.staticFolder.common.CommonImgFolder
import com.panomc.platform.route.staticFolder.common.CommonJsFolder
import com.panomc.platform.route.staticFolder.src.SrcFolderRoute
import com.panomc.platform.route.template.IndexTemplate
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
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
        arrayOf(
            CommonCSSFolder(),
            CommonFontsFolder(),
            CommonImgFolder(),
            CommonJsFolder(),

            AssetsCssFolder(),
            AssetsFontsFolder(),
            AssetsJsFolder(),
            AssetsImgFolder(),
            AssetsLangFolder(),

            SrcFolderRoute()
        )
    }

    private val mTemplateRouteList by lazy {
        arrayOf<Template>(
            IndexTemplate()
        )
    }

    private val mAPIRouteList by lazy {
        arrayOf(
            CheckAPI(),
            BackStepAPI(),
            NextStepAPI(),
            DBConnectionTestAPI(),
            FinishAPI(),

            BasicDataAPI(),

            DashboardAPI(),

            RefreshKeyAPI(),

            CloseGettingStartedCardAPI(),
            CloseConnectServerCardAPI(),

            LogoutAPI(),

            ConnectNewAPI(),

            TicketsPageInitAPI(),
            TicketCategoryPageInitAPI(),

            PostsPageInitAPI(),
            PostCategoryPageInitAPI(),

            CategoriesAPI(),

            TicketCategoryDeleteAPI(),
            TicketCategoryAddAPI(),
            TicketCategoryUpdateAPI(),

            PostCategoryAddAPI(),
            PostCategoryUpdateAPI(),
            PostCategoryDeleteAPI(),

            EditPostPageInitAPI(),

            PostDeleteAPI(),
            PostOnlyPublishAPI(),
            PostMoveTrashAPI(),
            PostMoveDraftAPI(),
            PostPublishAPI(),

            PanelNotificationsAPI(),
            PanelQuickNotificationsAPI()
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
        router.route().handler(SessionHandler.create(LocalSessionStore.create(mVertx)))

        val allowedHeaders: MutableSet<String> = HashSet()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")

        val allowedMethods: MutableSet<HttpMethod> = HashSet()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)

        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)
        allowedMethods.add(HttpMethod.PUT)

        router.route().handler(
            CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods)
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