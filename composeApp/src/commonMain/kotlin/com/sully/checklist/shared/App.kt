package com.sully.checklist.shared

import androidx.compose.runtime.Composable
import com.sully.checklist.ui.GameScreen
import com.sully.checklist.ui.theme.CheckListTheme

@Composable
fun App() {
    CheckListTheme {
        GameScreen()
    }
}
