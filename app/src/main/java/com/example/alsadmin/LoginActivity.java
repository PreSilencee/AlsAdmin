package com.example.alsadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.alsadmin.Handler.AESCrypt;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.object.Management;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout loginUsername, loginPassword;
    ConstraintLayout loginConstraintLayout;
    Connectivity device;
    Boolean loginStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        device = new Connectivity(this);
        loginConstraintLayout = findViewById(R.id.loginConstraintLayout);
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
    }

    //validate field
    public boolean validateTILField(TextInputLayout textInputLayout){
        //get user input
        String userInput = textInputLayout.getEditText().getText().toString().trim();

        //if user input == null
        if(userInput.isEmpty())
        {
            textInputLayout.getEditText().setError("Field can't be empty");
            return false;
        }
        else{
            return true;
        }
    }

    public void login(View view) {
        //startActivity(new Intent(getApplicationContext(), MainActivity.class));
        if(!validateTILField(loginUsername) | !validateTILField(loginPassword)){
            return;
        }

        if(device.haveNetwork()){
            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("Signing In...");

            //show dialog
            progressDialog.show();


            String username = loginUsername.getEditText().getText().toString().trim();
            String password = loginPassword.getEditText().getText().toString().trim();

            Config.MANAGEMENT_REF.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Management admin = dataSnapshot.getValue(Management.class);
                        if(admin != null){

                            if(admin.getUsername() != null && admin.getPassword() != null){
                                try{
                                    String dataPassword = AESCrypt.decrypt(admin.getPassword());

                                    if(username.equals(admin.getUsername()) && dataPassword.equals(password)){

                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
                                        Date dateObj = Calendar.getInstance().getTime();
                                        final String currentDateTime = simpleDateFormat.format(dateObj);

                                        admin.setLoggedInDateTime(currentDateTime);

                                        Map<String, Object> adminValues = admin.managementMap();

                                        Config.MANAGEMENT_REF.child(dataSnapshot.getKey()).updateChildren(adminValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                    //log into main activity
                                                    Intent login = new Intent(getApplicationContext(), MainActivity.class);
                                                    login.putExtra(Config.LOGGED_IN_STATE, "true");
                                                    login.putExtra(Config.MANAGEMENT_SESSION_ID, dataSnapshot.getKey());
                                                    login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(login);
                                                    finish();
                                                }
                                                else{
                                                    progressDialog.dismiss();
                                                    Snackbar.make(loginConstraintLayout, "Something went wrong", Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Snackbar.make(loginConstraintLayout, "Invalid username or password !", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                                catch (Exception e){
                                    Log.d("LoginActivity", e.getMessage());
                                }
                            }

                        }
//                            else{
//                                progressDialog.dismiss();
//                                Snackbar.make(loginConstraintLayout, "Invalid username or password !", Snackbar.LENGTH_LONG).show();
//                            }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("LoginActivity", error.getMessage());
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), device.NetworkError(), Toast.LENGTH_LONG).show();
        }

    }
}