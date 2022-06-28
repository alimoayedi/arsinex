package com.arsinex.com;

public class SpinnerCountryItem {

    private String countryName;
    private String countryCode;

    public SpinnerCountryItem(String countryName, String countryCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }
}
