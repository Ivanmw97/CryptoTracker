@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
package com.ivandev.cryptotracker.core.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ivandev.cryptotracker.core.presentation.util.ObserveAsEvents
import com.ivandev.cryptotracker.core.presentation.util.toString
import com.ivandev.cryptotracker.crypto.presentation.coin_detail.CoinDetailScreen
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListAction
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListEvent
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListScreen
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListViewModel
import com.ivandev.cryptotracker.crypto.presentation.coin_list.components.BottomNavigationBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdaptiveCoinListDetailPane(
    modifier: Modifier = Modifier,
    viewModel: CoinListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    ObserveAsEvents(events = viewModel.event) { event ->
        when (event) {
            is CoinListEvent.Error -> {
                Toast.makeText(
                    context,
                    event.error.toString(context),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // State to control the selected tab (Home or Favorites)
    var selectedTab by remember { mutableStateOf(0) } // 0 for Home, 1 for Favorites

    // Navigator for determining the current pane
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>()

    // Determine if the screen is in Landscape mode
    val isLandscape = LocalContext.current.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Derive visibility of BottomNavigationBar based on orientation and destination
    val isBottomBarVisible = isLandscape || navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.List

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) { // Show BottomNavigation only if not in Detail
                BottomNavigationBar(
                    selectedItem = selectedTab,
                    onItemSelected = { selectedTab = it }
                )
            }
        },
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) { // Apply padding explicitly
            val filteredCoins = if (selectedTab == 0) state.coins else state.coins.filter { it.isFavorite }

            NavigableListDetailPaneScaffold(
                navigator = navigator,
                listPane = {
                    AnimatedPane(
                        modifier = Modifier
                            .fillMaxSize() // Fill the size without padding
                    ) {
                        CoinListScreen(
                            state = state.copy(coins = filteredCoins),
                            onAction = { action ->
                                viewModel.onAction(action)
                                if (action is CoinListAction.OnCoinClick) {
                                    navigator.navigateTo(
                                        pane = ListDetailPaneScaffoldRole.Detail
                                    )
                                }
                            },
                            onToggleFavorite = { coinUi ->
                                viewModel.toggleFavorite(coinUi)
                            },
                            viewModel = viewModel,
                            showFavoritesOnly = selectedTab == 1
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        CoinDetailScreen(
                            state = state,
                            onToggleFavorite = { coinUi ->
                                viewModel.toggleFavorite(coinUi)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}