package com.ivandev.cryptotracker.crypto.data.repository

import android.util.Log
import com.ivandev.cryptotracker.crypto.data.local.CryptoDao
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow

class CryptoRepository(private val dao: CryptoDao): CryptoRepositoryInterface {

    override val allCryptos: Flow<List<CryptocurrencyEntity>> = dao.getAllCryptocurrencies()

    override val favoriteCryptos: Flow<List<CryptocurrencyEntity>> = dao.getFavoriteCryptocurrencies()

    override suspend fun insertCryptos(cryptos: List<CryptocurrencyEntity>) {
        dao.insertCryptocurrencies(cryptos)
    }

    override suspend fun updateCrypto(crypto: CryptocurrencyEntity) {
        dao.updateCryptocurrency(crypto)
    }

    override fun searchCryptos(query: String): Flow<List<CryptocurrencyEntity>> {
        val searchQuery = "%$query%"
        return dao.searchCryptocurrencies(searchQuery)
    }

    override suspend fun addFavorite(cryptoId: String) {
        Log.d("CryptoTracker", "Añadiendo favorito: $cryptoId")
        dao.updateFavoriteStatus(cryptoId, true)
    }

    override suspend fun removeFavorite(cryptoId: String) {
        Log.d("CryptoTracker", "Eliminando favorito: $cryptoId")
        dao.updateFavoriteStatus(cryptoId, false)
    }
}