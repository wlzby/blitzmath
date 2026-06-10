# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
# Leaderboard Fix
-keep class com.mawelly.blitzmath.leaderboard.** { *; }
-keepclassmembers class com.mawelly.blitzmath.leaderboard.LeaderboardEntry {
    public <init>();
    *** get*();
    *** set*(***);
}

# AdMob Rules
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.mediation.** { *; }

# Firebase Rules
-keep class com.google.firebase.** { *; }

# BlitzMath Data Models
-keep class com.mawelly.blitzmath.data.** { *; }
-keep class com.mawelly.blitzmath.game.** { *; }

# Huawei HMS Rules
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.huawei.hianalytics.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.agconnect.**{*;}

# Huawei Ads Kit Rules
-keep class com.huawei.openalliance.ad.** { *; }
-keep class com.huawei.hms.ads.** { *; }