package com.example.android.contactmanager;

/**
 * Created by SONY on 2015/4/3.
 */

public class ContactInfo {
    private String name;
    private String number;


    public ContactInfo(){}

    public ContactInfo(String name, String number) {
        this.name = name;
        this.number = number;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

}
