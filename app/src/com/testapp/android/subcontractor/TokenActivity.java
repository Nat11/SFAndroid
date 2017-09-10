package com.testapp.android.subcontractor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.R;
import com.testapp.android.jwt.JWTUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenActivity extends SalesforceActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 1;
    private String actorToken, subjectToken;
    private RestClient client;
    Button btnAssetToken;
    TextView tvAssetToken, tvTokenHeader, tvTokenBody;
    private RequestQueue MyRequestQueue;
    LinearLayout decodedToken_layout;
    private Context context;
    private String assetId = "";
    private String assetToken = "";
    File assetTokenFile;
    private String contactId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatCallback callback = new AppCompatCallback() {
            @Override
            public void onSupportActionModeStarted(ActionMode mode) {
            }

            @Override
            public void onSupportActionModeFinished(ActionMode mode) {
            }

            @Nullable
            @Override
            public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
                return null;
            }
        };
        AppCompatDelegate delegate = AppCompatDelegate.create(this, callback);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_token);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);

        context = this;
        actorToken = getIntent().getStringExtra("actorToken");
        btnAssetToken = (Button) findViewById(R.id.getAssetToken);
        tvAssetToken = (TextView) findViewById(R.id.assetToken);
        tvTokenBody = (TextView) findViewById(R.id.token_body);
        tvTokenHeader = (TextView) findViewById(R.id.token_header);
        decodedToken_layout = (LinearLayout) findViewById(R.id.token_decoded);
        MyRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        subjectToken = client.getAuthToken();
        btnAssetToken.setOnClickListener(this);
        try {
            getSubcontractorContact();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getAssetToken:
                getAssetToken();
        }
    }

    public void getSubcontractorContact() throws UnsupportedEncodingException {
        String userId = client.getClientInfo().userId;
        sendRequest("SELECT ContactId FROM User WHERE Id='" + userId + "'");
    }

    // get user info: contact and account info stored in salesforce
    private void sendRequest(String soql) throws UnsupportedEncodingException {

        final RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("subcontactId", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            contactId = records.getJSONObject(0).getString("ContactId");

                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
            }
        });
    }

    public void getAssetToken() {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/oauth2/token";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    assetToken = jsonResponse.getString("access_token");

                    //checkWritePermission(); //Check permission to save token before sending it to Asset

                    writeToFile(assetToken);

                    JWT jwt = new JWT(assetToken);
                    try {

                        Snackbar.make(btnAssetToken.getRootView(), "Asset Record is successfully registered", Snackbar.LENGTH_LONG)
                                .show();

                        //Decode token issued by Salesforce and display to screen
                        String[] decodedToken = JWTUtils.decoded(jwt.toString());

                        JSONObject header = new JSONObject(decodedToken[0]);
                        JSONObject payload = new JSONObject(decodedToken[1]);

                        decodedToken_layout.setVisibility(View.VISIBLE);

                        String kid = header.getString("kid");
                        String typ = header.getString("typ");
                        String alg = header.getString("alg");
                        String aud = payload.getString("aud");
                        String nbf = payload.getString("nbf");
                        Long timeNotBefore = Long.parseLong(nbf);
                        Date dateNotBefore = new Date((long) timeNotBefore * 1000);
                        String iss = payload.getString("iss");
                        String aid = payload.getString("aid"); //Asset Id
                        String did = payload.getString("did");

                        assetId = aid;
                        //Set Asset Status to Registered in Salesforce
                        updateAssetInstalled(aid);

                        tvTokenHeader.setText("\n Key Id: " + kid + "\n Type: " + typ + "\n Algorithm: " + alg);

                        tvTokenBody.setText("\n Not Before: " + dateNotBefore + "\n Issuer: " + iss
                                + "\n Asset Id: " + aid + "\n Device Id: " + did);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                //This code is executed if there is an error.
                Log.e("error", error.toString());
            }
        }) {
            //Send Post Request with Correspondent Parameters
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange"); //Add the data you'd like to send to the server.
                MyData.put("subject_token_type", "urn:ietf:params:oauth:token-type:access_token");
                MyData.put("subject_token", subjectToken);
                MyData.put("actor_token_type", "urn:ietf:params:oauth:token-type:jwt");
                MyData.put("actor_token", actorToken);
                return MyData;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    public void writeToFile(String data) throws IOException {

        File path = new File(Environment.getExternalStorageDirectory() + "/Documents");
        boolean isPresent = true;
        if (!path.exists()) {
            isPresent = path.mkdir();
        }
        if (isPresent) {
            assetTokenFile = new File(path.getAbsolutePath(), "AssetToken.txt");
        } else {
            // Failure
            Toast.makeText(context, "Error accessing storage", Toast.LENGTH_SHORT).show();
        }

        try {
            assetTokenFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(assetTokenFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();

            Toast.makeText(context, "token saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Set Asset to registered
    private void updateAssetInstalled(String aid) {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/data/v39.0/sobjects/Asset/" + aid;
        StringRequest MyStringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {

                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> params2 = new HashMap<String, String>();
                params2.put("Status", "Registered");
                return new JSONObject(params2).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                Log.d("accessToken", subjectToken);
                String bearer = "Bearer ".concat(subjectToken);
                headers.put("Authorization", bearer);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        MyRequestQueue.add(MyStringRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_token, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.install) {
            if (!assetId.isEmpty()) {
                Intent i = new Intent(TokenActivity.this, InstallAssetActivity.class);
                i.putExtra("assetId", assetId);
                i.putExtra("accessToken", subjectToken);
                i.putExtra("assetTokenFile", assetTokenFile);
                i.putExtra("subcontactrorContact", contactId);
                startActivity(i);
                return true;
            } else {
                Toast.makeText(context, "Get Asset Token first", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
