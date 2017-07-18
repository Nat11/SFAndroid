package com.testapp.android.subcontractor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.android.Model.Asset;
import com.testapp.android.Model.ClientRequest;
import com.testapp.android.R;

public class RequestDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private ClientRequest request;
    private TextView tvFullName, tvAccount, tvEmail, tvAssetType, tvAssetLocation, tvInfo;
    private Button btnDeviceInstallation;
    private String requestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvFullName = (TextView) findViewById(R.id.FBFullName);
        tvAccount = (TextView) findViewById(R.id.FBAccount);
        tvEmail = (TextView) findViewById(R.id.FBEmail);
        tvAssetType = (TextView) findViewById(R.id.FBAssetType);
        tvAssetLocation = (TextView) findViewById(R.id.FBAssetLocation);
        tvInfo = (TextView) findViewById(R.id.txtInfo);

        request = (ClientRequest) getIntent().getSerializableExtra("request");
        requestId = getIntent().getStringExtra("requestId");
        String activityId = getIntent().getStringExtra("ActivityId");

        if (activityId.equals("Done")) {
            findViewById(R.id.deviceInstallation).setVisibility(View.INVISIBLE);
            tvInfo.setVisibility(View.INVISIBLE);
        } else if (activityId.equals("Pending")) {
            btnDeviceInstallation = (Button) findViewById(R.id.deviceInstallation);
            btnDeviceInstallation.setVisibility(View.VISIBLE);
            btnDeviceInstallation.setOnClickListener(this);
        }
        tvFullName.setText(request.getFullName());
        tvAccount.setText(request.getAccountName());
        tvEmail.setText(request.getEmail());
        tvAssetType.setText(request.getAssetType());
        tvAssetLocation.setText(request.getInstallationLocation());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.deviceInstallation:
                Intent i = new Intent(RequestDetailsActivity.this, AssetActivity.class);
                i.putExtra("requestId", requestId);
                i.putExtra("assetType", request.getAssetType());
                i.putExtra("assetLocation", request.getInstallationLocation());
                startActivity(i);
        }
    }
}
