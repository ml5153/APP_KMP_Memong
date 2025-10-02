package com.memong.aos.`data`.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.memong.aos.`data`.entity.BodyItem
import com.memong.aos.`data`.entity.Converters
import com.memong.aos.`data`.entity.MemoEntity
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MemoDao_Impl(
  __db: RoomDatabase,
) : MemoDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMemoEntity: EntityInsertAdapter<MemoEntity>

  private val __converters: Converters = Converters()

  private val __insertAdapterOfMemoEntity_1: EntityInsertAdapter<MemoEntity>

  private val __deleteAdapterOfMemoEntity: EntityDeleteOrUpdateAdapter<MemoEntity>

  private val __updateAdapterOfMemoEntity: EntityDeleteOrUpdateAdapter<MemoEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMemoEntity = object : EntityInsertAdapter<MemoEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `memo` (`_id`,`userid`,`uuid`,`title`,`body`,`created`,`modified`,`read`,`custom`,`imagePath`,`isLocked`,`isImportant`,`bgColor`,`category`,`isDeleted`,`deleted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MemoEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindText(2, entity.userid)
        statement.bindText(3, entity.uuid)
        statement.bindText(4, entity.title)
        val _tmp: String = __converters.fromBodyList(entity.body)
        statement.bindText(5, _tmp)
        statement.bindLong(6, entity.created)
        statement.bindLong(7, entity.modified)
        statement.bindLong(8, entity.read)
        statement.bindLong(9, entity.custom)
        val _tmp_1: String = __converters.fromImagePathMap(entity.imagePath)
        statement.bindText(10, _tmp_1)
        val _tmp_2: Int = if (entity.isLocked) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.isImportant) 1 else 0
        statement.bindLong(12, _tmp_3.toLong())
        statement.bindText(13, entity.bgColor)
        statement.bindLong(14, entity.category.toLong())
        val _tmp_4: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
        statement.bindLong(16, entity.deleted)
      }
    }
    this.__insertAdapterOfMemoEntity_1 = object : EntityInsertAdapter<MemoEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `memo` (`_id`,`userid`,`uuid`,`title`,`body`,`created`,`modified`,`read`,`custom`,`imagePath`,`isLocked`,`isImportant`,`bgColor`,`category`,`isDeleted`,`deleted`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MemoEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindText(2, entity.userid)
        statement.bindText(3, entity.uuid)
        statement.bindText(4, entity.title)
        val _tmp: String = __converters.fromBodyList(entity.body)
        statement.bindText(5, _tmp)
        statement.bindLong(6, entity.created)
        statement.bindLong(7, entity.modified)
        statement.bindLong(8, entity.read)
        statement.bindLong(9, entity.custom)
        val _tmp_1: String = __converters.fromImagePathMap(entity.imagePath)
        statement.bindText(10, _tmp_1)
        val _tmp_2: Int = if (entity.isLocked) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.isImportant) 1 else 0
        statement.bindLong(12, _tmp_3.toLong())
        statement.bindText(13, entity.bgColor)
        statement.bindLong(14, entity.category.toLong())
        val _tmp_4: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
        statement.bindLong(16, entity.deleted)
      }
    }
    this.__deleteAdapterOfMemoEntity = object : EntityDeleteOrUpdateAdapter<MemoEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `memo` WHERE `_id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MemoEntity) {
        statement.bindLong(1, entity._id.toLong())
      }
    }
    this.__updateAdapterOfMemoEntity = object : EntityDeleteOrUpdateAdapter<MemoEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `memo` SET `_id` = ?,`userid` = ?,`uuid` = ?,`title` = ?,`body` = ?,`created` = ?,`modified` = ?,`read` = ?,`custom` = ?,`imagePath` = ?,`isLocked` = ?,`isImportant` = ?,`bgColor` = ?,`category` = ?,`isDeleted` = ?,`deleted` = ? WHERE `_id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MemoEntity) {
        statement.bindLong(1, entity._id.toLong())
        statement.bindText(2, entity.userid)
        statement.bindText(3, entity.uuid)
        statement.bindText(4, entity.title)
        val _tmp: String = __converters.fromBodyList(entity.body)
        statement.bindText(5, _tmp)
        statement.bindLong(6, entity.created)
        statement.bindLong(7, entity.modified)
        statement.bindLong(8, entity.read)
        statement.bindLong(9, entity.custom)
        val _tmp_1: String = __converters.fromImagePathMap(entity.imagePath)
        statement.bindText(10, _tmp_1)
        val _tmp_2: Int = if (entity.isLocked) 1 else 0
        statement.bindLong(11, _tmp_2.toLong())
        val _tmp_3: Int = if (entity.isImportant) 1 else 0
        statement.bindLong(12, _tmp_3.toLong())
        statement.bindText(13, entity.bgColor)
        statement.bindLong(14, entity.category.toLong())
        val _tmp_4: Int = if (entity.isDeleted) 1 else 0
        statement.bindLong(15, _tmp_4.toLong())
        statement.bindLong(16, entity.deleted)
        statement.bindLong(17, entity._id.toLong())
      }
    }
  }

  public override suspend fun insertMemo(memo: MemoEntity): Long = performSuspending(__db, false,
      true) { _connection ->
    val _result: Long = __insertAdapterOfMemoEntity.insertAndReturnId(_connection, memo)
    _result
  }

  public override suspend fun insertAll(memos: List<MemoEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfMemoEntity.insert(_connection, memos)
  }

  public override suspend fun insert(memo: MemoEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __insertAdapterOfMemoEntity_1.insert(_connection, memo)
  }

  public override suspend fun deleteMemo(memo: MemoEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __deleteAdapterOfMemoEntity.handle(_connection, memo)
  }

  public override suspend fun updateMemo(memo: MemoEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfMemoEntity.handle(_connection, memo)
  }

  public override suspend fun update(memo: MemoEntity): Unit = performSuspending(__db, false, true)
      { _connection ->
    __updateAdapterOfMemoEntity.handle(_connection, memo)
  }

  public override suspend fun restoreMemosWithoutTags(uuids: List<String>, now: Long): Unit =
      performInTransactionSuspending(__db) {
    super@MemoDao_Impl.restoreMemosWithoutTags(uuids, now)
  }

  public override suspend fun getAllMemos(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllNoConditionMemos(): List<MemoEntity> {
    val _sql: String = "SELECT * FROM memo WHERE isDeleted = 0 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMemosCreatedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY created ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMemosModifiedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY modified DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMemosModifiedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY modified ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMemosReadDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY read DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllMemosByCustomDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 0 AND isLocked = 0 ORDER BY custom DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllUnLockMemos(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 0 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByCreatedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByCreatedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY created ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByModifiedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY modified DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByModifiedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY modified ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByReadDesc(): List<MemoEntity> {
    val _sql: String = "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY read DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTextMemosByCustomDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 1 ORDER BY custom DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByCreatedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByCreatedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY created ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByModifiedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY modified DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByModifiedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY modified ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByReadDesc(): List<MemoEntity> {
    val _sql: String = "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY read DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImageMemosByCustomDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND category = 2 ORDER BY custom DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantLockScreenMemosByCreatedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByCreatedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByCreatedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY created ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByModifiedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY modified DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByModifiedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY modified ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByReadDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY read DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getImportantMemosByCustomDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isImportant = 1 AND isLocked = 0 ORDER BY custom DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByCreatedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY created DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByCreatedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY created ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByModifiedDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY modified DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByModifiedAsc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY modified ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByReadDesc(): List<MemoEntity> {
    val _sql: String = "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY read DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLockMemosByCustomDesc(): List<MemoEntity> {
    val _sql: String =
        "SELECT * FROM memo WHERE isDeleted = 0 AND isLocked = 1 ORDER BY custom DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getDeleteMemos(): List<MemoEntity> {
    val _sql: String = "SELECT * FROM memo WHERE isDeleted = 1 ORDER BY deleted DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMemoIdsByUuids(uuids: List<String>): List<Int> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT _id FROM memo WHERE uuid IN (")
    val _inputSize: Int = uuids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in uuids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _result: MutableList<Int> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: Int
          _item_1 = _stmt.getLong(0).toInt()
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMemoByUUID(uuid: String): MemoEntity? {
    val _sql: String = "SELECT * FROM memo WHERE uuid = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MemoEntity?
        if (_stmt.step()) {
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _result =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMemoById(id: Int): MemoEntity? {
    val _sql: String = "SELECT * FROM memo WHERE _id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MemoEntity?
        if (_stmt.step()) {
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _result =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAllMemos(): Flow<List<MemoEntity>> {
    val _sql: String = "SELECT * FROM memo ORDER BY created DESC"
    return createFlow(__db, false, arrayOf("memo")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "_id")
        val _columnIndexOfUserid: Int = getColumnIndexOrThrow(_stmt, "userid")
        val _columnIndexOfUuid: Int = getColumnIndexOrThrow(_stmt, "uuid")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfBody: Int = getColumnIndexOrThrow(_stmt, "body")
        val _columnIndexOfCreated: Int = getColumnIndexOrThrow(_stmt, "created")
        val _columnIndexOfModified: Int = getColumnIndexOrThrow(_stmt, "modified")
        val _columnIndexOfRead: Int = getColumnIndexOrThrow(_stmt, "read")
        val _columnIndexOfCustom: Int = getColumnIndexOrThrow(_stmt, "custom")
        val _columnIndexOfImagePath: Int = getColumnIndexOrThrow(_stmt, "imagePath")
        val _columnIndexOfIsLocked: Int = getColumnIndexOrThrow(_stmt, "isLocked")
        val _columnIndexOfIsImportant: Int = getColumnIndexOrThrow(_stmt, "isImportant")
        val _columnIndexOfBgColor: Int = getColumnIndexOrThrow(_stmt, "bgColor")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfIsDeleted: Int = getColumnIndexOrThrow(_stmt, "isDeleted")
        val _columnIndexOfDeleted: Int = getColumnIndexOrThrow(_stmt, "deleted")
        val _result: MutableList<MemoEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoEntity
          val _tmp_id: Int
          _tmp_id = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserid: String
          _tmpUserid = _stmt.getText(_columnIndexOfUserid)
          val _tmpUuid: String
          _tmpUuid = _stmt.getText(_columnIndexOfUuid)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpBody: List<BodyItem>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBody)
          _tmpBody = __converters.toBodyList(_tmp)
          val _tmpCreated: Long
          _tmpCreated = _stmt.getLong(_columnIndexOfCreated)
          val _tmpModified: Long
          _tmpModified = _stmt.getLong(_columnIndexOfModified)
          val _tmpRead: Long
          _tmpRead = _stmt.getLong(_columnIndexOfRead)
          val _tmpCustom: Long
          _tmpCustom = _stmt.getLong(_columnIndexOfCustom)
          val _tmpImagePath: Map<Int, List<String>>
          val _tmp_1: String
          _tmp_1 = _stmt.getText(_columnIndexOfImagePath)
          _tmpImagePath = __converters.toImagePathMap(_tmp_1)
          val _tmpIsLocked: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsLocked).toInt()
          _tmpIsLocked = _tmp_2 != 0
          val _tmpIsImportant: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfIsImportant).toInt()
          _tmpIsImportant = _tmp_3 != 0
          val _tmpBgColor: String
          _tmpBgColor = _stmt.getText(_columnIndexOfBgColor)
          val _tmpCategory: Int
          _tmpCategory = _stmt.getLong(_columnIndexOfCategory).toInt()
          val _tmpIsDeleted: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfIsDeleted).toInt()
          _tmpIsDeleted = _tmp_4 != 0
          val _tmpDeleted: Long
          _tmpDeleted = _stmt.getLong(_columnIndexOfDeleted)
          _item =
              MemoEntity(_tmp_id,_tmpUserid,_tmpUuid,_tmpTitle,_tmpBody,_tmpCreated,_tmpModified,_tmpRead,_tmpCustom,_tmpImagePath,_tmpIsLocked,_tmpIsImportant,_tmpBgColor,_tmpCategory,_tmpIsDeleted,_tmpDeleted)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getIdByUUID(uuid: String): Int? {
    val _sql: String = "SELECT _id FROM memo WHERE uuid = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, uuid)
        val _result: Int?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getLong(0).toInt()
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteExpiredTrash(cutoff: Long) {
    val _sql: String = "DELETE FROM memo WHERE isDeleted = 1 AND deleted <= ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, cutoff)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun restoreMemos(uuids: List<String>, now: Long) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("    UPDATE memo ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("    SET ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        isDeleted = 0, ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        created = ")
    _stringBuilder.append("?")
    _stringBuilder.append(", ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        modified = ")
    _stringBuilder.append("?")
    _stringBuilder.append(", ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        read = ")
    _stringBuilder.append("?")
    _stringBuilder.append(", ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        custom = ")
    _stringBuilder.append("?")
    _stringBuilder.append(", ")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        deleted = 0,")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("        isLocked = 0")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    _stringBuilder.append("    WHERE uuid IN (")
    val _inputSize: Int = uuids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    _stringBuilder.append("""
        |
        |""".trimMargin())
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, now)
        _argIndex = 2
        _stmt.bindLong(_argIndex, now)
        _argIndex = 3
        _stmt.bindLong(_argIndex, now)
        _argIndex = 4
        _stmt.bindLong(_argIndex, now)
        _argIndex = 5
        for (_item: String in uuids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTaggingsByMemoIds(memoIds: List<Int>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM tagging WHERE memoid IN (")
    val _inputSize: Int = memoIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: Int in memoIds) {
          _stmt.bindLong(_argIndex, _item.toLong())
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMemosPermanently(uuids: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM memo WHERE uuid IN (")
    val _inputSize: Int = uuids.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in uuids) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun purgeDeletedMemos() {
    val _sql: String = "DELETE FROM memo WHERE isDeleted = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun unlockAllLockedMemos() {
    val _sql: String = "UPDATE memo SET isLocked = 0 WHERE isLocked = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM memo"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateAllLockState(isLocked: Boolean) {
    val _sql: String = "UPDATE memo SET isLocked = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (isLocked) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun updateImagePath(uuid: String, newPath: Map<Int, List<String>>) {
    val _sql: String = "UPDATE memo SET imagePath = ? WHERE uuid = ?"
    return performBlocking(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: String = __converters.fromImagePathMap(newPath)
        _stmt.bindText(_argIndex, _tmp)
        _argIndex = 2
        _stmt.bindText(_argIndex, uuid)
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
