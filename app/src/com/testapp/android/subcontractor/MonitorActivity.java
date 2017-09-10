package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceListActivity;
import com.testapp.android.Adapter.AssetAdapter;
import com.testapp.android.Adapter.CaseAssetAdapter;
import com.testapp.android.Model.Asset;
import com.testapp.android.R;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MonitorActivity extends SalesforceListActivity {

    private String contactId;
    Context context;
    private RestClient client;
    private static List<Asset> assets = new ArrayList<>();
    private CaseAssetAdapter listAdapter;
    private static List<String> assetIds = new ArrayList<>();
    private TextView tv_empty;
    public static List<String> caseAssetIds = new ArrayList<>();

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
        delegate.setContentView(R.layout.activity_monitor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        // Enable the Back Up Arrow
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(MonitorActivity.this);
            }
        });

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();

        context = this;
        contactId = getIntent().getStringExtra("contactId");
        tv_empty = (TextView) findViewById(R.id.tv_empty);
        caseAssetIds.clear();
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        new assets().execute();
    }

    private void onFetchInstalledAssets(String soql, final String table) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("InstalledAsset", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            switch (table) {

                                case "Asset":
                                    for (int i = 0; i < records.length(); i++) {
                                        assetIds.add(records.getJSONObject(i).getString("Id"));
                                        String assetId = records.getJSONObject(i).getString("Id");
                                        String assetName = records.getJSONObject(i).getString("Name");
                                        String assetOwnerId = records.getJSONObject(i).getString("OwnerId");
                                        String assetLocation = records.getJSONObject(i).getString("location__c");
                                        String assetStatus = records.getJSONObject(i).getString("Status");

                                        Asset asset = new Asset(assetId, assetName, assetOwnerId, assetLocation, assetStatus);
                                        assets.add(asset);
                                    }
                                    if (!assets.isEmpty()) {
                                        listAdapter = new CaseAssetAdapter(context, R.layout.list_asset_case_item, assets);
                                        setListAdapter(listAdapter);
                                    } else {
                                        tv_empty.setVisibility(View.VISIBLE);
                                    }
                                    break;

                                case "Case":
                                    for (int i = 0; i < records.length(); i++) {
                                        String caseAssetId = records.getJSONObject(i).getString("AssetId");
                                        caseAssetIds.add(caseAssetId);
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
                        Toast.makeText(MonitorActivity.this,
                                MonitorActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
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
        String assetId = assetIds.get(position);

        if (caseAssetIds.contains(assetId)) {
            Intent i = new Intent(MonitorActivity.this, CaseAssetActivity.class);
            i.putExtra("assetId", assetId);
            i.putExtra("assetName", assets.get(position).getName());
            startActivity(i);
        }
    }

    private class assets extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            assets.clear();
            caseAssetIds.clear();
            //progressDialog = ProgressDialog.show(MonitorActivity.this, "Please wait", "Loading your assets", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //progressDialog.dismiss();
            //Display orders list when request is done
            findViewById(android.R.id.list).setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String... params) {

            try {
                onFetchInstalledAssets("SELECT Id, Name, OwnerId, location__c, Status FROM Asset WHERE Installed_By__c = '" + contactId +
                        "' AND Status = '" + "Installed" + "'", "Asset");

                onFetchInstalledAssets("SELECT Id, AssetId FROM Case WHERE Status != '" + "Closed" + "'", "Case");


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
