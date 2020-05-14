package com.nit.womenssecurity.ui.profile;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.pojos.Contact;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.ui.adapter.ContactAdapter;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private Context context;

    private TextView addContactTV;
    private SweetAlertDialog alertDialog;
    private WSPreference preference;
    private User mainUser;
    private ContactAdapter adapter;
    private RecyclerView contactRC;
    private TextView noContactTV;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        alertDialog = new SweetAlertDialog(context);
        preference = new WSPreference(context);
        mainUser = preference.getUser();

        addContactTV = root.findViewById(R.id.addContactTV);
        contactRC = root.findViewById(R.id.contactRC);
        noContactTV = root.findViewById(R.id.noContactTV);
        noContactTV.setVisibility(View.GONE);


        addContactTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.add_contact_layout, null);
                EditText nameET = view.findViewById(R.id.nameET);
                EditText numberEt = view.findViewById(R.id.numberET);

                alertDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                alertDialog.setCustomView(view);
                alertDialog.setTitle("Add Family Contact");
                alertDialog.setConfirmButton("Save", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                        String name = nameET.getText().toString().trim();
                        String number = numberEt.getText().toString().trim();
                        String pushKey = WSFirebase.contacts(mainUser.getId()).push().getKey();
                        Contact contact = new Contact(pushKey, name, number);
                        WSFirebase.contacts(mainUser.getId()).child(pushKey).setValue(contact)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Toast.makeText(context, "Error saving", Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.dismiss();
                    }
                }).setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).show();
            }
        });

        familyContacts();

        return root;
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
    }

    private void familyContacts() {
        WSFirebase.contacts(mainUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Contact> contacts = new ArrayList<>();
                    for (DataSnapshot a: dataSnapshot.getChildren()) {
                        Contact contact = a.getValue(Contact.class);
                        contacts.add(contact);
                    }
                    if (contacts.size() > 0) {
                        preference.saveContacts(contacts);
                        adapter = new ContactAdapter(context, contacts);
                        contactRC.setAdapter(adapter);
                        noContactTV.setVisibility(View.GONE);
                    } else {
                        noContactTV.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
