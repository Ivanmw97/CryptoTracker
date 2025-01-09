package com.ivandev.cryptotracker.crypto.presentation.coin_list

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
import com.ivandev.cryptotracker.crypto.presentation.models.toEntity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        // Observe the repository's favorites reactively
        viewModelScope.launch {
            repository.favoriteCryptos.collect { favoriteCoins ->
                val favoriteIds = favoriteCoins.map { it.id }.toSet()

                // Update the state of coins marked as favorites
                _state.update { currentState ->
                    val updatedCoins = currentState.coins.map { coin ->
                        coin.copy(isFavorite = favoriteIds.contains(coin.id))
                    }
                    currentState.copy(
                        coins = updatedCoins,
                        favorites = updatedCoins.filter { it.isFavorite }
                    )
                }
            }
        }
        // Combine the search query with the coin list to update the filtered state
        viewModelScope.launch {
            combine(_searchQuery, _state) { query, state ->
                if (query.isEmpty()) {
                    state.coins
                } else {
                    state.coins.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.symbol.contains(query, ignoreCase = true)
                    }
                }
            }.collect { filteredCoins ->
                _state.update { it.copy(filteredCoins = filteredCoins) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

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
                // Retrieve the favorite coins from the database
                val favoriteCoins = repository.favoriteCryptos.firstOrNull().orEmpty()
                val favoriteIds = favoriteCoins.map { it.id }.toSet()

                // Fetch coins from the API
                coinDataSource.getCoins()
                    .onSuccess { coins ->
                        val coinsUi = coins.map { coin ->
                            coin.toCoinUi(isFavorite = favoriteIds.contains(coin.id))
                        }

                        // Save all coins to the database (including favorites)
                        repository.insertCryptos(coinsUi.map { it.toEntity() })

                        // Update the state with the coins
                        _state.update {
                            it.copy(
                                isLoading = false,
                                coins = coinsUi,
                                favorites = coinsUi.filter { it.isFavorite }
                            )
                        }
                    }
                    .onError { error ->
                        _state.update { it.copy(isLoading = false) }
                        _event.send(CoinListEvent.Error(error))
                    }
            } catch (e: Exception) {
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
            } else {
                repository.removeFavorite(updatedCoin.id)
            }

            // Update local state
            _state.update { currentState ->
                val updatedCoins = currentState.coins.map {
                    if (it.id == updatedCoin.id) updatedCoin else it
                }
                // Also update the `selectedCoin`
                val updatedSelectedCoin = if (currentState.selectedCoin?.id == updatedCoin.id) {
                    updatedCoin
                } else {
                    currentState.selectedCoin
                }

                currentState.copy(
                    coins = updatedCoins,
                    favorites = updatedCoins.filter { it.isFavorite },
                    selectedCoin = updatedSelectedCoin
                )
            }
        }
    }

    // Update the filtered list based on the search
}