package com.example.compose.rally

import androidx.compose.material.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.toUpperCase
import com.example.compose.rally.ui.components.RallyTopAppBar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class TopAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule(RallyActivity::class.java)

//    @Before
//    fun setUp() {
//        val allScreens = RallyScreen.values().toList()
//
//        composeTestRule.setContent {
//            RallyTopAppBar(
//                allScreens = allScreens,
//                onTabSelected = {},
//                currentScreen = RallyScreen.Accounts
//            )
//        }
//    }

    @Test
    fun myTest() {
        composeTestRule.setContent {
            val allScreens = RallyScreen.values().toList()

            RallyTopAppBar(
                allScreens = allScreens,
                onTabSelected = { /* TODO */ },
                currentScreen = RallyScreen.Accounts
            )
        }
        Thread.sleep(5000)
    }

    @Test
    fun rallyTopAppBarTest_currentTabSelected() {
        val allScreens = RallyScreen.values().toList()

        composeTestRule.setContent {
            RallyTopAppBar(
                allScreens = allScreens,
                onTabSelected = {},
                currentScreen = RallyScreen.Accounts
            )
        }

        composeTestRule
            .onNodeWithContentDescription(RallyScreen.Accounts.name)
            .assertIsSelected()
    }

    @Test
    fun rallyTopAppBarTest_currentLabelExists() {
        val allScreens = RallyScreen.values().toList()

        composeTestRule.setContent {
            RallyTopAppBar(
                allScreens = allScreens,
                onTabSelected = {},
                currentScreen = RallyScreen.Accounts
            )
        }

//        composeTestRule.onRoot().printToLog("currentLabelExists")
//        composeTestRule.onRoot(useUnmergedTree = true).printToLog("currentLabelExists")

        composeTestRule
            .onNode(
                hasText(RallyScreen.Accounts.name.uppercase(Locale.getDefault())) and hasParent(
                    hasContentDescription(RallyScreen.Accounts.name)
                ),
                useUnmergedTree = true
            )
            .assertExists()
    }

    @Test
    fun rallyToAppBarTest_clickTabs() {
        val allScreens = RallyScreen.values().toList()
        val billsScreen = RallyScreen.Bills

        composeTestRule.setContent {
            RallyTopAppBar(
                allScreens = allScreens,
                onTabSelected = {},
                currentScreen = RallyScreen.Bills
            )
        }

        RallyScreen.values().forEach { screen ->
            composeTestRule
                .onNodeWithContentDescription(screen.name)
                .performClick()
            assert(billsScreen == screen)
        }
    }
}