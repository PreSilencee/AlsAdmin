package com.example.alsadmin.ui.event;

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

import com.example.alsadmin.Adapter.EventListFragmentAdapter;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.R;
import com.example.alsadmin.Config;
import com.example.alsadmin.object.Event;
import com.example.alsadmin.widgets.AlsRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventAllFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventAllListSRL;

    List<Event> eventAllList;
    EventListFragmentAdapter eventAllAdapter;
    AlsRecyclerView eventAllListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_all, container, false);
        device = new Connectivity(getContext());

        eventAllListSRL = root.findViewById(R.id.eventListSwipeRefreshLayout);
        View emptyEventView = root.findViewById(R.id.empty_event_list_view);
        //recycler view
        eventAllListRV = root.findViewById(R.id.eventListRecyclerView);
        eventAllListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventAllListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventAllListRV.setLayoutManager(layoutManager);
        eventAllListRV.showIfEmpty(emptyEventView);

        //swipeRefreshLayout function
        eventAllListSRL.setOnRefreshListener(this);
        eventAllListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventAllListSRL.post(() -> {
            eventAllListSRL.setRefreshing(true);
            loadAllEventList();
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
            loadAllEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAllEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove listener when fragment is stopped
        Config.EVENT_REF.removeEventListener(eventAllValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadAllEventList();
    }

    private void loadAllEventList(){
        eventAllList = new ArrayList<>();
        Config.EVENT_REF.addListenerForSingleValueEvent(eventAllValueEventListener);
    }

    private final ValueEventListener eventAllValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAllList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null) {
                    eventAllList.add(event);
                }
            }

            eventAllAdapter = new EventListFragmentAdapter(eventAllList, getContext());
            eventAllAdapter.notifyDataSetChanged();
            eventAllListRV.setAdapter(eventAllAdapter);
            eventAllListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventAllFragment", "Database Error: " + error.getMessage());
        }
    };
}