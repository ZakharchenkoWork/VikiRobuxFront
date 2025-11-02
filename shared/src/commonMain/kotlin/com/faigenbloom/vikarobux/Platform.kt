package com.faigenbloom.vikarobux

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform