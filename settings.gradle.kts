
rootProject.name = "HadlinksKMP"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
//    includeBuild("convention-plugins")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven   ("https://jitpack.io")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven   ("https://jitpack.io")
    }
}




include(":composeApp")
include(":shared")
include(":yunext-context")
include(":yunext-common")
include(":yunext-compose")
include(":yunext-ble")

gradle.beforeSettings{
    println("-x-------- beforeSettings -------")
}

gradle.settingsEvaluated{
    println("-x-------- settingsEvaluated  -------")
}

gradle.projectsLoaded {
    println("-x-------- projectsLoaded  -------")
}

gradle.projectsEvaluated {
    println("-x-------- projectsEvaluated  ------- 初始化+配置完成")
}

println("-x-- setting.gradle.kts logger!")

//gradle.buildFinished {
//    println("-x--结束！")
//}

// 7.3 废弃
//gradle.addListener(object :TaskExecutionListener{
//    override fun beforeExecute(task: Task) {
//        println("-x--------> beforeExecute  ------- ${task.name}")
//    }
//
//    override fun afterExecute(task: Task, state: TaskState) {
//        println("-x--------> afterExecute  ------- ${task.name}/${task.state.didWork}")
//    }
//
//})

gradle.allprojects {
    beforeEvaluate {
        println("-x-------- allprojects::beforeEvaluate ${project.name} -------")
    }

    afterEvaluate {
        println("-x-------- allprojects::afterEvaluate ${project.name} -------")
    }
}
