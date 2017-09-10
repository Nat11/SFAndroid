package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.testapp.android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ServicesActivity extends AppCompatActivity {

    private String assetLocation = "";
    private Button validate;
    private EditText etAssetLocation;
    private Spinner etServicesCompany;
    final String[] company = new String[1];
    RequestQueue MyRequestQueue;
    static Context context;
    private String accessToken;
    private String assetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        validate = (Button) findViewById(R.id.validate);
        etServicesCompany = (Spinner) findViewById(R.id.servicesCompany);
        etAssetLocation = (EditText) findViewById(R.id.input_assetLocation);
        context = this;
        accessToken = getIntent().getStringExtra("accessToken");
        assetId = getIntent().getStringExtra("assetId");
        MyRequestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        etServicesCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                company[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean fieldsOK = validate(new EditText[]{etAssetLocation});
                if (fieldsOK)
                    updateAsset(assetId);
            }
        });
    }

    private boolean validate(EditText[] fields) {
        for (EditText currentField : fields) {
            if (currentField.getText().toString().trim().equals("")) {
                currentField.setError("Required field");
                return false;
            }
        }
        return true;
    }

    // Update asset installationRequest Field in salesforce
    public void updateAsset(final String assetId) {

        assetLocation = etAssetLocation.getText().toString();

        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/data/v39.0/sobjects/Asset/" + assetId;
        StringRequest MyStringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    Log.d("responseRequest", response);
                    Toasty.success(context, "Your request was successfully saved", Toast.LENGTH_SHORT, true).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(context, ClientNavActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

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
                        Log.d("responseRequestError", obj.toString());

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
                params2.put("installationRequest__c", "true");
                params2.put("location__c", assetLocation);
                return new JSONObject(params2).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                Log.d("accessToken", accessToken);
                String bearer = "Bearer ".concat(accessToken);
                headers.put("Authorization", bearer);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }
}
