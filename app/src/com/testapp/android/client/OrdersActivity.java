package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceListActivity;
import com.testapp.android.Adapter.AssetAdapter;
import com.testapp.android.Model.Asset;
import com.testapp.android.R;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends SalesforceListActivity {

    Context context;
    private RestClient client;
    private static List<String> assetIds = new ArrayList<>();
    private static List<Asset> assets = new ArrayList<>();
    private String ownerId;
    private AssetAdapter listAdapter;
    private String accessToken;
    private SwipeRefreshLayout mySwipeRefreshLayout;

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
        delegate.setContentView(R.layout.activity_orders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        // Enable the Back Up Arrow
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(OrdersActivity.this);
            }
        });
        context = this;
        accessToken = getIntent().getStringExtra("accessToken");
        mySwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        ownerId = client.getClientInfo().userId;
        //Execute async task to load orders in background then display them
        new ClientOrders().execute();

        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("Refresh", "onRefresh called from SwipeRefreshLayout");
                        assets.clear();
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        new ClientOrders().execute();
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    private void onFetchShippedAssets(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("ShippedAsset", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                assetIds.add(records.getJSONObject(i).getString("Id"));
                                String assetId = records.getJSONObject(i).getString("Id");
                                String assetName = records.getJSONObject(i).getString("Name");
                                String assetLocation = records.getJSONObject(i).getString("location__c");
                                String assetOwnerId = records.getJSONObject(i).getString("OwnerId");
                                String assetStatus = records.getJSONObject(i).getString("Status");

                                Asset asset = new Asset(assetId, assetName, assetOwnerId, assetLocation, assetStatus);
                                assets.add(asset);
                            }
                            listAdapter = new AssetAdapter(context, R.layout.list_asset_item, assets);
                            setListAdapter(listAdapter);
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
                        Toast.makeText(OrdersActivity.this,
                                OrdersActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //get asset id if clicked
        if (assets.get(position).getStatus().equals("Shipped")) {
            String assetId = assets.get(position).getId();
            Intent i = new Intent(OrdersActivity.this, ServicesActivity.class);
            i.putExtra("assetId", assetId);
            i.putExtra("accessToken", accessToken);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(OrdersActivity.this, ClientNavActivity.class));
    }

    private class ClientOrders extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            assets.clear();
            progressDialog = ProgressDialog.show(OrdersActivity.this, "Please wait", "Loading your Shipped Orders", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            //Display orders list when request is done
            findViewById(android.R.id.list).setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String... params) {

            try {
                onFetchShippedAssets("SELECT Id, Name, OwnerId, location__c, Status FROM Asset WHERE OwnerId = '" + ownerId +
                        "' AND Status IN('" + "Purchased" + "', '" + "Shipped" + "', '" + "Registered" + "', '" + "Installed" + "')");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
