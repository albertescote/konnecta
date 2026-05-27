# ---- Kotlin ----
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-dontwarn kotlin.**

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# Keep all @Serializable data classes and their generated serializers
-keep @kotlinx.serialization.Serializable class * { *; }
-keep,includedescriptorclasses class com.konnecta.app.**$$serializer { *; }
-keepclassmembers class com.konnecta.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.konnecta.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Kotlinx Coroutines ----
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---- Supabase ----
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ---- Ktor ----
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.netty.** { *; }
-dontwarn io.netty.**

# ---- OneSignal ----
-keep class com.onesignal.** { *; }
-dontwarn com.onesignal.**
-keep class com.google.android.gms.common.api.GoogleApiClient { void connect(); void disconnect(); }

# ---- Coil ----
-keep class coil.** { *; }
-dontwarn coil.**

# ---- Google Auth / Credential Manager ----
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.libraries.**

# ---- OkHttp (used by Coil) ----
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ---- Jetpack Compose ----
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ---- Android / AndroidX ----
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**
