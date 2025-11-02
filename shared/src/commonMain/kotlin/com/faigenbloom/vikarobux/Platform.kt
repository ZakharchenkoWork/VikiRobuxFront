package com.faigenbloom.vikarobux

interface Platform {
    companion object{

    }
    val name: String
}

expect fun getPlatform(): Platform

expect fun getOS(): String