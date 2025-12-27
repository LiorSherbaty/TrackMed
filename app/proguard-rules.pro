# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Room entities
-keep class com.trackmed.data.local.entity.** { *; }

# Keep Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.trackmed.data.backup.** {
    *** Companion;
}

-keepclasseswithmembers class com.trackmed.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Glance
-keep class androidx.glance.** { *; }

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep generic signatures for Room
-keepattributes Signature

# For enumeration classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
