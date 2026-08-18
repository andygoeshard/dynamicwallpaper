# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Reglas para Iris
# No tocar los DTOs porque se rompe la serialización JSON
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature

# kotlinx.serialization: mantener todos los DTOs @Serializable y sus serializers
# (weather, images, location, userpreferences y domain.model)
-keep,includedescriptorclasses class com.andyl.iris.**$$serializer { *; }
-keepclasseswithmembers class com.andyl.iris.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class com.andyl.iris.data.weather.dto.** { *; }
-keepclasseswithmembers class com.andyl.iris.data.imagesprovider.dto.** { *; }
-keepclasseswithmembers class com.andyl.iris.data.location.dto.** { *; }
-keepclasseswithmembers class com.andyl.iris.data.userpreferences.dto.** { *; }
-keepclasseswithmembers class com.andyl.iris.domain.model.GeoPlace { *; }
-keepclasseswithmembers class com.andyl.iris.domain.model.WallpaperHistoryEntry { *; }

# Reglas para Ktor
-keep class io.ktor.** { *; }

-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn org.slf4j.impl.StaticLoggerBinder