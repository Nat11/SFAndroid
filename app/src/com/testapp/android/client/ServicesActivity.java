package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.testapp.android.Mail.Mail;
import com.testapp.android.R;

public class ServicesActivity extends AppCompatActivity {

    private String fn, cn, an, clientEmail;
    private String assetType = "", assetLocation = "";
    private Button validate;
    private EditText etAssetType, etAssetLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        clientEmail = getIntent().getStringExtra("clientEmail");
        fn = getIntent().getStringExtra("fullName");
        cn = getIntent().getStringExtra("ContactName");
        an = getIntent().getStringExtra("AccountName");

        validate = (Button) findViewById(R.id.validate);
        etAssetType = (EditText) findViewById(R.id.input_assetType);
        etAssetLocation = (EditText) findViewById(R.id.input_assetLocation);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
            progressDialog = ProgressDialog.show(ServicesActivity.this, "Please wait", "Sending mail", true, false);
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
            Mail m = new Mail("nataliootayek@gmail.com", "natzo__93");
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
            }
            return null;
        }
    }
}
