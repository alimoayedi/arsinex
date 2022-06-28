package com.arsinex.com.Objects;

public class BankObject {
    String bankId, title, type, workingHour, url;
    String accountNumber, accountIban, accountName;

    public BankObject(String bankId, String title, String type, String workingHour, String url) {
        this.bankId = bankId;
        this.title = title;
        this.type = type;
        this.workingHour = workingHour;
        this.url = url;
    }

    public String getBankId() {
        return bankId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getWorkingHour() {
        return workingHour;
    }

    public String getUrl() {
        return url;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountIban() {
        return accountIban;
    }

    public void setAccountIban(String accountIban) {
        this.accountIban = accountIban;
    }

    public String getAccountName() {
        return accountName;
    }

}