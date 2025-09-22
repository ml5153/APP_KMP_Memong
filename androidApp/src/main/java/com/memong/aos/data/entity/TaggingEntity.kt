package com.memong.aos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tagging")
data class TaggingEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val memoid: Int,
    val tagid: Int
)
