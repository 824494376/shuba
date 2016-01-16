# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/applemac/tools/Android/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.

-ignorewarnings

-keepattributes Signature
-keepattributes *Annotation*

-keep class sun.misc.Unsafe { *; }

-dontwarn dagger.internal.codegen.**
-dontwarn com.squareup.**
-dontwarn com.google.common.**
-dontwarn android.net.http.**
-dontwarn android.support.v4.**

-keep class android.support.v4.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

#-keep class * implements android.os.Parcelable {
#public static final android.os.Parcelable$Creator *;
#}

-dontwarn android.net.http.**

-dontwarn roboguice.**

-keep class com.google.** { *; }
-keep class com.google.gson.** { *; }
-keep class cn.jpush.android.** { *; }
-keep class com.viewpagerindicator.** { *; }
-keep class com.loopj.android.** { *; }
-keep class com.liuguangqiang.** { *; }

#QQ
-keep class com.tencent.** { *; }
-keep class * extends android.app.Dialog

#shuba
-keep class com.qiwenge.android.entity.** { *; }
-keep class com.qiwenge.android.entity.base.** { *; }

#ButterKnife --------------------------------------------------------------------------------------
-keep class butterknife.** { *; }

-dontwarn butterknife.internal.**

-keep class **$$ViewInjector { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#Dagger --------------------------------------------------------------------------------------

-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection﻿

-keep class dagger.** { *; }

-keep class com.qiwenge.android.module.** { *; }

-keep class com.qiwenge.android.app.** { *; }

#Keep the annotated things annotated
-keepattributes *Annotation*

#Keep the dagger annotation classes themselves
-keep @interface dagger.*,javax.inject.*

#Keep the Modules intact
-keep @dagger.Module class *

-keep class javax.inject.** { *; }

##-Keep the fields annotated with @Inject of any class that is not deleted.
-keepclasseswithmembernames class * {
  @javax.inject.* <fields>;
  @javax.inject.* <methods>;
  @javax.inject.* <init>(...);
}

# Keep the generated classes by dagger-compile
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection

# MVP
-keep class com.qiwenge.android.mvp.** { *; }
-keep class com.liuguangqiang.android.mvp** { *; }

#EventBus
-keep class de.greenrobot.** { *; }
-keepclassmembers class ** {
    public void onEvent*(**);
}

#RxJava and RxAndroid
-keep class rx.** { *; }