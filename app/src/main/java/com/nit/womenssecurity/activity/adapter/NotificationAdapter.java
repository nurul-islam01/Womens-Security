package com.nit.womenssecurity.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.activity.NotificationActivity;
import com.nit.womenssecurity.pojos.Notifi;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder>{
    private static final String TAG = "NotificationAdapter";

    private Context context;
    private List<Notifi> notifis;
    private WSPreference preference;
    private SweetAlertDialog alertDialog;

    public NotificationAdapter(Context context, List<Notifi> notifis) {
        this.context = context;
        this.notifis = notifis;
        preference = new WSPreference(context);
        alertDialog = new SweetAlertDialog(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifi notifi = notifis.get(position);
        try {
            holder.linearLayout.setBackgroundColor(notifi.isSeen() ? context.getResources().getColor(R.color.white)  : context.getResources().getColor(R.color.gray));
            holder.titleTV.setText(notifi.getTitle());
            holder.notificationBodyTV.setText(notifi.getBody());
            holder.timeTV.setText(getDateTime(notifi.getTime()));
        }catch (Exception e){
            Log.d(TAG, "onBindViewHolder: " + e.getMessage());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastTime() < notifi.getTime()) {
                    Intent intent = new Intent(context, DangerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("notification", notifi);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                    ((NotificationActivity)context).finish();
                } else {
                    Toast.makeText(context, "Notification expire", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                alertDialog.changeAlertType(SweetAlertDialog.NORMAL_TYPE);
                alertDialog.setTitle("Are you sure delete this?");
                alertDialog.setConfirmButton("Yes", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        WSFirebase.notifications().child(notifi.getId()).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                            }
                        });

                        alertDialog.dismiss();
                    }
                }).setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    }
                }).show();
                return true;
            }
        });
    }

    private long lastTime() {
        Date date = new Date(System.currentTimeMillis() - 108000 * 1000);
        long time = date.getTime();
        return time;
    }

    private String getDateTime(long time) {
        DateFormat df = new SimpleDateFormat("HH:mm, dd MMM");
        TimeZone timeZone = TimeZone.getDefault();
        df.setTimeZone(TimeZone.getTimeZone(timeZone.getID()));
        return df.format(time);
    }

    @Override
    public int getItemCount() {
        return notifis.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView titleTV, timeTV, notificationBodyTV;
        private LinearLayout linearLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            titleTV = itemView.findViewById(R.id.titleTV);
            timeTV = itemView.findViewById(R.id.timeTV);
            notificationBodyTV = itemView.findViewById(R.id.notificationBodyTV);
        }
    }
}
