# --- Xposed -------------------------------------------------------------------
# Modern libxposed entry point, referenced by name from META-INF/xposed/java_init.list.
-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# The compat layer resolves client classes/members by name at runtime.
-keep class com.my.televip.xposed.** { *; }
-keep class com.my.televip.base.AbstractMethodHook { *; }
-keep class com.my.televip.base.AbstractMethodHook$MethodHookParam { *; }
-keepclassmembers class com.my.televip.Clients.** { *; }
