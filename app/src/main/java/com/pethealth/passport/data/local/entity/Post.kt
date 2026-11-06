package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val remoteId: String? = null,
    val authorName: String,
    val petName: String = "",
    val speciesTag: String = "other",
    val content: String,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,
    val commentCount: Int = 0,
    val pendingSync: Boolean = false
)
