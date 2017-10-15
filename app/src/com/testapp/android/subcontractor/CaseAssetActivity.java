package com.testapp.android.subcontractor;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
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
import com.testapp.android.R;
import com.testapp.android.client.ClientNavActivity;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

public class CaseAssetActivity extends SalesforceActivity implements View.OnClickListener {

    private RestClient client;
    private String assetId, assetName;
    private TextView tvCaseNumber, tvAssetName, tvCaseReason;
    private Button btnHomepage;

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
        delegate.setContentView(R.layout.activity_case_asset);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);

        assetId = getIntent().getStringExtra("assetId");
        assetName = getIntent().getStringExtra("assetName");
        tvCaseNumber = (TextView) findViewById(R.id.caseNumber);
        tvAssetName = (TextView) findViewById(R.id.assetName);
        tvCaseReason = (TextView) findViewById(R.id.caseReason);
        btnHomepage = (Button) findViewById(R.id.homepageBtn);
        btnHomepage.setOnClickListener(this);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        new assetCase().execute();
    }

    private void onFetchAssetCase(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("CaseResult", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            String caseReason = records.getJSONObject(0).getString("Reason");
                            String caseNumber = records.getJSONObject(0).getString("CaseNumber");
                            tvCaseNumber.setText(caseNumber);
                            tvCaseReason.setText(caseReason);
                            tvAssetName.setText(assetName);

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
                        Toast.makeText(CaseAssetActivity.this,
                                CaseAssetActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CaseAssetActivity.this, SubcontractorNavActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.homepageBtn:
                startActivity(new Intent(this, SubcontractorNavActivity.class));
        }
    }

    private class assetCase extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Display orders list when request is done
        }

        protected Void doInBackground(String... params) {

            try {
                onFetchAssetCase("SELECT Reason, CaseNumber FROM Case WHERE AssetId = '" + assetId + "'");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
