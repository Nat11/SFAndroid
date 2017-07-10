package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.R;
import com.testapp.android.client.ClientNavActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class SubcontractorNavActivity extends SalesforceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private TextView tvUser, tvContact, tvAccount;
    private ProgressDialog progressDialog;
    private static String un, cn, an;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcontractor_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        Log.d("accessToken", client.getAuthToken());

        tvUser = (TextView) findViewById(R.id.sfUser);
        tvContact = (TextView) findViewById(R.id.sfContact);
        tvAccount = (TextView) findViewById(R.id.sfAccount);

        try {
            loadUserData();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loadUserData() throws UnsupportedEncodingException {
        progressDialog.show();
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT Name, ContactId FROM User WHERE Id='" + userId + "'", "ContactId", "fn");
    }

    public void onFetchContactUser(String contactId) throws UnsupportedEncodingException {
        sendRequest("SELECT Name, AccountId, Email FROM Contact WHERE Id='" + contactId + "'", "AccountId", "cn");
    }

    public void onFetchAccountUser(String accountId) throws UnsupportedEncodingException {
        sendRequest("SELECT Id, Name FROM Account WHERE Id='" + accountId + "'", "Id", "an");
        progressDialog.dismiss();
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
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            String name = records.getJSONObject(0).getString("Name");
                            String id = records.getJSONObject(0).getString(sfId);

                            switch (action) {
                                case "fn":
                                    onFetchContactUser(id);
                                    tvUser.setText("Welcome " + name);
                                    un = name;
                                    break;

                                case "cn":
                                    onFetchAccountUser(id);
                                    tvContact.setText("Contact: " + name);
                                    cn = name;
                                    break;

                                case "an":
                                    tvAccount.setText("Account: " + name);
                                    an = name;
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
                        Log.d("error", SubcontractorNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(SubcontractorNavActivity.this,
                                SubcontractorNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
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
            finish();
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.monitor) {

        } else if (id == R.id.register) {
            startActivity(new Intent(SubcontractorNavActivity.this, AssetActivity.class));

        } else if (id == R.id.logout) {
            SalesforceSDKManager.getInstance().logout(this);
            finish();
            moveTaskToBack(true);
        } else if (id == R.id.assets) {
            startActivity(new Intent(SubcontractorNavActivity.this, SFAssetsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
