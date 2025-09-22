package com.avatye.haru.memo.`data`.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.avatye.haru.memo.`data`.entity.TagEntity
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TagDao_Impl(
  __db: RoomDatabase,
) : TagDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTagEntity: EntityInsertAdapter<TagEntity>

  private val __deleteAdapterOfTagEntity: EntityDeleteOrUpdateAdapter<TagEntity>

  private val __updateAdapterOfTagEntity: EntityDeleteOrUpdateAdapter<TagEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTagEntity = object : EntityInsertAdapter<TagEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `tags` (`_id`,`tagName`,`created`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TagEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindText(2, entity.tagName)
        statement.bindLong(3, entity.created)
      }
    }
    this.__deleteAdapterOfTagEntity = object : EntityDeleteOrUpdateAdapter<TagEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `tags` WHERE `_id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TagEntity) {
        statement.bindLong(1, entity._id.toLong())
      }
    }
    this.__updateAdapterOfTagEntity = object : EntityDeleteOrUpdateAdapter<TagEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `tags` SET `_id` = ?,`tagName` = ?,`created` = ? WHERE `_id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TagEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindText(2, entity.tagName)
        statement.bindLong(3, entity.created)
        statement.bindLong(4, entity._id.toLong())
      }
    }
  }

  public override suspend fun insertTag(tag: TagEntity): Long = performSuspending(__db, false, true)
      { _connection ->
    val _result: Long = __insertAdapterOfTagEntity.insertAndReturnId(_connection, tag)
    _result
  }

  public override suspend fun insertTags(tags: List<TagEntity>): List<Long> =
      performSuspending(__db, false, true) { _connection ->
    val _result: List<Long> = __insertAdapterOfTagEntity.insertAndReturnIdsList(_connection, tags)
    _result
  }

  public override suspend fun deleteTag(tag: TagEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __deleteAdapterOfTagEntity.handle(_connection, tag)
  }

  public override suspend fun deleteTags(tags: List<TagEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfTagEntity.handleMultiple(_connection, tags)
  }

  public override suspend fun updateTag(tag: TagEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfTagEntity.handle(_connection, tag)
  }

  public override suspend fun getAllTags(): List<TagEntity> {
    val _sql: String = "SELECT * FROM tags ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfTagName: Int = getColumnIndexOrThrow(_stmt, "tagName")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _result: MutableList<TagEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TagEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTagName: String
          _tmpTagName = _stmt.getText(_columnIndexOfTagName)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          _item = TagEntity(_tmp_id,_tmpTagName,_tmpCreated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTagById(tagId: String): TagEntity? {
    val _sql: String = "SELECT * FROM tags WHERE _id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, tagId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfTagName: Int = getColumnIndexOrThrow(_stmt, "tagName")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _result: TagEntity?
        if (_stmt.step()) {
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTagName: String
          _tmpTagName = _stmt.getText(_columnIndexOfTagName)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          _result = TagEntity(_tmp_id,_tmpTagName,_tmpCreated)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTagByName(tagName: String): TagEntity? {
    val _sql: String = "SELECT * FROM tags WHERE tagName = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, tagName)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfTagName: Int = getColumnIndexOrThrow(_stmt, "tagName")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _result: TagEntity?
        if (_stmt.step()) {
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTagName: String
          _tmpTagName = _stmt.getText(_columnIndexOfTagName)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          _result = TagEntity(_tmp_id,_tmpTagName,_tmpCreated)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTagsForMemo(memoId: Int): List<TagEntity> {
    val _sql: String = """
        |
        |    SELECT * FROM tags 
        |    WHERE _id IN (
        |        SELECT tagid FROM tagging WHERE memoid = ?
        |    )
        |""".trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, memoId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfTagName: Int = getColumnIndexOrThrow(_stmt, "tagName")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _result: MutableList<TagEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TagEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTagName: String
          _tmpTagName = _stmt.getText(_columnIndexOfTagName)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          _item = TagEntity(_tmp_id,_tmpTagName,_tmpCreated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllActiveTags(): List<TagEntity> {
    val _sql: String = """
        |
        |    SELECT * FROM tags
        |    WHERE EXISTS (
        |        SELECT 1 FROM tagging
        |        INNER JOIN memo ON memo._id = tagging.memoid
        |        WHERE tagging.tagid = tags._id
        |        AND memo.isDeleted = 0
        |    )
        |    ORDER BY created DESC
        |""".trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfTagName: Int = getColumnIndexOrThrow(_stmt, "tagName")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _result: MutableList<TagEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TagEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpTagName: String
          _tmpTagName = _stmt.getText(_columnIndexOfTagName)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          _item = TagEntity(_tmp_id,_tmpTagName,_tmpCreated)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteQueryTag(tagId: Int) {
    val _sql: String = "DELETE FROM tags WHERE _id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, tagId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM tags"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun purgeDanglingTags() {
    val _sql: String = """
        |
        |    DELETE FROM tags
        |    WHERE _id NOT IN (
        |        SELECT DISTINCT tagid FROM tagging
        |    )
        |""".trimMargin()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
