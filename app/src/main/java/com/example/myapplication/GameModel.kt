package com.example.myapplication

/**
 * 翻牌游戏模型（Model）：维护所有卡牌状态，并提供翻牌、配对、重置等业务逻辑。
 */
class GameModel(private val pairCount: Int = 4) {

    enum class CardState { FACE_DOWN, FACE_UP, MATCHED }

    class Card(val value: Int) {
        var state: CardState = CardState.FACE_DOWN
            internal set

        val isFaceDown: Boolean get() = state == CardState.FACE_DOWN
        val isFaceUp: Boolean get() = state == CardState.FACE_UP
        val isMatched: Boolean get() = state == CardState.MATCHED
    }

    var flips: Int = 0
        private set
    var matchedPairs: Int = 0
        private set

    var cards: List<Card> = emptyList()
        private set

    init {
        reset()
    }

    /** 翻开一张牌，翻牌次数加一；牌面已翻开或已配对时返回 false。 */
    fun flip(index: Int): Boolean {
        if (isGameWon) return false
        val card = cards.getOrNull(index) ?: return false
        if (!card.isFaceDown) return false
        card.state = CardState.FACE_UP
        flips++
        return true
    }

    /** 检查两张翻开牌是否配对，配对则将它们标记为已配对。 */
    fun checkMatch(firstIndex: Int, secondIndex: Int): Boolean {
        val first = cards[firstIndex]
        val second = cards[secondIndex]
        if (first.value == second.value) {
            first.state = CardState.MATCHED
            second.state = CardState.MATCHED
            matchedPairs++
            return true
        }
        return false
    }

    /** 将指定牌翻回背面（已配对的牌不受影响）。 */
    fun flipBack(vararg indices: Int) {
        indices.forEach { index ->
            val card = cards.getOrNull(index) ?: return@forEach
            if (card.isFaceUp) {
                card.state = CardState.FACE_DOWN
            }
        }
    }

    val isGameWon: Boolean get() = matchedPairs == pairCount

    /** 根据翻牌次数计算得分：最少翻牌次数得满分100，每多翻一次扣5分。 */
    fun computeScore(): Int {
        val minFlips = pairCount * 2
        val extraFlips = (flips - minFlips).coerceAtLeast(0)
        return (100 - extraFlips * 5).coerceAtLeast(0)
    }

    /** 重置一局：重新洗牌、清空计数。 */
    fun reset() {
        val values = (1..pairCount).flatMap { listOf(it, it) }.shuffled()
        cards = values.map { Card(it) }
        flips = 0
        matchedPairs = 0
    }
}
