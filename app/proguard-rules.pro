# Add project specific Proguard rules here.
# By default, the flags in this file are appended to flags specified
# in getDefaultProguardFile('proguard-android-optimize.txt').

# Keep Hilt and Dagger annotations & components
-keep class javax.inject.Provider { *; }
-keep class dagger.hilt.internal.GeneratedComponent { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.RoomDatabase