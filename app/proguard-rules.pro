# Configure ProGuard to retain line numbers in stacktraces
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# For xml-specified drawable resources
-keep class me.jfenn.attribouter.R$*
-keepclassmembers class me.jfenn.attribouter.R$* {
    public static <fields>;
}

# For wedge construction (from xml parser)
-keep class * extends me.jfenn.attribouter.wedges.Wedge
