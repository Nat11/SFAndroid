package com.testapp.android.jwt;

import org.json.JSONException;
import org.json.JSONObject;

import io.jsonwebtoken.Jwts;

public class jwtToken {

    public String createToken(String did, String tokenName, String account, String SN, String contactId, String assetName,
                              String assetType, String assetLocation) throws JSONException {

        JSONObject jsonObj = new JSONObject("{\"did\":\"" + did + "\",\"Name\":\"" + tokenName + "\", \"Asset\":{" +
                "\"AccountId\":\"" + account + "\",\"SerialNumber\":\"" + SN + "\",\"ContactId\":\"" + contactId + "\"," +
                "\"Name\":\"" + assetName + "\",\"Type\":\"" + assetType + "\" ,\"Location\":\"" + assetLocation + "\"}}");

        System.out.print("test token" + Jwts.builder().setPayload(String.valueOf(jsonObj)).compact());

        return Jwts.builder().setPayload(String.valueOf(jsonObj)).compact();
    }

    public String createTokenNoContact(String did, String tokenName, String account, String SN, String assetName,
                                       String assetType, String assetLocation) throws JSONException {

        JSONObject jsonObj = new JSONObject("{\"did\":\"" + did + "\",\"Name\":\"" + tokenName + "\", \"Asset\":{" +
                "\"AccountId\":\"" + account + "\",\"SerialNumber\":\"" + SN + "\",\"Name\":\"" + assetName + "\"," +
                "\"Type\":\"" + assetType + "\" ,\"Location\":\"" + assetLocation + "\"}}");

        System.out.print("test token" + Jwts.builder().setPayload(String.valueOf(jsonObj)).compact());

        return Jwts.builder().setPayload(String.valueOf(jsonObj)).compact();
    }
}
