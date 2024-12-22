package com.ivandev.cryptotracker.crypto.data.repository

import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow

interface CryptoRepositoryInterface {
    val allCryptos: Flow<List<CryptocurrencyEntity>>

    val favoriteCryptos: Flow<List<CryptocurrencyEntity>>

    suspend fun insertCryptos(cryptos: List<CryptocurrencyEntity>)

    suspend fun updateCrypto(crypto: CryptocurrencyEntity)

    fun searchCryptos(query: String): Flow<List<CryptocurrencyEntity>>

    suspend fun addFavorite(cryptoId: String)

    suspend fun removeFavorite(cryptoId: String)
}