# Remove problematic Apache HttpClient classes if not used
-dontwarn javax.naming.**
-dontwarn javax.naming.directory.**
-dontwarn javax.naming.ldap.**
-dontwarn org.ietf.jgss.**

# Apache HttpClient 관련 보존
-keep class org.apache.http.** { *; }
-keep class javax.naming.** { *; }
-keep class org.ietf.jgss.** { *; }