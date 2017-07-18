package com.testapp.android.subcontractor;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.R;
import com.testapp.android.jwt.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenActivity extends SalesforceActivity implements View.OnClickListener {

    private String actorToken, subjectToken, requestId;
    private RestClient client;
    Button btnAssetToken;
    TextView tvAssetToken, tvTokenHeader, tvTokenBody;
    RequestQueue MyRequestQueue;
    LinearLayout decodedToken_layout;
    private DatabaseReference mDatabase;

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
        delegate.setTitle("Asset Token");
        actorToken = getIntent().getStringExtra("actorToken");
        requestId = getIntent().getStringExtra("requestId");
        btnAssetToken = (Button) findViewById(R.id.getAssetToken);
        tvAssetToken = (TextView) findViewById(R.id.assetToken);
        tvTokenBody = (TextView) findViewById(R.id.token_body);
        tvTokenHeader = (TextView) findViewById(R.id.token_header);
        decodedToken_layout = (LinearLayout) findViewById(R.id.token_decoded);
        MyRequestQueue = Volley.newRequestQueue(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        subjectToken = client.getAuthToken();
        btnAssetToken.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getAssetToken:
                getAssetToken();
                Snackbar.make(view, "Asset Record is successfully added to Salesforce", Snackbar.LENGTH_LONG)
                        .show();
        }
    }

    public void getAssetToken() {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/oauth2/token";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String assetToken = jsonResponse.getString("access_token");
                    JWT jwt = new JWT(assetToken);
                    try {

                        //Get Firebase Request by Id then set it to Done when Salesforce response is okay
                        FBRequestDone(requestId);

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
                        String aid = payload.getString("aid");
                        String did = payload.getString("did");

                        tvTokenHeader.setText("\n Key Id: " + kid + "\n Type: " + typ + "\n Algorithm: " + alg);

                        tvTokenBody.setText("\n Not Before: " + dateNotBefore + "\n Issuer: " + iss
                                + "\n Asset Id: " + aid + "\n Device Id: " + did);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
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

    //Set Firebase request to done
    private void FBRequestDone(String requestId) {
        mDatabase.child("ClientRequests").child("subcontractor").child(requestId).child("done").setValue("true");
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            startActivity(new Intent(TokenActivity.this, SubcontractorNavActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
