package com.ivandev.cryptotracker.crypto.domain

data class Coin(
    val id: String,
    val rank: Int,
    val name: String,
    val symbol:String,
    val priceUsd: Double,
    val changePercent24Hr: Double,
    val marketCapUsd: Double
)
