package com.testapp.android.subcontractor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.testapp.android.Adapter.AssetAdapter;
import com.testapp.android.Model.Asset;
import com.testapp.android.R;
import com.testapp.android.subcontractor.RequestDetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 17/07/2017.
 */

public class TabDoneRequests extends ListFragment {

    private AssetAdapter listAdapter;
    private List<Asset> assets = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requests_done, container, false);
        assets = (List<Asset>) getArguments().getSerializable("ASSETSDONE");
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        listAdapter = new AssetAdapter(getActivity(), R.layout.list_asset_item, assets);
        setListAdapter(listAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Asset asset = assets.get(position);
        Intent i = new Intent(getActivity(), RequestDetailsActivity.class);
        i.putExtra("assetDone", asset);
        i.putExtra("ActivityId", "Done");
        startActivity(i);
    }
}
