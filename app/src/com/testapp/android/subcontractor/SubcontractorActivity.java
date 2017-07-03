package com.testapp.android.subcontractor;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.Model.Asset;
import com.testapp.android.R;

public class SubcontractorActivity extends SalesforceActivity {

    private Asset asset;
    private EditText tokenName, assetId, assetSN, assetName, contactName, accountName;
    private RestClient client;
    private Button btnPair, btnCreate;
    private ScrollView scrollview;
    private ProgressDialog progressDialog;

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        btnPair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //simulate pairing by showing progress dialog and generating values
                progressDialog.show();
                Runnable progressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.cancel();
                    }
                };
                Handler pdCanceller = new Handler();
                pdCanceller.postDelayed(progressRunnable, 1500);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        scrollview.setVisibility(View.VISIBLE);
                        generateValues();
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcontractor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Asset Registration");
        toolbar.showOverflowMenu();

        scrollview = (ScrollView) findViewById(R.id.scroll);
        tokenName = (EditText) findViewById(R.id.input_tokenName);
        assetId = (EditText) findViewById(R.id.input_id);
        assetSN = (EditText) findViewById(R.id.input_sn);
        assetName = (EditText) findViewById(R.id.input_assetName);
        contactName = (EditText) findViewById(R.id.input_contact);
        accountName = (EditText) findViewById(R.id.input_account);
        btnCreate = (Button) findViewById(R.id.btn_createAsset);
        btnPair = (Button) findViewById(R.id.btn_pairing);

        Long t = System.currentTimeMillis() / 1000; //current timestamp
        asset = new Asset("thermoToken", "thermo001", t.toString(), "Thermostat living room");
    }

    public void onLogoutClick(View v) {
        SalesforceSDKManager.getInstance().logout(this);
    }

    public void generateValues() {
        tokenName.setText(asset.getAssetTokenName());
        assetId.setText(asset.getDeviceId());
        assetSN.setText(asset.getAssetSN());
        assetName.setText(asset.getAssetName());
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SubcontractorActivity.this, SubcontractorNavActivity.class));
    }
}

