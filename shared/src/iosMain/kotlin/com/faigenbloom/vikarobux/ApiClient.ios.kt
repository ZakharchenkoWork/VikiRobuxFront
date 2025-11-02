package com.faigenbloom.vikarobux

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun provideEngine(): HttpClientEngine = Darwin.create()