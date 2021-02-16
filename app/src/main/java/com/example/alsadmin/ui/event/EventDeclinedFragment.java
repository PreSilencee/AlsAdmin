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

public class EventDeclinedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventDeclinedListSRL;

    List<Event> eventDeclinedList;
    EventListFragmentAdapter eventDeclinedAdapter;
    AlsRecyclerView eventDeclinedListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_declined, container, false);

        device = new Connectivity(getContext());

        eventDeclinedListSRL = root.findViewById(R.id.eventDeclinedListSwipeRefreshLayout);
        View emptyDeclinedEventView = root.findViewById(R.id.empty_event_declined_list_view);
        //recycler view
        eventDeclinedListRV = root.findViewById(R.id.eventDeclinedListRecyclerView);
        eventDeclinedListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventDeclinedListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventDeclinedListRV.setLayoutManager(layoutManager);
        eventDeclinedListRV.showIfEmpty(emptyDeclinedEventView);

        //swipeRefreshLayout function
        eventDeclinedListSRL.setOnRefreshListener(this);
        eventDeclinedListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventDeclinedListSRL.post(() -> {
            eventDeclinedListSRL.setRefreshing(true);
            loadDeclinedEventList();
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
            loadDeclinedEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadDeclinedEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove listener when fragment is stopped
        Config.EVENT_REF.removeEventListener(eventDeclinedValueEventListener);
    }


    @Override
    public void onRefresh() {
        loadDeclinedEventList();
    }

    private void loadDeclinedEventList(){
        eventDeclinedList = new ArrayList<>();
        Config.EVENT_REF.addListenerForSingleValueEvent(eventDeclinedValueEventListener);
    }

    private final ValueEventListener eventDeclinedValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventDeclinedList.clear();

            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null && event.getEventStatus().equals(Config.DECLINED)) {
                    eventDeclinedList.add(event);
                }
            }

            eventDeclinedAdapter = new EventListFragmentAdapter(eventDeclinedList, getContext());
            eventDeclinedAdapter.notifyDataSetChanged();
            eventDeclinedListRV.setAdapter(eventDeclinedAdapter);
            eventDeclinedListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}