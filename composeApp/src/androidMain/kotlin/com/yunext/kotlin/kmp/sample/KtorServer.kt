package com.yunext.kotlin.kmp.sample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.yunext.kotlin.kmp.common.util.currentTime
import com.yunext.kotlin.kmp.common.util.datetimeFormat
import io.ktor.client.content.LocalFileContent
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.http.content.staticRootFolder
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class KtorServer : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val coroutineScope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("KtorServer"))

    private var server: NettyApplicationEngine? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startKtorServer()
        return START_STICKY
    }

    private fun startKtorServer() {
        println("[ktor]开启")
        coroutineScope.launch {
            this@KtorServer.server?.stop()
            val server = embeddedServer(Netty, 8080) {
                install(ContentNegotiation) {
                    json()
                }



                routing {
                    get("/api/hadlinks") {
                        println("[ktor]/api/hadlinks客户端请求")
                        call.respondText {
                            "Welcome to Hadlinks! 当前时间:${datetimeFormat { currentTime().toStr() }}"
                        }
                    }

                    get("/api/hadlinks/composeApp") {
                        try {
                            println("[ktor]/api/hadlinks/composeApp")
//                        val filePath = "release/composeApp-release.apk"
                            val inputStream = assets.open("composeApp.apk")
                            inputStream.use {
    //                            call.respond(LocalFileContent(File(filePath), ContentType.Text.Plain))
                                val filePath = applicationContext.filesDir.absolutePath + "/composeApp.apk"
                                val file = File(filePath)
                                if (!file.exists()){
                                    file.createNewFile()
                                }else{
                                    file.delete()
                                    file.createNewFile()
                                }
                                file.writeBytes(inputStream.readBytes())
                                println("[ktor]/api/apk file = ${file.absolutePath}")
//                                call.respondFile(file.parentFile!!,"composeApp.apk")
                                call.respondFile(file)
    //                            call.respondBytes(contentType = ContentType.Text.Plain) { inputStream.readBytes() }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }


                    }

                    staticResources("","apk")
                    println("staticRootFolder=$staticRootFolder")

                }

            }
            val rootPath = server.environment.rootPath
            println("rootPath=$rootPath")
            server.start()
            this@KtorServer.server = server

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("[ktor]结束")

        coroutineScope.cancel()
        this.server?.stop()
        this.server = null
    }


}