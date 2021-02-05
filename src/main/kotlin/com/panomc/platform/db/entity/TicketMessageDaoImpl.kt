package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.TicketMessageDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class TicketMessageDaoImpl(override val tableName: String = "ticket_message") : DaoImpl(), TicketMessageDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,
              `ticket_id` int NOT NULL,
              `message` text NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Ticket message table.';
        """
            ) {
                handler.invoke(it)
            }
        }
}