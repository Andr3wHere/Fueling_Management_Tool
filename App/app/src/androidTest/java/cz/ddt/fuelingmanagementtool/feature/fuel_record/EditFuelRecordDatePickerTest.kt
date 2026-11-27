package cz.ddt.fuelingmanagementtool.feature.fuel_record

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.ddt.fuelingmanagementtool.ui.theme.FuelingManagementToolTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditFuelRecordDatePickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysPrefilledDateHeadingAndSavesIsoDate() {
        var savedDate: String? = null

        composeTestRule.setContent {
            FuelingManagementToolTheme(dynamicColor = false) {
                EditFuelRecordScreen(
                    initialDate = "18.10.2026",
                    initialLiters = "42,5",
                    initialPrice = "1520",
                    onSave = { date, _, _, _ -> savedDate = date }
                )
            }
        }

        composeTestRule.onNodeWithText("Datum").assertIsDisplayed()
        composeTestRule.onNodeWithText("18.10.2026").assertIsDisplayed()

        composeTestRule.onNodeWithText("Uložit tankování")
            .performScrollTo()
            .performClick()

        assertEquals("2026-10-18", savedDate)
    }

    @Test
    fun clickingDateFieldOpensAndDismissesDatePickerDialog() {
        composeTestRule.setContent {
            FuelingManagementToolTheme(dynamicColor = false) {
                EditFuelRecordScreen(
                    initialDate = "18.10.2026",
                    initialLiters = "42,5",
                    initialPrice = "1520"
                )
            }
        }

        composeTestRule.onNodeWithText("18.10.2026")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Potvrdit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zrušit").assertIsDisplayed()

        composeTestRule.onNodeWithText("Zrušit").performClick()

        composeTestRule.onNodeWithText("Potvrdit").assertDoesNotExist()
        composeTestRule.onNodeWithText("18.10.2026").assertIsDisplayed()
    }

    @Test
    fun confirmingDatePickerDialogClosesDialogAndPreservesDateForSave() {
        var savedDate: String? = null

        composeTestRule.setContent {
            FuelingManagementToolTheme(dynamicColor = false) {
                EditFuelRecordScreen(
                    initialDate = "18.10.2026",
                    initialLiters = "42,5",
                    initialPrice = "1520",
                    onSave = { date, _, _, _ -> savedDate = date }
                )
            }
        }

        composeTestRule.onNodeWithText("18.10.2026")
            .performScrollTo()
            .performClick()

        composeTestRule.onNodeWithText("Potvrdit").performClick()
        composeTestRule.onNodeWithText("Potvrdit").assertDoesNotExist()

        composeTestRule.onNodeWithText("Uložit tankování")
            .performScrollTo()
            .performClick()

        assertEquals("2026-10-18", savedDate)
    }
}
