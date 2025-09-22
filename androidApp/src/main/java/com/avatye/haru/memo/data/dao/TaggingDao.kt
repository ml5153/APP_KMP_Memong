package com.avatye.haru.memo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.avatye.haru.memo.data.entity.TaggingEntity

@Dao
interface TaggingDao {

    @Query("SELECT * FROM tagging")
    suspend fun getAllTaggings(): List<TaggingEntity>

    @Query("SELECT * FROM tagging WHERE memoid = :memoId")
    suspend fun getTagsByMemoId(memoId: Int): List<TaggingEntity>

    @Query("SELECT * FROM tagging WHERE tagid = :tagId")
    suspend fun getMemosByTagId(tagId: Int): List<TaggingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTagging(tagging: TaggingEntity)

    @Delete
    suspend fun deleteTagging(tagging: TaggingEntity)

    @Query("DELETE FROM tagging WHERE memoid = :memoId")
    suspend fun deleteAllTagsForMemo(memoId: Int)

    @Query("DELETE FROM tagging")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM tagging WHERE tagid = :tagId")
    suspend fun countTaggingsForTag(tagId: Int): Int

    // tagging 테이블에 있는 memoid 중 memo 테이블에 없는 것 제거
    @Query("""
    DELETE FROM tagging
    WHERE memoid NOT IN (SELECT _id FROM memo)
""")
    suspend fun purgeDanglingTaggings()
}
