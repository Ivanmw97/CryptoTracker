package com.ivandev.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ivandev.cryptotracker.R
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListState
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi
import com.ivandev.cryptotracker.crypto.presentation.models.toDisplayableNumber
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CoinDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun toggleFavorite_updatesIcon() {
        composeTestRule.setContent {
            // Observable state
            var isFavorite by remember { mutableStateOf(false) }

            CoinDetailScreen(
                state = CoinListState(
                    selectedCoin = CoinUi(
                        id = "bitcoin",
                        rank = 1,
                        name = "Bitcoin",
                        symbol = "BTC",
                        marketCapUsd = 600000000.0.toDisplayableNumber(),
                        priceUsd = 50000.0.toDisplayableNumber(),
                        changePercent24Hr = 2.5.toDisplayableNumber(),
                        iconResource = R.drawable.btc,
                        isFavorite = isFavorite
                    )
                ),
                onToggleFavorite = {
                    isFavorite = !isFavorite // Update the observable state
                }
            )
        }

        // Verify that the initial state is "Favorite Border Icon"
        composeTestRule.onNodeWithContentDescription("Favorite Border Icon")
            .assertExists()
            .assertIsDisplayed()

        // Simulate the click to toggle to favorite
        composeTestRule.onNodeWithContentDescription("Favorite Border Icon").performClick()

        // Verify that the icon was updated to "Favorite Icon"
        composeTestRule.onNodeWithContentDescription("Favorite Icon")
            .assertExists()
            .assertIsDisplayed()
    }
}