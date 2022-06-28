package com.arsinex.com.Objects;

public class BalanceCreditObject {

    private String asset_name;
    private String available;
    private String freeze;
    private String total;
    private String equivalent;
    private String equivalent_unit;

    public BalanceCreditObject(String asset_name, String available, String freeze, String total, String equivalent, String equivalent_unit) {
        this.asset_name = asset_name;
        this.available = available;
        this.freeze = freeze;
        this.total = total;
        this.equivalent = equivalent;
        this.equivalent_unit = equivalent_unit;
    }

    public String getAsset_name() {
        return asset_name;
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

    public String getLogo() {
        return asset_name;
    }

    public String getEquivalent() {
        return equivalent;
    }

    public String getEquivalent_unit() {
        return equivalent_unit;
    }

    public void setAvailable(String available) {
        this.available = available;
    }
}
