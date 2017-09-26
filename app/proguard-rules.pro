# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\android_sdk/tools/proguard/proguard-android.txt
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





-libraryjars <java.home>/lib/rt.jar(java/**,javax/**)

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes InnerClasses

-keepattributes *Annotation*
-keepattributes Signature


-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep class com.itheima.mobilesafe.engine.AppInfoProvider
-keep class net.youmi.android.** {*;}
-keep class **.R$* { *; }

#UMENG 统计
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\UDisk\libs\umeng-analytics-v5.6.1.jar'

#-keep public class com.dmsys.airdiskpro.R$*{
#public static final int *;
#}


#########################

#UMENG 分享
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**


-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**

-keep public class com.umeng.socialize.* {*;}
-keep public class javax.**
-keep public class android.webkit.**

-keep class com.facebook.**
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**

-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}

-keep class com.tencent.mm.sdk.modelmsg.** implements com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}

-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}

##################################

##UMENG自动更新

-keep public class com.umeng.fb.ui.ThreadView {
}

# 添加第三方jar包
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\DMSoftwareUpgrade\libs\umeng-update-v2.6.0.jar'

# 以下类过滤不混淆
-keep public class * extends com.umeng.**
# 以下包不进行过滤
-keep class com.umeng.** { *; }

##################################


#eventBus
 -keepclassmembers class ** {
 public void onEvent*(**);
}





## UMENG反馈
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\UDisk\libs\umeng-feedback-v4.3.jar'


##迅雷库
-dontwarn com.xunlei.mediaplayer.**
-keep class com.xunlei.mediaplayer.** {*;}



-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    ** valueOf(java.lang.String);

}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}



-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\UDisk\libs\glide-3.6.1.jar'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\UDisk\libs\EventBus2.4.jar'
-libraryjars 'E:\DM airdisk\trunk\HiDisk\UDisk\libs\xlmediaplayer.jar'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\AodPlayer\libs\android-support-v4.jar'

-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\DMImageLoader\libs\okhttp-2.4.0.jar'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\DMImageLoader\libs\okio-1.4.0.jar'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\DMReader\libs\gson-2.2.4.jar'

-libraryjars 'E:\work\airdisk\DM airdisk pro\src\DMSDK\libs\armeabi\libDMSdk.so'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\LibJpeg\libs\armeabi\libjpeg8d.so'

-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\VodPlayer\libs\xlmediaplayer.jar'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\VodPlayer\libs\armeabi\libuffmpeg.so'
-libraryjars 'E:\work\airdisk\DM airdisk pro\svn\VodPlayer\libs\armeabi\libuplayer23.so'

-dontwarn com.sun.tools.**
-keep class com.sun.tools.** {*;}

-dontwarn org.teleal.**
-keep class org.teleal.** {*;}

-dontwarn android.support.v4.**
-keep class android.support.v4.** {*;}


#simple-xml-2.7.1 混淆
-keep class org.xmlpull.** {*;}
-keep class org.kxml2.io.** {*;}


#OKhttp 的混淆
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.** { *;}

-dontwarn okio.**
-keep class okio.** { *;}

-keep public class org.codehaus.**
-keep public class java.nio.**

-dontwarn org.apache.**
-keep class org.apache.** { *;}

-dontwarn java.nio.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn com.squareup.**

#GSON 的混淆

-keep class com.google.**{*;}
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }

-keep class com.dmreader.object.** { *; }  ##这里需要改成解析到哪个  javabean
-keep class com.dm.dmsdk.pri.json.model.** { *; }  ##这里需要改成解析到哪个  javabean


# commons-codec.jar 上面OKhttp 已经过滤了
#-keep class org.apache.commons.codec.** {*;}
#-dontwarn org.apache.commons.codec.**

#commons-logging 上面OKhttp 已经过滤了
#-keep public class org.apache.commons.logging.** {*;}
#-keep public class org.apache.commons.logging.impl.** {*;}

# 本项目过滤的文件
-dontwarn com.dmsys.libjpeg.ImageAdapter
-dontwarn com.example.textreader.MainActivity

-keep public class javax.xml.stream.events.**{ *; }
-dontwarn javax.xml.stream.events.**

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }

-keep class de.aflx.sardine.**{ *; }











