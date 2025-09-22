enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
        // caffeine
        maven {
            url = uri("https://dl.cloudsmith.io/public/avatye/kmp-caffeine-common/maven/")
        }

        // adcash + mediation
        maven {
            url = uri("https://dl.cloudsmith.io/public/avatye/android-adcash-internal/maven/")
        }
        maven {
            url = uri("https://dl.cloudsmith.io/public/avatye/android-adcash/maven/")
        }
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle")
        }
        maven {
            url = uri("https://nexus.wisernd.co.kr/repository/sdk/")
        }
        maven {
            url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")
        }
        maven {
            url = uri("https://dl.cloudsmith.io/public/avatye/android-adcash/maven/")
        }
        maven {
            url = uri("https://cauly.github.io/cauly-sdk-android-maven/maven-repo")
        }
    }
}

rootProject.name = "memog2"
include(":androidApp")
include(":shared")