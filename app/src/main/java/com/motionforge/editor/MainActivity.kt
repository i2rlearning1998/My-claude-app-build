package com.motionforge.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.motionforge.editor.ui.navigation.MotionForgeNavHost
import com.motionforge.editor.ui.theme.MotionForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MotionForgeRoot()
        }
    }
}

@Composable
private fun MotionForgeRoot() {
    MotionForgeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MotionForgeNavHost()
        }
    }
}
