package com.ivandev.cryptotracker.crypto.presentation.coin_list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.ivandev.cryptotracker.core.domain.util.NetworkError
import com.ivandev.cryptotracker.core.domain.util.Result
import com.ivandev.cryptotracker.crypto.data.models.CryptocurrencyEntity
import com.ivandev.cryptotracker.crypto.data.repository.CryptoRepositoryInterface
import com.ivandev.cryptotracker.crypto.domain.Coin
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.domain.CoinPrice
import com.ivandev.cryptotracker.crypto.presentation.coin_list.components.CoinListItem
import com.ivandev.cryptotracker.crypto.presentation.coin_list.components.SearchBar
import com.ivandev.cryptotracker.crypto.presentation.coin_list.components.previewCoin
import com.ivandev.cryptotracker.crypto.presentation.models.CoinUi
import com.ivandev.cryptotracker.ui.theme.CryptoTrackerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime

@Composable
fun CoinListScreen(
    state: CoinListState,
    onAction: (CoinListAction) -> Unit,
    onToggleFavorite: (CoinUi) -> Unit,
    viewModel: CoinListViewModel,
    modifier: Modifier = Modifier
) {
    Column {
        SearchBar(
            query = viewModel.searchQuery.collectAsState().value,
            onQueryChanged = viewModel::onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
        )
        if (state.isLoading) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredCoins) { coinUi ->
                    CoinListItem(
                        coinUi = coinUi,
                        onClick = {
                            onAction(CoinListAction.OnCoinClick(coinUi))
                        },
                        onToggleFavorite = onToggleFavorite,
                        modifier = Modifier.fillMaxSize()
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun CoinListScreenPreview() {
    CryptoTrackerTheme {
        val mockCoinDataSource = object : CoinDataSource {
            override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
                return Result.Success(
                    (1..100).map { id ->
                        Coin(
                            id = id.toString(),
                            name = "Coin $id",
                            rank = id,
                            symbol = "C$id",
                            priceUsd = 100.0 + id,
                            changePercent24Hr = (id % 5) - 2.5,
                            marketCapUsd = 1_000_000.0 + id * 10
                        )
                    }
                )
            }

            override suspend fun getCoinHistory(
                coinId: String,
                start: ZonedDateTime,
                end: ZonedDateTime
            ): Result<List<CoinPrice>, NetworkError> {
                return Result.Success(
                    (1..10).map { index ->
                        CoinPrice(
                            dateTime = start.plusHours(index.toLong()),
                            priceUsd = 100.0 + index
                        )
                    }
                )
            }
        }

        val mockRepository = object : CryptoRepositoryInterface {
            override val allCryptos = flowOf(emptyList<CryptocurrencyEntity>())
            override val favoriteCryptos = flowOf(emptyList<CryptocurrencyEntity>())
            override suspend fun insertCryptos(cryptos: List<CryptocurrencyEntity>) {}
            override suspend fun updateCrypto(crypto: CryptocurrencyEntity) {}
            override fun searchCryptos(query: String): Flow<List<CryptocurrencyEntity>> {
                return flowOf(emptyList())
            }
            override suspend fun addFavorite(cryptoId: String) {}
            override suspend fun removeFavorite(cryptoId: String) {}
        }

        val viewModel = CoinListViewModel(
            coinDataSource = mockCoinDataSource,
            repository = mockRepository
        )

        CoinListScreen(
            state = CoinListState(
                coins = (1..100).map {
                    previewCoin.copy(id = it.toString())
                }
            ),
            viewModel = viewModel,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            onAction = {},
            onToggleFavorite = {}
        )
    }
}