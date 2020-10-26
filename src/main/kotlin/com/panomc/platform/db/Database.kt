package com.panomc.platform.db

import com.panomc.platform.db.dao.*
import com.panomc.platform.db.entity.*
import io.vertx.ext.sql.SQLConnection

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl(),
    val userDao: UserDao = UserDaoImpl(),
    val permissionDao: PermissionDao = PermissionDaoImpl(),
    val tokenDao: TokenDao = TokenDaoImpl(),
    val panelConfigDao: PanelConfigDao = PanelConfigDaoImpl(),
    val serverDao: ServerDao = ServerDaoImpl(),
    val systemPropertyDao: SystemPropertyDao = SystemPropertyDaoImpl(),
    val panelNotificationDao: PanelNotificationDao = PanelNotificationDaoImpl(),
    val postDao: PostDao = PostDaoImpl(),
    val postCategoryDao: PostCategoryDao = PostCategoryDaoImpl(),
    val ticketDao: TicketDao = TicketDaoImpl(),
    val ticketCategoryDao: TicketCategoryDao = TicketCategoryDaoImpl()
) {
    fun init(
        sqlConnection: SQLConnection
    ) = listOf(
        schemeVersionDao.init(sqlConnection),
        userDao.init(sqlConnection),
        permissionDao.init(sqlConnection),
        tokenDao.init(sqlConnection),
        panelConfigDao.init(sqlConnection),
        serverDao.init(sqlConnection),
        systemPropertyDao.init(sqlConnection),
        panelNotificationDao.init(sqlConnection),
        postDao.init(sqlConnection),
        postCategoryDao.init(sqlConnection),
        ticketDao.init(sqlConnection),
        ticketCategoryDao.init(sqlConnection)
    )
}