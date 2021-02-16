package com.example.alsadmin.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alsadmin.Config;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Event;
import com.example.alsadmin.ui.event.EventViewActivity;

import java.util.List;

public class EventListFragmentAdapter extends RecyclerView.Adapter<EventListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "HomeEventListFragment";

    //create an array list for Event object
    private List<Event> eventList;

    //create a context for the adapter
    private Context context;

    //constructor (home event list, context)
    public EventListFragmentAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    //create view for each event object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_view,parent,false);
        return new EventListFragmentAdapter.ViewHolder(view);
    }

    //attach the data of the event object
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //get position of donation
        Event event = eventList.get(position);

        if(event.getEventDateTimeCreated() != null){

            //separate the date and time
            String[] separatedDateAndTime = event.getEventDateTimeCreated().split(" ");

            //get date
            String[] separatedDate = separatedDateAndTime[0].split("/");

            //separate the year, day, month of date to three text view
            holder.eventListDayTV.setText(separatedDate[0]);
            holder.eventListYearTV.setText(separatedDate[2]);
            holder.eventListMonthTV.setText(separatedDate[1]);
        }
        else{
            //set default value
            holder.eventListDayTV.setText(R.string.day);
            holder.eventListYearTV.setText(R.string.year);
            holder.eventListMonthTV.setText(R.string.month);
        }

        //if event title not null
        if(event.getEventTitle() != null){
            holder.eventListTitleTV.setText(event.getEventTitle());
        }
        else{
            //set "-" as default
            holder.eventListTitleTV.setText("-");
        }

        double fundProgress = (event.getEventCurrentAmount() / event.getEventTargetAmount()) * 100;
        //apply to the progress bar
        holder.eventListPB.setProgress((int) fundProgress);
        String progress = (int) fundProgress + "%";
        holder.eventListProgress.setText(progress);

        if(event.getEventStatus() != null){
            holder.eventListStatus.setText(event.getEventStatus());
        }
        else{
            holder.eventListStatus.setText("NULL");
        }


        //donate btn
        holder.eventListViewBtn.setOnClickListener(v -> {
            Intent i = new Intent(context, EventViewActivity.class);
            i.putExtra(Config.EVENT_SESSION_ID, event.getEventId());
            context.startActivity(i);
        });
    }

    //get the size of array list for home event list
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //text view
        public TextView eventListYearTV, eventListDayTV, eventListMonthTV, eventListTitleTV, eventListStatus, eventListProgress;

        //progress bar
        public ProgressBar eventListPB;

        //button
        public Button eventListViewBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for text view
            eventListYearTV = itemView.findViewById(R.id.eventListYearTextView);
            eventListDayTV = itemView.findViewById(R.id.eventListDayTextView);
            eventListMonthTV = itemView.findViewById(R.id.eventListMonthTextView);
            eventListTitleTV = itemView.findViewById(R.id.eventTitle);
            eventListStatus = itemView.findViewById(R.id.eventListStatus);
            eventListProgress = itemView.findViewById(R.id.eventListProgress);

            //find id for progress bar
            eventListPB = itemView.findViewById(R.id.eventListProgressBar);

            //find id for button
            eventListViewBtn = itemView.findViewById(R.id.eventListViewButton);
        }
    }
}
