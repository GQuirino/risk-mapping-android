package com.example.riskmapping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.riskmapping.ui.AppNavigation
import com.example.riskmapping.ui.theme.RiskMappingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiskMappingTheme {
                AppNavigation()
            }
        }
    }
}
