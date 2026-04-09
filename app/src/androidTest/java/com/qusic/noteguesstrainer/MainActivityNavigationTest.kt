package com.qusic.noteguesstrainer

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeNavigationReachesGameAndStats() {
        composeRule.onNodeWithTag("home_play").performClick()
        composeRule.onNodeWithTag("answer_grid").assertIsDisplayed()

        composeRule.onNodeWithTag("home_play")
        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithTag("home_stats").performClick()
        composeRule.onNodeWithTag("stats_accuracy").assertIsDisplayed()
    }

    @Test
    fun settingsChoicePersistsAfterNavigation() {
        composeRule.onNodeWithTag("home_settings").performClick()
        composeRule.onNodeWithTag("settings_label_full").performClick()

        composeRule.activity.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithTag("home_settings").performClick()
        composeRule.onNodeWithTag("settings_label_full").assertIsSelected()
    }
}
