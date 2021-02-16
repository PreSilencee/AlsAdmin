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

public class OrganizationVerifiedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout organizationVerifiedListSRL;

    List<Organization> organizationVerifiedList;
    OrganizationListFragmentAdapter organizationVerifiedAdapter;
    AlsRecyclerView organizationVerifiedListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_organization_verified, container, false);

        device = new Connectivity(getContext());

        organizationVerifiedListSRL = root.findViewById(R.id.organizationVerifiedListSwipeRefreshLayout);
        View emptyVerifiedOrganizationView = root.findViewById(R.id.empty_organization_verified_list_view);
        //recycler view
        organizationVerifiedListRV = root.findViewById(R.id.organizationVerifiedListRecyclerView);
        organizationVerifiedListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        organizationVerifiedListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        organizationVerifiedListRV.setLayoutManager(layoutManager);
        organizationVerifiedListRV.showIfEmpty(emptyVerifiedOrganizationView);

        //swipeRefreshLayout function
        organizationVerifiedListSRL.setOnRefreshListener(this);
        organizationVerifiedListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        organizationVerifiedListSRL.post(() -> {
            organizationVerifiedListSRL.setRefreshing(true);
            loadVerifiedOrganizationList();
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
            loadVerifiedOrganizationList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadVerifiedOrganizationList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.ORGANIZATION_REF.removeEventListener(organizationVerifiedValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadVerifiedOrganizationList();
    }

    private void loadVerifiedOrganizationList(){
        organizationVerifiedList = new ArrayList<>();
        Config.ORGANIZATION_REF.addListenerForSingleValueEvent(organizationVerifiedValueEventListener);
    }

    private final ValueEventListener organizationVerifiedValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            organizationVerifiedList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null && organization.getOrganizationVerifyStatus().equals(Config.VERIFIED)) {
                    organizationVerifiedList.add(organization);
                }
            }

            organizationVerifiedAdapter = new OrganizationListFragmentAdapter(organizationVerifiedList, getContext());
            organizationVerifiedAdapter.notifyDataSetChanged();
            organizationVerifiedListRV.setAdapter(organizationVerifiedAdapter);
            organizationVerifiedListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationVFragment", "Database Error: " + error.getMessage());
        }
    };
}