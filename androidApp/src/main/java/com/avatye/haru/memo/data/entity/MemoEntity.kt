package com.avatye.haru.memo.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "memo")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val _id: Int = 0,
    val userid: String,
    val uuid: String,
    val title: String,
    val body: List<BodyItem>,
    val created: Long,
    val modified: Long,
    val read: Long,
    val custom: Long,
    val imagePath: Map<Int, List<String>>,
    val isLocked: Boolean,
    val isImportant: Boolean,
    val bgColor: String,
    val category: Int,
    val isDeleted: Boolean,
    val deleted: Long
) : Parcelable {
    companion object {
        fun empty(): MemoEntity {
            return MemoEntity(
                userid = "",
                uuid = "",
                title = "",
                body = listOf(),
                created = 0,
                modified = 0,
                read = 0,
                custom = 0,
                imagePath = mapOf(),
                isLocked = false,
                isImportant = false,
                bgColor = "#FFFFFF",
                category = 0,
                isDeleted = false,
                deleted = 0
            )
        }
    }
}
