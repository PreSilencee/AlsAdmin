package com.example.alsadmin.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alsadmin.Config;
import com.example.alsadmin.Handler.GlideApp;
import com.example.alsadmin.R;
import com.example.alsadmin.object.Organization;
import com.example.alsadmin.object.User;
import com.example.alsadmin.ui.organization.OrganizationViewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OrganizationListFragmentAdapter extends RecyclerView.Adapter<OrganizationListFragmentAdapter.ViewHolder> {

    //console tag
    private static final String TAG = "OrganizationAllAdapter";

    //create an array list for Event object
    private List<Organization> organizationList;

    //create a context for the adapter
    private Context context;

    //constructor (home event list, context)
    public OrganizationListFragmentAdapter(List<Organization> organizationList, Context context) {
        this.organizationList = organizationList;
        this.context = context;
    }

    //create view for each event object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.organization_list_view,parent,false);
        return new OrganizationListFragmentAdapter.ViewHolder(view);
    }

    //attach the data of the event object
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //get position of donation
        Organization organization = organizationList.get(position);

        if(organization.getOrganizationProfileImageName() != null && organization.getUserId() != null){

            //go to the firebase storage reference
            StorageReference profileImageRef = Config.ORGANIZATION_SR.child(organization.getUserId())
                    .child("profile").child(organization.getOrganizationProfileImageName());

            //get download url
            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    //push image into image view
                    GlideApp.with(context)
                            .load(uri)
                            .placeholder(R.mipmap.loading_image)
                            .into(holder.organizationImageView);
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //show loading image view
                            holder.organizationImageView.setImageResource(R.drawable.ic_baseline_business_24);
                        }
                    });
        }
        else{
            //show loading image view
            holder.organizationImageView.setImageResource(R.drawable.ic_baseline_business_24);
        }

        if(organization.getOrganizationName() != null){
            holder.organizationListNameTV.setText(organization.getOrganizationName());
        }
        else{
            String notFound = "NULL";
            holder.organizationListNameTV.setText(notFound);
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
                                holder.organizationListRegisterDateTimeTV.setText(registerDateTime);
                            }
                            else{
                                String registerDateTime = "Register Date Time: NULL";
                                holder.organizationListRegisterDateTimeTV.setText(registerDateTime);
                            }
                        }
                        else{
                            String registerDateTime = "Register Date Time: NULL";
                            holder.organizationListRegisterDateTimeTV.setText(registerDateTime);
                        }
                    }
                    else{
                        String registerDateTime = "Register Date Time: NULL";
                        holder.organizationListRegisterDateTimeTV.setText(registerDateTime);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Database Error: " + error.getMessage());
                }
            });
        }



        if(organization.getOrganizationVerifyStatus() != null){
            holder.organizationListStatusTV.setText(organization.getOrganizationVerifyStatus());
        }
        else{
            holder.organizationListStatusTV.setText("NULL");
        }

        //donate btn
        holder.organizationListViewBtn.setOnClickListener(v -> {
            Intent i = new Intent(context, OrganizationViewActivity.class);
            i.putExtra(Config.ORGANIZATION_SESSION_ID, organization.getUserId());
            context.startActivity(i);
        });
    }

    //get the size of array list for home event list
    @Override
    public int getItemCount() {
        return organizationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        //imageview
        public CircleImageView organizationImageView;

        //text view
        public TextView organizationListNameTV, organizationListRegisterDateTimeTV, organizationListStatusTV;

        //button
        public Button organizationListViewBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //find id for image view
            organizationImageView = itemView.findViewById(R.id.organizationListProfileImageView);

            //find id for text view
            organizationListNameTV = itemView.findViewById(R.id.organizationListNameTextView);
            organizationListRegisterDateTimeTV = itemView.findViewById(R.id.organizationRegisterDateTimeTextView);
            organizationListStatusTV = itemView.findViewById(R.id.organizationListStatusTextView);

            //find id for button
            organizationListViewBtn = itemView.findViewById(R.id.organizationListViewButton);
        }
    }
}
