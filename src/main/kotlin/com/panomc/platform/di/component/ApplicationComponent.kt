package com.panomc.platform.di.component

import com.panomc.platform.Main
import com.panomc.platform.di.module.*
import com.panomc.platform.route.api.get.panel.BasicDataAPI
import com.panomc.platform.route.api.get.panel.initPage.DashboardAPI
import com.panomc.platform.route.api.get.panel.platformAuth.RefreshKeyAPI
import com.panomc.platform.route.api.get.panel.post.category.CategoriesAPI
import com.panomc.platform.route.api.post.auth.LogoutAPI
import com.panomc.platform.route.api.post.panel.dashboard.CloseConnectServerCardAPI
import com.panomc.platform.route.api.post.panel.dashboard.CloseGettingStartedCardAPI
import com.panomc.platform.route.api.post.panel.post.PostCategoryPageInitAPI
import com.panomc.platform.route.api.post.panel.post.PostsPageInitAPI
import com.panomc.platform.route.api.post.panel.ticket.TicketCategoryPageInitAPI
import com.panomc.platform.route.api.post.panel.ticket.TicketsPageInitAPI
import com.panomc.platform.route.api.post.panel.ticket.category.TicketCategoryAddAPI
import com.panomc.platform.route.api.post.panel.ticket.category.TicketCategoryDeleteAPI
import com.panomc.platform.route.api.post.server.ConnectNewAPI
import com.panomc.platform.route.api.post.setup.DBConnectionTestAPI
import com.panomc.platform.route.api.post.setup.FinishAPI
import com.panomc.platform.route.api.post.setup.step.BackStepAPI
import com.panomc.platform.route.api.post.setup.step.CheckAPI
import com.panomc.platform.route.api.post.setup.step.NextStepAPI
import com.panomc.platform.route.staticFolder.src.SrcFolderRoute
import com.panomc.platform.route.template.IndexTemplate
import com.panomc.platform.util.AssetsStaticHandler
import com.panomc.platform.util.Auth
import com.panomc.platform.util.PlatformCodeGenerator
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (RecaptchaModule::class),
        (VertxModule::class),
        (LoggerModule::class),
        (RouterModule::class),
        (TemplateEngineModule::class),
        (ConfigManagerModule::class),
        (SetupManagerModule::class),
        (DatabaseManagerModule::class)
    ]
)
interface ApplicationComponent {
    fun inject(main: Main)

    fun inject(indexTemplate: IndexTemplate)

    fun inject(assetsStaticHandler: AssetsStaticHandler)

    fun inject(srcFolderRoute: SrcFolderRoute)

    fun inject(checkAPI: CheckAPI)

    fun inject(backStepAPI: BackStepAPI)

    fun inject(nextStepAPI: NextStepAPI)

    fun inject(dbConnectionTestAPI: DBConnectionTestAPI)

    fun inject(finishAPI: FinishAPI)

    fun inject(auth: Auth)

    fun inject(basicDataAPI: BasicDataAPI)

    fun inject(platformCodeGenerator: PlatformCodeGenerator)

    fun inject(dashboardAPI: DashboardAPI)

    fun inject(refreshKeyAPI: RefreshKeyAPI)

    fun inject(closeGettingStartedCardAPI: CloseGettingStartedCardAPI)

    fun inject(closeConnectServerCardAPI: CloseConnectServerCardAPI)

    fun inject(logoutAPI: LogoutAPI)

    fun inject(connectNewAPI: ConnectNewAPI)

    fun inject(ticketsPageInitAPI: TicketsPageInitAPI)

    fun inject(ticketCategoryPageInitAPI: TicketCategoryPageInitAPI)

    fun inject(postsPageInitAPI: PostsPageInitAPI)

    fun inject(postCategoryPageInitAPI: PostCategoryPageInitAPI)

    fun inject(categoriesAPI: CategoriesAPI)

    fun inject(ticketCategoryDeleteAPI: TicketCategoryDeleteAPI)

    fun inject(ticketCategoryAddAPI: TicketCategoryAddAPI)
}