package com.testapp.android.subcontractor.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.testapp.android.Adapter.DeviceListAdapter;
import com.testapp.android.Model.Asset;
import com.testapp.android.Model.Contact;
import com.testapp.android.R;
import com.testapp.android.subcontractor.AssetInstallationActivity;
import com.testapp.android.subcontractor.DeviceMetadataActivity;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class DeviceListActivity extends AppCompatActivity {
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private Button btnNext;
    private static String dName = "";
    private static String dAddress = "";
    private boolean paired;
    private String did, tokenName, SN;
    private Asset asset;
    private Contact clientContact;
    private String accessToken;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");
        mListView = (ListView) findViewById(R.id.lv_paired);
        btnNext = (Button) findViewById(R.id.btn_next);

        asset = (Asset) getIntent().getSerializableExtra("asset");
        accessToken = getIntent().getStringExtra("accessToken");
        clientContact = (Contact) getIntent().getSerializableExtra("clientContact");

        mAdapter = new DeviceListAdapter(this);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
            @Override
            public void onPairButtonClick(int position, String deviceName, String deviceAddress) {
                BluetoothDevice device = mDeviceList.get(position);
                dName = deviceName;
                dAddress = deviceAddress;
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    unpairDevice(device);
                } else {
                    showToast("Pairing...");
                    pairDevice(device);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long t = System.currentTimeMillis() / 1000; //current timestamp
                did = dName + t;
                tokenName = "token" + t;
                SN = "SN " + dAddress; //SN + MAC @

                if (paired) {
                    if (asset != null && !accessToken.isEmpty() && clientContact != null) {
                        Intent i = new Intent(DeviceListActivity.this, AssetInstallationActivity.class);
                        i.putExtra("assetId", asset.getId());
                        i.putExtra("did", did);
                        i.putExtra("tokenName", tokenName);
                        i.putExtra("sn", SN);
                        i.putExtra("assetName", asset.getName());
                        i.putExtra("assetLocation", asset.getLocation());
                        i.putExtra("clientContact", clientContact);
                        i.putExtra("accessToken", accessToken);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(DeviceListActivity.this, DeviceMetadataActivity.class);
                        i.putExtra("did", did);
                        i.putExtra("tokenName", tokenName);
                        i.putExtra("sn", SN);
                        startActivity(i);
                        finish();
                    }

                } else
                    showToast("Pairing was unsuccessful");
            }
        });

        mListView.setAdapter(mAdapter);
        registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);
        super.onDestroy();
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    showToast("Paired");
                    paired = true;
                    Log.d("BTPaired", String.valueOf(paired));

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("Unpaired");
                    paired = false;
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

}