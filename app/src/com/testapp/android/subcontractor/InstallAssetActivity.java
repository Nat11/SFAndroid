package com.testapp.android.subcontractor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.testapp.android.R;
import com.testapp.android.utils.SubmissionStatusDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class InstallAssetActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 1;
    Button btnSendToken;
    private String assetId, accessToken, subcontractorContact;
    private RequestQueue MyRequestQueue;
    private static final String STATE_ENABLE_ROTATION = "state_debug_rotation";
    private static final String STATE_SHOW_CPS = "state_debug_cps";
    private static final String STATE_SLOW_ANIMATION = "state_debug_slow_animation;";
    private static final String STATE_ICON_TYPE = "state_icon_type";
    private SubmissionStatusDrawable mDrawable;
    private File assetTokenFile;
    private Context context;

    /**
     * intent action used to indicate the completion of a handover transfer
     */
    public static final String ACTION_BT_OPP_TRANSFER_DONE =
            "android.btopp.intent.action.BT_OPP_TRANSFER_DONE";

    /**
     * intent extra used to indicate the success of a handover transfer
     */
    public static final String EXTRA_BT_OPP_TRANSFER_STATUS =
            "android.btopp.intent.extra.BT_OPP_TRANSFER_STATUS";

    public static final int HANDOVER_TRANSFER_STATUS_SUCCESS = 0;
    public static final int HANDOVER_TRANSFER_STATUS_FAILURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_asset);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

       /* mDrawable = new SubmissionStatusDrawable(this);

        if (savedInstanceState != null) {
            //noinspection WrongConstant
            mDrawable.setIconType(savedInstanceState.getInt(STATE_ICON_TYPE));
            mDrawable.setDebugEnableRotation(savedInstanceState.getBoolean(STATE_ENABLE_ROTATION));
            mDrawable.setDebugShowControlPoints(savedInstanceState.getBoolean(STATE_SHOW_CPS));
            mDrawable.setDebugSlowDownAnimation(savedInstanceState.getBoolean(STATE_SLOW_ANIMATION));
        }

        //noinspection deprecation
        findViewById(R.id.submission_status_view).setBackgroundDrawable(mDrawable);*/

        assetTokenFile = (File) getIntent().getExtras().get("assetTokenFile");

        assetId = getIntent().getStringExtra("assetId");
        accessToken = getIntent().getStringExtra("accessToken");
        subcontractorContact = getIntent().getStringExtra("subcontactrorContact");
        MyRequestQueue = Volley.newRequestQueue(this);
        btnSendToken = (Button) findViewById(R.id.btn_sendToken);
        btnSendToken.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_install, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.home) {
            startActivity(new Intent(InstallAssetActivity.this, SubcontractorNavActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sendToken:
                sendAssetTokenFile();
                updateAssetInstalled(assetId);
        }
    }


    private void sendAssetTokenFile() {

        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        File file = new File(Environment.getExternalStorageDirectory() + "/Documents" + "/AssetToken.txt");
        sharingIntent.setType("text/plain");
        sharingIntent.setPackage("com.android.bluetooth");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

        //Broadcasts when bond state changes (ie:pairing)
      /* IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);*/

        startActivity(Intent.createChooser(sharingIntent, "Share File"));
        //startActivity(new Intent(InstallAssetActivity.this, SubcontractorNavActivity.class));
    }

    /**
     * Broadcast Receiver that detects if data transfer is done
     */
    /*private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("Status", "test");

            final String action = intent.getAction();

            if (action.equals(ACTION_BT_OPP_TRANSFER_DONE)) {
                int handoverStatus = intent.getIntExtra(EXTRA_BT_OPP_TRANSFER_STATUS,
                        HANDOVER_TRANSFER_STATUS_FAILURE);

                Log.d("Status", String.valueOf(handoverStatus));

                if (handoverStatus == HANDOVER_TRANSFER_STATUS_SUCCESS) {
                    //mDrawable.setIconType(SubmissionStatusDrawable.DONE);
                    Toasty.success(getApplicationContext(), "Asset token was successfully sent to your device", Toast.LENGTH_LONG, true).show();
                }
                if (handoverStatus == HANDOVER_TRANSFER_STATUS_FAILURE) {
                    mDrawable.setIconType(SubmissionStatusDrawable.LATE);
                }
            }
        }
    };*/
    private void updateAssetInstalled(String assetId) {
        String url = "https://arisiot-developer-edition.eu11.force.com/demo/services/data/v39.0/sobjects/Asset/" + assetId;
        StringRequest MyStringRequest = new StringRequest(Request.Method.PATCH, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toasty.success(getApplicationContext(), "Asset token was successfully sent to your device", Toast.LENGTH_LONG, true).show();
                //mDrawable.setIconType(SubmissionStatusDrawable.DONE);

            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {

                // As of f605da3 the following should work
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> params2 = new HashMap<String, String>();
                params2.put("Status", "Installed");
                params2.put("Installed_By__c", subcontractorContact);
                params2.put("InstallDate", String.valueOf(System.currentTimeMillis())); //Install Date when installation is complete
                return new JSONObject(params2).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                String bearer = "Bearer ".concat(accessToken);
                headers.put("Authorization", bearer);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        MyRequestQueue.add(MyStringRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*outState.putBoolean(STATE_ENABLE_ROTATION, mDrawable.getDebugEnableRotation());
        outState.putBoolean(STATE_SHOW_CPS, mDrawable.getDebugShowControlPoints());
        outState.putBoolean(STATE_SLOW_ANIMATION, mDrawable.getDebugSlowAnimation());
        outState.putInt(STATE_ICON_TYPE, mDrawable.getIconType());*/
    }
}
