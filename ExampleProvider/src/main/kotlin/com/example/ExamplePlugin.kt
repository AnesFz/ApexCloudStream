package com.exampleprovider

import com.lagradost.cloudstream3.*

class ApexPlugin : Plugin() {
    override fun getProviders(): List<MainAPI> {
        return listOf(ApexFlix(), ApexAnime())
    }
}
