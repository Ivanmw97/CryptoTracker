package com.ivandev.cryptotracker.crypto.presentation.coin_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.onError
import com.ivandev.cryptotracker.core.domain.util.onSuccess
import com.ivandev.cryptotracker.crypto.data.repository.CryptoRepositoryInterface
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.presentation.coin_detail.DataPoint
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi
import com.ivandev.cryptotracker.crypto.presentation.models.toCoinUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CoinListViewModel(
    private val coinDataSource: CoinDataSource,
    private val repository: CryptoRepositoryInterface
): ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state = _state
        .onStart { loadCoins() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            CoinListState()
        )

    private val _event = Channel<CoinListEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: CoinListAction) {
        when (action) {
            is CoinListAction.OnCoinClick -> {
                selectCoin(action.coinUi)
            }
        }
    }

    private fun selectCoin(coinUi: CoinUi) {
        _state.update { it.copy(selectedCoin = coinUi) }

        viewModelScope.launch {
            coinDataSource
                .getCoinHistory(
                    coinId = coinUi.id,
                    start = ZonedDateTime.now().minusDays(5),
                    end = ZonedDateTime.now()
                )
                .onSuccess { history ->
                    val dataPoints = history
                        .sortedBy { it.dateTime }
                        .map {
                            DataPoint(
                                x = it.dateTime.hour.toFloat(),
                                y = it.priceUsd.toFloat(),
                                xLabel = DateTimeFormatter
                                    .ofPattern("ha\nM/d")
                                    .format(it.dateTime)
                            )
                    }
                    _state.update {
                        it.copy(
                            selectedCoin = it.selectedCoin?.copy(
                                coinPriceHistory = dataPoints
                            )
                        )
                    }
                }
                .onError { error ->
                    _event.send(CoinListEvent.Error(error))
                }
        }
    }

    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Get the current favorites from Room
                val favoriteCoins = repository.favoriteCryptos.firstOrNull().orEmpty()
                val favoriteIds = favoriteCoins.map { it.id }.toSet()

                // Fetch data from the API
                coinDataSource.getCoins()
                    .onSuccess { coins ->
                        // Map the coins from the API and mark the favorites
                        val coinsUi = coins.map { coin ->
                            coin.toCoinUi().copy(isFavorite = favoriteIds.contains(coin.id))
                        }

                        // Update the complete list in the state
                        _state.update {
                            it.copy(
                                isLoading = false,
                                coins = coinsUi,
                                favorites = coinsUi.filter { it.isFavorite }
                            )
                        }
                    }
                    .onError { error ->
                        // Handle the error when loading from the API
                        _state.update { it.copy(isLoading = false) }
                        _event.send(CoinListEvent.Error(error))
                    }
            } catch (e: Exception) {
                // Handle any other errors
                _state.update { it.copy(isLoading = false) }
                _event.send(CoinListEvent.Error(NetworkError.UNKNOWN_ERROR))
            }
        }
    }

    fun toggleFavorite(coinUi: CoinUi) {
        viewModelScope.launch {
            val updatedCoin = coinUi.copy(isFavorite = !coinUi.isFavorite)

            // Update favorites in Room
            if (updatedCoin.isFavorite) {
                repository.addFavorite(updatedCoin.id)
                Log.d("CryptoTracker", "Favoritos actualizados: ${repository.favoriteCryptos.first()}")
            } else {
                repository.removeFavorite(updatedCoin.id)
            }

            // Update local state
            _state.update { currentState ->
                val updatedCoins = currentState.coins.map {
                    if (it.id == updatedCoin.id) updatedCoin else it
                }
                currentState.copy(
                    coins = updatedCoins,
                    favorites = updatedCoins.filter { it.isFavorite }
                )
            }
        }
    }
}