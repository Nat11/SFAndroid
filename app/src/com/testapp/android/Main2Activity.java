package com.testapp.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class Main2Activity extends SalesforceActivity {

    private RestClient client;
    private TextView fullName, accountName, contactName;
    //private static String fn = "";
    private static String cn = "";
    private static String an = "";
    //private static String contactId = "";
    //private static String accountId = "";
    public static String accessToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    @Override
    public void onResume() {
        // Hide everything until we are logged in
        //findViewById(R.id.root).setVisibility(View.INVISIBLE);
        super.onResume();
    }

    @Override
    public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client;
        accessToken = client.getAuthToken();
        Log.d("token", accessToken);
        fullName = (TextView) findViewById(R.id.fullName);
        accountName = (TextView) findViewById(R.id.accountName);
        contactName = (TextView) findViewById(R.id.contactName);
        try {
            onFetchCurrentUser();
            // onFetchContactUser();
            // onFetchAccountUser();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onLogoutClick(View v) {
        SalesforceSDKManager.getInstance().logout(this);
    }

    public void onFetchCurrentUser() throws UnsupportedEncodingException {
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT Name,ContactId,Flag__c FROM User WHERE Id='" + userId + "'", "ContactId", "fn");
    }

    public void onFetchContactUser(String contactId) throws UnsupportedEncodingException {
        Log.d("contactId", contactId);
        sendRequest("SELECT Name, AccountId FROM Contact WHERE Id='" + contactId + "'", "AccountId", "cn");
    }

    public void onFetchAccountUser(String accountId) throws UnsupportedEncodingException {
        Log.d("AccountId", accountId);
        sendRequest("SELECT Name FROM Account WHERE Id='" + accountId + "'", null, "an");
    }


    private void sendRequest(String soql, final String sfId, final String action) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("result", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            String name = records.getJSONObject(0).getString("Name");
                            String id = records.getJSONObject(0).getString(sfId);

                            switch (action) {
                                case "fn":
                                    onFetchContactUser(id);
                                    fullName.append(name);
                                    break;

                                case "cn":
                                    onFetchAccountUser(id);
                                    contactName.append(name);
                                    break;

                                case "an":
                                    accountName.setText(name);
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
                        Log.d("error", Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(Main2Activity.this,
                                Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


   /* private void sendContactRequest(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("contactId result", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            cn = records.getJSONObject(0).getString("Name");
                            accountId = records.getJSONObject(0).getString("AccountId");
                            onFetchAccountUser();
                            contactName.append(cn);

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
                        Log.d("error", Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(Main2Activity.this,
                                Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void sendAccountContactRequest(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("accountId result", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            an = records.getJSONObject(0).getString("Name");
                            accountName.setText("Account: " + an);

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
                        Log.d("error", Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(Main2Activity.this,
                                Main2Activity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    */
}
