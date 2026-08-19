# ============================================
# WebHub ProGuard / R8 Rules
# ============================================

# --------------------------------------------
# Room
# --------------------------------------------
# Keep all Room entities (annotated with @Entity)
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Keep DAO interfaces
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

# Keep database classes
-keep @androidx.room.Database class * { *; }

# Keep TypeConverters
-keep @androidx.room.TypeConverter class * { *; }

# Room uses reflection for entities
-keepclassmembers class * {
    @androidx.room.Entity <fields>;
}

# --------------------------------------------
# Hilt / Dagger
# --------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Hilt-generated code
-keepclassmembers class * {
    @dagger.hilt.android.* *;
    @javax.inject.* *;
}

# Keep entry point accessors
-keep @dagger.hilt.EntryPoint class * { *; }

# --------------------------------------------
# Kotlin Coroutines
# --------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --------------------------------------------
# Gson / JSON
# --------------------------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter { *; }
-keep class * implements com.google.gson.TypeAdapterFactory { *; }
-keep class * implements com.google.gson.JsonSerializer { *; }
-keep class * implements com.google.gson.JsonDeserializer { *; }

# Keep any model classes used with Gson (if applicable)
-keep class com.pratham.webhub.domain.model.** { *; }
-keep class com.pratham.webhub.data.db.entity.** { *; }

# --------------------------------------------
# DataStore Preferences
# --------------------------------------------
-keep class androidx.datastore.preferences.core.** { *; }

# --------------------------------------------
# AndroidX / General
# --------------------------------------------
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# Keep enum values (used by Room for type converters)
-keepclassmembers enum * {
    **[] values();
    public *;
}

# --------------------------------------------
# WebView (JavaScript interface)
# --------------------------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# --------------------------------------------
# Serializable (session snapshots, etc.)
# --------------------------------------------
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# --------------------------------------------
# Optimization
# --------------------------------------------
# Don't warn about OkHttp (used internally by WebView)
-dontwarn okhttp3.**
-dontwarn okio.**

# Don't warn about Kotlin reflection
-dontwarn kotlin.reflect.**
