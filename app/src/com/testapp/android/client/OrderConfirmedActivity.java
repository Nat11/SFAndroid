package com.testapp.android.client;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.testapp.android.R;

public class OrderConfirmedActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnHomepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnHomepage = (Button) findViewById(R.id.homepageBtn);
        btnHomepage.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.homepageBtn:
                startActivity(new Intent(this, ClientNavActivity.class));
        }
    }
}
