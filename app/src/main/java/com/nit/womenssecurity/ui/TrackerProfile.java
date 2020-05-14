package com.nit.womenssecurity.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.listener.DangerDataListener;
import com.nit.womenssecurity.listener.DangerListener;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.ui.adapter.TrackerContactAdapter;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class TrackerProfile extends Fragment {

    private static final String TAG = "TrackerProfile";
    private LatLng latLng;
    private User user = new User();
    private WSPreference preference;
    private TrackerContactAdapter adapter;
    private RecyclerView contactRC;
    private TextView noContactTV;
    private Context context;

    private TextView nameTV, emailTV, phoneTV;
    private CircleImageView personCIV;
    private CardView otherInfoCV;

    public TrackerProfile() {
        // Required empty public constructor
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_tracker_profile, container, false);
        preference = new WSPreference(context);

        contactRC = root.findViewById(R.id.contactRC);
        noContactTV = root.findViewById(R.id.noContactTV);
        nameTV = root.findViewById(R.id.nameTV);
        emailTV = root.findViewById(R.id.emailTV);
        phoneTV = root.findViewById(R.id.phoneTV);
        personCIV = root.findViewById(R.id.personCIV);
        otherInfoCV = root.findViewById(R.id.otherInfoCV);
        otherInfoCV.setVisibility(View.GONE);
        noContactTV.setVisibility(View.GONE);

        ((DangerActivity) context).setDangerListener(new DangerListener() {
            @Override
            public void trackerLocation(LatLng latLng) {

            }

            @Override
            public void trackerProfile(User user) {
                nameTV.setText(user.getFullName());
                emailTV.setText("Email : " + user.getEmail());
                phoneTV.setText("Phone : " + user.getPhone());

                WSFirebase.contacts(user.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Contact> contacts = new ArrayList<>();
                            for (DataSnapshot a: dataSnapshot.getChildren()) {
                                Contact contact = a.getValue(Contact.class);
                                contacts.add(contact);
                            }
                            if (contacts.size() > 0) {
                                adapter = new TrackerContactAdapter(context, contacts);
                                contactRC.setAdapter(adapter);
                                noContactTV.setVisibility(View.GONE);
                                otherInfoCV.setVisibility(View.VISIBLE);
                            } else {
                                noContactTV.setVisibility(View.VISIBLE);
                                otherInfoCV.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }


}
