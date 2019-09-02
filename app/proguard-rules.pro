-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Coroutine optimisation pour Dispatchers.Main, potentiellement plus nécessaire depuis AS 3.6.
-assumevalues class kotlinx.coroutines.internal.MainDispatcherLoader {
  boolean FAST_SERVICE_LOADER_ENABLED return false;
}
# -checkdiscard class kotlinx.coroutines.internal.FastServiceLoader

# OkHttp
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Glide integration libraries
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
