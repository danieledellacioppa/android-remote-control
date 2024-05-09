package com.forteur.androidremotecontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.forteur.androidremotecontroller.ui.composable.TermuxInterface
import com.forteur.androidremotecontroller.ui.theme.AndroidRemoteControllerTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: TermuxViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TermuxViewModel::class.java]

        window.navigationBarColor = getColor(R.color.status_bar_color)

        setContent {
            AndroidRemoteControllerTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Gray) {
                    TermuxInterface(viewModel)
                }
            }
        }
    }
}