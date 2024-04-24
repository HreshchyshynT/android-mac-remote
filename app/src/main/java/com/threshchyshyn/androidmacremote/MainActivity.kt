package com.threshchyshyn.androidmacremote

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.threshchyshyn.androidmacremote.ui.theme.AndroidmacremoteTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothService: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setContent {
            AndroidmacremoteTheme {
                val navController: NavHostController = rememberNavController()
                val navHostEngine: NavHostEngine = rememberAnimatedNavHostEngine()
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    modifier = Modifier.fillMaxSize(),
                    engine = navHostEngine,
                    navController = navController,
                    dependenciesContainerBuilder = {
                        dependency(bluetoothService.adapter)
                    }
                )
            }
        }
    }
}
