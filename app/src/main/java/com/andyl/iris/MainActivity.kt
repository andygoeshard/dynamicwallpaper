package com.andyl.iris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.andyl.iris.ui.navigation.AppNav
import com.andyl.iris.ui.theme.DynamicwallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DynamicwallpaperTheme {
                AppNav()
            }
        }
    }
}
