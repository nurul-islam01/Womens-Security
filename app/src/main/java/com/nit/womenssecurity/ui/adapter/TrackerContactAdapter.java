package com.nit.womenssecurity.ui.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.Contact;

import java.util.List;

public class TrackerContactAdapter extends RecyclerView.Adapter<TrackerContactAdapter.ViewHolder> {
    private static final String TAG = "ContactAdapter";
    private Context context;
    private List<Contact> contacts;

    public TrackerContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        try {
            holder.nameTV.setText(contact.getName());
            holder.numberTV.setText(contact.getNumber());

        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: " + e.getMessage());
        }

        holder.callIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Toast.makeText(context, "Calling....", Toast.LENGTH_SHORT).show();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + contact.getNumber()));
                    context.startActivity(callIntent);
                }
            }
        });
    }


    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, 222);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTV, numberTV;
        private ImageView callIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTV = itemView.findViewById(R.id.nameTV);
            numberTV = itemView.findViewById(R.id.numberTV);
            callIV = itemView.findViewById(R.id.callIV);

        }
    }
}