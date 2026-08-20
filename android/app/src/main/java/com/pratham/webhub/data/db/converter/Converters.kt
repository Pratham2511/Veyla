package com.pratham.webhub.data.db.converter

import androidx.room.TypeConverter

/**
 * Room TypeConverters for the WebHub database.
 *
 * All entity fields currently use primitive types (String, Int, Boolean, Long),
 * so no custom converters are strictly required. This class is provided as a
 * registration point for any future type conversions (e.g., enums, JSON lists).
 */
class Converters {

    // Placeholder for future type converters.
    // Example:
    // @TypeConverter
    // fun fromStringList(value: List<String>?): String? =
    //     value?.joinToString(",")
    //
    // @TypeConverter
    // fun toStringList(value: String?): List<String>? =
    //     value?.split(",")
}
