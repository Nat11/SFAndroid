package com.testapp.android.subcontractor;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.testapp.android.R;
import com.testapp.android.utils.DevicePairingSimulator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SFAssetsActivity extends AppCompatActivity {

    private TextView tvPairing;
    private Button btnCreate;
    private RelativeLayout pairingLayout;
    private ProgressDialog progressDialog;
    ProgressBar pb;
    private String devicePairingName;
    private String did;
    private String tokenName;
    private String serialNumber;
    EditText etAssetName, etAssetLocation;
    private EditText etEndDate;
    Calendar myCalendar;
    private long endDateInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sfassets);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pairingLayout = (RelativeLayout) findViewById(R.id.pairing_layout);
        btnCreate = (Button) findViewById(R.id.btn_createAsset);
        tvPairing = (TextView) findViewById(R.id.tv_pairing);
        pb = (ProgressBar) findViewById(R.id.pbHeader);
        etAssetName = (EditText) findViewById(R.id.input_assetName);
        etAssetLocation = (EditText) findViewById(R.id.input_assetLocation);
        etEndDate = (EditText) findViewById(R.id.input_endUsage);

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SFAssetsActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        devicePairingName = DevicePairingSimulator.generateDevicePairingName();
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
                tvPairing.setText(R.string.device_detected);
                pb.setVisibility(View.INVISIBLE);
                DevicePairingSimulator.createPairDialog(SFAssetsActivity.this, devicePairingName, progressDialog, pairingLayout);
            }
        }, 2500);

        //Generate asset metadata then create actor token
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                did = DevicePairingSimulator.generateAssetValues()[0];
                tokenName = DevicePairingSimulator.generateAssetValues()[1];
                serialNumber = DevicePairingSimulator.generateAssetValues()[2];

                if (validateFields(new EditText[]{etAssetName, etAssetLocation})) {

                    Intent i = new Intent(SFAssetsActivity.this, ClientSelectActivity.class);
                    i.putExtra("did", did);
                    i.putExtra("tokenName", tokenName);
                    i.putExtra("sn", serialNumber);
                    i.putExtra("assetName", etAssetName.getText().toString());
                    i.putExtra("assetLocation", etAssetLocation.getText().toString());
                    i.putExtra("endUsageDate", endDateInMillis);
                    startActivity(i);
                }
            }
        });
    }

    private boolean validateFields(EditText[] fields) {
        for (EditText currentField : fields) {
            if (currentField.getText().toString().trim().length() <= 0) {
                currentField.setError("Required field");
                return false;
            }
        }
        return true;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        endDateInMillis = myCalendar.getTime().getTime();
        etEndDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SFAssetsActivity.this, SubcontractorNavActivity.class));
        finish();
        moveTaskToBack(true);
    }
}
