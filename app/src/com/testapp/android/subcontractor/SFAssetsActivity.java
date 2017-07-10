package com.testapp.android.subcontractor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceListActivity;
import com.testapp.android.R;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SFAssetsActivity extends SalesforceListActivity {

    private RestClient client;
    private ArrayAdapter<String> listAdapter;
    private static List<String> assetIds = new ArrayList<>();
    private static List<String> ownerIds = new ArrayList<>();
    private String assetStatus;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sfassets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Your Salesforce Assets");
        toolbar.showOverflowMenu();
        context = this;
        listAdapter = new ArrayAdapter<String>(context, R.layout.listrow, new ArrayList<String>());
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        String ownerId = client.getClientInfo().userId;
        try {
            onFetchAssets("SELECT Id, Name, OwnerId, Status FROM Asset WHERE OwnerId = '" + ownerId + "'");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onFetchAssets(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Log.d("Asset", result.toString());
                            //listAdapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                assetIds.add(records.getJSONObject(i).getString("Id"));
                                ownerIds.add(records.getJSONObject(i).getString("OwnerId"));
                                assetStatus = records.getJSONObject(i).getString("Status");
                                String assetName = records.getJSONObject(i).getString("Name");

                                if (assetStatus.equals("Installed")) {
                                    assetName += "\n Installed";
                                }
                                listAdapter.add(assetName);
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
                        Toast.makeText(SFAssetsActivity.this,
                                SFAssetsActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


}
