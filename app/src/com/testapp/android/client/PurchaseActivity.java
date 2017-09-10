package com.testapp.android.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;
import com.salesforce.androidsdk.ui.SalesforceListActivity;
import com.testapp.android.Adapter.CustomAdapter;
import com.testapp.android.Model.Product;
import com.testapp.android.R;
import com.testapp.android.subcontractor.SFAssetsActivity;
import com.testapp.android.subcontractor.SubcontractorNavActivity;
import com.testapp.android.subcontractor.TokenActivity;

import org.json.JSONArray;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PurchaseActivity extends SalesforceListActivity implements View.OnClickListener {

    private RestClient client;
    private static List<String> productIds = new ArrayList<>();
    Context context;
    private String standardPriceBookId;
    public static List<Product> products = new ArrayList<Product>();
    public static List<Product> addedProducts = new ArrayList<Product>();
    public static int btnClickedColor;
    private String accessToken, accountId;
    public static int itemsCount;
    static TextView counter = null;
    private ImageView shoppingCart;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnClickedColor = getResources().getColor(R.color.light);

        AppCompatCallback callback = new AppCompatCallback() {
            @Override
            public void onSupportActionModeStarted(ActionMode mode) {
            }

            @Override
            public void onSupportActionModeFinished(ActionMode mode) {
            }

            @Nullable
            @Override
            public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
                return null;
            }
        };
        AppCompatDelegate delegate = AppCompatDelegate.create(this, callback);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_purchase);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        delegate.setSupportActionBar(toolbar);
        // Enable the Back Up Arrow
        delegate.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavUtils.navigateUpFromSameTask(PurchaseActivity.this);
            }
        });

        context = this;
        accessToken = getIntent().getStringExtra("accessToken");
        accountId = getIntent().getStringExtra("accountId");
        itemsCount = 0;
        addedProducts.clear();
    }


    @Override
    public void onResume(RestClient client) {
        this.client = client;
        new SFProducts().execute();
    }

    private void onLoadStandardPriceBook(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("PriceBook", result.toString());
                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                standardPriceBookId = records.getJSONObject(i).getString("Id");
                                onFetchAssets("Select Id, Name, Product2Id, UnitPrice FROM PricebookEntry WHERE Pricebook2Id='" + standardPriceBookId + "'");
                            }
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PurchaseActivity.this,
                                PurchaseActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void onFetchAssets(String soql) throws UnsupportedEncodingException {
        RestRequest restRequest = RestRequest.getRequestForQuery(ApiVersionStrings.getVersionNumber(this), soql);
        client.sendAsync(restRequest, new RestClient.AsyncRequestCallback() {
            @Override
            public void onSuccess(RestRequest request, final RestResponse result) {
                result.consumeQuietly(); // consume before going back to main thread
                products.clear();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            JSONArray records = result.asJSONObject().getJSONArray("records");
                            for (int i = 0; i < records.length(); i++) {
                                productIds.add(records.getJSONObject(i).getString("Product2Id"));
                                String productName = records.getJSONObject(i).getString("Name");
                                String productPrice = records.getJSONObject(i).getString("UnitPrice");
                                Product product = new Product(productIds.get(i), productName, productPrice);
                                products.add(product);
                            }
                            CustomAdapter adapter = new CustomAdapter(context, R.layout.list_product_item, products);
                            setListAdapter(adapter);
                        } catch (Exception e) {
                            onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(final Exception exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PurchaseActivity.this,
                                PurchaseActivity.this.getString(SalesforceSDKManager.getInstance().getSalesforceR().stringGenericError(), exception.toString()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_purchase, menu);
        View item = menu.findItem(R.id.purchase).getActionView();
        counter = (TextView) item.findViewById(R.id.shopping_counter);
        shoppingCart = (ImageView) item.findViewById(R.id.shopping_cart);
        shoppingCart.setOnClickListener(this);
        updateShoppingCounter(itemsCount);
        return true;
    }

    public static void updateShoppingCounter(int newItemsCount) {
        itemsCount = newItemsCount;

        if (itemsCount == 0)
            counter.setVisibility(View.INVISIBLE);
        else {
            counter.setVisibility(View.VISIBLE);
            counter.setText(Integer.toString(itemsCount));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shopping_cart:
                Intent i = new Intent(PurchaseActivity.this, ShoppingCartActivity.class);
                i.putExtra("PRODUCTS", (Serializable) addedProducts);
                i.putExtra("accessToken", accessToken);
                i.putExtra("accountId", accountId);
                if (addedProducts.size() == 0)
                    Toast.makeText(context, "Shopping cart is empty", Toast.LENGTH_SHORT).show();
                else if (addedProducts.size() > 0)
                    startActivity(i);
        }
    }

    private class SFProducts extends AsyncTask<String, Integer, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(PurchaseActivity.this, "Please wait", "Loading Products", true, false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            //Display products list when request is done
            findViewById(android.R.id.list).setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(String... params) {

            try {
                onLoadStandardPriceBook("Select Id, Name FROM Pricebook2 WHERE IsStandard=true");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
