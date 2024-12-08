package com.ivandev.cryptotracker.crypto.presentation.coin_list

import com.ivandev.cryptotracker.core.domain.util.NetworkError

sealed interface CoinListEvent {
    data class Error(val error: NetworkError) : CoinListEvent
}