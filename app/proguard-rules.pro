# Keep Kotlin metadata (ضروري للتطبيقات المكتوبة بـ Kotlin)
-keepclassmembers class kotlin.Metadata { *; }

# Keep ViewModel classes (Android Architecture Components)
-keep public class * extends androidx.lifecycle.ViewModel

# Keep Room database entities and DAO interfaces
-keep class androidx.room.RoomDatabase { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Keep Firebase classes (Firestore, Auth, Analytics)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep classes used in Jetpack Compose (reflection)
-keep class androidx.compose.** { *; }

# Keep Parcelable implementations (To-Do items may implement Parcelable)
-keepclassmembers class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep all classes annotated with @Keep (if you use androidx.annotation.Keep)
-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep data classes getters/setters (Kotlin data classes)
-keepclassmembers class * {
    kotlin.Metadata metadata;
    <fields>;
    <methods>;
}

# Keep Proguard from stripping lambda classes (used in Kotlin)
-keepclassmembers class kotlin.jvm.internal.** { *; }

# Optional: Keep debug info for better crash reports (if not disabled)
-keepattributes SourceFile,LineNumberTable

# Keep Crashlytics classes (Prevent obfuscation)
-keep class com.google.firebase.crashlytics.** { *; }

# Keep all annotations to prevent removal
-keepattributes *Annotation*

# Keep native symbols for crash reporting
-keep class com.google.firebase.crashlytics.internal.ndk.** { *; }

# Keep Firebase analytics classes (optional but recommended)
-keep class com.google.android.gms.measurement.** { *; }

# Keep classes used in automatic breadcrumbs and custom keys
-keep class com.google.firebase.crashlytics.internal.common.CrashlyticsController { *; }
-keep class com.google.firebase.crashlytics.internal.model.** { *; }
