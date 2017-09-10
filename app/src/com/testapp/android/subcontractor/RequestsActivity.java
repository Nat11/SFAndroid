package com.testapp.android.subcontractor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.testapp.android.Model.Asset;
import com.testapp.android.Model.ObjectSerializer;
import com.testapp.android.R;
import com.testapp.android.subcontractor.fragments.TabDoneRequests;
import com.testapp.android.subcontractor.fragments.TabPendingRequests;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private List<Asset> assets = new ArrayList<>();
    private List<Asset> assetsDone = new ArrayList<>();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        assets = (List<Asset>) getIntent().getSerializableExtra("ASSETS");
        assetsDone = (List<Asset>) getIntent().getSerializableExtra("ASSETSDONE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // l both assets lists values when activity is paused
        if (null == assets || null == assetsDone) {
            assets = new ArrayList<Asset>();
            assetsDone = new ArrayList<Asset>();
            // load tasks from preference
            SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);

            try {
                assets = (ArrayList<Asset>) ObjectSerializer.deserialize(prefs.getString("assetsPending", ObjectSerializer.serialize(new ArrayList<Asset>())));
                assetsDone = (ArrayList<Asset>) ObjectSerializer.deserialize(prefs.getString("assetsDone", ObjectSerializer.serialize(new ArrayList<Asset>())));

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    //Send data to fragment
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ASSETS", (Serializable) assets);
                    TabPendingRequests pendingFrag = new TabPendingRequests();
                    pendingFrag.setArguments(bundle);
                    return pendingFrag;
                case 1:
                    //Send data to fragment
                    Bundle bundleDone = new Bundle();
                    bundleDone.putSerializable("ASSETSDONE", (Serializable) assetsDone);
                    TabDoneRequests doneFrag = new TabDoneRequests();
                    doneFrag.setArguments(bundleDone);
                    return doneFrag;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Pending";
                case 1:
                    return "Done";
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RequestsActivity.this, SubcontractorNavActivity.class));
    }

    public void saveListsState() {
        // save the assets lists to preference
        SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString("assetsPending", ObjectSerializer.serialize((Serializable) assets));
            editor.putString("assetsDone", ObjectSerializer.serialize((Serializable) assetsDone));

        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveListsState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveListsState();
    }
}
