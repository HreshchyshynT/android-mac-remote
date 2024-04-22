package com.threshchyshyn.androidmacremote

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.threshchyshyn.androidmacremote.discover.DiscoverBLEScreen
import com.threshchyshyn.androidmacremote.ui.theme.AndroidmacremoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothService: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        setContent {
            AndroidmacremoteTheme {
                DiscoverBLEScreen(bluetoothService.adapter)
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
