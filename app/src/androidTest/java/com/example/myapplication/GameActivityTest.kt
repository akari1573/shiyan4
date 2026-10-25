package com.example.myapplication

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameActivityTest {

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        PlayerInfo.resetDefaults("张可航", "男", "13800000000")
    }

    @Test
    fun game_showsInitialScoreAndFlips() {
        ActivityScenario.launch(GameActivity::class.java)
        onView(withId(R.id.tvScore))
            .check(matches(withText(targetContext.getString(R.string.game_score, 100))))
        onView(withId(R.id.tvFlips))
            .check(matches(withText(targetContext.getString(R.string.game_flips, 0))))
    }

    @Test
    fun clickingACardIncrementsFlipCounter() {
        ActivityScenario.launch(GameActivity::class.java)
        onView(withId(R.id.cardGameView)).perform(clickAtIndex(0))
        onView(withId(R.id.tvFlips))
            .check(matches(withText(targetContext.getString(R.string.game_flips, 1))))
    }

    @Test
    fun restartResetsGameState() {
        ActivityScenario.launch(GameActivity::class.java)
        onView(withId(R.id.cardGameView)).perform(clickAtIndex(0))
        onView(withId(R.id.tvFlips))
            .check(matches(withText(targetContext.getString(R.string.game_flips, 1))))
        onView(withId(R.id.btnRestart)).perform(click())
        onView(withId(R.id.tvFlips))
            .check(matches(withText(targetContext.getString(R.string.game_flips, 0))))
        onView(withId(R.id.tvScore))
            .check(matches(withText(targetContext.getString(R.string.game_score, 100))))
    }

    @Test
    fun gameWon_updatesPlayerHighScore() {
        ActivityScenario.launch(GameActivity::class.java)
        playUntilWon()
        assertEquals(100, PlayerInfo.highScore)
    }

    /** 读取 Activity 中真实模型，按配对顺序点击，保证每局都能获胜。 */
    private fun playUntilWon() {
        var realModel: GameModel? = null
        onView(withId(R.id.cardGameView)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(90)

            override fun getDescription(): String = "read game model from view"

            override fun perform(uiController: UiController, view: View) {
                realModel = (view as CardGameView).model
            }
        })
        val model = realModel ?: throw IllegalStateException("model not found")
        val values = model.cards.map { it.value }
        while (!model.isGameWon) {
            val faceDown = model.cards.indices.filter { model.cards[it].isFaceDown }
            val first = faceDown.firstOrNull() ?: break
            onView(withId(R.id.cardGameView)).perform(clickAtIndex(first))
            val second = faceDown.firstOrNull {
                it != first && values[it] == values[first]
            } ?: continue
            onView(withId(R.id.cardGameView)).perform(clickAtIndex(second))
        }
    }

    private fun clickAtIndex(index: Int): ViewAction = clickAt(
        (index % 4) / 4f + 0.125f,
        (index / 4) / 2f + 0.25f
    )

    private fun clickAt(xFraction: Float, yFraction: Float): ViewAction =
        object : ViewAction {
            override fun getConstraints(): Matcher<View> = isDisplayingAtLeast(90)

            override fun getDescription(): String =
                "click at fraction ($xFraction, $yFraction)"

            override fun perform(uiController: UiController, view: View) {
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val x = location[0] + (view.width * xFraction).toInt()
                val y = location[1] + (view.height * yFraction).toInt()
                val downTime = SystemClock.uptimeMillis()
                val down = MotionEvent.obtain(
                    downTime, downTime, MotionEvent.ACTION_DOWN, x.toFloat(), y.toFloat(), 0
                )
                val up = MotionEvent.obtain(
                    downTime, downTime + 20, MotionEvent.ACTION_UP, x.toFloat(), y.toFloat(), 0
                )
                try {
                    uiController.injectMotionEvent(down)
                    uiController.injectMotionEvent(up)
                } finally {
                    down.recycle()
                    up.recycle()
                }
            }
        }
}
