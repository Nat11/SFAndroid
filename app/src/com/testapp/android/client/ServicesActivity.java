package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.testapp.android.Mail.Mail;
import com.testapp.android.Model.User;
import com.testapp.android.R;

public class ServicesActivity extends AppCompatActivity {

    private static String fn, cn, an, clientEmail, assetName, assetType, assetSN;
    private Button validate;
    private EditText etAssetType, etAssetName, etSN;
    private User userClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        clientEmail = getIntent().getStringExtra("clientEmail");
        fn = getIntent().getStringExtra("fullName");
        cn = getIntent().getStringExtra("ContactName");
        an = getIntent().getStringExtra("AccountName");

        userClient = new User(fn, cn, an, clientEmail);
        validate = (Button) findViewById(R.id.validate);
        etAssetName = (EditText) findViewById(R.id.input_assetName);
        etAssetType = (EditText) findViewById(R.id.input_assetType);
        etSN = (EditText) findViewById(R.id.input_sn);

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SendMail().execute();
            }
        });
    }

    private class SendMail extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            assetName = etAssetName.getText().toString();
            assetType = etAssetType.getText().toString();
            assetSN = etSN.getText().toString();
            progressDialog = ProgressDialog.show(ServicesActivity.this, "Please wait", "Sending mail", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        protected Void doInBackground(String... params) {
            Mail m = new Mail("nataliootayek@gmail.com", "natzo__93");

            String[] toArr = {"natalio.otayek@arismore.fr"};
            m.setTo(toArr);
            m.setFrom("nataliootayek@gmail.com");
            m.setSubject("Asset installation request");
            m.setBody("Client Full Name: " + fn + "\n" + "Email: " + clientEmail + "\n" + "Contact Name: " + cn + "\n" + "Account: " + an +
                    "\n" + "Asset Name: " + assetName + "\n" + "Asset Type: " + assetType + "\n" + "Asset SN: " + assetSN);

            try {
                if (m.send()) {
                    Log.d("MailApp", "sent email");
                    startActivity(new Intent(ServicesActivity.this, ClientNavActivity.class));
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
