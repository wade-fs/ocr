package com.wade.ocr.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { gson.fromJson(it, object : TypeToken<List<String>>() {}.type) }

    // Assuming PhoneEntry is a data class with fields (type, number)
    @TypeConverter
    fun fromPhoneList(value: List<com.wade.ocr.data.model.PhoneEntry>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toPhoneList(value: String?): List<com.wade.ocr.data.model.PhoneEntry>? =
        value?.let { gson.fromJson(it, object : TypeToken<List<com.wade.ocr.data.model.PhoneEntry>>() {}.type) }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? =
        value?.let { gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type) }
}
