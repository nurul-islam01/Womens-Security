package com.nit.womenssecurity.ui;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.activity.DangerActivity;
import com.nit.womenssecurity.listener.DangerDataListener;
import com.nit.womenssecurity.listener.DangerListener;
import com.nit.womenssecurity.pojos.User;

public class LocationFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationChangeListener, DangerDataListener {

    private static final String TAG = "LocationFragment";

    private LatLng latLng;
    private User user;
    private MapView mMapView;
    private GoogleMap googleMap;
    private Context context;
    private TextView distanceTV;

    public LocationFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_location, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapView);
        distanceTV =  root.findViewById(R.id.distanceTV);
        distanceTV.setVisibility(View.GONE);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        ((DangerActivity)context).setDangerDataListener(this);

        mMapView.getMapAsync(this);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.setOnMyLocationChangeListener(this);

        if (this.googleMap != null && this.latLng != null && this.user != null) {
            addMarker();
        }

        Log.d(TAG, "onMapReady: ");
    }

    private void addMarker() {
        googleMap.addMarker(new MarkerOptions().position(latLng)
                .title(user.getFullName()));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( latLng, 15f);
        googleMap.animateCamera(cameraUpdate);
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMyLocationChange(Location location) {
        if (latLng != null) {
            int distance = distanceTo(location.getLatitude(), location.getLongitude(), latLng.latitude, latLng.longitude);
            distanceTV.setText("Distance " + distance + " meter");
            distanceTV.setVisibility(View.VISIBLE);
        }
    }

    private int distanceTo(double currentlatitude, double currentlongitude, double originLat, double originLon) {
        float[] results = new float[1];
        Location.distanceBetween(currentlatitude, currentlongitude, originLat, originLon, results);
        float distanceInMeters = results[0];
        return (int) distanceInMeters;
    }

    @Override
    public void trackerProfile(User user) {
        this.user = user;
        if (this.googleMap != null && this.latLng != null && this.user != null) {
            addMarker();
        }
    }

    @Override
    public void trackerLocation(LatLng latLng) {
        this.latLng = latLng;
        if (this.googleMap != null && this.latLng != null && this.user != null) {
            this.googleMap.clear();
            addMarker();
        }
    }
}
