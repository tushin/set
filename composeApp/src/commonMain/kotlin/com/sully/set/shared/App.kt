package com.sully.set.shared

import androidx.compose.runtime.Composable
import com.sully.set.ui.GameScreen
import com.sully.set.ui.theme.CheckListTheme

@Composable
fun App() {
    CheckListTheme {
        GameScreen()
    }
}
