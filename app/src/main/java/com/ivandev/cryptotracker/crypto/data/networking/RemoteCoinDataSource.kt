package com.ivandev.cryptotracker.crypto.data.networking

import com.ivandev.cryptotracker.core.data.networking.constructUrl
import com.ivandev.cryptotracker.core.data.networking.safeCall
import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.Result
import com.ivandev.cryptotracker.core.domain.util.map
import com.ivandev.cryptotracker.crypto.data.mappers.toCoin
import com.ivandev.cryptotracker.crypto.data.networking.dto.CoinResponseDto
import com.ivandev.cryptotracker.crypto.domain.Coin
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get

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
}