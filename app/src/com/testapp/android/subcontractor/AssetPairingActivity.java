package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.testapp.android.Model.Asset;
import com.testapp.android.Model.Contact;
import com.testapp.android.R;
import com.testapp.android.utils.DevicePairingSimulator;

public class AssetPairingActivity extends AppCompatActivity {

    private TextView tvPairing;
    private Button btnCreate;
    private RelativeLayout relativeLayout;
    private ProgressDialog progressDialog;
    private String did;
    private String tokenName;
    private String serialNumber;
    private String devicePairingName;
    Context context;
    ProgressBar pb;
    private Asset asset;
    private Contact clientContact;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_pairing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        btnCreate = (Button) findViewById(R.id.btn_createAsset);
        tvPairing = (TextView) findViewById(R.id.tv_pairing);
        pb = (ProgressBar) findViewById(R.id.pbHeader);
        devicePairingName = DevicePairingSimulator.generateDevicePairingName();
        context = this;
        asset = (Asset) getIntent().getSerializableExtra("asset");
        accessToken = getIntent().getStringExtra("accessToken");
        clientContact = (Contact) getIntent().getSerializableExtra("clientContact");
    }

    @Override
    public void onResume() {
        super.onResume();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Device Metadata...");
        progressDialog.setCanceledOnTouchOutside(false);
        pb.setVisibility(View.VISIBLE);

        //Simulate delay to display pairing request
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvPairing.setText("Device Detected !");
                pb.setVisibility(View.INVISIBLE);
                DevicePairingSimulator.createPairDialog(AssetPairingActivity.this, devicePairingName, progressDialog, relativeLayout);
            }
        }, 2500);

        //Generate asset metadata then create actor token
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                did = DevicePairingSimulator.generateAssetValues()[0];
                tokenName = DevicePairingSimulator.generateAssetValues()[1];
                serialNumber = DevicePairingSimulator.generateAssetValues()[2];

                Intent i = new Intent(AssetPairingActivity.this, AssetInstallationActivity.class);
                i.putExtra("assetId", asset.getId());
                i.putExtra("did", did);
                i.putExtra("tokenName", tokenName);
                i.putExtra("sn", serialNumber);
                i.putExtra("assetName", asset.getName());
                i.putExtra("assetLocation", asset.getLocation());
                i.putExtra("clientContact", clientContact);
                i.putExtra("accessToken", accessToken);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AssetPairingActivity.this, RequestDetailsActivity.class));
        finish();
        moveTaskToBack(true);
    }
}

