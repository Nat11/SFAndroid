package com.testapp.android.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.android.Model.Product;
import com.testapp.android.R;
import com.testapp.android.client.PurchaseActivity;
import com.testapp.android.client.ShoppingCartActivity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 19/07/2017.
 */

public class CartAdapter extends ArrayAdapter<Product> {

    private List<Product> products;

    public CartAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        products = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.list_cart_item, parent, false);
        }

        final Product product = products.get(position);
        TextView nameText = (TextView) convertView.findViewById(R.id.nameText);
        nameText.setText(product.getName());
        TextView priceText = (TextView) convertView.findViewById(R.id.priceText);
        priceText.setText(product.getPrice() + " " + getContext().getString(R.string.euro));
        TextView qtyText = (TextView) convertView.findViewById(R.id.qtyText);
        qtyText.setText("Qty :1");
        final TextView removeText = (TextView) convertView.findViewById(R.id.removeText);

        removeText.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), product.getName() + " removed", Toast.LENGTH_SHORT).show();
                //remove item from listview
                ShoppingCartActivity.confirmedProducts.remove(product);
                //remove item from arrayList in case back button was clicked
                PurchaseActivity.addedProducts.remove(position);
                PurchaseActivity.itemsCount--;
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
}
