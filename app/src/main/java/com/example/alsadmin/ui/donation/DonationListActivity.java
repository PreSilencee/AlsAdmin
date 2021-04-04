package com.example.alsadmin.ui.donation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.alsadmin.Adapter.DonationHistoryListFragmentAdapter;
import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Donation;
import com.example.alsadmin.widgets.AlsRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DonationListActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "DonationHistory";
    private DonationHistoryListFragmentAdapter donationHistoryListFragmentAdapter;
    private List<Donation> donationHistoryList;
    private Connectivity device;
    private SwipeRefreshLayout donationHistoryListSRL;
    private AlsRecyclerView donationHistoryRecyclerView;

    //toolbar
    Toolbar donationCustomizeToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_list);

        donationCustomizeToolbar = findViewById(R.id.customizeToolbar);
        donationCustomizeToolbar.setNavigationIcon(R.mipmap.alslogo);
        donationCustomizeToolbar.setTitle("Donation List");
        setSupportActionBar(donationCustomizeToolbar);

        device = new Connectivity(getApplicationContext());
        donationHistoryRecyclerView = findViewById(R.id.donationHistoryListRecyclerView);
        donationHistoryRecyclerView.setHasFixedSize(true);
        donationHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        donationHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL));
        View emptyDonationHistoryView = findViewById(R.id.empty_donation_history);
        donationHistoryRecyclerView.showIfEmpty(emptyDonationHistoryView);

        donationHistoryListSRL = findViewById(R.id.donationHistoryListSwipeRefreshLayout);


        //swipeRefreshLayout function
        donationHistoryListSRL.setOnRefreshListener(this);
        donationHistoryListSRL.setColorSchemeResources(R.color.purple_500,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        donationHistoryListSRL.post(new Runnable() {
            @Override
            public void run() {
                donationHistoryListSRL.setRefreshing(true);
                loadDonationHistoryList();
            }
        });
    }

    @Override
    public void onRefresh() {
        loadDonationHistoryList();
    }

    private void loadDonationHistoryList(){

        donationHistoryList = new ArrayList<>();

        Config.DONATION_REF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                donationHistoryList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Donation donation = dataSnapshot.getValue(Donation.class);

                    if(donation != null){
                        donationHistoryList.add(donation);
                    }

                }

                donationHistoryListFragmentAdapter = new DonationHistoryListFragmentAdapter(donationHistoryList, getApplicationContext());
                donationHistoryListFragmentAdapter.notifyDataSetChanged();
                donationHistoryRecyclerView.setAdapter(donationHistoryListFragmentAdapter);
                donationHistoryListSRL.setRefreshing(false);
                donationHistoryListFragmentAdapter.setOnClickListener(new DonationHistoryListFragmentAdapter.OnDonationListener() {
                    @Override
                    public void onDonationClicked(int position) {
                        Intent i = new Intent(getApplicationContext(), DonationDetailsActivity.class);
                        i.putExtra(Config.DONATION_SESSION_ID, donationHistoryList.get(position).getDonationId());
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "databaseError: "+error.getMessage());
            }
        });
    }
}