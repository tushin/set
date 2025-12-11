package com.sully.checklist.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.sully.checklist.model.SetCard
import com.sully.checklist.model.SetColor
import com.sully.checklist.model.SetNumber
import com.sully.checklist.model.SetShading
import com.sully.checklist.model.SetShape
import com.sully.checklist.ui.theme.CardBackgroundColor
import kotlin.math.roundToInt

@Composable
fun SetCardView(
        card: SetCard,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isHinted: Boolean = false,
        isLandscape: Boolean = false
) {
    val borderColor =
            when {
                isSelected -> Color.Blue
                isHinted -> Color.Yellow // or Color(0xFFFFD700) for Gold
                else -> Color.LightGray
            }
    val borderWidth = if (isSelected || isHinted) 3.dp else 1.dp

    // Landscape mode -> Horizontal Card (Aspect Ratio > 1, e.g., 3/2 = 1.5)
    // Portrait mode -> Vertical Card (Aspect Ratio < 1, e.g., 2/3 = 0.66)
    val cardAspectRatio = if (isLandscape) 1.5f else 0.66f

    Surface(
            modifier =
                    modifier.aspectRatio(cardAspectRatio)
                            .padding(4.dp)
                            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                            .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            color = CardBackgroundColor,
            shadowElevation = 4.dp
    ) {
        val count =
                when (card.number) {
                    SetNumber.One -> 1
                    SetNumber.Two -> 2
                    SetNumber.Three -> 3
                }

        if (isLandscape) {
            // Horizontal Card: Shapes in a Row
            Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement =
                            Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until count) {
                    ShapeView(card, modifier = Modifier.fillMaxHeight(0.6f).aspectRatio(0.6f))
                }
            }
        } else {
            // Vertical Card: Shapes in a Column
            Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (i in 0 until count) {
                    // For vertical card, shapes are wide.
                    // ShapeView usually draws a "vertical" shape in previous implementation?
                    // Let's check ShapeView. It draws based on `size`.
                    // If we want the shape to look the same but rotated?
                    // Actually, standard SET shapes are usually "Pill/Diamond/Squiggle" which are
                    // often depicted as elongated.
                    // If the card is vertical, the shapes usually run horizontally across the card?
                    // No, usually they stack vertically.
                    // Vertical Card:
                    //  [ S ]
                    //  [ S ]
                    //  [ S ]
                    // So we stack them in a Column.
                    // The ShapeView itself:
                    // In the original Horizontal card, ShapeView was
                    // `fillMaxHeight(0.6f).aspectRatio(0.6f)`.
                    // This means the shape was roughly 1:1 or slightly taller than wide? 0.6 / 1.0
                    // (height) * 0.6 (ratio) = width 0.6*height?
                    // Wait, `aspectRatio(0.6f)` means width/height = 0.6. So it's taller than wide.
                    // Existing ShapePaths (Diamond, Oval, Squiggle) seem designed for "Taller than
                    // wide" shapes.

                    // So for Vertical Card (Stacking them):
                    // We want the shape to maintain its "Taller than wide" aspect?
                    // Or should they be "Wider than tall"?
                    // Standard Set card:
                    // Vertical Card: Shapes are stacked. Each shape is "Wider than tall"?
                    // Let's look at real SET cards.
                    // Real SET cards are typically vertical. The shapes are stacked vertically.
                    // The shapes themselves are "Pill" shapes that are wider than tall.

                    // My `ShapeView` paths (Diamond, Squiggle) seem to be drawn "normalized" to
                    // size.
                    // `createDiamondPath`: width/2, 0 -> width, height/2... It draws a diamond
                    // fitting the box.

                    // If I want "Wider than tall" shapes in a Vertical Card:
                    // I should give them a wide Aspect Ratio (e.g., 1.5 or 2.0).

                    // Current `ShapeView` implementation (original) was used in a `Row`.
                    // `modifier = Modifier.fillMaxHeight(0.6f).aspectRatio(0.6f)`
                    // This meant the container for the shape was Taller than Wide.

                    // Let's try to keep the shape orientation consistent with the card orientation?
                    // If the user said "Rotate cards accordingly", maybe they mean the whole card
                    // rotates.
                    // If I rotate the card 90 degrees:
                    // Horizontal Card -> Vertical Card.
                    // The shapes inside also rotate?
                    // Yes, if you physically rotate a card, the shapes rotate.
                    // So if we have "Vertical Worms" arranged in a Row (Horizontal Card).
                    // We should have "Horizontal Worms" arranged in a Column (Vertical Card).

                    // So, for Vertical Card:
                    // Stack in Column.
                    // Shape Aspect Ratio should be inverted compared to Horizontal Card?
                    // Horizontal Card Shape: `aspectRatio(0.6f)` (Taller than wide).
                    // Vertical Card Shape: `aspectRatio(1.66f)` (Wider than tall).

                    ShapeView(card, modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1.66f))
                }
            }
        }
    }
}

@Composable
fun ShapeView(card: SetCard, modifier: Modifier = Modifier) {
    val color =
            when (card.color) {
                SetColor.Red -> Color.Red
                SetColor.Green -> Color(0xFF00AA00) // Darker green for visibility
                SetColor.Purple -> Color(0xFF800080)
            }

    Canvas(modifier = modifier) {
        val path =
                when (card.shape) {
                    SetShape.Diamond -> createDiamondPath(size)
                    SetShape.Oval -> createOvalPath(size)
                    SetShape.Squiggle -> createSquigglePath(size)
                }

        when (card.shading) {
            SetShading.Solid -> {
                drawPath(path, color)
            }
            SetShading.Open -> {
                drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
            }
            SetShading.Striped -> {
                drawPath(path, color, style = Stroke(width = 3.dp.toPx()))
                // Draw stripes
                drawStripes(path, color, size)
            }
        }
    }
}

fun DrawScope.drawStripes(path: Path, color: Color, size: Size) {
    // We can clip to the path and draw lines
    clipPath(path) {
        val spacing = 8.dp.toPx()
        for (x in 0..size.width.roundToInt() step spacing.roundToInt()) {
            // For vertical stripes. But Set usually has horizontal or diagonal shading.
            // Let's do horizontal for simplicity and clarity.
        }
        val ySpacing = 6.dp.toPx()
        for (y in 0..size.height.roundToInt() step ySpacing.roundToInt()) {
            drawLine(
                    color = color,
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1.dp.toPx()
            )
        }
    }
}

fun createDiamondPath(size: Size): Path {
    return Path().apply {
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height / 2f)
        lineTo(size.width / 2f, size.height)
        lineTo(0f, size.height / 2f)
        close()
    }
}

fun createSquigglePath(size: Size): Path {
    return Path().apply {
        val w = size.width
        val h = size.height

        // A vertical "worm" or "peanut" shape.
        // It's taller than it is wide.
        // Coordinates normalized to w, h.

        // Top-left Curve start
        moveTo(w * 0.2f, h * 0.15f)

        // Curve top
        cubicTo(w * 0.2f, h * 0.0f, w * 0.8f, h * 0.0f, w * 0.8f, h * 0.15f)

        // Right side - slight S-curve down
        cubicTo(
                w * 0.6f,
                h * 0.35f, // Indent in
                w * 1.0f,
                h * 0.65f, // Bulge out
                w * 0.8f,
                h * 0.85f // Bottom right
        )

        // Curve bottom
        cubicTo(w * 0.8f, h * 1.0f, w * 0.2f, h * 1.0f, w * 0.2f, h * 0.85f)

        // Left side - slight S-curve up (mirroring right roughly)
        cubicTo(
                w * 0.0f,
                h * 0.65f, // Bulge out (left side, so 0.0 is left)
                w * 0.4f,
                h * 0.35f, // Indent in
                w * 0.2f,
                h * 0.15f // Top left start
        )

        close()
    }
}

fun createRectanglePath(size: Size): Path {
    return Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) }
}

fun createOvalPath(size: Size): Path {
    return Path().apply {
        val cornerRadius = size.height / 2f
        // Draw a rounded rectangle with full radius to make an oval/pill shape
        // Or actually just an Oval if aspect ratio permits
        // Typically Set "Ovals" are Pill shapes.
        // Let's use a RoundRect approximation or consistent Arc logic

        // Simpler: Just standard Oval
        addOval(Rect(0f, 0f, size.width, size.height))
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 180)
@Composable
fun PreviewSetCardView(@PreviewParameter(SetCardPreviewParameterProvider::class) card: SetCard) {
    SetCardView(card = card, isSelected = false, onClick = {})
}

@Preview(showBackground = true, widthDp = 120, heightDp = 180)
@Composable
fun PreviewSelectedSetCardView(
        @PreviewParameter(SetCardPreviewParameterProvider::class) card: SetCard
) {
    SetCardView(card = card, isSelected = true, onClick = {})
}

@Preview(showBackground = true, widthDp = 100, heightDp = 50)
@Composable
fun PreviewShapeView(@PreviewParameter(SetCardPreviewParameterProvider::class) card: SetCard) {
    ShapeView(card = card, modifier = Modifier.fillMaxSize())
}

class SetCardPreviewParameterProvider : PreviewParameterProvider<SetCard> {
    override val values: Sequence<SetCard>
        get() {
            var id = 0
            return sequenceOf(
                    // Diamonds
                    SetCard(
                            id = id++,
                            color = SetColor.Red,
                            shape = SetShape.Diamond,
                            shading = SetShading.Solid,
                            number = SetNumber.One
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Green,
                            shape = SetShape.Diamond,
                            shading = SetShading.Open,
                            number = SetNumber.Two
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Purple,
                            shape = SetShape.Diamond,
                            shading = SetShading.Striped,
                            number = SetNumber.Three
                    ),
                    // Ovals
                    SetCard(
                            id = id++,
                            color = SetColor.Red,
                            shape = SetShape.Oval,
                            shading = SetShading.Solid,
                            number = SetNumber.One
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Green,
                            shape = SetShape.Oval,
                            shading = SetShading.Open,
                            number = SetNumber.Two
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Purple,
                            shape = SetShape.Oval,
                            shading = SetShading.Striped,
                            number = SetNumber.Three
                    ),
                    // Squiggles
                    SetCard(
                            id = id++,
                            color = SetColor.Red,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Solid,
                            number = SetNumber.One
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Green,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Open,
                            number = SetNumber.Two
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Purple,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Striped,
                            number = SetNumber.Three
                    ),
                    // Rectangles
                    SetCard(
                            id = id++,
                            color = SetColor.Red,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Solid,
                            number = SetNumber.One
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Green,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Open,
                            number = SetNumber.Two
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Purple,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Striped,
                            number = SetNumber.Three
                    ),
                    // Mixed examples
                    SetCard(
                            id = id++,
                            color = SetColor.Red,
                            shape = SetShape.Diamond,
                            shading = SetShading.Striped,
                            number = SetNumber.Two
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Green,
                            shape = SetShape.Oval,
                            shading = SetShading.Solid,
                            number = SetNumber.Three
                    ),
                    SetCard(
                            id = id++,
                            color = SetColor.Purple,
                            shape = SetShape.Squiggle,
                            shading = SetShading.Open,
                            number = SetNumber.One
                    ),
            )
        }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 180)
@Composable
fun PreviewAllSetCards() {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cards =
                SetCardPreviewParameterProvider()
                        .values
                        .take(3)
                        .toList() // Just take a few for a compact preview
        cards.forEach { card ->
            SetCardView(
                    card = card,
                    isSelected = false,
                    onClick = {},
                    modifier = Modifier.width(100.dp).height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 120, heightDp = 180)
@Composable
fun PreviewAllSelectedSetCards() {
    Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cards =
                SetCardPreviewParameterProvider()
                        .values
                        .take(3)
                        .toList() // Just take a few for a compact preview
        cards.forEach { card ->
            SetCardView(
                    card = card,
                    isSelected = true,
                    onClick = {},
                    modifier = Modifier.width(100.dp).height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 100, heightDp = 150)
@Composable
fun PreviewAllShapes() {
    Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cards =
                SetCardPreviewParameterProvider()
                        .values
                        .filter { it.number == SetNumber.One }
                        .take(3)
                        .toList()
        cards.forEach { card ->
            ShapeView(card = card, modifier = Modifier.width(80.dp).height(40.dp))
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
