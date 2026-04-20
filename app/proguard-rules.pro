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

# --- WorkManager/Worker ---
# Keep worker class and constructor for reflection
-keepnames class com.kblack.offlinemap.data.worker.MapDownloadWorker
-keepclassmembers class com.kblack.offlinemap.data.worker.MapDownloadWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# --- MainActivity reflection ---
-keepnames class com.kblack.offlinemap.presentation.MainActivity

# --- Hilt/Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-keep class com.kblack.offlinemap.**_Factory { *; }
-keep class com.kblack.offlinemap.**_HiltModules* { *; }
-keep class com.kblack.offlinemap.MyApp { *; }
-keep class com.kblack.offlinemap.**_Impl { *; }
-keep class com.kblack.offlinemap.**_MembersInjector { *; }
-keep class com.kblack.offlinemap.**_Component { *; }
-keep class com.kblack.offlinemap.**_Subcomponent { *; }
-keep class com.kblack.offlinemap.**_Module { *; }
-keepattributes *Annotation*

# --- Gson/Moshi/Retrofit/Room models ---
-keep class com.kblack.offlinemap.data.model.** { *; }
-keep class com.kblack.offlinemap.domain.models.** { *; }
-keep class com.kblack.offlinemap.data.repository.MapAllowlistRepositoryImpl { *; }
# Giữ riêng cho model allowlist nếu cần
-keep class com.kblack.offlinemap.data.model.MapAllowlist { *; }
-keep class com.squareup.moshi.** { *; }
-keep class retrofit2.** { *; }
-keep class androidx.room.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-keepattributes Signature

# --- Notification/Service/Unzip ---
# (No extra keep needed for NotificationCompat, ServiceInfo, or unzip libs)

# --- Native/JNI ---
# Keep native methods for zstd-jni, tar, graphhopper
-keep class com.github.luben.zstd.** { *; }
-keep class org.apache.commons.compress.** { *; }
-keep class com.graphhopper.** { *; }

# --- General AndroidX/Compose (optional, for Compose preview/debug) ---
#-keep class androidx.compose.** { *; }

# --- Ignore missing desktop-only classes for graphhopper/commons-compress ---
-dontwarn java.awt.**
-dontwarn java.awt.image.**
-dontwarn javax.imageio.**
-dontwarn java.lang.management.**
-dontwarn org.apache.xmlgraphics.image.codec.**
-dontwarn org.brotli.dec.**
-dontwarn org.objectweb.asm.**
-dontwarn org.tukaani.xz.**