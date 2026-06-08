# ============================================
# MIQAT APP — COMPLETE PROGUARD RULES
# ============================================

# --- GENERAL ANDROID ---
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Keep all public classes referenced from AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --- KOTLIN ---
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# --- JETPACK COMPOSE ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.activity.compose.** { *; }
-keep class androidx.lifecycle.compose.** { *; }
-keep class androidx.navigation.compose.** { *; }

# --- HILT / DAGGER ---
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep class dagger.hilt.** { *; }
-dontwarn dagger.**
-dontwarn javax.annotation.**
-keepclasseswithmembernames class * {
    @javax.inject.* <fields>;
    @javax.inject.* <methods>;
}

# --- VIEWMODEL ---
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- ROOM DATABASE ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }
-dontwarn androidx.room.**

# --- DATASTORE ---
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
-dontwarn androidx.datastore.**

# --- MEDIA3 / EXOPLAYER ---
-keep class androidx.media3.** { *; }
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class androidx.media3.common.** { *; }
-dontwarn androidx.media3.**
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**

# --- WORKMANAGER ---
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.CoroutineWorker { *; }
-keepclassmembers class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-dontwarn androidx.work.**

# --- ADHAN PRAYER LIBRARY ---
-keep class com.batoulapps.adhan.** { *; }
-keepclassmembers class com.batoulapps.adhan.** { *; }
-dontwarn com.batoulapps.adhan.**

# --- GSON / JSON PARSING ---
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

# --- KEEP ALL DATA/MODEL CLASSES ---
# These are used for JSON parsing and Room — must not be removed
-keep class **.model.** { *; }
-keep class **.data.** { *; }
-keep class **.domain.** { *; }
-keep class **.entity.** { *; }
-keepclassmembers class **.model.** { *; }
-keepclassmembers class **.data.** { *; }

# --- ENUMS ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    **[] $VALUES;
    public *;
}

# --- PARCELABLE ---
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# --- SERIALIZABLE ---
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# --- ICU / ISLAMIC CALENDAR ---
-keep class android.icu.** { *; }
-keep class android.icu.util.IslamicCalendar { *; }
-dontwarn android.icu.**

# --- ALARM MANAGER / BROADCAST RECEIVERS ---
-keep class * extends android.content.BroadcastReceiver { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver {
    public void onReceive(android.content.Context, android.content.Intent);
}

# --- KEEP ASSETS (Quran JSON) ---
-keep class **.R$raw { *; }
-keep class **.R$font { *; }
-keep class **.R$drawable { *; }

# --- NAVIGATION ---
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# --- COIL (if used for images) ---
-keep class coil.** { *; }
-dontwarn coil.**

# --- SUPPRESS COMMON WARNINGS ---
-dontwarn java.lang.invoke.**
-dontwarn **$$Lambda$*
-dontwarn sun.misc.Unsafe
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.*
