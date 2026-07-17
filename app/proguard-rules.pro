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

# Keep our data models, database entities, and API models to prevent Moshi reflection errors
-keep class com.example.core.models.** { *; }
-keep class com.example.core.api.** { *; }
-keep class com.example.database.** { *; }

# Keep Moshi, OkHttp, Retrofit, and associated metadata
-keep class com.squareup.moshi.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}

# Keep support for reflection-based Moshi JSON serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, AnnotationDefault, *Annotation*
-dontwarn com.squareup.moshi.**
-dontwarn okio.**
-dontwarn javax.annotation.**

