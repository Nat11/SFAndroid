package com.testapp.android.subcontractor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.testapp.android.R;
import com.testapp.android.utils.DevicePairingSimulator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DeviceMetadataActivity extends AppCompatActivity {

    private String did, tokenName, serialNumber;
    EditText etAssetName, etAssetLocation;
    private EditText etEndDate;
    Calendar myCalendar;
    private long endDateInMillis;
    Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_metadata);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        did = getIntent().getStringExtra("did");
        tokenName = getIntent().getStringExtra("tokenName");
        serialNumber = getIntent().getStringExtra("sn");

        etAssetName = (EditText) findViewById(R.id.input_assetName);
        etAssetLocation = (EditText) findViewById(R.id.input_assetLocation);
        etEndDate = (EditText) findViewById(R.id.input_endUsage);
        btnCreate = (Button) findViewById(R.id.btn_next);

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
                new DatePickerDialog(DeviceMetadataActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Generate asset metadata then create actor token
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateFields(new EditText[]{etAssetName, etAssetLocation})) {

                    Intent i = new Intent(DeviceMetadataActivity.this, ClientSelectActivity.class);
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

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        endDateInMillis = myCalendar.getTime().getTime();
        etEndDate.setText(sdf.format(myCalendar.getTime()));
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
}
