# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.jnetaol.sshcommander.data.model.** { *; }
-keep class com.jnetaol.sshcommander.data.db.** { *; }
-dontwarn javax.naming.**
-dontwarn com.jcraft.jsch.**
