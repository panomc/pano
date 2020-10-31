package com.panomc.platform.util

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.db.Connection
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.Error
import com.panomc.platform.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

@Suppress("LeakingThis")
open class Auth {
//    @Inject
//    lateinit var recaptcha: ReCaptcha

//    @Inject
//    lateinit var secretKey: Future<Key>

    @Inject
    lateinit var databaseManager: DatabaseManager

    init {
        getComponent().inject(this)
    }

    var checkRecaptcha = true

    var ipAddress: String? = null

    lateinit var connection: Connection

    fun getConnection() = databaseManager.getSQLConnection(connection)

    fun closeConnection(handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        databaseManager.closeConnection(connection, handler)
    }

    fun createConnection(resultHandler: (authResult: Result) -> Unit, handler: () -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection != null) {
                this.connection = connection

                handler.invoke()
            } else
                resultHandler.invoke(Error(ErrorCode.INVALID_DATA))
        }
    }

    fun isLoggedIn(context: RoutingContext, handler: (isLoggedIn: Boolean) -> Unit) {
        val cookie = context.getCookie("pano_token")
        val token = if (cookie == null) "" else cookie.value

        if (token == "")
            handler.invoke(false)
        else
            createConnection({
                if (it is Error)
                    handler.invoke(false)
            }) {
                databaseManager.getDatabase().tokenDao.isTokenExists(token, getConnection()) { isTokenExists, _ ->
                    when {
                        isTokenExists == null -> closeConnection {
                            handler.invoke(false)
                        }
                        isTokenExists -> handler.invoke(true)
                        else -> closeConnection {
                            handler.invoke(false)
                        }
                    }
                }
            }
    }

    fun isAdmin(context: RoutingContext, handler: (isAdmin: Boolean) -> Unit) {
        isLoggedIn(context) { isLoggedIn ->
            if (isLoggedIn) {
                val token = context.getCookie("pano_token").value

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(token, getConnection()) { userID, _ ->
                    if (userID == null || userID == 0)
                        closeConnection {
                            handler.invoke(false)
                        }
                    else
                        databaseManager.getDatabase().userDao.getPermissionIDFromUserID(
                            userID,
                            getConnection()
                        ) { permissionID, _ ->
                            if (permissionID == null || permissionID == 0)
                                closeConnection {
                                    handler.invoke(false)
                                }
                            else
                                databaseManager.getDatabase().permissionDao.getPermissionByID(
                                    permissionID,
                                    getConnection()
                                ) { permission, _ ->
                                    closeConnection {
                                        when {
                                            permission == null -> handler.invoke(false)
                                            permission.name == "admin" -> handler.invoke(true)
                                            else -> handler.invoke(false)
                                        }
                                    }
                                }
                        }
                }
            } else
                handler.invoke(false)
        }
    }
}