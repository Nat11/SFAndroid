package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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
import com.testapp.android.R;
import com.testapp.android.subcontractor.SubcontractorNavActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClientNavActivity extends SalesforceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private TextView tvUser, tvContact, tvEmail;
    private ProgressDialog progressDialog;
    private static String un, cn, an, email, clientId, profileImageUrl, accessToken = "";
    private static String instanceUrl = "https://arisiot-developer-edition.eu11.force.com/demo";
    CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

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
        accessToken = client.getAuthToken();
        clientId = client.getClientInfo().userId;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        profileImageView = (CircleImageView) findViewById(R.id.profile_image);

        tvUser = (TextView) findViewById(R.id.sfUser);
        //tvContact = (TextView) findViewById(R.id.sfContact);
        tvEmail = (TextView) findViewById(R.id.sfEmail);

        try {
            loadUserData();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void loadUserData() throws UnsupportedEncodingException {
        progressDialog.show();
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT Name, ContactId FROM User WHERE Id='" + userId + "'", "ContactId", "user");
    }

    public void onFetchContactUser(String contactId) throws UnsupportedEncodingException {
        sendRequest("SELECT Name, AccountId, Email FROM Contact WHERE Id='" + contactId + "'", "AccountId", "contact");
        // request contact profile picture
        String picture = "Contact Picture";
        sendRequest("SELECT Body, Name FROM Attachment WHERE ParentId='" + contactId + "'" + "and Name='" + picture + "'", "Body", "contactImage");
    }

    public void onFetchAccountUser(String accountId) throws UnsupportedEncodingException {
        sendRequest("SELECT Id, Name FROM Account WHERE Id='" + accountId + "'", "Id", "account");
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
                            Log.d("result", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            String name = records.getJSONObject(0).getString("Name");
                            String id = records.getJSONObject(0).getString(sfId);

                            switch (action) {
                                case "user":
                                    onFetchContactUser(id);
                                    tvUser.setText(name);
                                    un = name;
                                    break;

                                case "contact":
                                    onFetchAccountUser(id);
                                    //tvContact.setText(name);
                                    cn = name;
                                    email = records.getJSONObject(0).getString("Email");
                                    tvEmail.setText("Email: " + email);
                                    break;

                                case "contactImage":
                                    profileImageUrl = id;
                                    displayContactPhoto(profileImageUrl);
                                    break;

                                case "account":
                                    an = name;
                            }

                        } catch (Exception e) {
                            profileImageView.setImageResource(R.drawable.defaultavatar);
                            Log.e("Error Json", e.getLocalizedMessage());
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

    public void displayContactPhoto(String profileImageUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = instanceUrl + profileImageUrl;

        Log.d("bitmapUrl", url);

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
                        //mImageView.setImageResource(R.drawable.image_load_error);
                        Log.d("bitmapError", error.toString());
                        profileImageView.setImageResource(R.drawable.defaultavatar);
                        Toast.makeText(ClientNavActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
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

        } else if (id == R.id.purchase) {
            startActivity(new Intent(ClientNavActivity.this, PurchaseActivity.class));

        } else if (id == R.id.services) {
            Intent i = new Intent(ClientNavActivity.this, ServicesActivity.class);
            i.putExtra("clientEmail", email);
            i.putExtra("fullName", un);
            i.putExtra("ContactName", cn);
            i.putExtra("AccountName", an);
            i.putExtra("clientId", clientId);
            startActivity(i);

        } else if (id == R.id.logout) {
            SalesforceSDKManager.getInstance().logout(this);
            finish();
            moveTaskToBack(true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putString("contact", tvContact.getText().toString());
        outState.putString("email", tvEmail.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //tvContact.setText(savedInstanceState.getString("contact"));
        tvEmail.setText(savedInstanceState.getString("email"));
    }
}
