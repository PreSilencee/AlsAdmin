package com.example.alsadmin.ui.event;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.Handler.GlideApp;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Contributor;
import com.example.alsadmin.object.Event;
import com.example.alsadmin.object.Organization;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class EventViewActivity extends AppCompatActivity {

    //tag
    private static final String TAG = "EventViewActivity";
    //Connectivity
    Connectivity device;

    String eventSessionId;
    //toolbar
    Toolbar customizeToolbar;

    //Image View
    ImageView eventViewIV;

    //Text View
    TextView eventCreatedDateTimeTV, eventTitleTV, eventDurationTV,
            eventDescriptionTV, eventPersonInChargeTV, eventCurrentFundTV,
            eventTargetFundTV, eventStatusTV;

    ProgressBar eventProgressBar;

    //linear layout
    LinearLayout eventButtonViewLinearLayout;

    //button
    Button eventAcceptBtn, eventDeclineBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        customizeToolbar = findViewById(R.id.customizeToolbar);
        customizeToolbar.setTitle("Event");
        setSupportActionBar(customizeToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        eventViewIV = findViewById(R.id.eventViewImageView);
        eventCreatedDateTimeTV = findViewById(R.id.eventViewCreatedDateTimeTextView);
        eventTitleTV = findViewById(R.id.eventViewTitleTextView);
        eventDurationTV = findViewById(R.id.eventViewDurationTextView);
        eventDescriptionTV = findViewById(R.id.eventViewDescriptionTextView);
        eventPersonInChargeTV = findViewById(R.id.eventViewPersonInChargeTextView);
        eventCurrentFundTV = findViewById(R.id.eventViewCurrentFundTextView);
        eventTargetFundTV = findViewById(R.id.eventViewTargetFundTextView);
        eventStatusTV = findViewById(R.id.eventViewStatusTextView);

        eventProgressBar = findViewById(R.id.eventViewProgressBar);

        eventButtonViewLinearLayout = findViewById(R.id.buttonViewLinearLayout);
        eventAcceptBtn = findViewById(R.id.eventViewAcceptButton);
        eventDeclineBtn = findViewById(R.id.eventViewDeclineButton);


        device = new Connectivity(getApplicationContext());
        Intent session = getIntent();


        if(session.hasExtra(Config.EVENT_SESSION_ID)){
            eventSessionId = session.getStringExtra(Config.EVENT_SESSION_ID);

            if(eventSessionId != null){
                if(device.haveNetwork()){
                    Config.EVENT_REF.child(eventSessionId).addListenerForSingleValueEvent(eventValueListener);
                }
                else{
                    Toast.makeText(getApplicationContext(), device.NetworkError(), Toast.LENGTH_LONG).show();
                }

            }
        }
        else{
            Toast.makeText(getApplicationContext(), "Something went wrong. Please Try Again", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void updateEvent(Event event, String status){

        String currentStatus = null;

        if(status.equals(Config.AVAILABLE))
        {
            currentStatus = "accept";
        }
        else if(status.equals(Config.DECLINED)){
            currentStatus = "decline";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(EventViewActivity.this);
        builder.setMessage("Are you sure want to "+currentStatus+" this event?")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        event.setEventStatus(status);

                        Map<String, Object> eventValues = event.eventMap();

                        Config.EVENT_REF.child(eventSessionId).updateChildren(eventValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    Config.EVENT_REF.child(eventSessionId).addListenerForSingleValueEvent(eventValueListener);
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Confirmation");
        alert.show();
    }

    private final ValueEventListener eventValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                Event event = snapshot.getValue(Event.class);

                if(event != null){
                    if(event.getEventImageName() != null){

                        //define the url for image
                        final StorageReference eventImageRef = Config.EVENT_SR.child(event.getEventImageName());

                        //get url and download it
                        eventImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //push image to image view
                                GlideApp.with(getApplicationContext())
                                        .load(uri)
                                        .placeholder(R.mipmap.loading_image)
                                        .into(eventViewIV);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                //show loading image
                                eventViewIV.setImageResource(R.mipmap.loading_image);
                            }
                        });
                    }

                    if(event.getEventDateTimeCreated() != null){
                        String dateTimeCreated = "Created Date Time: "+event.getEventDateTimeCreated();
                        eventCreatedDateTimeTV.setText(dateTimeCreated);
                    }
                    else{
                        String dateTimeCreated = "Created Date Time: NULL";
                        eventCreatedDateTimeTV.setText(dateTimeCreated);
                    }

                    if(event.getEventTitle() != null){
                        String title = "Title: "+event.getEventTitle();
                        eventTitleTV.setText(title);
                    }
                    else{
                        String title = "Title: NULL";
                        eventTitleTV.setText(title);
                    }

                    if(event.getEventStartDate() != null && event.getEventEndDate() != null){
                        String duration = "Duration: "+event.getEventStartDate()+" ~ "+event.getEventEndDate();
                        eventDurationTV.setText(duration);
                    }
                    else{
                        String duration = "Duration: NULL";
                        eventDurationTV.setText(duration);
                    }

                    if(event.getEventDescription() != null){
                        String description = "Description: "+event.getEventDescription();
                        eventDescriptionTV.setText(description);
                    }
                    else{
                        String description = "Description: NULL";
                        eventDescriptionTV.setText(description);
                    }

                    if(event.getEventHandler() != null){
                        Config.CONTRIBUTOR_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Contributor contributor = snapshot.getValue(Contributor.class);

                                    if(contributor != null){
                                        String personInCharge = "Person In Charge: "+contributor.getName();
                                        eventPersonInChargeTV.setText(personInCharge);
                                    }
                                    else{
                                        String personInCharge = "Person In Charge: NULL";
                                        eventPersonInChargeTV.setText(personInCharge);
                                    }
                                }
                                else{
                                    Config.ORGANIZATION_REF.child(event.getEventHandler()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                Organization organization = snapshot.getValue(Organization.class);

                                                if(organization != null) {
                                                    String personInCharge = "Person In Charge: "+organization.getOrganizationName();
                                                    eventPersonInChargeTV.setText(personInCharge);
                                                }
                                                else{
                                                    String personInCharge = "Person In Charge: NULL";
                                                    eventPersonInChargeTV.setText(personInCharge);
                                                }
                                            }
                                            else{
                                                String personInCharge = "Person In Charge: NULL";
                                                eventPersonInChargeTV.setText(personInCharge);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.d(TAG, "Database Error: "+error.getMessage());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "Database Error: "+error.getMessage());
                            }
                        });
                    }
                    else{
                        String personInCharge = "Person In Charge: NULL";
                        eventPersonInChargeTV.setText(personInCharge);
                    }

                    //if event current amount more than 0
                    if(event.getEventCurrentAmount() > 0){
                        String currentAmount = "RM " + event.getEventCurrentAmount();
                        eventCurrentFundTV.setText(currentAmount);
                    }
                    else{
                        //set "RM 0" as default
                        String currentAmount = "RM 0";
                        eventCurrentFundTV.setText(currentAmount);
                    }

                    //if event target amount more than 0
                    if(event.getEventTargetAmount() > 0){
                        String targetAmount = "RM " + event.getEventTargetAmount();
                        eventTargetFundTV.setText(targetAmount);
                    }
                    else{
                        //set "RM 0" as default
                        String targetAmount = "RM 0";
                        eventTargetFundTV.setText(targetAmount);
                    }

                    //calculate the currentAmount/targetAmount
                    double fundProgress = (event.getEventCurrentAmount() / event.getEventTargetAmount()) * 100;
                    //apply to the progress bar
                    eventProgressBar.setProgress((int) fundProgress);

                    if(event.getEventStatus() != null){
                        eventStatusTV.setText(event.getEventStatus());
                    }
                    else{
                        String status = "NULL";
                        eventStatusTV.setText(status);
                    }

                    if(eventStatusTV.getText().equals(Config.PENDING)){
                        eventButtonViewLinearLayout.setVisibility(View.VISIBLE);

                        eventAcceptBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateEvent(event, Config.AVAILABLE);
                            }
                        });

                        eventDeclineBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateEvent(event, Config.DECLINED);
                            }
                        });
                    }
                    else{
                        eventButtonViewLinearLayout.setVisibility(View.GONE);
                    }

                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, "Database Error: "+error.getMessage());
        }
    };


}