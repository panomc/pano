package com.panomc.platform.util

import com.panomc.platform.db.DatabaseManager
import io.vertx.ext.web.RoutingContext

class ServerAuthProvider(
    private val databaseManager: DatabaseManager,
    private val tokenProvider: TokenProvider,
) {
    companion object {
        const val HEADER_PREFIX = "Bearer "
    }

    suspend fun isAuthenticated(
        context: RoutingContext
    ): Boolean {
        val token = getTokenFromRoutingContext(context) ?: return false

        val sqlConnection = databaseManager.createConnection()

        val isTokenValid = tokenProvider.isTokenValid(token, TokenType.SERVER_AUTHENTICATION, sqlConnection)

        databaseManager.closeConnection(sqlConnection)

        return isTokenValid
    }

    fun getServerIdFromRoutingContext(context: RoutingContext): Long {
        val token = getTokenFromRoutingContext(context)

        return getServerIdFromToken(token!!)
    }

    fun getServerIdFromToken(token: String): Long {
        val jwt = tokenProvider.parseToken(token)

        return jwt.body.subject.toLong()
    }

    fun getTokenFromRoutingContext(context: RoutingContext): String? {
        val authorizationHeader = context.request().getHeader("Authorization") ?: return null

        if (!authorizationHeader.contains(HEADER_PREFIX)) {
            return null
        }

        val splitHeader = authorizationHeader.split(HEADER_PREFIX)

        if (splitHeader.size != 2) {
            return null
        }

        return try {
            val token = splitHeader.last()

            token
        } catch (exception: Exception) {
            null
        }
    }
}