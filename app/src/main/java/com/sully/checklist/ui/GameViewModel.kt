package com.sully.checklist.ui

import androidx.lifecycle.ViewModel
import com.sully.checklist.logic.SetGameEngine
import com.sully.checklist.model.SetCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val engine = SetGameEngine()
    private val deck = mutableListOf<SetCard>()

    // Exposed State flows
    private val _board = MutableStateFlow<List<SetCard>>(emptyList())
    val board = _board.asStateFlow()

    private val _selectedCards = MutableStateFlow<List<SetCard>>(emptyList())
    val selectedCards = _selectedCards.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    init {
        startNewGame()
    }

    fun startNewGame() {
        deck.clear()
        deck.addAll(engine.generateDeck())
        _score.value = 0
        _selectedCards.value = emptyList()
        dealInitialBoard()
    }

    private fun dealInitialBoard() {
        val newBoard = mutableListOf<SetCard>()
        repeat(12) {
            if (deck.isNotEmpty()) {
                newBoard.add(deck.removeAt(0))
            }
        }
        _board.value = newBoard
        ensureSetExistsOrDealMore()
    }

    private fun ensureSetExistsOrDealMore() {
        // In standard rules: if no set, deal 3 more cards.
        // Repeat until a set exists or deck is empty.
        var currentBoard = _board.value.toMutableList()

        while (engine.findSet(currentBoard) == null && deck.isNotEmpty()) {
            repeat(3) {
                if (deck.isNotEmpty()) {
                    currentBoard.add(deck.removeAt(0))
                }
            }
        }
        _board.value = currentBoard
    }

    fun onCardSelected(card: SetCard) {
        val currentSelected = _selectedCards.value.toMutableList()
        if (currentSelected.contains(card)) {
            currentSelected.remove(card)
        } else {
            if (currentSelected.size < 3) {
                currentSelected.add(card)
            }
        }
        _selectedCards.value = currentSelected

        if (currentSelected.size == 3) {
            checkSet(currentSelected)
        }
    }

    private fun checkSet(selected: List<SetCard>) {
        if (engine.isSet(selected[0], selected[1], selected[2])) {
            // It's a set!
            _score.value += 1
            removeAndReplace(selected)
            _selectedCards.value = emptyList()
        } else {
            // Not a set.
            // Ideally show some visual feedback (shake/error).
            // For now, just deselect after a short delay or immediately.
            // Let's just reset selection for now, UI can handle animation if needed.
            _selectedCards.value = emptyList()
        }
    }

    private fun removeAndReplace(set: List<SetCard>) {
        val currentBoard = _board.value.toMutableList()
        val cardsToRemove = set.toSet()

        // Logic:
        // If board size > 12, just remove the cards (unless removing them leaves < 12 and deck not
        // empty - handled by ensure)
        // If board size <= 12 and deck not empty, replace with new cards.
        // If deck empty, just remove.

        val indicesToRemove = set.map { currentBoard.indexOf(it) }.sorted()

        // We will try to replace in-place if possible to maintain grid stability
        if (currentBoard.size > 12 || deck.isEmpty()) {
            currentBoard.removeAll(cardsToRemove)
        } else {
            // Replace the 3 cards with 3 from deck
            for (card in set) {
                val index = currentBoard.indexOf(card)
                if (index != -1) {
                    if (deck.isNotEmpty()) {
                        currentBoard[index] = deck.removeAt(0)
                    } else {
                        currentBoard.removeAt(index)
                    }
                }
            }
        }

        _board.value = currentBoard
        ensureSetExistsOrDealMore()
    }
}
