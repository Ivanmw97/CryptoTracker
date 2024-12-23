package com.ivandev.cryptotracker.crypto.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class CryptoDaoTest {

    private lateinit var database: CryptoDatabase
    private lateinit var dao: CryptoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CryptoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.cryptoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetFavorites() = runBlocking {
        // Arrange
        val crypto = CryptocurrencyEntity(
            id = "bitcoin",
            name = "Bitcoin",
            symbol = "BTC",
            price = 20000.0,
            isFavorite = true
        )
        dao.insertCryptocurrencies(listOf(crypto))

        // Act
        val favorites = dao.getFavoriteCryptocurrencies()

        // Assert
        val result = favorites.first()
        assertEquals(1, result.size)
        assertEquals(crypto.id, result[0].id)
        assertTrue(result[0].isFavorite)
    }

    @Test
    fun updateFavoriteStatus() = runBlocking {
        // Arrange
        val crypto = CryptocurrencyEntity(
            id = "ethereum",
            name = "Ethereum",
            symbol = "ETH",
            price = 1500.0,
            isFavorite = false
        )
        dao.insertCryptocurrencies(listOf(crypto))

        // Act
        dao.updateFavoriteStatus("ethereum", true)
        val favorites = dao.getFavoriteCryptocurrencies()

        // Assert
        val result = favorites.first()
        assertEquals(1, result.size)
        assertEquals("ethereum", result[0].id)
        assertTrue(result[0].isFavorite)
    }
}