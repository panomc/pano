package com.panomc.platform.model

import com.panomc.platform.util.NotificationStatus

data class PanelNotification(
    val id: Int,
    val userID: Int,
    val typeID: String,
    val date: Long,
    val status: NotificationStatus
)