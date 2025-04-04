package com.ivandev.cryptotracker.crypto.presentation.coin_list

import androidx.compose.runtime.Immutable
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi

@Immutable
data class CoinListState(
    val isLoading: Boolean = false,
    val coins: List<CoinUi> = emptyList(),
    val filteredCoins: List<CoinUi> = emptyList(),
    val favorites: List<CoinUi> = emptyList(),
    val selectedCoin: CoinUi? = null,
)
