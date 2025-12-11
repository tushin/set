package com.sully.checklist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sully.checklist.ui.theme.GameButtonColor
import com.sully.checklist.ui.theme.GameButtonText
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
        val board by viewModel.board.collectAsState()
        val selectedCards by viewModel.selectedCards.collectAsState()
        val score by viewModel.score.collectAsState()
        val hintCards by viewModel.hintCards.collectAsState()
        val isGameOver by viewModel.isGameOver.collectAsState()
        val deckSize by viewModel.deckSize.collectAsState()
        val shouldAnimate by viewModel.shouldAnimate.collectAsState()

        val shakeOffset = remember { Animatable(0f) }

        if (isGameOver) {
                AlertDialog(
                        onDismissRequest = { /* No dismiss, must start new game */},
                        title = { Text("Game Over") },
                        text = { Text("No more sets available.\nFinal Score: ${score * 3}") },
                        confirmButton = {
                                Button(onClick = { viewModel.startNewGame() }) { Text("New Game") }
                        }
                )
        }

        LaunchedEffect(Unit) {
                viewModel.effects.collectLatest { effect ->
                        when (effect) {
                                GameEffect.ShowError -> {
                                        // Simple shake animation
                                        for (i in 0..2) {
                                                shakeOffset.animateTo(
                                                        10f,
                                                        animationSpec = tween(50)
                                                )
                                                shakeOffset.animateTo(
                                                        -10f,
                                                        animationSpec = tween(50)
                                                )
                                        }
                                        shakeOffset.animateTo(0f, animationSpec = tween(50))
                                }
                        }
                }
        }

        BoxWithConstraints(
                modifier =
                        Modifier.fillMaxSize()
                                .background(com.sully.checklist.ui.theme.DarkGreenBackground)
        ) {
                // Draw recurring "SET" pattern on background
                SetBackgroundPattern()

                val isLandscape = maxWidth > maxHeight

                val shakeOffset = remember { Animatable(0f) }
                val safePadding = WindowInsets.safeDrawing.asPaddingValues()
                val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current

                if (isLandscape) {
                        // Landscape Layout: Dynamic Columns x 3 Rows (Left), Controls (Right)

                        // Layout Calculations
                        val constraintsMaxHeight = maxHeight
                        val constraintsMaxWidth = maxWidth
                        val topPadding = safePadding.calculateTopPadding()
                        val bottomPadding = safePadding.calculateBottomPadding()

                        // 1. Grid Logic: Align to 3 Rows fixed
                        // Columns = ceil(cardCount / 3.0)
                        val cardCount = if (board.isEmpty()) 12 else board.size
                        val rows = 3

                        // "Stable" cols for SIZE calculation (always at least 5 to prevent jitter)
                        val stableCols = kotlin.math.ceil(cardCount / 3.0).toInt().coerceAtLeast(5)
                        // "Display" cols for LAYOUT (actual columns needed: 4 for 12 cards, 5 for
                        // 15 cards)
                        val displayCols = kotlin.math.ceil(cardCount / 3.0).toInt().coerceAtLeast(1)

                        // 2. Calculate Available Space
                        // Vertical: MaxHeight - VerticalPadding - GridVerticalPadding(16dp) -
                        // Spacing
                        // Horizontal: (MaxHeight * weight?) No, we use weight 1f.
                        // But we need to know the Width available for the Grid Box.
                        // Controls take 120dp fixed. Padding 16dp * 3 (Left, Right, Mid).
                        // Grid Internal Content Padding 16dp (8+8)
                        // Safe Padding (Notch, etc) must also be subtracted as the root container
                        // has
                        // padding(safePadding)
                        // Width = MaxWidth - SafePadding(L+R) - 120.dp - 48.dp - 16.dp
                        val availableWidth =
                                constraintsMaxWidth -
                                        safePadding.calculateLeftPadding(layoutDirection) -
                                        safePadding.calculateRightPadding(layoutDirection) -
                                        120.dp -
                                        48.dp -
                                        16.dp
                        val availableHeight =
                                constraintsMaxHeight -
                                        topPadding -
                                        bottomPadding -
                                        16.dp // 16dp content padding vertical

                        // 3. Size constraints (Use stableCols for consistent sizing)
                        // Spacing
                        // Vertical Spacing: (rows - 1) * spacing
                        // Horizontal Spacing: (stableCols - 1) * spacing
                        val stableSpacingH = 8.dp * (stableCols - 1)
                        val totalSpacingV = 8.dp * (rows - 1)

                        // Candidate A: Limited by Height
                        // h = (AvalHeight - spaceV) / rows
                        // w = h * 1.5
                        val cardHeightByH = (availableHeight - totalSpacingV) / rows
                        val cardWidthByH = cardHeightByH * 1.5f

                        // Candidate B: Limited by Width (Use stableCols)
                        val cardWidthByW = (availableWidth - stableSpacingH) / stableCols
                        // val cardHeightByW = cardWidthByW / 1.5f

                        // Final decision: Use the smaller width to fit both constraints
                        val finalCardWidth =
                                kotlin.math.min(cardWidthByH.value, cardWidthByW.value).dp

                        // Calculate final grid width based on DISPLAY cols (to layout 4x4x4
                        // correctly for 12
                        // cards)
                        val displaySpacingH = 8.dp * (displayCols - 1)
                        val maxGridWidth =
                                (finalCardWidth * displayCols) +
                                        displaySpacingH +
                                        16.dp // +16dp content padding

                        Row(
                                modifier =
                                        Modifier.fillMaxSize().padding(safePadding).padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                // Game Board (Left)
                                Box(
                                        modifier = Modifier.weight(1f).fillMaxHeight(), // Remove
                                        // padding(end=16.dp) as
                                        // it's
                                        // handled by Arrangement.spacedBy
                                        contentAlignment = Alignment.Center
                                ) {
                                        LazyVerticalGrid(
                                                columns =
                                                        GridCells.Fixed(
                                                                displayCols
                                                        ), // Use Display Columns (4 for 12 cards)
                                                contentPadding = PaddingValues(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                userScrollEnabled = false,
                                                modifier =
                                                        Modifier.widthIn(max = maxGridWidth)
                                                                .graphicsLayer {
                                                                        translationX =
                                                                                shakeOffset.value
                                                                }
                                        ) {
                                                items(board, key = { it.id }) { card ->
                                                        SetCardView(
                                                                card = card,
                                                                isSelected =
                                                                        selectedCards.contains(
                                                                                card
                                                                        ),
                                                                isHinted = hintCards.contains(card),
                                                                onClick = {
                                                                        viewModel.onCardSelected(
                                                                                card
                                                                        )
                                                                },
                                                                modifier =
                                                                        if (shouldAnimate)
                                                                                Modifier.animateItem()
                                                                                        .fillMaxWidth()
                                                                        else
                                                                                Modifier.fillMaxWidth(),
                                                                isLandscape = true
                                                        )
                                                }
                                        }
                                }

                                // Controls (Right)
                                Column(
                                        modifier =
                                                Modifier.fillMaxHeight()
                                                        .width(120.dp), // Fixed width for controls
                                        verticalArrangement =
                                                Arrangement.Center, // Center vertically
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        Text(
                                                text = "${score * 3} / 81",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Column(
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                Button(
                                                        onClick = { viewModel.onDraw3Clicked() },
                                                        enabled = deckSize > 0 && board.size < 15,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText,
                                                                        disabledContainerColor =
                                                                                GameButtonColor
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        ),
                                                                        disabledContentColor =
                                                                                GameButtonText.copy(
                                                                                        alpha = 0.5f
                                                                                )
                                                                )
                                                ) { Text("Draw 3") }
                                                Button(
                                                        onClick = { viewModel.onHintClicked() },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText
                                                                )
                                                ) { Text("Hint") }
                                                Button(
                                                        onClick = { viewModel.startNewGame() },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText
                                                                )
                                                ) { Text("New Game") }
                                        }
                                }
                        }
                } else {
                        // Portrait Layout: 3 Columns x Dynamic Rows (Top), Controls (Bottom)

                        // Layout Calculations
                        val constraintsMaxHeight = maxHeight
                        val constraintsMaxWidth = maxWidth
                        val topPadding = safePadding.calculateTopPadding()
                        val bottomPadding = safePadding.calculateBottomPadding()

                        // 1. Grid Logic: Align to 3 Cols fixed
                        // Rows = ceil(cardCount / 3.0)
                        val cardCount = if (board.isEmpty()) 12 else board.size
                        val cols = 3
                        // Ensure at least 5 rows to prevent jitter when drawing cards (12 -> 15)
                        val rows = kotlin.math.ceil(cardCount / 3.0).toInt().coerceAtLeast(5)

                        // 2. Control Space
                        val bottomBarHeight = 130.dp

                        // 3. Available Space
                        // Vertical: MaxHeight - TopPad - BottomPad - BottomBar - Buffer(32dp) -
                        // GridPad(16dp)
                        val availableHeight =
                                constraintsMaxHeight -
                                        topPadding -
                                        bottomPadding -
                                        bottomBarHeight -
                                        48.dp
                        // Horizontal: MaxWidth - Padding(32dp: 16 left + 16 right) - GridPad(16dp)
                        val availableWidth = constraintsMaxWidth - 32.dp - 16.dp

                        // 4. Size Constraints
                        val totalSpacingV = 8.dp * (rows - 1)
                        val totalSpacingH = 8.dp * (cols - 1)

                        // Candidate A: Limited by Height
                        // h = (AvailH - spaceV) / rows
                        // w = h * 0.66
                        val cardHeightByH = (availableHeight - totalSpacingV) / rows
                        // val cardWidthByH = cardHeightByH * 0.66f

                        // Candidate B: Limited by Width
                        // w = (AvailW - spaceH) / cols
                        // h = w / 0.66
                        val cardWidthByW = (availableWidth - totalSpacingH) / cols
                        val cardHeightByW = cardWidthByW / 0.66f

                        // Final Decision: Use smaller Height
                        val finalCardHeight =
                                kotlin.math.min(cardHeightByH.value, cardHeightByW.value).dp
                        val finalCardWidth = finalCardHeight * 0.66f

                        // Total Grid Width
                        val maxGridWidth = (finalCardWidth * cols) + totalSpacingH + 16.dp

                        Column(
                                modifier =
                                        Modifier.fillMaxSize().padding(safePadding).padding(16.dp),
                        ) {
                                // Grid Area
                                Box(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                ) {
                                        LazyVerticalGrid(
                                                columns = GridCells.Fixed(cols), // 3 columns
                                                contentPadding = PaddingValues(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                userScrollEnabled = false,
                                                modifier =
                                                        Modifier.widthIn(max = maxGridWidth)
                                                                .graphicsLayer {
                                                                        translationX =
                                                                                shakeOffset.value
                                                                }
                                        ) {
                                                items(board, key = { it.id }) { card ->
                                                        SetCardView(
                                                                card = card,
                                                                isSelected =
                                                                        selectedCards.contains(
                                                                                card
                                                                        ),
                                                                isHinted = hintCards.contains(card),
                                                                onClick = {
                                                                        viewModel.onCardSelected(
                                                                                card
                                                                        )
                                                                },
                                                                modifier =
                                                                        if (shouldAnimate)
                                                                                Modifier.animateItem()
                                                                                        .fillMaxWidth()
                                                                        else
                                                                                Modifier.fillMaxWidth(),
                                                                isLandscape = false
                                                        )
                                                }
                                        }
                                }

                                // Bottom Controls
                                Column(
                                        modifier = Modifier.fillMaxWidth().height(bottomBarHeight),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        // Score
                                        Text(
                                                text = "${score * 3} / 81",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Buttons
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                                Button(
                                                        onClick = { viewModel.onDraw3Clicked() },
                                                        enabled = deckSize > 0 && board.size < 15,
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText,
                                                                        disabledContainerColor =
                                                                                GameButtonColor
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        ),
                                                                        disabledContentColor =
                                                                                GameButtonText.copy(
                                                                                        alpha = 0.5f
                                                                                )
                                                                )
                                                ) { Text("Draw 3") }
                                                Button(
                                                        onClick = { viewModel.onHintClicked() },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText
                                                                )
                                                ) { Text("Hint") }
                                                Button(
                                                        onClick = { viewModel.startNewGame() },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        containerColor =
                                                                                GameButtonColor,
                                                                        contentColor =
                                                                                GameButtonText
                                                                )
                                                ) { Text("New") }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun SetBackgroundPattern() {
        Canvas(modifier = Modifier.fillMaxSize()) {
                val patternText = "SET"
                val textPaint =
                        android.graphics.Paint().apply {
                                color =
                                        android.graphics.Color.parseColor(
                                                "#00332B"
                                        ) // Slightly darker than bg
                                textSize = 100f
                                textAlign = android.graphics.Paint.Align.CENTER
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                alpha = 50 // Low opacity
                        }

                val canvasWidth = size.width
                val canvasHeight = size.height
                val stepX = 300f
                val stepY = 300f

                // Draw rotated SET text in a grid pattern
                val cols = (canvasWidth / stepX).toInt() + 2
                val rows = (canvasHeight / stepY).toInt() + 2

                for (i in 0 until cols) {
                        for (j in 0 until rows) {
                                drawContext.canvas.nativeCanvas.save()
                                val x = i * stepX
                                val y = j * stepY
                                drawContext.canvas.nativeCanvas.rotate(-45f, x, y)
                                drawContext.canvas.nativeCanvas.drawText(
                                        patternText,
                                        x,
                                        y,
                                        textPaint
                                )
                                drawContext.canvas.nativeCanvas.restore()
                        }
                }
        }
}
