package com.arsinex.com.Objects;

public class SharedMarketObject {

    private MarketObject market;

    public MarketObject getMarket() {
        if(market == null){
            return new MarketObject("BTCTRY","BTC", "TRY", "4", "8");
        }
        return market;
    }

    public void setMarket(MarketObject market) {
        this.market = market;
    }
}
