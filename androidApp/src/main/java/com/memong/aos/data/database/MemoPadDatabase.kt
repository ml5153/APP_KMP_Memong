package com.memong.aos.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.memong.aos.data.dao.MemoDao
import com.memong.aos.data.dao.TagDao
import com.memong.aos.data.dao.TaggingDao
import com.memong.aos.data.entity.Converters
import com.memong.aos.data.entity.MemoEntity
import com.memong.aos.data.entity.TagEntity
import com.memong.aos.data.entity.TaggingEntity

object MemoDatabase {
    @Volatile
    private var INSTANCE: MemoPadDatabase? = null

    fun getInstance(context: Context): MemoPadDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                MemoPadDatabase::class.java,
                "memopad.db"
            ).build().also { INSTANCE = it }
        }
    }

    fun closeInstance() {
        INSTANCE?.close()
        INSTANCE = null
    }
}

@Database(
    entities = [MemoEntity::class, TaggingEntity::class, TagEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MemoPadDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun taggingDao(): TaggingDao
    abstract fun tagDao(): TagDao
}
