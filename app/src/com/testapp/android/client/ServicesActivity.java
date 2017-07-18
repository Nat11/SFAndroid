package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.testapp.android.Mail.Mail;
import com.testapp.android.Model.ClientRequest;
import com.testapp.android.R;

public class ServicesActivity extends AppCompatActivity {

    private String fn, cn, an, clientEmail, clientId;
    private String assetType = "", assetLocation = "";
    private Button validate;
    private EditText etAssetType, etAssetLocation;
    private DatabaseReference mDatabase;
    private ClientRequest request;
    private Spinner etServicesCompany;
    final String[] company = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clientEmail = getIntent().getStringExtra("clientEmail");
        fn = getIntent().getStringExtra("fullName");
        cn = getIntent().getStringExtra("ContactName");
        an = getIntent().getStringExtra("AccountName");
        clientId = getIntent().getStringExtra("clientId");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        validate = (Button) findViewById(R.id.validate);
        etServicesCompany = (Spinner) findViewById(R.id.servicesCompany);
        etAssetType = (EditText) findViewById(R.id.input_assetType);
        etAssetLocation = (EditText) findViewById(R.id.input_assetLocation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        etServicesCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                company[0] = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean fieldsOK = validate(new EditText[]{etAssetType, etAssetLocation});
                if (fieldsOK)
                    new SendMail().execute();
            }
        });
    }

    private boolean validate(EditText[] fields) {
        for (EditText currentField : fields) {
            if (currentField.getText().toString().trim().equals("")) {
                currentField.setError("Required field");
                return false;
            }
        }
        return true;
    }

    private class SendMail extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            assetType = etAssetType.getText().toString();
            assetLocation = etAssetLocation.getText().toString();

            request = new ClientRequest(fn, clientEmail, cn, an, company[0], assetType, assetLocation, "false");

            progressDialog = ProgressDialog.show(ServicesActivity.this, "Please wait", "Sending request", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ServicesActivity.this);
            alertBuilder.setMessage("Would you like to make another request?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            startActivity(getIntent());
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            finish();
                            startActivity(new Intent(ServicesActivity.this, ClientNavActivity.class));
                        }
                    });

            AlertDialog alert = alertBuilder.create();
            alert.show();
        }

        protected Void doInBackground(String... params) {

            // Add request to database with autoId generated in firebase for each request object
            mDatabase.child("ClientRequests").child(company[0].toLowerCase()).push().setValue(request);

            /*Mail m = new Mail("nataliootayek@gmail.com", "natzo__93");
            String[] toArr = {"natalio.otayek@arismore.fr"};
            m.setTo(toArr);
            m.setFrom("nataliootayek@gmail.com");
            m.setSubject("Asset installation request");
            m.setBody("Client Full Name: " + fn + "\n" + "Email: " + clientEmail + "\n" + "Contact Name: " + cn + "\n" + "Account: " + an +
                    "\n" + "Asset Type: " + assetType + "\n" + "Installation position: " + assetLocation);

            try {
                if (m.send()) {
                    Log.d("MailApp", "sent email");
                } else {
                    Log.e("MailApp", "not sent");
                }
            } catch (Exception e) {
                Log.e("MailApp", "Could not send email", e);
            }*/
            return null;
        }
    }
}
