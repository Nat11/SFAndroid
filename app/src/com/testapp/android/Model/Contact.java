package com.testapp.android.Model;

import java.io.Serializable;

/**
 * Created by natal on 03-Jul-17.
 */

public class Contact implements Serializable {

    private String contactId, contactName, accountId, accountName;

    public Contact() {
    }

    public Contact(String contactId, String contactName, String accountId, String accountName) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.accountId = accountId;
        this.accountName = accountName;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
