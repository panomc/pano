package com.panomc.platform.util

import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Token
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
        password: String,
        rememberMe: Boolean,
        routingContext: RoutingContext,
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        handler: (isLoggedIn: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.isLoginCorrect(
            usernameOrEmail,
            password,
            sqlConnection
        ) { loginCorrect, asyncResult ->
            if (loginCorrect == null) {
                handler.invoke(null, asyncResult)

                return@isLoginCorrect
            }

            if (loginCorrect) {
                databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
                    usernameOrEmail,
                    sqlConnection
                ) { userID, asyncResultOfUserID ->
                    if (userID == null) {
                        handler.invoke(null, asyncResultOfUserID)

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
                                handler.invoke(null, asyncResultOfCreateToken)

                                return@createToken
                            }

                            val age = 60 * 60 * 24 * 365 * 2L // 2 years valid
                            val path = "/" // root dir

                            val tokenCookie = Cookie.cookie(COOKIE_NAME, token)

                            tokenCookie.setMaxAge(age)
                            tokenCookie.path = path

                            routingContext.addCookie(tokenCookie)

                            handler.invoke(true, asyncResultOfCreateToken)
                        }
                    else {
                        routingContext.session().put(SESSION_NAME, userID)


                        handler.invoke(true, asyncResult)
                    }
                }

                return@isLoginCorrect
            }

            handler.invoke(false, asyncResult)
        }
    }

    fun isLoggedIn(
        databaseManager: DatabaseManager,
        routingContext: RoutingContext,
        handler: (isLoggedIn: Boolean, asyncResult: AsyncResult<*>?) -> Unit
    ) {
        val session = routingContext.session().get<String?>(SESSION_NAME)

        if (session != null) {
            handler.invoke(true, null)

            return
        }

        val cookie = routingContext.getCookie(COOKIE_NAME)

        if (cookie == null) {
            handler.invoke(false, null)

            return
        }

        val token = cookie.value

        databaseManager.createConnection { sqlConnection, asyncResult ->
            if (sqlConnection == null) {
                handler.invoke(false, asyncResult)

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(
                token,
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
//                    if (permissionGroupID == 0) {
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
        val cookie = routingContext.getCookie(COOKIE_NAME)

        val token = cookie.value

        databaseManager.createConnection { sqlConnection, asyncResultOfCreateConnection ->
            if (sqlConnection == null) {
                handler.invoke(false, asyncResultOfCreateConnection)

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                token,
                sqlConnection
            ) { userID, asyncResultGetUserIDFromToken ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(false, asyncResultGetUserIDFromToken)
                    }

                    return@getUserIDFromToken
                }

                if (userID == 0) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(false, asyncResultGetUserIDFromToken)
                    }

                    return@getUserIDFromToken
                }

                databaseManager.getDatabase().userDao.getPermissionGroupIDFromUserID(
                    userID,
                    sqlConnection
                ) { permissionGroupID, asyncResultOfGetPermissionGroupIDFromUserID ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (permissionGroupID == null) {
                            handler.invoke(false, asyncResultOfGetPermissionGroupIDFromUserID)

                            return@closeConnection
                        }

                        if (permissionGroupID == 0) {
                            handler.invoke(false, asyncResultOfGetPermissionGroupIDFromUserID)
                        }

                        return@closeConnection
                    }

                    handler.invoke(true, asyncResultOfGetPermissionGroupIDFromUserID)
                }
            }
        }
    }

    fun logout(
        databaseManager: DatabaseManager,
        routingContext: RoutingContext,
        handler: (isLoggedOut: Result?, asyncResult: AsyncResult<*>?) -> Unit
    ) {
        val session = routingContext.session().get<String?>(SESSION_NAME)

        if (session != null) {
            routingContext.session().destroy()

            handler.invoke(Successful(), null)

            return
        }

        val cookie = routingContext.getCookie(COOKIE_NAME)

        if (cookie != null) {
            val token = cookie.value

            routingContext.removeCookie(COOKIE_NAME)

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
}