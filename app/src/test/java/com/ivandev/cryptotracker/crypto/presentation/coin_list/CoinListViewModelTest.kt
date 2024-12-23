package com.ivandev.cryptotracker.crypto.presentation.coin_list

import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.Result
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import com.ivandev.cryptotracker.crypto.data.repository.CryptoRepository
import com.ivandev.cryptotracker.crypto.domain.Coin
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi
import com.ivandev.cryptotracker.crypto.presentation.models.toDisplayableNumber
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinListViewModelTest {

    private lateinit var viewModel: CoinListViewModel
    private lateinit var coinDataSource: CoinDataSource
    private lateinit var repository: CryptoRepository // Mock of the repository
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set Main dispatcher for testing
        coinDataSource = mockk()
        repository = mockk() // Initialize the repository mock
        viewModel = CoinListViewModel(coinDataSource, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset Main dispatcher after the test
    }

    @Test
    fun `loadCoins should handle all NetworkErrors correctly`() = runTest(testDispatcher) {
        val errors = NetworkError.values()

        errors.forEach { error ->
            // Mock for return actual error
            coEvery { coinDataSource.getCoins() } returns Result.Error(error)

            // Trigger `loadCoins`
            viewModel::class.java.getDeclaredMethod("loadCoins").apply {
                isAccessible = true
            }.invoke(viewModel)

            advanceUntilIdle()

            // Verify state
            val state = viewModel.state.first()
            assertEquals(false, state.isLoading)
            assertEquals(emptyList<CoinUi>(), state.coins)
        }
    }

    @Test
    fun `loadCoins should sync with Room favorites`() = runTest(testDispatcher) {
        // Mock API coins
        val apiCoins = listOf(
            Coin("bitcoin", 1, "Bitcoin", "BTC", 50000.0, 600000000.0, 2.5),
            Coin("ethereum", 2, "Ethereum", "ETH", 3000.0, 300000000.0, -1.0)
        )

        // Mock Room favorites
        val favoriteEntities = listOf(
            CryptocurrencyEntity("bitcoin", "Bitcoin", "BTC", 50000.0, true)
        )

        // Configure mocks
        coEvery { coinDataSource.getCoins() } returns Result.Success(apiCoins)
        coEvery { repository.favoriteCryptos } returns flowOf(favoriteEntities)

        // Trigger loadCoins
        viewModel::class.java.getDeclaredMethod("loadCoins").apply {
            isAccessible = true
        }.invoke(viewModel)

        // Allow state to propagate
        advanceUntilIdle()

        // Fetch state after propagation
        val state = viewModel.state.first { it.coins.isNotEmpty() }

        // Assert state
        assertEquals(2, state.coins.size)
        assertEquals(true, state.coins.first { it.id == "bitcoin" }.isFavorite)
        assertEquals(false, state.coins.first { it.id == "ethereum" }.isFavorite)

        assertEquals(1, state.favorites.size)
        assertEquals("bitcoin", state.favorites.first().id)
    }

    @Test
    fun `loadCoins should handle API success and no Room favorites`() = runTest(testDispatcher) {
        // Mock API data
        val apiCoins = listOf(
            Coin("bitcoin", 1, "Bitcoin", "BTC", 50000.0, 2.5, 600000000.0)
        )

        // Mock Repository data
        coEvery { coinDataSource.getCoins() } returns Result.Success(apiCoins)
        coEvery { repository.favoriteCryptos } returns flowOf(emptyList())

        // Trigger loadCoins
        viewModel::class.java.getDeclaredMethod("loadCoins").apply {
            isAccessible = true
        }.invoke(viewModel)

        advanceUntilIdle()

        // Verify state
        val state = viewModel.state.first { it.coins.isNotEmpty() }

        // Assert coins size
        assertEquals(1, state.coins.size)

        // Assert coin details
        val coin = state.coins.first()
        assertEquals("bitcoin", coin.id)
        assertEquals(false, coin.isFavorite)
        assertEquals("Bitcoin", coin.name)
        assertEquals("BTC", coin.symbol)
        assertEquals(600000000.0.toDisplayableNumber(), coin.marketCapUsd)
        assertEquals(50000.0.toDisplayableNumber(), coin.priceUsd)
        assertEquals(2.5.toDisplayableNumber(), coin.changePercent24Hr)
    }
}