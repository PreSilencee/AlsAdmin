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

public class EventAvailableFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    Connectivity device;
    SwipeRefreshLayout eventAvailableListSRL;

    List<Event> eventAvailableList;
    EventListFragmentAdapter eventAvailableAdapter;
    AlsRecyclerView eventAvailableListRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_event_available, container, false);

        device = new Connectivity(getContext());

        eventAvailableListSRL = root.findViewById(R.id.eventAvailableListSwipeRefreshLayout);
        View emptyAvailableEventView = root.findViewById(R.id.empty_event_available_list_view);
        //recycler view
        eventAvailableListRV = root.findViewById(R.id.eventAvailableListRecyclerView);
        eventAvailableListRV.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        eventAvailableListRV.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
        eventAvailableListRV.setLayoutManager(layoutManager);
        eventAvailableListRV.showIfEmpty(emptyAvailableEventView);

        //swipeRefreshLayout function
        eventAvailableListSRL.setOnRefreshListener(this);
        eventAvailableListSRL.setColorSchemeResources(R.color.purple_200,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        eventAvailableListSRL.post(() -> {
            eventAvailableListSRL.setRefreshing(true);
            loadAvailableEventList();
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
            loadAvailableEventList();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!device.haveNetwork()) {
            Toast.makeText(requireContext(), device.NetworkError(), Toast.LENGTH_SHORT).show();
        }
        else{
            loadAvailableEventList();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRefresh() {
        loadAvailableEventList();
    }

    private void loadAvailableEventList(){
        eventAvailableList = new ArrayList<>();
        Config.EVENT_REF.addListenerForSingleValueEvent(eventAvailableValueEventListener);
    }

    private final ValueEventListener eventAvailableValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            eventAvailableList.clear();

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

                            if((currentDate.compareTo(eventStartDate) == 0 || currentDate.compareTo(eventStartDate) > 0)
                                    && event.getEventStatus().equals(Config.AVAILABLE)){
                                eventAvailableList.add(event);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            eventAvailableAdapter = new EventListFragmentAdapter(eventAvailableList, getContext());
            eventAvailableAdapter.notifyDataSetChanged();
            eventAvailableListRV.setAdapter(eventAvailableAdapter);
            eventAvailableListSRL.setRefreshing(false);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("EventPendingFragment", "Database Error: " + error.getMessage());
        }
    };
}