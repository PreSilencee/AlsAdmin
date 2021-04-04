package com.example.alsadmin.ui.donation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.Connectivity;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Contributor;
import com.example.alsadmin.object.Donation;
import com.example.alsadmin.object.Organization;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class DonationDetailsActivity extends AppCompatActivity {

    Connectivity device;
    WebView donationWebView;
    Button downloadAsPdfBtn;

    String donationSessionId;
    Toolbar donationDetailsCustomizeToolbar;

    String[] separatedDateAndTime;
    String donationAmount;
    String status;
    String eventId;
    String name;
    String paymentMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_details);

        device = new Connectivity(this);

        if(!device.haveNetwork()){
            Toast.makeText(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
        else{
            donationWebView = findViewById(R.id.donationDetailsWebView);
            downloadAsPdfBtn = findViewById(R.id.downloadAsPdfButton);
            donationDetailsCustomizeToolbar = findViewById(R.id.customizeToolbar);
            setSupportActionBar(donationDetailsCustomizeToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Donation Details");

            initialize();

            downloadAsPdfBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = DonationDetailsActivity.this;
                    String documentName=donationSessionId +"Document";
                    PrintManager printManager=(PrintManager)DonationDetailsActivity.this.getSystemService(context.PRINT_SERVICE);
                    PrintDocumentAdapter adapter = donationWebView.createPrintDocumentAdapter(documentName);
                    printManager.print(donationSessionId, adapter, new PrintAttributes.Builder().build());

                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initialize(){
        Intent session = getIntent();

        if(session.hasExtra(Config.DONATION_SESSION_ID)){
            donationSessionId = session.getStringExtra(Config.DONATION_SESSION_ID);
        }
        else{
            Toast.makeText(getApplicationContext(), "Session is expired. Please try again. ", Toast.LENGTH_LONG).show();
            finish();
        }

        if(donationSessionId != null){
            Config.DONATION_REF.child(donationSessionId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){

                        final Donation donation = snapshot.getValue(Donation.class);

                        if(donation != null){
                            if(donation.getDonationDateTime() != null){
                                //separate the date and time
                                separatedDateAndTime = donation.getDonationDateTime().split(" ");
                            }

                            if(donation.getDonationAmount() != 0){
                                donationAmount = String.valueOf(donation.getDonationAmount());
                            }

                            if(donation.getDonationStatus() != null){
                                status = donation.getDonationStatus();
                            }

                            if(donation.getDonationEventId() != null){
                                eventId = donation.getDonationEventId();
                            }

                            if(donation.getDonationPaymentMethod() != null){
                                paymentMethod = donation.getDonationPaymentMethod();
                            }

                            if(donation.getDonationUserId() != null){
                                Config.CONTRIBUTOR_REF.child(donation.getDonationUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.exists()){
                                            Contributor contributor = snapshot.getValue(Contributor.class);

                                            if(contributor != null){
                                                if(contributor.getName() != null){
                                                    name = contributor.getName();
                                                }

                                                String html = "<!DOCTYPE html>\n" +
                                                        "<html lang=\"en\">\n" +
                                                        "<head>\n" +
                                                        "    <meta charset=\"UTF-8\">\n" +
                                                        "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                                                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                                        "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                                                        "    <title>Document</title>\n" +
                                                        "</head>\n" +
                                                        "<body>\n" +
                                                        "    <div class=\"container\">\n" +
                                                        "      <table>\n" +
                                                        "        <tr>\n" +
                                                        "          <th> <img src=\"file:android_asset/AlittleShare-1.png\" class=\"img-fluid\" alt=\"Responsive image\" width=\"50px\" height=\"50px\"></th>\n" +
                                                        "          <th> <h3>AlittleShare</h3> </th>\n" +
                                                        "        </tr>\n" +
                                                        "      </table>\n" +
                                                        "        <h5>Donation Receipt</h5>\n" +
                                                        "        <table class=\"table\">\n" +
                                                        "          <thead>\n" +
                                                        "            <tr>\n" +
                                                        "              <td>Invoice Number: <strong>"+donationSessionId+"</strong></td>\n" +
                                                        "              <td>Date: <strong>"+separatedDateAndTime[0]+"</strong></td>\n" +
                                                        "            </tr>\n" +
                                                        "            <tr>\n" +
                                                        "              <td>Donated By: <strong>"+name+"</strong></td>\n" +
                                                        "              <td>Payment Method: <strong>"+paymentMethod+"</strong></td>\n" +
                                                        "            </tr>\n" +
                                                        "          </thead>\n" +
                                                        "        </table>\n" +
                                                        "        <table class=\"table\">\n" +
                                                        "            <thead>\n" +
                                                        "              <tr>\n" +
                                                        "                <th scope=\"col\">Description</th>\n" +
                                                        "                <th scope=\"col\">Amount (RM)</th>\n" +
                                                        "              </tr>\n" +
                                                        "            </thead>\n" +
                                                        "            <tbody>\n" +
                                                        "              <tr>\n" +
                                                        "                <td id=\"description\">Donation for "+eventId+"</td>\n" +
                                                        "                <td id=\"amount\">"+donationAmount+"</td>\n" +
                                                        "              </tr>\n" +
                                                        "            </tbody>\n" +
                                                        "          </table>\n" +
                                                        "          <h6>Thank you for your generosity. We appreciate your support!</h6>\n" +
                                                        "    </div>\n" +
                                                        "    \n" +
                                                        "</body>\n" +
                                                        "</html>";

                                                donationWebView.loadDataWithBaseURL(null,html,"text/html","utf-8",null);
                                            }
                                        }
                                        else{
                                            Config.ORGANIZATION_REF.child(donation.getDonationUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if(snapshot.exists()){
                                                        Organization organization = snapshot.getValue(Organization.class);

                                                        if(organization != null){
                                                            if(organization.getOrganizationName() != null){
                                                                name = organization.getOrganizationName();
                                                            }

                                                            String html = "<!DOCTYPE html>\n" +
                                                                    "<html lang=\"en\">\n" +
                                                                    "<head>\n" +
                                                                    "    <meta charset=\"UTF-8\">\n" +
                                                                    "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                                                                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                                                                    "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.0-beta3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n" +
                                                                    "    <title>Document</title>\n" +
                                                                    "</head>\n" +
                                                                    "<body>\n" +
                                                                    "    <div class=\"container\">\n" +
                                                                    "      <table>\n" +
                                                                    "        <tr>\n" +
                                                                    "          <th> <img src=\"file:android_asset/AlittleShare-1.png\" class=\"img-fluid\" alt=\"Responsive image\" width=\"50px\" height=\"50px\"></th>\n" +
                                                                    "          <th> <h3>AlittleShare</h3> </th>\n" +
                                                                    "        </tr>\n" +
                                                                    "      </table>\n" +
                                                                    "        <h5>Donation Receipt</h5>\n" +
                                                                    "        <table class=\"table\">\n" +
                                                                    "          <thead>\n" +
                                                                    "            <tr>\n" +
                                                                    "              <td>Invoice Number: <strong>"+donationSessionId+"</strong></td>\n" +
                                                                    "              <td>Date: <strong>"+separatedDateAndTime[0]+"</strong></td>\n" +
                                                                    "            </tr>\n" +
                                                                    "            <tr>\n" +
                                                                    "              <td>Donated By: <strong>"+name+"</strong></td>\n" +
                                                                    "              <td>Payment Method: <strong>"+paymentMethod+"</strong></td>\n" +
                                                                    "            </tr>\n" +
                                                                    "          </thead>\n" +
                                                                    "        </table>\n" +
                                                                    "        <table class=\"table\">\n" +
                                                                    "            <thead>\n" +
                                                                    "              <tr>\n" +
                                                                    "                <th scope=\"col\">Description</th>\n" +
                                                                    "                <th scope=\"col\">Amount (RM)</th>\n" +
                                                                    "              </tr>\n" +
                                                                    "            </thead>\n" +
                                                                    "            <tbody>\n" +
                                                                    "              <tr>\n" +
                                                                    "                <td id=\"description\">Donation for "+eventId+"</td>\n" +
                                                                    "                <td id=\"amount\">"+donationAmount+"</td>\n" +
                                                                    "              </tr>\n" +
                                                                    "            </tbody>\n" +
                                                                    "          </table>\n" +
                                                                    "          <h6>Thank you for your generosity. We appreciate your support!</h6>\n" +
                                                                    "    </div>\n" +
                                                                    "    \n" +
                                                                    "</body>\n" +
                                                                    "</html>";

                                                            donationWebView.loadDataWithBaseURL(null,html,"text/html","utf-8",null);
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    //
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        //
                                    }
                                });
                            }


                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("DonationDetails", "DatabaseError: " + error.getMessage());
                }
            });
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!device.haveNetwork()){
            Toast.makeText(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!device.haveNetwork()){
            Toast.makeText(getApplicationContext(),device.NetworkError(), Toast.LENGTH_LONG).show();
        }
    }
}