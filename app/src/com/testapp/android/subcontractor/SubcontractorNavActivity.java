package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.Model.Asset;
import com.testapp.android.Model.ClientRequest;
import com.testapp.android.R;
import com.testapp.android.client.ClientNavActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.action;
import static android.R.attr.data;
import static android.R.attr.theme;

public class SubcontractorNavActivity extends SalesforceActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RestClient client;
    private TextView tvUser, tvAccount, tvNewRequests;
    private ProgressDialog progressDialog;
    private static String un, cn, an, profileImageUrl, accessToken = "";
    private static String instanceUrl = "https://arisiot-developer-edition.eu11.force.com/demo";
    CircleImageView profileImageView;
    private DatabaseReference mDatabase;
    private int nbRequests = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcontractor_nav);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        tvUser = (TextView) findViewById(R.id.sfUser);
        tvAccount = (TextView) findViewById(R.id.sfAccount);
        profileImageView = (CircleImageView) findViewById(R.id.profile_image);
        tvNewRequests = (TextView) findViewById(R.id.newRequests);

        getClientRequests(mDatabase);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        accessToken = client.getAuthToken();
        Log.d("accessToken", accessToken);
        getClientRequests(mDatabase);

        try {
            loadUserData();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //Get number of new client requests then update UI
    private void getClientRequests(DatabaseReference db) {
        nbRequests = 0;
        db.child("ClientRequests").child("subcontractor").orderByChild("done").equalTo("false").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    nbRequests = (int) dataSnapshot.getChildrenCount();
                }
                updateNewRequestUI(nbRequests);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        progressDialog.show();
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT Name, ContactId FROM User WHERE Id='" + userId + "'", "ContactId", "user");
    }

    public void onFetchContactUser(String contactId) throws UnsupportedEncodingException {
        sendRequest("SELECT Name, AccountId FROM Contact WHERE Id='" + contactId + "'", "AccountId", "contact");
        // request contact profile picture
        String picture = "Contact Picture";
        sendRequest("SELECT Body, Name FROM Attachment WHERE ParentId='" + contactId + "'" + "and Name='" + picture + "'", "Body", "contactImage");
    }

    public void onFetchAccountUser(String accountId) throws UnsupportedEncodingException {
        sendRequest("SELECT Id, Name FROM Account WHERE Id='" + accountId + "'", "Id", "account");
        progressDialog.dismiss();
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
                                    cn = name;
                                    break;

                                case "contactImage":
                                    profileImageUrl = id;
                                    displayContactPhoto(profileImageUrl);
                                    break;

                                case "account":
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

        } //else if (id == R.id.register) {
        //startActivity(new Intent(SubcontractorNavActivity.this, AssetActivity.class));}
        //
        else if (id == R.id.logout) {
            SalesforceSDKManager.getInstance().logout(this);
            finish();
            moveTaskToBack(true);
        } else if (id == R.id.assets) {
            startActivity(new Intent(SubcontractorNavActivity.this, SFAssetsActivity.class));
        } else if (id == R.id.requests) {
            startActivity(new Intent(SubcontractorNavActivity.this, RequestsActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        progressDialog.dismiss();
        super.onSaveInstanceState(outState);
        outState.putString("fullName", tvUser.getText().toString());
        outState.putString("account", tvAccount.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        tvUser.setText(savedInstanceState.getString("fullName"));
        tvAccount.setText(savedInstanceState.getString("account"));
    }
}
