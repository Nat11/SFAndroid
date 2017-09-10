package com.testapp.android.subcontractor;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.Model.Asset;
import com.testapp.android.Model.Contact;
import com.testapp.android.R;
import com.testapp.android.subcontractor.Bluetooth.BTMainActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class RequestDetailsActivity extends SalesforceActivity implements View.OnClickListener {

    private TextView tvOwnerName, tvAssetName, tvAccount, tvAssetLocation, tvInfo;
    private Button btnDeviceInstallation;
    private RestClient client;
    private String ownerName, contactId, accountId;
    private Contact clientContact;
    private Asset asset;
    private String accessToken;

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
        delegate.setContentView(R.layout.activity_request_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(RequestDetailsActivity.this);
            }
        });
        tvOwnerName = (TextView) findViewById(R.id.fullName);
        tvAccount = (TextView) findViewById(R.id.account);
        tvAssetLocation = (TextView) findViewById(R.id.assetLocation);
        tvAssetName = (TextView) findViewById(R.id.assetName);
        tvInfo = (TextView) findViewById(R.id.txtInfo);

        String activityId = getIntent().getStringExtra("ActivityId");

        if (activityId.equals("Done")) {
            asset = (Asset) getIntent().getSerializableExtra("assetDone");
            findViewById(R.id.deviceInstallation).setVisibility(View.INVISIBLE);
            tvInfo.setVisibility(View.INVISIBLE);
        } else if (activityId.equals("Pending")) {
            asset = (Asset) getIntent().getSerializableExtra("assetPending");
            btnDeviceInstallation = (Button) findViewById(R.id.deviceInstallation);
            btnDeviceInstallation.setVisibility(View.VISIBLE);
            btnDeviceInstallation.setOnClickListener(this);
        }
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        accessToken = client.getAuthToken();
        try {
            onFetchContactUser(asset.getOwnerId());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onFetchContactUser(String userId) throws UnsupportedEncodingException {
        sendRequest("SELECT Name, ContactId, AccountId FROM User WHERE Id='" + userId + "'", "contact");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deviceInstallation:
                Intent i = new Intent(RequestDetailsActivity.this, BTMainActivity.class);
                i.putExtra("asset", asset);
                i.putExtra("clientContact", clientContact);
                i.putExtra("accessToken", accessToken);
                startActivity(i);
        }
    }

    private void sendRequest(String soql, final String action) throws UnsupportedEncodingException {

        final RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);

        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray records = result.asJSONObject().getJSONArray("records");

                            switch (action) {
                                case "contact":
                                    ownerName = records.getJSONObject(0).getString("Name");
                                    contactId = records.getJSONObject(0).getString("ContactId");
                                    accountId = records.getJSONObject(0).getString("AccountId");
                                    sendRequest("SELECT Name FROM Account WHERE " +
                                            "Id='" + accountId + "'", "account");
                                    break;

                                case "account":
                                    String accountName = records.getJSONObject(0).getString("Name");
                                    clientContact = new Contact(contactId, ownerName, accountId, accountName);
                                    tvOwnerName.setText(ownerName);
                                    tvAssetName.setText(asset.getName());
                                    tvAccount.setText(accountName);
                                    tvAssetLocation.setText(asset.getLocation());
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
                        Log.d("error", RequestDetailsActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()));
                        Toast.makeText(RequestDetailsActivity.this,
                                RequestDetailsActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
