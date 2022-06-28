package com.arsinex.com.Objects;

public class AddressObject {
    private boolean crypto;
    private String currency, network, walletAddress, addressName;
    private long savedTime;

    /**
     *
     * @param savedTime
     * @param crypto
     * @param currency
     * @param network
     * @param walletAddress
     * @param addressName
     */

    public AddressObject(long savedTime, boolean crypto, String currency, String network, String walletAddress, String addressName) {
        this.savedTime = savedTime;
        this.crypto = crypto;
        this.currency = currency;
        this.network = network;
        this.walletAddress = walletAddress;
        this.addressName = addressName;
    }

    public long getSavedTime() {
        return savedTime;
    }

    public boolean isCrypto() {
        return crypto;
    }

    public void setCrypto(boolean crypto) {
        this.crypto = crypto;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }
}
