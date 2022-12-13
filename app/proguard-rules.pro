# Merge
-flattenpackagehierarchy com.github.catvod.spider.merge
-repackageclasses com.github.catvod.spider.merge

# Spider
-keep class com.github.catvod.crawler.* { *; }
-keep class com.github.catvod.spider.* { public <methods>; }
-keep class com.github.catvod.parser.* { public <methods>; }
-keep class com.github.catvod.js.* { public <methods>; }

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