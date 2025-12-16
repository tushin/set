package com.sully.checklist.model

enum class SetColor {
    Red,
    Green,
    Purple
}

enum class SetShape {
    Diamond,
    Oval,
    Squiggle
}

enum class SetShading {
    Open,
    Striped,
    Solid
}

enum class SetNumber {
    One,
    Two,
    Three
}

data class SetCard(
        val id: Int,
        val number: SetNumber,
        val color: SetColor,
        val shape: SetShape,
        val shading: SetShading
)
