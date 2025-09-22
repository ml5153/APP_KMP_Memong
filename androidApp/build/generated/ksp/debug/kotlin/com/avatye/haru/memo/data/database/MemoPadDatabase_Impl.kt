package com.avatye.haru.memo.`data`.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.avatye.haru.memo.`data`.dao.MemoDao
import com.avatye.haru.memo.`data`.dao.MemoDao_Impl
import com.avatye.haru.memo.`data`.dao.TagDao
import com.avatye.haru.memo.`data`.dao.TagDao_Impl
import com.avatye.haru.memo.`data`.dao.TaggingDao
import com.avatye.haru.memo.`data`.dao.TaggingDao_Impl
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MemoPadDatabase_Impl : MemoPadDatabase() {
  private val _memoDao: Lazy<MemoDao> = lazy {
    MemoDao_Impl(this)
  }

  private val _taggingDao: Lazy<TaggingDao> = lazy {
    TaggingDao_Impl(this)
  }

  private val _tagDao: Lazy<TagDao> = lazy {
    TagDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "76779e66edbe73d6fd10d5d21f8be457", "e8a960553d9618ccdc3438d6123dbcc9") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `memo` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userid` TEXT NOT NULL, `uuid` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `created` INTEGER NOT NULL, `modified` INTEGER NOT NULL, `read` INTEGER NOT NULL, `custom` INTEGER NOT NULL, `imagePath` TEXT NOT NULL, `isLocked` INTEGER NOT NULL, `isImportant` INTEGER NOT NULL, `bgColor` TEXT NOT NULL, `category` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL, `deleted` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tagging` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `memoid` INTEGER NOT NULL, `tagid` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tags` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tagName` TEXT NOT NULL, `created` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '76779e66edbe73d6fd10d5d21f8be457')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `memo`")
        connection.execSQL("DROP TABLE IF EXISTS `tagging`")
        connection.execSQL("DROP TABLE IF EXISTS `tags`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsMemo: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMemo.put("_id", TableInfo.Column("_id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("userid", TableInfo.Column("userid", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("uuid", TableInfo.Column("uuid", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("body", TableInfo.Column("body", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("created", TableInfo.Column("created", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("modified", TableInfo.Column("modified", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("read", TableInfo.Column("read", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("custom", TableInfo.Column("custom", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("imagePath", TableInfo.Column("imagePath", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("isLocked", TableInfo.Column("isLocked", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("isImportant", TableInfo.Column("isImportant", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("bgColor", TableInfo.Column("bgColor", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("category", TableInfo.Column("category", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("isDeleted", TableInfo.Column("isDeleted", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemo.put("deleted", TableInfo.Column("deleted", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMemo: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMemo: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMemo: TableInfo = TableInfo("memo", _columnsMemo, _foreignKeysMemo, _indicesMemo)
        val _existingMemo: TableInfo = read(connection, "memo")
        if (!_infoMemo.equals(_existingMemo)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |memo(com.avatye.haru.memo.data.entity.MemoEntity).
              | Expected:
              |""".trimMargin() + _infoMemo + """
              |
              | Found:
              |""".trimMargin() + _existingMemo)
        }
        val _columnsTagging: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTagging.put("_id", TableInfo.Column("_id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTagging.put("memoid", TableInfo.Column("memoid", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTagging.put("tagid", TableInfo.Column("tagid", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTagging: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTagging: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTagging: TableInfo = TableInfo("tagging", _columnsTagging, _foreignKeysTagging,
            _indicesTagging)
        val _existingTagging: TableInfo = read(connection, "tagging")
        if (!_infoTagging.equals(_existingTagging)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tagging(com.avatye.haru.memo.data.entity.TaggingEntity).
              | Expected:
              |""".trimMargin() + _infoTagging + """
              |
              | Found:
              |""".trimMargin() + _existingTagging)
        }
        val _columnsTags: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTags.put("_id", TableInfo.Column("_id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTags.put("tagName", TableInfo.Column("tagName", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsTags.put("created", TableInfo.Column("created", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTags: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTags: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoTags: TableInfo = TableInfo("tags", _columnsTags, _foreignKeysTags, _indicesTags)
        val _existingTags: TableInfo = read(connection, "tags")
        if (!_infoTags.equals(_existingTags)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |tags(com.avatye.haru.memo.data.entity.TagEntity).
              | Expected:
              |""".trimMargin() + _infoTags + """
              |
              | Found:
              |""".trimMargin() + _existingTags)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "memo", "tagging", "tags")
  }

  public override fun clearAllTables() {
    super.performClear(false, "memo", "tagging", "tags")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(MemoDao::class, MemoDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TaggingDao::class, TaggingDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(TagDao::class, TagDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun memoDao(): MemoDao = _memoDao.value

  public override fun taggingDao(): TaggingDao = _taggingDao.value

  public override fun tagDao(): TagDao = _tagDao.value
}
