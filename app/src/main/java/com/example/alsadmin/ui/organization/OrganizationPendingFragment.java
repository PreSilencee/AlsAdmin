package com.example.alsadmin.ui.organization;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.alsadmin.Adapter.OrganizationListFragmentAdapter;
import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Organization;
import com.example.alsadmin.widgets.AlsRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrganizationPendingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout organizationPendingListSRL;

    List<Organization> organizationPendingList;
    OrganizationListFragmentAdapter organizationPendingAdapter;
    AlsRecyclerView organizationPendingListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_organization_pending, container, false);

        device = new Connectivity(getContext());

        organizationPendingListSRL = root.findViewById(R.id.organizationPendingListSwipeRefreshLayout);
        View emptyPendingOrganizationView = root.findViewById(R.id.empty_organization_pending_list_view);
        //recycler view
        organizationPendingListRV = root.findViewById(R.id.organizationPendingListRecyclerView);
        organizationPendingListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        organizationPendingListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        organizationPendingListRV.setLayoutManager(layoutManager);
        organizationPendingListRV.showIfEmpty(emptyPendingOrganizationView);

        //swipeRefreshLayout function
        organizationPendingListSRL.setOnRefreshListener(this);
        organizationPendingListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        organizationPendingListSRL.post(() -> {
            organizationPendingListSRL.setRefreshing(true);
            loadPendingOrganizationList();
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadPendingOrganizationList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadPendingOrganizationList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.ORGANIZATION_REF.removeEventListener(organizationPendingValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadPendingOrganizationList();
    }

    private void loadPendingOrganizationList(){
        organizationPendingList = new ArrayList<>();
        Config.ORGANIZATION_REF.addListenerForSingleValueEvent(organizationPendingValueEventListener);
    }

    private final ValueEventListener organizationPendingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            organizationPendingList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null && organization.getOrganizationVerifyStatus().equals(Config.PENDING)) {
                    organizationPendingList.add(organization);
                }
            }

            organizationPendingAdapter = new OrganizationListFragmentAdapter(organizationPendingList, getContext());
            organizationPendingAdapter.notifyDataSetChanged();
            organizationPendingListRV.setAdapter(organizationPendingAdapter);
            organizationPendingListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationAllFragment", "Database Error: " + error.getMessage());
        }
    };
}