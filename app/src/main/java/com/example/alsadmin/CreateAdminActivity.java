package com.example.alsadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alsadmin.Handler.AESCrypt;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.object.Management;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateAdminActivity extends AppCompatActivity {

    //tag
    public static final String TAG = "CreateAdminActivity";
    //toolbar
    Toolbar createAdminCustomizeToolbar;

    Connectivity device;
    //text input layout for username, password, confirm password
    TextInputLayout createAdminUsernameTV, createAdminPasswordTV, createAdminConfirmPasswordTV;

    Spinner createAdminSpinner;

    String mType;

    ConstraintLayout createAdminContain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_admin);

        device = new Connectivity(getApplicationContext());

        createAdminCustomizeToolbar = findViewById(R.id.customizeToolbar);
        createAdminCustomizeToolbar.setTitle("Create Admin");
        setSupportActionBar(createAdminCustomizeToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        createAdminContain = findViewById(R.id.createAdminContainer);
        createAdminUsernameTV = findViewById(R.id.createAdminUsername);
        createAdminPasswordTV = findViewById(R.id.createAdminPassword);
        createAdminConfirmPasswordTV = findViewById(R.id.createAdminConfirmPassword);
        createAdminSpinner = findViewById(R.id.createAdminSpinner);

        //create an array list that store the permission
        List<String> managementType = new ArrayList<>();
        managementType.add("--Select Permission--");
        managementType.add("ADMIN");

        //create array adapter for spinner
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, managementType){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                //if list == "--Select Organization Type--"
                if(position == 0)
                {
                    tv.setTextColor(Color.GRAY);
                }
                else
                {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }
        };

        //set drop down resources
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //set adapter to spinner
        createAdminSpinner.setAdapter(myAdapter);

        createAdminSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if current position not "--Select Organization Type--"
                if(position > 0)
                {
                    mType = (String) parent.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void registerAdmin(View view) {
        if(!validateTILField(createAdminUsernameTV) | !validatePassword(createAdminPasswordTV)
                | !validateConfirmPassword(createAdminPasswordTV, createAdminConfirmPasswordTV)
                | !validatePermissionType()){
            return;
        }

        if(!device.haveNetwork()){
            Toast.makeText(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{
            //a progress dialog to view progress of create account
            final ProgressDialog progressDialog = new ProgressDialog(CreateAdminActivity.this);

            //set message for progress dialog
            progressDialog.setMessage("One moment...");

            //show dialog
            progressDialog.show();

            String username = createAdminUsernameTV.getEditText().getText().toString().trim();
            String password = null;
            try{
                password = AESCrypt.encrypt(createAdminPasswordTV.getEditText().getText().toString().trim());
            }
            catch (Exception e){
                Log.d(TAG, "Encrypt error: "+ e.getMessage());
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US);
            Date dateObj = Calendar.getInstance().getTime();
            final String currentDateTime = simpleDateFormat.format(dateObj);

            Management management = new Management();
            management.setUsername(username);
            management.setPassword(password);
            management.setRole(mType);
            management.setRegisterDateTime(currentDateTime);

            Config.MANAGEMENT_REF.push().setValue(management).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Snackbar.make(createAdminContain, "Success", Snackbar.LENGTH_LONG).show();
                        clearField();
                    }
                    else{
                        progressDialog.dismiss();
                        Snackbar.make(createAdminContain, "Failed", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    //validate organizationType
    private boolean validatePermissionType() {
        //if selectedType == null
        if(mType == null){
            Snackbar.make(createAdminContain, "Please select the permission", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
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

    //validate password
    public boolean validatePassword(TextInputLayout passwordTIL) {
        //get password
        String passwordInput = passwordTIL.getEditText().getText().toString().trim();

        //if password == null
        if (passwordInput.isEmpty()) {
            passwordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        //if password less than 6 character or have white space
        else if (!Config.PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            passwordTIL.getEditText().setError("The length of password must have at least 6 character");
            return false;
        } else {
            return true;
        }
    }

    //validate confirm password
    public boolean validateConfirmPassword(TextInputLayout passwordTIL, TextInputLayout confirmPasswordTIL) {
        //get password
        String passwordInput = passwordTIL.getEditText().getText().toString().trim();
        //get confirm password
        String confirmPasswordInput = confirmPasswordTIL.getEditText().getText().toString().trim();

        //if confirm password == null
        if(confirmPasswordInput.isEmpty()){
            confirmPasswordTIL.getEditText().setError("Field can't be empty");
            return false;
        }
        // if confirm password != password
        else if(!confirmPasswordInput.equals(passwordInput)){
            confirmPasswordTIL.getEditText().setError("Passwords are not same");
            return false;
        }
        else{
            return true;
        }
    }

    //clear fields that input by user
    private void clearField() {
        createAdminUsernameTV.getEditText().getText().clear();
        createAdminPasswordTV.getEditText().getText().clear();
        createAdminConfirmPasswordTV.getEditText().getText().clear();
        createAdminSpinner.setSelection(0);
    }
}