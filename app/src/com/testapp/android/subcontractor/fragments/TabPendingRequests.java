package com.testapp.android.subcontractor.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.testapp.android.Model.ClientRequest;
import com.testapp.android.R;
import com.testapp.android.subcontractor.RequestDetailsActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 17/07/2017.
 */

public class TabPendingRequests extends ListFragment {

    private DatabaseReference mDatabase;
    private ArrayAdapter<String> listAdapter;
    private List<ClientRequest> clientRequests = new ArrayList<>();
    private List<String> requestIds = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_requests_pending, container, false);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.clientlistrow, new ArrayList<String>());
        setListAdapter(listAdapter);
        listAdapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadClientRequests();
    }

    public void loadClientRequests() {
        mDatabase.child("ClientRequests").child("subcontractor").orderByChild("done").equalTo("false").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listAdapter.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    requestIds.add(snapshot.getKey());
                    ClientRequest request = snapshot.getValue(ClientRequest.class);
                    clientRequests.add(request);
                    String clientRequest = request.getFullName() + " " + request.getAssetType().toUpperCase() + " in "
                            + request.getInstallationLocation().toUpperCase();
                    listAdapter.add(clientRequest);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ClientRequest request = clientRequests.get(position);
        String requestId = requestIds.get(position);
        Intent i = new Intent(getActivity(), RequestDetailsActivity.class);
        i.putExtra("request", (Serializable) request);
        i.putExtra("requestId", requestId);
        Log.d("requestId", requestId);
        i.putExtra("ActivityId", "Pending");
        startActivity(i);
    }


}