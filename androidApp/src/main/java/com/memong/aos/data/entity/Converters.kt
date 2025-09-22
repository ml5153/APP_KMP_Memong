package com.memong.aos.data.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromBodyList(value: List<BodyItem>): String = gson.toJson(value)

    @TypeConverter
    fun toBodyList(value: String): List<BodyItem> =
        gson.fromJson(value, object : TypeToken<List<BodyItem>>() {}.type)

    @TypeConverter
    fun fromImagePathMap(value: Map<Int, List<String>>): String = gson.toJson(value)

    @TypeConverter
    fun toImagePathMap(value: String): Map<Int, List<String>> =
        gson.fromJson(value, object : TypeToken<Map<Int, List<String>>>() {}.type)
}
