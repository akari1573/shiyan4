package com.example.myapplication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setUp() {
        PlayerInfo.resetDefaults("张可航", "男", "13800000000")
    }

    @Test
    fun mainActivity_displaysPlayerInfo() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.tvName)).check(matches(withText("张可航")))
        onView(withId(R.id.tvGender)).check(matches(withText("男")))
        onView(withId(R.id.tvPhone)).check(matches(withText("13800000000")))
        onView(withId(R.id.tvHighScore)).check(matches(withText("0")))
    }

    @Test
    fun clickEditInfo_opensInfoEditActivity() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnEditInfo)).perform(click())
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
        onView(withId(R.id.etPhone)).check(matches(isDisplayed()))
        onView(withId(R.id.rgGender)).check(matches(isDisplayed()))
    }

    @Test
    fun clickStartGame_opensGameActivity() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnStartGame)).perform(click())
        onView(withId(R.id.cardGameView)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRestart)).check(matches(isDisplayed()))
    }

    @Test
    fun editInfo_updatesMainActivity() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withId(R.id.btnEditInfo)).perform(click())
        onView(withId(R.id.etName)).perform(replaceText("李四"))
        onView(withId(R.id.etPhone)).perform(replaceText("13912345678"))
        onView(withId(R.id.rbFemale)).perform(click())
        onView(withId(R.id.btnSave)).perform(click())

        onView(withId(R.id.tvName)).check(matches(withText("李四")))
        onView(withId(R.id.tvGender)).check(matches(withText("女")))
        onView(withId(R.id.tvPhone)).check(matches(withText("13912345678")))
    }
}
