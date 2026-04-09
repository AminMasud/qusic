package com.qusic.noteguesstrainer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.qusic.noteguesstrainer.data.NoteCatalog
import com.qusic.noteguesstrainer.model.NoteId
import com.qusic.noteguesstrainer.model.RoundFeedback
import com.qusic.noteguesstrainer.ui.screens.GameScreen
import com.qusic.noteguesstrainer.ui.theme.NoteGuessTrainerTheme
import com.qusic.noteguesstrainer.viewmodel.GameUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun answerButtonsAndFeedbackAreVisible() {
        composeRule.setContent {
            NoteGuessTrainerTheme {
                GameScreen(
                    uiState = GameUiState(
                        currentRoundNote = NoteId.C4,
                        unlockedNotes = listOf(NoteId.C4, NoteId.D4, NoteId.E4),
                        feedback = RoundFeedback(
                            message = "Correct!",
                            isCorrect = true,
                            correctAnswer = NoteId.C4,
                        ),
                        canAnswer = true,
                        canReplay = true,
                    ),
                    onPlayNote = {},
                    onReplayNote = {},
                    onAnswerSelected = {},
                    onDismissUnlockDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag("answer_C4").assertIsDisplayed()
        composeRule.onNodeWithTag("answer_D4").assertIsDisplayed()
        composeRule.onNodeWithTag("answer_E4").assertIsDisplayed()
        composeRule.onNodeWithTag("feedback_banner").assertIsDisplayed()
        composeRule.onNodeWithText("Correct!").assertIsDisplayed()
    }

    @Test
    fun unlockDialogAppearsWhenUnlockNoteExists() {
        composeRule.setContent {
            NoteGuessTrainerTheme {
                GameScreen(
                    uiState = GameUiState(
                        currentRoundNote = NoteId.D4,
                        unlockedNotes = NoteCatalog.initialUnlocked,
                        unlockDialogNote = NoteId.E4,
                        canReplay = true,
                    ),
                    onPlayNote = {},
                    onReplayNote = {},
                    onAnswerSelected = {},
                    onDismissUnlockDialog = {},
                )
            }
        }

        composeRule.onNodeWithTag("unlock_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("New Note Unlocked").assertIsDisplayed()
        composeRule.onNodeWithText("Keep Training").assertIsDisplayed()
    }
}
