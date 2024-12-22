package com.ivandev.cryptotracker.crypto.presentation.coin_list

import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.Result
import com.ivandev.cryptotracker.crypto.data.repository.CryptoRepository
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
}