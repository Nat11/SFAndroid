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
import com.testapp.android.subcontractor.MonitorActivity;

import java.util.List;

/**
 * Created by Administrator on 05/09/2017.
 */

public class CaseAssetAdapter extends ArrayAdapter<Asset> {
    private List<Asset> assets;
    private List<String> caseAssetIds;

    public CaseAssetAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Asset> objects) {
        super(context, resource, objects);
        assets = objects;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).
                    inflate(R.layout.list_asset_case_item, parent, false);
        }

        caseAssetIds = MonitorActivity.caseAssetIds;

        final Asset asset = assets.get(position);
        TextView nameText = (TextView) convertView.findViewById(R.id.assetNameText);
        nameText.setText(asset.getName());
        TextView locationText = (TextView) convertView.findViewById(R.id.assetLocationText);
        if (asset.getLocation().equals("null")) {
            locationText.setText("");
        } else
            locationText.setText(asset.getLocation());

        TextView stateText = (TextView) convertView.findViewById(R.id.stateText);

        Log.d("testAsset", asset.getId());

        for (String id : caseAssetIds) {
            Log.d("assetCase", id);
        }

        if (caseAssetIds.contains(asset.getId())) {
            stateText.setText("Unstable Performance");
        } else
            stateText.setText("Stable Performance");

        return convertView;
    }
}
