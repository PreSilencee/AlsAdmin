package com.example.alsadmin;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.regex.Pattern;

public class Config {
    //password pattern
    public static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=\\S+$)" + //no white spaces
                    ".{6,}" +     //at least 6 character
                    "$");
    public static final DatabaseReference MANAGEMENT_REF = FirebaseDatabase.getInstance().getReference().child("management");
    public static final DatabaseReference EVENT_REF = FirebaseDatabase.getInstance().getReference().child("event");
    public static final DatabaseReference CONTRIBUTOR_REF = FirebaseDatabase.getInstance().getReference().child("contributor");
    public static final DatabaseReference ORGANIZATION_REF = FirebaseDatabase.getInstance().getReference().child("organization");
    public static final DatabaseReference USER_REF = FirebaseDatabase.getInstance().getReference().child("user");

    public static final StorageReference EVENT_SR = FirebaseStorage.getInstance().getReference("event");
    public static final StorageReference ORGANIZATION_SR = FirebaseStorage.getInstance().getReference("organization");
    public static final String EVENT_SESSION_ID = "eventSessionId";
    public static final String PENDING = "PENDING";
    public static final String AVAILABLE ="AVAILABLE";
    public static final String DECLINED = "DECLINED";
    public static final String VERIFIED = "VERIFIED";

    public static final String ORGANIZATION_SESSION_ID = "organizationSessionId";
    public static final String LOGGED_IN_STATE = "loggedIn";
    public static final String MANAGEMENT_SESSION_ID = "managementSessionId";

    //use for AESCrypt
    public static final String ALGORITHM = "AES";
    public static final byte[] encryptionKey = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
}
