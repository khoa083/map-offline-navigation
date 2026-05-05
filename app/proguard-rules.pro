-keepnames class com.kblack.offlinemap.MyApp
-keepnames class com.kblack.offlinemap.ui.MainActivity
-keepnames class com.kblack.offlinemap.ui.BugHandlerActivity

-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.** { *; }
-keep class com.kblack.offlinemap.**_Factory { *; }
-keep class com.kblack.offlinemap.**_HiltModules* { *; }
-keep class com.kblack.offlinemap.**_Impl { *; }
-keep class com.kblack.offlinemap.**_MembersInjector { *; }
-keep class com.kblack.offlinemap.**_Component { *; }
-keep class com.kblack.offlinemap.**_Subcomponent { *; }
-keep class com.kblack.offlinemap.**_Module { *; }
-keepattributes *Annotation*
-keepattributes Signature

-keep class com.kblack.offlinemap.data.repository.* {
    public <init>(...);
    public <methods>;
}
-keep class com.kblack.offlinemap.data.remote.** {
    public <init>(...);
    public <methods>;
}
-keep class com.kblack.offlinemap.presentation.viewmodel.** {
    public <init>(...);
    public <methods>;
}
-keep class com.kblack.offlinemap.usecase.** {
    public <init>(...);
    public <methods>;
}
-keep class com.kblack.offlinemap.data.models.** {
    public <init>(...);
    public <methods>;
    public <fields>;
}
-keep class com.kblack.offlinemap.models.** {
    public <init>(...);
    public <methods>;
    public <fields>;
}
-keep class com.kblack.offlinemap.data.mapper.** {
    public <init>(...);
    public <methods>;
}
-keepnames class com.kblack.offlinemap.data.worker.MapDownloadWorker
-keepclassmembers class com.kblack.offlinemap.data.worker.MapDownloadWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.squareup.moshi.** { *; }
-keepclasseswithmembers class com.squareup.moshi.adapters.** { *; }
-keep class retrofit2.** { *; }
-keepclasseswithmembers class retrofit2.adapters.** { *; }
-keepclasseswithmembers class retrofit2.converters.** { *; }
-keep class okhttp3.** { *; }
-keep class okhttp3.logging.** { *; }
-keep class com.kblack.offlinemap.BuildConfig { *; }
-keep class com.kblack.offlinemap.data.utils.Constant { *; }
-keep class com.graphhopper.** { *; }
-keepclassmembers class com.graphhopper.** {
    public <init>(...);
    public <methods>;
    public <fields>;
}
-keep class com.github.luben.zstd.** { *; }
-keep class org.apache.commons.compress.** { *; }
-keep class org.jetbrains.annotations.** { *; }
-dontwarn java.awt.**
-dontwarn java.awt.image.**
-dontwarn javax.imageio.**
-dontwarn java.lang.management.**
-dontwarn org.apache.xmlgraphics.image.codec.**
-dontwarn org.brotli.dec.**
-dontwarn org.objectweb.asm.**
-dontwarn org.tukaani.xz.**
-dontwarn org.slf4j.**
-dontwarn net.sf.saxon.**
