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
import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Event;
import com.example.alsadmin.widgets.AlsRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventPendingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventPendingListSRL;

    List<Event> eventPendingList;
    EventListFragmentAdapter eventPendingAdapter;
    AlsRecyclerView eventPendingListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_pending, container, false);

        device = new Connectivity(getContext());

        eventPendingListSRL = root.findViewById(R.id.eventPendingListSwipeRefreshLayout);
        View emptyPendingEventView = root.findViewById(R.id.empty_event_pending_list_view);
        //recycler view
        eventPendingListRV = root.findViewById(R.id.eventPendingListRecyclerView);
        eventPendingListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventPendingListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventPendingListRV.setLayoutManager(layoutManager);
        eventPendingListRV.showIfEmpty(emptyPendingEventView);

        //swipeRefreshLayout function
        eventPendingListSRL.setOnRefreshListener(this);
        eventPendingListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventPendingListSRL.post(() -> {
            eventPendingListSRL.setRefreshing(true);
            loadPendingEventList();
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
            loadPendingEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadPendingEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove listener when fragment is stopped
        Config.EVENT_REF.removeEventListener(eventPendingValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadPendingEventList();
    }

    private void loadPendingEventList(){
        eventPendingList = new ArrayList<>();
        Config.EVENT_REF.addListenerForSingleValueEvent(eventPendingValueEventListener);
    }

    private final ValueEventListener eventPendingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventPendingList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null && event.getEventStatus().equals(Config.PENDING)) {
                    eventPendingList.add(event);
                }
            }

            eventPendingAdapter = new EventListFragmentAdapter(eventPendingList, getContext());
            eventPendingAdapter.notifyDataSetChanged();
            eventPendingListRV.setAdapter(eventPendingAdapter);
            eventPendingListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}