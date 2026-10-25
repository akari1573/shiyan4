package com.example.myapplication

import android.os.Handler
import android.os.Looper

/**
 * 翻牌游戏控制器（Controller）：接收用户的点击交互，调用模型处理逻辑并通知界面刷新。
 */
class GameController(
    private val model: GameModel,
    private val listener: GameListener
) {

    interface GameListener {
        fun onGameStateChanged()
        fun onGameWon(score: Int)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var firstIndex = -1
    private var isLocked = false
    private var pendingRunnable: Runnable? = null

    fun onCardClick(index: Int) {
        if (isLocked || model.isGameWon) return

        if (firstIndex == -1) {
            if (model.flip(index)) {
                firstIndex = index
                listener.onGameStateChanged()
            }
        } else {
            if (index == firstIndex) return
            if (model.flip(index)) {
                listener.onGameStateChanged()
                val secondIndex = index
                if (model.checkMatch(firstIndex, secondIndex)) {
                    firstIndex = -1
                    if (model.isGameWon) {
                        listener.onGameWon(model.computeScore())
                    }
                } else {
                    isLocked = true
                    val runnable = Runnable {
                        model.flipBack(firstIndex, secondIndex)
                        firstIndex = -1
                        isLocked = false
                        listener.onGameStateChanged()
                    }
                    pendingRunnable = runnable
                    handler.postDelayed(runnable, FLIP_BACK_DELAY_MS)
                }
            }
        }
    }

    fun restart() {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        pendingRunnable = null
        firstIndex = -1
        isLocked = false
        model.reset()
        listener.onGameStateChanged()
    }

    fun onDestroy() {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        pendingRunnable = null
    }

    companion object {
        private const val FLIP_BACK_DELAY_MS = 800L
    }
}
