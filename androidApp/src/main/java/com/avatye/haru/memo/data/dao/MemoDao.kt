package com.avatye.haru.memo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.avatye.haru.memo.data.entity.MemoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    // 전체보기
    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY created DESC")
    suspend fun getAllMemos(): List<MemoEntity>
    @Query("SELECT * FROM memo WHERE isDeleted = 0 ORDER BY created DESC")
    suspend fun getAllNoConditionMemos(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY created ASC")
    suspend fun getAllMemosCreatedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY modified DESC")
    suspend fun getAllMemosModifiedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY modified ASC")
    suspend fun getAllMemosModifiedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY read DESC")
    suspend fun getAllMemosReadDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY custom DESC")
    suspend fun getAllMemosByCustomDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 0 ORDER BY created DESC")
    suspend fun getAllUnLockMemos(): List<MemoEntity>

    // 텍스트메모
    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY created DESC")
    suspend fun getTextMemosByCreatedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY created ASC")
    suspend fun getTextMemosByCreatedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY modified DESC")
    suspend fun getTextMemosByModifiedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY modified ASC")
    suspend fun getTextMemosByModifiedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY read DESC")
    suspend fun getTextMemosByReadDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY custom DESC")
    suspend fun getTextMemosByCustomDesc(): List<MemoEntity>

    // 이미지메모
    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY created DESC")
    suspend fun getImageMemosByCreatedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY created ASC")
    suspend fun getImageMemosByCreatedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY modified DESC")
    suspend fun getImageMemosByModifiedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY modified ASC")
    suspend fun getImageMemosByModifiedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY read DESC")
    suspend fun getImageMemosByReadDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY custom DESC")
    suspend fun getImageMemosByCustomDesc(): List<MemoEntity>

    // 즐겨찾기메모
    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created DESC")
    suspend fun getImportantLockScreenMemosByCreatedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created DESC")
    suspend fun getImportantMemosByCreatedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created ASC")
    suspend fun getImportantMemosByCreatedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY modified DESC")
    suspend fun getImportantMemosByModifiedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY modified ASC")
    suspend fun getImportantMemosByModifiedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY read DESC")
    suspend fun getImportantMemosByReadDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY custom DESC")
    suspend fun getImportantMemosByCustomDesc(): List<MemoEntity>

    // 비밀메모
    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY created DESC")
    suspend fun getLockMemosByCreatedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY created ASC")
    suspend fun getLockMemosByCreatedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY modified DESC")
    suspend fun getLockMemosByModifiedDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY modified ASC")
    suspend fun getLockMemosByModifiedAsc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY read DESC")
    suspend fun getLockMemosByReadDesc(): List<MemoEntity>

    @Query("SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY custom DESC")
    suspend fun getLockMemosByCustomDesc(): List<MemoEntity>


    // 삭제
    @Query("SELECT * FROM memo WHERE isDeleted = 1 ORDER BY deleted DESC")
    suspend fun getDeleteMemos(): List<MemoEntity>

    @Query("DELETE FROM memo WHERE isDeleted = 1 AND deleted <= :cutoff")
    suspend fun deleteExpiredTrash(cutoff: Long)

    // 태그 연결 유지 복원 트랜잭션 <- 현재 쓰임
    @Query(
        """
    UPDATE memo 
    SET 
        isDeleted = 0, 
        created = :now, 
        modified = :now, 
        read = :now, 
        custom = :now, 
        deleted = 0,
        isLocked = 0
    WHERE uuid IN (:uuids)
"""
    )
    suspend fun restoreMemos(uuids: List<String>, now: Long)

    // 태그 연결 제거 복원 트랜잭션 <- 현재 쓰이지 않음
    @Transaction
    suspend fun restoreMemosWithoutTags(uuids: List<String>, now: Long) {
        restoreMemos(uuids, now)
        val memoIds = getMemoIdsByUuids(uuids)
        if (memoIds.isNotEmpty()) {
            deleteTaggingsByMemoIds(memoIds)
        }
    }

    // 2. uuid → memo _id 매핑
    @Query("SELECT _id FROM memo WHERE uuid IN (:uuids)")
    suspend fun getMemoIdsByUuids(uuids: List<String>): List<Int>

    // 3. 태그 연결 제거
    @Query("DELETE FROM tagging WHERE memoid IN (:memoIds)")
    suspend fun deleteTaggingsByMemoIds(memoIds: List<Int>)

    @Query("DELETE FROM memo WHERE uuid IN (:uuids)")
    suspend fun deleteMemosPermanently(uuids: List<String>)


    @Query("SELECT * FROM memo WHERE uuid = :uuid LIMIT 1")
    suspend fun getMemoByUUID(uuid: String): MemoEntity?

    @Query("SELECT * FROM memo WHERE _id = :id LIMIT 1")
    suspend fun getMemoById(id: Int): MemoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemo(memo: MemoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memos: List<MemoEntity>)

    @Update
    suspend fun updateMemo(memo: MemoEntity)

    @Delete
    suspend fun deleteMemo(memo: MemoEntity)

    @Query("DELETE FROM memo WHERE isDeleted = 1")
    suspend fun purgeDeletedMemos()

    @Query("SELECT * FROM memo ORDER BY created DESC")
    fun observeAllMemos(): Flow<List<MemoEntity>>


    @Query("UPDATE memo SET isLocked = 0 WHERE isLocked = 1")
    suspend fun unlockAllLockedMemos()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(memo: MemoEntity)

    @Update
    suspend fun update(memo: MemoEntity)

    @Query("DELETE FROM memo")
    suspend fun deleteAll()

    @Query("UPDATE memo SET isLocked = :isLocked")
    suspend fun updateAllLockState(isLocked: Boolean)

    @Query("UPDATE memo SET imagePath = :newPath WHERE uuid = :uuid")
    fun updateImagePath(uuid: String, newPath: Map<Int, List<String>>)

    @Query("SELECT _id FROM memo WHERE uuid = :uuid")
    suspend fun getIdByUUID(uuid: String): Int?
}

