package com.testapp.android.Model;

public class Asset {

    private String assetTokenName, deviceId, assetSN, assetName;

    public Asset(String assetToken, String deviceId, String assetSN, String assetName) {
        this.assetTokenName = assetToken;
        this.deviceId = deviceId;
        this.assetSN = assetSN;
        this.assetName = assetName;
    }

    public String getAssetTokenName() {
        return assetTokenName;
    }

    public void setAssetTokenName(String assetTokenName) {
        this.assetTokenName = assetTokenName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAssetSN() {
        return assetSN;
    }

    public void setAssetSN(String assetSN) {
        this.assetSN = assetSN;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }
}
