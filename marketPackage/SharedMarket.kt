package com.arsinex.com.marketPackage

import com.arsinex.com.Objects.MarketObject

class SharedMarket {
    var market: MarketObject? = null
        get(): MarketObject? {
            return field ?: MarketObject("BTCTRY","BTC", "TRY", "4", "8")
        }
        set(market) {
            field = market
        }
}