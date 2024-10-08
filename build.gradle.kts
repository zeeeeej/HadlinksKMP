import org.gradle.internal.operations.OperationStartEvent

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

val buildServiceListener = gradle.sharedServices.registerIfAbsent("buildServiceListener", BuildListenerService::class.java) { }

abstract class Services @Inject constructor(
    val buildEventsListenerRegistry: BuildEventsListenerRegistry
)

val services = objects.newInstance(Services::class)

services.buildEventsListenerRegistry.onTaskCompletion(buildServiceListener)