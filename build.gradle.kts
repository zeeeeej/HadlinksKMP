import org.gradle.internal.operations.OperationStartEvent
import java.io.FileInputStream
import java.io.FileOutputStream

plugins {
//    id("root.publication")
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false

    alias(libs.plugins.maven.publish)
}

// https://juejin.cn/post/7170684769083555877
abstract class BuildListenerService :
    BuildService<BuildListenerService.Params>,
    org.gradle.tooling.events.OperationCompletionListener {

    interface Params : BuildServiceParameters

    override fun onFinish(event: org.gradle.tooling.events.FinishEvent) {
        println("BuildListenerService got event $event")
    }
}

val buildServiceListener = gradle.sharedServices.registerIfAbsent(
    "buildServiceListener",
    BuildListenerService::class.java
) { }

abstract class Services @Inject constructor(
    val buildEventsListenerRegistry: BuildEventsListenerRegistry
)

val services = objects.newInstance(Services::class)

services.buildEventsListenerRegistry.onTaskCompletion(buildServiceListener)


//// 任务 所有task完成后，复制包到assets下
private val finalTaskName = "finalTask"
private val finalTaskNameOpened = false
// 定义一个自定义任务
if (finalTaskNameOpened) {
    tasks.register(finalTaskName) {
        println("======> $finalTaskName ")
//    doLast {
        println("======> finalTask 所有任务执行完毕后的自定义任务。")
//        // 在这里添加你希望在所有任务执行完毕后执行的代码

        try {
            val path = project.projectDir.absolutePath
            val source = path + ("/composeApp/release/composeApp-release.apk")
            val dest = path + ("/composeApp/src/androidMain/assets/composeApp.apk")
            println("===> path = $path")
            println("===> source = $source")
            println("===> dest = $dest")
            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { out ->
                    out.write(input.readBytes())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
//    }
    }
    project.afterEvaluate {
        println("======> ${project.name} afterEvaluate-------")
        // 创建一个任务，该任务依赖于所有已知的任务
        tasks.register("allTasksComplete") {
            // 依赖于所有已知的任务（除了finalTask）
            dependsOn(tasks.names.filter {
                // println("======>tasks.name = ${it}")
                it != ("${finalTaskName}") && it != "allTasksComplete"
            })
            doLast {
                println("======>allTasksComplete 所有任务都已完成。现在执行${finalTaskName}。")
            }
        }

        // 将finalTask设置为依赖于allTasksComplete任务
        tasks.named(finalTaskName).configure {
            println("======> ${project.name} afterEvaluate::configure-------")
            dependsOn("allTasksComplete")
        }
    }
}


