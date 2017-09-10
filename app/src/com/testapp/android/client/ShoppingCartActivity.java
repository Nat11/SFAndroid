package com.testapp.android.client;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.testapp.android.Adapter.CartAdapter;
import com.testapp.android.Adapter.CustomAdapter;
import com.testapp.android.Model.Product;
import com.testapp.android.R;
import com.testapp.android.jwt.JWTUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ShoppingCartActivity extends ListActivity implements View.OnClickListener {

    private List<Product> products = new ArrayList<>();
    Context context;
    public static List<Product> confirmedProducts;
    private Button btnConfirmOrder;
    private CartAdapter adapter;
    RequestQueue MyRequestQueue;
    private String accessToken, accountId;

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
        delegate.setContentView(R.layout.activity_shopping_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        // Enable the Back Up Arrow
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(ShoppingCartActivity.this);
            }
        });

        products = (List<Product>) getIntent().getSerializableExtra("PRODUCTS");
        confirmedProducts = new ArrayList<>(products);
        context = this;
        accessToken = getIntent().getStringExtra("accessToken");
        accountId = getIntent().getStringExtra("accountId");
        MyRequestQueue = Volley.newRequestQueue(this);
        btnConfirmOrder = (Button) findViewById(R.id.confirmOrderBtn);
        btnConfirmOrder.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new CartAdapter(context, R.layout.list_cart_item, confirmedProducts);
        setListAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.confirmOrderBtn:
                // check if bucket is empty to create asset
                if (confirmedProducts.size() > 0) {
                    for (Product p : confirmedProducts) {
                        createAsset(p.getId(), p.getName());
                    }
                } else
                    Toast.makeText(context, "No orders in cart", Toast.LENGTH_SHORT).show();
        }
    }

    // Create asset in salesforce
    public void createAsset(final String productId, final String productName) {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/data/v39.0/sobjects/Asset";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Log.d("response", jsonResponse.toString());

                    Toasty.success(context, "Order confirmed", Toast.LENGTH_SHORT, true).show();

                    // Go to Confirmation screen and finish current activity
                    Intent i = new Intent(context, OrderConfirmedActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                } catch (JSONException e) {
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
                params2.put("Name", "Asset " + productName);
                params2.put("AccountId", accountId);
                params2.put("Status", "Purchased");
                params2.put("Product2Id", productId);
                params2.put("PurchaseDate", String.valueOf(System.currentTimeMillis())); //current date is milliseconds
                return new JSONObject(params2).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String bearer = "Bearer ".concat(accessToken);
                headers.put("Authorization", bearer);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }
}
