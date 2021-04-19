package com.panomc.platform.db.model

data class Token(val id: Int, val token: String, val createdTime: Long, val userID: Int, val subject: String)