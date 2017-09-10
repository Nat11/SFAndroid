package com.testapp.android.Adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.testapp.android.Model.Asset;
import com.testapp.android.R;

import java.util.List;

/**
 * Created by Administrator on 19/07/2017.
 */

public class AssetAdapter extends ArrayAdapter<Asset> {

    private List<Asset> assets;

    public AssetAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Asset> objects) {
        super(context, resource, objects);
        assets = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.list_asset_item, parent, false);
        }

        final Asset asset = assets.get(position);
        TextView nameText = (TextView) convertView.findViewById(R.id.assetNameText);
        nameText.setText(asset.getName());
        ImageView checkImage = (ImageView) convertView.findViewById(R.id.checkImage);
        ImageView chevronImage = (ImageView) convertView.findViewById(R.id.chevronImage);

        TextView locationText = (TextView) convertView.findViewById(R.id.assetLocationText);
        if (asset.getLocation().equals("null")) {
            Log.d("location", asset.getLocation());
            locationText.setText("");
        } else
            locationText.setText(asset.getLocation());

        TextView statusText = (TextView) convertView.findViewById(R.id.statusText);
        switch (asset.getStatus()) {

            case "Purchased":
                statusText.setText("Purchased");
                chevronImage.setVisibility(View.VISIBLE);
                break;

            case "Shipped":
                if (asset.getLocation().equals("null"))
                    statusText.setText("Delivered");
                else
                    statusText.setText("Delivered and requested installation");
                chevronImage.setVisibility(View.VISIBLE);
                break;

            case "Installed":
                statusText.setText("Installed");
                checkImage.setVisibility(View.VISIBLE);
                break;

            case "Registered":
                statusText.setText("Registered");
                break;
        }

        return convertView;
    }
}
