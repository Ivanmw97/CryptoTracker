package com.ivandev.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ivandev.cryptotracker.R
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListState
import com.ivandev.cryptotracker.crypto.presentation.coin_list.components.SearchBar
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

    @Test
    fun searchBar_filtersCoinList() {
        composeTestRule.setContent {
            // Simulate an observable state for the search query and the filtered coin list
            var searchQuery by remember { mutableStateOf("") }
            val allCoins = listOf(
                CoinUi(
                    id = "bitcoin",
                    rank = 1,
                    name = "Bitcoin",
                    symbol = "BTC",
                    marketCapUsd = 600_000_000.0.toDisplayableNumber(),
                    priceUsd = 50_000.0.toDisplayableNumber(),
                    changePercent24Hr = 2.5.toDisplayableNumber(),
                    iconResource = R.drawable.btc,
                    isFavorite = false
                ),
                CoinUi(
                    id = "ethereum",
                    rank = 2,
                    name = "Ethereum",
                    symbol = "ETH",
                    marketCapUsd = 400_000_000.0.toDisplayableNumber(),
                    priceUsd = 3_000.0.toDisplayableNumber(),
                    changePercent24Hr = 3.0.toDisplayableNumber(),
                    iconResource = R.drawable.eth,
                    isFavorite = false
                )
            )
            val filteredCoins = allCoins.filter { it.name.contains(searchQuery, ignoreCase = true) }

            // Component under test
            Column {
                SearchBar(
                    query = searchQuery,
                    onQueryChanged = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn {
                    items(filteredCoins) { coin ->
                        Text(
                            text = coin.name,
                            modifier = Modifier.semantics { contentDescription = coin.name }
                        )
                    }
                }
            }
        }

        // Verify that both coins are displayed initially
        composeTestRule.onNodeWithContentDescription("Bitcoin").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ethereum").assertExists().assertIsDisplayed()

        // Simulate typing "Bitcoin" into the SearchBar
        composeTestRule.onNodeWithContentDescription("SearchBar")
            .performClick() // Ensure the field receives focus
            .performTextInput("Bitcoin") // Enter text

        // Verify that only "Bitcoin" is displayed after filtering
        composeTestRule.onNodeWithContentDescription("Bitcoin").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ethereum").assertDoesNotExist()

        // Clear the text in the SearchBar
        composeTestRule.onNodeWithContentDescription("SearchBar").performTextClearance()

        // Verify that both coins are displayed again
        composeTestRule.onNodeWithContentDescription("Bitcoin").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Ethereum").assertExists().assertIsDisplayed()
    }
}