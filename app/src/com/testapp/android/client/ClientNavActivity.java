package com.testapp.android.client;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.Main2Activity;
import com.testapp.android.R;
import com.testapp.android.RedirectActivity;
import com.testapp.android.subcontractor.SubcontractorActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class ClientNavActivity extends SalesforceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private TextView tvUser, tvContact, tvAccount;
    private String username, contactName, accountName;
    private static String contactId, accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvUser = (TextView) findViewById(R.id.sfUser);
        tvContact = (TextView) findViewById(R.id.sfContact);
        tvAccount = (TextView) findViewById(R.id.sfAccount);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;

        try {
            loadUserData();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loadUserData() throws UnsupportedEncodingException {
        String userId = client.getClientInfo().userId;
        Log.d("userId", userId);
        sendRequest("SELECT Name, ContactId FROM User WHERE Id='" + userId + "'");
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
                            Log.d("username", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            username = records.getJSONObject(0).getString("Name");
                            contactId = records.getJSONObject(0).getString("ContactId");
                            onFetchContactUser();
                            tvUser.setText(username);

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
                        Log.d("error", ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(ClientNavActivity.this,
                                ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void onFetchContactUser() throws UnsupportedEncodingException {
        sendContactRequest("SELECT Name, AccountId FROM Contact WHERE Id='" + contactId + "'");
    }

    public void sendContactRequest(String soql) throws UnsupportedEncodingException {
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
                            contactName = records.getJSONObject(0).getString("Name");
                            accountId = records.getJSONObject(0).getString("AccountId");
                            onFetchAccountUser();
                            tvContact.setText(contactName);
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
                        Log.d("error", ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(ClientNavActivity.this,
                                ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void onFetchAccountUser() throws UnsupportedEncodingException {
        sendAccountContactRequest("SELECT Name FROM Account WHERE Id='" + accountId + "'");
    }

    public void sendAccountContactRequest(String soql) throws UnsupportedEncodingException {
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
                            accountName = records.getJSONObject(0).getString("Name");
                            tvAccount.setText(accountName);
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
                        Log.d("error", ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(ClientNavActivity.this,
                                ClientNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.monitor) {

        } else if (id == R.id.services) {

        } else if (id == R.id.logout) {
            SalesforceSDKManager.getInstance().logout(this);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
