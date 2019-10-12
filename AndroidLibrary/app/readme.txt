// 集成三方库配置
buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url "https://jitpack.io"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        // 集成Mob
        classpath "com.mob.sdk:MobSDK:+"
    }
}
// 以上拷贝到工程根目录

// 集成Mob
apply plugin: 'com.mob.sdk' // 拷贝到使用或引用Mob的工程目录
在Manifest清单文件application节点中配置:tools:replace="android:name"