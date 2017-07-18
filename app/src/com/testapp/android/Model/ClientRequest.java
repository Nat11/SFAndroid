package com.testapp.android.Model;

import java.io.Serializable;

/**
 * Created by Administrator on 13/07/2017.
 */

public class ClientRequest implements Serializable {

    private String fullName, email, contactName, accountName, servicesCompany, assetType, installationLocation, done;

    public ClientRequest(){

    }

    public ClientRequest(String fullName, String email, String contactName, String accountName, String servicesCompany,
                         String assetType, String installationLocation, String done) {
        this.fullName = fullName;
        this.email = email;
        this.contactName = contactName;
        this.accountName = accountName;
        this.servicesCompany = servicesCompany;
        this.assetType = assetType;
        this.installationLocation = installationLocation;
        this.done = done;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getContactName() {
        return contactName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getServicesCompany() {
        return servicesCompany;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getInstallationLocation() {
        return installationLocation;
    }

    public String isDone() {
        return done;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setServicesCompany(String servicesCompany) {
        this.servicesCompany = servicesCompany;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public void setInstallationLocation(String installationLocation) {
        this.installationLocation = installationLocation;
    }

    public void setDone(String done) {
        this.done = done;
    }
}
