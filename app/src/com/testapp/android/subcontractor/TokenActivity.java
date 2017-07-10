package com.testapp.android.subcontractor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class TokenActivity extends SalesforceActivity implements View.OnClickListener {

    private String actorToken, subjectToken;
    private RestClient client;
    Button btnAssetToken;
    TextView tvAssetToken;
    RequestQueue MyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);
        actorToken = getIntent().getStringExtra("actorToken");
        btnAssetToken = (Button) findViewById(R.id.getAssetToken);
        tvAssetToken = (TextView) findViewById(R.id.assetToken);
        MyRequestQueue = Volley.newRequestQueue(this);
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
        }
    }

    public void getAssetToken() {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/oauth2/token";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("testReponse", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String assetToken = jsonResponse.getString("access_token");
                    tvAssetToken.append("\n\nAsset Token: \n" + assetToken);
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
}
