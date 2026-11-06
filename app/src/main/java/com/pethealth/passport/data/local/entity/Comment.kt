package com.pethealth.passport.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("postId")]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val postId: Long,
    val parentId: Long? = null,
    val authorName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
