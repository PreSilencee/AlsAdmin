package com.example.alsadmin.ui.organization;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class OrganizationAllFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout organizationAllListSRL;

    List<Organization> organizationAllList;
    OrganizationListFragmentAdapter adapter;
    AlsRecyclerView organizationAllListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_organization_all, container, false);

        device = new Connectivity(getContext());

        organizationAllListSRL = root.findViewById(R.id.organizationListSwipeRefreshLayout);
        View emptyOrganizationView = root.findViewById(R.id.empty_organization_list_view);
        //recycler view
        organizationAllListRV = root.findViewById(R.id.organizationListRecyclerView);
        organizationAllListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        organizationAllListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        organizationAllListRV.setLayoutManager(layoutManager);
        organizationAllListRV.showIfEmpty(emptyOrganizationView);

        //swipeRefreshLayout function
        organizationAllListSRL.setOnRefreshListener(this);
        organizationAllListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        organizationAllListSRL.post(() -> {
            organizationAllListSRL.setRefreshing(true);
            loadAllOrganizationList();
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
            loadAllOrganizationList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAllOrganizationList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Config.ORGANIZATION_REF.removeEventListener(organizationAllValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadAllOrganizationList();
    }

    private void loadAllOrganizationList(){
        organizationAllList = new ArrayList<>();
        Config.ORGANIZATION_REF.addListenerForSingleValueEvent(organizationAllValueEventListener);
    }

    private final ValueEventListener organizationAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            organizationAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Organization organization = dataSnapshot.getValue(Organization.class);

                if(organization != null) {
                    organizationAllList.add(organization);
                }
            }

            adapter = new OrganizationListFragmentAdapter(organizationAllList, getContext());
            adapter.notifyDataSetChanged();
            organizationAllListRV.setAdapter(adapter);
            organizationAllListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("OrganizationAllFragment", "Database Error: " + error.getMessage());
        }
    };
}