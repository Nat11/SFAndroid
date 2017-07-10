package com.testapp.android.subcontractor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.android.R;

import java.util.Random;

import static android.app.PendingIntent.getActivity;

public class AssetActivity extends AppCompatActivity {

    private EditText etAssetName, pinCode;
    private TextView tvPairing;
    private Button btnCreate;
    private RelativeLayout relativeLayout;
    private ProgressDialog progressDialog;
    private String did, tokenName, serialNumber, assetName, accountId, contactId, devicePairingName;
    Context context;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative);
        etAssetName = (EditText) findViewById(R.id.input_assetName);
        btnCreate = (Button) findViewById(R.id.btn_createAsset);
        tvPairing = (TextView) findViewById(R.id.tv_pairing);
        pb = (ProgressBar) findViewById(R.id.pbHeader);
        devicePairingName = generateDevicePairingName();
        context = this;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);

        pb.setVisibility(View.VISIBLE);

        //Simulate delay to display pairing request
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvPairing.setText("Device Detected !");
                pb.setVisibility(View.INVISIBLE);
                createPairDialog();
            }
        }, 3500);

        //Generate asset metada then create actor token
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assetName = etAssetName.getText().toString();
                if (assetName.isEmpty()) {
                    Toast.makeText(context, "Please enter an asset name", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(AssetActivity.this, ClientSelectActivity.class);
                    i.putExtra("did", did);
                    i.putExtra("tokenName", tokenName);
                    i.putExtra("sn", serialNumber);
                    i.putExtra("assetName", assetName);
                    startActivity(i);
                }
            }
        });
    }

    public void createProgressDialog() {
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
                Toast.makeText(AssetActivity.this, "Asset Metadata collected successfully", Toast.LENGTH_LONG).show();
                relativeLayout.setVisibility(View.VISIBLE);
                generateAssetValues();
            }
        });
    }

    public void createPairDialog() {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AssetActivity.this);
        alertBuilder.setMessage("Device " + devicePairingName + " would like to pair with your phone")
                .setCancelable(false)
                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        final View customLayout = LayoutInflater.from(context).inflate(R.layout.pincode_layout, null);
                        pinCode = (EditText) customLayout.findViewById(R.id.pin_code);

                        final AlertDialog.Builder newAlertBuilder = new AlertDialog.Builder(AssetActivity.this);
                        newAlertBuilder.setTitle("Pairing Request").setMessage("Please Enter Pin Code").setView(customLayout)
                                .setCancelable(false)
                                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (pinCode.getText().toString().equals("0000")) {
                                            dialogInterface.cancel();
                                            createProgressDialog();
                                        } else {
                                            Toast.makeText(AssetActivity.this, "Wrong Pin", Toast.LENGTH_SHORT).show();
                                            createPairDialog();
                                        }
                                    }
                                }).create()
                                .show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        finish();
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle("Pairing Request");
        alert.show();
    }

    public void generateAssetValues() {
        Long t = System.currentTimeMillis() / 1000; //current timestamp
        did = "device" + t;
        tokenName = "token" + t;
        serialNumber = "SN" + t;
    }

    //Generate Random device pairing name
    public String generateDevicePairingName() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String randStr = sb.toString().toUpperCase();

        Random r = new Random();
        String randInt = "";
        for (int i = 0; i < 3; i++) {
            randInt += r.nextInt(10);
        }
        return randStr + "-" + randInt;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(AssetActivity.this, ClientSelectActivity.class));
    }
}

