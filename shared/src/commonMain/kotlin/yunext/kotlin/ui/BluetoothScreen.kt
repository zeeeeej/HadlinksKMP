package yunext.kotlin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import color
import randomZhongGuoSe
import yunext.kotlin.util.randomBG

internal enum class Screen {
    Main, Slave, Master
    ;
}

@Composable
fun BluetoothScreen(modifier: Modifier) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Main.name) {
        composable(Screen.Main.name) {
            Main(modifier, navController, onSlave = {
                navController.navigate(Screen.Slave.name)
            }, onMaster = {
                navController.navigate(Screen.Master.name)
            })
        }

        composable(Screen.Slave.name) {
            SlaveScreen(modifier, navController, onBack = {
                navController.popBackStack()
            })
        }

        composable(Screen.Master.name) {
            MasterScreen(modifier, navController, onBack = {
                navController.popBackStack()
            })
        }
    }


}

@Composable
private fun Main(
    modifier: Modifier = Modifier,
    controller: NavHostController,
    onSlave: () -> Unit,
    onMaster: () -> Unit
) {
    Column(modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).randomBG()
        ) {
            Item(Modifier.fillMaxSize().clickable {
onSlave()
            }, "Slave")
        }

        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).randomBG()
        ) {
            Item(Modifier.fillMaxSize().clickable {
onMaster()
            }, "Master")
        }
    }
}

@Composable
private fun Item(modifier: Modifier, text: String) {
    Box(modifier, contentAlignment = Alignment.Center) {

        Text(
            text,
            style = TextStyle.Default.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold)
        )
    }
}