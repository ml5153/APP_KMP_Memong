package com.avatye.haru.memo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.avatye.haru.memo.data.entity.TagEntity

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY created DESC")
    suspend fun getAllTags(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE _id = :tagId LIMIT 1")
    suspend fun getTagById(tagId: String): TagEntity?

    @Query("SELECT * FROM tags WHERE tagName = :tagName LIMIT 1")
    suspend fun getTagByName(tagName: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Delete
    suspend fun deleteTags(tags: List<TagEntity>)

    @Query("DELETE FROM tags WHERE _id = :tagId")
    suspend fun deleteQueryTag(tagId: Int)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Query("""
    SELECT * FROM tags 
    WHERE _id IN (
        SELECT tagid FROM tagging WHERE memoid = :memoId
    )
""")
    suspend fun getTagsForMemo(memoId: Int): List<TagEntity>

    @Query("""
    SELECT * FROM tags
    WHERE EXISTS (
        SELECT 1 FROM tagging
        INNER JOIN memo ON memo._id = tagging.memoid
        WHERE tagging.tagid = tags._id
        AND memo.isDeleted = 0
    )
    ORDER BY created DESC
""")
    suspend fun getAllActiveTags(): List<TagEntity>

    @Query("""
    DELETE FROM tags
    WHERE _id NOT IN (
        SELECT DISTINCT tagid FROM tagging
    )
""")
    suspend fun purgeDanglingTags()
}

