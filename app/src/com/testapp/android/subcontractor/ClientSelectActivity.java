package com.testapp.android.subcontractor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceListActivity;
import com.testapp.android.R;
import com.testapp.android.jwt.jwtToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ClientSelectActivity extends SalesforceListActivity implements View.OnClickListener {

    private RestClient client;
    private ArrayAdapter<String> listAdapter;
    private String did, tokenName, serialNumber, assetName, contactId = "", accountId = "";
    private static List<String> contactKey = new ArrayList<>();
    private static List<String> accountKey = new ArrayList<>();
    Button btnCreateToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Select your Customer");
        toolbar.showOverflowMenu();
        listAdapter = new ArrayAdapter<String>(this, R.layout.listrow, new ArrayList<String>());
        setListAdapter(listAdapter);
        did = getIntent().getStringExtra("did");
        tokenName = getIntent().getStringExtra("tokenName");
        serialNumber = getIntent().getStringExtra("sn");
        assetName = getIntent().getStringExtra("assetName");
        btnCreateToken = (Button) findViewById(R.id.btn_createToken);
        btnCreateToken.setOnClickListener(this);
    }

    @Override
    public void onResume(RestClient client) {
        this.client = client;
        try {
            onFetchContacts("SELECT Id, Name, AccountId FROM Contact WHERE Flag__c = 'client'");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void onFetchContacts(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Log.d("client", result.toString());
                            listAdapter.clear();
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                contactKey.add(records.getJSONObject(i).getString("Id"));
                                accountKey.add(records.getJSONObject(i).getString("AccountId"));
                                listAdapter.add(records.getJSONObject(i).getString("Name"));
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
                        Toast.makeText(ClientSelectActivity.this,
                                ClientSelectActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        v.setSelected(true);
        //l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        contactId = contactKey.get(position);
    }

    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ClientSelectActivity.this, AssetActivity.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_createToken:
                try {
                    createActorToken();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }
    }

    public void createActorToken() throws JSONException {
        String actorToken = null;
        accountId = accountKey.get(0);
        Intent i = new Intent(ClientSelectActivity.this, TokenActivity.class);

        if (contactId.trim().equals(""))
            actorToken = new jwtToken().createTokenNoContact(did, tokenName, accountId, serialNumber, assetName);

        else if (accountId.trim().equals("") && contactId.trim().equals(""))
            Toast.makeText(this, "Error retrieving Account information", Toast.LENGTH_SHORT).show();

        else
            actorToken = new jwtToken().createToken(did, tokenName, accountId, serialNumber, contactId, assetName);

        i.putExtra("actorToken", actorToken);
        startActivity(i);
    }
}