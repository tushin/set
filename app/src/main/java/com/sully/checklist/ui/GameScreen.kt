package com.sully.checklist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val board by viewModel.board.collectAsState()
    val selectedCards by viewModel.selectedCards.collectAsState()
    val score by viewModel.score.collectAsState()
    val hintCards by viewModel.hintCards.collectAsState()

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                GameEffect.ShowError -> {
                    // Simple shake animation
                    for (i in 0..2) {
                        shakeOffset.animateTo(10f, animationSpec = tween(50))
                        shakeOffset.animateTo(-10f, animationSpec = tween(50))
                    }
                    shakeOffset.animateTo(0f, animationSpec = tween(50))
                }
            }
        }
    }

    Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "${score * 3} / 81",
                            style = MaterialTheme.typography.headlineMedium
                    )
                }
            },
            bottomBar = {
                Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    val deckSize by viewModel.deckSize.collectAsState()
                    Button(
                            onClick = { viewModel.onDraw3Clicked() },
                            enabled = deckSize > 0 && board.size < 15
                    ) { Text("Draw 3") }
                    Button(onClick = { viewModel.onHintClicked() }) { Text("Hint") }
                    Button(onClick = { viewModel.startNewGame() }) { Text("New Game") }
                }
            }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier =
                            Modifier.weight(1f).graphicsLayer { translationX = shakeOffset.value }
            ) {
                items(board, key = { it.id }) { card ->
                    SetCardView(
                            card = card,
                            isSelected = selectedCards.contains(card),
                            isHinted = hintCards.contains(card),
                            onClick = { viewModel.onCardSelected(card) },
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
