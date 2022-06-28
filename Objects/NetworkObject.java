package com.arsinex.com.Objects;

public class NetworkObject {
    private String asset_id, network_id, network_name, contract_address, commission;

    public NetworkObject(String asset_id, String network_id, String network_name, String contract_address, String commission) {
        this.asset_id = asset_id;
        this.network_id = network_id;
        this.network_name = network_name;
        this.contract_address = contract_address;
        this.network_id = network_id;
    }

    public String getAsset_id() {
        return asset_id;
    }

    public String getNetwork_id() {
        return network_id;
    }

    public String getNetwork_name() {
        return network_name;
    }

    public String getContract_address() {
        return contract_address;
    }

    public String getCommission() {
        return commission;
    }
}