package com.threshchyshyn.androidmacremote

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.threshchyshyn.androidmacremote.discover.DiscoverBLEScreen
import com.threshchyshyn.androidmacremote.discover.model.ScannedBleDevice
import com.threshchyshyn.androidmacremote.ui.theme.AndroidmacremoteTheme

sealed interface ScreenState {
    data class DiscoverBLE(val adapter: BluetoothAdapter) : ScreenState
    data class TogglePlaybackScreen(
        val device: ScannedBleDevice,
        val adapter: BluetoothAdapter,
    ) : ScreenState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothService: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setContent {
            var screenState by remember { mutableStateOf<ScreenState>(ScreenState.DiscoverBLE(bluetoothService.adapter)) }
            AndroidmacremoteTheme {
                AnimatedContent(
                    targetState = screenState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    label = "screen content",
                ) { targetState ->
                    when (targetState) {
                        is ScreenState.DiscoverBLE -> DiscoverBLEScreen(
                            bluetoothService.adapter,
                            onDeviceSelected = { device ->
                                screenState = ScreenState.TogglePlaybackScreen(device, bluetoothService.adapter)
                            },
                        )

                        is ScreenState.TogglePlaybackScreen -> TogglePlaybackScreen(
                            scannedBleDevice = targetState.device,
                        )
                    }

                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AndroidmacremoteTheme {
        Greeting("Android")
    }
}
