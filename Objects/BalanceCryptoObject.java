package com.arsinex.com.Objects;

public class BalanceCryptoObject {

    private String asset;
    private String available;
    private String freeze;
    private String total;
    private String equivalent;
    private String equivalent_unit;

    public BalanceCryptoObject(String asset, String available, String freeze, String total, String equivalent, String equivalent_unit) {
        this.asset = asset;
        this.available = available;
        this.freeze = freeze;
        this.total = total;
        this.equivalent = equivalent;
        this.equivalent_unit = equivalent_unit;
    }

    public String getAsset() {
        return asset;
    }

    public String getAvailable() {
        return available;
    }

    public String getFreeze() {
        return freeze;
    }

    public String getTotal() {
        return total;
    }

    public String getLogoURL() {
        return asset;
    }

    public String getEquivalent() {
        return equivalent;
    }

    public String getEquivalent_unit() {
        return equivalent_unit;
    }
}
