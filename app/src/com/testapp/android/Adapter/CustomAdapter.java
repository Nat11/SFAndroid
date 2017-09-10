package com.testapp.android.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.android.Model.Product;
import com.testapp.android.R;
import com.testapp.android.client.PurchaseActivity;

import java.util.List;

/**
 * Created by Administrator on 18/07/2017.
 */

public class CustomAdapter extends ArrayAdapter<Product> {

    private List<Product> products;

    public CustomAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        products = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.list_product_item, parent, false);
        }

        final Product product = products.get(position);
        TextView nameText = (TextView) convertView.findViewById(R.id.nameText);
        nameText.setText(product.getName());
        TextView priceText = (TextView) convertView.findViewById(R.id.priceText);
        priceText.setText(product.getPrice() + " " + getContext().getString(R.string.euro));
        final FloatingActionButton addBtn = (FloatingActionButton) convertView.findViewById(R.id.addToCartButton);
        final String productId = product.getId();
        final String productName = product.getName();
        final String productPrice = product.getPrice();

        addBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), productName + " added", Toast.LENGTH_SHORT).show();
                PurchaseActivity.addedProducts.add(product);
                //PurchaseActivity.products.remove(position);
                //Change Button Color
                view.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}},
                        new int[]{PurchaseActivity.btnClickedColor}));
                //Disable button
                addBtn.setEnabled(false);
                PurchaseActivity.itemsCount ++;
                PurchaseActivity.updateShoppingCounter(PurchaseActivity.itemsCount);
            }
        });
        return convertView;
    }
}
