package com.yunext.kotlin.kmp.sample

import App
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.yunext.kotlin.kmp.context.application
import com.yunext.kotlin.kmp.context.registerHDContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, KtorServer::class.java))
    }

    override fun onStop() {
        super.onStop()
        stopService(Intent(this, KtorServer::class.java))
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    LaunchedEffect(Unit) {
        registerHDContext {
            init(application)
        }
    }
    App()
}