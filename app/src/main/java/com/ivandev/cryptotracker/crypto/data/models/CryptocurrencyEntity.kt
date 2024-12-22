package com.ivandev.cryptotracker.crypto.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cryptocurrencies")
data class CryptocurrencyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val price: Double,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean
)