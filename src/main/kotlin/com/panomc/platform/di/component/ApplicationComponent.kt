package com.panomc.platform.di.component

import com.panomc.platform.Main
import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.DatabaseMigration
import com.panomc.platform.di.module.*
import com.panomc.platform.model.LoggedInApi
import com.panomc.platform.model.PanelApi
import com.panomc.platform.route.api.TestSendNotificationAPI
import com.panomc.platform.route.api.auth.LogoutAPI
import com.panomc.platform.route.api.panel.*
import com.panomc.platform.route.api.panel.dashboard.CloseConnectServerCardAPI
import com.panomc.platform.route.api.panel.dashboard.CloseGettingStartedCardAPI
import com.panomc.platform.route.api.panel.platformAuth.RefreshKeyAPI
import com.panomc.platform.route.api.panel.post.*
import com.panomc.platform.route.api.panel.post.category.CategoriesAPI
import com.panomc.platform.route.api.panel.post.category.PostCategoryAddAPI
import com.panomc.platform.route.api.panel.post.category.PostCategoryDeleteAPI
import com.panomc.platform.route.api.panel.post.category.PostCategoryUpdateAPI
import com.panomc.platform.route.api.panel.ticket.TicketCategoryPageInitAPI
import com.panomc.platform.route.api.panel.ticket.TicketPageCloseTicketsAPI
import com.panomc.platform.route.api.panel.ticket.TicketPageDeleteTicketsAPI
import com.panomc.platform.route.api.panel.ticket.TicketsPageInitAPI
import com.panomc.platform.route.api.panel.ticket.category.TicketCategoryAddAPI
import com.panomc.platform.route.api.panel.ticket.category.TicketCategoryDeleteAPI
import com.panomc.platform.route.api.panel.ticket.category.TicketCategoryUpdateAPI
import com.panomc.platform.route.api.server.ConnectNewAPI
import com.panomc.platform.route.api.setup.CheckAPI
import com.panomc.platform.route.api.setup.DBConnectionTestAPI
import com.panomc.platform.route.api.setup.FinishAPI
import com.panomc.platform.route.api.setup.step.BackStepAPI
import com.panomc.platform.route.api.setup.step.NextStepAPI
import com.panomc.platform.route.staticFolder.src.SrcFolderRoute
import com.panomc.platform.route.template.IndexTemplate
import com.panomc.platform.util.AssetsStaticHandler
import com.panomc.platform.util.Auth
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
        (DatabaseManagerModule::class),
        (PlatformCodeManagerModule::class),
        (MailClientModule::class)
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

    fun inject(dashboardAPI: DashboardAPI)

    fun inject(refreshKeyAPI: RefreshKeyAPI)

    fun inject(closeGettingStartedCardAPI: CloseGettingStartedCardAPI)

    fun inject(closeConnectServerCardAPI: CloseConnectServerCardAPI)

    fun inject(logoutAPI: LogoutAPI)

    fun inject(connectNewAPI: ConnectNewAPI)

    fun inject(panelNotificationsAPI: PanelNotificationsAPI)

    fun inject(panelQuickNotificationsAPI: PanelQuickNotificationsAPI)

    fun inject(panelQuickNotificationsAndReadAPI: PanelQuickNotificationsAndReadAPI)

    fun inject(testSendNotificationAPI: TestSendNotificationAPI)

    fun inject(ticketsPageInitAPI: TicketsPageInitAPI)

    fun inject(ticketCategoryPageInitAPI: TicketCategoryPageInitAPI)

    fun inject(postsPageInitAPI: PostsPageInitAPI)

    fun inject(postCategoryPageInitAPI: PostCategoryPageInitAPI)

    fun inject(categoriesAPI: CategoriesAPI)

    fun inject(ticketCategoryDeleteAPI: TicketCategoryDeleteAPI)

    fun inject(ticketCategoryAddAPI: TicketCategoryAddAPI)

    fun inject(ticketCategoryUpdateAPI: TicketCategoryUpdateAPI)

    fun inject(postCategoryAddAPI: PostCategoryAddAPI)

    fun inject(postCategoryUpdateAPI: PostCategoryUpdateAPI)

    fun inject(postCategoryDeleteAPI: PostCategoryDeleteAPI)

    fun inject(editPostPageInitAPI: EditPostPageInitAPI)

    fun inject(postDeleteAPI: PostDeleteAPI)

    fun inject(postOnlyPublishAPI: PostOnlyPublishAPI)

    fun inject(postMoveTrashAPI: PostMoveTrashAPI)

    fun inject(postMoveDraftAPI: PostMoveDraftAPI)

    fun inject(postPublishAPI: PostPublishAPI)

    fun inject(loggedInApi: LoggedInApi)

    fun inject(panelApi: PanelApi)

    fun inject(ticketPageCloseTicketsAPI: TicketPageCloseTicketsAPI)

    fun inject(ticketPageDeleteTicketsAPI: TicketPageDeleteTicketsAPI)

    fun inject(playersPageInitAPI: PlayersPageInitAPI)

    fun inject(daoImpl: DaoImpl)

    fun inject(databaseMigration: DatabaseMigration)
}