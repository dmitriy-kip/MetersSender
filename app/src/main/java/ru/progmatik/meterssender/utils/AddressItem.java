package ru.progmatik.meterssender.utils;

public class AddressItem {
    private String server;
    private String session;
    private String street;
    private String restAddress;
    private String town_id;
    private String account_id;

    public String getServer() {
        return server;
    }

    public String getSession() {
        return session;
    }

    public String getStreet() {
        return street;
    }

    public String getTown_id() {
        return town_id;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setTown_id(String town_id) {
        this.town_id = town_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getRestAddress() {
        return restAddress;
    }

    public void setRestAddress(String restAddress) {
        this.restAddress = restAddress;
    }
}
