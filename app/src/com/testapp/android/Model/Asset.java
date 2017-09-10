package com.testapp.android.Model;

import java.io.Serializable;

public class Asset implements Serializable {

    private String id, ownerId, name, location, status;

    public Asset() {
    }

    public Asset(String id, String name, String ownerId, String location, String status) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.location = location;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
