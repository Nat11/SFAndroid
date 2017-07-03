package com.testapp.android.Model;

/**
 * Created by natal on 03-Jul-17.
 */

public class User {

    private String fullName, contactName, accountName, email;

    public User(String fullName, String contactName, String accountName, String email) {
        this.fullName = fullName;
        this.contactName = contactName;
        this.accountName = accountName;
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
