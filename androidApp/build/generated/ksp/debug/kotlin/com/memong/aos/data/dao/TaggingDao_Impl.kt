package com.memong.aos.`data`.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.memong.aos.`data`.entity.TaggingEntity
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TaggingDao_Impl(
  __db: RoomDatabase,
) : TaggingDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTaggingEntity: EntityInsertAdapter<TaggingEntity>

  private val __deleteAdapterOfTaggingEntity: EntityDeleteOrUpdateAdapter<TaggingEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTaggingEntity = object : EntityInsertAdapter<TaggingEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `tagging` (`_id`,`memoid`,`tagid`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TaggingEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindLong(2, entity.memoid.toLong())
        statement.bindLong(3, entity.tagid.toLong())
      }
    }
    this.__deleteAdapterOfTaggingEntity = object : EntityDeleteOrUpdateAdapter<TaggingEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `tagging` WHERE `_id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TaggingEntity) {
        statement.bindLong(1, entity._id.toLong())
      }
    }
  }

  public override suspend fun insertTagging(tagging: TaggingEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfTaggingEntity.insert(_connection, tagging)
  }

  public override suspend fun deleteTagging(tagging: TaggingEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __deleteAdapterOfTaggingEntity.handle(_connection, tagging)
  }

  public override suspend fun getAllTaggings(): List<TaggingEntity> {
    val _sql: String = "SELECT * FROM tagging"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfMemoid: Int = getColumnIndexOrThrow(_stmt, "memoid")
        val _columnIndexOfTagid: Int = getColumnIndexOrThrow(_stmt, "tagid")
        val _result: MutableList<TaggingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaggingEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMemoid: Int
          _tmpMemoid = _stmt.getLong(_columnIndexOfMemoid).toInt()
          val _tmpTagid: Int
          _tmpTagid = _stmt.getLong(_columnIndexOfTagid).toInt()
          _item = TaggingEntity(_tmp_id,_tmpMemoid,_tmpTagid)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTagsByMemoId(memoId: Int): List<TaggingEntity> {
    val _sql: String = "SELECT * FROM tagging WHERE memoid = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, memoId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfMemoid: Int = getColumnIndexOrThrow(_stmt, "memoid")
        val _columnIndexOfTagid: Int = getColumnIndexOrThrow(_stmt, "tagid")
        val _result: MutableList<TaggingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaggingEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMemoid: Int
          _tmpMemoid = _stmt.getLong(_columnIndexOfMemoid).toInt()
          val _tmpTagid: Int
          _tmpTagid = _stmt.getLong(_columnIndexOfTagid).toInt()
          _item = TaggingEntity(_tmp_id,_tmpMemoid,_tmpTagid)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMemosByTagId(tagId: Int): List<TaggingEntity> {
    val _sql: String = "SELECT * FROM tagging WHERE tagid = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, tagId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfMemoid: Int = getColumnIndexOrThrow(_stmt, "memoid")
        val _columnIndexOfTagid: Int = getColumnIndexOrThrow(_stmt, "tagid")
        val _result: MutableList<TaggingEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TaggingEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpMemoid: Int
          _tmpMemoid = _stmt.getLong(_columnIndexOfMemoid).toInt()
          val _tmpTagid: Int
          _tmpTagid = _stmt.getLong(_columnIndexOfTagid).toInt()
          _item = TaggingEntity(_tmp_id,_tmpMemoid,_tmpTagid)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countTaggingsForTag(tagId: Int): Int {
    val _sql: String = "SELECT COUNT(*) FROM tagging WHERE tagid = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, tagId.toLong())
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllTagsForMemo(memoId: Int) {
    val _sql: String = "DELETE FROM tagging WHERE memoid = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, memoId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM tagging"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun purgeDanglingTaggings() {
    val _sql: String = """
        |
        |    DELETE FROM tagging
        |    WHERE memoid NOT IN (SELECT _id FROM memo)
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
