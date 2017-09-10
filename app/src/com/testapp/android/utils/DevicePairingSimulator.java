package com.testapp.android.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;
import com.testapp.android.R;
import com.testapp.android.subcontractor.SubcontractorNavActivity;

import java.util.Random;

public class DevicePairingSimulator {

    //Simulate Pairing Dialog
    public static void createPairDialog(final Activity activity, final String devicePairingName, final ProgressDialog progressDialog, final RelativeLayout rl) {
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setMessage("Device " + devicePairingName + " would like to pair with your phone")
                .setCancelable(false)
                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        final View customLayout = LayoutInflater.from(activity).inflate(R.layout.pincode_layout, null);
                        final EditText pinCode = (EditText) customLayout.findViewById(R.id.pin_code);

                        final AlertDialog.Builder newAlertBuilder = new AlertDialog.Builder(activity);
                        newAlertBuilder.setTitle("Pairing Request").setMessage("Please Enter Pin Code").setView(customLayout)
                                .setCancelable(false)
                                .setPositiveButton("Pair", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (pinCode.getText().toString().equals("0000")) {
                                            dialogInterface.cancel();
                                            createProgressDialog(progressDialog, rl);
                                        } else {
                                            Toast.makeText(activity, "Wrong Pin", Toast.LENGTH_SHORT).show();
                                            createPairDialog(activity, devicePairingName, progressDialog, rl);
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
                        activity.finish();
                        activity.startActivity(new Intent(activity, SubcontractorNavActivity.class));
                    }
                });

        AlertDialog alert = alertBuilder.create();
        alert.setTitle("Pairing Request");
        if (!activity.isFinishing())
            alert.show();
    }

    //Generate Random device pairing name
    public static String generateDevicePairingName() {
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

    //Simulate pairing delay and generate values
    private static void createProgressDialog(final ProgressDialog progressDialog, final RelativeLayout rl) {
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
                rl.setVisibility(View.VISIBLE);
                generateAssetValues();
            }
        });
    }

    //Generate Random device values
    public static String[] generateAssetValues() {
        Long t = System.currentTimeMillis() / 1000; //current timestamp
        String did = "device" + t;
        String tokenName = "token" + t;
        String serialNumber = "SN" + t;
        return new String[]{did, tokenName, serialNumber};
    }
}
