# 代码混淆压缩比，在0~7之间，默认为5
-optimizationpasses 0
# 混淆时不使用大小写混合，由于windows对大小写不敏感, 混淆后的类名为小写
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库的类
-dontskipnonpubliclibraryclasses
# 指定不去忽略非公共的库的类的成员
-dontskipnonpubliclibraryclassmembers
# Android不需要预校验preverify, proguard的4个步骤之一, 去掉这一步可加快混淆速度
-dontpreverify
-dontoptimize
# 有了verbose这句话，混淆后就会生成映射文件
-verbose
# 包含有类名->混淆后类名的前后映射关系, 然后使用printmapping指定映射文件的名称
-printmapping proguardMapping.txt
# 指定混淆时采用的算法，后面的参数是一个过滤器
# 过滤器是谷歌推荐的算法，一般不用变
-optimizations !code/simplification/cast,!field/*,!class/merging/*
# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*,InnerClasses
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation {*;}
# 类型转换错误 添加如下代码以便过滤泛型（不写可能会出现类型转换错误，一般情况把这个加上就是了）,即避免泛型被混淆
-keepattributes Signature
-keepattributes EnclosingMethod, InnerClasses
# 抛出异常时保留代码行号
-keepattributes LineNumberTable
-renamesourcefileattribute SourceFile

-obfuscationdictionary build/obfuscation-dictionary.txt
-classobfuscationdictionary build/class-dictionary.txt
-packageobfuscationdictionary build/package-dictionary.txt

-flattenpackagehierarchy com.github.catvod.spider.merge
-repackageclasses com.github.catvod.spider.merge

-dontwarn javax.script.**
-keep class javax.script.**{*;}
-dontwarn com.sun.script.**
-keep class com.sun.script.**{*;}
-dontwarn okio.**
-keep class okio.**{*;}
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
-dontwarn com.google.**
-keep class com.google.**{*;}
-dontwarn com.android.**
-keep class com.android.**{*;}
-dontwarn rxhttp.**
-keep class rxhttp.**{*;}
-dontwarn kotlin.**
-keep class kotlin.**{*;}
-dontwarn kotlinx.**
-keep class kotlinx.**{*;}

# Spider
-keep class com.github.catvod.crawler.* { *; }
-keep class com.github.catvod.spider.* { *; }
-keep class com.github.catvod.parser.* { *; }
