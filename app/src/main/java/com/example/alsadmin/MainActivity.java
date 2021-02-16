package com.example.alsadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.alsadmin.object.Management;
import com.example.alsadmin.ui.event.EventActivity;
import com.example.alsadmin.ui.event.EventViewActivity;
import com.example.alsadmin.ui.organization.OrganizationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    //toolbar
    Toolbar customizeToolbar;

    ConstraintLayout mainActivityContain;

    //long value backPressedTime for back button
    long backPressedTime;

    //Toast for back button
    Snackbar backSnackbar;

    String managementSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        customizeToolbar = findViewById(R.id.customizeToolbar);
        customizeToolbar.setNavigationIcon(R.mipmap.alslogo);
        customizeToolbar.setTitle("Admin Portal");
        setSupportActionBar(customizeToolbar);

        mainActivityContain = findViewById(R.id.mainActivityContainer);

        Intent session = getIntent();

        if(session.hasExtra(Config.MANAGEMENT_SESSION_ID) && session.hasExtra(Config.LOGGED_IN_STATE)
                && session.getStringExtra(Config.LOGGED_IN_STATE).equals("true")){
            Snackbar.make(mainActivityContain, "Login Success", Snackbar.LENGTH_LONG).show();
            managementSessionId = session.getStringExtra(Config.MANAGEMENT_SESSION_ID);
        }
    }

    @Override
    public void onBackPressed() {
        //if back pressed time more than system currenttime millies
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            //dismiss toast
            backSnackbar.dismiss();

            //quit the app
            super.onBackPressed();
            return;
        }
        else {
            //initialize the message
            backSnackbar = Snackbar.make(mainActivityContain, "Press back again to exit", Snackbar.LENGTH_LONG);

            //show the message to the user
            backSnackbar.show();
        }

        //backpressedtime = system.currentimemillies
        backPressedTime = System.currentTimeMillis();
    }

    public void startEventPage(View view) {
        startActivity(new Intent(getApplicationContext(), EventActivity.class));
    }

    public void startOrganizationPage(View view) {
        startActivity(new Intent(getApplicationContext(), OrganizationActivity.class));
    }

    public void startCreateAdminPage(View view) {
        startActivity(new Intent(getApplicationContext(), CreateAdminActivity.class));
    }

    public void logOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure want to log out?")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Config.MANAGEMENT_REF.child(managementSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Management admin = snapshot.getValue(Management.class);

                                    if(admin != null){
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);
                                        admin.setLoggedOutDateTime(currentDateTime);

                                        Map<String, Object> adminValues = admin.managementMap();

                                        Config.MANAGEMENT_REF.child(managementSessionId).updateChildren(adminValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    //log out main activity
                                                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                }
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "DatabaseError: " + error.getMessage());
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
}