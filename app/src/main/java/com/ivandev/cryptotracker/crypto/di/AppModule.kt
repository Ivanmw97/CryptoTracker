package com.ivandev.cryptotracker.crypto.di

import com.ivandev.cryptotracker.core.data.networking.HttpClientFactory
import com.ivandev.cryptotracker.crypto.data.networking.RemoteCoinDataSource
import com.ivandev.cryptotracker.crypto.domain.CoinDataSource
import com.ivandev.cryptotracker.crypto.presentation.coin_list.CoinListViewModel
import io.ktor.client.engine.cio.CIO
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { HttpClientFactory.create(CIO.create()) }
    singleOf(::RemoteCoinDataSource).bind<CoinDataSource>()

    viewModelOf(::CoinListViewModel)
}