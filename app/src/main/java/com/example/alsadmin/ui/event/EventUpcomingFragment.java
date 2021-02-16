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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventUpcomingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventUpcomingListSRL;

    List<Event> eventUpcomingList;
    EventListFragmentAdapter eventUpcomingAdapter;
    AlsRecyclerView eventUpcomingListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_upcoming, container, false);

        device = new Connectivity(getContext());

        eventUpcomingListSRL = root.findViewById(R.id.eventUpcomingListSwipeRefreshLayout);
        View emptyUpcomingEventView = root.findViewById(R.id.empty_event_upcoming_list_view);
        //recycler view
        eventUpcomingListRV = root.findViewById(R.id.eventUpcomingListRecyclerView);
        eventUpcomingListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventUpcomingListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventUpcomingListRV.setLayoutManager(layoutManager);
        eventUpcomingListRV.showIfEmpty(emptyUpcomingEventView);

        //swipeRefreshLayout function
        eventUpcomingListSRL.setOnRefreshListener(this);
        eventUpcomingListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventUpcomingListSRL.post(() -> {
            eventUpcomingListSRL.setRefreshing(true);
            loadUpcomingEventList();
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
            loadUpcomingEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadUpcomingEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove listener when fragment is stopped
        Config.EVENT_REF.removeEventListener(eventUpcomingValueEventListener);
    }

    @Override
    public void onRefresh() {
        loadUpcomingEventList();
    }

    private void loadUpcomingEventList(){
        eventUpcomingList = new ArrayList<>();
        Config.EVENT_REF.addListenerForSingleValueEvent(eventUpcomingValueEventListener);
    }

    private final ValueEventListener eventUpcomingValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventUpcomingList.clear();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date dateObj = Calendar.getInstance().getTime();
            Date currentDate = null;
            try {
                currentDate = simpleDateFormat.parse(simpleDateFormat.format(dateObj));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                Event event = dataSnapshot.getValue(Event.class);

                if(event != null){
                    if(event.getEventStartDate() != null){
                        try {
                            Date eventStartDate = simpleDateFormat.parse(event.getEventStartDate());

                            if(currentDate.compareTo(eventStartDate) < 0 && event.getEventStatus().equals(Config.AVAILABLE)){
                                eventUpcomingList.add(event);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            eventUpcomingAdapter = new EventListFragmentAdapter(eventUpcomingList, getContext());
            eventUpcomingAdapter.notifyDataSetChanged();
            eventUpcomingListRV.setAdapter(eventUpcomingAdapter);
            eventUpcomingListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}