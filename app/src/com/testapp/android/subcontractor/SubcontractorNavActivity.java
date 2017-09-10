package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.Model.Asset;
import com.testapp.android.R;
import com.testapp.android.client.ClientNavActivity;
import com.testapp.android.subcontractor.Bluetooth.BTMainActivity;

import org.json.JSONArray;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SubcontractorNavActivity extends SalesforceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private TextView tvUser, tvUsername, tvAccount, tvNewRequests;
    private ProgressDialog progressDialog;
    private static String un, cn, an, profileImageUrl, accessToken = "";
    private static String instanceUrl = "https://arisiot-developer-edition.eu11.force.com/demo";
    CircleImageView profileImageView;
    private int nbRequests = 0;
    private List<Asset> assetsPending = new ArrayList<>();
    private List<Asset> assetsDone = new ArrayList<>();
    private RelativeLayout mainRelativLayout;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcontractor_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        mainRelativLayout = (RelativeLayout) findViewById(R.id.mainRelativeLayout);
        tvUser = (TextView) findViewById(R.id.sfUser);
        tvAccount = (TextView) findViewById(R.id.sfAccount);
        tvUsername = (TextView) findViewById(R.id.sfUsername);
        profileImageView = (CircleImageView) findViewById(R.id.profile_image);
        tvNewRequests = (TextView) findViewById(R.id.newRequests);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        accessToken = client.getAuthToken();
        assetsDone.clear();
        assetsPending.clear();
        Log.d("accessToken", accessToken);
        new UserProfile().execute();
    }

    //Get number of new client requests then update UI, and get Asset data
    private void getClientRequests(String soql, final String action) throws UnsupportedEncodingException {
        nbRequests = 0;
        final RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("AssetResult", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            switch (action) {
                                case "count":
                                    nbRequests = records.getJSONObject(0).getInt("expr0");
                                    updateNewRequestUI(nbRequests);
                                    break;
                                case "assetPending":
                                    for (int i = 0; i < records.length(); i++) {
                                        String name = records.getJSONObject(i).getString("Name");
                                        String id = records.getJSONObject(i).getString("Id");
                                        String ownerId = records.getJSONObject(i).getString("OwnerId");
                                        String location = records.getJSONObject(i).getString("location__c");
                                        String status = records.getJSONObject(i).getString("Status");
                                        assetsPending.add(new Asset(id, name, ownerId, location, status));
                                    }
                                    break;
                                case "assetDone":
                                    for (int i = 0; i < records.length(); i++) {
                                        String name = records.getJSONObject(i).getString("Name");
                                        String id = records.getJSONObject(i).getString("Id");
                                        String ownerId = records.getJSONObject(i).getString("OwnerId");
                                        String location = records.getJSONObject(i).getString("location__c");
                                        String status = records.getJSONObject(i).getString("Status");
                                        assetsDone.add(new Asset(id, name, ownerId, location, status));
                                    }
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
                        Log.d("error", SubcontractorNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(SubcontractorNavActivity.this,
                                SubcontractorNavActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateNewRequestUI(int newRequests) {
        if (newRequests == 1)
            tvNewRequests.setText("Hello, you have " + newRequests + " new request! For more details go to Requests on the left menu");

        else if (newRequests > 1)
            tvNewRequests.setText("Hello, you have " + newRequests + " new requests! For more details go to Requests on the left menu");

        else
            tvNewRequests.setText("Hello, you have no new requests");
    }

    public void loadUserData() throws UnsupportedEncodingException {
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT Name, Username, ContactId FROM User WHERE Id='" + userId + "'", "ContactId", "user");
    }

    public void onFetchContactUser(String contactId) throws UnsupportedEncodingException {
        sendRequest("SELECT Name, AccountId FROM Contact WHERE Id='" + contactId + "'", "AccountId", "contact");
        // request contact profile picture
        String picture = "Contact Picture";
        sendRequest("SELECT Body, Name FROM Attachment WHERE ParentId='" + contactId + "'" + "and Name='" + picture + "'", "Body", "contactImage");
    }

    public void onFetchAccountUser(String accountId) throws UnsupportedEncodingException {
        sendRequest("SELECT Id, Name FROM Account WHERE Id='" + accountId + "'", "Id", "account");
    }

    // get user info: contact and account info stored in salesforce
    private void sendRequest(String soql, final String sfId, final String action) throws UnsupportedEncodingException {

        final RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

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
                            String name = "";
                            String id = "";

                            switch (action) {
                                case "user":
                                    name = records.getJSONObject(0).getString("Name");
                                    id = records.getJSONObject(0).getString(sfId);
                                    contactId = id;
                                    String username = records.getJSONObject(0).getString("Username");
                                    onFetchContactUser(id);
                                    tvUser.setText(name);
                                    tvUsername.setText("Username: " + username);
                                    un = name;
                                    break;

                                case "contact":
                                    name = records.getJSONObject(0).getString("Name");
                                    id = records.getJSONObject(0).getString(sfId);
                                    onFetchAccountUser(id);
                                    cn = name;
                                    break;

                                case "contactImage":
                                    id = records.getJSONObject(0).getString(sfId);
                                    profileImageUrl = id;
                                    displayContactPhoto(profileImageUrl);
                                    break;

                                case "account":
                                    name = records.getJSONObject(0).getString("Name");
                                    if (name.equals("Subcontractor"))
                                        tvAccount.setText("Company: Energy Services");
                                    else
                                        tvAccount.setText("Company: " + name);
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

    public void displayContactPhoto(String profileImageUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = instanceUrl + profileImageUrl;

        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest imageRequest = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        profileImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Log.d("bitmapError", error.toString());
                        Toast.makeText(SubcontractorNavActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })

        {
            //Add authorization bearer in Header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String bearer = "Bearer ".concat(accessToken);
                headers.put("Authorization", bearer);
                return headers;
            }
        };
        ;
        // Add the request to the RequestQueue.
        queue.add(imageRequest);
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
            Intent i = new Intent(SubcontractorNavActivity.this, MonitorActivity.class);
            i.putExtra("contactId", contactId);
            startActivity(i);
        } else if (id == R.id.register) {
            startActivity(new Intent(SubcontractorNavActivity.this, BTMainActivity.class));
        } else if (id == R.id.logout) {
            SalesforceSDKManager.getInstance().logout(this);
            finish();
            //moveTaskToBack(true);
        } else if (id == R.id.requests) {
            Intent i = new Intent(SubcontractorNavActivity.this, RequestsActivity.class);
            i.putExtra("ASSETS", (Serializable) assetsPending);
            i.putExtra("ASSETSDONE", (Serializable) assetsDone);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class UserProfile extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SubcontractorNavActivity.this);
            progressDialog.setMessage("Loading Profile");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            mainRelativLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                loadUserData();
                //Count new requests
                getClientRequests("Select Count(Id) FROM Asset WHERE Status='" + "Shipped" + "'" +
                        "AND installationRequest__c=" + true + "", "count");

                //Pending requests (shipped or installed)
                getClientRequests("Select Id, Name, AccountId, OwnerId, location__c, installationRequest__c, Status FROM Asset WHERE Status IN('" + "Shipped" +
                        "', '" + "Registered" + "')" + "AND installationRequest__c=" + true + "", "assetPending");

                //Done requests
                getClientRequests("Select Id, Name, AccountId, OwnerId, location__c, installationRequest__c, Status FROM Asset WHERE Status='" +
                        "Installed" + "'" + "AND installationRequest__c=" + true + "", "assetDone");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
