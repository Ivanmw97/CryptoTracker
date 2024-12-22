package com.ivandev.cryptotracker.crypto.data.local

import androidx.room.*
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {

    @Query("SELECT * FROM cryptocurrencies ORDER BY name ASC")
    fun getAllCryptocurrencies(): Flow<List<CryptocurrencyEntity>>

    @Query("SELECT * FROM cryptocurrencies WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCryptocurrencies(): Flow<List<CryptocurrencyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCryptocurrencies(cryptos: List<CryptocurrencyEntity>)

    @Update
    suspend fun updateCryptocurrency(crypto: CryptocurrencyEntity)

    @Query("SELECT * FROM cryptocurrencies WHERE name LIKE :searchQuery OR symbol LIKE :searchQuery ORDER BY name ASC")
    fun searchCryptocurrencies(searchQuery: String): Flow<List<CryptocurrencyEntity>>

    @Query("DELETE FROM cryptocurrencies WHERE id = :cryptoId")
    suspend fun deleteCryptocurrencyById(cryptoId: String)

    @Query("UPDATE cryptocurrencies SET isFavorite = :isFavorite WHERE id = :cryptoId")
    suspend fun updateFavoriteStatus(cryptoId: String, isFavorite: Boolean)
}