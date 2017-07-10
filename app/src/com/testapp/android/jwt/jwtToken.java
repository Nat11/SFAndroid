package com.testapp.android.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class jwtToken {

    public String createToken(String did, String tokenName, String account, String SN, String contactId, String assetName) throws JSONException {

        JSONObject jsonObj = new JSONObject("{\"did\":\"" + did + "\",\"Name\":\"" + tokenName + "\", \"Asset\":{" +
                "\"AccountId\":\"" + account + "\",\"SerialNumber\":\"" + SN + "\",\"ContactId\":\"" + contactId + "\"," +
                "\"Name\":\"" + assetName + "\"}}");

        System.out.print("test token" + Jwts.builder().setPayload(String.valueOf(jsonObj)).compact());

        return Jwts.builder().setPayload(String.valueOf(jsonObj)).compact();
    }

    public String createTokenNoContact(String did, String tokenName, String account, String SN, String assetName) throws JSONException {

        JSONObject jsonObj = new JSONObject("{\"did\":\"" + did + "\",\"Name\":\"" + tokenName + "\", \"Asset\":{" +
                "\"AccountId\":\"" + account + "\",\"SerialNumber\":\"" + SN + "\",\"Name\":\"" + assetName + "\"}}");

        System.out.print("test token" + Jwts.builder().setPayload(String.valueOf(jsonObj)).compact());

        return Jwts.builder().setPayload(String.valueOf(jsonObj)).compact();
    }
}
