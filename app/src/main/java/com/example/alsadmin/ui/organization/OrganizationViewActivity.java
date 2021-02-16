package com.example.alsadmin.ui.organization;

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
import com.example.alsadmin.object.User;
import com.example.alsadmin.ui.event.EventViewActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationViewActivity extends AppCompatActivity {

    //tag
    private static final String TAG = "OrganizationViewAct";

    //Connectivity
    Connectivity device;

    String organizationSessionId;

    //toolbar
    Toolbar organizationCustomizeToolbar;

    //Image View
    CircleImageView organizationViewIV;

    //Text View
    TextView organizationRegisteredDateTimeTV, organizationNameTV, organizationTypeTV,
            organizationRegistrationNumberTV, organizationEmailTV, organizationPhoneTV,
            organizationDescriptionTV, organizationAddressTV, organizationStatusTV;

    //button
    Button organizationViewVerifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_view);

        organizationCustomizeToolbar = findViewById(R.id.customizeToolbar);
        organizationCustomizeToolbar.setTitle("Organization");
        setSupportActionBar(organizationCustomizeToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        organizationViewIV = findViewById(R.id.organizationViewImageView);
        organizationRegisteredDateTimeTV = findViewById(R.id.organizationViewRegisteredDateTimeTextView);
        organizationNameTV = findViewById(R.id.organizationViewNameTextView);
        organizationTypeTV = findViewById(R.id.organizationViewTypeTextView);
        organizationRegistrationNumberTV = findViewById(R.id.organizationViewRegistrationNumberTextView);
        organizationEmailTV = findViewById(R.id.organizationViewEmailTextView);
        organizationPhoneTV = findViewById(R.id.organizationViewPhoneTextView);
        organizationDescriptionTV = findViewById(R.id.organizationViewDescriptionTextView);
        organizationAddressTV = findViewById(R.id.organizationViewAddressTextView);
        organizationStatusTV = findViewById(R.id.organizationViewStatusTextView);

        organizationViewVerifyBtn = findViewById(R.id.organizationViewVerifyButton);


        device = new Connectivity(getApplicationContext());

        Intent session = getIntent();

        if(session.hasExtra(Config.ORGANIZATION_SESSION_ID)){
            organizationSessionId = session.getStringExtra(Config.ORGANIZATION_SESSION_ID);

            if(organizationSessionId != null){
                if(device.haveNetwork()){
                    Config.ORGANIZATION_REF.child(organizationSessionId).addListenerForSingleValueEvent(organizationValueListener);
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

    private void updateOrganization(Organization organization, String status){
        String currentStatus = null;

        if(status.equals(Config.VERIFIED))
        {
            currentStatus = "verify";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(OrganizationViewActivity.this);
        builder.setMessage("Are you sure want to "+currentStatus+" this event?")
                .setCancelable(false)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        organization.setOrganizationVerifyStatus(status);

                        Map<String, Object> organizationValues = organization.organizationMap();

                        Config.ORGANIZATION_REF.child(organizationSessionId).updateChildren(organizationValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    Config.ORGANIZATION_REF.child(organizationSessionId).addListenerForSingleValueEvent(organizationValueListener);
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

    private final ValueEventListener organizationValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if(snapshot.exists()){
                Organization organization = snapshot.getValue(Organization.class);

                if(organization != null){
                    if(organization.getOrganizationProfileImageName() != null && organization.getUserId() != null){

                        //go to the firebase storage reference
                        StorageReference profileImageRef = Config.ORGANIZATION_SR.child(organization.getUserId())
                                .child("profile").child(organization.getOrganizationProfileImageName());

                        //get download url
                        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //push image into image view
                                GlideApp.with(getApplicationContext())
                                        .load(uri)
                                        .placeholder(R.mipmap.loading_image)
                                        .into(organizationViewIV);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //show loading image view
                                        organizationViewIV.setImageResource(R.drawable.ic_baseline_business_24);
                                    }
                                });
                    }
                    else{
                        //show loading image view
                        organizationViewIV.setImageResource(R.drawable.ic_baseline_business_24);
                    }

                    if(organization.getUserId() != null){
                        Config.USER_REF.child(organization.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    User user = snapshot.getValue(User.class);

                                    if(user != null){
                                        if(user.getRegisterDateTime() != null){
                                            String registerDateTime = "Register Date Time: "+user.getRegisterDateTime();
                                            organizationRegisteredDateTimeTV.setText(registerDateTime);
                                        }
                                        else{
                                            String registerDateTime = "Register Date Time: NULL";
                                            organizationRegisteredDateTimeTV.setText(registerDateTime);
                                        }
                                    }
                                    else{
                                        String registerDateTime = "Register Date Time: NULL";
                                        organizationRegisteredDateTimeTV.setText(registerDateTime);
                                    }
                                }
                                else{
                                    String registerDateTime = "Register Date Time: NULL";
                                    organizationRegisteredDateTimeTV.setText(registerDateTime);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "Database Error: " + error.getMessage());
                            }
                        });
                    }

                    if(organization.getOrganizationName() != null){
                        String name = "Name: " + organization.getOrganizationName();
                        organizationNameTV.setText(name);
                    }
                    else{
                        String name = "Name: NULL";
                        organizationNameTV.setText(name);
                    }

                    if(organization.getOrganizationType() != null){
                        String type = "Type: " + organization.getOrganizationType();
                        organizationTypeTV.setText(type);
                    }
                    else{
                        String type = "Type: NULL";
                        organizationTypeTV.setText(type);
                    }

                    if(organization.getOrganizationRegistrationNumber() != null){
                        String registrationNumber = "Registration Number: " + organization.getOrganizationRegistrationNumber();
                        organizationRegistrationNumberTV.setText(registrationNumber);
                    }
                    else{
                        String registrationNumber = "Registration Number: NULL";
                        organizationRegistrationNumberTV.setText(registrationNumber);
                    }

                    if(organization.getOrganizationEmail() != null){
                        String email = "Email: " + organization.getOrganizationEmail();
                        organizationEmailTV.setText(email);
                    }
                    else{
                        String email = "Email: NULL";
                        organizationEmailTV.setText(email);
                    }

                    if(organization.getOrganizationPhone() != null){
                        String phone = "Phone: " + organization.getOrganizationPhone();
                        organizationPhoneTV.setText(phone);
                    }
                    else{
                        String phone = "Phone: NULL";
                        organizationPhoneTV.setText(phone);
                    }

                    if(organization.getOrganizationDescription() != null){
                        String description = "Description: " + organization.getOrganizationDescription();
                        organizationDescriptionTV.setText(description);
                    }
                    else{
                        String description = "Description: NULL" + organization.getOrganizationDescription();
                        organizationDescriptionTV.setText(description);
                    }

                    if(organization.getOrganizationAddress() != null){
                        String address = "Address: " + organization.getOrganizationAddress();
                        organizationAddressTV.setText(address);
                    }
                    else{
                        String address = "Address: NULL";
                        organizationAddressTV.setText(address);
                    }

                    if(organization.getOrganizationVerifyStatus() != null){
                        organizationStatusTV.setText(organization.getOrganizationVerifyStatus());
                    }
                    else{
                        String status = "NULL";
                        organizationStatusTV.setText(status);
                    }

                    if(organizationStatusTV.getText().equals(Config.PENDING)){
                        organizationViewVerifyBtn.setVisibility(View.VISIBLE);

                        organizationViewVerifyBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateOrganization(organization, Config.VERIFIED);
                            }
                        });
                    }
                    else{
                        organizationViewVerifyBtn.setVisibility(View.GONE);
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