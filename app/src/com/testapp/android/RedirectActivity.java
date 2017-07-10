package com.testapp.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.client.ClientNavActivity;
import com.testapp.android.subcontractor.SubcontractorNavActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class RedirectActivity extends SalesforceActivity {

    private RestClient client;
    private static String flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redirect);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;

        try {
            onFetchUserFlag();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onFetchUserFlag() throws UnsupportedEncodingException {
        String userId = client.getClientInfo().userId;
        Log.d("userId", userId);
        sendRequest("SELECT Flag__c FROM User WHERE Id='" + userId + "'");
    }

    private void sendRequest(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("flag", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            flag = records.getJSONObject(0).getString("Flag__c");

                            switch (flag) {
                                case "client":
                                    startActivity(new Intent(RedirectActivity.this, ClientNavActivity.class));
                                    break;

                                case "subcontractor":
                                    startActivity(new Intent(RedirectActivity.this, SubcontractorNavActivity.class));
                                    break;
                            }

                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("error", RedirectActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(RedirectActivity.this,
                                RedirectActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
