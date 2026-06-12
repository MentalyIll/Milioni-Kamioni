# StayFree ProGuard rules

# Keep Room entities
-keep class com.example.stayfree.data.local.entity.** { *; }

# Keep Hilt generated classes
-keepclasseswithmembernames class * { @dagger.* <fields>; }
-keep class dagger.hilt.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
