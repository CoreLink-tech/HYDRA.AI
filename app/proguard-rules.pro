# App-level keep rules. Kotlinx serialization and Room are kept by their consumer rules.

# SQLCipher's native code looks up these classes and methods by name through JNI.
-keep class net.zetetic.database.** { *; }
-keepclassmembers class net.zetetic.database.** { *; }

# Keep readable stack traces in the on-device crash report.
-keepattributes SourceFile,LineNumberTable
