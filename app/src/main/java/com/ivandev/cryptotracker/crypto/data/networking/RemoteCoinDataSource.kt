package com.ivandev.cryptotracker.crypto.data.networking

import com.ivandev.cryptotracker.core.data.networking.constructUrl
import com.ivandev.cryptotracker.core.data.networking.safeCall
import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.Result
import com.ivandev.cryptotracker.core.domain.util.map
import com.ivandev.cryptotracker.crypto.data.mappers.toCoin
import com.ivandev.cryptotracker.crypto.data.mappers.toCoinPrice
import com.ivandev.cryptotracker.crypto.data.networking.dto.CoinHistoryDto
import com.ivandev.cryptotracker.crypto.data.networking.dto.CoinResponseDto
import com.ivandev.cryptotracker.crypto.domain.Coin
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.domain.CoinPrice
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import java.time.ZoneId
import java.time.ZonedDateTime

class RemoteCoinDataSource (
    private val httpClient: HttpClient
): CoinDataSource {

    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        return safeCall<CoinResponseDto> {
          httpClient.get(
              urlString = constructUrl("/assets")
          )
        }.map { response ->
            response.data.map { it.toCoin() }
        }
    }

    override suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError> {
        val startMillis = start
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val endMillis = end
            .withZoneSameInstant(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()

         return safeCall<CoinHistoryDto> {
             httpClient.get(
                 urlString = constructUrl("/assets/$coinId/history")
             ){
                 parameter("interval", "h6")
                 parameter("start", startMillis)
                 parameter("end", endMillis)
             }
         }.map { response ->
             response.data.map { it.toCoinPrice() }
         }
    }
}