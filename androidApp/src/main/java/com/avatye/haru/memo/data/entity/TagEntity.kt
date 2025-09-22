package com.avatye.haru.memo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val tagName: String,
    val created: Long
)
