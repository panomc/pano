package com.panomc.platform.util

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Token
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.http.Cookie
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

object LoginUtil {
    const val COOKIE_NAME = "pano_token"
    const val SESSION_NAME = "user_id"

    fun login(
        usernameOrEmail: String,
        rememberMe: Boolean,
        routingContext: RoutingContext,
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        handler: (isLoggedIn: Any, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
            usernameOrEmail,
            sqlConnection
        ) { userID, asyncResultOfUserID ->
            if (userID == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_219), asyncResultOfUserID)

                return@getUserIDFromUsernameOrEmail
            }

            if (rememberMe)
                TokenUtil.createToken(
                    TokenUtil.SUBJECT.LOGIN_SESSION,
                    userID,
                    databaseManager,
                    sqlConnection
                ) { token, asyncResultOfCreateToken ->
                    if (token == null) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_220), asyncResultOfCreateToken)

                        return@createToken
                    }

                    val age = 60 * 60 * 24 * 365 * 2L // 2 years valid
                    val path = "/" // root dir

                    val tokenCookie = Cookie.cookie(COOKIE_NAME, token)

                    tokenCookie.setMaxAge(age)
                    tokenCookie.path = path

                    routingContext.response().addCookie(tokenCookie)

                    handler.invoke(true, asyncResultOfCreateToken)
                }
            else {
                routingContext.session().put(SESSION_NAME, userID)


                handler.invoke(true, asyncResultOfUserID)
            }
        }
    }

    fun isLoggedIn(
        databaseManager: DatabaseManager,
        routingContext: RoutingContext,
        handler: (isLoggedIn: Boolean, asyncResult: AsyncResult<*>?) -> Unit
    ) {
        val idOrToken = getUserIDOrToken(routingContext)

        if (idOrToken == null) {
            handler.invoke(false, null)

            return
        }

        if (idOrToken is Int) {
            handler.invoke(true, null)

            return
        }

        if (idOrToken !is String) {
            handler.invoke(false, null)

            return
        }

        databaseManager.createConnection { sqlConnection, asyncResult ->
            if (sqlConnection == null) {
                handler.invoke(false, asyncResult)

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(
                idOrToken,
                sqlConnection
            ) { isTokenExists, asyncResultOfIsTokenExists ->
                databaseManager.closeConnection(sqlConnection) {
                    if (isTokenExists == null) {
                        handler.invoke(false, asyncResultOfIsTokenExists)

                        return@closeConnection
                    }

                    handler.invoke(isTokenExists, asyncResult)
                }
            }
        }
    }

//    fun isAdmin(
//        databaseManager: DatabaseManager,
//        routingContext: RoutingContext,
//        handler: (isAdmin: Boolean, asyncResult: AsyncResult<*>?) -> Unit
//    ) {
//        val cookie = routingContext.getCookie(COOKIE_NAME)
//
//        val token = cookie.value
//
//        databaseManager.createConnection { sqlConnection, asyncResultOfCreateConnection ->
//            if (sqlConnection == null) {
//                handler.invoke(false, asyncResultOfCreateConnection)
//
//                return@createConnection
//            }
//
//            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
//                token,
//                sqlConnection
//            ) { userID, asyncResultGetUserIDFromToken ->
//                if (userID == null) {
//                    databaseManager.closeConnection(sqlConnection) {
//                        handler.invoke(false, asyncResultGetUserIDFromToken)
//                    }
//
//                    return@getUserIDFromToken
//                }
//
//                if (userID == 0) {
//                    databaseManager.closeConnection(sqlConnection) {
//                        handler.invoke(false, asyncResultGetUserIDFromToken)
//                    }
//
//                    return@getUserIDFromToken
//                }
//
//                databaseManager.getDatabase().userDao.getPermissionGroupIDFromUserID(
//                    userID,
//                    sqlConnection
//                ) { permissionGroupID, asyncResultOfGetPermissionGroupIDFromUserID ->
//                    if (permissionGroupID == null) {
//                        databaseManager.closeConnection(sqlConnection) {
//                            handler.invoke(false, asyncResultOfGetPermissionGroupIDFromUserID)
//                        }
//
//                        return@getPermissionGroupIDFromUserID
//                    }
//
//                    if (permissionGroupID == -1) {
//                        databaseManager.closeConnection(sqlConnection) {
//                            handler.invoke(false, asyncResultOfGetPermissionGroupIDFromUserID)
//                        }
//
//                        return@getPermissionGroupIDFromUserID
//                    }
//
//                    databaseManager.getDatabase().permissionGroupDao.getPermissionGroupByID(
//                        permissionGroupID,
//                        sqlConnection
//                    ) { permissionGroup, asyncResultOfGetPermissionGroupID ->
//                        databaseManager.closeConnection(sqlConnection) {
//                            if (permissionGroup == null) {
//                                handler.invoke(false, asyncResultOfGetPermissionGroupID)
//
//                                return@closeConnection
//                            }
//
//                            if (permissionGroup.name != "admin") {
//                                handler.invoke(false, asyncResultOfGetPermissionGroupID)
//
//                                return@closeConnection
//                            }
//
//                            handler.invoke(true, asyncResultOfGetPermissionGroupID)
//                        }
//                    }
//                }
//            }
//        }
//    }

    fun hasAccessPanel(
        databaseManager: DatabaseManager,
        routingContext: RoutingContext,
        handler: (hasAccess: Boolean, asyncResult: AsyncResult<*>?) -> Unit
    ) {
        val idOrToken = getUserIDOrToken(routingContext)

        if (idOrToken == null) {
            handler.invoke(false, null)

            return
        }

        databaseManager.createConnection { sqlConnection, asyncResultOfCreateConnection ->
            if (sqlConnection == null) {
                handler.invoke(false, asyncResultOfCreateConnection)

                return@createConnection
            }

            if (idOrToken is Int) {
                databaseManager.getDatabase().userDao.getPermissionGroupIDFromUserID(
                    idOrToken,
                    sqlConnection,
                    (this::getPermissionGroupIDFromUserIDHandler)(databaseManager, sqlConnection, handler)
                )

                return@createConnection
            }

            if (idOrToken !is String) {
                databaseManager.closeConnection(sqlConnection) {
                    handler.invoke(false, null)
                }

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(
                idOrToken,
                sqlConnection
            ) { isTokenExists: Boolean?, asyncResultOfIsTokenExists: AsyncResult<*> ->
                if (isTokenExists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(false, asyncResultOfIsTokenExists)
                    }

                    return@isTokenExists
                }

                if (!isTokenExists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(false, asyncResultOfIsTokenExists)
                    }

                    return@isTokenExists
                }

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                    idOrToken,
                    sqlConnection
                ) { userID, asyncResultGetUserIDFromToken ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(false, asyncResultGetUserIDFromToken)
                        }

                        return@getUserIDFromToken
                    }

                    databaseManager.getDatabase().userDao.getPermissionGroupIDFromUserID(
                        userID,
                        sqlConnection,
                        (this::getPermissionGroupIDFromUserIDHandler)(databaseManager, sqlConnection, handler)
                    )
                }
            }
        }
    }

    fun logout(
        databaseManager: DatabaseManager,
        routingContext: RoutingContext,
        handler: (isLoggedOut: Result?, asyncResult: AsyncResult<*>?) -> Unit
    ) {
        val session = routingContext.session().get<Int?>(SESSION_NAME)

        if (session != null) {
            routingContext.session().destroy()

            handler.invoke(Successful(), null)

            return
        }

        val cookie = routingContext.request().getCookie(COOKIE_NAME)

        if (cookie != null) {
            val token = cookie.value

            routingContext.response().removeCookie(COOKIE_NAME)

            databaseManager.createConnection { sqlConnection, asyncResult ->
                if (sqlConnection == null) {
                    handler.invoke(null, asyncResult)

                    return@createConnection
                }

                databaseManager.getDatabase().tokenDao.delete(
                    Token(
                        -1,
                        token,
                        -1,
                        -1,
                        TokenUtil.SUBJECT.LOGIN_SESSION.toString()
                    ), sqlConnection
                ) { result, asyncResultOfDelete ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (result == null) {
                            handler.invoke(null, asyncResultOfDelete)

                            return@closeConnection
                        }

                        handler.invoke(Successful(), asyncResultOfDelete)
                    }
                }
            }

            return
        }

        handler.invoke(Successful(), null)
    }

    fun getUserIDOrToken(routingContext: RoutingContext): Any? {
        val session = routingContext.session().get<Int?>(SESSION_NAME)

        if (session != null) {
            return session
        }

        val cookie = routingContext.request().getCookie(COOKIE_NAME) ?: return null

        if (cookie.value !is String)
            return null

        return cookie
    }

    private fun getPermissionGroupIDFromUserIDHandler(
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        handler: (hasAccess: Boolean, asyncResult: AsyncResult<*>?) -> Unit
    ) = handler@{ permissionGroupID: Int?, asyncResult: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (permissionGroupID == null) {
                handler.invoke(false, asyncResult)

                return@closeConnection
            }

            if (permissionGroupID == -1) {
                handler.invoke(false, asyncResult)

                return@closeConnection
            }

            handler.invoke(true, asyncResult)
        }
    }
}