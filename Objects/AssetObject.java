package com.arsinex.com.Objects;

public class AssetObject {
    private String assetId, assetTitle, symbol, assetType, logoUrl;

    public AssetObject(String assetId, String assetTitle, String symbol, String assetType, String logoUrl) {
        this.assetId = assetId;
        this.assetTitle = assetTitle;
        this.symbol = symbol;
        this.assetType = assetType;
        this.logoUrl = logoUrl;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetTitle() {
        return assetTitle;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}