package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameModelTest {

    @Test
    fun initializesWithEightFaceDownCards() {
        val model = GameModel(4)
        assertEquals(8, model.cards.size)
        assertTrue(model.cards.all { it.isFaceDown })
        assertEquals(0, model.flips)
        assertEquals(0, model.matchedPairs)
        assertFalse(model.isGameWon)
    }

    @Test
    fun everyValueAppearsExactlyTwice() {
        val model = GameModel(4)
        val groups = model.cards.groupBy { it.value }
        assertEquals(4, groups.size)
        assertTrue(groups.values.all { it.size == 2 })
    }

    @Test
    fun flipTurnsCardFaceUpAndCounts() {
        val model = GameModel(4)
        assertTrue(model.flip(0))
        assertTrue(model.cards[0].isFaceUp)
        assertEquals(1, model.flips)
    }

    @Test
    fun cannotFlipFaceUpOrMatchedCard() {
        val model = GameModel(4)
        assertTrue(model.flip(0))
        assertFalse(model.flip(0))
        assertFalse(model.flip(-1))
        assertFalse(model.flip(8))
        assertEquals(1, model.flips)

        val pairIndex = model.cards.indices.first {
            it != 0 && model.cards[it].value == model.cards[0].value
        }
        assertTrue(model.flip(pairIndex))
        assertTrue(model.checkMatch(0, pairIndex))
        assertTrue(model.cards[0].isMatched)
        assertFalse(model.flip(0))
        assertFalse(model.flip(pairIndex))
    }

    @Test
    fun matchingPairBecomesMatched() {
        val model = GameModel(4)
        val pairIndex = model.cards.indices.first {
            it != 0 && model.cards[it].value == model.cards[0].value
        }
        model.flip(0)
        model.flip(pairIndex)
        assertTrue(model.checkMatch(0, pairIndex))
        assertTrue(model.cards[0].isMatched)
        assertTrue(model.cards[pairIndex].isMatched)
        assertEquals(1, model.matchedPairs)
    }

    @Test
    fun mismatchedPairFlipsBack() {
        val model = GameModel(4)
        val otherIndex = model.cards.indices.first {
            model.cards[it].value != model.cards[0].value
        }
        model.flip(0)
        model.flip(otherIndex)
        assertFalse(model.checkMatch(0, otherIndex))
        model.flipBack(0, otherIndex)
        assertTrue(model.cards[0].isFaceDown)
        assertTrue(model.cards[otherIndex].isFaceDown)
        assertEquals(0, model.matchedPairs)
    }

    @Test
    fun flipBackDoesNotAffectMatchedCards() {
        val model = GameModel(4)
        val pairIndex = model.cards.indices.first {
            it != 0 && model.cards[it].value == model.cards[0].value
        }
        model.flip(0)
        model.flip(pairIndex)
        assertTrue(model.checkMatch(0, pairIndex))
        model.flipBack(0, pairIndex)
        assertTrue(model.cards[0].isMatched)
        assertTrue(model.cards[pairIndex].isMatched)
    }

    @Test
    fun gameWonAfterAllPairsMatched() {
        val model = GameModel(4)
        val values = model.cards.map { it.value }
        while (!model.isGameWon) {
            val faceDown = model.cards.indices.filter { model.cards[it].isFaceDown }
            val first = faceDown.firstOrNull() ?: break
            assertTrue(model.flip(first))
            val second = faceDown.firstOrNull {
                it != first && values[it] == values[first]
            } ?: break
            assertTrue(model.flip(second))
            assertTrue(model.checkMatch(first, second))
        }
        assertTrue(model.isGameWon)
    }

    @Test
    fun perfectPlayScoresOneHundred() {
        val model = GameModel(4)
        playPerfectly(model)
        assertEquals(100, model.computeScore())
    }

    @Test
    fun extraFlipsReduceScoreButNeverNegative() {
        val model = GameModel(4)
        val values = model.cards.map { it.value }
        val otherIndex = model.cards.indices.first { values[it] != values[0] }
        model.flip(0)
        model.flip(otherIndex)
        assertFalse(model.checkMatch(0, otherIndex))
        model.flipBack(0, otherIndex)
        playPerfectly(model)
        assertEquals(90, model.computeScore())
    }

    @Test
    fun scoreNeverGoesNegative() {
        val model = GameModel(4)
        val values = model.cards.map { it.value }
        while (!model.isGameWon) {
            val faceDown = model.cards.indices.filter { model.cards[it].isFaceDown }
            val first = faceDown.firstOrNull() ?: break
            model.flip(first)
            for (other in faceDown) {
                if (other == first) continue
                if (model.flip(other)) {
                    if (model.checkMatch(first, other)) break
                    model.flipBack(first, other)
                }
            }
        }
        assertTrue(model.isGameWon)
        assertTrue(model.computeScore() >= 0)
    }

    @Test
    fun resetRestoresInitialState() {
        val model = GameModel(4)
        val pairIndex = model.cards.indices.first {
            it != 0 && model.cards[it].value == model.cards[0].value
        }
        model.flip(0)
        model.flip(pairIndex)
        model.checkMatch(0, pairIndex)
        model.reset()
        assertEquals(0, model.flips)
        assertEquals(0, model.matchedPairs)
        assertTrue(model.cards.all { it.isFaceDown })
        assertFalse(model.isGameWon)
    }

    private fun playPerfectly(model: GameModel) {
        val values = model.cards.map { it.value }
        while (!model.isGameWon) {
            val faceDown = model.cards.indices.filter { model.cards[it].isFaceDown }
            val first = faceDown.firstOrNull() ?: break
            if (!model.flip(first)) break
            val second = faceDown.firstOrNull {
                it != first && values[it] == values[first]
            } ?: break
            if (!model.flip(second)) break
            model.checkMatch(first, second)
        }
    }
}
