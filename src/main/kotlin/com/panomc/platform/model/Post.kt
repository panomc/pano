package com.panomc.platform.model

data class Post(
    val id: Int,
    val title: String,
    val categoryId: Int,
    val writerUserID: Int,
    val post: String,
    val imageCode: String,
)