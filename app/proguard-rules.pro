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
#-renamesourcefileattribute SourceFile

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

#-keep class com.highsecure.pixel.art.data** { *; }
#-keep class com.highsecure.pixel.art.domain** { *; }
-dontwarn okhttp3.**

-keep class com.google.** { *; }


-keepattributes Signature


-dontwarn com.squareup.okhttp.**
-dontwarn okio.**

-dontpreverify
# Reduce the size of the output some more.
-repackageclasses ''
-allowaccessmodification
# Switch off some optimizations that trip older versions of the Dalvik VM.
-optimizations !code/simplification/arithmetic
# Keep a fixed source file attribute and all line number tables to get line
# numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
# RemoteViews might need annotations.
-keepattributes *Annotation*
# Preserve all fundamental application classes.
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class androidx.appcompat.widget.** { *; }
-keep class kotlin.** { *; }

-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}


# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit.**
-dontwarn rx.**

-keep public class com.google.android.gms.** { public protected *; }

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}


# Preserve all View implementations, their special context constructors, and
# their setters.
-keep public class * extends android.view.View {
public <init>(android.content.Context);
public <init>(android.content.Context, android.util.AttributeSet);
public <init>(android.content.Context, android.util.AttributeSet, int);
public void set*(...);
}


# Preserve all classes that have special context constructors, and the
# constructors themselves.
-keepclasseswithmembers class * {
public <init>(android.content.Context, android.util.AttributeSet);
}
# Preserve all classes that have special context constructors, and the
# constructors themselves.
-keepclasseswithmembers class * {
public <init>(android.content.Context, android.util.AttributeSet, int);
}
# Preserve the special fields of all Parcelable implementations.
-keepclassmembers class * implements android.os.Parcelable {
static android.os.Parcelable$Creator CREATOR;
}
# Preserve static fields of inner classes of R classes that might be accessed
# through introspection.
-keepclassmembers class **.R$* {
public static <fields>;
}
# Preserve the required interface from the License Verification Library
# (but don't nag the developer if the library is not used at all).
#-keep public interface com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
# The Android Compatibility library references some classes that may not be
# present in all versions of the API, but we know that's ok.
-dontwarn android.support.**
# Preserve all native method names and the names of their classes.
-keepclasseswithmembernames class * {
native <methods>;
}
# Preserve the special static methods that are required in all enumeration
# classes.
-keepclassmembers class * extends java.lang.Enum {
public static **[] values();
public static ** valueOf(java.lang.String);
}
# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your application doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.
-keepclassmembers class * implements java.io.Serializable {
static final long serialVersionUID;
static final java.io.ObjectStreamField[] serialPersistentFields;
private void writeObject(java.io.ObjectOutputStream);
private void readObject(java.io.ObjectInputStream);
java.lang.Object writeReplace();
java.lang.Object readResolve();
}
# Your application may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:
# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface





#progard of googleadmod lib
# Keep SafeParcelable value, needed for reflection. This is required to support backwards
# compatibility of some classes.
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

# Keep the names of classes/members we need for client functionality.
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Needed for Parcelable/SafeParcelable Creators to not get stripped
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Needed when building against the Marshmallow SDK
-dontwarn org.apache.http.**
-dontwarn com.squareup.**
-dontwarn com.google.**



# Keep Picasso
-keep class com.squareup.picasso.** { *; }
-keep class com.bumptech.** { *; }
-dontwarn com.bumptech.**
-keep class okio.Okio.** { *; }
-dontwarn okio.Okio.**
-keep class org.codehaus.** { *; }
-dontwarn org.codehaus.**


-keepclasseswithmembers class * {
    @com.squareup.picasso.** *;
}
-keepclassmembers class * {
    @com.squareup.picasso.** *;
}

-keepattributes *Annotation*

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}

-keep class rx.** { *; }
-dontwarn rx.**

-keep public class * extends rx.functions.** { *; }
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keep class umagic.ai.aiart.retrofit.** { *; }

-keepclasseswithmembernames class * {
native <methods>;
}
-keep class androidx.renderscript.** { *; }

-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn com.squareup.okhttp.**


-keepclassmembers enum * { *; }

-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
-keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.


# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson** { *; }
# Prevent R8 from leaving Data object members always null
-keepclasseswithmembers class * {
    <init>(...);
    @com.google.gson.annotations.SerializedName <fields>;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken