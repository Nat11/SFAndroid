package com.testapp.android.subcontractor;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.testapp.android.Model.Contact;
import com.testapp.android.R;
import com.testapp.android.jwt.jwtToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AssetInstallationActivity extends AppCompatActivity implements View.OnClickListener {

    private String assetId, did, tokenName, serialNumber, assetName, assetLocation;
    private Contact clientContact;
    Button btnCreateToken;
    TextView tvConfirmInstallation;
    RequestQueue MyRequestQueue;
    Context context;
    private String accessToken;
    private EditText etEndDate;
    Calendar myCalendar;
    private long endDateInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_installation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MyRequestQueue = Volley.newRequestQueue(this);
        context = this;
        tvConfirmInstallation = (TextView) findViewById(R.id.tv_confirmInstallation);
        etEndDate = (EditText) findViewById(R.id.endDate);
        accessToken = getIntent().getStringExtra("accessToken");
        assetId = getIntent().getStringExtra("assetId");
        did = getIntent().getStringExtra("did");
        tokenName = getIntent().getStringExtra("tokenName");
        serialNumber = getIntent().getStringExtra("sn");
        assetName = getIntent().getStringExtra("assetName");
        assetLocation = getIntent().getStringExtra("assetLocation");
        clientContact = (Contact) getIntent().getSerializableExtra("clientContact");

        tvConfirmInstallation.setText("Your asset will be linked to " + clientContact.getContactName() + " from " +
                clientContact.getAccountName() + ". \n\nClick Next to proceed.");

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AssetInstallationActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnCreateToken = (Button) findViewById(R.id.btn_createAssetToken);
        btnCreateToken.setOnClickListener(this);
    }

    public void createActorToken() throws JSONException {
        String actorToken = null;
        updateAssetSN(assetId);
        Intent i = new Intent(AssetInstallationActivity.this, TokenActivity.class);

        actorToken = new jwtToken().createToken(did, tokenName, clientContact.getAccountId(), serialNumber, clientContact.getContactId(),
                assetName, assetLocation);

        Log.d("actorToken", actorToken);
        i.putExtra("actorToken", actorToken);
        startActivity(i);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        endDateInMillis = myCalendar.getTime().getTime();
        etEndDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void updateAssetSN(String assetId) {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/data/v39.0/sobjects/Asset/" + assetId;
        StringRequest MyStringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Toast.makeText(context, "Asset Serial Number updated", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                params2.put("SerialNumber", serialNumber);
                params2.put("UsageEndDate", String.valueOf(endDateInMillis));   //end date
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_createAssetToken:
                try {
                    if (!etEndDate.getText().toString().trim().equals("")) {
                        etEndDate.setError(null);
                        createActorToken();
                    } else etEndDate.setError("Required field");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }
}
