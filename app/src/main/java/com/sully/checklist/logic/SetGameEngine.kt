package com.sully.checklist.logic

import com.sully.checklist.model.SetCard
import com.sully.checklist.model.SetColor
import com.sully.checklist.model.SetNumber
import com.sully.checklist.model.SetShading
import com.sully.checklist.model.SetShape

class SetGameEngine {

    private val deck = CopiedDeck.toMutableList()

    // Helper to reset efficiently
    private object CopiedDeck {
        private val cards = mutableListOf<SetCard>()
        init {
            var idCounter = 0
            for (number in SetNumber.values()) {
                for (color in SetColor.values()) {
                    for (shape in SetShape.values()) {
                        for (shading in SetShading.values()) {
                            cards.add(SetCard(idCounter++, number, color, shape, shading))
                        }
                    }
                }
            }
        }
        fun toMutableList() = cards.toMutableList()
    }

    fun generateDeck(): List<SetCard> {
        return deck.shuffled()
    }

    /**
     * Checks if 3 cards form a valid Set. Rule: For each feature (Number, Color, Shape, Shading),
     * the three cards must all have the same value OR all have different values.
     */
    fun isSet(c1: SetCard, c2: SetCard, c3: SetCard): Boolean {
        return checkFeature(c1.number, c2.number, c3.number) &&
                checkFeature(c1.color, c2.color, c3.color) &&
                checkFeature(c1.shape, c2.shape, c3.shape) &&
                checkFeature(c1.shading, c2.shading, c3.shading)
    }

    private fun <T> checkFeature(v1: T, v2: T, v3: T): Boolean {
        val allSame = (v1 == v2 && v2 == v3)
        val allDifferent = (v1 != v2 && v1 != v3 && v2 != v3)
        return allSame || allDifferent
    }

    /** Finds a set in the given list of cards if one exists. */
    fun findSet(cards: List<SetCard>): List<SetCard>? {
        val n = cards.size
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                for (k in j + 1 until n) {
                    val c1 = cards[i]
                    val c2 = cards[j]
                    val c3 = cards[k]
                    if (isSet(c1, c2, c3)) {
                        return listOf(c1, c2, c3)
                    }
                }
            }
        }
        return null
    }
}
