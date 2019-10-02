package com.panomc.platform.util

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
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

    @Inject
    lateinit var configManager: ConfigManager

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

    fun createConnection(resultHandler: (authResult: AuthResult) -> Unit, handler: () -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection != null) {
                this.connection = connection

                handler.invoke()
            } else
                resultHandler.invoke(Error(ErrorCode.INVALID_DATA))
        }
    }

    private fun isLoginSessionTokenExists(token: String, handler: (isExists: Boolean) -> Unit) {
        createConnection({
            if (it is Error)
                handler.invoke(false)
        }) {
            val query =
                "SELECT COUNT(id) FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token where token = ?"

            getConnection().queryWithParams(query, JsonArray().add(token)) { queryResult ->
                if (!queryResult.succeeded())
                    closeConnection {
                        handler.invoke(false)
                    }
                else if (queryResult.result().results[0].getInteger(0) == 1)
                    handler.invoke(true)
                else
                    closeConnection {
                        handler.invoke(false)
                    }
            }
        }
    }

    private fun getUserIDFromToken(
        token: String,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT user_id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}token where token = ?"

        getConnection().queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                closeConnection {
                    handler.invoke(0)
                }
        }
    }

    private fun getPermissionIDFromUserID(
        userID: Int,
        handler: (permissionID: Int) -> Unit
    ) {
        val query =
            "SELECT permission_id FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}user where id = ?"

        getConnection().queryWithParams(query, JsonArray().add(userID)) { queryResult ->

            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                closeConnection {
                    handler.invoke(-1)
                }
        }
    }

    private fun getPermissionFromPermissionID(
        permissionID: Int,
        handler: (permission: String?) -> Unit
    ) {
        val query =
            "SELECT name FROM ${(configManager.config["database"] as Map<*, *>)["prefix"].toString()}permission where id = ?"

        getConnection().queryWithParams(query, JsonArray().add(permissionID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0))
            else
                closeConnection {
                    handler.invoke(null)
                }
        }
    }

    fun isLoggedIn(context: RoutingContext, handler: (isLoggedIn: Boolean) -> Unit) {
        val token = context.getCookie("pano_token").value

        isLoginSessionTokenExists(token) {
            if (it)
                handler.invoke(true)
            else
                handler.invoke(false)
        }
    }

    fun isAdmin(context: RoutingContext, handler: (isAdmin: Boolean) -> Unit) {
        isLoggedIn(context) { isLoggedIn ->
            if (isLoggedIn) {
                val token = context.getCookie("pano_token").value

                getUserIDFromToken(token) { userID ->
                    if (userID == 0)
                        handler.invoke(false)
                    else
                        getPermissionIDFromUserID(userID) { permissionID ->
                            if (permissionID == -1)
                                handler.invoke(false)
                            else
                                getPermissionFromPermissionID(permissionID) { permission ->
                                    closeConnection {
                                        when {
                                            permission.isNullOrEmpty() -> handler.invoke(false)
                                            permission == "admin" -> handler.invoke(true)
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

    abstract class AuthResult

    class Error(val errorCode: ErrorCode) : AuthResult()

    class Successful : AuthResult()
}