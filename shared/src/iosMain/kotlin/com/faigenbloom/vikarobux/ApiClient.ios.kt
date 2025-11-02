package com.faigenbloom.vikarobux

actual fun provideEngine(): HttpClientEngine = Darwin.create()