package com.ivandev.cryptotracker.crypto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity

@Database(entities = [CryptocurrencyEntity::class], version = 2, exportSchema = true) // Increment the version to 2 for applying updates
abstract class CryptoDatabase : RoomDatabase() {

    abstract fun cryptoDao(): CryptoDao

    companion object {
        @Volatile
        private var INSTANCE: CryptoDatabase? = null

        fun getInstance(context: Context): CryptoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CryptoDatabase::class.java,
                    "crypto_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        // Migration from version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the `isFavorite` column with a default value of `false` (0 in SQLite)
                database.execSQL("ALTER TABLE cryptocurrencies ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}