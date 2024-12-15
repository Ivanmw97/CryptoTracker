package com.ivandev.cryptotracker.crypto.presentation.models

import androidx.annotation.DrawableRes
import com.ivandev.cryptotracker.crypto.domain.Coin
import com.ivandev.cryptotracker.core.presentation.util.getDrawableIdForCoin
import com.ivandev.cryptotracker.crypto.presentation.coin_detail.DataPoint
import java.text.NumberFormat
import java.util.Locale

data class CoinUi(
    val id: String,
    val rank: Int,
    val name: String,
    val symbol:String,
    val marketCapUsd: DisplayableNumber,
    val priceUsd: DisplayableNumber,
    val changePercent24Hr: DisplayableNumber,
    @DrawableRes val iconResource: Int,
    val coinPriceHistory: List<DataPoint> = emptyList()
)

data class DisplayableNumber(
    val value: Double,
    val formatted: String
)

fun Coin.toCoinUi(): CoinUi {
    return CoinUi(
        id = id,
        rank = rank,
        name = name,
        symbol = symbol,
        marketCapUsd = marketCapUsd.toDisplayableNumber(),
        priceUsd = priceUsd.toDisplayableNumber(),
        changePercent24Hr = changePercent24Hr.toDisplayableNumber(),
        iconResource = getDrawableIdForCoin(symbol)
        )
}

fun Double.toDisplayableNumber(): DisplayableNumber {
    val formatted = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return DisplayableNumber(value = this, formatted = formatted.format(this))
}
