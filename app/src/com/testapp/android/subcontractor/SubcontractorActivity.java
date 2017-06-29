package com.testapp.android.subcontractor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.testapp.android.R;

public class SubcontractorActivity extends SalesforceActivity {

    @Override
    public void onResume(RestClient client) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcontractor);
    }

    public void onLogoutClick(View v) {
        SalesforceSDKManager.getInstance().logout(this);
    }
}
