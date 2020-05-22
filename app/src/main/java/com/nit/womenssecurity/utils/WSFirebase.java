package com.nit.womenssecurity.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class WSFirebase {

    private static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

    private static FirebaseStorage getStorageRef() {
        return FirebaseStorage.getInstance();
    }

    private static DatabaseReference reference() {
        return getDatabase().getReference();
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static DatabaseReference user() {
        return reference().child("USERS");
    }

    public static DatabaseReference userLocation() { return reference().child("USERS_LOCATION");}

    public static DatabaseReference userToken() { return reference().child("USERS_TOKEN");}

    public static DatabaseReference notifications() { return reference().child("NOTIFICATIONS");}

    public static DatabaseReference contacts(String userId) {
        return user().child(userId).child("CONTACTS");
    }

    public static StorageReference photoSaveOnStorage() {
       return getStorageRef().getReference().child("profile");
    }

}
