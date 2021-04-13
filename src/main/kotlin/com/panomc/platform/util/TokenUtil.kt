package com.panomc.platform.util

import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Token
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.sqlclient.SqlConnection
import java.util.*

object TokenUtil {
    enum class SUBJECT {
        LOGIN_SESSION
    }

    fun createToken(
        subject: SUBJECT,
        userID: Int,
        databaseManager: DatabaseManager,
        sqlConnection: SqlConnection,
        handler: (token: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.getSecretKeyByID(
            userID,
            sqlConnection
        ) { secretKey, asyncResultOfSecretKey ->
            if (secretKey == null) {
                handler.invoke(null, asyncResultOfSecretKey)

                return@getSecretKeyByID
            }

            val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

            val token = Jwts.builder()
                .setSubject(subject.toString())
                .setHeaderParam("key", Encoders.BASE64.encode(key.encoded))
                .signWith(
                    Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(
                            secretKey
                        )
                    )
                )
                .compact()

            databaseManager.getDatabase().tokenDao.add(
                Token(-1, token, userID, subject.toString()),
                sqlConnection
            ) { result, asyncResult ->
                if (result == null) {
                    handler.invoke(null, asyncResult)

                    return@add
                }

                handler.invoke(token, asyncResult)
            }
        }
    }
}